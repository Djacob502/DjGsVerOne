package com.ecolumbia.djgs;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.w3c.dom.Text;

import dji.sdk.products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;

public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = ConnectActivity.class.getName();
    private Button btnConnect;
    private TextView tvConnectStatus;
    private TextView tvAppRegistration;
    private TextView tvSDK;
    private TextView tvOpenCv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        // When the compile and target version is higher than 22, please request the following permissions at runtime to ensure the SDK work well.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.VIBRATE,
                            Manifest.permission.INTERNET, Manifest.permission.ACCESS_WIFI_STATE,
                            Manifest.permission.WAKE_LOCK, Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.SYSTEM_ALERT_WINDOW,
                            Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH
                    }
                    , 1);
        }

        btnConnect = (Button) this.findViewById(R.id.btnConnect);
        tvConnectStatus = (TextView) this.findViewById(R.id.tvConnectStatus);
        tvAppRegistration = (TextView) this.findViewById(R.id.tvAppRegistration);
        tvSDK = (TextView) this.findViewById(R.id.tvSDK);
        tvOpenCv = (TextView) this.findViewById(R.id.tvOpenCv);

        Toolbar toolbarConnect = (Toolbar) findViewById(R.id.toolbarConnect);
        setSupportActionBar(toolbarConnect);
        toolbarConnect.setLogo(R.drawable.ic_logo_toolbar_black_48dp);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            DjiApplication.disconnectFromDrone();
        } catch (Exception e) {
            Log.e(TAG, "Error on disconnect from drone" + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isConnected = false;
        DJIBaseProduct aircraft = DjiApplication.getProductInstance();
        if (aircraft != null) {
            if (aircraft instanceof DJIAircraft) {
                try {
                    isConnected = true;

                } catch (Exception e) {
                    isConnected = false;
                }
            } else {
                isConnected = false;
            }
        } else {
            isConnected = false;
        }
        refreshTitle(isConnected);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }



    private void refreshTitle(boolean isConnected) {
        Log.e("refreshTitle","RefreshTitle");
        if (! isConnected) {
            tvConnectStatus.setText("Connect an aircraft to begin");
            btnConnect.setEnabled(false);
        } else {
            DJIBaseProduct aircraft = DjiApplication.getProductInstance();
            if (aircraft != null) {
                if (aircraft instanceof DJIAircraft) {
                    try {
                        tvConnectStatus.setText("Connected to " + aircraft.getModel().getDisplayName());
                        btnConnect.setEnabled(true);

                    } catch (Exception e) {
                        // If the above statement: aircraft.getModel().getDisplayName() returns and error, then this implies the connection is dropped.
                        tvConnectStatus.setText("Connect to an aircraft to begin ");
                        btnConnect.setEnabled(false);
                    }
                } else {
                    tvConnectStatus.setText("Product is not an aircraft");
                    btnConnect.setEnabled(false);
                }
            } else {
                tvConnectStatus.setText("Connect to an aircraft to begin");
                btnConnect.setEnabled(false);
            }
        }
        if (OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mOpenCVCallBack)) {
            Log.e(TAG, "Connected to OpenCv Manager");
            tvOpenCv.setText("Successfully Connected to OpenCV Manager");
        }
    }

    // Menu items are shown and acted upon below.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public void gotoControl(View view) {
        // Start the control activity
        Intent intent = new Intent(this, ControlActivity.class);
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.ProductConnected event) {
        refreshTitle(event.isConnected);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.RegisterApp event) {
        boolean isAppRegistered = event.isAppRegistered;
        if (isAppRegistered) {
            tvAppRegistration.setText("Application registration with DJI is successful");
        } else {
            tvAppRegistration.setText("PROBLEM: Application registration with DJI is not successful");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.SDKVersion event) {
        String stSDKVersion = event.temporaryMessage;
        tvSDK.setText("SDK Version: " + stSDKVersion);
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.e(TAG, "OpenCv load success");
                }
                break;
                case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION: {
                    Log.e(TAG, "OpenCv incompatible version");
                    tvOpenCv.setText("Error: OpenCv is an incompatible version");
                }
                break;
                case LoaderCallbackInterface.INIT_FAILED: {
                    Log.e(TAG, "OpenCv Init Failed");
                    tvOpenCv.setText("Error: OpenCv initialization failed");
                }
                break;
                case LoaderCallbackInterface.INSTALL_CANCELED: {
                    Log.e(TAG, "OpenCV intall canceled");
                    tvOpenCv.setText("Error: OpenCv intall canceled");
                }
                break;
                case LoaderCallbackInterface.MARKET_ERROR: {
                    Log.e(TAG, "OpenCV Market Error");
                    tvOpenCv.setText("Error: OpenCv market error");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };



}
