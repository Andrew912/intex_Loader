package com.example.and.loader;

/**
 * Created by Андрей on 05.08.2017.
 */

public class Messager {

    MainActivity
            a;

    String
            request_Device, request_Feed, request_Value;

    public Messager(MainActivity activity) {
        this.a = activity;
        request_Device = null;
        request_Feed = null;
        request_Value = null;
    }

    void setRequestString(String[] s) {
        request_Device = s[0];
        request_Feed = s[1];
        request_Value = s[2];
    }

    public String requestString() {
        return request_Device + "\n" +
                request_Feed + "\n" +
                request_Value;
    }

    String pair(String pKey, String pValue) {
        return pKey + "=\'" + pValue + "\'";
    }

    String msg_header() {
        return "";
    }

    String msg_serviceRequestAccept_No(String operId) {
        return msg_header() +
                pair("cmd", "accept") +
                pair("oper", operId) +
                pair("accept", "no");
    }

    String msg_serviceRequestAccept_Yes(String operId) {
        return msg_header() +
                pair("cmd", "accept") +
                pair("operid", operId) +
                pair("accept", "yes");
    }

    String msg_serviceStop(String operId) {
        return msg_header() +
                pair("operid", operId) +
                pair("cmd", "stop");
    }

    String msg_Ok() {
        return msg_header() +
                pair("st", "0");
    }

    String msg_Error() {
        return msg_header() +
                pair("st", "1");
    }

}
