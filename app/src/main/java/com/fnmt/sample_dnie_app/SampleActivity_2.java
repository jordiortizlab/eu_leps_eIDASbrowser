package com.fnmt.sample_dnie_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.data.CANSpecDOStore;
import de.tsenger.androsmex.mrtd.DG1_Dnie;
import de.tsenger.androsmex.mrtd.EF_COM;
import es.gob.fnmt.gui.PasswordUI;
import es.gob.fnmt.gui.ProgressDialogUI;
import es.gob.fnmt.gui.SignatureNotification;
import es.gob.fnmt.gui.fragment.NFCCommunicationFragment;
import es.gob.fnmt.gui.fragment.NetworkCommunicationFragment;
import es.gob.fnmt.net.DroidHttpClient;
import es.gob.fnmt.nfc.NFCCommReaderFragment;
import es.gob.fnmt.policy.KeyManagerPolicy;
import es.gob.jmulticard.jse.provider.DnieKeyStore;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;

//import com.fnmt.sample_dnie_app.R;

public class SampleActivity_2 extends Activity implements NFCCommReaderFragment.NFCCommReaderFragmentListener, NetworkCommunicationFragment.NetCommFragmentListener, SignatureNotification{

    private static final String AUTH_CERT_ALIAS = "CertAutenticacion";
    private static final String URL_TUSEGURIDADSOCIAL = "https://tuc.seg-social.gob.es";

    private static final String HTML_SAMPLE =  "<html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"><style>body{background-color: #EEEEEE;}.GrupoHome{color: black;font-size: 15px;}"+
                                               "h5{font-family: arial;color: black;font-size: 20px;}p{font-family: verdana;font-size: 15px;color: #333333;}</style></head><body>##COTIZADO####JUBILACION##</body></html>";

    CANSpecDO can = null;

    String _SSLresultado = "";

    TextView baseInfo = null;
    TextView resultInfo = null;
    private Button setCan = null;

    private PrivateKey _privateKey = null;
    private X509Certificate[] _certificateChain = null;

    NFCCommunicationFragment _readerFragment = null;
    NetworkCommunicationFragment _networkFragment = null;

    ProgressDialogUI _myNetProgressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity);

        baseInfo = (TextView)this.findViewById(R.id.base_info);
        resultInfo = (TextView)this.findViewById(R.id.result_info);
        setCan = (Button)findViewById(R.id.can_button);

        setCan.setVisibility(VISIBLE);

//        PasswordUI.setTextColor(Color.YELLOW);
//        PasswordUI.setBackgroundColor(Color.BLUE);

        NFCCommunicationFragment.setTextColor(Color.WHITE);
        NetworkCommunicationFragment.setTextColor(Color.WHITE);

        DroidHttpClient.setAppContext(SampleActivity_2.this);

        setCan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factory = LayoutInflater.from(SampleActivity_2.this);
                final View canEntryView = factory.inflate(R.layout.sample_can, null);
                final AlertDialog ad = new AlertDialog.Builder(SampleActivity_2.this).create();
                ad.setCancelable(false);
                ad.setIcon(R.drawable.alert_dialog_icon);
                ad.setView(canEntryView);
                ad.setButton(AlertDialog.BUTTON_POSITIVE, "Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        EditText text = (EditText) ad.findViewById(R.id.can_edit);
                        can = new CANSpecDO(text.getText().toString(), "", "");
                        CANSpecDOStore store = new CANSpecDOStore(SampleActivity_2.this);
                        store.getAll();

                        setCan.setVisibility(GONE);

//                      Argumentos para el fragment NFC : CAN y si queremos precargar los certificados (solicitud de PIN)
                        Bundle arg = new Bundle();
                        arg.putParcelable(NFCCommunicationFragment.CAN_ARGUMENT_KEY_STRING, can);
                        arg.putBoolean(NFCCommunicationFragment.PRELOADKEYSTORE_ARGUMENT_KEY_STRING, true);
                        _readerFragment = new NFCCommunicationFragment();
                        _readerFragment.setArguments(arg);

                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, _readerFragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
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

        //Indicamos quién va a manejar las notificaciones de firma (implementa interfaz es.gob.fnmt.gui.SignatureNotification)
        NFCCommReaderFragment.setSignatureNotification(this);
    }

