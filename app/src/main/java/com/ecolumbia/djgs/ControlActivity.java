package com.ecolumbia.djgs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DecimalFormat;

import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJIFlightControllerDataType;
import dji.common.flightcontroller.DJILocationCoordinate3D;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;

public class ControlActivity extends AppCompatActivity implements MainMapFragment.OnFragmentInteractionListenerMap, VideoFragment.OnFragmentInteractionListenerVideo   {

    private boolean blVideoInLarge = false;
    private MainMapFragment mainMapFragment;
    private VideoFragment videoFragment;
    private TextView tvToolbarMessage;
    private TextView tvBatteryStatus;
    private TextView tvFlightMode;
    private TextView tvSatellites;
    private TextView tvRemoteControllerBatteryPercent;
    private TextView tvAirlinkUplinkLevel;
    private TextView tvAirlinkDownlinkLevel;
    private TextView tvTemporaryMessage;
    private TextView tvTimeRemaining;
    private ProgressBar pbar_TimeRemaining;
    private TextView tvFlightStatus;
    private TextView tvAltitude;
    private TextView tvVerticalSpeed;
    private TextView tvHorizontalSpeed;
    private TextView tv_Altitude_Ultrasonic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        Toolbar toolbar_Control = (Toolbar) findViewById(R.id.toolbar_control);
        setSupportActionBar(toolbar_Control);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        // Initialize the fragments.
        mainMapFragment = new MainMapFragment();
        videoFragment = new VideoFragment();


        // Add a remove the video fragment to correct an error with the video not appearing when adding at first.
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction secondVideoTransaction = getSupportFragmentManager().beginTransaction();
        secondVideoTransaction.replace(R.id.ll_FragmentContainer_Small, videoFragment ); // Used to correct an error with video not appearing at first.
        secondVideoTransaction.commit();
        fm.executePendingTransactions();

        FragmentTransaction removeVideoTransaction = getSupportFragmentManager().beginTransaction();
        removeVideoTransaction.remove(videoFragment ); // Used to correct an error with video not appearing at first.
        removeVideoTransaction.commit();
        fm.executePendingTransactions();


        // Add the fragments.
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back

        transaction.replace(R.id.ll_FragmentContainer_Large, videoFragment );
        transaction.replace(R.id.ll_FragmentContainer_Small, mainMapFragment );
        blVideoInLarge = true;
        // Commit the transaction
        transaction.commit();


        // Hide the status bar.
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        // Set the custom control toolbar
        Toolbar toolbarControl = (Toolbar) findViewById(R.id.toolbar_control);
        setSupportActionBar(toolbarControl);
        tvToolbarMessage = (TextView) findViewById(R.id.tvToolbarMessage);
        tvBatteryStatus = (TextView) findViewById(R.id.tvBatteryStatus);
        tvFlightMode = (TextView) findViewById(R.id.tvFlightMode);
        tvSatellites = (TextView) findViewById(R.id.tvSatellite);
        tvRemoteControllerBatteryPercent = (TextView) findViewById(R.id.tvRemoteControllerBatteryPercent);
        tvAirlinkUplinkLevel = (TextView) findViewById(R.id.tvAirlinkUplinkLevel);
        tvAirlinkDownlinkLevel = (TextView) findViewById(R.id.tvAirlinkDownlinkLevel);
        tvTemporaryMessage = (TextView) findViewById(R.id.tvTemporaryMessage);
        tvTimeRemaining = (TextView) findViewById(R.id.tvTimeRemaining);
        pbar_TimeRemaining = (ProgressBar) findViewById(R.id.pbar_RemainingTime);
        tvFlightStatus = (TextView) findViewById(R.id.tvFlightStatus);
        tvAltitude = (TextView) findViewById(R.id.tvAltitude);
        tvVerticalSpeed = (TextView) findViewById(R.id.tvVerticalSpeed);
        tvHorizontalSpeed = (TextView) findViewById(R.id.tvHorizontalSpeed);
        tv_Altitude_Ultrasonic = (TextView) findViewById(R.id.tv_Altitude_UltraSonic);

