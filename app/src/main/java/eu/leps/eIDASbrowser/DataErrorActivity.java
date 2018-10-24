package eu.leps.eIDASbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class DataErrorActivity extends Activity {

	private String mError;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Quitamos la barra del título
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(eu.leps.eIDASbrowser.R.layout.data_error);

		Context myContext = DataErrorActivity.this;

		// Ajustamos el tipo de letra de textos
		Typeface typeface = Typeface.createFromAsset(myContext.getAssets(),"fonts/HelveticaNeue.ttf");
		((TextView)findViewById(eu.leps.eIDASbrowser.R.id.result1)).setTypeface(typeface);
		((TextView)findViewById(eu.leps.eIDASbrowser.R.id.resultinfo)).setTypeface(typeface);

		Bundle extras = getIntent().getExtras();
		if(extras != null) {
			// Leemos el código de error que vamos a utilizar
			mError = extras.getString("ERROR_MSG");
			TextView tvloc = (TextView) findViewById(eu.leps.eIDASbrowser.R.id.resultinfo);
			tvloc.setText(mError);
		}

		///////////////////////////////////////////////////////////////////////////////////
		// Botón de vuelta al Activity anterior
		Button btnNFCBack = (Button)findViewById(eu.leps.eIDASbrowser.R.id.butVolver);
		btnNFCBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// Volvemos al activity de selección de documento
				Intent intent = new Intent(DataErrorActivity.this, DNIeCanSelection.class);
				startActivity(intent);
			}
		});

		///////////////////////////////////////////////////////////////////////////////////
		// Botón de configuración
		Button btnLectura = (Button)findViewById(eu.leps.eIDASbrowser.R.id.butConfigurar);
		btnLectura.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				// Lanzamos el activity de configuración
				Intent intent = new Intent(DataErrorActivity.this, DataConfiguration.class);
				startActivityForResult(intent, 1);
			}
		});
	}
}