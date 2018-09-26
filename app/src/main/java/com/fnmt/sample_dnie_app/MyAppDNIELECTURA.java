package com.fnmt.sample_dnie_app;

import android.app.Application;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import de.tsenger.androsmex.data.CANSpecDO;
import de.tsenger.androsmex.data.CANSpecDOStore;

public class MyAppDNIELECTURA extends Application {

	public boolean m_started;

    private CANSpecDO selectedCAN;

    private CANSpecDOStore canStore;

    private MyWebViewClient webViewClient;

    private PrivateKey _privateKey = null;

    private X509Certificate[] _certificateChain = null;

    public void setCAN(CANSpecDO can)
    {
        selectedCAN = can;
    }

    public CANSpecDO getCAN()
    {
        return selectedCAN;
    }

    public CANSpecDOStore getCanStore() {
        return canStore;
    }

    public void setCanStore(CANSpecDOStore canStore) {
        this.canStore = canStore;
    }

    public boolean isStarted()
	{
		return m_started;
	}
	
	public void setStarted(boolean state)
	{
		m_started = state;
	}

    public MyWebViewClient getWebViewClient() {
        return webViewClient;
    }

    public void setWebViewClient(MyWebViewClient webViewClient) {
        this.webViewClient = webViewClient;
    }

    public PrivateKey get_privateKey() {
        return _privateKey;
    }

    public void set_privateKey(PrivateKey _privateKey) {
        this._privateKey = _privateKey;
    }

    public X509Certificate[] get_certificateChain() {
        return _certificateChain;
    }

    public void set_certificateChain(X509Certificate[] _certificateChain) {
        this._certificateChain = _certificateChain;
    }


}