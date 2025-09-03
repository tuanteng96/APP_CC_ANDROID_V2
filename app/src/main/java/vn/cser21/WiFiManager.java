package vn.cser21;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WiFiManager {

    public static Map<String, Object> getWiFiInfo(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Map<String, Object> wifiInfoMap = new HashMap<>();


        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo != null) {
                wifiInfoMap.put("SSID", wifiInfo.getSSID());
                wifiInfoMap.put("BSSID", wifiInfo.getBSSID());
            }
        }
        return  wifiInfoMap;
    }
}
