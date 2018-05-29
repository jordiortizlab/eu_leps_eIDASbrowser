package com.fnmt.sample_dnie_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.mrtd.EF_COM;
import es.gob.fnmt.net.DroidHttpClient;
import es.gob.fnmt.nfc.NFCCommReaderFragment;
import es.gob.jmulticard.jse.provider.DnieKeyStore;
import es.gob.jmulticard.jse.provider.DnieProvider;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

public class SampleActivity_1 extends Activity implements NFCCommReaderFragment.NFCCommReaderFragmentListener{

    private static final String AUTH_CERT_ALIAS = "CertAutenticacion";

    private static final String EXAMPLE_TEXT = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. " +
                                               "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
                                               "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                                               "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    CANSpecDO _can = null;

    TextView _baseInfo = null;
    TextView _resultInfo = null;
    private Button _setCan = null;

    NFCCommReaderFragment _readerFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sample_activity);

        _baseInfo = (TextView)this.findViewById(R.id.base_info);
        _resultInfo = (TextView)this.findViewById(R.id.result_info);
        _setCan = (Button)findViewById(R.id.can_button);

        _setCan.setVisibility(VISIBLE);

        DroidHttpClient.setAppContext(SampleActivity_1.this);

        _setCan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(SampleActivity_1.this);
                final View canEntryView = factory.inflate(R.layout.sample_can, null);
                final AlertDialog ad = new AlertDialog.Builder(SampleActivity_1.this).create();
                ad.setCancelable(false);
                ad.setIcon(R.drawable.alert_dialog_icon);
                ad.setView(canEntryView);
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText text = (EditText) ad.findViewById(R.id.can_edit);
                        _can = new CANSpecDO(text.getText().toString(), "", "");

                        _setCan.setVisibility(GONE);

//                      Argumentos para el fragment NFC : CAN y si queremos precargar los certificados (solicitud de PIN)
                        Bundle arg = new Bundle();
                        arg.putParcelable(NFCCommReaderFragment.CAN_ARGUMENT_KEY_STRING, _can);
                        arg.putBoolean(NFCCommReaderFragment.PRELOADKEYSTORE_ARGUMENT_KEY_STRING, true);
                        NFCCommReaderFragment readerFragment = new NFCCommReaderFragment();
                        readerFragment.setArguments(arg);

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, readerFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();

                        _baseInfo.setText("Aproxime el Dnie al dispositivo");
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

    /** Callback Interfaz es.gob.fnmt.gui.fragment.NFCCommReaderFragment.NFCCommReaderFragmentListener */
    @Override
    public CANSpecDO getCanToStore(KeyStore keyStore, String can) throws KeyStoreException {

        EF_COM dataef = ((DnieKeyStore)keyStore).getEFCOM();

        String certSubject = ((X509Certificate) keyStore.getCertificate(AUTH_CERT_ALIAS)).getSubjectDN().toString();

        return new CANSpecDO(can,
                            certSubject.substring((certSubject.indexOf("CN=")+3)),
                            certSubject.substring((certSubject.indexOf("NIF ")==-1?(certSubject.indexOf("OID.2.5.4.5=")+12):(certSubject.indexOf("NIF ")+4))));
    }

    /** Callback Interfaz es.gob.fnmt.gui.fragment.NFCCommReaderFragment.NFCCommReaderFragmentListener */
    @Override
    public void doNotify(final NFC_callback_notify notify, String msg, boolean error) {
        String message =msg;
        switch (notify){
            case NFC_TASK_INIT:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        _resultInfo.setVisibility(GONE);
                    }
                });
                break;
            case NFC_TASK_UPDATE:
                updateInfo("Recuperando datos...", null);
                break;
            case ERROR:
                updateInfo("Aproxime el Dnie al dispositivo", null);
                if(msg.contains("CAN incorrecto")){
                    Toast.makeText(this.getApplicationContext(),msg, Toast.LENGTH_LONG).show();
                    _setCan.setVisibility(VISIBLE);

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.remove(_readerFragment);
                    transaction.commit();
                    return;
                }else{
                    updateInfo(null,"ERROR: "+message);
                }
                break;
            case  NFC_TASK_FINISHED:
                if(error) {
                    updateInfo("Aproxime el Dnie al dispositivo","Error: "+msg);
                }
                else updateInfo(null,msg);
                try {
                    generarFirma();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    private void generarFirma() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, KeyStoreException, UnrecoverableKeyException, IOException, CertificateException {
        PrivateKey privateKey = (PrivateKey)NFCCommReaderFragment.getKeyStore().getKey(AUTH_CERT_ALIAS,null);

        updateInfo("Realizando firma...", null);

        Signature signatureEngine = Signature.getInstance("SHA256withRSA",new DnieProvider());
        signatureEngine.initSign(privateKey);
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
}