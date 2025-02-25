package com.example.sleeptracker;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private int startHr = 23;
    private int startMin;
    private int endHr = 7;
    private int endMin;
    private int totalHrsSlept = 8;
    private int totalMinsSlept = 0;
    public int goalSleepMinutes = 360;
    ArrayList<Integer> allSleepTimes = new ArrayList<>();
    private boolean undoClicked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView warning = findViewById(R.id.warning);
        warning.setVisibility(View.INVISIBLE);

        TextView totalSleepTime = findViewById(R.id.totalSleepTimeCalc);

        TextView sleepStartBtn = findViewById(R.id.startTimeDisplay);
        sleepStartBtn.setOnClickListener(v -> timePickerDialog(sleepStartBtn, true, totalSleepTime));

        TextView sleepEndBtn = findViewById(R.id.endTimeDisplay);
        sleepEndBtn.setOnClickListener(v -> timePickerDialog(sleepEndBtn, false, totalSleepTime));

        Button undoButton = findViewById(R.id.undoBtn);

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null){
                    int sleepHours = result.getData().getIntExtra("sliderHours", 6);
                    int sleepMinutes = result.getData().getIntExtra("sliderMinutes", 0);
                    SharedPreferences prefs = getSharedPreferences("SleepPreferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("savedSleepHours", sleepHours);
                    editor.putInt("savedSleepMinutes", sleepMinutes);
                    editor.apply();
                    goalSleepMinutes = sleepHours*60 + sleepMinutes;
                }
            }
        );

        ImageButton settingsBtn = findViewById(R.id.settingsBtn);
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            launcher.launch(intent);
        });

        TextView errorDisp = findViewById(R.id.undoErrorMsg);
        errorDisp.setVisibility(View.INVISIBLE);
        TextView sleepAvg = findViewById(R.id.avgDisplay);

        undoButton.setOnClickListener(v->undo(errorDisp, sleepAvg));
        Button submitTimeBtn = findViewById(R.id.submitBtn);
        submitTimeBtn.setOnClickListener(v -> updateArrayList(sleepAvg, warning, errorDisp));

    }

    private void timePickerDialog(TextView timeDisplay, boolean startTime, TextView sleepTime) {
        TimePickerDialog timePickWindow = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    int hour = hourOfDay;
                    String timeOfDay;

                    if (hour == 0) {
                        hour+=12;
                        timeOfDay = "AM";
                    } else if (hour == 12) {
                        timeOfDay = "PM";
                    } else if (hour > 12) {
                        hour -= 12;
                        timeOfDay = "PM";
                    } else {
                        timeOfDay = "AM";
                    }

                    String hourDisplay;
                    String minuteDisplay;

                    if (hour < 10) {
                        hourDisplay = "0" + hour;
                    } else {
                        hourDisplay = String.valueOf(hour);
                    }

                    if (minute < 10) {
                        minuteDisplay = "0" + minute;
                    } else {
                        minuteDisplay = String.valueOf(minute);
                    }

                    if (startTime) {
                        startHr = hour;
                        startMin = minute;
                    } else {
                        endHr = hour;
                        endMin = minute;
                    }

                    String msg = hourDisplay + ":" + minuteDisplay + " " + timeOfDay;
                    timeDisplay.setText(msg);

                    if (endHr < startHr) {
                        totalHrsSlept = 12 - startHr + endHr;
                    } else if (endHr > startHr) {
                        totalHrsSlept = endHr - startHr;
                    }

                    if (endMin > startMin) {
                        totalMinsSlept = endMin - startMin;
                    } else if (endMin < startMin) {
                        totalMinsSlept = 60 - (startMin - endMin);
                        totalHrsSlept -= 1;
                    }

                    String timeSleptMsg = totalHrsSlept + " Hours " + totalMinsSlept + " Minutes ";
                    sleepTime.setText(timeSleptMsg);


                },
                12,
                0,
                false
        );
        timePickWindow.show();
    }

    private void updateArrayList(TextView avgDisplay, ImageView warningImage, TextView error) {
        int durationMinutes = (totalHrsSlept * 60) + totalMinsSlept;

        allSleepTimes.add(durationMinutes);
        double avg = 0;
        for (int i : allSleepTimes) {
            avg += i;
        }

        avg = avg / allSleepTimes.size();
        double avgHours = Math.round(avg/60 * 100.0) / 100.0;


        String avgMsg = avgHours + " Hours";
        avgDisplay.setText(avgMsg);

        if (durationMinutes < goalSleepMinutes) {
            warningImage.setVisibility(View.VISIBLE);
        } else {
            warningImage.setVisibility(View.INVISIBLE);
        }

        undoClicked = false;
        error.setVisibility(View.INVISIBLE);
    }

    private void undo(TextView errorDisplay, TextView avgDisplay) {
        if (undoClicked) {
            errorDisplay.setVisibility(View.VISIBLE);
        } else {
            allSleepTimes.remove(allSleepTimes.size()-1);
            double avg = 0;
            for (int i : allSleepTimes) {
                avg += i;
            }
            avg = avg / allSleepTimes.size();
            double avgHours = Math.round(avg/60 * 100.0) / 100.0;

            String avgMsg = avgHours + " Hours";
            avgDisplay.setText(avgMsg);
            undoClicked = true;
        }
    }
}