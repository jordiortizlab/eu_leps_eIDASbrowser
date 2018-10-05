package eu.leps.eIDASbrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class DNIeLectura extends Activity {

	private boolean doubleBackToExitPressedOnce = false;

	public final static String SETTING_READ_DG1 = "read_DG_1";
	public final static String SETTING_READ_DG2 = "read_DG_2";
	public final static String SETTING_READ_DG7 = "read_DG_7";
	public final static String SETTING_READ_DG11 = "read_DG_11";

	//
	public void ExecuteOption1 (View view)
	{
		//Creamos el Intent correspondiente
		Intent intent = new Intent(DNIeLectura.this, DataConfiguration.class);
		startActivityForResult(intent, 1);
	}

	//
//	public void ExecuteOption2 (View view)
//	{
//		//Creamos el Intent correspondiente
//		Intent intent = new Intent(DNIeLectura.this, DNIeHelp.class);
//		startActivityForResult(intent, 1);
//	}

	//
	public void ExecuteOption3 (View view)
	{
		//Creamos el Intent correspondiente
		Intent intent = new Intent(DNIeLectura.this, DNIeCanSelection.class);
		startActivityForResult(intent, 1);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Quitamos la barra del título
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(eu.leps.eIDASbrowser.R.layout.main);

		// Indicamos que la aplicación ha empezado bien
		((MyAppDNIELECTURA)getApplicationContext()).setStarted(true);
	}

	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce) {
			this.moveTaskToBack(true);
			return;
		}
		this.doubleBackToExitPressedOnce = true;
		Toast.makeText(this, "Pulse de nuevo VOLVER para salir...", Toast.LENGTH_SHORT).show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				doubleBackToExitPressedOnce=false;
			}
		}, 2000);
	}
}