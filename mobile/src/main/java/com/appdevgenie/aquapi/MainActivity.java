package com.appdevgenie.aquapi;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.appdevgenie.aquapi.models.PiStatus;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_AQUA_PI;
import static com.appdevgenie.aquapi.utils.Constants.DB_CHILD_STATUS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private TextView tvIP, tvPiStatus, tvArduinoStatus, tvRoomTemp, tvSystemTemp, tvWaterTemp;
    private ImageView ivIp, ivPiStatus, ivArduinoStatus;
    ProgressBar pbRoom, pbSystem, pbWater;

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
        tvArduinoStatus = findViewById(R.id.tvArduinoStatus);
        ivIp = findViewById(R.id.ivIpAddress);
        ivPiStatus = findViewById(R.id.ivPiStatus);
        ivArduinoStatus = findViewById(R.id.ivArduinoStatus);

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

        Query query = databaseReference
                .child(DB_CHILD_STATUS);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChanged: " + dataSnapshot.getValue());
                PiStatus piStatus = dataSnapshot.getValue(PiStatus.class);
                if (piStatus != null) {
                    if (piStatus.isOnlinePi()) {

                        ivPiStatus.setImageResource(R.drawable.led_green);
                        tvPiStatus.setText("Pi connected");
                        ivIp.setImageResource(R.drawable.led_green);
                        tvIP.setText(piStatus.getIp());

                        float tempRoom = piStatus.getTempRoom();
                        tvRoomTemp.setText(String.format(Locale.getDefault(), "%.2f", tempRoom));
                        pbRoom.setProgress((int) tempRoom + 5);

                        float tempWater = piStatus.getTempWater();
                        tvWaterTemp.setText(String.format(Locale.getDefault(), "%.2f", tempWater));
                        pbWater.setProgress((int) tempWater + 5);

                        float tempSystem = piStatus.getTempSystem();
                        tvSystemTemp.setText(String.format(Locale.getDefault(), "%.2f", tempSystem));
                        pbSystem.setProgress((int) tempSystem + 5);

                        if(piStatus.isOnlineArduino()){
                            ivArduinoStatus.setImageResource(R.drawable.led_green);
                            tvArduinoStatus.setText("Arduino connected");
                        }else{
                            ivArduinoStatus.setImageResource(R.drawable.led_red);
                            tvArduinoStatus.setText("Arduino not connected");
                        }

                    } else {

                        ivPiStatus.setImageResource(R.drawable.led_red);
                        tvPiStatus.setText("Offline");
                        ivIp.setImageResource(R.drawable.led_red);
                        tvIP.setText("Pi not connected");
                        ivArduinoStatus.setImageResource(R.drawable.led_red);
                        tvArduinoStatus.setText("Arduino not connected");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
