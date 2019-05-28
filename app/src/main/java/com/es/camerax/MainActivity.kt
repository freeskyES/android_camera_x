package com.es.camerax


//import android.os.Bundle
//import android.Manifest
//import android.annotation.TargetApi
//import android.app.Activity
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.Matrix
//import android.hardware.camera2.CameraCharacteristics
//import android.hardware.camera2.CameraManager
//import android.os.Build
//import android.os.Handler
//import android.os.HandlerThread
//import android.util.Log
//
//import android.util.Rational
//import android.util.Size
//import android.view.*
//import android.widget.ImageButton
//import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import androidx.camera.core.*
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
//import java.io.File
//import java.nio.ByteBuffer
//import java.util.concurrent.TimeUnit


private const val REQUEST_CODE_PERMISSIONS = 10

//private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
//
//@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity : AppCompatActivity(), LifecycleOwner {

//    private lateinit var viewFinder: TextureView
//    private lateinit var switchBtn: ImageButton
//    private lateinit var flashBtn: ImageButton
//
//    private var lensFacing = CameraX.LensFacing.BACK
//    private var isFlash = false
//
//
//    private var fingerSpacing: Float = 0F
//    private var zoomLevel: Int = 1
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        viewFinder = findViewById(R.id.view_finder)
//        switchBtn = findViewById(R.id.switch_button)
//        flashBtn = findViewById(R.id.flash_button)
//
//        // request permissions
//        if(allPermissionsGranted()) {
//            viewFinder.post { startCamera() }
//        } else {
//            ActivityCompat.requestPermissions(
//                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
//        }
//        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
//            updateTransform()
//        }
//
//        switchBtn.setOnClickListener {
//            lensFacing = if (lensFacing == CameraX.LensFacing.BACK) {
//                CameraX.LensFacing.FRONT
//            } else {
//                CameraX.LensFacing.BACK
//            }
//        }
//
//        flashBtn.setOnClickListener {
//            isFlash = !isFlash
//            setFlashMode(if (isFlash) {
//                FlashMode.ON
//            } else {
//                FlashMode.OFF
//            })
//        }
//    }
//
//
//
//    private fun setFlashMode(mode: FlashMode) {
//        ImageCaptureConfig.Builder().apply {
//            setFlashMode(mode)
//        }.build()
//    }
//
//
//    // Add this after onCreate
//
//    private fun startCamera() {
//
//        val previewConfig = PreviewConfig.Builder().apply {
//            setTargetAspectRatio(Rational(1, 1))
//            setTargetResolution(Size(640, 640))
//            lensFacing
//        }.build()
//
//        val preview = Preview(previewConfig)
//
//        // Every time the viewfinder is updated, recompute layout
//        preview.setOnPreviewOutputUpdateListener {
//
//            val parent = viewFinder.parent as ViewGroup
//            parent.removeView(viewFinder)
//            parent.addView(viewFinder, 0)
//
//            viewFinder.surfaceTexture = it.surfaceTexture
//            updateTransform()
//        }
//
////========================================
//// Add this before CameraX.bindToLifecycle
//
//        // Create configuration object for the image capture use case
//        val imageCaptureConfig = ImageCaptureConfig.Builder()
//            .apply {
//                setTargetAspectRatio(Rational(1, 1))
//                // We don't set a resolution for image capture; instead, we
//                // select a capture mode which will infer the appropriate
//                // resolution based on aspect ration and requested mode
//                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
//            }.build()
//
//        // Build the image capture use case and attach button click listener
//        val imageCapture = ImageCapture(imageCaptureConfig)
//        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
//            val file = File(externalMediaDirs.first(),
//                "${System.currentTimeMillis()}.jpg")
//            imageCapture.takePicture(file,
//                object : ImageCapture.OnImageSavedListener {
//                    override fun onError(error: ImageCapture.UseCaseError,
//                                         message: String, exc: Throwable?) {
//                        val msg = "Photo capture failed: $message"
//                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                        Log.e("CameraXApp", msg)
//                        exc?.printStackTrace()
//                    }
//
//                    override fun onImageSaved(file: File) {
//                        val msg = "Photo capture succeeded: ${file.absolutePath}"
//                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                        Log.d("CameraXApp", msg)
//                    }
//                })
//        }
//
//        //===================================
//        // Add this before CameraX.bindToLifecycle
//
//        // Setup image analysis pipeline that computes average pixel luminance
//        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
//            // Use a worker thread for image analysis to prevent glitches
//            val analyzerThread = HandlerThread(
//                "LuminosityAnalysis").apply { start() }
//            setCallbackHandler(Handler(analyzerThread.looper))
//            // In our analysis, we care more about the latest image than
//            // analyzing *every* image
//            setImageReaderMode(
//                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//        }.build()
//
//        // Build the image analysis use case and instantiate our analyzer
//        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
//            analyzer = LuminosityAnalyzer()
//        }
//
//
//
//
//        CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase)
//    }
//
//    private fun updateTransform() {
//        // TODO: Implement camera viewfinder transformations
//        val matrix = Matrix()
//
//        val centerX = viewFinder.width / 2f
//        val centerY = viewFinder.height / 2f
//
//        val rotationDegrees = when(viewFinder.display.rotation) {
//            Surface.ROTATION_0 -> 0
//            Surface.ROTATION_90 -> 90
//            Surface.ROTATION_180 -> 180
//            Surface.ROTATION_270 -> 270
//            else-> return
//        }
//        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
//
//        viewFinder.setTransform(matrix)
//    }
//
//    /**
//     * Process result from permission request dialog box, has the request
//     * been granted? If yes, start Camera. Otherwise display a toast
//     */
//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        if (requestCode == REQUEST_CODE_PERMISSIONS) {
//            if (allPermissionsGranted()) {
//                viewFinder.post { startCamera() }
//            } else {
//                Toast.makeText(this,
//                    "Permissions not granted by the user.",
//                    Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }
//
//    /**
//     * Check if all permission specified in the manifest have been granted
//     */
//    private fun allPermissionsGranted(): Boolean {
//        for (permission in REQUIRED_PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(
//                    this, permission) != PackageManager.PERMISSION_GRANTED) {
//                return false
//            }
//        }
//        return true
//    }
//
//    private class LuminosityAnalyzer : ImageAnalysis.Analyzer {
//        private var lastAnalyzedTimestamp = 0L
//
//        /**
//         * Helper extension function used to extract a byte array from an
//         * image plane buffer
//         */
//        private fun ByteBuffer.toByteArray(): ByteArray {
//            rewind()    // Rewind the buffer to zero
//            val data = ByteArray(remaining())
//            get(data)   // Copy the buffer into a byte array
//            return data // Return the byte array
//        }
//
//        override fun analyze(image: ImageProxy, rotationDegrees: Int) {
//            val currentTimestamp = System.currentTimeMillis()
//            // Calculate the average luma no more often than every second
//            if (currentTimestamp - lastAnalyzedTimestamp >=
//                TimeUnit.SECONDS.toMillis(1)) {
//                // Since format in ImageAnalysis is YUV, image.planes[0]
//                // contains the Y (luminance) plane
//                val buffer = image.planes[0].buffer
//                // Extract image data from callback object
//                val data = buffer.toByteArray()
//                // Convert the data into an array of pixel values
//                val pixels = data.map { it.toInt() and 0xFF }
//                // Compute average luminance for the image
//                val luma = pixels.average()
//                // Log the new luma value
//                Log.d("CameraXApp", "Average luminosity: $luma")
//                // Update timestamp of last analyzed frame
//                lastAnalyzedTimestamp = currentTimestamp
//            }
//        }
//    }

