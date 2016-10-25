package com.ecolumbia.djgs;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import dji.common.battery.DJIBatteryState;
import dji.sdk.battery.DJIBattery.DJIBatteryStateUpdateCallback;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class BatteryUpdateFragment extends Fragment {


    public BatteryUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("Battery Update Fragment");
        textView.setVisibility(View.INVISIBLE);
        return textView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StartBatteryUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        StartBatteryUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove the battery state callback
        try {
            DJIAircraft djiAircraft = DjiApplication.getAircraftInstance();
            if (djiAircraft != null) {
                djiAircraft.getBattery().setBatteryStateUpdateCallback(null);
            }
        } catch (Exception exception) {
            // TODO - add event to catch exception.
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the battery state callback
        try {
            DJIAircraft djiAircraft = DjiApplication.getAircraftInstance();
            if (djiAircraft != null) {
                djiAircraft.getBattery().setBatteryStateUpdateCallback(null);
            }
        } catch (Exception exception) {
            // TODO - add event to catch exception.
        }
    }

    private void StartBatteryUpdates() {
        try {
            DJIAircraft djiAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
            if (djiAircraft != null) {
                djiAircraft.getBattery().setBatteryStateUpdateCallback(new DJIBatteryStateUpdateCallback() {
                    @Override
                    public void onResult(DJIBatteryState djiBatteryState) {
                        EventBus.getDefault().post(new GreenRobotEvents.BatteryLevel(String.valueOf(djiBatteryState.getBatteryEnergyRemainingPercent())));
                    }
                });

            } else {
                // TODO - add event to catch aircraft is null
            }
        } catch (Exception e) {
            // TODO - add event to catch exception
        }
    }
}
