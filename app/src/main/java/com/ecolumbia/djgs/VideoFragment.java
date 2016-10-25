package com.ecolumbia.djgs;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import dji.sdk.airlink.DJILBAirLink;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.codec.DJICodecManager;

/**
 * Created by djacc on 9/26/2016.
 */

public class VideoFragment extends Fragment implements TextureView.SurfaceTextureListener, View.OnClickListener  {

    private static final String TAG = VideoFragment.class.getName();
    private TextView tv_OnTopVideo;
    private VideoFragment.OnFragmentInteractionListenerVideo mListener;
    protected TextureView mVideo_texture = null;
    protected SurfaceView mVideo_surface = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    private DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallback = null;
    private DJILBAirLink.DJIOnReceivedVideoCallback mOnReceivedVideoCallback = null;

    private DJIBaseProduct mProduct = null;
    private DJICamera mCamera = null;

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_video, container, false);

        return v;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            tv_OnTopVideo = (TextView) view.findViewById(R.id.tv_OnTopVideo);
            tv_OnTopVideo.setOnClickListener(this);
            if (R.id.ll_FragmentContainer_Large == ((ViewGroup) getView().getParent()).getId()) {
                tv_OnTopVideo.setVisibility(View.INVISIBLE);
            } else {
                tv_OnTopVideo.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());

        }

        initUI(view);
        initSDKCallback();

    }

    private void initUI(View view) {
        // init mVideoSurface
        mVideo_texture = (TextureView) view.findViewById(R.id.livestream_preview_ttv);
        mVideo_surface = (SurfaceView) view.findViewById(R.id.livestream_preview_sv);
        mVideo_surface.setVisibility(View.INVISIBLE);
        if (null != mVideo_texture) {
            mVideo_texture.setSurfaceTextureListener(this);
        }
    }

    private void initSDKCallback() {

        mReceivedVideoDataCallback = new DJICamera.CameraReceivedVideoDataCallback() {

            @Override
            public void onResult(byte[] videoBuffer, int size) {
                //Log.d(TAG, "camera recv video data size: " + size);
                if(mCodecManager != null){
                    // Send the raw H264 video data to codec manager for decoding
                    mCodecManager.sendDataToDecoder(videoBuffer, size);
                }else {
                    Log.e(TAG, "mCodecManager is null");
                }
            }
        };



    }

    @Override
    public void onResume() {
        setVideoCallbacks();
        super.onResume();
        if (mVideo_texture == null) {
            Log.e(TAG, "video texture surface is null");
        }
    }

    @Override
    public void onPause() {
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        uninitPreviewer();
        super.onDestroy();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setVideoCallbacks();
        try {
            mListener = (VideoFragment.OnFragmentInteractionListenerVideo) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        uninitPreviewer();
        mListener = null;
    }



    @Override
    public void onClick(View v) {
        if (v.getId() == tv_OnTopVideo.getId()) {
            boolean blOnTopJust_click = true;
            if (mListener != null) {
                mListener.onFragmentInteractionVideo(blOnTopJust_click);
            }
        }
    }

    private void setVideoCallbacks() {

        mProduct = DjiApplication.getProductInstance();

        mCamera = mProduct.getCamera();
        if (mCamera != null) {
            mCamera.setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallback);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(getActivity(), surfaceTexture, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    public interface OnFragmentInteractionListenerVideo {
        public void onFragmentInteractionVideo(boolean bl_OnTopJustClicked);
    }

    private void uninitPreviewer() {
        mProduct = DjiApplication.getProductInstance();

        mCamera = mProduct.getCamera();
        if (mCamera != null) {
            mCamera.setDJICameraReceivedVideoDataCallback(null);
        }

        if (mCodecManager != null) {

            mCodecManager.cleanSurface();
            mCodecManager.destroyCodec();
            mCodecManager = null;
        }

    }


}
