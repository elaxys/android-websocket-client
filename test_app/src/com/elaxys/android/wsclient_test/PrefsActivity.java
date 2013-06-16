package com.elaxys.android.wsclient_test;

import com.elaxys.android.wsclient_test.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
    public static final String PREF_SERVER_URI     		= "server_uri";
    public static final String PREF_CONN_TIMEOUT   		= "conn_timeout";
    public static final String PREF_RETRY_INTERVAL 		= "retry_interval";
    public static final String PREF_MAX_RXSIZE     		= "max_rxsize";
    public static final String PREF_RESPOND_PING   		= "respond_ping";
    public static final String PREF_SERVER_CERT   		= "server_cert";
    public static final String PREF_TEST_COUNT     		= "test_count";
    public static final String PREF_TEST_PAYLOAD_SIZE	= "test_payload_size";
	
    @SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        updateSummary();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);     
    }

    
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		updateSummary();
	}

	
    @SuppressWarnings("deprecation")
	private void updateSummary() {
    	EditTextPreference prefText;
    	
		prefText = (EditTextPreference)findPreference(PREF_SERVER_URI);
		prefText.setSummary(getServerURI(this));
		
		prefText = (EditTextPreference)findPreference(PREF_CONN_TIMEOUT);
		prefText.setSummary(Integer.toString(getConnTimeout(this)));
    	
		prefText = (EditTextPreference)findPreference(PREF_RETRY_INTERVAL);
		prefText.setSummary(Integer.toString(getRetryInterval(this)));
		
		prefText = (EditTextPreference)findPreference(PREF_MAX_RXSIZE);
		prefText.setSummary(Integer.toString(getMaxRxSize(this)/1024));
    	
		prefText = (EditTextPreference)findPreference(PREF_TEST_COUNT);
		prefText.setSummary(Integer.toString(getTestCount(this)));
		
		prefText = (EditTextPreference)findPreference(PREF_TEST_PAYLOAD_SIZE);
		prefText.setSummary(Integer.toString(getTestPayloadSize(this)/1024));
    }
   
    
    public static String getServerURI(Activity ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    	return prefs.getString(PREF_SERVER_URI, "").trim();
    }
   
    public static int getConnTimeout(Activity ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String val = prefs.getString(PREF_CONN_TIMEOUT, "0").trim();
        return Integer.parseInt(val);
    }
    
    public static int getRetryInterval(Activity ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String val = prefs.getString(PREF_RETRY_INTERVAL, "0").trim();
        return Integer.parseInt(val);
    }
    
    public static int getMaxRxSize(Activity ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String val = prefs.getString(PREF_MAX_RXSIZE, "0").trim();
        return Integer.parseInt(val) * 1024;
    }
   
    public static boolean getRespondPing(Activity ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(PREF_RESPOND_PING, false);
    }
    
    public static boolean getServerCert(Activity ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(PREF_SERVER_CERT, true);
    }
   
    public static int getTestCount(Activity ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String val = prefs.getString(PrefsActivity.PREF_TEST_COUNT, "0").trim();
        return Integer.parseInt(val);
    }
    
    public static int getTestPayloadSize(Activity ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String val = prefs.getString(PrefsActivity.PREF_TEST_PAYLOAD_SIZE, "0").trim();
        return Integer.parseInt(val) * 1024;
    }
}
