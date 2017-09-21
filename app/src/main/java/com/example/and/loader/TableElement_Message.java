package com.example.and.loader;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Андрей on 02.06.2017.
 * -------------------------------
 * Message
 * num
 * key
 * par
 * name
 * value
 */

public class TableElement_Message {

    String
            logTAG = "TableElement_Message";
    MainActivity
            activity;
    boolean
            logging = false;
    int
            num;
    String
            dev;
    String
            key;
    ArrayList<TableElement_MessageParameter>
            par;

    // Конструктор
    public TableElement_Message(MainActivity activity, String dev, String key, ArrayList<TableElement_MessageParameter> pPar) {

        this.activity = activity;
        this.dev = dev;
        this.key = key;
        Log.i(logTAG, "TableElement_Message: NEW: dev:" + dev + " key:" + key + " num:" + num);
        this.par = pPar;
    }

    public int getNum() {
        return num;
    }

    public String getParam(String pName) {
        for (TableElement_MessageParameter p :
                par) {
            if (p.getName().equals(pName)) {
                return p.getValue();
            }
        }
        return "";
    }

    public String getKey() {
        return key;
    }

    public String getDev() {
        return dev;
    }

    public ArrayList<TableElement_MessageParameter> getPar() {
        return par;
    }
}
