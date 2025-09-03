package vn.cser21;

import android.Manifest;
import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.lang.reflect.Field;

import pub.devrel.easypermissions.EasyPermissions;

public class QRCodeFragment extends Fragment {

    public static QRCodeFragment newInstance(QRCodeResult listener) {
        Bundle args = new Bundle();

        QRCodeFragment fragment = new QRCodeFragment();
        fragment.setArguments(args);
        fragment.listener = listener;
        return fragment;
    }

    public static Camera cam = null;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private View root;
    private SurfaceView surfaceView;
    private Camera camera = null;
    boolean flashmode = false;
    private QRCodeResult listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = LayoutInflater.from(inflater.getContext()).inflate(R.layout.activity_qrcode, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@androidx.annotation.NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        root.findViewById(R.id.ivClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = requireActivity().getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }
            }
        });

        root.findViewById(R.id.ivFlash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashOnButton();
            }
        });
        methodRequiresPermission();
    }

    private void methodRequiresPermission() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(requireActivity(), perms)) {
            initBarcodeScanner();
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Vui lòng cấp quyền camera ! ",
                    201, perms);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeDetector == null) return;
        barcodeDetector.release();
    }

    private void flashOnButton() {
        camera = getCamera(cameraSource);
        if (camera != null) {
            try {
                Camera.Parameters param = camera.getParameters();
                param.setFlashMode(!flashmode ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(param);
                flashmode = !flashmode;
                if (flashmode) {
                } else {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private static Camera getCamera(@NonNull CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        return camera;
                    }
                    return null;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return null;
    }

    public void initBarcodeScanner() {
        barcodeDetector = new BarcodeDetector.Builder(requireContext()).setBarcodeFormats(Barcode.QR_CODE).build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(@androidx.annotation.NonNull Detector.Detections<Barcode> detections) {
                if (barcodeDetector.isOperational()) {
                    SparseArray<Barcode> barcodes = detections.getDetectedItems();
                    String code = barcodes.valueAt(0).displayValue;
                    if (!code.isEmpty()) {
                        FragmentManager fm = requireActivity().getSupportFragmentManager();
                        if (fm.getBackStackEntryCount() > 0) {
                            listener.onQRCode(code);
                            fm.popBackStack();
                        }
                    }
                }
            }
        });

        cameraSource = new CameraSource.Builder(requireContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setAutoFocusEnabled(true)
                .setRequestedFps(35.5f)
                .build();

        surfaceView = root.findViewById(R.id.sfvCamera);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.release();
            }
        });
    }


    interface QRCodeResult {
        void onQRCode(String code);
    }
}
