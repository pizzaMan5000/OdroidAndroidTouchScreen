package org.swampsoft.odroidTouchscreen;

/*
The most important code in the project is XPT2046.java borrowed from https://github.com/MatthewLowden/RPi-XPT2046-Touchscreen-Java
Its the actual driver, the rest is my code and HardKernel's modified wiringPi for odroid
 */

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button buttonSave;
    // left side
    private Spinner screenSizeList;
    private EditText editTextScreenResX;
    private EditText editTextScreenResY;
    private TextView textMultiplier;
    // right side
    private EditText editTextShiftX;
    private EditText editTextShiftY;
    private EditText editTextTouchResX;
    private EditText editTextTouchResY;
    private ImageButton buttonTouchResHelp;

    private ArrayList<String> screenSizeArray;
    private ArrayAdapter<String> screenSizeArrayAdapter;

    private int screenResX = 800;
    private int screenResY = 480;
    private int shiftX = 0;
    private int shiftY = 0;
    private int touchResolutionX = 4096; // 3840
    private int touchResolutionY = 4096;

    AlertDialog.Builder dialogBuilder;

    private SharedPreferences sharedpreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // make list of resolutions
        screenSizeArray = new ArrayList<>();
        screenSizeArray.add("320x240");
        screenSizeArray.add("480x320");
        screenSizeArray.add("800x480");
        screenSizeArray.add("Custom");

        // match objects to activity layout objects
        buttonSave = findViewById(R.id.buttonSave);
        // left side
        screenSizeList = findViewById(R.id.screenSizeSpinner);
        editTextScreenResX = findViewById(R.id.editTextScreenResX);
        editTextScreenResY = findViewById(R.id.editTextScreenResY);
        textMultiplier = findViewById(R.id.textMultiplier);
        // right side
        editTextShiftX = findViewById(R.id.editTextShiftX);
        editTextShiftY = findViewById(R.id.editTextShiftY);
        editTextTouchResX = findViewById(R.id.editTextTouchResX);
        editTextTouchResY = findViewById(R.id.editTextTouchResY);
        buttonTouchResHelp = findViewById(R.id.buttonHelp);

        // get saved settings
        sharedpreferences = getSharedPreferences("org.swampsoft.odroidTouchscreen", Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        screenResX = sharedpreferences.getInt("screenResX", 800);
        screenResY = sharedpreferences.getInt("screenResY", 480);
        shiftX = sharedpreferences.getInt("shiftX", 0);
        shiftY = sharedpreferences.getInt("shiftY", 0);
        touchResolutionX = sharedpreferences.getInt("touchResolutionX", 4096);
        touchResolutionY = sharedpreferences.getInt("touchResolutionY", 4096);

        // set texts to saved settings
        editTextScreenResX.setText(""+screenResX);
        editTextScreenResY.setText(""+screenResY);
        editTextShiftX.setText(""+shiftX);
        editTextShiftY.setText(""+shiftY);
        editTextTouchResX.setText(""+touchResolutionX);
        editTextTouchResY.setText(""+touchResolutionY);

        // figure out which screen resolution to have pre-selected on the drop down list. If screenResX = 800 and screenResY = 480, then set it to 3rd option, etc, etc.
        int dropDownSelection = 3; // set it to custom (4th option)
        if (screenResX == 320 && screenResY == 240) {
            dropDownSelection = 0;
        } else if (screenResX == 480 && screenResY == 320) {
            dropDownSelection = 1;
        } else if (screenResX == 800 && screenResY == 480) {
            dropDownSelection = 2;
        }

        // setup dropdown list with resolutions and make a click listener
        screenSizeArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, screenSizeArray);
        screenSizeList.setAdapter(screenSizeArrayAdapter);
        screenSizeList.setSelection(dropDownSelection);
        screenSizeList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                //Toast.makeText(adapterView.getContext(), "Selected: " + position, Toast.LENGTH_LONG).show();
                if (position == 3){
                    // custom screen size selected, show x/y entry boxes
                    editTextScreenResX.setVisibility(View.VISIBLE);
                    editTextScreenResY.setVisibility(View.VISIBLE);
                    textMultiplier.setVisibility(View.VISIBLE);

                } else {
                    editTextScreenResX.setVisibility(View.INVISIBLE);
                    editTextScreenResY.setVisibility(View.INVISIBLE);
                    textMultiplier.setVisibility(View.INVISIBLE);
                    if (position == 0){
                        screenResX = 320;
                        screenResY = 240;
                    } else if (position == 1){
                        screenResX = 480;
                        screenResY = 320;
                    } else if (position == 2){
                        screenResX = 800;
                        screenResY = 480;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // yup
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });

        buttonTouchResHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show help dialog box
                dialogBuilder.show();
            }
        });

        // make help dialog box
        dialogBuilder = new AlertDialog.Builder(this);
        String dialogMessage = "How to adjust tap location:\n\n" +
                "SHIFT:\n\nIf your taps are always off target, then shift the X and/or Y coordinate. Add or subtract to X or Y to move taps left, right, up, down. Then press save and restart the service to see changes. " +
                "These adjustments are done to the touch resolution, not the screen resolution, so you will need to use more than expected.\n\n" +
                "TOUCH RESOLUTION:\n\nIf your taps are not on target and inconsistent, change the resolutions. Example: Taps are sometimes too far left on the left side of the screen but too far right on the right side, then increase " +
                "the X dimension. If taps are too far left on the right side and too far right on the left side, decrease the X dimension. You will need to shift the X and Y around as you change these and you will have to save and " +
                "restart the service to see changes to any settings.\n\nDefault Touch Resolution for XPT2046 is 4096 for X and Y.";
        dialogBuilder.setMessage(dialogMessage);
    }

    private void saveSettings(){
        // get custom screen resolution if "custom" is selected from screen resolution list
        int selectedRes = screenSizeList.getSelectedItemPosition();
        if (selectedRes == 3){
            screenResX = Integer.parseInt(editTextScreenResX.getText().toString());
            screenResY = Integer.parseInt(editTextScreenResY.getText().toString());
        }

        // get numbers from editTexts
        shiftX = Integer.parseInt(editTextShiftX.getText().toString());
        shiftY = Integer.parseInt(editTextShiftY.getText().toString());
        touchResolutionX = Integer.parseInt(editTextTouchResX.getText().toString());
        touchResolutionY = Integer.parseInt(editTextTouchResY.getText().toString());

        // save them to file
        editor.putInt("screenResX", screenResX);
        editor.putInt("screenResY", screenResY);
        editor.putInt("shiftX", shiftX);
        editor.putInt("shiftY", shiftY);
        editor.putInt("touchResolutionX", touchResolutionX);
        editor.putInt("touchResolutionY", touchResolutionY);
        editor.commit();
        Toast.makeText(getApplicationContext(), "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        saveSettings();
        super.onBackPressed();
    }

    // this loads the wpi_android c++ library from the odroid people
    static {
        System.loadLibrary("wpi_android");
    }
}
