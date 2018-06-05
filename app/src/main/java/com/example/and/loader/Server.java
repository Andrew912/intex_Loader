package com.example.and.loader;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.and.loader.MainActivity.B1_Call;

/**
 * Created by Андрей on 05.08.2017.
 */

public class Server {

    String
            logTAG = "SERVER: ";
    MainActivity
            activity;
    ServerSocket
            serverSocket;
    String
            interfaceToSend;
    String
            message;
    static final int
            socketServerPORT = 18080;
    ArrayList<TableElement_MessageParameter>
            p;
    boolean
            requsetTypeIsHTML;
    String
            serviceRequestAnswer = null;

    enum ServerMessage {
        NONE,   // только код получения сообщения (st='0/1')
        YES,    // положительный ответ на запрос на обслуживание
        NO,     // отрицательный ответ на запрос на обслуживание
        STOP,   // остановить погрузку
        BUSY,   // для "чужих" запросов - "ЗАНЯТ"
        NAME    // ответ на запрос "who"
    }

    ServerMessage
            serverMessage = ServerMessage.NONE;

    enum FromDeviceMessageTypes {
        LOAD_REQUEST,
        LOAD_BEGIN,
        LOAD_WEIGHT,
        LOAD_STOP,
        WHO,
        UNKNOWN
    }

    FromDeviceMessageTypes
            fromDeviceMessageType;

