package com.fnmt.sample_dnie_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.data.CANSpecDOStore;


public class DNIeCanSelection extends Activity implements OnClickListener, OnItemLongClickListener, OnItemClickListener {

	
	private static final String ACTION_LABEL_READ 	= "Leer";
	private static final String ACTION_LABEL_EDIT 	= "Modificar";
	private static final String ACTION_LABEL_DELETE = "Borrar";
	private static final String[] ACTION_LABELS = {ACTION_LABEL_READ,ACTION_LABEL_EDIT, ACTION_LABEL_DELETE};
	
	private static final int REQ_EDIT_NEW_CAN 	= 1;
	private static final int REQ_EDIT_CAN 		= 2;
	private static final int REQ_READ_PP 		= 3;
	private Button readNewW;
	private ListView listW;

	private Typeface fontType;

	private CANSpecDOStore cans;
	private ArrayAdapter<CANSpecDO> listA;
	
	private CANSpecDO selectedBac;
	
	AlertDialog ad = null;
	private Context myContext = null;
	
	ArrayList<MrtdItem> mrtdItems = new ArrayList<>();
	private SampleAdapter m_adapter;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myContext = DNIeCanSelection.this;
     
        // Quitamos la barra del título
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.can_list);
        
        cans = new CANSpecDOStore(this);
        prepareWidgets();

		// Ajustamos tipo de letra
		fontType = Typeface.createFromAsset(myContext.getAssets(), "fonts/HelveticaNeue.ttf");
		((TextView)findViewById(R.id.can_TEXT)).setTypeface(fontType);

        ///////////////////////////////////////////////////////////////////////////////////
        // Botón 1: Volver
        final Button btnSolicitar = (Button)findViewById(R.id.butVolver);
        btnSolicitar.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v) {

				// Volvemos el activity principal
				Intent intent = new Intent(DNIeCanSelection.this, SampleActivity_2.class);
				startActivity(intent);
            }
        });

        ///////////////////////////////////////////////////////////////////////////////////
		// Botón 2: Configuración
    	Button btnConfig = (Button)findViewById(R.id.butConfigurar);
    	btnConfig.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {

				// Devolvemos el activity adecuado
				Intent intent = new Intent(DNIeCanSelection.this, DataConfiguration.class);
            	startActivityForResult(intent,1);
			}
		});
    }
	
	private void prepareWidgets() {
        readNewW = (Button) findViewById(R.id.BtnCAN_NEW);
        readNewW.setOnClickListener(this);
        
        listW = (ListView) findViewById(R.id.canList);
        listA = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cans.getAll());
        
        int idx=0;
        while(idx < listA.getCount())
        {
        	CANSpecDO canItem = listA.getItem(idx);
			String can6digitos = canItem.getCanNumber();
			while(can6digitos.length()<6)
				can6digitos = "0"+can6digitos;
	        mrtdItems.add(new MrtdItem(can6digitos, canItem.getUserName(), canItem.getUserNif()));
	        idx++;
        }
        m_adapter = new SampleAdapter(getApplicationContext(), mrtdItems);
        listW.setAdapter(m_adapter);

        listW.setOnItemClickListener( this );
        listW.setOnItemLongClickListener( this );
	}
		
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		setIntent(data);
		
		if( requestCode == REQ_EDIT_NEW_CAN )
		{
			if( resultCode == RESULT_OK )
			{
				CANSpecDO can = data.getExtras().getParcelable( CANSpecDO.EXTRA_CAN );
				cans.save(can);
				refreshAdapter();
				read(can);
			}
		}
		else if( requestCode == REQ_EDIT_CAN )
		{
			if( resultCode == RESULT_OK )
			{
				CANSpecDO can = data.getExtras().getParcelable( CANSpecDO.EXTRA_CAN );
				cans.save(can);
				refreshAdapter();
			}
		}
		else if( requestCode == REQ_READ_PP )
		{
			if( resultCode == RESULT_OK )
			{
				Intent i;
				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2)
			    	i = new Intent(this, DNIeReader.class).putExtras(data.getExtras());
			    else
			    	// Build.VERSION_CODES.KITKAT
			    	i = new Intent(this, NFCOperationsEncKitKat.class).putExtras(data.getExtras());
				
				startActivityForResult(i, 1);
			}
			else if( resultCode == RESULT_CANCELED )
			{
				toastIt("error");
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		if( v == readNewW )
		{
			LayoutInflater factory = LayoutInflater.from(myContext);
            final View canEntryView = factory.inflate(R.layout.can_entry, null);
            ad = new AlertDialog.Builder(myContext).create();
		    ad.setCancelable(false);
		    ad.setIcon(R.drawable.alert_dialog_icon);
		    ad.setView(canEntryView);
			ad.setTitle(getString(R.string.title_dlg_newcan));
		    ad.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.psswd_dialog_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					EditText text = (EditText) ad.findViewById(R.id.can_editbox);

					// Nos aseguramos de que el CAN introducido tiene la longitud correcta
					if (text.getText().length() != 0x06) {
						Toast.makeText(myContext, R.string.help_can_len, Toast.LENGTH_LONG).show();
						return;
					}

					// Almacenamos el CAN en la lista de documentos.
					CANSpecDO can = new CANSpecDO(text.getText().toString(), "", "");
					cans.save(can);
					refreshAdapter();
					read(can);
				}
			});
		    ad.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.psswd_dialog_cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {


				}
			});

			Button helpBtn = ((Button)canEntryView.findViewById(R.id.helpButton));
			helpBtn.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					Toast.makeText(myContext, R.string.help_can, Toast.LENGTH_LONG).show();
				}
			});

			ad.show();

			// Ajustamos textos y tipos de letra
			((TextView)ad.findViewById(R.id.can_textview)).setTypeface(fontType);
			((EditText)ad.findViewById(R.id.can_editbox)).setTypeface(fontType);
		}
	}

	private void read(CANSpecDO b)
	{
		ArrayList<CANSpecDO> cans = new ArrayList<>();
		cans.add(b);

        Context applicationContext = getApplicationContext();
        // Dejamos disponible el CAN para la lectura del DNIe
		((MyAppDNIELECTURA)getApplicationContext()).setCAN(b);

		read( cans );
	}

	private void read(ArrayList<CANSpecDO> bs)
	{
		Intent i;

		int currentapiVersion = Build.VERSION.SDK_INT;
		if (currentapiVersion <= Build.VERSION_CODES.JELLY_BEAN_MR2)
			i = new Intent( DNIeCanSelection.this, DNIeReader.class )
			.putParcelableArrayListExtra(CANSpecDO.EXTRA_CAN_COL, bs )
			.setAction( DNIeReader.ACTION_READ );
		else
	    	// Build.VERSION_CODES.KITKAT
	    	i = new Intent( this, NFCOperationsEncKitKat.class );

		startActivityForResult(i, 1);
	}

	private void delete(CANSpecDO b)
	{
		cans.delete(b);
		refreshAdapter();
	}

	private void edit(final CANSpecDO b)
	{
		LayoutInflater factory = LayoutInflater.from(myContext);
        final View canEntryView = factory.inflate(R.layout.can_entry, null);
        ad = new AlertDialog.Builder(myContext).create();
	    ad.setCancelable(false);
	    ad.setIcon(R.drawable.alert_dialog_icon);
	    ad.setView(canEntryView);
		ad.setTitle(getString(R.string.title_dlg_newcan));
	    ad.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.psswd_dialog_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				EditText text = (EditText) ad.findViewById(R.id.can_editbox);

				// Nos aseguramos de que el CAN introducido tiene la longitud correcta
				if (text.getText().length() != 0x06) {
					Toast.makeText(myContext, R.string.help_can_len, Toast.LENGTH_SHORT).show();
					return;
				}

				// Almacenamos el CAN en la lista de documentos.
				CANSpecDO can = new CANSpecDO(text.getText().toString(), "", "");
				cans.delete(b);
				cans.save(can);
				refreshAdapter();
			}
		});
	    ad.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.psswd_dialog_cancel), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		Button helpBtn = ((Button)canEntryView.findViewById(R.id.helpButton));
		helpBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Toast.makeText(myContext, R.string.help_can, Toast.LENGTH_LONG).show();
			}
		});
		ad.show();

		// Ajustamos textos y tipos de letra
		((TextView)ad.findViewById(R.id.can_textview)).setTypeface(fontType);
		((EditText)ad.findViewById(R.id.can_editbox)).setTypeface(fontType);
		((EditText)ad.findViewById(R.id.can_editbox)).setText(b.getCanNumber());
	}
	
	private void toastIt( String msg )
	{
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}


	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		selectedBac = listA.getItem(position);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Opciones");
		builder.setItems( ACTION_LABELS, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	switch (item) {
				case 0: read(selectedBac); break;
				case 1: edit(selectedBac); break;
				case 2: delete(selectedBac); break;

				default:
					break;
				}
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
		
		return true;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		selectedBac = listA.getItem(position);
		
		// Si la pulsación es corta, directamente leemos con ese CAN
		read(selectedBac);
	}
	
	private void refreshAdapter() {
		m_adapter.clear();
		listA.clear();
		for(CANSpecDO b : cans.getAll())
			listA.add(b);
		
		int idx=0;
        while(idx < listA.getCount())
        {
        	CANSpecDO canItem = listA.getItem(idx);
	        mrtdItems.add(new MrtdItem(canItem.getCanNumber(), canItem.getUserName(), canItem.getUserNif()));
	        idx++;
        }
	}
	
	public class SampleAdapter extends ArrayAdapter<MrtdItem> {
		private Context context;
		private ArrayList<MrtdItem> items;
		private LayoutInflater vi;
		
		public SampleAdapter(Context context,ArrayList<MrtdItem> items) {
			super(context,0, items);
			this.context = context;
			this.items = items;
			vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public SampleAdapter(Context context) {
			super(context, 0);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			final MrtdItem ei = items.get(position);
			if (ei != null)
			{
				v = vi.inflate(R.layout.list_mrtd_row, null);
				final TextView title 	= (TextView)v.findViewById(R.id.row_title);
				final TextView name 	= (TextView)v.findViewById(R.id.row_name);
				final TextView nif 		= (TextView)v.findViewById(R.id.row_nif);

				// Ajustamos el tipo de letra
				if(title != null) {
					title.setText(ei.strCan);
					title.setTypeface(fontType);
				}
				if(name != null) {
					name.setText(ei.strName);
					name.setTypeface(fontType);
				}
				if(nif != null) {
					nif.setText(ei.strNif);
					nif.setTypeface(fontType);
				}
					
		    	Button deleteImageView = (Button)  v.findViewById(R.id.Btn_DESTROYENTRY);
		    	deleteImageView.setOnClickListener(new OnClickListener() {
		    		public void onClick(View v) {
		    			RelativeLayout vwParentRow = (RelativeLayout)v.getParent();
		    			int position = listW.getPositionForView(vwParentRow);
		    			
		    			// Borramos la entrada y refrescamos el listado
		    			selectedBac = listA.getItem(position);
		    			delete(selectedBac);
		    		}
	    		});
			}
			return v;
		}
	}
    
	// Clase que define una tarjeta MRTD
  	private class MrtdItem {
  		public final String strCan;
  		public final String strName;
  		public final String strNif;

  		public MrtdItem(String strCan, String strName, String strNif)
  		{
  			this.strCan  = strCan;
  			this.strName = strName;
  			this.strNif  = strNif;
  		}
  	}
}