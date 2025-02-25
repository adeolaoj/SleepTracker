package com.example.sleeptracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.slider.Slider;

public class SettingsActivity extends AppCompatActivity {

    private int selectedHours = 6;
    private int selectedMinutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SharedPreferences prefs = getSharedPreferences("SleepPreferences", MODE_PRIVATE);
        selectedHours = prefs.getInt("savedSleepHours", 6);
        selectedMinutes = prefs.getInt("savedSleepMinutes", 0);

        Slider sleepHoursSlider = findViewById(R.id.hoursSlider);
        Slider sleepMinutesSlider = findViewById(R.id.minutesSlider);

        sleepHoursSlider.addOnChangeListener((slider, value, fromUser) -> {
            selectedHours = (int) value;
        });

        sleepMinutesSlider.addOnChangeListener((slider, value, fromUser) -> {
            selectedMinutes = (int) value;
        });

        Button saved = findViewById(R.id.saveButton);
        saved.setOnClickListener(v-> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("sliderHours", selectedHours);
            resultIntent.putExtra("sliderMinutes", selectedMinutes);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        findViewById(R.id.cancelBtn).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

    }


}