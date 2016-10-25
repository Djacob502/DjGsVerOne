package com.ecolumbia.djgs;


import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJIFlightControllerDataType;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class FlightControllerUpdateFragment extends Fragment {


    public FlightControllerUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("Flight Conroller Update Fragment");
        textView.setVisibility(View.INVISIBLE);
        return textView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StartFlightControllerUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the flight controller state callback
        try {
            DJIAircraft djiAircraft = DjiApplication.getAircraftInstance();
            if (djiAircraft != null) {
                DJIFlightController flightController = djiAircraft.getFlightController();
                flightController.setUpdateSystemStateCallback(null);
            }
        } catch (Exception exception) {

        }

    }


    private void StartFlightControllerUpdates() {
        try {

            DJIAircraft djiAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
            if (djiAircraft != null) {

                final DJIFlightController flightController = djiAircraft.getFlightController();

                flightController.setUpdateSystemStateCallback(
                        new DJIFlightControllerDelegate.FlightControllerUpdateSystemStateCallback() {

                            @Override
                            public void onResult(DJIFlightControllerCurrentState
                                                         djiFlightControllerCurrentState) {
                                EventBus.getDefault().post(new GreenRobotEvents.FlightControllerCurrentState(djiFlightControllerCurrentState));
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