    /** Callback Interfaz es.gob.fnmt.gui.fragment.NFCCommReaderFragment.NFCCommReaderFragmentListener */
    @Override
    public CANSpecDO getCanToStore(KeyStore keyStore, String can) throws KeyStoreException {

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
                _readerFragment.updateInfo(notify,"Comunicando con Dnie", "estableciendo canal seguro...");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultInfo.setVisibility(GONE);
                    }
                });
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
                    setCan.setVisibility(VISIBLE);

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
            if(!error) {

                ProgressDialog myProgressDialog = new ProgressDialog(this);

                myProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myProgressDialog.setTitle("Descargando datos");
                myProgressDialog.setMessage("");
                myProgressDialog.setProgress(0);
                myProgressDialog.setMax(100);

                _myNetProgressDialog = new ProgressDialogUI.Builder(myProgressDialog)
                                        .contentLayout(R.layout.progress)
                                        .progressBar(R.id.externalProgressRead)
                                        .title(R.id.title)
                                        .description(R.id.messages)
                                        .build();

                _networkFragment = new NetworkCommunicationFragment();
                _networkFragment.setDialog(_myNetProgressDialog,false);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, _networkFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }else{
                resultInfo.setText(msg);
            }
        }
    }

    //Callback para la interfaz es.gob.fnmt.gui.fragment.NetworkCommunicationFragment.NetCommFragmentListener
    @Override
    public void netConnDownload() throws IOException {
        try {
            KeyManagerPolicy keyManagerPolicy = new KeyManagerPolicy.Builder().init().addAlias(AUTH_CERT_ALIAS).addKeyUsage(KeyManagerPolicy.KeyUsage.digitalSignature).build();

            if(DroidHttpClient.isInitialized()) DroidHttpClient.reset();
            // Procedemos a la conexión con el servidor
//                SSLSocketFactory sslSocketFactory =
                DroidHttpClient.getBuilder()
                        .clientKeyStore(NFCCommReaderFragment.getKeyStore())
                        .trustKeyStore(DroidHttpClient.getKeyStoreFromResource(R.raw.truststore))
//                        .keyManagerPolicy(new KeyManagerPolicy.Builder().init().addKeyUsage(KeyManagerPolicy.KeyUsage.digitalSignature).build())
                        .keyManagerPolicy(new KeyManagerPolicy.Builder().init().addAlias(AUTH_CERT_ALIAS).addKeyUsage(KeyManagerPolicy.KeyUsage.digitalSignature).build())
//                            .enableCustomRedirectHandler()
                            .done();
//                        .getSSLSocketFactory();
//                HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);

            runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                _myNetProgressDialog.setTitle("Descargando datos...");
                                _myNetProgressDialog.setMessage("Primera llamada");
                                _myNetProgressDialog.incrementProgressBy(17);
                            }
                        });

            DroidHttpClient.Parse parser = new DroidHttpClient.Parse(DroidHttpClient.getHTMLContent(URL_TUSEGURIDADSOCIAL));

            Elements elements = parser.getDocument().getElementsByAttributeValue("value", "ACCEDER CON DNI O CERTIFICADO");
            String actionUrl = elements.first().parent().attr("action");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _myNetProgressDialog.setMessage("Segunda llamada");
                    _myNetProgressDialog.incrementProgressBy(17);
                }
            });
            parser = new DroidHttpClient.Parse(DroidHttpClient.getHTMLContent(actionUrl));

