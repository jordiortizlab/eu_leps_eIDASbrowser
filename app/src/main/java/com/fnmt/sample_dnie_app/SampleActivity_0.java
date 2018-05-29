package com.fnmt.sample_dnie_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import de.tsenger.androsmex.mrtd.EF_COM;
import es.gob.fnmt.net.DroidHttpClient;
import es.gob.jmulticard.jse.provider.DnieKeyStore;
import es.gob.jmulticard.jse.provider.DnieProvider;
import es.gob.jmulticard.jse.provider.MrtdKeyStoreImpl;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

public class SampleActivity_0 extends Activity implements NfcAdapter.ReaderCallback{

    private static final String AUTH_CERT_ALIAS = "CertAutenticacion";

    private static final String EXAMPLE_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                                               "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                                               "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                                               "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    TextView _baseInfo = null;
    TextView _resultInfo = null;
    private Button _setCan = null;

    String _can = null;

    private static NfcAdapter _myNfcAdapter = null;
    private static PrivateKey _privateKey= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sample_activity);

        _baseInfo = (TextView)this.findViewById(R.id.base_info);
        _resultInfo = (TextView)this.findViewById(R.id.result_info);
        _setCan = (Button)findViewById(R.id.can_button);

        _setCan.setVisibility(VISIBLE);

        DroidHttpClient.setAppContext(SampleActivity_0.this);

        _setCan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(SampleActivity_0.this);
                final View canEntryView = factory.inflate(R.layout.sample_can, null);
                final AlertDialog ad = new AlertDialog.Builder(SampleActivity_0.this).create();
                ad.setCancelable(false);
                ad.setIcon(R.drawable.alert_dialog_icon);
                ad.setView(canEntryView);
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText text = (EditText) ad.findViewById(R.id.can_edit);
                        _can = text.getText().toString();
                        _setCan.setVisibility(GONE);
                        _baseInfo.setText("Aproxime el Dnie al dispositivo");
                        EnableReaderMode();
                    }
                });
                ad.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which){
                        finish();
                    }
                });
                ad.show();
            }
        });
    }

    private void generarFirma() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, KeyStoreException, UnrecoverableKeyException, IOException, CertificateException {
        updateInfo("Realizando firma...", null);
        Signature signatureEngine = Signature.getInstance("SHA256withRSA",new DnieProvider());
        signatureEngine.initSign(_privateKey);
        signatureEngine.update(EXAMPLE_TEXT.getBytes());
        byte[] signature = signatureEngine.sign();

        updateInfo("Firma realizada.",Base64.encodeToString(signature,Base64.DEFAULT));
    }

    public void updateInfo(final String info, final String extra){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(info!=null){
                    _baseInfo.setText(info);
                }
                if(extra!=null){
                    _resultInfo.setVisibility(VISIBLE);
                    _resultInfo.setText(extra);
                }
            }
        });
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        try {
            final DnieProvider p = new DnieProvider();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _resultInfo.setVisibility(GONE);
                        _baseInfo.setText("Leyendo datos...");
                        }
                    });
            p.setProviderTag(tag);
            p.setProviderCan(_can);
            Security.insertProviderAt(p, 1);

            KeyStore keyStore = new DnieKeyStore(new MrtdKeyStoreImpl(), p, MrtdKeyStoreImpl.TYPE_NAME);
//            KeyStore keyStore = KeyStore.getInstance(MrtdKeyStoreImpl.TYPE_NAME);
            keyStore.load(null, null);

            EF_COM data = ((DnieKeyStore)keyStore).getEFCOM();

            _privateKey = (PrivateKey)keyStore.getKey(AUTH_CERT_ALIAS, null);

            generarFirma();
        }
        catch (Exception e){
            updateInfo(null,"ERROR: "+e.getMessage());
            e.printStackTrace();
        }
    }

    private void EnableReaderMode ()
    {
        _myNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        _myNfcAdapter.setNdefPushMessage(null, this);
        _myNfcAdapter.setNdefPushMessageCallback(null, this);

        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000);
        _myNfcAdapter.enableReaderMode(this,
                this,
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK 	|
                        NfcAdapter.FLAG_READER_NFC_A 	|
                        NfcAdapter.FLAG_READER_NFC_B,
                options);
    }
}