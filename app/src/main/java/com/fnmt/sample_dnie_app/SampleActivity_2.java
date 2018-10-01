package com.fnmt.sample_dnie_app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import com.fnmt.sample_dnie_app.R;

public class SampleActivity_2 extends Activity {
    private static final String TAG = SampleActivity_2.class.getSimpleName();

    private static final String AUTH_CERT_ALIAS = "CertAutenticacion";

    CANSpecDO can = null;

    String _SSLresultado = "";

    TextView baseInfo = null;
    TextView resultInfo = null;
    private Button startBrowsingButton = null;
    TextView urlTextView = null;
    LinearLayout subContainerLL = null;
    LinearLayout fragmentContainerLL = null;
    LinearLayout buttonContainerLL = null;

    public static final int RESULT_DNIeOK = 1;
    public static final int RESULT_DNIeNOK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_activity);

        baseInfo = (TextView) this.findViewById(R.id.base_info);
        resultInfo = (TextView) this.findViewById(R.id.result_info);
        startBrowsingButton = (Button) findViewById(R.id.browser_button);
        urlTextView = (TextView) findViewById(R.id.urlTextView);
        subContainerLL = (LinearLayout) findViewById(R.id.sub_container);
        fragmentContainerLL = (LinearLayout) findViewById(R.id.fragment_container);
        buttonContainerLL = (LinearLayout) findViewById(R.id.buttonsLayout);

        urlTextView.setText("http://lab9054.inv.uji.es/~paco/clave/");


        NFCCommunicationFragment.setTextColor(Color.WHITE);
        NetworkCommunicationFragment.setTextColor(Color.WHITE);

        DroidHttpClient.setAppContext(SampleActivity_2.this);

        startBrowsingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentContainerLL.setVisibility(View.GONE);
                buttonContainerLL.setVisibility(View.GONE);

                CANSpecDO can = ((MyAppDNIELECTURA) getApplicationContext()).getCAN();
                CANSpecDOStore store = new CANSpecDOStore(SampleActivity_2.this);
                ((MyAppDNIELECTURA) getApplicationContext()).setCanStore(store);
                store.getAll();


                LoadBrowser();
            }
        });

        setCan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SampleActivity_2.this, DNIeCanSelection.class);
                startActivityForResult(intent, 1);
            }
        });

    }

    void makeSubContainerVisible() {
        subContainerLL.setVisibility(VISIBLE);
    }

    void makeSubContainerInVisible() {
        subContainerLL.setVisibility(GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
            case RESULT_DNIeOK:
                Log.d(TAG, "DNIe Result OK, resuming browser");
                makeSubContainerVisible();
                MyWebViewClient webViewClient = ((MyAppDNIELECTURA) getApplicationContext()).getWebViewClient();
                webViewClient.notifySuccess();
                break;
            case RESULT_DNIeNOK:
                Log.d(TAG, "DNIe Result NOK, Rise Alert!!");
                break;
            default:
                break;
        }
    }

    void LoadBrowser() {
        WebView webView = (WebView) findViewById(R.id.webViewURL);
        webView.setInitialScale(1);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.loadData(_SSLresultado, "text/html; charset=utf-8", "utf-8");

        MyWebViewClient myWebViewClient = new MyWebViewClient(this, getApplicationContext());
        ((MyAppDNIELECTURA) getApplicationContext()).setWebViewClient(myWebViewClient);
        // The webViewClient has saved the navigation context, so we can recoved later
        Log.d(TAG, "mywebview aux: " + myWebViewClient);


        webView.setWebViewClient(myWebViewClient);

        webView.loadUrl("http://lab9054.inv.uji.es/~paco/clave/");
        Log.d(TAG, "Fixed: http://lab9054.inv.uji.es/~paco/clave/");

    }
}
