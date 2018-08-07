package com.example.and.loader;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    String
            logTAG = "Loader: ";

    Button
            b0_Data,
            b1_Call, b1_Exit, b1_Refresh,
            b2_Accept, b2_Reject,
            b3_Abort, b3_Done, b3_Refresh,
            b5_Abort, b5_getBegin, b5_Refresh,
            b4_Done;

    TextView[]
            textViews;
    TextView[]
            textWeight;
    TextView
            statusString;
    static final int
            numOfLayouts = 6;

    LinearLayout[]
            layouts;
    int
            currentLayout;

    static final int
            LAYOUT_0 = 0,
            LAYOUT_1_WAIT = 1,
            LAYOUT_2_REQUEST = 2,
            LAYOUT_3_LOADING = 3,
            LAYOUT_4_DONE = 4,
            LAYOUT_5_WAITBEGIN = 5,
            B0_Start = 101,
            B0_DATA = 102,
            B1_Call = 150,
            B1_Exit = 151,
            B1_Refresh = 152,
            B2_Accept = 201,
            B2_Reject = 202,
            B3_Abort = 301,
            B3_Done = 302,
            B3_Refresh = 303,
            B5_Abort = 501,
            B5_getBegin = 502,
            B5_Refresh = 503,
            B4_Done = 401;

    Controller
            c;
    Messager
            messager;
    Server
            server;
    Config
            config;
    Timer
            timer_ClickButton_B4_Done,
            timer_ClickButton_B2_Accept;
    String
            displayMeggase = "display";

    Handler[]
            handlers_textView,
            handlers_textWeight,
            handlers_layouts;

//    Handler
//            handler1,
//            handler2,
//            handler3,
//            handler4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        config
                = new Config(this);
        c
                = new Controller(this);
        messager
                = new Messager(this);
        server
                = new Server(this);

        layouts = new LinearLayout[numOfLayouts];
        layouts[0] = (LinearLayout) findViewById(R.id.L0);
        layouts[1] = (LinearLayout) findViewById(R.id.L1);
        layouts[2] = (LinearLayout) findViewById(R.id.L2_ServiceRequest);
        layouts[3] = (LinearLayout) findViewById(R.id.L3_Loading);
        layouts[4] = (LinearLayout) findViewById(R.id.L4_Done);
        layouts[5] = (LinearLayout) findViewById(R.id.L5_WaitBegin);
//        layoutVisiblitySet(1);

        statusString = (TextView) findViewById(R.id.text);

        textViews
                = new TextView[numOfLayouts];
        textViews[0] = (TextView) findViewById(R.id.text_0_info);
        textViews[1] = (TextView) findViewById(R.id.text_1_info);
        textViews[2] = (TextView) findViewById(R.id.text_2_info);
        textViews[3] = (TextView) findViewById(R.id.text_3_info);
        textViews[4] = (TextView) findViewById(R.id.text_4_info);
        textViews[5] = (TextView) findViewById(R.id.text_5_info);

        textWeight
                = new TextView[numOfLayouts];
