package com.appdevgenie.aquapi.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_AQUA_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_CONTROL;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_STATUS;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_TEMPERATURES;

public class TestDisplayActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private ToggleButton tbWater, tbSystem, tbRoom;
    private Button bBypass, bLight;
    private boolean avoidRecursions = false;
    //private ToggleButton tbBypass, tbLight;
    private ArrayList<TemperatureInfo> infoArrayList;
    private Control control = new Control();
    private ProgressBar progressBar;
    private TextView tvIP, tvPiStatus, tvTime, tvArduinoStatus, tvNowTemp, tvMinTemp, tvMaxTemp, tvSelection, tvTimeMin, tvTimeMax;
    private ImageView ivIp, ivPiStatus, ivArduinoStatus, ivBypass, ivLight;
    private TextView tvLight, tvBypass;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_digital_display);

        init();
    }

    private void init() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child(DB_CHILD_AQUA_PI);

        Typeface custom_font = Typeface.createFromAsset(getAssets(), "digital_it.ttf");
        ConstraintLayout c1 = findViewById(R.id.include_digital);
        TextView tvTempNow = c1.findViewById(R.id.tvTempNow);
        TextView tvTempMax = c1.findViewById(R.id.tvTempMax);
        TextView tvTempMin = c1.findViewById(R.id.tvTempMin);
        tvTempNow.setTypeface(custom_font);
        tvTempMax.setTypeface(custom_font);
        tvTempMin.setTypeface(custom_font);

        progressBar = findViewById(R.id.progressBar);

        tvIP = findViewById(R.id.tvIpAddress);
        tvPiStatus = findViewById(R.id.tvPiStatus);
        tvTime = findViewById(R.id.tvTime);
        tvBypass = findViewById(R.id.tvBypass);
        tvLight = findViewById(R.id.tvLight);
        tvArduinoStatus = findViewById(R.id.tvArduinoStatus);
        tvNowTemp = findViewById(R.id.tvTempNow);
        tvMinTemp = findViewById(R.id.tvTempMin);
        tvMaxTemp = findViewById(R.id.tvTempMax);
        tvSelection = findViewById(R.id.tvSelection);
        tvTimeMin = findViewById(R.id.tvTimeMin);
        tvTimeMax = findViewById(R.id.tvTimeMax);
        TextClock tvClock = findViewById(R.id.tvClock);
        tvClock.setTypeface(custom_font);
        /*Date today = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:MM", Locale.UK);
        tvClock.setText(formatter.format(today));*/

        ivIp = findViewById(R.id.ivIpAddress);
        ivPiStatus = findViewById(R.id.ivPiStatus);
        ivArduinoStatus = findViewById(R.id.ivArduinoStatus);
        ivBypass = findViewById(R.id.ivBypass);
        ivLight = findViewById(R.id.ivLight);

        ConstraintLayout c2 = findViewById(R.id.include_digital_control);
        tbWater = c2.findViewById(R.id.tbWater);
        tbSystem = c2.findViewById(R.id.tbSystem);
        tbRoom = c2.findViewById(R.id.tbRoom);
        /*tbBypass = findViewById(R.id.tbBypass);
        tbBypass.setOnCheckedChangeListener(this);
        tbLight = findViewById(R.id.tbLight);
        tbLight.setOnCheckedChangeListener(this);
        if (!tbBypass.isChecked()) {
            tbLight.setClickable(false);
            tbLight.setEnabled(false);
        } else {
            tbLight.setClickable(true);
            tbLight.setEnabled(true);
        }*/
        //r0.setTypeface(custom_font);
        //r1.setTypeface(custom_font);
        //r2.setTypeface(custom_font);
        tbWater.setOnCheckedChangeListener(this);
        tbSystem.setOnCheckedChangeListener(this);
        tbRoom.setOnCheckedChangeListener(this);

        bBypass = findViewById(R.id.bBypass);
        bBypass.setOnClickListener(this);
        bLight = findViewById(R.id.bLight);
        bLight.setOnClickListener(this);
        /*bBypass.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.performClick();

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Toast.makeText(TestDisplayActivity.this, "bypass down", Toast.LENGTH_LONG).show();
                        control.setBypassOn(Boolean.TRUE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                        break;

                    case MotionEvent.ACTION_UP:
                        Toast.makeText(TestDisplayActivity.this, "up", Toast.LENGTH_LONG).show();
                        control.setBypassOn(Boolean.FALSE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                        break;
                }

                return false;
            }
        });*/

        infoArrayList = TemperatureInfo.createTempList();

        setupFirebaseListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.reboot_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_reboot:
                openRebootDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupFirebaseListener() {

        progressBar.setVisibility(View.VISIBLE);

        Query queryStatus = databaseReference
                .child(DB_CHILD_STATUS);

        Query queryTemps = databaseReference
                .child(DB_CHILD_TEMPERATURES);

        Query queryControl = databaseReference
                .child(DB_CHILD_CONTROL);

        queryStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Log.d(TAG, "onDataChanged: " + dataSnapshot.getValue());
                progressBar.setVisibility(View.GONE);

                PiStatus piStatus = dataSnapshot.getValue(PiStatus.class);
                if (piStatus != null) {
                    if (piStatus.isOnlinePi()) {

                        ivPiStatus.setImageResource(R.drawable.circle_on);
                        //tvPiStatus.setText("Raspberry Pi");
                        tvTime.setText(getFormattedDate(piStatus.getLastOnlinePi()));
                        ivIp.setImageResource(R.drawable.circle_on);
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
                                                    //float tempWater = temperatureInfo.getTemp();
                                                    /*tvWaterTemp
                                                            .setText(TextUtils.concat(
                                                                    String.format(Locale.getDefault(), "%.2f", tempWater), " \u00b0C"));*/
                                                    break;

                                                case "system":
                                                    infoArrayList.get(1).setTemp(temperatureInfo.getTemp());
                                                    infoArrayList.get(1).setTempMax(temperatureInfo.getTempMax());
                                                    infoArrayList.get(1).setTempMin(temperatureInfo.getTempMin());
                                                    infoArrayList.get(1).setMinTimeStamp(temperatureInfo.getMinTimeStamp());
                                                    infoArrayList.get(1).setMaxTimeStamp(temperatureInfo.getMaxTimeStamp());
                                                    //float tempSystem = temperatureInfo.getTemp();
                                                    /*tvSystemTemp
                                                            .setText(TextUtils.concat(
                                                                    String.format(Locale.getDefault(), "%.2f", tempSystem), " \u00b0C"));*/
                                                    break;

                                                case "room":
                                                    infoArrayList.get(2).setTemp(temperatureInfo.getTemp());
                                                    infoArrayList.get(2).setTempMax(temperatureInfo.getTempMax());
                                                    infoArrayList.get(2).setTempMin(temperatureInfo.getTempMin());
                                                    infoArrayList.get(2).setMinTimeStamp(temperatureInfo.getMinTimeStamp());
                                                    infoArrayList.get(2).setMaxTimeStamp(temperatureInfo.getMaxTimeStamp());
                                                    //float tempRoom = temperatureInfo.getTemp();
                                                    /*tvRoomTemp
                                                            .setText(TextUtils.concat(
                                                                    String.format(Locale.getDefault(), "%.2f", tempRoom), " \u00b0C"));*/
                                                    break;
                                            }

                                            tvNowTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(0).getTemp()));
                                            tvMinTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(0).getTempMin()));
                                            tvMaxTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(0).getTempMax()));
                                            tvTimeMin.setText(getFormattedDate(infoArrayList.get(0).getMinTimeStamp()));
                                            tvTimeMax.setText(getFormattedDate(infoArrayList.get(0).getMaxTimeStamp()));
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                progressBar.setVisibility(View.GONE);
                            }
                        });

                        queryControl.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                Control controlData = dataSnapshot.getValue(Control.class);
                                control.setBypassOn(controlData.isBypassOn());
                                //tbBypass.setChecked(controlData.isBypassOn());
                                if(controlData.isBypassOn()) {
                                    tvBypass.setText("Bypass On");
                                    ivBypass.setBackgroundResource(R.drawable.circle_on);
                                }else{
                                    tvBypass.setText("Bypass Off");
                                    ivBypass.setBackgroundResource(R.drawable.circle_off);
                                }
                                control.setLightOn(controlData.isLightOn());
                                //tbLight.setChecked(controlData.isLightOn());
                                if(controlData.isLightOn()) {
                                    tvLight.setText("Light On");
                                    ivLight.setBackgroundResource(R.drawable.circle_on);
                                }else{
                                    tvLight.setText("Light Off");
                                    ivLight.setBackgroundResource(R.drawable.circle_off);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        if (piStatus.isOnlineArduino()) {
                            ivArduinoStatus.setImageResource(R.drawable.circle_on);
                            //tvArduinoStatus.setText("Arduino");
                        } else {
                            ivArduinoStatus.setImageResource(R.drawable.circle_off);
                            //tvArduinoStatus.setText("Arduino");
                        }

                    } else {

                        ivPiStatus.setImageResource(R.drawable.circle_off);
                        tvPiStatus.setText("Offline");
                        tvTime.setText(getFormattedDate(piStatus.getLastOnlinePi()));
                        ivIp.setImageResource(R.drawable.circle_off);
                        //tvIP.setText("Raspberry Pi");
                        ivArduinoStatus.setImageResource(R.drawable.circle_off);
                        //tvArduinoStatus.setText("Arduino");

                        tvNowTemp.setText("--.--");
                        tvMaxTemp.setText("--.--");
                        tvMinTemp.setText("--.--");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        if(compoundButton.getId() == R.id.tbBypass || compoundButton.getId() == R.id.tbLight){
            /*if (!tbBypass.isChecked()) {
                tbLight.setClickable(false);
                tbLight.setEnabled(false);
            } else {
                tbLight.setClickable(true);
                tbLight.setEnabled(true);
            }*/
        }else {

            if (avoidRecursions) return;
            avoidRecursions = true;

            // don't allow the un-checking
            if (!b) {
                compoundButton.setChecked(true);
                avoidRecursions = false;
                return;
            }

            // un-check the previous checked button
            if (compoundButton != tbWater && tbWater.isChecked()) {
                tbWater.setChecked(false);
                tbWater.setTextColor(Color.LTGRAY);
            } else if (compoundButton != tbSystem && tbSystem.isChecked()) {
                tbSystem.setChecked(false);
                tbSystem.setTextColor(Color.LTGRAY);
            } else if (compoundButton != tbRoom && tbRoom.isChecked()) {
                tbRoom.setChecked(false);
                tbRoom.setTextColor(Color.LTGRAY);
            }

            avoidRecursions = false;
        }

        switch (compoundButton.getId()) {

            case R.id.tbWater:
                if (tbWater.isChecked()) {
                    tvSelection.setText("water");
                    tbWater.setTextColor(Color.CYAN);
                    tvNowTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(0).getTemp()));
                    tvMinTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(0).getTempMin()));
                    tvMaxTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(0).getTempMax()));
                    tvTimeMin.setText(getFormattedDate(infoArrayList.get(0).getMinTimeStamp()));
                    tvTimeMax.setText(getFormattedDate(infoArrayList.get(0).getMaxTimeStamp()));
                }
                break;

            case R.id.tbSystem:
                if (tbSystem.isChecked()) {
                    tvSelection.setText("system");
                    tbSystem.setTextColor(Color.CYAN);
                    tvNowTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(1).getTemp()));
                    tvMinTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(1).getTempMin()));
                    tvMaxTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(1).getTempMax()));
                    tvTimeMin.setText(getFormattedDate(infoArrayList.get(1).getMinTimeStamp()));
                    tvTimeMax.setText(getFormattedDate(infoArrayList.get(1).getMaxTimeStamp()));
                }
                break;

            case R.id.tbRoom:
                if (tbRoom.isChecked()) {
                    tvSelection.setText("room");
                    tbRoom.setTextColor(Color.CYAN);
                    tvNowTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(2).getTemp()));
                    tvMinTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(2).getTempMin()));
                    tvMaxTemp.setText(String.format(Locale.UK, "%.2f", infoArrayList.get(2).getTempMax()));
                    tvTimeMin.setText(getFormattedDate(infoArrayList.get(2).getMinTimeStamp()));
                    tvTimeMax.setText(getFormattedDate(infoArrayList.get(2).getMaxTimeStamp()));
                }
                break;

           /* case R.id.tbBypass:
                if (tbBypass.isChecked()) {
                    tbBypass.setTextColor(Color.CYAN);
                }else{
                    tbBypass.setTextColor(Color.LTGRAY);
                }
                control.setBypassToggle(b);
                databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                break;

            case R.id.tbLight:
                if (tbLight.isChecked()) {
                    tbLight.setTextColor(Color.CYAN);
                }else{
                    tbLight.setTextColor(Color.LTGRAY);
                }
                control.setLightOn(b);
                databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                break;*/
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void openRebootDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TestDisplayActivity.this);
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
                        Toast.makeText(TestDisplayActivity.this, "Resetting Raspberry Pi", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(TestDisplayActivity.this, "Resetting Arduino", Toast.LENGTH_LONG).show();
                        control.setResetArduino(Boolean.TRUE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                        break;

                    case MotionEvent.ACTION_UP:
                        //Toast.makeText(MainActivity.this, "up", Toast.LENGTH_LONG).show();
                        control.setResetArduino(Boolean.FALSE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                        break;
                }

                return false;
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
    public void onClick(View view) {
        switch( view.getId()){
            case R.id.bBypass:
                //control.setBypassToggle(Boolean.TRUE);

                if(control.isBypassOn()){
                    control.setBypassOn(Boolean.FALSE);
                }else{
                    control.setBypassOn(Boolean.TRUE);
                }

                databaseReference.child(DB_CHILD_CONTROL).setValue(control);

                /*Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        control.setBypassToggle(Boolean.FALSE);
                        databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                    }
                },1000);*/
                break;

            case R.id.bLight:
                if(control.isLightOn()){
                    control.setLightOn(Boolean.FALSE);
                }else{
                    control.setLightOn(Boolean.TRUE);
                }

                databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                break;
        }
    }
}
