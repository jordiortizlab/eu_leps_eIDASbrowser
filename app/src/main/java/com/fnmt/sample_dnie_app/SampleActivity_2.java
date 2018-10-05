package com.fnmt.sample_dnie_app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.data.CANSpecDOStore;
import es.gob.fnmt.gui.fragment.NFCCommunicationFragment;
import es.gob.fnmt.gui.fragment.NetworkCommunicationFragment;
import es.gob.fnmt.net.DroidHttpClient;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;


public class SampleActivity_2 extends Activity {
    private static final String TAG = SampleActivity_2.class.getSimpleName();

    private static final String AUTH_CERT_ALIAS = "CertAutenticacion";

    CANSpecDO can = null;

    String _SSLresultado = "";

    TextView resultInfo = null;
    private Button startBrowsingButton = null;
    private ImageButton eltaButton = null;
    private ImageButton athexButton = null;
    private ImageButton correosButton = null;
    TextView urlTextView = null;
    LinearLayout subContainerLL = null;
    LinearLayout fragmentContainerLL = null;
    LinearLayout buttonContainerLL = null;
    LinearLayout webViewContainerLL = null;
    LinearLayout eltaServicesLayout = null;
    LinearLayout athexServicesLayout = null;
    LinearLayout correosServicesLayout = null;

    public static final int RESULT_DNIeOK = 1;
    public static final int RESULT_DNIeNOK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sample_activity);

        resultInfo = (TextView) this.findViewById(R.id.result_info);
        startBrowsingButton = (Button) findViewById(R.id.browser_button);
        urlTextView = (TextView) findViewById(R.id.urlTextView);
        subContainerLL = (LinearLayout) findViewById(R.id.sub_container);
        fragmentContainerLL = (LinearLayout) findViewById(R.id.fragment_container);
        buttonContainerLL = (LinearLayout) findViewById(R.id.buttonsLayout);
        webViewContainerLL = (LinearLayout) findViewById(R.id.webviewlayout);
        eltaServicesLayout = (LinearLayout) findViewById(R.id.eltaServicesLayout);
        eltaServicesLayout.setVisibility(GONE);
        athexServicesLayout = (LinearLayout) findViewById(R.id.athexServicesLayout);
        athexServicesLayout.setVisibility(GONE);
        correosServicesLayout = (LinearLayout) findViewById(R.id.correosServicesLayout);
        correosServicesLayout.setVisibility(GONE);
        eltaButton = (ImageButton) findViewById(R.id.elta1ImageButton);
        athexButton = (ImageButton) findViewById(R.id.athex1imageButton);
        correosButton = (ImageButton) findViewById(R.id.correosImageButton);

        addServices(R.array.EltaServices, R.array.EltaServicesNames, eltaServicesLayout);
        addServices(R.array.AthexServices, R.array.AthexServicesNames, athexServicesLayout);
        addServices(R.array.CorreosServices, R.array.CorreosServicesNames, correosServicesLayout);

        hideWebView();
        hideFragmentBar(); // TODO: We shall remove the FragmentLayout. Now it is of no use
        showSubContainer();

        urlTextView.setText("http://lab9054.inv.uji.es/~paco/clave/");

        NFCCommunicationFragment.setTextColor(Color.WHITE);
        NetworkCommunicationFragment.setTextColor(Color.WHITE);

        DroidHttpClient.setAppContext(SampleActivity_2.this);

        startBrowsingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showWebView();

                CANSpecDO can = ((MyAppDNIELECTURA) getApplicationContext()).getCAN();
                CANSpecDOStore store = new CANSpecDOStore(SampleActivity_2.this);
                ((MyAppDNIELECTURA) getApplicationContext()).setCanStore(store);
                store.getAll();


                LoadBrowser();
            }
        });

        eltaButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(eltaServicesLayout.getVisibility()) {
                    case VISIBLE:
                        eltaServicesLayout.setVisibility(GONE);
                        break;
                    default:
                        eltaServicesLayout.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        athexButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(athexServicesLayout.getVisibility()) {
                    case VISIBLE:
                        athexServicesLayout.setVisibility(GONE);
                        break;
                    default:
                        athexServicesLayout.setVisibility(VISIBLE);
                        break;
                }
            }
        });

        correosButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(correosServicesLayout.getVisibility()) {
                    case VISIBLE:
                        correosServicesLayout.setVisibility(GONE);
                        break;
                    default:
                        correosServicesLayout.setVisibility(VISIBLE);
                        break;
                }
            }
        });



    }

    private void addServices(int services, int servicesnames, LinearLayout destinationLayout){
        String[] servicesUrls = getResources().getStringArray(services);
        String[] servicesNames = getResources().getStringArray(servicesnames);
        int position = 0;

        for(final String s : servicesUrls) {
            Button serviceButton = new Button(this);
            serviceButton.setText(servicesNames[position]);
            destinationLayout.addView(serviceButton);
            serviceButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    urlTextView.setText(s);
                }
            });
            position++;
        }
    }

    private void modifyLinearLayoutWeight(LinearLayout ll, float weight) {
        LinearLayout.LayoutParams actualp = (LinearLayout.LayoutParams) ll.getLayoutParams();
        actualp.weight = weight;
        ll.setLayoutParams(actualp);
    }

    void showSubContainer() {
        modifyLinearLayoutWeight(subContainerLL, 1.0F);
        subContainerLL.setVisibility(VISIBLE);
    }

    void hideSubContainer() {
        modifyLinearLayoutWeight(subContainerLL, 0);
        subContainerLL.setVisibility(GONE);
    }

    void hideWebView() {
        modifyLinearLayoutWeight(subContainerLL, 0);
        webViewContainerLL.setVisibility(GONE);
    }

    void showWebView() {
        modifyLinearLayoutWeight(subContainerLL, 1.0F);
        webViewContainerLL.setVisibility(VISIBLE);
        hideButtonBar();
        hideFragmentBar();
    }

    void hideButtonBar() {
        buttonContainerLL.setVisibility(View.GONE);

    }

    void hideFragmentBar() {
        fragmentContainerLL.setVisibility(View.GONE);

    }

    void showButtonBar() {
        buttonContainerLL.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(resultCode) {
            case RESULT_DNIeOK:
                Log.d(TAG, "DNIe Result OK, resuming browser");
                showSubContainer();
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

//        webView.loadUrl("http://lab9054.inv.uji.es/~paco/clave/");
        webView.loadUrl(urlTextView.getText().toString());
        Log.d(TAG, "Fixed: http://lab9054.inv.uji.es/~paco/clave/");

    }
}
