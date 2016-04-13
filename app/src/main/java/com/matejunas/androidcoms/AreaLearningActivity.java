package com.matejunas.androidcoms;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoErrorException;

public class AreaLearningActivity extends AppCompatActivity {

    private static final String uuid = "test uuid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_area_learning);
        loadAdf();
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);

    }

    public void loadAdf() {
        TangoConfig mConfig = MainActivity.mTango.getConfig(TangoConfig.CONFIG_TYPE_AREA_DESCRIPTION);
        try {
            mConfig.putString(TangoConfig.KEY_STRING_AREADESCRIPTION, uuid);
        } catch (TangoErrorException e) {
            e.printStackTrace();
            Toast.makeText(this, "Couldn't put uuid", Toast.LENGTH_LONG).show();
        }
    }
}