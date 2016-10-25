package com.ecolumbia.djgs;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJIFlightControllerDataType;
import dji.sdk.flightcontroller.DJIFlightController;

/**
 * Created by djacc on 8/30/2016.
 */
public class GreenRobotEvents {

    // Returns remote controller battery remaining percent
    public static class RemoteControllerBatteryState {
        public final int remoteControllerBatteryRemainingPercent;
        public RemoteControllerBatteryState(int remoteControllerBatteryRemainingPercent) {
            this.remoteControllerBatteryRemainingPercent = remoteControllerBatteryRemainingPercent;
        }
    }

    // Returns batter level - this is remaining battery on the drone
    public static class BatteryLevel {
        public final String batteryLevel;
        public BatteryLevel(String batteryLevel) {this.batteryLevel = batteryLevel; }
    }

    // Returns airlink downlink signal - this is a measure of how strong the video link is.
    public static class AirLinkDownlinkLevel {
        public final int airLinkDownlinkLevel;
        public AirLinkDownlinkLevel(int airLinkDownlinkLevel) {
            this.airLinkDownlinkLevel = airLinkDownlinkLevel;
        }
    }

    // Returns the strength of the airlink uplink signal.
    public static class AirlinkUplinkLevel {
        public final int airLinkUplinkLevel;
        public AirlinkUplinkLevel(int airLinkUplinkLevel) {
            this.airLinkUplinkLevel = airLinkUplinkLevel;
        }
    }

    // Returns if the product is connected or not.
    public static class ProductConnected {
        public final Boolean isConnected;
        public ProductConnected(boolean isConnected) {
            this.isConnected = isConnected;
        }
    }

    // Returns if the App was registered or not.
    public static class RegisterApp {
        public final Boolean isAppRegistered;
        public RegisterApp(boolean isAppRegistered) {
            this.isAppRegistered = isAppRegistered;
        }
    }

    // Returns a text message showing the status of the drone: connected/disconnected...
    public static class DroneStatus {
        public final String droneStatus;
        public DroneStatus(String droneStatus) {
            this.droneStatus = droneStatus;
        }
    }

    // Returns the current flight controller state
    public static class FlightControllerCurrentState {
        public final dji.common.flightcontroller.DJIFlightControllerCurrentState flightControllerCurrentState;
        public FlightControllerCurrentState(dji.common.flightcontroller.DJIFlightControllerCurrentState flightControllerCurrentState) {
            this.flightControllerCurrentState = flightControllerCurrentState;
        }
    }

    // Returns the current Camera Mode
    public static class CameraModeCurrentState {
        public final DJICameraSettingsDef.CameraMode cameraModeCurrentState;
        public CameraModeCurrentState(DJICameraSettingsDef.CameraMode cameraModeCurrentState) {
            this.cameraModeCurrentState = cameraModeCurrentState;
        }
    }

    // Returns the current Camera Mode
    public static class ShowTemporaryMessage {
        public final String temporaryMessage;
        public ShowTemporaryMessage(String temporaryMessage) {
            this.temporaryMessage = temporaryMessage;
        }
    }

    // Returns the sdk version
    public static class SDKVersion {
        public final String temporaryMessage;
        public SDKVersion(String temporaryMessage) {
            this.temporaryMessage = temporaryMessage;
        }
    }

    // Returns two values - use frames or use livestream. Used to control seeing either YUV frames or the livestream
    // Returns the current Camera Mode
    public static class DjiStreamType {
        public final String streamType;
        public DjiStreamType(String streamType) {
            this.streamType = streamType;
        }
    }


}