//            String url = URL_COTIZACIONES;
            Element form = parser.getDocument().getElementById("formCapturaMovil");
            String url = URL_TUSEGURIDADSOCIAL + form.attr("action").trim() + "?prefijo=&movil=&tipoAccionCapDatosSol=CANCELAR#Z7_5090HKK0K8M4C0AERR78H700O5";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _myNetProgressDialog.setMessage("Tercera llamada");
                    _myNetProgressDialog.incrementProgressBy(17);
                }
            });
            parser = new DroidHttpClient.Parse(DroidHttpClient.getHTMLContent(url));

            url = URL_TUSEGURIDADSOCIAL+parser.getDocument().getElementById("urlRecarga").val()+"?paso=1";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _myNetProgressDialog.setMessage("Cuarta llamada");
                    _myNetProgressDialog.incrementProgressBy(17);
                }
            });
            String data = DroidHttpClient.getHTMLContent(url);

            url = URL_TUSEGURIDADSOCIAL+parser.getDocument().getElementById("portletActual").val()+"?paso=2";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _myNetProgressDialog.setMessage("Última llamada");
                    _myNetProgressDialog.incrementProgressBy(17);
                }
            });
            parser = new DroidHttpClient.Parse(DroidHttpClient.getHTMLContent(url));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _myNetProgressDialog.setProgress(_myNetProgressDialog.getMaxSize());
                }
            });

            parser.removeLinks();
            Document document = (Document)parser.getDocument();

            _SSLresultado = HTML_SAMPLE.replace("##COTIZADO##",document.getElementsByClass("GrupoHome").first().outerHtml()).
                                        replace("##JUBILACION##",document.getElementsByClass("subGrupo").first().outerHtml());
        }
        catch(Exception e){
            throw new IOException(e);
        }
    }

    //Callback para la interfaz import es.gob.fnmt.gui.fragment.NetworkCommunicationFragment.NetCommFragmentListener
    @Override
    public void netConnDone(boolean error) {
        AlertDialog.Builder alert = new AlertDialog.Builder(SampleActivity_2.this);
        alert.setNegativeButton("Salir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                setCan.setVisibility(VISIBLE);
                dialog.dismiss();
            }
        });
        try {
            if(!error) {
                alert.setTitle("Tu seguridad social");

                WebView webView = new WebView(SampleActivity_2.this);
                webView.setInitialScale(1);
                webView.getSettings().setSupportZoom(true);
                webView.getSettings().setBuiltInZoomControls(true);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.getSettings().setUseWideViewPort(true);

                webView.getSettings().setAllowContentAccess(true);
                webView.getSettings().setAllowFileAccess(true);
                webView.getSettings().setAllowFileAccessFromFileURLs(true);
                webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
                webView.loadData(_SSLresultado, "text/html; charset=utf-8","utf-8");
                alert.setView(webView);
            }
            else{
                alert.setTitle("Error en comunicaciones");
                alert.setTitle("Se ha producido un error en la petición de información.");
            }
        }catch (Exception e){
            alert.setTitle("Error en aplicación:");
            alert.setTitle("Se ha generado una excepción:"+e.getMessage());
        }
        alert.show();
    }


    //Callback para la interfaz es.gob.fnmt.gui.SignatureNotification
    @Override
    public void doNotify(sign_callback_notify notify, String msg) {
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
                break;
            default:
                message = null;
        }
        runOnUiThread(new Runnable() {
              @Override
              public void run() {
                  baseInfo.setText("Proceso de firma");
                  if (message != null) {
                      resultInfo.setText(message);
                      resultInfo.setVisibility(VISIBLE);
                  }
              }
          });
    }
}