        FragmentTransaction flightTransactions = getSupportFragmentManager().beginTransaction();
        FlightControllerUpdateFragment flightControllerUpdateFragment = new FlightControllerUpdateFragment();
        BatteryUpdateFragment batteryUpdateFragment = new BatteryUpdateFragment();
        RemoteControlUpdateFragment remoteControlUpdateFragment = new RemoteControlUpdateFragment();
        AirLinkUpdateFragment airLinkUpdateFragment = new AirLinkUpdateFragment();
        flightTransactions.add(R.id.ll_FragmentContainer, flightControllerUpdateFragment, "flightControllerUpdateFragment");
        flightTransactions.add(R.id.ll_FragmentContainer, batteryUpdateFragment, "batteryUpdateFragment");
        flightTransactions.add(R.id.ll_FragmentContainer, remoteControlUpdateFragment, "remoteControlUpdateFragment");
        flightTransactions.add(R.id.ll_FragmentContainer, airLinkUpdateFragment, "airLinkUpdateFragment");
        flightTransactions.commit();
        showConnection(true); // Send  true to the showConnection. Assume the connection is running the first time it is loaded.
    }

    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    // Menu items are shown and acted upon below.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           case R.id.action_settings:
               // Start the control activity
               Intent intent = new Intent(this, OptionsActivity.class);
               startActivity(intent);
               return true;

            case R.id.action_facerecognition:
                // Start the face recognition activity
                Intent intentFaceRecognition = new Intent(this, FaceRecognitionActivity.class);
                intentFaceRecognition.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentFaceRecognition);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onFragmentInteractionMap(boolean bl_OnTopJustClicked) {
        if (bl_OnTopJustClicked) {
            exchangeSmallAndLargeContainers();
        }
    }

    @Override
    public void onFragmentInteractionVideo(boolean bl_OnTopJustClicked) {
        if (bl_OnTopJustClicked) {
            exchangeSmallAndLargeContainers();
        }
    }

    private void exchangeSmallAndLargeContainers() {
        //Exhange the map and video in the appropriate fragments.
        // Add the fragments.
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().remove(mainMapFragment).commit();
        fm.executePendingTransactions();
        fm.beginTransaction().remove(videoFragment).commit();
        fm.executePendingTransactions();

        // Add a remove the video fragment to correct an error with the video not appearing when adding at first.
        FragmentManager fm2 = getSupportFragmentManager();
        FragmentTransaction secondVideoTransaction = getSupportFragmentManager().beginTransaction();
        secondVideoTransaction.replace(R.id.ll_FragmentContainer_Small, videoFragment ); // Used to correct an error with video not appearing at first.
        secondVideoTransaction.commit();
        fm2.executePendingTransactions();

        FragmentTransaction removeVideoTransaction2 = getSupportFragmentManager().beginTransaction();
        removeVideoTransaction2.remove(videoFragment ); // Used to correct an error with video not appearing at first.
        removeVideoTransaction2.commit();
        fm.executePendingTransactions();



        FragmentTransaction transaction = fm.beginTransaction();

        blVideoInLarge = !blVideoInLarge; // Ensure the large and small fragments are switched each time.

        if (blVideoInLarge) {
            transaction.replace(R.id.ll_FragmentContainer_Small, mainMapFragment);
            transaction.replace(R.id.ll_FragmentContainer_Large, videoFragment);
        } else {
            transaction.replace(R.id.ll_FragmentContainer_Small, videoFragment);
            transaction.replace(R.id.ll_FragmentContainer_Large, mainMapFragment);
        }
        // Commit the transaction
        transaction.commit();
    }







    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.DroneStatus event) {
        tvToolbarMessage.setText(event.droneStatus);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.BatteryLevel event) {
        String batteryLevel = event.batteryLevel + "%";
        int intBatteryLevel = Integer.parseInt(event.batteryLevel);
        tvBatteryStatus.setText("Bat: " + batteryLevel);
        float batteryLevelInSeconds = ( (intBatteryLevel/100f) * 30 * 60);
        int minutes = (int) (batteryLevelInSeconds / 60) ;
        int seconds =  (int) batteryLevelInSeconds - (60 * minutes);
        String secondsFormatted;
        if (seconds < 10) {
            secondsFormatted = "0" +  Integer.toString(seconds);
        } else {
            secondsFormatted = Integer.toString(seconds);
        }
        tvTimeRemaining.setText("Remaining Time " + minutes  + ":" + secondsFormatted);
        pbar_TimeRemaining.setProgress(intBatteryLevel);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.FlightControllerCurrentState event) {
        DJIFlightControllerCurrentState fltCtl = event.flightControllerCurrentState;
        // Obtain flight controller mode
        tvFlightMode.setText(fltCtl.getFlightModeString());
        // Obtain the satellite number
        tvSatellites.setText("Sat: " + fltCtl.getSatelliteCount());

        if (fltCtl.isFlying()) {
            tvFlightStatus.setText("Flying");
        } else if (fltCtl.areMotorsOn()) {
            tvFlightStatus.setText("Motors On");
        }else if (fltCtl.isIMUPreheating()) {
            tvFlightStatus.setText("IMU Preheating");
        } else {
            tvFlightStatus.setText("");
        }

        DJILocationCoordinate3D coor3d = fltCtl.getAircraftLocation();
        float altitude = coor3d.getAltitude();
        float ultrasonicHeight = fltCtl.getUltrasonicHeight();
        float velocityX = fltCtl.getVelocityX();
        float velocityY = fltCtl.getVelocityY();
        float velocityHorizontal = (float) Math.sqrt( Math.pow( (double) velocityX, 2.0) + (float) Math.pow( (double) velocityY, 2.0));
        float velocityZ = fltCtl.getVelocityZ();
        tvAltitude.setText("Alt: " + new DecimalFormat("#.#").format(altitude));
        tvHorizontalSpeed.setText("Vel H: " + new DecimalFormat("#.#").format(velocityHorizontal));
        tvVerticalSpeed.setText("Vel V: " + new DecimalFormat("#.#").format(velocityZ));
        if (fltCtl.isUltrasonicBeingUsed()) {
            tv_Altitude_Ultrasonic.setText("Alt Ultra: " + new DecimalFormat("#.#").format(ultrasonicHeight));
        } else {
            tv_Altitude_Ultrasonic.setText("Alt Ultra: "  + "n/a");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.RemoteControllerBatteryState event) {
        int remoteControllerBatteryRemainingPercent = event.remoteControllerBatteryRemainingPercent;
        tvRemoteControllerBatteryPercent.setText("Bat: " + remoteControllerBatteryRemainingPercent + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.AirlinkUplinkLevel event) {
        int airLinkUplinkLevel = event.airLinkUplinkLevel;
        tvAirlinkUplinkLevel.setText("UL: " + airLinkUplinkLevel + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.AirLinkDownlinkLevel event) {
        int airLinkDownlinkLevel = event.airLinkDownlinkLevel;
        tvAirlinkDownlinkLevel.setText("DL: " + airLinkDownlinkLevel + "%");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.ProductConnected event) {
        showConnection(event.isConnected);
    }

    private void showConnection(boolean isConnected) {

        if (! isConnected) {
            tvToolbarMessage.setText("Not Connected");
        } else {
            DJIBaseProduct aircraft = DjiApplication.getProductInstance();
            if (aircraft != null) {
                if (aircraft instanceof DJIAircraft) {
                    try {
                        if (aircraft.getModel() != null) {
                            tvToolbarMessage.setText("Connected");
                        } else {
                            tvToolbarMessage.setText("Not Connected");
                        }

                    } catch (Exception e) {
                        // If the above statement: aircraft.getModel().getDisplayName() returns and error, then this implies the connection is dropped.
                        tvToolbarMessage.setText("Not Connected");
                    }
                } else {
                    tvToolbarMessage.setText("Not Connected");
                }
            } else {
                tvToolbarMessage.setText("Not Connected");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.ShowTemporaryMessage event) {
        tvTemporaryMessage.setVisibility(View.VISIBLE);
        tvTemporaryMessage.setText(event.temporaryMessage);

        // fade out view nicely after 5 seconds
        AlphaAnimation alphaAnim = new AlphaAnimation(1.0f,0.0f);
        alphaAnim.setStartOffset(5000);                        // start in 5 seconds
        alphaAnim.setDuration(400);
        alphaAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            public void onAnimationEnd(Animation animation)
            {
                // make invisible when animation completes, you could also remove the view from the layout
                tvTemporaryMessage.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        tvTemporaryMessage.setAnimation(alphaAnim);

    }


    public void shootPhoto(View v) {
        CameraPhoto.shootPicture_SinglePhoto();
    }
    public void startVideo(View v) {
        CameraPhoto.startVideoRecording();
    }

    public void stopVideo(View v) {
        CameraPhoto.stopVideoRecording();
    }

    public void takeoff(View v) {
        AircraftControls.Takeoff();
    }

    public void land(View v) {
        AircraftControls.Land();
    }

}
