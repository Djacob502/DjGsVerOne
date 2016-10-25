package com.ecolumbia.djgs;

import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.base.DJIBaseComponent;
import dji.common.error.DJIError;
import dji.common.util.DJICommonCallbacks;

import static android.content.res.Resources.*;


/**
 * Created by djacc on 9/1/2016.
 */
public class AircraftControls {

    public static void Takeoff(){
        DJIAircraft djiAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();

        if ((DjiApplication.getProductInstance() instanceof DJIAircraft) && null != DjiApplication.getProductInstance()) {
            DJIFlightController FlightController = djiAircraft.getFlightController();
            FlightController.takeOff(new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage("Error issuing takeoff command") + djiError.getDescription());
                    } else {
                        EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage("Takeoff Successfully issued: "));
                    }
                }


            });
        }
    }

    public static void Land() {

        DJIAircraft djiAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();

        if ((DjiApplication.getProductInstance() instanceof DJIAircraft) && null != DjiApplication.getProductInstance()) {
            DJIFlightController flightController = djiAircraft.getFlightController();
            flightController.autoLanding(new DJICommonCallbacks.DJICompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError != null) {
                        String error_Landing = "Landing  command not issued successfully - ";
                        EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage(error_Landing + djiError.getDescription()));
                    } else {
                        String success_Landing = "Landing command issued successfully";
                        EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage(success_Landing));
                    }
                }
            });


        }
    }
}
