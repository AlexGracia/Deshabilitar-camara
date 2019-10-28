package org.alexgracia.android.deshabilitarcamara;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Clase principal que ejecuta la app.
 *
 * @author Alex Gracia
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    // Declarar variables
    public static final int DPM_ACTIVATION_REQUEST_CODE = 100;
    private Switch swDeshabilitarCamara;
    private DevicePolicyManager dPM;
    private ComponentName adminComponent;
    private boolean booCancelarDesinstalacionApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar variables
        swDeshabilitarCamara = findViewById(R.id.switch1);
        dPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(getPackageName(), getPackageName() + ".DeviceAdministrator");
        booCancelarDesinstalacionApp = false;

        // Inicializar switch dependiendo del estado de la camara (On/Off)
        swDeshabilitarCamara.setChecked(dPM.getCameraDisabled(adminComponent));

        // Solicitar la activacion del administrador del dispositivo si no esta habilitado.
        activarAdmin();

    }

    /**
     * Metodo para deshabilitar/habilitar la camara.
     *
     * @param view
     */
    public void deshabilitarCamara(View view) {
        try {
            dPM.setCameraDisabled(adminComponent, swDeshabilitarCamara.isChecked());
        } catch (SecurityException securityException) {
            // Reiniciar app
            finish();
            startActivity(getIntent());
            overridePendingTransition(0, 0);
            Log.i("Device Administrator", "Un error ha ocurrido al deshabilitar/habilitar la camara - " + securityException.getMessage());
        }
    }

    /**
     * Metodo para desinstalar la app.
     * Pasos:
     * 1. Quitar administrador del dispositivo
     * 2. Desinstalar app
     *
     * @param view
     */
    public void desinstalarApp(View view) {
        booCancelarDesinstalacionApp = true;
        swDeshabilitarCamara.setChecked(false);

        // 1. Quitar administrador
        dPM.removeActiveAdmin(adminComponent);

        // 2. Desinstalar app
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + this.getClass().getPackage().getName()));
        startActivity(intent);

    }

    /**
     * Metodo para solicitar la activacion del administrador del dispositivo.
     */
    private void activarAdmin() {
        if (!dPM.isAdminActive(adminComponent)) {
            Intent activateDeviceAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            startActivityForResult(activateDeviceAdmin, DPM_ACTIVATION_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (booCancelarDesinstalacionApp) {
            booCancelarDesinstalacionApp = false;
            // Activar administracion del dispositivo
            activarAdmin();
        }
    }
}