//        textWeight[0] = (TextView) findViewById(R.id.text_0_info);
//        textWeight[1] = (TextView) findViewById(R.id.text_1_info);
//        textWeight[2] = (TextView) findViewById(R.id.text_2_info);
//        textWeight[4] = (TextView) findViewById(R.id.text_4_info);
        textWeight[3] = (TextView) findViewById(R.id.text_3_weigt);
        textWeight[5] = (TextView) findViewById(R.id.text_5_weigt);

        /* Хандлеры лайаутов*/
        handlers_layouts
                = new Handler[numOfLayouts];

        handlers_layouts[0]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LinearLayout layout =
                        (LinearLayout) findViewById(R.id.L0);
                if (msg.obj.equals("VISIBLE")) {
                    layout.setVisibility(VISIBLE);
                } else {
                    layout.setVisibility(INVISIBLE);
                }
            }
        };

        handlers_layouts[1]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LinearLayout layout =
                        (LinearLayout) findViewById(R.id.L1);
                if (msg.obj.equals("VISIBLE")) {
                    layout.setVisibility(VISIBLE);
                } else {
                    layout.setVisibility(INVISIBLE);
                }
            }
        };

        handlers_layouts[2]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LinearLayout layout =
                        (LinearLayout) findViewById(R.id.L2_ServiceRequest);
                if (msg.obj.equals("VISIBLE")) {
                    layout.setVisibility(VISIBLE);
                } else {
                    layout.setVisibility(INVISIBLE);
                }
            }
        };

        handlers_layouts[3]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LinearLayout layout =
                        (LinearLayout) findViewById(R.id.L3_Loading);
                if (msg.obj.equals("VISIBLE")) {
                    layout.setVisibility(VISIBLE);
                } else {
                    layout.setVisibility(INVISIBLE);
                }
            }
        };

        handlers_layouts[4]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LinearLayout layout =
                        (LinearLayout) findViewById(R.id.L4_Done);
                if (msg.obj.equals("VISIBLE")) {
                    layout.setVisibility(VISIBLE);
                } else {
                    layout.setVisibility(INVISIBLE);
                }
            }
        };

        handlers_layouts[5]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                LinearLayout layout =
                        (LinearLayout) findViewById(R.id.L5_WaitBegin);
                if (msg.obj.equals("VISIBLE")) {
                    layout.setVisibility(VISIBLE);
                } else {
                    layout.setVisibility(INVISIBLE);
                }
            }
        };

        layoutVisiblitySet(1);

        /* Хандлеры поля "вес" */
        handlers_textWeight
                = new Handler[numOfLayouts];

        handlers_textWeight[3]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView infoTextView =
                        (TextView) findViewById(R.id.text_3_weigt);
                infoTextView.setText(msg.obj.toString());
            }
        };

        handlers_textWeight[5]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView infoTextView =
                        (TextView) findViewById(R.id.text_5_weigt);
                infoTextView.setText(msg.obj.toString());
            }
        };

        /* Хандлеры текстового поля */
        handlers_textView
                = new Handler[numOfLayouts];

        handlers_textView[0]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView infoTextView =
                        (TextView) findViewById(R.id.text_0_info);
                infoTextView.setText(msg.obj.toString());
            }
        };

        handlers_textView[1]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView infoTextView =
                        (TextView) findViewById(R.id.text_1_info);
                infoTextView.setText(msg.obj.toString());
            }
        };

        handlers_textView[2]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView infoTextView =
                        (TextView) findViewById(R.id.text_2_info);
                infoTextView.setText(msg.obj.toString());
            }
        };

        handlers_textView[3]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView infoTextView =
                        (TextView) findViewById(R.id.text_3_info);
                infoTextView.setText(msg.obj.toString());
            }
        };

        handlers_textView[4]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView infoTextView =
                        (TextView) findViewById(R.id.text_4_info);
                infoTextView.setText(msg.obj.toString());
            }
        };

        handlers_textView[5]
                = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView infoTextView =
                        (TextView) findViewById(R.id.text_5_info);
                infoTextView.setText(msg.obj.toString());
            }
        };

        /* Управление выводом видимостью слоёв  */

        // b0_Data
        b0_Data = (Button) findViewById(R.id.button_0_data_now);
        b0_Data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B0_DATA);
            }
        });

        // b1_Call
        b1_Call = (Button) findViewById(R.id.button_1_Call);
        b1_Call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c.serverBusyStatus == Controller.ServerBusyStatus.FREE) {
                    c.controller(B1_Call);
                }
            }
        });

        // b1_Exit
        b1_Exit = (Button) findViewById(R.id.button_1_Done);
        b1_Exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B1_Exit);
            }
        });

