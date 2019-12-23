package com.appdevgenie.aquapi.activities;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.appdevgenie.aquapi.R;
import com.appdevgenie.aquapi.models.PiStatus;
import com.appdevgenie.aquapi.models.Temperatures;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_AQUA_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_STATUS;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_TEMPERATURES;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private TextView tvIP, tvPiStatus, tvTime, tvArduinoStatus, tvRoomTemp, tvSystemTemp, tvWaterTemp;
    private ImageView ivIp, ivPiStatus, ivArduinoStatus, ivTempLed;
    private ProgressBar pbStatus, pbTemps, pbRoom, pbSystem, pbWater;

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
        ivTempLed = findViewById(R.id.ivTempLed);

        ConstraintLayout clRoomTemp = findViewById(R.id.includeRoomTemp);
        TextView tvRoomTitle = clRoomTemp.findViewById(R.id.tvTempTitle);
        tvRoomTitle.setText("Room");
        tvRoomTemp = clRoomTemp.findViewById(R.id.tvTemperature);
        pbRoom = clRoomTemp.findViewById(R.id.pbTemperature);

        ConstraintLayout clSystemTemp = findViewById(R.id.includeSystemTemp);
        TextView tvSystemTitle = clSystemTemp.findViewById(R.id.tvTempTitle);
        tvSystemTitle.setText("System");
        tvSystemTemp = clSystemTemp.findViewById(R.id.tvTemperature);
        pbSystem = clSystemTemp.findViewById(R.id.pbTemperature);

        ConstraintLayout clWaterTemp = findViewById(R.id.includeWaterTemp);
        TextView tvWaterTitle = clWaterTemp.findViewById(R.id.tvTempTitle);
        tvWaterTitle.setText("Water");
        tvWaterTemp = clWaterTemp.findViewById(R.id.tvTemperature);
        pbWater = clWaterTemp.findViewById(R.id.pbTemperature);

        setupFirebaseListener();
    }

    private void setupFirebaseListener() {

        pbStatus.setVisibility(View.VISIBLE);
        pbTemps.setVisibility(View.VISIBLE);

        Query queryStatus = databaseReference
                .child(DB_CHILD_STATUS);

        Query queryTemps = databaseReference
                .child(DB_CHILD_TEMPERATURES);

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
                                //pbTemps.setVisibility(View.GONE);

                                Temperatures temperatures = dataSnapshot.getValue(Temperatures.class);
                                if (temperatures != null) {
                                    ivTempLed.setImageResource(R.drawable.led_green);

                                    float tempRoom = temperatures.getTempRoom();
                                    tvRoomTemp.setText(String.format(Locale.getDefault(), "%.2f", tempRoom));
                                    pbRoom.setProgress((int) tempRoom + 5);

                                    float tempWater = temperatures.getTempWater();
                                    tvWaterTemp.setText(String.format(Locale.getDefault(), "%.2f", tempWater));
                                    pbWater.setProgress((int) tempWater + 5);

                                    float tempSystem = temperatures.getTempSystem();
                                    tvSystemTemp.setText(String.format(Locale.getDefault(), "%.2f", tempSystem));
                                    pbSystem.setProgress((int) tempSystem + 5);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                pbTemps.setVisibility(View.GONE);
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
                        ivTempLed.setImageResource(R.drawable.led_red);

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
}
