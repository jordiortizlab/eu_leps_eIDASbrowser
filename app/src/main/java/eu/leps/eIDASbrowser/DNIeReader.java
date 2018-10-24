package eu.leps.eIDASbrowser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class DNIeReader extends Activity {

    public static final String ACTION_READ = "ACTION_READ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);  
        setContentView(eu.leps.eIDASbrowser.R.layout.dnie_00);
    	    	    	    	             
        // Si no hemos abierto correctamente, salimos
        if(!((MyAppDNIELECTURA)getApplicationContext()).isStarted())
        {
        	// Desactivamos la activity ENABLE = false
        	PackageManager packman = getApplicationContext().getPackageManager();
        	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
        	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
      
        	android.os.Process.killProcess(android.os.Process.myPid());
	     	System.exit(0);
	     	
        	return;
        }
        
		// Activamos la activity ENABLE = true
    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

		// Ajustamos tipo de letra
		Typeface fontType = Typeface.createFromAsset(DNIeReader.this.getAssets(), "fonts/HelveticaNeue.ttf");
		((TextView)findViewById(eu.leps.eIDASbrowser.R.id.textoproceso)).setTypeface(fontType);

		///////////////////////////////////////////////////////////////////////////////////
		// Botón 1: Volver
		final Button btnSolicitar = (Button)findViewById(eu.leps.eIDASbrowser.R.id.butVolver);
		btnSolicitar.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {

				// Volvemos a la activity anterior
        		onBackPressed();
			}
		});

		///////////////////////////////////////////////////////////////////////////////////
		// Botón 2: Configurar
		final Button btnConfigurar = (Button)findViewById(eu.leps.eIDASbrowser.R.id.butConfigurar);
		btnConfigurar.setOnClickListener(new OnClickListener() 
		{
			public void onClick(View v) {
				//Creamos el Intent correspondiente
				Intent intent = new Intent(DNIeReader.this, DataConfiguration.class);
            	startActivityForResult(intent,1);
			}
		});
     }
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		// Activamos la activity ENABLE = true
    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
	}
    
    @Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		
		// Desactivamos la activity ENABLE = false
    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);//*/
	}
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		// Desactivamos la activity ENABLE = false
    	PackageManager packman = getApplicationContext().getPackageManager();
    	ComponentName componentName = new ComponentName(getApplicationContext(), NFCOperationsEnc.class);
    	packman.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);//*/
	}
}