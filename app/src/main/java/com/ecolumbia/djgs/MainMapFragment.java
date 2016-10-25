package com.ecolumbia.djgs;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import dji.common.flightcontroller.DJIFlightControllerCurrentState;
import dji.common.flightcontroller.DJILocationCoordinate2D;
import dji.common.flightcontroller.DJILocationCoordinate3D;
import dji.midware.data.model.P3.DataFlycStartHotPointMissionWithInfo;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;


public class MainMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, View.OnClickListener {

    private final static String TAG = MainMapFragment.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private GoogleMap mMap;
    private Marker mMarkerAircraft;
    private Marker mMarkerHome;
    private static View view;
    private OnFragmentInteractionListenerMap mListener;
    private TextView tv_OnTopMap;

    public MainMapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_main_map, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }

        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        tv_OnTopMap = (TextView) view.findViewById(R.id.tv_OnTopMap);
        if (tv_OnTopMap != null) {
            tv_OnTopMap.setOnClickListener(this);
            // Create an instance of GoogleAPIClient.
            if (R.id.ll_FragmentContainer_Large == ((ViewGroup) getView().getParent()).getId()) {
                tv_OnTopMap.setVisibility(View.INVISIBLE);
            } else {
                tv_OnTopMap.setVisibility(View.VISIBLE);
            }
        }

        try {
            com.google.android.gms.maps.MapFragment mapFragment = (com.google.android.gms.maps.MapFragment) getActivity().getFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } catch (Exception e) {

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
        try {
            mListener = (OnFragmentInteractionListenerMap) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mGoogleApiClient.disconnect();
        mListener = null;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } catch (SecurityException securityException) {

        }
        if (mLastLocation != null) {
            UpdateMap();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        try {
            // Add an additional check to ensure ActivityCompat.checkSelfPermission goes not provide an error when there is not activity.
            if (getActivity() != null) {
                if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
            }
        } catch (SecurityException se) {
            Log.e(TAG, se.getMessage());
        }
    }

    private void UpdateMap() {

        String stLatitude = String.valueOf(mLastLocation.getLatitude());
        String stLongitude = String.valueOf(mLastLocation.getLongitude());
        Double dbLatitude;
        Double dbLongitude;
        try {
            dbLatitude = Double.parseDouble(stLatitude);
            dbLongitude = Double.parseDouble(stLongitude);
        } catch (Exception e) {
            dbLatitude = 0.0;
            dbLongitude = 0.0;
        }
        LatLng positionHome = new LatLng(dbLatitude, dbLongitude);
        try {
            mMap.addMarker(new MarkerOptions()
                    .position(positionHome)
                    .title("Marker"))
                    .setVisible(false);
            CameraUpdate center =
                    CameraUpdateFactory.newLatLng(positionHome);
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

            mMap.moveCamera(center);
            mMap.animateCamera(zoom);

            // Get a marker for the aircraft but do not show it.
            mMarkerAircraft = mMap.addMarker(new MarkerOptions()
                    .position(positionHome)
                    .title("Aircraft"));
            mMarkerAircraft.setVisible(false);
            mMarkerAircraft.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_logo_toolbar_black_48dp)));

            // Get a marker for home but do not show it.
            mMarkerHome = mMap.addMarker(new MarkerOptions()
                    .position(positionHome)
                    .title("Home"));
            mMarkerHome.setVisible(false);
            mMarkerHome.setIcon((BitmapDescriptorFactory.fromResource(R.drawable.ic_action_home_black_18dp)));
        } catch (Exception e) {

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == tv_OnTopMap.getId()) {
            boolean blOnTopJust_click = true;
            if (mListener != null) {
                mListener.onFragmentInteractionMap(blOnTopJust_click);
            }
        }
    }

    public interface OnFragmentInteractionListenerMap {
        public void onFragmentInteractionMap(boolean bl_OnTopJustClicked);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(GreenRobotEvents.FlightControllerCurrentState event) {
        DJIFlightControllerCurrentState fltCtrlCurrentState = event.flightControllerCurrentState;
        DJILocationCoordinate3D coor3d = fltCtrlCurrentState.getAircraftLocation();
        Double lat = coor3d.getLatitude();
        Double lng = coor3d.getLongitude();

        // Find heading
        DJIAircraft djiAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
        double heading = djiAircraft.getFlightController().getCompass().getHeading();
        showAircraft(coor3d, heading);
        DJILocationCoordinate2D coorHome = fltCtrlCurrentState.getHomeLocation();
        if (fltCtrlCurrentState.isHomePointSet()) {
            showHome(coorHome, true);
        } else {
            showHome(coorHome, false);
        }
    }

    private void showHome(DJILocationCoordinate2D coorHome, boolean ShowHomeMarker) {

        try {
            Double dbLat = coorHome.getLatitude();
            Double dbLng = coorHome.getLongitude();
            LatLng positionHome = new LatLng(dbLat, dbLng);
            mMarkerHome.setVisible(ShowHomeMarker);
            mMarkerHome.setPosition(positionHome);
        } catch (Exception e) {
            Log.e(TAG, "Error obtaining home location coordinates: " + e.getMessage());
        }
    }

    private void showAircraft(DJILocationCoordinate3D coor3D, double Heading) {

        try {
            Double dbLat = coor3D.getLatitude();
            Double dbLng = coor3D.getLongitude();
            LatLng positionAircraft = new LatLng(dbLat, dbLng);
            mMarkerAircraft.setPosition(positionAircraft);
            mMarkerAircraft.setVisible(true);
            mMarkerAircraft.setRotation((float) Heading);
        } catch (Exception e) {
            Log.e(TAG, "Error obtaining aircraft location coordinates: " + e.getMessage());
        }
    }


}
