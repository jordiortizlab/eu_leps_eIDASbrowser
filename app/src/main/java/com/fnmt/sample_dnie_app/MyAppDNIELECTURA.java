package com.fnmt.sample_dnie_app;

import android.app.Application;

import de.tsenger.androsmex.data.CANSpecDO;

public class MyAppDNIELECTURA extends Application {

	public boolean m_started;

    private CANSpecDO selectedCAN;

    public void setCAN(CANSpecDO can)
    {
        selectedCAN = can;
    }

    public CANSpecDO getCAN()
    {
        return selectedCAN;
    }

	public boolean isStarted()
	{
		return m_started;
	}
	
	public void setStarted(boolean state)
	{
		m_started = state;
	}
}