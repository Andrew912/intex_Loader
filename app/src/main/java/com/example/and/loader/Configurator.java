package com.example.and.loader;

import android.widget.Toast;

import static DBHandler.PARAMETER_VALUE;

/**
 * Created by Андрей on 22.12.2017.
 */

public class Configurator {

    String
            logTag = "Configurator";
    MainActivity
            mainActivity;

    /**
     * ============================================================
     * Переменные среды не запоминаемые
     * =============================================================
     */

    String
            ipAddress;                      //
    String
            terminalAddress;                //
    String
            networkMask;                    //
    int
            terminalPort = 18080;           // Порты подключения весовых терминалов
    int
            terminalFindAddr = 80;          // Адрес в сети, с которого будет начат поиск терминала
    int
            mixerPort = 28080;              // Порты подключения миксеров
    boolean
            is_Connected_to_network;        //
    int
            terminalStartAddress = 35;      // Стартовый адрес для поиска терминала в сети
    String
            deviceName = "mixer.001";       //
    String
            devicePass = "mixer.001";       //
    String
            terminalName = "mixerterm.001"; //
    int
            dataLoadBufferSize = 1024;      // Размер буфера для получения даннфх с сервера управления

    public static class ControlServer {
        String socketAddr
                = "91.218.229.25";
        int socketPort
                = 60001;
        int buffSize
                = 1024;
     }

    ControlServer
            controlServer;

    /**============================================================
     * Переменные среды, запоминаемые при приостановке приложения
     *=============================================================
     */

    /**
     * ============================================================
     * Переменные, знвачения которых показывают состояние системы,
     * которое надо будет восстановить по окнчании работы программы
     * =============================================================
     */

    boolean
            current_WiFi_status;            // Текущий системный статус Wi-Fi соединения

    /**
     * Конструктор
     *
     * @param mainActivity
     */
    public Configurator(MainActivity mainActivity) {
        this.mainActivity
                = mainActivity;
        controlServer
                = new ControlServer();

    }

    /**
     * Устанавливает значения параметров конфигурации
     */
    public void setSystemParameters() {
        String[] tmp;

        /* Проверяем и загружаем параметр "WiFiNet" */
        tmp = mainActivity.dbHandler.paramGet("WiFiNet");
        if (tmp != null) {
            deviceName = tmp[PARAMETER_VALUE];
        }

        /* Проверяем и загружаем параметр "WiFiPass" */
        tmp = mainActivity.dbHandler.paramGet("WiFiPass");
        if (tmp != null) {
            deviceName = tmp[PARAMETER_VALUE];
        }

        /* Проверяем и загружаем параметр "MixerTermName" */
        tmp = mainActivity.dbHandler.paramGet("MixerTermName");
        if (tmp != null) {
            terminalName = tmp[PARAMETER_VALUE];
        }

        /* Проверяем и загружаем параметр "MixerName" */
        tmp = mainActivity.dbHandler.paramGet("MixerName");
        if (tmp != null) {
            deviceName = tmp[PARAMETER_VALUE];
        }

        /* Проверяем и загружаем параметр "MixerTermAddr" */
        tmp = mainActivity.dbHandler.paramGet("MixerTermAddr");
        if (tmp != null) {
            terminalAddress = tmp[PARAMETER_VALUE];
        }

        /* Вычисляем полный адрес терминала в сети */
        termAddrRefresh();

    }

    /**
     * Обновить адрес весового терминала
     */
    public void termAddrRefresh() {
        terminalAddress
                = mainActivity.dbHandler.getDeviceAddrfromDB(networkMask, terminalName, terminalAddress)[mainActivity.net.NET_DEVICE_ADDR];
        Toast
                .makeText(mainActivity.getApplicationContext(), "Терминал:" + terminalAddress, Toast.LENGTH_LONG).show();
    }

//    /**
//     * Обновить адрес погрузчика
//     */
//    public void loaderAddrRefresh(String deviceName) {
//        terminalAddress
//                = mainActivity.dbHandler.getDeviceAddrfromDB(networkMask, deviceName, terminalAddress)[mainActivity.net.NET_DEVICE_ADDR];
//        Toast
//                .makeText(mainActivity.getApplicationContext(), "Погрузчик:" + terminalAddress, Toast.LENGTH_LONG).show();
//    }
//

    /**
     * Пароль устройства в системе
     *
     * @return
     */
    public String getDevPassowrd() {
        return
                devicePass;
    }

    /**
     * Идентификатор устройства в системе
     *
     * @return
     */
    public String getDevId() {
        return
                mainActivity.conf.deviceName;
    }

    /**
     * Версия протокола
     *
     * @return
     */
    public String getDevProtocol() {
        return mainActivity.getString(R.string.CONFIG_PROTOCOL);
    }

}
