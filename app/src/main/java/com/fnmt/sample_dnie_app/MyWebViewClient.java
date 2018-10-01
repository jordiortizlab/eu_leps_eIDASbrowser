package com.fnmt.sample_dnie_app;

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
 * Created by emtg on 23/7/18.
 */

public class MyWebViewClient extends WebViewClient implements IDNIeEventsCallback {

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
        return false;
    }

    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request){
        //Log.d(TAG, "shouldInterceptRequest " + request.getUrl());
        //Log.d(TAG, "shouldInterceptRequest tostring: " + request.toString());
        Log.d(TAG, "shouldInterceptRequest url: " + request.getUrl());
        Log.d(TAG, "shouldInterceptRequest method: " + request.getMethod());
        Log.d(TAG, "shouldInterceptRequest headers: " + request.getRequestHeaders());


        return null;
    }

    @Override
    public void onReceivedClientCertRequest(WebView view, final ClientCertRequest request) {

        Log.d(TAG, "onReceivedClientCertRequest");

        this.view = view;
        this.request = request;

        Log.d (TAG, "onReceivedClientCertRequest - default: " + request.toString());
        Log.d (TAG, "onReceivedClientCertRequest - host: " + request.getHost());
        Log.d (TAG, "onReceivedClientCertRequest - port: " + request.getPort());
        //Log.d (TAG, "onReceivedClientCertRequest - keyTypes: " +request.getKeyTypes().toString());
        //Log.d (TAG, "onReceivedClientCertRequest - principals: " +request.getPrincipals().toString());

        createCertSourceSelectorDialog();
    }


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


    public void showAndroidCertSelector() {

        KeyChain.choosePrivateKeyAlias(fatherActivity,
                new KeyChainAliasCallback() {

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


    }


    /* Method for exec the callback of KeyChain.choosePrivateKeyAlias (onReceivedClientCertRequest) */
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

    public void onChosenCertForClientCertRequest(String alias)  {


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
    }


    @Override
    public void notifySuccess() {
        PrivateKey privateKey = ((MyAppDNIELECTURA) fatherActivity.getApplicationContext()).get_privateKey();
        X509Certificate[] certifcateChain = ((MyAppDNIELECTURA) fatherActivity.getApplicationContext()).get_certificateChain();

        if (privateKey != null) {
            request.proceed(privateKey, certifcateChain);
        }
        else {
            Log.d(TAG, "onChosenCertForClientCertRequest - error retriving private key");
        }

    }

    @Override
    public void notifyError() {

    }

    public void onDNIeSelection () {

        Log.d(TAG,"onDNIeSelection started");
        SampleActivity_2 s2 = (SampleActivity_2)fatherActivity;
        Intent intent = new Intent(s2,  DNIeCanSelection.class);

        s2.makeSubContainerVisible();
        s2.startActivityForResult(intent, 1);
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
