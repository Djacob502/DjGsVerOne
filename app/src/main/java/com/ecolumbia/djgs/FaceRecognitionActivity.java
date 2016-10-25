package com.ecolumbia.djgs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.FaceDetector;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ecolumbia.djgs.media.DJIVideoStreamDecoder;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import dji.common.product.Model;
import dji.sdk.airlink.DJILBAirLink;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.codec.DJICodecManager;

import static org.opencv.core.Core.flip;

public class FaceRecognitionActivity extends AppCompatActivity implements DJIVideoStreamDecoder.IYuvDataListener {

    private static final String TAG = FaceRecognitionActivity.class.getSimpleName();

    private TextureView videostreamPreviewTtView;
    private SurfaceView videostreamPreviewSf;
    private SurfaceHolder videostreamPreviewSh;

    private DJIBaseProduct mProduct;
    private DJICamera mCamera;
    private DJICodecManager mCodecManager;

    private Button btnStartFaceRecognition;

    protected DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;
    protected DJILBAirLink.DJIOnReceivedVideoCallback mOnReceivedVideoCallback = null;

    byte[] m_yuvFrame;
    int m_yuvWidth;
    int m_yuvHeight;
    int m_iShow;
    int m_iFrameNumber;
    protected Bitmap mBitMapImage;
    protected byte[] mBytes;
    Paint mPaint = new Paint();
    PointF m_facesMidPoint = null;
    float m_facesConfidence = 0.0f;
    float m_facesDistance = 0.0f;
    int mframeWidth = 0;
    int mframeHeight = 0;
    int m_iShowFindFace = 0;
    int m_TryToFindFaces = 0;
    long timePrevFrame = 0;
    long timeDelta;
    long timeNow;
    CanvasThread canvasThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);



    }

    @Override
    protected void onResume() {
        super.onResume();
        initUi();
        initPreviewer();

        notifyStatusChange();
        DJIVideoStreamDecoder.getInstance().resume();
    }

    @Override
    protected void onPause() {
        if (null == mProduct || !mProduct.isConnected()) {
            mCamera = null;
        } else {
            if (!mProduct.getModel().equals(Model.UnknownAircraft)) {
                mCamera = mProduct.getCamera();
                if (mCamera != null) {
                    mCamera.setDJICameraReceivedVideoDataCallback(null);
                }
            } else {
                if (null != mProduct.getAirLink()) {
                    if (null != mProduct.getAirLink().getLBAirLink()) {
                        mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(null);
                    }
                }
            }
        }

        videostreamPreviewTtView.setSurfaceTextureListener(null);
        videostreamPreviewSh.removeCallback(null);
        videostreamPreviewTtView.setVisibility(View.GONE);
        videostreamPreviewSf.setVisibility(View.GONE);

        boolean retry = true;
        if (canvasThread != null) {
            canvasThread.setRunning(false);
            while (retry) {
                try {
                    canvasThread.join();
                    retry = false;
                } catch (InterruptedException e) {

                }
            }
        }


        DJIVideoStreamDecoder.getInstance().stop();
        super.onPause();
        finish(); // When the activity is paused, the back button is pressed, then stop the activity entirely.
    }

    @Override
    protected void onDestroy() {

        try {
            DJIVideoStreamDecoder.getInstance().destroy();
            Log.e(TAG, "DJI Video stream decoder was destroyed");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        super.onDestroy();
    }

    private void initUi() {
        btnStartFaceRecognition = (Button) findViewById(R.id.btnFrames);
        videostreamPreviewTtView = (TextureView) findViewById(R.id.livestream_ttv);
        videostreamPreviewSf = (SurfaceView) findViewById(R.id.livestream_sv);
        videostreamPreviewSh = videostreamPreviewSf.getHolder();
        videostreamPreviewSh.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                DJIVideoStreamDecoder.getInstance().init(getApplicationContext(), videostreamPreviewSh.getSurface());
                DJIVideoStreamDecoder.getInstance().setYuvDataListener(FaceRecognitionActivity.this);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                try {
                    DJIVideoStreamDecoder.getInstance().changeSurface(holder.getSurface());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                holder.removeCallback(null);
                boolean retry = true;
                if (canvasThread != null) {
                    canvasThread.setRunning(false);
                    while (retry) {
                        try {
                            canvasThread.join();
                            retry = false;
                        } catch (InterruptedException e) {

                        }
                    }
                }
            }
        });
    }

    private void notifyStatusChange() {

        mProduct = DjiApplication.getProductInstance();

        Log.d(TAG, "notifyStatusChange: " + (mProduct == null ? "Disconnect" : (mProduct.getModel() == null ? "null model" : mProduct.getModel().name())));


        mReceivedVideoDataCallBack = new DJICamera.CameraReceivedVideoDataCallback() {

            @Override
            public void onResult(byte[] videoBuffer, int size) {
                Log.d(TAG, "camera recv video data size: " + size);
                DJIVideoStreamDecoder.getInstance().parse(videoBuffer, size);
            }
        };
        mOnReceivedVideoCallback = new DJILBAirLink.DJIOnReceivedVideoCallback() {

            @Override
            public void onResult(byte[] videoBuffer, int size) {
                Log.d(TAG, "airlink recv video data size: " + size);
                DJIVideoStreamDecoder.getInstance().parse(videoBuffer, size);
            }
        };

        if (null == mProduct || !mProduct.isConnected()) {
            mCamera = null;
        } else {
            if (!mProduct.getModel().equals(Model.UnknownAircraft)) {
                mCamera = mProduct.getCamera();
                if (mCamera != null) {
                    mCamera.setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallBack);
                }
            } else {
                if (null != mProduct.getAirLink()) {
                    if (null != mProduct.getAirLink().getLBAirLink()) {
                        mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(mOnReceivedVideoCallback);
                    }
                }
            }
        }
    }

    /**
     * Init a fake texture view to for the codec manager, so that the video raw data can be received
     * by the camera
     */
    private void initPreviewer() {
        videostreamPreviewTtView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.d(TAG, "real onSurfaceTextureAvailable");
                if (mCodecManager == null) {
                    mCodecManager = new DJICodecManager(getApplicationContext(), surface, width, height);
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void useLiveStream() {
        boolean retry = true;
        if (canvasThread != null) {
            canvasThread.setRunning(false);
            while (retry) {
                try {
                    canvasThread.join();
                    retry = false;
                } catch (InterruptedException e) {

                }
            }
        }
        DJIVideoStreamDecoder.getInstance().changeSurface(videostreamPreviewSh.getSurface());
    }

    private void useFrames() {
        DJIVideoStreamDecoder.getInstance().changeSurface(null);
        if (canvasThread == null) {
            canvasThread = new CanvasThread(videostreamPreviewSh);

        }
        canvasThread.setRunning(true);
        canvasThread.start();
    }

    public void btnLiveStream(View v) {
        useLiveStream();
    }

    public void btnFrames(View v) {
        useFrames();
        btnStartFaceRecognition.setEnabled(false);

    }


    @Override
    public void onYuvDataReceived(byte[] yuvFrame, int width, int height) {

        // YUV data received one frame at a time.
        if (m_iShow == 1) {
            m_iShow = 0;

            Log.e(TAG, "Receving frame" + m_iFrameNumber++);
            // Set m_yuvFrame which is then drawn in another thread.
            m_yuvFrame = java.util.Arrays.copyOf(yuvFrame, yuvFrame.length);
            m_yuvWidth = width;
            m_yuvHeight = height;
        } else {
            m_iShow++;
        }
    }


    public void draw(Canvas canvas) {
        if (m_yuvFrame != null) {
            if (m_yuvFrame.length > 0) {
                if (videostreamPreviewSh != null) {
                    showFrame(canvas, videostreamPreviewSh, m_yuvFrame, m_yuvWidth, m_yuvHeight);
                }
            }
        }
    }


    private void showFrame(Canvas canvas, SurfaceHolder holder, byte[] yuvFrame, int yuv_width, int yuv_height) {

        if (holder != null) {
            //Canvas canvas = holder.lockCanvas();
            // Convert YUV to bitmap.
            if (mBitMapImage != null) {
                mBitMapImage.recycle();
            }
            mBitMapImage = createBitmapFromYuvFrame(yuvFrame, yuv_width, yuv_height);
            if (canvas == null) {
                Log.e(TAG, "Cannot draw onto the canvas as it's null");
            } else {
                int canvas_width = holder.getSurfaceFrame().width();
                int canvas_height = holder.getSurfaceFrame().height();
                updateCanvas(canvas, mBitMapImage, canvas_width, canvas_height);
            }
        }
        mBytes = null;
        mBitMapImage.recycle();

    }

    private void updateCanvas(final Canvas canvas, Bitmap bm, int canvasWidth, int canvasHeight) {

        canvas.drawBitmap(bm, null, new RectF(0, 0, canvasWidth, canvasHeight), mPaint);
        Paint redPaint = new Paint();
        redPaint.setStyle(Paint.Style.STROKE);
        redPaint.setColor(Color.rgb(255, 0, 0));
        redPaint.setStrokeWidth(10);
        if (m_facesMidPoint != null) {
            int adjust_x = Math.round((1920 - mframeWidth) / 2.0f);
            int facesDistance = Math.round(m_facesDistance / 2);
            canvas.drawRect(m_facesMidPoint.x - facesDistance + adjust_x, m_facesMidPoint.y, m_facesMidPoint.x + facesDistance + adjust_x, m_facesMidPoint.y + (2 * facesDistance), redPaint);

        }
        Paint bluePaint = new Paint();
        bluePaint.setStyle(Paint.Style.STROKE);
        bluePaint.setColor(Color.rgb(8, 0, 153));
        bluePaint.setStrokeWidth(30);

    }

    private Bitmap createBitmapFromYuvFrame(byte[] yuvFrame, int yuv_Width, int yuv_Height) {
        // Create bitmap from (newBuf, newWidth, newHeight);
        Mat mat_newBuf = new Mat(yuv_Height + yuv_Height / 2, yuv_Width, CvType.CV_8UC1);
        mat_newBuf.put(0, 0, yuvFrame);

        // Convert mat to BGRA to create a valid
        Mat matBRGA = new Mat(DJIVideoStreamDecoder.getInstance().height, DJIVideoStreamDecoder.getInstance().width, CvType.CV_8UC4);
        Imgproc.cvtColor(mat_newBuf, matBRGA, Imgproc.COLOR_YUV420sp2BGRA);
        flip(matBRGA, matBRGA, 1); // Flip the image around the x axis.

        // convert to bitmap:
        Bitmap bm_Format_565 = Bitmap.createBitmap(matBRGA.cols(), matBRGA.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(matBRGA, bm_Format_565);
        mframeWidth = bm_Format_565.getWidth();
        mframeHeight = bm_Format_565.getHeight();
        if (m_iShowFindFace == 25) {
            m_iShowFindFace = 0;

            try {
                FaceDetector.Face[] faces = new FaceDetector.Face[1];
                FaceDetector fd = new FaceDetector(bm_Format_565.getWidth(), bm_Format_565.getHeight(), 1);
                int countFaces = fd.findFaces(bm_Format_565, faces);
                // check if we detect any faces
                if (countFaces > 0) {
                    int[] fpx = new int[countFaces];
                    int[] fpy = new int[countFaces];
                    PointF midpoint = new PointF();
                    for (int i = 0; i < countFaces; i++) {
                        try {
                            faces[i].getMidPoint(midpoint);
                            fpx[i] = (int) midpoint.x;
                            fpy[i] = (int) midpoint.y;
                            m_facesMidPoint = midpoint;
                            m_facesConfidence = faces[i].confidence();
                            m_facesDistance = faces[i].eyesDistance();
                        } catch (Exception e) {
                            Log.e("faces midpoint", "Faces midpoint error: " + e.getMessage());
                        }
                    }
                }
                Log.e(TAG, "Found Faces: " + countFaces + " - Attempts at find faces: " + m_TryToFindFaces++);
            } catch (Exception e) {
                Log.e(TAG, "Error Face: " + e.getMessage());
            }
        } else {
            m_iShowFindFace++;
        }

        mat_newBuf.release();
        matBRGA.release();
        return bm_Format_565;
    }


    class CanvasThread extends Thread {
        private SurfaceHolder surfaceHolder;
        private boolean run = false;

        public CanvasThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean run) {
            this.run = run;
        }

        public SurfaceHolder getSurfaceHolder() {
            return surfaceHolder;
        }

        @Override
        public void run() {
            Canvas c;
            while (run) {
                c = null;

                //limit frame rate to max 60fps
                timeNow = System.currentTimeMillis();
                timeDelta = timeNow - timePrevFrame;
                if (timeDelta < 16) {
//                    try {
//                        Thread.sleep(16 - timeDelta);
//                    } catch (InterruptedException e) {
//
//                    }
                }
                timePrevFrame = System.currentTimeMillis();

                try {
                    c = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        //call methods to draw and process next fame
                        if (c != null) {
                            draw(c);
                        }
                    }
                } finally {
                    if (c != null) {
                        try {
                            surfaceHolder.unlockCanvasAndPost(c);
                        } catch (Exception e) {Log.e(TAG, "surface Unlock" + e.getMessage());}
                    }
                }
            }
        }
    }

}