//    fun onTouch(v : View,
//                event: MotionEvent) : Boolean {
//
//        var manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
//
//        var cameraId: Int
//
//        for ()
//
//
//        var characteristics: CameraCharacteristics = manager.getCameraCharacteristics()
//
//
//        String cameraId = null;
//        for (String each : manager.getCameraIdList()) {
//            if (this.facing == manager.getCameraCharacteristics(each).get(CameraCharacteristics.LENS_FACING)) {
//                cameraId = each;
//                break;
//            }
//            if (cameraId == null) throw new Exception("No correct facing camera is found.");
//
//
//
//    }
//public boolean onTouch(View v, MotionEvent event) {
//    try {
//        Activity activity = getActivity();
//        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
//
//
//        CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
//        float maxzoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM))*10;
//
//        Rect m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
//        int action = event.getAction();
//        float current_finger_spacing;
//
//        if (event.getPointerCount() > 1) {
//            // Multi touch logic
//            current_finger_spacing = getFingerSpacing(event);
//            if(finger_spacing != 0){
//                if(current_finger_spacing > finger_spacing && maxzoom > zoom_level){
//                    zoom_level++;
//                } else if (current_finger_spacing < finger_spacing && zoom_level > 1){
//                    zoom_level--;
//                }
//                int minW = (int) (m.width() / maxzoom);
//                int minH = (int) (m.height() / maxzoom);
//                int difW = m.width() - minW;
//                int difH = m.height() - minH;
//                int cropW = difW /100 *(int)zoom_level;
//                int cropH = difH /100 *(int)zoom_level;
//                cropW -= cropW & 3;
//                cropH -= cropH & 3;
//                Rect zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
//                mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
//            }
//            finger_spacing = current_finger_spacing;
//        } else{
//            if (action == MotionEvent.ACTION_UP) {
//                //single touch logic
//            }
//        }
//
//        try {
//            mCaptureSession
//                .setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        } catch (NullPointerException ex) {
//            ex.printStackTrace();
//        }
//    } catch (CameraAccessException e) {
//        throw new RuntimeException("can not access camera.", e);
//    }
//    return true;
//}

}
