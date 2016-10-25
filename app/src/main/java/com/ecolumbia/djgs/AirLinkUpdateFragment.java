package com.ecolumbia.djgs;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;

import dji.common.airlink.DJISignalInformation;
import dji.sdk.airlink.DJILBAirLink;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * A simple {@link Fragment} subclass.
 */
public class AirLinkUpdateFragment extends Fragment {


    public AirLinkUpdateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText("Airlink Update Fragment");
        textView.setVisibility(View.INVISIBLE);
        return textView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove the battery state callback
        try {
            DJIAircraft djiAircraft = (DJIAircraft) DjiApplication.getAircraftInstance();
            if (djiAircraft != null) {
                djiAircraft.getAirLink().getLBAirLink().setLBAirLinkUpdatedRemoteControllerSignalInformationCallback(null);
            }
        } catch (Exception exception) {
            // TODO - add event to catch exception.
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startAirlinkUplinkUpdates();
        startAirlinkDownlinkUpdates();
    }

    private void startAirlinkUplinkUpdates() {
        try {
            DJIAircraft djiAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
            if (djiAircraft != null) {

                djiAircraft.getAirLink().getLBAirLink().setLBAirLinkUpdatedRemoteControllerSignalInformationCallback(new DJILBAirLink.DJILBAirLinkUpdatedRemoteControllerSignalInformationCallback() {
                    @Override
                    public void onResult(ArrayList<DJISignalInformation> arrayList) {
                        Iterator iter = arrayList.iterator();
                        DJISignalInformation djiSignalInformation;
                        double total_percent = 0;
                        double total_antennas = 0;
                        while (iter.hasNext()) {
                            djiSignalInformation = (DJISignalInformation) iter.next();
                            total_percent += djiSignalInformation.getPercent();
                            total_antennas ++;
                        }
                        int percent = 0;
                        if (total_antennas > 0 ) {
                            percent = (int) (Math.round(total_percent / total_antennas));
                        }
                        EventBus.getDefault().post(new GreenRobotEvents.AirlinkUplinkLevel(percent));
                    }
                });
            } else {
                // TODO - add event to catch aircraft is null
            }
        } catch (Exception e) {
            // TODO - add event to catch exception
        }
    }

    private void startAirlinkDownlinkUpdates() {
        try {
            DJIAircraft djiAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
            if (djiAircraft != null) {

                djiAircraft.getAirLink().getLBAirLink().setDJILBAirLinkUpdatedLightbridgeModuleSignalInformationCallback(new DJILBAirLink.DJILBAirLinkUpdatedLightbridgeModuleSignalInformationCallback() {
                    @Override
                    public void onResult(ArrayList<DJISignalInformation> arrayList) {
                        Iterator iter = arrayList.iterator();
                        DJISignalInformation djiSignalInformation;
                        double total_percent = 0;
                        double total_antennas = 0;
                        while (iter.hasNext()) {
                            djiSignalInformation = (DJISignalInformation) iter.next();
                            total_percent += djiSignalInformation.getPercent();
                            total_antennas ++;
                        }
                        int percent = 0;
                        if (total_antennas > 0 ) {
                            percent = (int) (Math.round(total_percent / total_antennas));
                        }
                        EventBus.getDefault().post(new GreenRobotEvents.AirLinkDownlinkLevel(percent));
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