//class MyPasswordDialog implements DialogUIHandler {
//
//    static private AlertDialog.Builder alertDialogBuilder;
//    static private Context mContext;
//    private final Activity activity;
//
//    /**
//     * Flag que indica si se cachea el PIN.
//     */
//    private final boolean cachePIN;
//
//    /**
//     * El password introducido. Si está activado el cacheo se reutilizará.
//     */
//    private char[] password = null;
//
//    public MyPasswordDialog(final Context context, final boolean cachePIN) {
//
//        // Guardamos el contexto para poder mostrar el diálogo
//        mContext = context;
//        activity = ((Activity) context);
//        this.cachePIN = cachePIN;
//
//        // Cuadro de diálogo para confirmación de firmas
//        alertDialogBuilder = new AlertDialog.Builder(mContext);
//        alertDialogBuilder.setIcon(R.drawable.alert_dialog_icon);
//    }
//
//    @Override
//    public int showConfirmDialog(String message) {
//        return doShowConfirmDialog(message);
//    }
//
//    public int doShowConfirmDialog(String message) {
//        final AlertDialog.Builder dialog 	= new AlertDialog.Builder(activity);
//        final MyPasswordDialog instance 	= this;
//        final StringBuilder resultBuilder 	= new StringBuilder();
//        resultBuilder.append(message);
//
//        synchronized (instance)
//        {
//            activity.runOnUiThread( new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        dialog.setTitle("Proceso de firma con el DNI electrónico");
//                        dialog.setMessage(resultBuilder);
//                        dialog.setPositiveButton(R.string.psswd_dialog_ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(final DialogInterface dialog, final int which) {
//                                synchronized (instance) {
//                                    resultBuilder.delete(0, resultBuilder.length());
//                                    resultBuilder.append("0");
//                                    instance.notifyAll();
//                                }
//                            }
//                        });
//                        dialog.setNegativeButton(R.string.psswd_dialog_cancel, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(final DialogInterface dialog, final int which) {
//                                synchronized (instance) {
//                                    resultBuilder.delete(0, resultBuilder.length());
//                                    resultBuilder.append("1");
//                                    instance.notifyAll();
//                                }
//                            }
//                        });
//                        dialog.setCancelable(false);
//                        dialog.create().show();
//                    } catch (CancelledOperationException ex) {
//                        Log.e("MyPasswordFragment", "Excepción en diálogo de confirmación" + ex.getMessage());
//                    } catch (Error err) {
//                        Log.e("MyPasswordFragment", "Error en diálogo de confirmación" + err.getMessage());
//                    }
//                }
//            });
//            try
//            {
//                instance.wait();
//                return Integer.parseInt(resultBuilder.toString());
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } catch (Exception ex) {
//                throw new CancelledOperationException();
//            }
//        }
//    }
//
//    @SuppressLint("InflateParams")
//    private char[] doShowPasswordDialog(final int retries) {
//        final AlertDialog.Builder dialog 	= new AlertDialog.Builder(activity);
//        final LayoutInflater inflater 		= activity.getLayoutInflater();
//        final StringBuilder passwordBuilder = new StringBuilder();
//        final MyPasswordDialog instance 	= this;
//        dialog.setMessage(getTriesMessage(retries));
//
//        synchronized (instance)
//        {
//            activity.runOnUiThread( new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        final View passwordView = inflater.inflate(R.layout.passwordentry, null);
//
//                        final TextView passwordText = (TextView) passwordView.findViewById(R.id.password_view);
//                        final EditText passwordEdit = (EditText) passwordView.findViewById(R.id.password_edit);
//                        final CheckBox passwordShow = (CheckBox) passwordView.findViewById(R.id.checkBoxShow);
//
//                        // Ajustamos el tipo de letra
//                        Typeface type = Typeface.createFromAsset(mContext.getAssets(),"fonts/NeutraText-LightAlt.otf");
//                        passwordText.setTypeface(type);
//                        passwordEdit.setTypeface(type);
//                        passwordShow.setTypeface(type);
//
//                        dialog.setPositiveButton(R.string.psswd_dialog_ok, new DialogInterface.OnClickListener() {
//
//                            /**
//                             * @param dialog El diálogo que genera el evento.
//                             * @see DialogInterface.OnClickListener#onClick(DialogInterface,
//                             *      int)
//                             */
//                            @Override
//                            public void onClick(final DialogInterface dialog, final int which) {
//                                synchronized (instance) {
//                                    passwordBuilder.delete(0, passwordBuilder.length());
//                                    passwordBuilder.append(passwordEdit.getText().toString());
//                                    instance.notifyAll();
//                                }
//                            }
//                        });
//                        dialog.setNegativeButton(R.string.psswd_dialog_cancel, new DialogInterface.OnClickListener() {
//
//                            /**
//                             * @param dialog El diálogo que genera el evento.
//                             * @see DialogInterface.OnClickListener#onClick(DialogInterface,
//                             *      int)
//                             */
//                            @Override
//                            public void onClick(final DialogInterface dialog, final int which) {
//                                synchronized (instance) {
//                                    passwordBuilder.delete(0, passwordBuilder.length());
//                                    instance.notifyAll();
//                                }
//                            }
//                        });
//                        passwordShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//
//                            @Override
//                            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
//                                if (isChecked) {
//                                    passwordEdit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                                    passwordShow.setText(activity.getString(R.string.psswd_dialog_show));
//                                } else {
//                                    passwordEdit.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                                    passwordShow.setText(activity.getString(R.string.psswd_dialog_hide));
//                                }
//                            }
//                        });
//                        dialog.setCancelable(false);
//                        dialog.setView(passwordView);
//                        dialog.create().show();
//                    } catch (Exception ex) {
//                        Log.e("MyPasswordFragment", "Excepción en diálogo de contraseña" + ex.getMessage());
//                    } catch (Error err) {
//                        Log.e("MyPasswordFragment", "Error en diálogo de contraseña" + err.getMessage());
//                    }
//                }
//            });
//            try
//            {
//                instance.wait();
//                return passwordBuilder.toString().toCharArray();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    @Override
//    public char[] showPasswordDialog(final int retries) {
//        char[] returning;
//
//        if (retries < 0 && cachePIN && password != null && password.length > 0)
//            returning = password.clone();
//        else
//            returning = doShowPasswordDialog(retries);
//
//        if (cachePIN && returning != null && returning.length > 0)
//            password = returning.clone();
//        else
//            return null;
//
//        return returning;
//    }
//
//    /**
//     * Genera el mensaje de reintentos del diálogo de contraseña.
//     *
//     * @param retries El número de reintentos pendientes. Si es negativo, se considera que no se conocen los intentos.
//     * @return El mensaje a mostrar.
//     */
//    private String getTriesMessage(final int retries) {
//        String text;
//        if (retries < 0) {
//            text = activity.getString(R.string.dni_password_msg);
//        } else if (retries == 1) {
//            text = activity.getString(R.string.dni_password_msg_1_try);
//        } else {
//            //text = activity.getString(R.string.dni_password_msg_n_tries, retries);
//            text = "Introduzca PIN. Quedan " +retries+" reintentos.";
//        }
//        return text;
//    }
//}
