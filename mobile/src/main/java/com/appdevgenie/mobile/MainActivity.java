package com.appdevgenie.mobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {

        ConstraintLayout clRoomTemp = findViewById(R.id.includeRoomTemp);
        TextView tvRoomTitle = clRoomTemp.findViewById(R.id.tvTempTitle);
        tvRoomTitle.setText("Room");
        ProgressBar pbRoom = clRoomTemp.findViewById(R.id.pbTemperature);
        pbRoom.setProgress(30);


        ConstraintLayout clSystemTemp = findViewById(R.id.includeSystemTemp);
        TextView tvSystemTitle = clSystemTemp.findViewById(R.id.tvTempTitle);
        tvSystemTitle.setText("System");
        ProgressBar pbSystem = clSystemTemp.findViewById(R.id.pbTemperature);
        pbSystem.setProgress(45);


        ConstraintLayout clWaterTemp = findViewById(R.id.includeWaterTemp);
        TextView tvWaterTemp = clWaterTemp.findViewById(R.id.tvTempTitle);
        tvWaterTemp.setText("Water");
        ProgressBar pbWater = clWaterTemp.findViewById(R.id.pbTemperature);
        pbWater.setProgress(20);
    }
}
