package com.ecolumbia.djgs;

import android.content.res.Resources;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.sdk.products.DJIAircraft;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.camera.DJICamera;
import dji.common.util.DJICommonCallbacks;

/**
 * Created by djacc on 8/18/2016.
 */
public class CameraPhoto {
    private static final String TAG = CameraPhoto.class.getSimpleName();
    public static void SetCameraToNewMode(final DJICameraSettingsDef.CameraMode newCameraMode){
        DJIAircraft aircraft = DjiApplication.getAircraftInstance();
        DJICamera camera;
        if (DjiApplication.getAircraftInstance() != null) {
            camera = aircraft.getCamera();
            if (camera != null) {
                camera.setCameraMode(
                        newCameraMode,
                        new DJICommonCallbacks.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (null == djiError) {
                                    EventBus.getDefault().post(new GreenRobotEvents.CameraModeCurrentState(newCameraMode));
                                } else {
                                    //TODO - return error setting camera mode
                                }
                            }
                        });
            } else {
                //TODO - return camera not available
            }
        } else {
            // TODO - return aircraft not available
        }
    }

    public static void GetCameraMode(){
        DJIAircraft aircraft = DjiApplication.getAircraftInstance();
        DJICamera camera;
        if (DjiApplication.getAircraftInstance() != null) {
            camera = aircraft.getCamera();
            if (camera != null) {
                camera.getCameraMode(new DJICommonCallbacks.DJICompletionCallbackWith<DJICameraSettingsDef.CameraMode>() {
                    @Override
                    public void onSuccess(DJICameraSettingsDef.CameraMode cameraMode) {
                        EventBus.getDefault().post(new GreenRobotEvents.CameraModeCurrentState(cameraMode));
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        // TODO return failure to obtain the camera mode.
                    }
                });
            } else {
                // TODO - return camera not available.
            }
        } else {
            // TODO - return aircraft not available
        }
    }

    public static void shootPicture_SinglePhoto() {
        DJIAircraft aircraft = DjiApplication.getAircraftInstance();
        DJICamera camera;
        if (DjiApplication.getAircraftInstance() != null) {
            camera = aircraft.getCamera();
            if (camera != null) {
                camera.startShootPhoto(
                        DJICameraSettingsDef.CameraShootPhotoMode.Single,
                        new DJICommonCallbacks.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (null == djiError) {
                                    EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage("Camera: Single Photo success"));
                                } else {
                                    EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage("Camera: Single Photo failure"));
                                }

                            }
                        }
                );
            } else {
                Log.e(TAG, "Camera Not available");
            }
        } else {
            Log.e(TAG,"Drone not available");
        }
    }

    public static void startVideoRecording() {
        DJIAircraft aircraft = DjiApplication.getAircraftInstance();
        DJICamera camera;
        if (DjiApplication.getAircraftInstance() != null) {
            camera = aircraft.getCamera();
            if (camera != null) {
                camera.startRecordVideo(
                        new DJICommonCallbacks.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (null == djiError) {
                                    EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage("Camera: Start Video success"));
                                } else {
                                    EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage("Camera: Start Video failure"));
                                }

                            }
                        }
                );
            } else {
                // TODO - show camera is not available
            }
        } else {
            // TODO - show drone is not available
        }
    }

    public static void stopVideoRecording() {
        DJIAircraft aircraft = DjiApplication.getAircraftInstance();
        DJICamera camera;
        if (DjiApplication.getAircraftInstance() != null) {
            camera = aircraft.getCamera();
            if (camera != null) {
                camera.stopRecordVideo(
                        new DJICommonCallbacks.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (null == djiError) {
                                    EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage("Camera: Stop Video success"));
                                } else {
                                    EventBus.getDefault().post(new GreenRobotEvents.ShowTemporaryMessage("Camera: Stop Video failure"));
                                }

                            }
                        }
                );
            } else {
                // TODO - show camera is not available
            }
        } else {
            // TODO - show drone is not available
        }
    }


}