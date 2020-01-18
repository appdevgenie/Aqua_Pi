package com.appdevgenie.aquapi.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_AQUA_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_CONTROL;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_STATUS;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_TEMPERATURES;

public class TestDisplayActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private ToggleButton tbWater, tbSystem, tbRoom;
    private boolean avoidRecursions = false;
    private ToggleButton tbBypass, tbLight;
    private ArrayList<TemperatureInfo> infoArrayList;
    private Control control = new Control();
    private ProgressBar progressBar;
    private TextView tvIP, tvPiStatus, tvTime, tvArduinoStatus, tvNowTemp, tvMinTemp, tvMaxTemp, tvSelection, tvTimeMin, tvTimeMax;
    private ImageView ivIp, ivPiStatus, ivArduinoStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_digital_display);

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
        tvArduinoStatus = findViewById(R.id.tvArduinoStatus);
        tvNowTemp = findViewById(R.id.tvTempNow);
        tvMinTemp = findViewById(R.id.tvTempMin);
        tvMaxTemp = findViewById(R.id.tvTempMax);
        tvSelection = findViewById(R.id.tvSelection);
        tvTimeMin = findViewById(R.id.tvTimeMin);
        tvTimeMax = findViewById(R.id.tvTimeMax);

        ivIp = findViewById(R.id.ivIpAddress);
        ivPiStatus = findViewById(R.id.ivPiStatus);
        ivArduinoStatus = findViewById(R.id.ivArduinoStatus);

        ConstraintLayout c2 = findViewById(R.id.include_digital_control);
        tbWater = c2.findViewById(R.id.tbWater);
        tbSystem = c2.findViewById(R.id.tbSystem);
        tbRoom = c2.findViewById(R.id.tbRoom);
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
        //r0.setTypeface(custom_font);
        //r1.setTypeface(custom_font);
        //r2.setTypeface(custom_font);
        tbWater.setOnCheckedChangeListener(this);
        tbSystem.setOnCheckedChangeListener(this);
        tbRoom.setOnCheckedChangeListener(this);

        infoArrayList = TemperatureInfo.createTempList();

        setupFirebaseListener();
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

                                            tvNowTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(0).getTemp()));
                                            tvMinTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(0).getTempMin()));
                                            tvMaxTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(0).getTempMax()));
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
                                control.setLightOn(controlData.isLightOn());
                                tbLight.setChecked(controlData.isLightOn());

                                control.setBypassOn(controlData.isBypassOn());
                                tbBypass.setChecked(controlData.isBypassOn());
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
            if (!tbBypass.isChecked()) {
                tbLight.setClickable(false);
                tbLight.setEnabled(false);
            } else {
                tbLight.setClickable(true);
                tbLight.setEnabled(true);
            }
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
                    tvNowTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(0).getTemp()));
                    tvMinTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(0).getTempMin()));
                    tvMaxTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(0).getTempMax()));
                    tvTimeMin.setText(getFormattedDate(infoArrayList.get(0).getMinTimeStamp()));
                    tvTimeMax.setText(getFormattedDate(infoArrayList.get(0).getMaxTimeStamp()));
                }
                break;

            case R.id.tbSystem:
                if (tbSystem.isChecked()) {
                    tvSelection.setText("system");
                    tbSystem.setTextColor(Color.CYAN);
                    tvNowTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(1).getTemp()));
                    tvMinTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(1).getTempMin()));
                    tvMaxTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(1).getTempMax()));
                    tvTimeMin.setText(getFormattedDate(infoArrayList.get(1).getMinTimeStamp()));
                    tvTimeMax.setText(getFormattedDate(infoArrayList.get(1).getMaxTimeStamp()));
                }
                break;

            case R.id.tbRoom:
                if (tbRoom.isChecked()) {
                    tvSelection.setText("room");
                    tbRoom.setTextColor(Color.CYAN);
                    tvNowTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(2).getTemp()));
                    tvMinTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(2).getTempMin()));
                    tvMaxTemp.setText(String.format(Locale.getDefault(), "%.2f", infoArrayList.get(2).getTempMax()));
                    tvTimeMin.setText(getFormattedDate(infoArrayList.get(2).getMinTimeStamp()));
                    tvTimeMax.setText(getFormattedDate(infoArrayList.get(2).getMaxTimeStamp()));
                }
                break;

            case R.id.tbBypass:
                if (tbBypass.isChecked()) {
                    tbBypass.setTextColor(Color.CYAN);
                }else{
                    tbBypass.setTextColor(Color.LTGRAY);
                }
                //control.setBypassOn(b);
                //databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                break;

            case R.id.tbLight:
                if (tbLight.isChecked()) {
                    tbLight.setTextColor(Color.CYAN);
                }else{
                    tbLight.setTextColor(Color.LTGRAY);
                }
                //control.setLightOn(b);
                //databaseReference.child(DB_CHILD_CONTROL).setValue(control);
                break;
        }
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
}
