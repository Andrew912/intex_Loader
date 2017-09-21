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

/**
 * Created by Андрей on 05.08.2017.
 */

public class Server {

    String
            logTAG = "SERVER: ";
    MainActivity
            a;
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
        BUSY    // для "чужих" запросов - "ЗАНЯТ"
    }

    ServerMessage
            serverMessage = ServerMessage.NONE;

    enum FromDeviceMessageTypes {
        LOAD_REQUEST, LOAD_BEGIN, LOAD_WEIGHT, LOAD_STOP, UNKNOWN
    }

    FromDeviceMessageTypes
            fromDeviceMessageType;

    public Server(MainActivity activity) {
        this.a = activity;
        Thread socketServerThread = new Thread(new SocketServerThread());
        socketServerThread.start();
    }

    // Определение типа входящего сообщения и реакция на него
    FromDeviceMessageTypes messageTypeIs() {
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
                    a.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            a.printOut(finalMsg);
                            // Определяем тип входящего сообщения и реакцию на него
                            Choreographer();

                        }
                    });
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
        FromDeviceMessageTypes messageType = messageTypeIs();
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

        /**
         * Запрос на обслуживание.
         * Статус сервера:
         * WAIT_REQUEST - ожидает запрос на обслуживание
         * WAIT_BEGIN   - ожидает начало погрузки
         */
        if (messageType == FromDeviceMessageTypes.LOAD_REQUEST) {

            // Статус сервера: WAIT_REQUEST ("Ожидает запрос на обслуживание")
            if (a.c.serverStatus == Controller.ServerStatus.WAIT_REQUEST) {

                // Статус ответа: NONE (неопределен)
                if (a.c.serverAcceptStatus == Controller.ServerAcceptStatus.NONE) {
                    // Это - первичный запрос на обслуживание
                    a.b1_Call.callOnClick();
                }
                return;
            }
            // Статус ответа: REJECT (отклонен)
            if (a.c.serverAcceptStatus == Controller.ServerAcceptStatus.REJECT) {
                serverMessage = ServerMessage.NO;
                return;
            }

            /**
             * Если сервер в состоянии "ОНП", а Девайс шлет запросы на обслуживание, то надо послать
             * Девайсу подтверждение запроса на обслуживание еще раз.
             */
            // Статус сервера: WAIT_BEGIN ("Ожидает начало погрузки")
            if (a.c.serverStatus == Controller.ServerStatus.WAIT_BEGIN) {

                // Статус ответа: ACCEPT (подтвержден)
                if (a.c.serverAcceptStatus == Controller.ServerAcceptStatus.ACCEPT) {
                    serverMessage = ServerMessage.YES;
                }
            }
            return;
        }

        /**
         * Сообщение о начале загрузки.
         * Если входящее сообщение "начало погрузки" и сервер "ожидает погрузку", то
         * переходим в режим "погрузка"
         */
        if (messageType == FromDeviceMessageTypes.LOAD_BEGIN) {
            // Статус сервера "Ожидает начало погрузки"
            if (a.c.serverStatus == Controller.ServerStatus.WAIT_BEGIN) {
                a.c.weightRemain = getParam("weight");
                a.b5_getBegin.callOnClick();
                return;
            }
            if (a.c.serverStatus == Controller.ServerStatus.WAIT_WEIGHT) {
                a.c.weightRemain = getParam("weight");
                a.b3_Refresh.callOnClick();
                return;
            }
        }

        /**
         * Сообщение содержит вес
         *
         */
        if (messageType == FromDeviceMessageTypes.LOAD_WEIGHT) {
            // Статус сервера "ожидает вес"
            if (a.c.serverStatus == Controller.ServerStatus.WAIT_WEIGHT) {
                a.c.weightRemain = getParam("weight");
                a.b3_Refresh.callOnClick();
            }
            return;
        }

        /**
         * Сообщение - Остановка текущей операции
         */
        if (messageType == FromDeviceMessageTypes.LOAD_STOP) {
            // Статус сервера "ожидает вес"
            if (a.c.serverStatus == Controller.ServerStatus.WAIT_WEIGHT) {
                a.b3_Done.callOnClick();
            }
            return;
        }

        /**
         *  Юзер в порядке, но
         *  Тип сообщения не распознан,
         *  сообщение игнорируется
         */
        if (messageType == FromDeviceMessageTypes.UNKNOWN) {
            if (a.c.serverStatus == Controller.ServerStatus.WAIT_WEIGHT) {
                a.c.serverStatus = Controller.ServerStatus.WAIT_WEIGHT;
                a.b3_Refresh.callOnClick();
            }
            return;
        }

    }

    private class SocketServerReplyThread extends Thread {
        private Socket hostThreadSocket;
        int cnt;

        SocketServerReplyThread(Socket socket, int c) {
            hostThreadSocket = socket;
            cnt = c;
        }

        @Override
        public void run() {
            OutputStream outputStream;
            try {
                outputStream = hostThreadSocket.getOutputStream();
                PrintStream printStream = new PrintStream(outputStream);
                printStream.print(formatMessageToReply(requsetTypeIsHTML, makeMessageToReply()));
                printStream.close();
//                message += "replayed: " + msgReply + "\n";
//                a.runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        a.textViews[1].setText(message);
//                    }
//                });
//
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                message += "Something wrong! " + e.toString() + "\n";
            }
//            a.runOnUiThread(new Runnable() {
//
//                @Override
//                public void run() {
//                    a.textViews[1].setText(message);
//                }
//            });
        }
    }

    // Строка для передачи абоненту
    String makeMessageToReply() {
        switch (serverMessage) {
            case NONE:
                return a.messager.msg_Ok() + " MessageType=" + fromDeviceMessageType + " serverStatus=" + a.c.serverStatus;
            case BUSY:
                serverMessage = ServerMessage.NONE;
                return a.messager.msg_serviceRequestAccept_No(getParam("oper")) + " MessageType=" + fromDeviceMessageType + " serverStatus=" + a.c.serverStatus;
            case YES:
                serverMessage = ServerMessage.NONE;
                return a.messager.msg_serviceRequestAccept_Yes(getParam("oper")) + " MessageType=" + fromDeviceMessageType + " serverStatus=" + a.c.serverStatus;
            case NO:
                serverMessage = ServerMessage.NONE;
                return a.messager.msg_serviceRequestAccept_No(getParam("oper")) + " MessageType=" + fromDeviceMessageType + " serverStatus=" + a.c.serverStatus;
            case STOP:
                serverMessage = ServerMessage.NONE;
                return a.messager.msg_serviceStop(a.c.operId);
        }
        return a.c.printMessageParameters();
    }

    // Выделяем отдельные команды
    public ArrayList<TableElement_MessageParameter> ExtractParametersOfCommand(String inS) {
        Log.i(logTAG, "ExtractParametersOfCommand: start");
        ArrayList<TableElement_MessageParameter> parameters;
        parameters = new ArrayList<>();
        Pattern pattern = Pattern.compile(a.getString(R.string.pattern_Cmd_Name) + "=\'" + a.getString(R.string.pattern_Cmd_Value) + "\'");
        Matcher matcher = pattern.matcher(inS);
        TableElement_MessageParameter t = new TableElement_MessageParameter();
        while (matcher.find()) {
            t = extractParam(matcher.group());
            Log.i(logTAG, "ExtractParametersOfCommand: name=" + t.getName() + " value=" + t.getValue());
            parameters.add(t);
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
        if (getParam("device").equals("null")) {
            return false;
        }

        if (a.c.deviceName == null) {
            return true;
        }

        if (getParam("device").equals(a.c.deviceName)) {
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
        Pattern patternOfName = Pattern.compile("^" + a.getString(R.string.pattern_Cmd_Name));
        Matcher matcherOfName = patternOfName.matcher(inS);
        if (matcherOfName.find()) {
            Pattern patternOfValue = Pattern.compile("=\'" + a.getString(R.string.pattern_Cmd_Value) + "\'$");
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
            return message;
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