    public Server(MainActivity activity) {
        this.activity
                = activity;
        Log.i("*****", "Server: Start");
        Thread socketServerThread
                = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    // Определение типа входящего сообщения и реакция на него
    FromDeviceMessageTypes messageTypeIs() {
        Log.i("FromDeviceMessageTypes", "start recognition = '" + p.get(0).getName() + "'");
        // WHO
        if (p.get(0).getName().equals(activity.getString(R.string.WHO_ARE_YOU_STRING))) {
            fromDeviceMessageType = FromDeviceMessageTypes.WHO;
            Log.i("FromDeviceMessageTypes", "WHO");
            return FromDeviceMessageTypes.WHO;
        }

        // LOAD_REQUEST
        if (getParam("cmd").equals("load") &
                paramNow("oper") &
                paramGone("status") &
                paramGone("weight")) {
            fromDeviceMessageType = FromDeviceMessageTypes.LOAD_REQUEST;
            return FromDeviceMessageTypes.LOAD_REQUEST;
        }

        // LOAD_BEGIN
        if (getParam("cmd").equals("load") &
                paramNow("oper") &
                paramNow("device") &
                paramNow("status") &
                paramNow("weight")) {
            fromDeviceMessageType = FromDeviceMessageTypes.LOAD_BEGIN;
            return FromDeviceMessageTypes.LOAD_BEGIN;
        }
        // LOAD_WEIGHT
        if (getParam("cmd").equals("weight") &
                paramNow("weight")) {
            fromDeviceMessageType = FromDeviceMessageTypes.LOAD_WEIGHT;
            return FromDeviceMessageTypes.LOAD_WEIGHT;
        }
        // LOAD_STOP
        if (getParam("cmd").equals("stop") &
                paramNow("oper")) {
            fromDeviceMessageType = FromDeviceMessageTypes.LOAD_STOP;
            return FromDeviceMessageTypes.LOAD_STOP;
        }

        fromDeviceMessageType = FromDeviceMessageTypes.UNKNOWN;
        return FromDeviceMessageTypes.UNKNOWN;
    }

    private class SocketServerThread extends Thread {
        int buffSize = 512;
        int count = 0;

        @Override
        public void run() {
            InputStream is;
            OutputStream os;
            Socket socket;
            String msg;
            try {
                serverSocket = new ServerSocket(socketServerPORT);
                while (true) {
                    socket = serverSocket.accept();
                    is = socket.getInputStream();
                    requsetTypeIsHTML = true;

                    byte[] buffer = new byte[buffSize];
                    int read = is.read(buffer, 0, buffSize);

                    Log.i(logTAG, "read=" + read);

                    if (read > 0) {
                        byte[] b = new byte[read];
                        System.arraycopy(buffer, 0, b, 0, read);
                        msg = new String(b);
                        requsetTypeIsHTML = requestIsHTML(msg);
                    } else {
                        msg = "empty string";
                    }
                    Log.i(logTAG, "msg=" + msg);

                    p = ExtractParametersOfCommand(msg);

                    final String finalMsg = msg;
//                    activity.runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            activity.printOut(finalMsg);
                    // Определяем тип входящего сообщения и реакцию на него
                    Choreographer();
//                        }
//                    });
                    SocketServerReplyThread
                            socketServerReplyThread = new SocketServerReplyThread(
                            socket, count);
                    socketServerReplyThread.run();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Обработка событий, связанных с поступающими сообщениями.
     * Определяет действие, исходя из статуса и входного сигнала.
     */

    void Choreographer() {
        FromDeviceMessageTypes messageType
                = messageTypeIs();
        Log.i("Choreographer", "messageType=" + messageType);
        Log.i("Choreographer", "==========================");
        /**
         * Если поступил запрос "Кто?", отправыть ответ
         */
        if (messageType == FromDeviceMessageTypes.WHO) {
            serverMessage = ServerMessage.NAME;
            return;
        }


        Log.i("Choreographer", "STEP 1" + messageType);

        /**
         * Если отправитель - это не текущий юзер, то игнорируем все запросы, кроме
         * запроса на обслуживание.
         * На запрос на обслуживание автоматически отправляем ответ "занято".
         * Никаких команд Контроллеру не передается.
         */
        if (paramUserCorrect() == false) {
            if (messageType == FromDeviceMessageTypes.LOAD_REQUEST) {
                serverMessage = ServerMessage.BUSY;
            } else {
                serverMessage = ServerMessage.NONE;
            }

            return;
        }


        Log.i("Choreographer", "STEP 2" + messageType);

        /**
         * Запрос на обслуживание.
         * Статус сервера:
         * WAIT_REQUEST - ожидает запрос на обслуживание
         * WAIT_BEGIN   - ожидает начало погрузки
         */
        if (messageType == FromDeviceMessageTypes.LOAD_REQUEST) {

            // Статус сервера: WAIT_REQUEST ("Ожидает запрос на обслуживание")
            if (activity.c.serverStatus == Controller.ServerStatus.WAIT_REQUEST) {

                // Статус ответа: NONE (неопределен)
                if (activity.c.serverAcceptStatus == Controller.ServerAcceptStatus.NONE) {
                    // Это - первичный запрос на обслуживание

                    Log.i("Choreographer", "b1_Call.CallOnClick");

//                    if (activity.c.serverBusyStatus == Controller.ServerBusyStatus.FREE) {
//                        activity.c.controller(B1_Call);
//                    }

                    activity.b1_Call.callOnClick();
                }
                return;
            }
            // Статус ответа: REJECT (отклонен)
            if (activity.c.serverAcceptStatus == Controller.ServerAcceptStatus.REJECT) {
                serverMessage = ServerMessage.NO;
                return;
            }

            /**
             * Если сервер в состоянии "ОНП", а Девайс шлет запросы на обслуживание, то надо послать
             * Девайсу подтверждение запроса на обслуживание еще раз.
             */
            // Статус сервера: WAIT_BEGIN ("Ожидает начало погрузки")
            if (activity.c.serverStatus == Controller.ServerStatus.WAIT_BEGIN) {

                // Статус ответа: ACCEPT (подтвержден)
                if (activity.c.serverAcceptStatus == Controller.ServerAcceptStatus.ACCEPT) {
                    serverMessage = ServerMessage.YES;
                }
            }
            return;
        }


        Log.i("Choreographer", "STEP 3" + messageType);

        /**
         * Сообщение о начале загрузки.
         * Если входящее сообщение "начало погрузки" и сервер "ожидает погрузку", то
         * переходим в режим "погрузка"
         */
        if (messageType == FromDeviceMessageTypes.LOAD_BEGIN) {
            // Статус сервера "Ожидает начало погрузки"
            if (activity.c.serverStatus == Controller.ServerStatus.WAIT_BEGIN) {
                activity.c.weightRemain = getParam("weight");
                activity.b5_getBegin.callOnClick();
                return;
            }
            if (activity.c.serverStatus == Controller.ServerStatus.WAIT_WEIGHT) {
                activity.c.weightRemain = getParam("weight");
                activity.b3_Refresh.callOnClick();
                return;
            }
        }


        Log.i("Choreographer", "STEP 4" + messageType);

        /**
         * Сообщение содержит вес
         *
         */
        if (messageType == FromDeviceMessageTypes.LOAD_WEIGHT) {
            // Статус сервера "ожидает вес"
            if (activity.c.serverStatus == Controller.ServerStatus.WAIT_WEIGHT) {
                activity.c.weightRemain = getParam("weight");
                activity.b3_Refresh.callOnClick();
            }
            return;
        }


        Log.i("Choreographer", "STEP 5" + messageType);

        /**
         * Сообщение - Остановка текущей операции
         */
        if (messageType == FromDeviceMessageTypes.LOAD_STOP) {
            // Статус сервера "ожидает вес"

            if (activity.c.serverStatus == Controller.ServerStatus.WAIT_WEIGHT) {
                activity.b3_Done.callOnClick();
            }
            return;
        }

        /**
         *  Юзер в порядке, но
         *  Тип сообщения не распознан,
         *  сообщение игнорируется
         */
        if (messageType == FromDeviceMessageTypes.UNKNOWN) {
            if (activity.c.serverStatus == Controller.ServerStatus.WAIT_WEIGHT) {
                activity.c.serverStatus = Controller.ServerStatus.WAIT_WEIGHT;
                activity.b3_Refresh.callOnClick();
            }
            return;
        }

    }

    /**
     *
     */
    private class SocketServerReplyThread extends Thread {
        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream
                    outputStream;
            try {
                outputStream
                        = hostThreadSocket.getOutputStream();
                PrintStream printStream
                        = new PrintStream(outputStream);
                printStream
                        .print(formatMessageToReply(requsetTypeIsHTML, makeMessageToReply()));
                printStream
                        .close();
//                message += "replayed: " + msgReply + "\n";
//                activity.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        activity.textViews[1].setText(message);
//                    }
//                });
//
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }
//            activity.runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    activity.textViews[1].setText(message);
//                }
//            });
        }
    }

    // Строка для передачи абоненту
    String makeMessageToReply() {
        Log.i("makeMessageToReply", "serverMessage=" + serverMessage);
        switch (serverMessage) {
            case NAME:
                serverMessage = ServerMessage.NONE;
                return
                        "server='" +
                                activity.getString(R.string.SERVER_NAME) + "'";
            case NONE:
                return
                        activity.messager.msg_Ok() +
                                " MessageType=" + fromDeviceMessageType +
                                " serverStatus=" + activity.c.serverStatus;
            case BUSY:
                serverMessage = ServerMessage.NONE;
                return
                        activity.messager.msg_serviceRequestAccept_No(getParam("oper")) +
                                " MessageType=" + fromDeviceMessageType +
                                " serverStatus=" + activity.c.serverStatus;
            case YES:
                serverMessage = ServerMessage.NONE;
                return
                        activity.messager.msg_serviceRequestAccept_Yes(getParam("oper")) +
                                " MessageType=" + fromDeviceMessageType +
                                " serverStatus=" + activity.c.serverStatus;
            case NO:
                serverMessage = ServerMessage.NONE;
                return
                        activity.messager.msg_serviceRequestAccept_No(getParam("oper")) +
                                " MessageType=" + fromDeviceMessageType +
                                " serverStatus=" + activity.c.serverStatus;
            case STOP:
                serverMessage = ServerMessage.NONE;
                return activity.messager.msg_serviceStop(activity.c.operId);
        }
        return activity.c.printMessageParameters();
    }

    // Выделяем отдельные команды
    public ArrayList<TableElement_MessageParameter> ExtractParametersOfCommand(String inS) {
//        Log.i(logTAG, "ExtractParametersOfCommand: inS='" + inS + "'");
        ArrayList<TableElement_MessageParameter>
                parameters;
        parameters
                = new ArrayList<>();
        // Если это PING
//        Log.i(logTAG, "ExtractParametersOfCommand: who=" + inS.substring(0, 3));
        if (inS.substring(0, 3).equals(activity.getString(R.string.WHO_ARE_YOU_STRING))) {
//            Log.i(logTAG, "ExtractParametersOfCommand: start");
            parameters.add(
                    new TableElement_MessageParameter(activity.getString(R.string.WHO_ARE_YOU_STRING), activity.getString(R.string.WHO_ARE_YOU_STRING)));
        } else {
            Pattern pattern
                    = Pattern.compile(activity.getString(R.string.pattern_Cmd_Name) + "=\'" + activity.getString(R.string.pattern_Cmd_Value) + "\'");
            Matcher matcher
                    = pattern.matcher(inS);
            TableElement_MessageParameter t
                    = new TableElement_MessageParameter();
            while (matcher.find()) {
                t = extractParam(matcher.group());
//                Log.i(logTAG, "ExtractParametersOfCommand: name=" + t.getName() + " value=" + t.getValue());
                parameters.add(t);
            }
        }
        return parameters;
    }

    // Показывает, что такого параметра в списке нет
    public boolean paramGone(String pName) {
        for (TableElement_MessageParameter temp : p) {
            if (temp.getName().equals(pName)) {
                return false;
            }
        }
        return true;
    }

    // Показывает, что такой параметр в списке есть и имеет не пустое значение (???)
    public boolean paramNow(String pName) {
        for (TableElement_MessageParameter temp : p) {
            if (temp.getName().equals(pName)) {
                if (temp.getValue() != null) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    // Показывает, что параметр - не пустой
    public boolean paramNotEmpty(String pName) {
        for (TableElement_MessageParameter temp : p) {
            if (temp.getName().equals(pName)) {
                if (temp.getValue() != null) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    // Сообщение поступило от текущего пользователя
    public boolean paramUserCorrect() {
        /**
         * Определяет корректность идентификтора устройства.
         * Если Девайс уже зарегистрирован на опрацию, то Ид хозяина входящего запроса сравнивается с
         * сохраненным в системе идентификатором Девайса.
         * Если идентификатор девайса не указан - ошибка.
         */

        Log.i("paramUserCorrect", "device=" + getParam("device"));
        Log.i("paramUserCorrect", "activity.c.deviceName=" + activity.c.deviceName);

        if (getParam("device").equals("null")) {
            return false;
        }


        if (activity.c.deviceName == null) {
            return true;
        }

        if (getParam("device").equals(activity.c.deviceName)) {
            return true;
        } else {
            return false;
        }
    }

    // Возвращает значение параметра по его имени
    public String getParam(String pName) {
        for (TableElement_MessageParameter temp : p) {
            if (temp.getName().equals(pName)) {
                return temp.getValue();
            }
        }
        return "null";
    }

    private TableElement_MessageParameter extractParam(String inS) {
        TableElement_MessageParameter retV = null;
        Pattern patternOfName = Pattern.compile("^" + activity.getString(R.string.pattern_Cmd_Name));
        Matcher matcherOfName = patternOfName.matcher(inS);
        if (matcherOfName.find()) {
            Pattern patternOfValue = Pattern.compile("=\'" + activity.getString(R.string.pattern_Cmd_Value) + "\'$");
            Matcher matcherOfValue = patternOfValue.matcher(inS);
            if (matcherOfValue.find()) {
                retV = new TableElement_MessageParameter(matcherOfName.group(), matcherOfValue.group().replace("'", "").replace("=", ""));
            }
        }
        return retV;
    }

    String formatMessageToReply(boolean isHTML, String message) {
//        HTTP/1.1 200 OK
//        Server: nginx/1.2.1
//        Date: Sat, 08 Mar 2014 22:53:46 GMT
//        Content-Type: application/octet-stream
//        Content-Length: 7
//        Last-Modified: Sat, 08 Mar 2014 22:53:30 GMT
//        Connection: keep-alive
//        Accept-Ranges: bytes
//
//                Wisdom
        if (isHTML == false) {
            return message + "\r";
        }
        return
                "HTTP/1.1 200 OK\n" +
                        "Server: nginx/1.2.1\n" +
                        "Date: " + date() + "\n" +
                        "Content-Type: text/plain\n" +
                        "Content-Length: " + String.valueOf(message.length() + 1) + "\n" +
                        "Last-Modified: " + date() + "\n" +
                        "Connection: keep-alive\n" +
                        "Accept-Ranges: bytes\n" +
                        "\n\n" +
                        message + "\n";
    }

    String date() {
        return "Sat, 06 Aug 2017 22:53:46 GMT";
    }

    // Определение типа запроса - HTML или простой
    boolean requestIsHTML(String inS) {
        Pattern pattern = Pattern.compile("GET /");
        Matcher matcher = pattern.matcher(inS);
        if (matcher.find()) {
            return true;
        } else {
            return false;
        }
    }

    public int getPort() {
        return socketServerPORT;
    }

    public void onDestroy() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += "Server running at : "
                                + inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

}
