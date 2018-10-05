package eu.leps.eIDASbrowser;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.mrtd.DG1_Dnie;
import de.tsenger.androsmex.mrtd.EF_COM;
import es.gob.fnmt.gui.ProgressDialogUI;
import es.gob.fnmt.gui.SignatureNotification;
import es.gob.fnmt.gui.fragment.NFCCommunicationFragment;
import es.gob.fnmt.gui.fragment.NetworkCommunicationFragment;
import es.gob.fnmt.nfc.NFCCommReaderFragment;
import es.gob.fnmt.policy.KeyManagerPolicy;
import es.gob.jmulticard.jse.provider.DnieKeyStore;

public class ReadCrypto extends Activity implements NFCCommReaderFragment.NFCCommReaderFragmentListener, NetworkCommunicationFragment.NetCommFragmentListener, SignatureNotification {

    private static final String TAG = ReadCrypto.class.getSimpleName();
    private static final String AUTH_CERT_ALIAS = "CertAutenticacion";

    NFCCommunicationFragment _readerFragment = null;
    NetworkCommunicationFragment _networkFragment = null;
    ProgressDialogUI _myNetProgressDialog = null;

    private PrivateKey _privateKey = null;
    private X509Certificate[] _certificateChain = null;

    String _SSLresultado = ""; // Is this really necessary? Does this has connection with SampleActivity2?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(eu.leps.eIDASbrowser.R.layout.activity_read_crypto);
//        getActionBar().setDisplayHomeAsUpEnabled(true);


