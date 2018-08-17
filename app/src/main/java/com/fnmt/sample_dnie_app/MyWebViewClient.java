package com.leps.android.eidasbrowser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.security.KeyChain;
import android.security.KeyChainAliasCallback;
import android.security.KeyChainException;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.webkit.ClientCertRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Created by elenat on 2/2/18.
 */

public class MyWebViewClient extends WebViewClient {

    private static final String TAG = MyWebViewClient.class.getSimpleName();
    private Activity fatherActivity;
    private Context appContext;
    private String savedAlias;
    private WebView view;

    public ClientCertRequest getRequest() {
        return request;
    }

    public void setRequest(ClientCertRequest request) {
        this.request = request;
    }

    private ClientCertRequest request;


    public MyWebViewClient(Activity fatherActivity, Context appContext) {
        super();
        this.fatherActivity = fatherActivity;
        this.appContext = appContext;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        Log.d(TAG, "shouldOverrideUrlLoading " + request.getUrl());


        /*if(request.getUrl().getHost().endsWith("idp.testshib.org")) {
            return false;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
        view.getContext().startActivity(intent);
        return true;
*/
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request){
        //Log.d(TAG, "shouldInterceptRequest " + request.getUrl());
        //Log.d(TAG, "shouldInterceptRequest tostring: " + request.toString());
        Log.d(TAG, "shouldInterceptRequest url: " + request.getUrl());
        Log.d(TAG, "shouldInterceptRequest method: " + request.getMethod());
        Log.d(TAG, "shouldInterceptRequest headers: " + request.getRequestHeaders());


        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceivedClientCertRequest(WebView view, ClientCertRequest request) {

        Log.d(TAG, "onReceivedClientCertRequest");

        this.view = view;
        this.request = request;

        Log.d (TAG, "onReceivedClientCertRequest - default: " +request.toString());
        Log.d (TAG, "onReceivedClientCertRequest - host: " +request.getHost());
        Log.d (TAG, "onReceivedClientCertRequest - port: " +request.getPort());
        //Log.d (TAG, "onReceivedClientCertRequest - keyTypes: " +request.getKeyTypes().toString());
        //Log.d (TAG, "onReceivedClientCertRequest - principals: " +request.getPrincipals().toString());

        createCertSourceSelectorDialog();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createCertSourceSelectorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(fatherActivity);
        builder.setMessage(R.string.dialog_cert_selector_message)
                .setTitle(R.string.dialog_cert_selector_title);

        builder.setPositiveButton(R.string.dialog_cert_selector_softCert, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showAndroidCertSelector();
            }
        });
        builder.setNegativeButton(R.string.dialog_cert_selector_dnie, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(DialogInterface dialog, int id) {
                onDNIeSelection();
            }
        });
        builder.setNeutralButton(R.string.dialog_cert_selector_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //TODO: User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void showAndroidCertSelector() {

        KeyChain.choosePrivateKeyAlias(fatherActivity,
                new KeyChainAliasCallback() {

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    public void alias(String alias) {
                        // Credential alias selected.  Remember the alias selection for future use.
                        if (alias != null) {
                            saveAlias(alias);
                        } else {
                            //TODO if no private key is available or the user cancels the request.
                            Log.d (TAG, "Recovered alias is null. Has user cancelled the cert selection?");
                        }
                    }
                },
                new String[] {"RSA", "DSA"}, // List of acceptable key types. null for any
                null,                // issuer, null for any
                request.getHost(),           // host name of server requesting the cert, null if unavailable
                443,                   // port of server requesting the cert, -1 if unavailable
                savedAlias);                // alias to preselect, null if unavailable

        /*Intent installIntent = KeyChain.createInstallIntent();
        installIntent.putExtra(KeyChain.EXTRA_PKCS12, keystore);
        startActivityForResult(installIntent, INSTALL_KEYSTORE_CODE);*/

    }


    /* Method for exec the callback of KeyChain.choosePrivateKeyAlias (onReceivedClientCertRequest) */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void saveAlias(String alias) {
        if (alias != null) {
            this.savedAlias = alias;
            Log.d(TAG, "alias: "+ alias);
            onChosenCertForClientCertRequest (alias);
        }
        else {
            Log.d (TAG, "Recovered alias is null");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onChosenCertForClientCertRequest(String alias)  {

        /*
        File extStore = Environment.getExternalStorageDirectory();

        String certPath = extStore.getAbsolutePath() + File.separator + "Android/spain_test_credential.pfx";
        Log.d(TAG, "Fixed path to cert: " + certPath);

        AssetManager assetManager = fatherActivity.getAssets();
        AssetFileDescriptor fileDescriptor = null;
        FileInputStream stream = null;
        try {
            fileDescriptor = assetManager.openFd("spain_test_credential.pfx");
            //fileDescriptor = assetManager.open(@idR.raw.spain_test_credential);
            if (fileDescriptor == null)
                Log.d(TAG, "error opening fileDescriptor of PFX credential");
            else
                Log.d(TAG, "Fixed path to cert: " + fileDescriptor.toString());
            stream = fileDescriptor.createInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String passphrase = "test";
        //Log.d(TAG, "Fixed path to cert: " + certPath);
        //KeyStore ks = getCertsFromP12(certPath, passphrase);
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("pkcs12");
            ks.load(stream, passphrase.toCharArray());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        */

        //Log.d(TAG, "onChosenCertForClientCertRequest");
        PrivateKey privateKey = null;
        try {
            privateKey = KeyChain.getPrivateKey(appContext, savedAlias);
            //Log.d(TAG, "onChosenCertForClientCertRequest - private key");
            //Log.d(TAG, "onChosenCertForClientCertRequest - algor: " + privateKey.getAlgorithm());
            //Log.d(TAG, "onChosenCertForClientCertRequest - format: " + privateKey.getFormat());
            //Log.d(TAG, "onChosenCertForClientCertRequest - obj:\n" + privateKey.toString());
            //Log.d(TAG, "onChosenCertForClientCertRequest - string:\n" + new String (privateKey.getEncoded()));


            X509Certificate[] certifcateChain = KeyChain.getCertificateChain(appContext, savedAlias);
            Log.d(TAG, "onChosenCertForClientCertRequest - public certs");

            if (privateKey != null) {
                request.proceed(privateKey, certifcateChain);
            }
            else {
                Log.d(TAG, "onChosenCertForClientCertRequest - error retriving private key");
            }
        } catch (KeyChainException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*KeyStore.PrivateKeyEntry pkEntry = null;
        try {
            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(passphrase.toCharArray());
            pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, protParam);
            PrivateKey myPrivateKey = pkEntry.getPrivateKey();
            List<X509Certificate> certList = new ArrayList<X509Certificate>();

            Enumeration e = ks.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Log.d(TAG, "onReceivedClientCertRequest - alias: "+ alias);
                certList.add((X509Certificate) ks.getCertificate(alias));
            }

            request.proceed(myPrivateKey, (X509Certificate[]) certList.toArray());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        */


        /*KeyChain.choosePrivateKeyAlias(WebViewActivity.this, new KeyChainAliasCallback() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void alias(String alias) {
                Log.e(getClass().getSimpleName(), "===>Key alias is: " + alias);
                try {
                    PrivateKey changPrivateKey = KeyChain.getPrivateKey(WebViewActivity.this, alias);
                    X509Certificate[] certificates = KeyChain.getCertificateChain(WebViewActivity.this, alias);
                    Log.v(getClass().getSimpleName(), "===>Getting Private Key Success!");
                    request.proceed(changPrivateKey, certificates);
                } catch (KeyChainException e) {
                    Log.e(getClass().getSimpleName(), Util.printException(e));
                } catch (InterruptedException e) {
                    Log.e(getClass().getSimpleName(), Util.printException(e));
                }
            }
        }, new String[]{"RSA"}, null, null, -1, null);
        //}, request.getKeyTypes(), request.getPrincipals(), request.getHost(), request.getPort(), null);

        super.onReceivedClientCertRequest(view,request);
        */
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onDNIeSelection () {

        Log.d(TAG,"onDNIeSelection started");

        ((eIDASBrowserApp)fatherActivity.getApplicationContext()).setStarted(true);

        Intent intent = new Intent(fatherActivity, DNIeCanSelection.class);

        fatherActivity.startActivityForResult(intent, DisplayURLActivity.REQ_DNIE_READ);

        // Se instancia el proveedor y se añade
        /*
        final DnieProvider p = new DnieProvider();
        Tag tagFromIntent = null; //TODO implementar
        p.setProviderTag(tagFromIntent); // Tag discovered by the activity
        p.setProviderCan(String.valueOf(canNumber)); // DNIe’s Can number
        Security.insertProviderAt(p, 1);

        // Creamos el cuadro de diálogo que gestionará la solicitud del PIN
        //MyPasswordDialog myFragment = new MyPasswordDialog(NFCOperationsEnc.this, true);
        NFCOperationsEnc nfcOperationsEnc = new NFCOperationsEnc();
        MyPasswordDialog myFragment = new MyPasswordDialog(nfcOperationsEnc, true);
        DNIeDialogManager.setDialogUIHandler(myFragment);*/

        // Cargamos certificados y keyReferences
        /*final KeyStore ksUserDNIe;
        try {
            ksUserDNIe = KeyStore.getInstance("MRTD");
            ksUserDNIe.load(null, null);
            Certificate signCert = ksUserDNIe.getCertificate("CertFirmaDigital");
            Certificate authCert = ksUserDNIe.getCertificate("CertAutenticacion");

            // El siguiente certificado es completo/real ya que nos pedirá el PIN
            KeyStore.Entry entry = ksUserDNIe.getEntry("CertAutenticacion", null);
            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) entry;
            Certificate authCertCompleto = pkEntry.getCertificate();
            Log.d(TAG, "onDNIeSelection - authCertCompleto");

            PrivateKey privateKey = pkEntry.getPrivateKey();
            Log.d(TAG, "onDNIeSelection - private key");

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            ByteArrayInputStream bais = new ByteArrayInputStream(authCertCompleto.getEncoded());
            X509Certificate x509 =  (X509Certificate) cf.generateCertificate(bais);
            X509Certificate[] certifcateChain = new X509Certificate[]{x509};
            Log.d(TAG, "onDNIeSelection - public certs");

            if (privateKey != null) {
                request.proceed(privateKey, certifcateChain);
            }
            else {
                Log.d(TAG, "onDNIeSelection - error retriving private key");
            }

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
    }

    public void onReceivedError(WebView view, int errorCode,
                                String description, String failingUrl) {
        super.onReceivedError( view,  errorCode,
                description,  failingUrl);
    }

    @Override
    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(fatherActivity);
        String message = "SSL Certificate error.";
        switch (error.getPrimaryError()) {
            case SslError.SSL_UNTRUSTED:
                message = "The certificate authority is not trusted.";
                break;
            case SslError.SSL_EXPIRED:
                message = "The certificate has expired.";
                break;
            case SslError.SSL_IDMISMATCH:
                message = "The certificate Hostname mismatch.";
                break;
            case SslError.SSL_NOTYETVALID:
                message = "The certificate is not yet valid.";
                break;
        }
        Log.d("TAG", message);

        handler.proceed();
        message += " Do you want to continue anyway?";

        builder.setTitle("SSL Certificate Error");
        builder.setMessage(message);
        builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

    }



    private KeyStore getCertsFromP12(String pathToFile, String passphrase){
        KeyStore p12 = null;

        try {
            p12 = KeyStore.getInstance("pkcs12");
            p12.load(new FileInputStream(pathToFile), passphrase.toCharArray());

            /*Enumeration e = p12.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                X509Certificate c = (X509Certificate) p12.getCertificate(alias);
                addCertificateToKeyStore(c);
            }*/
        } catch (Exception e) {
            Log.d(TAG, "getCertsFromP12 error" + e);
        }

        return p12;
    }

    private void addCertificateToKeyStore(X509Certificate c) {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
            ks.setCertificateEntry("myCertAlias", c);
        } catch (Exception e){}
    }


}
