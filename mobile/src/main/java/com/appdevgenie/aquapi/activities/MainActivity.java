package com.appdevgenie.aquapi.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.appdevgenie.aquapi.R;
import com.appdevgenie.aquapi.models.Control;
import com.appdevgenie.aquapi.models.PiStatus;
import com.appdevgenie.aquapi.models.TemperatureInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_AQUA_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_CONTROL;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_STATUS;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_TEMPERATURES;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private TextView tvIP, tvPiStatus, tvTime, tvArduinoStatus, tvRoomTemp, tvSystemTemp, tvWaterTemp;
    private ImageView ivIp, ivPiStatus, ivArduinoStatus, ivBypass, ivLight;
    private ProgressBar pbStatus, pbTemps, pbRoom, pbSystem, pbWater;

    private ToggleButton tbBypass, tbLight;
    //AnimationDrawable animationDrawable;
    private ArrayList<TemperatureInfo> infoArrayList;
    private Control control = new Control();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(DB_CHILD_AQUA_PI);

        tvIP = findViewById(R.id.tvIpAddress);
        tvPiStatus = findViewById(R.id.tvPiStatus);
        tvTime = findViewById(R.id.tvTime);
        tvArduinoStatus = findViewById(R.id.tvArduinoStatus);
        ivIp = findViewById(R.id.ivIpAddress);
        ivPiStatus = findViewById(R.id.ivPiStatus);
        ivArduinoStatus = findViewById(R.id.ivArduinoStatus);
        pbStatus = findViewById(R.id.pbStatus);
        pbTemps = findViewById(R.id.pbTemps);
        ivBypass = findViewById(R.id.ivControlBypass);
        ivLight = findViewById(R.id.ivControlLight);
        tbBypass = findViewById(R.id.tbBypass);
        tbBypass.setOnCheckedChangeListener(this);
        tbLight = findViewById(R.id.tbLight);
        tbLight.setOnCheckedChangeListener(this);
        if (!tbBypass.isChecked()) {
            tbLight.setClickable(false);
            tbLight.setEnabled(false);
        } else {
            tbLight.setClickable(true);
            tbLight.setEnabled(true);
        }

        tbLight.setChecked(true);
        //ivTempLed = findViewById(R.id.ivTempLed);
        Button bReboot = findViewById(R.id.bControlReboot);
        bReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRebootDialog();
            }
        });

        ConstraintLayout clRoomTemp = findViewById(R.id.includeRoomTemp);
        TextView tvRoomTitle = clRoomTemp.findViewById(R.id.tvTempTitle);
        tvRoomTitle.setText("Room");
        tvRoomTemp = clRoomTemp.findViewById(R.id.tvTemperature);
        pbRoom = clRoomTemp.findViewById(R.id.pbTemperature);
        clRoomTemp.setOnClickListener(this);

        ConstraintLayout clSystemTemp = findViewById(R.id.includeSystemTemp);
        TextView tvSystemTitle = clSystemTemp.findViewById(R.id.tvTempTitle);
        tvSystemTitle.setText("System");
        tvSystemTemp = clSystemTemp.findViewById(R.id.tvTemperature);
        pbSystem = clSystemTemp.findViewById(R.id.pbTemperature);
        clSystemTemp.setOnClickListener(this);

        ConstraintLayout clWaterTemp = findViewById(R.id.includeWaterTemp);
        TextView tvWaterTitle = clWaterTemp.findViewById(R.id.tvTempTitle);
        tvWaterTitle.setText("Water");
        tvWaterTemp = clWaterTemp.findViewById(R.id.tvTemperature);
        pbWater = clWaterTemp.findViewById(R.id.pbTemperature);
        clWaterTemp.setOnClickListener(this);

        /*ImageView imageView = findViewById(R.id.ivLed);
        imageView.setBackgroundResource(R.drawable.animation);
        //animationDrawable = (AnimationDrawable) imageView.getBackground();
        imageView.post(new Runnable() {
            @Override
            public void run() {
                animationDrawable = (AnimationDrawable) imageView.getBackground();
                animationDrawable.start();
            }
        });

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animationDrawable.start();
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animationDrawable.stop();
            }
        });*/

        /*ObjectAnimator objAnimator = ObjectAnimator.ofInt(imageView, "alpha",R.drawable.led_off, R.drawable.led_red,
                R.drawable.led_off);
        objAnimator.setDuration(1000);
        objAnimator.setRepeatCount(100);
        //objAnimator.setRepeatMode(Animation.REVERSE);
        objAnimator.setRepeatCount(Animation.INFINITE);
        objAnimator.start();*/

        infoArrayList = TemperatureInfo.createTempList();

        setupFirebaseListener();
    }

    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }*/

    private void setupFirebaseListener() {

        pbStatus.setVisibility(View.VISIBLE);
        pbTemps.setVisibility(View.VISIBLE);

        Query queryStatus = databaseReference
                .child(DB_CHILD_STATUS);

        Query queryTemps = databaseReference
                .child(DB_CHILD_TEMPERATURES);

        Query queryControl = databaseReference
                .child(DB_CHILD_CONTROL);

        queryStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChanged: " + dataSnapshot.getValue());
                pbStatus.setVisibility(View.GONE);
                pbTemps.setVisibility(View.GONE);

                PiStatus piStatus = dataSnapshot.getValue(PiStatus.class);
                if (piStatus != null) {
                    if (piStatus.isOnlinePi()) {

                        ivPiStatus.setImageResource(R.drawable.led_green);
                        tvPiStatus.setText("Pi connected: ");
                        tvTime.setText(getFormattedDate(piStatus.getLastOnlinePi()));
                        ivIp.setImageResource(R.drawable.led_green);
                        tvIP.setText(piStatus.getIp());

                        queryTemps.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (TemperatureInfo tempInfo : infoArrayList) {
                                    TemperatureInfo temperatureInfo = dataSnapshot.child(tempInfo.getName()).getValue(TemperatureInfo.class);
                                    String name = dataSnapshot.child(tempInfo.getName()).getKey();

                                    if (tempInfo.getName().equals(name)) {
                                        if (temperatureInfo != null) {

                                            switch (name) {
                                                case "water":
                                                    infoArrayList.get(0).setTemp(temperatureInfo.getTemp());
                                                    infoArrayList.get(0).setTempMax(temperatureInfo.getTempMax());
                                                    infoArrayList.get(0).setTempMin(temperatureInfo.getTempMin());
                                                    infoArrayList.get(0).setMinTimeStamp(temperatureInfo.getMinTimeStamp());
                                                    infoArrayList.get(0).setMaxTimeStamp(temperatureInfo.getMaxTimeStamp());
                                                    float tempWater = temperatureInfo.getTemp();
                                                    tvWaterTemp.setText(String.format(Locale.getDefault(), "%.2f", tempWater));
                                                    pbWater.setProgress((int) tempWater);
                                                    break;

                                                case "system":
                                                    infoArrayList.get(1).setTemp(temperatureInfo.getTemp());
                                                    infoArrayList.get(1).setTempMax(temperatureInfo.getTempMax());
                                                    infoArrayList.get(1).setTempMin(temperatureInfo.getTempMin());
                                                    infoArrayList.get(1).setMinTimeStamp(temperatureInfo.getMinTimeStamp());
                                                    infoArrayList.get(1).setMaxTimeStamp(temperatureInfo.getMaxTimeStamp());
                                                    float tempSystem = temperatureInfo.getTemp();
                                                    tvSystemTemp.setText(String.format(Locale.getDefault(), "%.2f", tempSystem));
                                                    pbSystem.setProgress((int) tempSystem);
                                                    break;

                                                case "room":
                                                    infoArrayList.get(2).setTemp(temperatureInfo.getTemp());
                                                    infoArrayList.get(2).setTempMax(temperatureInfo.getTempMax());
                                                    infoArrayList.get(2).setTempMin(temperatureInfo.getTempMin());
                                                    infoArrayList.get(2).setMinTimeStamp(temperatureInfo.getMinTimeStamp());
                                                    infoArrayList.get(2).setMaxTimeStamp(temperatureInfo.getMaxTimeStamp());
                                                    float tempRoom = temperatureInfo.getTemp();
                                                    tvRoomTemp.setText(String.format(Locale.getDefault(), "%.2f", tempRoom));
                                                    pbRoom.setProgress((int) tempRoom);
                                                    break;
                                            }
                                        }
                                    }
                                }

                                //pbTemps.setVisibility(View.GONE);

                                /*Temperatures temperatures = dataSnapshot.getValue(Temperatures.class);
                                if (temperatures != null) {
                                    //ivTempLed.setImageResource(R.drawable.led_green);

                                    float tempRoom = temperatures.getTempRoom();
                                    tvRoomTemp.setText(String.format(Locale.getDefault(), "%.2f", tempRoom));
                                    pbRoom.setProgress((int) tempRoom);

                                    float tempWater = temperatures.getTempWater();
                                    tvWaterTemp.setText(String.format(Locale.getDefault(), "%.2f", tempWater));
                                    pbWater.setProgress((int) tempWater);

                                    float tempSystem = temperatures.getTempSystem();
                                    tvSystemTemp.setText(String.format(Locale.getDefault(), "%.2f", tempSystem));
                                    pbSystem.setProgress((int) tempSystem);
                                }*/
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                pbTemps.setVisibility(View.GONE);
                            }
                        });

                        queryControl.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Control controlData = dataSnapshot.getValue(Control.class);
                                //control.setLightOn(controlData.isLightOn());
                                tbLight.setChecked(controlData.isLightOn());

                                //control.setBypassOn(controlData.isBypassOn());
                                tbBypass.setChecked(controlData.isBypassOn());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        /*float tempRoom = piStatus.getTempRoom();
                        tvRoomTemp.setText(String.format(Locale.getDefault(), "%.2f", tempRoom));
                        pbRoom.setProgress((int) tempRoom + 5);

                        float tempWater = piStatus.getTempWater();
                        tvWaterTemp.setText(String.format(Locale.getDefault(), "%.2f", tempWater));
                        pbWater.setProgress((int) tempWater + 5);

                        float tempSystem = piStatus.getTempSystem();
                        tvSystemTemp.setText(String.format(Locale.getDefault(), "%.2f", tempSystem));
                        pbSystem.setProgress((int) tempSystem + 5);*/

                        if (piStatus.isOnlineArduino()) {
                            ivArduinoStatus.setImageResource(R.drawable.led_green);
                            tvArduinoStatus.setText("Arduino connected");
                        } else {
                            ivArduinoStatus.setImageResource(R.drawable.led_red);
                            tvArduinoStatus.setText("Arduino not connected");
                        }

                    } else {

                        ivPiStatus.setImageResource(R.drawable.led_red);
                        tvPiStatus.setText("Offline: ");
                        tvTime.setText(getFormattedDate(piStatus.getLastOnlinePi()));
                        ivIp.setImageResource(R.drawable.led_red);
                        tvIP.setText("Pi not connected");
                        ivArduinoStatus.setImageResource(R.drawable.led_red);
                        tvArduinoStatus.setText("Arduino not connected");
                        //ivTempLed.setImageResource(R.drawable.led_red);

                        pbSystem.setProgress(0);
                        pbRoom.setProgress(0);
                        pbWater.setProgress(0);
                        tvWaterTemp.setText("- - . - -");
                        tvRoomTemp.setText("- - . - -");
                        tvSystemTemp.setText("- - . - -");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                pbStatus.setVisibility(View.GONE);
                pbTemps.setVisibility(View.GONE);
            }
        });
    }

    private String getFormattedDate(long timestamp) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        Calendar now = Calendar.getInstance();

        final String timeFormatString = "HH:mm";
        final String dateTimeFormatString = "EEE, MMM d, HH:mm";

        if (now.get(Calendar.DATE) == calendar.get(Calendar.DATE)) {
            return DateFormat.format(timeFormatString, calendar).toString();
        } else if (now.get(Calendar.DATE) - calendar.get(Calendar.DATE) == 1) {
            return "yesterday " + DateFormat.format(timeFormatString, calendar);
        } else if (now.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, calendar).toString();
        } else {
            return DateFormat.format("MMM dd yyyy, HH:mm", calendar).toString();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if (!tbBypass.isChecked()) {
            tbLight.setClickable(false);
            tbLight.setEnabled(false);
        } else {
            tbLight.setClickable(true);
            tbLight.setEnabled(true);
        }

        switch (compoundButton.getId()) {

            case R.id.tbBypass:

                if (b) {
                    ivBypass.setImageResource(R.drawable.led_red);
                } else {
                    ivBypass.setImageResource(R.drawable.led_off);
                }
                control.setBypassOn(b);
                databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                break;

            case R.id.tbLight:
                if (b) {
                    ivLight.setImageResource(R.drawable.led_green);
                } else {
                    ivLight.setImageResource(R.drawable.led_off);
                }
                control.setLightOn(b);
                databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void openRebootDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        View layoutView = getLayoutInflater().inflate(R.layout.dialog_reboot, null);

        Button bRebootPi = layoutView.findViewById(R.id.bRebootPi);
        Button bRebootArduino = layoutView.findViewById(R.id.bRebootArduino);

        dialogBuilder.setView(layoutView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

        bRebootPi.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Toast.makeText(MainActivity.this, "Resetting Raspberry Pi", Toast.LENGTH_LONG).show();
                        control.setRebootPi(Boolean.TRUE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                        break;

                    case MotionEvent.ACTION_UP:
                        //Toast.makeText(MainActivity.this, "up", Toast.LENGTH_LONG).show();
                        control.setRebootPi(Boolean.FALSE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                        break;
                }

                return false;
            }
        });

        bRebootArduino.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Toast.makeText(MainActivity.this, "down", Toast.LENGTH_LONG).show();
                        control.setResetArduino(Boolean.TRUE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                        break;

                    case MotionEvent.ACTION_UP:
                        Toast.makeText(MainActivity.this, "up", Toast.LENGTH_LONG).show();
                        control.setResetArduino(Boolean.FALSE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                        break;
                }

                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.includeWaterTemp:
                openTemperatureInfoDialog(0);
                //Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.includeSystemTemp:
                openTemperatureInfoDialog(1);
                //Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
                break;

            case R.id.includeRoomTemp:
                openTemperatureInfoDialog(2);
                //Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    private void openTemperatureInfoDialog(int index) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        View layoutView = getLayoutInflater().inflate(R.layout.dialog_temperature_info, null);

        TextView tvMin = layoutView.findViewById(R.id.tvTempInfoMin);
        tvMin.setText(
                TextUtils.concat(String.format(Locale.getDefault(),
                        "%.2f", infoArrayList.get(index).getTempMin()), " \u00b0C"));
        TextView tvMax = layoutView.findViewById(R.id.tvTempInfoMax);
        tvMax.setText(
                TextUtils.concat(String.format(Locale.getDefault(),
                        "%.2f", infoArrayList.get(index).getTempMax()), " \u00b0C"));

        TextView tvMinTime = layoutView.findViewById(R.id.tvTempMinTime);
        tvMinTime.setText(getFormattedDate(infoArrayList.get(index).getMinTimeStamp()));
        TextView tvMaxTime = layoutView.findViewById(R.id.tvTempMaxTime);
        tvMaxTime.setText(getFormattedDate(infoArrayList.get(index).getMaxTimeStamp()));

        dialogBuilder.setView(layoutView);
        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();

    }
}
