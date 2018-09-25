package com.fnmt.sample_dnie_app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

//import com.dnielectura.jj2000.J2kStreamDecoder;

import java.io.ByteArrayInputStream;

import de.tsenger.androsmex.mrtd.DG11;
import de.tsenger.androsmex.mrtd.DG1_Dnie;
import de.tsenger.androsmex.mrtd.DG2;
import de.tsenger.androsmex.mrtd.DG7;

public class DataResult extends Activity {

    private DG1_Dnie m_dg1;
    private DG11 m_dg11;
    private DG2 m_dg2;
    private DG7 m_dg7;

    private Bitmap loadedImage;
    private Bitmap loadedSignature;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.data_result);

        // Almacenamos el contexto
        Context myContext = DataResult.this;

        Bundle extras = getIntent().getExtras();
        if(extras != null) {

            // Recuperamos los datos obtenidos en la lectura anterior
            byte [] m_dataDG1	= extras.getByteArray("DGP_DG1");
            byte []  m_dataDG2	= extras.getByteArray("DGP_DG2");
            byte [] m_dataDG7	= extras.getByteArray("DGP_DG7");
            byte [] m_dataDG11 	= extras.getByteArray("DGP_DG11");

            // Construimos los objetos Data Group que hayamos leído
            if(m_dataDG1!=null) m_dg1   = new DG1_Dnie(m_dataDG1);
            if(m_dataDG2!=null) m_dg2   = new DG2(m_dataDG2);
            if(m_dataDG7!=null) m_dg7   = new DG7(m_dataDG7);
            if(m_dataDG11!=null)m_dg11  = new DG11(m_dataDG11);

            TextView tvloc;
            ////////////////////////////////////////////////////////////////////////
            // Información del DG1, si la tenemos
            if(m_dg1!=null) {
                // Nombre
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_01);
                tvloc.setText(m_dg1.getName());
                // Apellidos
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_02);
                tvloc.setText(m_dg1.getSurname());
                // Doc Number
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_03);
                tvloc.setText(m_dg1.getDocNumber());
                // Doc caducity
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_03_caducity);
                tvloc.setText(m_dg1.getDateOfExpiry());
                // Fecha de nacimiento
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_07);
                tvloc.setText(m_dg1.getDateOfBirth());
                // País de nacimiento
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_09);
                tvloc.setText(m_dg1.getNationality().toUpperCase());
            }

            ////////////////////////////////////////////////////////////////////////
            // Información del DG11, si la tenemos
            if(m_dg11!=null) {
                // Lugar de nacimiento
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_08);
                tvloc.setText(m_dg11.getBirthPlace().replace("<", " (") + ")");
                // DNIe Number
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_03);
                tvloc.setText(m_dg11.getPersonalNumber());
                // Dirección actual
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_04);
                tvloc.setText(m_dg11.getAddress(DG11.ADDR_DIRECCION));
                // Localidad
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_05);
                tvloc.setText(m_dg11.getAddress(DG11.ADDR_LOCALIDAD));
                // Provincia
                tvloc = (TextView) findViewById(R.id.CITIZEN_data_tab_06);
                tvloc.setText(m_dg11.getAddress(DG11.ADDR_PROVINCIA));
            }

            ////////////////////////////////////////////////////////////////////////
            // Información del DG2 (foto), si la tenemos
            ImageView ivFoto = (ImageView) findViewById(R.id.CITIZEN_data_tab_00);
//            if(m_dataDG2!=null){
//                try {
//                    // Parseo de la foto en formato JPEG-2000
//                    byte [] imagen = m_dg2.getImageBytes();
//                    J2kStreamDecoder j2k = new J2kStreamDecoder();
//                    ByteArrayInputStream bis = new ByteArrayInputStream(imagen);
//                    loadedImage = j2k.decode(bis);
//                }catch(Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//
//            // Mostramos la foto si hemos podido decodificarla
//            if(loadedImage!=null)
//                ivFoto.setImageBitmap(loadedImage);
//            else
              ivFoto.setImageResource(R.drawable.noface);
//
//            ////////////////////////////////////////////////////////////////////////
//            // Información del DG7, si la tenemos
            ImageView ivFirma = (ImageView) findViewById(R.id.CITIZEN_data_tab_00_FIRMA);
//            if(m_dataDG7!=null){
//                try {
//                    // Parseo de la firma en formato JPEG-2000
//                    byte [] imagen = m_dg7.getImageBytes();
//                    J2kStreamDecoder j2k = new J2kStreamDecoder();
//                    ByteArrayInputStream bis = new ByteArrayInputStream(imagen);
//                    loadedSignature = j2k.decode(bis);
//                }catch(Exception e)
//                {
//                    e.printStackTrace();
//                }
//
//                // Mostramos la firma si hemos podido decodificarla
//                if(loadedSignature!=null) {
//                    // Mostramos la firma
//                    ivFirma.setVisibility(ImageView.VISIBLE);
//                    ivFirma.setImageBitmap(loadedSignature);
//                }
//            }
            ivFirma.setImageResource(R.drawable.noface);

            // Ajustamos el tipo de letra del título y de toda la tabla
            Typeface typeFace = Typeface.createFromAsset(myContext.getAssets(), "fonts/HelveticaNeue.ttf");
            TableLayout miTabla = (TableLayout) findViewById(R.id.data_table);
            for(int i = 0, j = miTabla.getChildCount(); i < j; i++) {
                View view = miTabla.getChildAt(i);
                if (view instanceof TableRow) {
                    TableRow row = (TableRow) view;
                    for(int idx = 0; idx < row.getChildCount(); idx++)
                    {
                        View viewText = row.getChildAt(idx);
                        if (viewText instanceof TextView)
                            ((TextView)viewText).setTypeface(typeFace);
                    }
                }
            }

            miTabla = (TableLayout) findViewById(R.id.data_table2);
            for(int i = 0, j = miTabla.getChildCount(); i < j; i++) {
                View view = miTabla.getChildAt(i);
                if (view instanceof TableRow) {
                    TableRow row = (TableRow) view;
                    for(int idx = 0; idx < row.getChildCount(); idx++)
                    {
                        View viewText = row.getChildAt(idx);
                        if (viewText instanceof TextView)
                            ((TextView)viewText).setTypeface(typeFace);
                    }
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////
        // Botón de vuelta al Activity anterior
        Button btnNFCBack = (Button)findViewById(R.id.butVolver);
        btnNFCBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // Volvemos a la pantalla principal
                Intent intent = new Intent(DataResult.this, SampleActivity_2.class);
                startActivity(intent);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////
        // Botón de configuración
        Button btnConfig = (Button)findViewById(R.id.butConfigurar);
        btnConfig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Creamos el Intent correspondiente
                Intent intent = new Intent(DataResult.this, DataConfiguration.class);
                startActivityForResult(intent, 1);
            }
        });
    }
}
