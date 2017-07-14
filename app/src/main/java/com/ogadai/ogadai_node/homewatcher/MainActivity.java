package com.ogadai.ogadai_node.homewatcher;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private HomeSecureClient mClient;
    private boolean mConnected;

    private TextView mConnectionState;
    private Camera2 mCamera2;

    private static final String TAG = "MainActivity";

    static final int REQUEST_CAMERA_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnectionState = (TextView)findViewById(R.id.connectionState);

        mClient = new HomeSecureClient();
        mConnected = false;

        mClient.setCallback(new HomeSecureClient.ClientCallback() {
            @Override
            public void updateState(final boolean connectionOpen) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnectionState.setText(connectionOpen ? "connected" : "not connected");
                    }
                });
            }
        });

        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    private void initialiseCamera() {
        mCamera2 = new Camera2(this);
        mClient.setCameraControls(mCamera2);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Configuration config = Configuration.readSettings(this);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(config.getName());
        }

        if (!mConnected && (mCamera2 != null)) {
            mConnected = true;
            mClient.connect(config);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mConnected) {
            mConnected = false;
            mClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Wasn't granted camera permissions");
            } else {
                initialiseCamera();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                showSettings();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivityForResult(settingsIntent, 0);
    }
}
