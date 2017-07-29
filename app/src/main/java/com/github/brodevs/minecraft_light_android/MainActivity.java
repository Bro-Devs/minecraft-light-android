package com.github.brodevs.minecraft_light_android;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Rcon rcon;
    private int DAY_LUMINOSITY_THRESHOLD = 1000;
    private EditText addressEditText;
    private EditText portEditText;
    private EditText passwordEditText;
    private Button connectButton;
    private SensorEventListener sensorEventListener;
    private FrameLayout serverView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addressEditText = findViewById(R.id.activity_main_server_address);
        portEditText = findViewById(R.id.activity_main_server_port);
        passwordEditText = findViewById(R.id.activity_main_server_password);
        connectButton = findViewById(R.id.activity_main_button_connect);
        serverView = findViewById(R.id.activity_main_view_server);

        sensorEventListener = this;

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectToServer();
                sensorManager.registerListener(sensorEventListener, lightSensor, SensorManager.SENSOR_DELAY_UI);
                serverView.setVisibility(View.GONE);
            }
        });

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }



    private void connectToServer(){

        new Thread(new Runnable(){
            @Override
            public void run(){
                try {
                    rcon = new Rcon(
                            addressEditText.getText().toString(),
                            Integer.parseInt(portEditText.getText().toString()),
                            passwordEditText.getText().toString().getBytes());

                // TODO: Handle exceptions
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AuthenticationException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float light = sensorEvent.values[0];
        if (rcon != null){
            final int mineTime = (int) ((light * 3000/DAY_LUMINOSITY_THRESHOLD) + 22000);

            // x = luminance
            // y = minecraft time
            // x : 3000 = y : 1000
            // y = (x * 3000/1000)
            // TODO: Explain why we're starting from 29000


            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        if (mineTime>29000) {
                            rcon.command("time set " + 29000);
                        } else {
                            rcon.command("time set " + mineTime);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Nothing to do here
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Maybe register sensor listener again here after server connection
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
