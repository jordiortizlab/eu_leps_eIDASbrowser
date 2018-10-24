package eu.leps.eIDASbrowser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class DataConfiguration extends Activity {

    private Context myContext;

    private SharedPreferences sharedPreferences;

    void updateUserData()
    {
        // Actualizamos los valores mostrados para cuenta y contraseña
        sharedPreferences = getApplicationContext().getSharedPreferences("com.sp.main_preferences", Context.MODE_PRIVATE);

        // Actualizamos los checkBox con los valores recuperados
        ((CheckBox)findViewById(R.id.checkBoxDG1)).setChecked(sharedPreferences.getBoolean(DNIeLectura.SETTING_READ_DG1, true));
        ((CheckBox)findViewById(R.id.checkBoxDG2)).setChecked(sharedPreferences.getBoolean(DNIeLectura.SETTING_READ_DG2, true));
        ((CheckBox)findViewById(R.id.checkBoxDG7)).setChecked(sharedPreferences.getBoolean(DNIeLectura.SETTING_READ_DG7, false));
        ((CheckBox)findViewById(R.id.checkBoxDG11)).setChecked(sharedPreferences.getBoolean(DNIeLectura.SETTING_READ_DG11, true));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(eu.leps.eIDASbrowser.R.layout.data_configuration);

        myContext = DataConfiguration.this;

        // Actualizamos valores
        updateUserData();

        // Ajustamos el tipo de letra
        Typeface typeFace = Typeface.createFromAsset(myContext.getAssets(), "fonts/HelveticaNeue.ttf");
        ((TextView)findViewById(eu.leps.eIDASbrowser.R.id.configuration_description)).setTypeface(typeFace);
        ((CheckBox)findViewById(eu.leps.eIDASbrowser.R.id.checkBoxDG1)).setTypeface(typeFace);
        ((CheckBox)findViewById(eu.leps.eIDASbrowser.R.id.checkBoxDG2)).setTypeface(typeFace);
        ((CheckBox)findViewById(eu.leps.eIDASbrowser.R.id.checkBoxDG7)).setTypeface(typeFace);
        ((CheckBox)findViewById(eu.leps.eIDASbrowser.R.id.checkBoxDG11)).setTypeface(typeFace);

        // Controlamos el cambio en los CheckBox de lectura de DG's.
        // Modificación del check DG1
        CheckBox checkBox = (CheckBox)findViewById(eu.leps.eIDASbrowser.R.id.checkBoxDG1);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
            {
                // Guarda el valor del modo de funcionamiento en las shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(DNIeLectura.SETTING_READ_DG1, isChecked);
                editor.commit();
            }
        });

        // Modificación del check DG11
        checkBox = (CheckBox)findViewById(eu.leps.eIDASbrowser.R.id.checkBoxDG11);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
            {
                // Guarda el valor del modo de funcionamiento en las shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(DNIeLectura.SETTING_READ_DG11, isChecked);
                editor.commit();
            }
        });

        // Modificación del check DG2
        checkBox = (CheckBox)findViewById(eu.leps.eIDASbrowser.R.id.checkBoxDG2);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
            {
                // Guarda el valor del modo de funcionamiento en las shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(DNIeLectura.SETTING_READ_DG2, isChecked);
                editor.commit();
            }
        });

        // Modificación del check DG7
        checkBox = (CheckBox)findViewById(eu.leps.eIDASbrowser.R.id.checkBoxDG7);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked)
            {
                // Guarda el valor del modo de funcionamiento en las shared preferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(DNIeLectura.SETTING_READ_DG7, isChecked);
                editor.commit();
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////
        // Botón de vuelta al Activity anterior
        Button btnNFCBack = (Button)findViewById(eu.leps.eIDASbrowser.R.id.butDataVolver);
        btnNFCBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Volvemos al activity anterior
                onBackPressed();
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////
        // Botón de lectura de nuevo documento
        Button btnLectura = (Button)findViewById(eu.leps.eIDASbrowser.R.id.butDataLeer);
        btnLectura.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Devolvemos el activity adecuado
                Intent intent = new Intent(DataConfiguration.this, DNIeCanSelection.class);
                startActivity(intent);
            }
        });
    }
}