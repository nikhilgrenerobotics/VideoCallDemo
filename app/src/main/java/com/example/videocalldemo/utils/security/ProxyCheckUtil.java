package com.example.videocalldemo.utils.security;

import com.example.videocalldemo.utils.AppLogger;

public class ProxyCheckUtil {

    private String TAG = this.getClass().getSimpleName();

    public boolean isProxySet() {
        String proxyAddress = "";
        try {
            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");

            proxyAddress = (proxyHost == null ? "" : proxyHost) + ":" + ((proxyPort == null || proxyPort.equalsIgnoreCase("0") ? "" : proxyPort));
            AppLogger.getInstance().e(TAG, "Proxy: " + proxyAddress);
        } catch (Exception ex) {
            AppLogger.getInstance().e(TAG, "Exec: " + ex.getLocalizedMessage());
        } finally {
            return proxyAddress.length() != 0 && !proxyAddress.equalsIgnoreCase(":");
        }
    }
}
