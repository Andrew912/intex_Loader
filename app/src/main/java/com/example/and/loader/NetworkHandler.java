package com.example.and.loader;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Андрей on 22.12.2017.
 */

public class NetworkHandler {

    String
            logTag = "NetworkHandler";
    MainActivity
            activity;

    public NetworkHandler(MainActivity mainActivity) {
        this.activity
                = mainActivity;

        /* Включить Wi-Fi */
        turn_WIFI_on();

        /*  */
        if (terminalConnectedToNetwork()) {
            activity.config.is_Connected_to_network
                    = true;
            activity.config.ipAddress
                    = get_My_IP();
            activity.config.networkMask
                    = get_My_Net_Mask(activity.config.ipAddress);
            Log.i
                    (logTag, "IPaddr  = " + activity.config.ipAddress);
            Log.i
                    (logTag, "netmask = " + activity.config.networkMask);
        } else {
            /**
             * Тут надо подумать, что делать, если к сети устройство не подключилось
             */
            activity.config.is_Connected_to_network
                    = false;
            activity
                    .sayToast(activity.getString(R.string.NETWORK_NOT_CONNECTED_WIFI));
        }
    }

    /**
     * Определяет, подключено ли устройство к сети Wi-Fi
     *
     * @return
     */
    boolean terminalConnectedToNetwork() {
        WifiManager wifiMan
                = (WifiManager) activity.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInf
                = wifiMan.getConnectionInfo();
        int ipAddress
                = wifiInf.getIpAddress();
        if (ipAddress == 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Определяет адрес устройства в сети Wi-Fi
     *
     * @return
     */
    String get_My_IP() {
        WifiManager wifiMan
                = (WifiManager) activity.getSystemService(WIFI_SERVICE);
        WifiInfo wifiInf
                = wifiMan.getConnectionInfo();
        int ipAddress
                = wifiInf.getIpAddress();
        Log.i
                (logTag, "ipAddress=" + ipAddress);
        String ip
                = "0.0.0.0";
        if (ipAddress != 0) {
            /* Тут - хитрожопая процедура преобразования IP-адреса */
            ip = String.format(
                    "%s.%s.%s.%s",
                    (ipAddress & 0xff),
                    (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff),
                    (ipAddress >> 24 & 0xff));
        }
        Log.i
                (logTag, "ip=" + ip);
        return
                ip;
    }

    /**
     * Вычисляет маску сети для поиска новых устройств
     *
     * @param ip
     * @return
     */
    String get_My_Net_Mask(String ip) {
        Pattern pattern
                = Pattern.compile("(\\d+.\\d+.\\d+.)");
        Matcher matcher
                = pattern.matcher(ip);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return ip;
        }
    }

    /**
     * Wi-Fi turn ON
     */
    void turn_WIFI_on() {
        WifiManager wifiManager
                = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
    }

    /**
     * Wi-Fi turn OFF
     */
    void turn_WIFI_off() {
        WifiManager wifiManager
                = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(false);
    }

    /**
     * Current system Wi-Fi status
     *
     * @return
     */
    boolean get_Current_WiFi_Status() {
        WifiManager wifiManager
                = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        return
                wifiManager.isWifiEnabled();
    }

    /**
     * Поиск в сети указанного устройства
     *
     * @param serverName - имя
     * @param serverAddr - стартовый адрес для поиска, полученный из DNS
     * @param serverPort - порт
     * @param whatFind   - номер счетика запущенных классов поиска устройства
     * @return String[2]
     * [0] - адрес
     * [1] - порт
     */
    /**
     * Выделяет последниюю группу цифр IP-адреса, то есть адрес устройства в сети
     *
     * @param address
     * @return
     */

    public int extractAddress(String address) {
        if (address == null) {
            return 0;
        }
        int myAddr
                = 0;
        Log.i
                (getClass().getSimpleName(), "address: " + address);
        Pattern pattern
                = Pattern.compile("\\d+$");
        Matcher matcher
                = pattern.matcher(address);
        if (matcher.find()) {
            myAddr = Integer.parseInt(matcher.group());
        }
        return
                myAddr;
    }

    /**
     * Вычисляет маску сети для поиска новых устройств
     *
     * @param ip
     * @return
     */
    String get_Net_Mask_from_IP(String ip) {
        Pattern pattern = Pattern.compile("(\\d+.\\d+.\\d+.)");
        Matcher matcher = pattern.matcher(ip);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return null;
        }
    }

}