//        // b1_Refresh
//        b1_Refresh = (Button) findViewById(R.id.button_1_Refresh);
//        b1_Refresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                c.controller(B1_Refresh);
//            }
//        });
//
        // b2_Accept
        b2_Accept = (Button) findViewById(R.id.button_2_Ok);
        b2_Accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B2_Accept);
            }
        });

        //b2_Reject
        b2_Reject = (Button) findViewById(R.id.button_2_Cancel);
        b2_Reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B2_Reject);
            }
        });

        // b3_Abort
        b3_Abort = (Button) findViewById(R.id.button_3_Cancel);
        b3_Abort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B3_Abort);
            }
        });

        // b3_Done
        b3_Done = (Button) findViewById(R.id.button_3_Ok);
        b3_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B3_Done);
            }
        });

        // b3_Refresh
        b3_Refresh = (Button) findViewById(R.id.button_3_Refresh);
        b3_Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B3_Refresh);
            }
        });

        // b5_Abort
        b5_Abort = (Button) findViewById(R.id.button_5_Cancel);
        b5_Abort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B5_Abort);
            }
        });

        // b5_getBegin
        b5_getBegin = (Button) findViewById(R.id.button_5_getBegin);
        b5_getBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B5_getBegin);
            }
        });

        // b5_Refresh
        b5_Refresh = (Button) findViewById(R.id.button_5_Refresh);
        b5_Refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.controller(B5_Refresh);
            }
        });

        // b4_Done
        b4_Done = (Button) findViewById(R.id.button_4_Ok);
        b4_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer_ClickButton_B4_Done != null) {
                    timer_ClickButton_B4_Done.cancel();
                    timer_ClickButton_B4_Done = null;
                }
                c.controller(B4_Done);
            }
        });

        //
        c.controller(B0_Start);

    }

    // Переключение на слой
    void gotoLayout(int newLayout, String pTextToInfo) {

        Log.i("gotoLayout", "Start");

        String textToInfo = pTextToInfo;
        currentLayout = newLayout;
        layoutVisiblitySet(newLayout);                          // Установить видимость слоя
        switch (newLayout) {
            case LAYOUT_0:
                break;
            case LAYOUT_1_WAIT:
                break;
            case LAYOUT_2_REQUEST:

                Log.i("gotoLayout", "LAYOUT_2_REQUEST");

                // Через 5 секунд после попадания в экран нажать кнопку "Принято"
                timer_ClickButton_B2_Accept = new Timer();
                ClickButton_B2_Accept click2 = new ClickButton_B2_Accept();
                timer_ClickButton_B2_Accept.schedule(click2, 10000);
                break;
            case LAYOUT_3_LOADING:
                break;
            case LAYOUT_4_DONE:
                Beep();
                Log.i("gotoLayout","LAYOUT_4_DONE !!!");
                // Через 5 секунд после попадания в экран нажать кнопку "Закончить"
                timer_ClickButton_B4_Done = new Timer();
                ClickButton_B4_Done click4 = new ClickButton_B4_Done();
                timer_ClickButton_B4_Done.schedule(click4, 5000);
                break;
            case LAYOUT_5_WAITBEGIN:
                break;
        }
        setTextInLayout(newLayout, textToInfo);     // Сообщение в поле инфо
    }

    // Задержка времени и выполнение задачи в LAYOUT_4_DONE
    class ClickButton_B4_Done extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    timer_ClickButton_B4_Done.cancel();
                    b4_Done.callOnClick();
                }
            });
        }
    }

    // Задержка времени и выполнение задачи в LAYOUT_4_DONE
    class ClickButton_B2_Accept extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    timer_ClickButton_B2_Accept.cancel();
                    b2_Accept.callOnClick();
                }
            });
        }
    }

    // Устанавливает видимость экрана по номеру
    void layoutVisiblitySet(int layoutToSet) {


        for (int i = 0; i < numOfLayouts; i++) {
            Message msg = new Message();
            if (i == layoutToSet) {
                msg.obj = "VISIBLE";
            } else {
                msg.obj = "INVISIBLE";
            }
            handlers_layouts[i].sendMessage(msg);
        }
//        for (int i = 0; i < numOfLayouts; i++) {
//            if (i == layoutToSet) {
//                layouts[i].setVisibility(VISIBLE);
//            } else {
//                layouts[i].setVisibility(INVISIBLE);
//            }
//    }
    }

    // Текст в инфоблоке лайаута
    void setTextInLayout(int n, String s) {

        Message msg = new Message();
        msg.obj = s;
        handlers_textView[n].sendMessage(msg);

//        this.textViews[n].setText(s);
    }

    //
    class Config {
        MainActivity
                a;
        String
                ipAddress = "";
        String
                networkMask = "";
        String
                serverName;
        int
                serverPort;
        String
                deviceName;
        boolean
                is_Connected_to_network = false;

        public Config(MainActivity activity) {
            this.a
                    = activity;
            serverName
                    = a.getString(R.string.SERVER_NAME);
            serverPort
                    = Integer.parseInt(a.getString(R.string.SERVER_PORT));
        }


    }

    public void printOut(String m) {
        statusString
                .setText(m);
    }

    void Beep() {
        /**
         * Звуковой сигнал о запросе на погрузку
         */
        try {
            Uri notify
                    = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r
                    = RingtoneManager.getRingtone(getApplicationContext(), notify);
            r
                    .play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void setTextViews(String pmp, String wr) {
        textViews[3]
                .setText(pmp);
        textWeight[3]
                .setText(wr);
    }

    /**
     * Вывод "тоста"
     *
     * @param whatSay
     */
    public void sayToast(String whatSay) {
        Beep();
        Toast toast
                = Toast.makeText(this, whatSay, Toast.LENGTH_LONG);
        toast
                .show();
    }

}
