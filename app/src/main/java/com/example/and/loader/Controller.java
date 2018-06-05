package com.example.and.loader;

import android.app.Notification;
import android.os.Message;
import android.util.Log;

import static com.example.and.loader.MainActivity.B0_DATA;
import static com.example.and.loader.MainActivity.B0_Start;
import static com.example.and.loader.MainActivity.B1_Call;
import static com.example.and.loader.MainActivity.B1_Exit;
import static com.example.and.loader.MainActivity.B1_Refresh;
import static com.example.and.loader.MainActivity.B2_Accept;
import static com.example.and.loader.MainActivity.B2_Reject;
import static com.example.and.loader.MainActivity.B3_Abort;
import static com.example.and.loader.MainActivity.B3_Done;
import static com.example.and.loader.MainActivity.B3_Refresh;
import static com.example.and.loader.MainActivity.B4_Done;
import static com.example.and.loader.MainActivity.B5_Abort;
import static com.example.and.loader.MainActivity.B5_Refresh;
import static com.example.and.loader.MainActivity.B5_getBegin;
import static com.example.and.loader.MainActivity.LAYOUT_1_WAIT;
import static com.example.and.loader.MainActivity.LAYOUT_2_REQUEST;
import static com.example.and.loader.MainActivity.LAYOUT_3_LOADING;
import static com.example.and.loader.MainActivity.LAYOUT_4_DONE;
import static com.example.and.loader.MainActivity.LAYOUT_5_WAITBEGIN;

/**
 * Created by Андрей on 05.08.2017.
 */

public class Controller {

    MainActivity
            a;

    Message
            msg;

    enum ServerBusyStatus {
        FREE, BUSY
    }

    enum ServerStatus {
        WAIT_REQUEST, WAIT_BEGIN, WAIT_WEIGHT, REQUEST_ACCEPT, REQUEST_REJECT
    }

    enum ServerAcceptStatus {
        ACCEPT, REJECT, NONE
    }

    ServerBusyStatus
            serverBusyStatus;   // Сервер "занят"
    ServerStatus
            serverStatus;       // Состояние сервера
    ServerAcceptStatus
            serverAcceptStatus; // Состояние подтверждения запроса на обслуживание
    int
            controllerStatus;
    String
            deviceName,         // Миксер
            operId,             // Операция
            weightRemain,       // Остаток веса для загрузки
            value,              // Вес в операции
            feed;               // Наименование компонента
    boolean
            dataProtect;        // Данные операции изменяться не должны


    public Controller(MainActivity activity) {
        this.a = activity;
        dataInit();
    }

    void controller(int action) {
        controllerStatus = action;
        switch (controllerStatus) {
            case B0_DATA:

                break;
            case B0_Start:
                serverBusyStatus = ServerBusyStatus.FREE;
                a.gotoLayout(LAYOUT_1_WAIT, "");
                dataInit();
                break;
            case B1_Exit:
                serverBusyStatus = ServerBusyStatus.FREE;
                System.exit(0);
                break;
            case B1_Refresh:

                break;
            case B1_Call:
                a.Beep();
                setMessageParameters();
                a.server.serverMessage = Server.ServerMessage.NONE;
                Log.i("Controller", "before gotoLayout(LAYOUT_2_REQUEST)...");
                a.gotoLayout(LAYOUT_2_REQUEST, printMessageParameters());
                break;
            case B2_Reject:
                serverBusyStatus = ServerBusyStatus.FREE;
                serverAcceptStatus = ServerAcceptStatus.REJECT;
                a.server.serverMessage = Server.ServerMessage.NO;
                a.gotoLayout(LAYOUT_1_WAIT, "");
                dataInit();
                break;
            case B2_Accept:
                // Запрос на обслуживание принят
                serverStatus = ServerStatus.WAIT_BEGIN;
                serverBusyStatus = ServerBusyStatus.BUSY;
                serverAcceptStatus = ServerAcceptStatus.ACCEPT;
                a.server.serverMessage = Server.ServerMessage.YES;
                dataProtect = true; // Основные параметры больше не перезаписываются
                a.gotoLayout(LAYOUT_5_WAITBEGIN, a.messager.requestString());
                break;
            case B5_Abort:
                serverBusyStatus = ServerBusyStatus.FREE;
                a.gotoLayout(LAYOUT_1_WAIT, "");
                dataInit();
                break;
            case B5_Refresh:

                break;
            case B5_getBegin:
                // Если получен сигнал начала погрузки
                serverStatus = ServerStatus.WAIT_WEIGHT;

                msg = new Message();
                msg.obj = weightRemain;
                a.handlers_textWeight[3].sendMessage(msg);

//                a.textWeight[3].setText(weightRemain);

                a.gotoLayout(LAYOUT_3_LOADING, a.messager.requestString());
                break;
            case B3_Abort:
                serverBusyStatus = ServerBusyStatus.FREE;
                a.gotoLayout(LAYOUT_1_WAIT, "");
                dataInit();
                break;
            case B3_Refresh:
//                serverStatus = ServerStatus.WAIT_WEIGHT;

//                a.setTextViews(printMessageParameters(), weightRemain);
//                a.textViews[3].setText(printMessageParameters());
//                a.textWeight[3].setText(weightRemain);

                msg = new Message();
                msg.obj = printMessageParameters();
                a.handlers_textView[3].sendMessage(msg);

                msg = new Message();
                msg.obj = weightRemain;
                a.handlers_textWeight[3].sendMessage(msg);

                a.gotoLayout(LAYOUT_3_LOADING, a.messager.requestString());
                break;
            case B3_Done:
                a.gotoLayout(LAYOUT_4_DONE, a.messager.requestString());
                break;
            case B4_Done:
                serverBusyStatus = ServerBusyStatus.FREE;
                a.gotoLayout(LAYOUT_1_WAIT, "");
                dataInit();
                break;
        }
    }

    // Установка параметров операции из данных операции
    void setMessageParameters() {
        if (dataProtect == false) {
            deviceName = getParam("device");
            operId = getParam("oper");
            value = getParam("value");
            feed = getParam("feed");
        }
        weightRemain = getParam("weight");
    }

    public String getParam(String pName) {
        for (TableElement_MessageParameter p :
                a.server.p) {
            if (p.getName().equals(pName)) {
                return p.getValue();
            }
        }
        return null;
    }

    void dataInit() {
        serverBusyStatus = ServerBusyStatus.FREE;
        serverStatus = ServerStatus.WAIT_REQUEST;
        serverAcceptStatus = ServerAcceptStatus.NONE;
        deviceName = null;
        operId = null;
        weightRemain = null;
        controllerStatus = B0_Start;
        dataProtect = false;
    }

    /**
     * @return
     */
    String printMessageParameters() {
        a.messager.setRequestString(new String[]{deviceName, feed, value});
        return ""
                + deviceName + "\n"
                + feed + "\n"
                + value;
    }

}
