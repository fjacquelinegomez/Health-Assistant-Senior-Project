package com.example.healthassistant;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        //sets up where video will be held
        previewView = findViewById(R.id.previewView);
        cameraExecutor = Executors.newSingleThreadExecutor();

        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    //set camera up
    @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
    private void bindCameraUseCases() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Toast.makeText(this, "Preview Created", Toast.LENGTH_SHORT).show();
        //checks and decodes barcodes
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
            if (image.getImage() == null) {
                image.close();
                return;
            }

            try {
                InputImage inputImage = InputImage.fromMediaImage(
                        image.getImage(), image.getImageInfo().getRotationDegrees());
                scanBarcode(inputImage);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                image.close();
            }
        });

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }
    //scans the barcode
    private void scanBarcode(InputImage image) {
        BarcodeScanner scanner = BarcodeScanning.getClient();
        scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        String barcodeValue = barcode.getRawValue();
                        Intent intent = new Intent(getApplicationContext(), Search.class);
                        intent.putExtra("message_key", barcodeValue);
                        Toast.makeText(this, "Scanned: " + barcodeValue, Toast.LENGTH_SHORT).show();
                        Log.d("BarcodeScanner", "Barcode detected: " + barcodeValue);
                        cameraExecutor.shutdown();
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> Log.e("BarcodeScanner", "Failed to scan barcode", e));
    }
    //ends camera
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
