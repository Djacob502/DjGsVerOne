package com.ecolumbia.djgs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import dji.common.remotecontroller.DJIRCBatteryInfo;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.products.DJIAircraft;
import dji.sdk.remotecontroller.DJIRemoteController;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class RemoteControlUpdateFragment extends Fragment {


    public RemoteControlUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("Remote Control Update Fragment");
        textView.setVisibility(View.INVISIBLE);
        return textView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StartRemoteControllerBatteryUpdates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the remote controllers two updates: battery and hardware state.
        try {
            DJIAircraft djiAircraft = DjiApplication.getAircraftInstance();
            if (djiAircraft != null) {
                DJIRemoteController remoteController = djiAircraft.getRemoteController();
                remoteController.setBatteryStateUpdateCallback(null);
            }
        } catch (Exception exception) {

        }
    }

    private void StartRemoteControllerBatteryUpdates() {
        try {

            DJIAircraft djiAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
            if (djiAircraft != null) {

                final DJIRemoteController remoteController = djiAircraft.getRemoteController();
                remoteController.setBatteryStateUpdateCallback(new DJIRemoteController.RCBatteryStateUpdateCallback() {
                    @Override
                    public void onBatteryStateUpdate(DJIRemoteController djiRemoteController, DJIRCBatteryInfo djircBatteryInfo) {
                        EventBus.getDefault().post(new GreenRobotEvents.RemoteControllerBatteryState (djircBatteryInfo.remainingEnergyInPercent));
                    }
                });

            } else {
                // TODO - send event when aircraft is null.
            }
        } catch (Exception e) {
            // TODO - send event when error obtaining updates
        }
    }
}
