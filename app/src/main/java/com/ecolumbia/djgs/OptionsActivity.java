package com.ecolumbia.djgs;

import android.graphics.Camera;
import android.graphics.Point;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dji.common.camera.DJICameraSettingsDef;

public class OptionsActivity extends AppCompatActivity {

    private TextView tvCurrentCameraMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);
        setFinishOnTouchOutside (false);

        // Size the new window to replicate a dialog.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = (int) (height * 0.75);
        params.width = (int) (width * 0.75);
        this.getWindow().setAttributes(params);

        // Add in a toolbar
        Toolbar toolbar_Options = (Toolbar) findViewById(R.id.toolbarOptions);
        setSupportActionBar(toolbar_Options);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(false);

        tvCurrentCameraMode = (TextView) findViewById(R.id.tvCurrentCameraMode);
        CameraPhoto.GetCameraMode(); // Obtain the camera mode.

    }

    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    public void btnClose_onClick(View v) {
        // Close out the activity by simulating the back button key press.
        super.onBackPressed();
    }

    // prevent "back" from leaving this activity
    @Override public void onBackPressed() {
        super.onBackPressed();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.CameraModeCurrentState event) {
        DJICameraSettingsDef.CameraMode cameraModeCurrentState = event.cameraModeCurrentState;
         tvCurrentCameraMode.setText("Current Camera Mode: " + cameraModeCurrentState.toString());
    }

    public void setModeToCamera(View v) {
        CameraPhoto.SetCameraToNewMode(DJICameraSettingsDef.CameraMode.ShootPhoto);
    }
    public void setModeToVideo(View v) {
        CameraPhoto.SetCameraToNewMode(DJICameraSettingsDef.CameraMode.RecordVideo);
    }
}
