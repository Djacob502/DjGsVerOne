package com.ecolumbia.djgs;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import dji.sdk.products.DJIAircraft;
import dji.sdk.products.DJIHandHeld;
import dji.sdk.remotecontroller.DJIRemoteController;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.common.error.DJIError;;
import dji.common.error.DJISDKError;

/**
 * Created by DaveJacob on 8/4/2016.
 */
public class DjiApplication extends Application {
    private static final String TAG = DjiApplication.class.getName();
    public static final String FLAG_CONNECTION_CHANGE = DjiApplication.class.getSimpleName();
    private static DJIBaseProduct mProduct;

    /**
     * Gets instance of the specific product connected after the
     * API KEY is successfully validated. Please make sure the
     * API_KEY has been added in the Manifest
     */
    public static synchronized DJIBaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getDJIProduct();
        }
        return mProduct;
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof DJIAircraft;
    }

    public static boolean isHandHeldConnected() {
        return getProductInstance() != null && getProductInstance() instanceof DJIHandHeld;
    }

    public static synchronized DJIAircraft getAircraftInstance() {
        if (!isAircraftConnected()) return null;
        return (DJIAircraft) getProductInstance();
    }

    public static synchronized DJIHandHeld getHandHeldInstance() {
        if (!isHandHeldConnected()) return null;
        return (DJIHandHeld) getProductInstance();
    }

    public static void disconnectFromDrone() {
        try {
            DJISDKManager.getInstance().stopConnectionToProduct();
        } catch (Exception e) {
            Log.v(TAG, "Error: Disconnect from drone: " + e.getMessage());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /**
         * handles SDK Registration using the API_KEY
         */
        DJISDKManager.getInstance().initSDKManager(this, mDJISDKManagerCallback);
    }

    protected void attachBaseContext(Context base){
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private DJISDKManager.DJISDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.DJISDKManagerCallback() {

        @Override
        public void onGetRegisteredResult(DJIError error) {
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                EventBus.getDefault().post(new GreenRobotEvents.RegisterApp(true));
                DJISDKManager.getInstance().startConnectionToProduct();
            } else {
                EventBus.getDefault().post(new GreenRobotEvents.RegisterApp(false));
            }
            Log.v(TAG, error.getDescription());
            String stSdkVersion = DJISDKManager.getInstance().getSDKVersion();
            EventBus.getDefault().post(new GreenRobotEvents.SDKVersion(stSdkVersion));
        }

        @Override
        public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {

            Log.v(TAG, String.format("onProductChanged oldProduct: " + "%s, " + "new Product " +"%s", oldProduct, newProduct));
            mProduct = newProduct;
            if(mProduct != null) {
                mProduct.setDJIBaseProductListener(mDJIBaseProductListener);
            }

            // Show product was connected.
            EventBus.getDefault().post(new GreenRobotEvents.ProductConnected(true));
        }

        private DJIBaseProduct.DJIBaseProductListener mDJIBaseProductListener = new DJIBaseProduct.DJIBaseProductListener() {

            @Override
            public void onComponentChange(DJIBaseProduct.DJIComponentKey key, DJIBaseComponent oldComponent, DJIBaseComponent newComponent) {

                if(newComponent != null) {
                    newComponent.setDJIComponentListener(mDJIComponentListener);
                }
                Log.v(TAG, String.format("onComponent Change key " + "%s, " + "oldComponent" + "%s, " + "newComponent " + "%s", key, oldComponent, newComponent));

                //notifyStatusChange();
                EventBus.getDefault().post(new GreenRobotEvents.ProductConnected(true));
            }

            @Override
            public void onProductConnectivityChanged(boolean isConnected) {

                Log.v(TAG, "onProductConnectivityChanged: " + isConnected);

                //notifyStatusChange();
                EventBus.getDefault().post(new GreenRobotEvents.ProductConnected(isConnected));
            }

        };

        private DJIBaseComponent.DJIComponentListener mDJIComponentListener = new DJIBaseComponent.DJIComponentListener() {

            @Override
            public void onComponentConnectivityChanged(boolean isConnected) {
                //notifyStatusChange();
                EventBus.getDefault().post(new GreenRobotEvents.ProductConnected(isConnected));
            }

        };

    };


}