        Bundle arg = new Bundle();
        arg.putParcelable(NFCCommunicationFragment.CAN_ARGUMENT_KEY_STRING, ((MyAppDNIELECTURA)getApplicationContext()).getCAN());
        arg.putBoolean(NFCCommunicationFragment.PRELOADKEYSTORE_ARGUMENT_KEY_STRING, true);
        _readerFragment = new NFCCommunicationFragment();
        _readerFragment.setArguments(arg);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(eu.leps.eIDASbrowser.R.id.fragmentFrameLayout, _readerFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        NFCCommReaderFragment.setSignatureNotification(this);
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        Intent intent = new Intent(getParent(), MyWebViewClient.class);
//        getParent().startActivityForResult(intent, 1);
//        // Give back control to the WebviewClient to continue browsing
//        IDNIeEventsCallback webViewClient = ((MyAppDNIELECTURA) getApplicationContext()).getWebViewClient();
//        webViewClient.notifySuccess();
//    }

    @Override
    public void doNotify(sign_callback_notify notify, String s) {
        final String message;
        switch(notify){
            case SIGNATURE_INIT:
                message = "Iniciando firma, no retire el DNIe del dispositivo NFC.";
                break;
            case SIGNATURE_UPDATE:
                message = "Actualizando datos a firmar.";
                break;
            case SIGNATURE_START:
                message = "Firmando los datos.";
                break;
            case SIGNATURE_DONE:
                message = "Firma realizada, puede retirar el DNIe. Continuando con descarga de datos...";
//                IDNIeEventsCallback webViewClient = ((MyAppDNIELECTURA) getApplicationContext()).getWebViewClient();
//                webViewClient.notifySuccess();
                break;
            default:
                message = null;
        }
        Log.d(TAG, "Proceso de Firma " + message);
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                baseInfo.setText("Proceso de firma");
//                if (message != null) {
//                    resultInfo.setText(message);
//                    resultInfo.setVisibility(VISIBLE);
//                }
//            }
//        });
    }

    @Override
    public void netConnDownload() throws IOException {
        try {
            KeyManagerPolicy keyManagerPolicy = new KeyManagerPolicy.Builder().init().addAlias(AUTH_CERT_ALIAS).addKeyUsage(KeyManagerPolicy.KeyUsage.digitalSignature).build();
        }
        catch(Exception e){
            Log.d(TAG, "Execption building KeyManagerPolicy" + e.getMessage());
            throw new IOException(e);
        }
    }

    @Override
    public void netConnDone(boolean error) {
        Log.d(TAG, "netConnDone(" + error + ")");
        Intent intent = new Intent(ReadCrypto.this, SampleActivity_2.class);
        setResult(DNIeCanSelection.DNIeReadOK);
        finish();
        //startActivityForResult(intent, SampleActivity_2.RESULT_DNIeOK);


    }

    @Override
    public CANSpecDO getCanToStore(KeyStore keyStore, String s) throws KeyStoreException {
        DG1_Dnie data1 = ((DnieKeyStore)keyStore).getDatagroup1();
//        DG2 data2 = ((DnieKeyStore)keyStore).getDatagroup2();
//        DG7 data7 = ((DnieKeyStore)keyStore).getDatagroup7();
//        DG11 data11 = ((DnieKeyStore)keyStore).getDatagroup11();
//        DG13 data13 = ((DnieKeyStore)keyStore).getDatagroup13();

        EF_COM dataef = ((DnieKeyStore)keyStore).getEFCOM();

        /**********************DNIE********************************/
        String certSubject = ((X509Certificate) keyStore.getCertificate(AUTH_CERT_ALIAS)).getSubjectDN().toString();

        try {
            _privateKey = (PrivateKey) keyStore.getKey(AUTH_CERT_ALIAS,null);
            _certificateChain = (X509Certificate[]) keyStore.getCertificateChain(AUTH_CERT_ALIAS);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        }

        return new CANSpecDO(((MyAppDNIELECTURA)getApplicationContext()).getCAN().getCanNumber(),
                certSubject.substring((certSubject.indexOf("CN=")+3)),
                certSubject.substring((certSubject.indexOf("NIF ")==-1?(certSubject.indexOf("OID.2.5.4.5=")+12):(certSubject.indexOf("NIF ")+4))));
    }

    @Override
    public void doNotify(NFC_callback_notify notify, String msg, boolean error) {
        String message = msg;
        Log.d(TAG, "doNotify Received: " + msg);
        switch (notify){
            case NFC_TASK_INIT:
                _readerFragment.updateInfo(notify,"Comunicando con Dnie", "estableciendo canal seguro...");
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        resultInfo.setVisibility(GONE);
//                    }
//                });
                break;
            case NFC_TASK_UPDATE:
                _readerFragment.updateInfo(notify,"Comunicando con Dnie", "obteniendo información...");
                break;
            case NETWORKCOMM_TASK_INIT:
                message = "Connectando con sitio web...";
                break;
            case NETWORKCOMM_TASK_DONE:
                message = "Conexión finalizada.";
                break;
            case ERROR:
                _readerFragment.updateInfo(notify, "Error en comunicación", message);
                if(msg.contains("CAN incorrecto")){
                    Toast.makeText(this.getApplicationContext(),msg, Toast.LENGTH_LONG).show();
//                    setCan.setVisibility(VISIBLE);

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.remove(_readerFragment);
                    transaction.commit();
                    return;
                }
                break;
            default:
                _readerFragment.updateInfo(notify, "Comunicando con Dnie", message);

        }

        if(notify == NFC_callback_notify.NFC_TASK_FINISHED){
            Log.d(TAG, "NFC_TASK_FINISHED");
            if(!error) {

                ProgressDialog myProgressDialog = new ProgressDialog(this);

                myProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myProgressDialog.setTitle("Descargando datos");
                myProgressDialog.setMessage("");
                myProgressDialog.setProgress(0);
                myProgressDialog.setMax(100);

                _myNetProgressDialog = new ProgressDialogUI.Builder(myProgressDialog)
                        .contentLayout(eu.leps.eIDASbrowser.R.layout.progress)
                        .progressBar(eu.leps.eIDASbrowser.R.id.externalProgressRead)
                        .title(eu.leps.eIDASbrowser.R.id.title)
                        .description(eu.leps.eIDASbrowser.R.id.messages)
                        .build();

                _networkFragment = new NetworkCommunicationFragment();
                _networkFragment.setDialog(_myNetProgressDialog,false);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(eu.leps.eIDASbrowser.R.id.fragmentFrameLayout, _networkFragment);
                transaction.addToBackStack(null);
                transaction.commit();
                Log.d(TAG, "NFC_TASK_FINISHED Transaction commit");
                // Store data in common context
                ((MyAppDNIELECTURA) getApplicationContext()).set_certificateChain(_certificateChain);
                ((MyAppDNIELECTURA) getApplicationContext()).set_privateKey(_privateKey);
//                // Give back control to the WebviewClient to continue browsing
//                IDNIeEventsCallback webViewClient = ((MyAppDNIELECTURA) getApplicationContext()).getWebViewClient();
//                webViewClient.notifySuccess();
//                Intent intent = new Intent(ReadCrypto.this, SampleActivity_2.class);
//                startActivityForResult(intent, SampleActivity_2.RESULT_DNIeOK);

            }else{
//                resultInfo.setText(msg);
                Log.d(TAG, msg);
//                Intent intent = new Intent(ReadCrypto.this, SampleActivity_2.class);
//                startActivityForResult(intent, SampleActivity_2.RESULT_DNIeNOK);
            }
        }

    }
}
