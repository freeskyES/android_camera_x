package com.es.camerax

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.Camera
import android.hardware.camera2.*
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.es.camerax.utils.ANIMATION_FAST_MILLIS
import com.es.camerax.utils.ANIMATION_SLOW_MILLIS
import com.es.camerax.utils.AutoFitPreviewBuilder
import com.es.camerax.utils.simulateClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Semaphore
import kotlin.coroutines.CoroutineContext


private const val TAG = "CameraFragment"
private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
private const val PHOTO_EXTENSION = ".jpg"

private fun createFile(baseFolder: File, format: String, extension: String): File {
    return File(baseFolder,
        SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension)
}

class CameraFragment : Fragment(), CoroutineScope, LifecycleOwner {

    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: TextureView
    private lateinit var outputDirectory: File
    private lateinit var switchBtn: ImageButton
    private lateinit var flashBtn: ImageButton


    private var lensFacing = CameraX.LensFacing.BACK
    private var imageCapture: ImageCapture? = null
    private var isFlash = false



    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job


    private val cameraOpenCloseLock = Semaphore(1)

    private var cameraDevice: CameraDevice? = null

    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraFragment.cameraDevice = cameraDevice
//            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraFragment.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
            this@CameraFragment.activity?.finish()
        }
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mark this as a retain fragment, so the lifecycle does not get restarted on config change
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Unregister the broadcast receivers
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(volumeDownReceiver)

        // Turn off all camera operations when we navigate away
        CameraX.unbindAll()
    }

    override fun onDestroy() {
//        job.cancel()
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
        viewFinder = container.findViewById(R.id.view_finder)

        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        LocalBroadcastManager.getInstance(context!!).registerReceiver(volumeDownReceiver, filter)

        // Determine the output directory
        outputDirectory = CameraActivity.getOutputDirectory(requireContext())

        setViewFinder()
    }

    private fun setViewFinder() {
        viewFinder.post {

            updateCameraUi()
            bindCameraUseCases()

//            launch(coroutineContext) {
//                outputDirectory.listFiles { file ->
//                    EXTENSION_WHITELIST.contains(file.extension.toUpperCase())
//                }.sorted().reversed().firstOrNull()?.let { setGalleryThumbnail(it) }
//            }
        }
    }



        private fun setFlashMode(mode: Boolean) {

//        var builder: CaptureRequest.Builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//        var flash: Int = if (mode) {
//            CameraMetadata.FLASH_MODE_TORCH
//        } else {
//            CameraMetadata.FLASH_MODE_OFF
//        }
//        builder.set(CaptureRequest.FLASH_MODE, flash)
//        var request : CaptureRequest = builder.build();
//        cameraCaptureSession.capture(request, null, null);


        var requestBuilder : CaptureRequest.Builder = cameraDevice!!.createCaptureRequest(
            CameraDevice.TEMPLATE_PREVIEW
        )
        requestBuilder.set(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        CameraX.unbindAll()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var camManager: CameraManager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            var cameraId: String = "0"
            for (id :String in camManager.cameraIdList) {
                var c : CameraCharacteristics = camManager.getCameraCharacteristics(id)
                var flashAvailable: Boolean = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                var lensFacing : Int = c.get(CameraCharacteristics.LENS_FACING)
                if (flashAvailable != null && flashAvailable && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK){
                    cameraId = id
                }
            }
            try {
                camManager.setTorchMode(cameraId, mode)
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
//        }else {
//            ImageCaptureConfig.Builder().apply {
//                setFlashMode(mode)
//            }.build()
        }


    }

    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {
        override fun onError(
            error: ImageCapture.UseCaseError, message: String, exc: Throwable?) {
            Log.e(TAG, "Photo capture failed: $message")
            exc?.printStackTrace()
        }

        override fun onImageSaved(photoFile: File) {
            Log.d(TAG, "Photo capture succeeded: ${photoFile.absolutePath}")


//            // We can only change the foreground Drawable using API level 23+ API
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//                // Update the gallery thumbnail with latest picture taken
//                setGalleryThumbnail(photoFile)
//            }

            // Implicit broadcasts will be ignored for devices running API
            // level >= 24, so if you only target 24+ you can remove this statement
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                requireActivity().sendBroadcast(
                    Intent(Camera.ACTION_NEW_PICTURE).setData(Uri.fromFile(photoFile)))
            }

            // If the folder selected is an external media directory, this is unnecessary
            // but otherwise other apps will not be able to access our images unless we
            // scan them using [MediaScannerConnection]
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(photoFile.extension)
            MediaScannerConnection.scanFile(
                context, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null)
        }
    }

    // Volume down button receiver
    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val keyCode = intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)
            when (keyCode) {
                // When the volume down button is pressed, simulate a shutter button click
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val shutter = container
                        .findViewById<ImageButton>(R.id.camera_capture_button)
                    shutter.simulateClick()
                }
            }
        }
    }

    private fun bindCameraUseCases() {

        // Make sure that there are no other use cases bound to CameraX
        CameraX.unbindAll()

        val metrics = DisplayMetrics().also { viewFinder.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        Log.d(javaClass.simpleName, "Metrics: ${metrics.widthPixels} x ${metrics.heightPixels}")

        // Set up the view finder use case to display camera preview
        val viewFinderConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            // We request a specific resolution matching screen size
            setTargetResolution(screenSize)
            setFlashMode(true)
            // We also provide an aspect ratio in case the exact resolution is not available
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)

        }.build()


        // Use the auto-fit preview builder to automatically handle size and orientation changes
        val preview = AutoFitPreviewBuilder.build(viewFinderConfig, viewFinder)

        // Set up the capture use case to allow users to take photos
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setLensFacing(lensFacing)
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            setFlashMode(if (isFlash) FlashMode.ON else FlashMode.OFF)
            // We request aspect ratio but no resolution to match preview config but letting
            // CameraX optimize for whatever specific resolution best fits requested capture mode
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(viewFinder.display.rotation)

        }.build()

        imageCapture = ImageCapture(imageCaptureConfig)

        // Setup image analysis pipeline that computes average pixel luminance in real time
//        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
//            setLensFacing(lensFacing)
//            // Use a worker thread for image analysis to prevent preview glitches
//            val analyzerThread = HandlerThread("LuminosityAnalysis").apply { start() }
//            setCallbackHandler(Handler(analyzerThread.looper))
//            // In our analysis, we care more about the latest image than analyzing *every* image
//            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//        }.build()

//        val imageAnalyzer = ImageAnalysis(analyzerConfig).apply {
//            analyzer = LuminosityAnalyzer().apply { onFrameAnalyzed { luma ->
//                // Values returned from our analyzer are passed to the attached listener
//                // We log image analysis results here -- you should do something
//                // useful instead!
//                Log.d(TAG, "Average luminosity: $luma. " +
//                        "Frames per second: ${"%.01f".format(framesPerSecond)}") }
//            } }

        // Apply declared configs to CameraX using the same lifecycle owner
        CameraX.bindToLifecycle(
            this, preview, imageCapture/*, imageAnalyzer*/)
    }


    /** Method used to re-draw the camera UI controls, called every time configuration changes */
    @SuppressLint("RestrictedApi")
    private fun updateCameraUi() {

        // Remove previous UI if any
        container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }

        // Inflate a new view containing all UI for controlling the camera
        val controls = View.inflate(requireContext(), R.layout.camera_ui_container, container)

        // Listener for button used to capture photo
        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
            val photoFile = createFile(outputDirectory, FILENAME, PHOTO_EXTENSION)

            // Setup image capture metadata
            val metadata = ImageCapture.Metadata().apply {
                // Mirror image when using the front camera
                isReversedHorizontal = lensFacing == CameraX.LensFacing.FRONT
            }

            // Setup image capture listener which is triggered after photo has been taken
            imageCapture?.takePicture(photoFile, imageSavedListener, metadata)

            // We can only change the foreground Drawable using API level 23+ API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Display flash animation to indicate that photo was captured
                container.postDelayed({
                    container.foreground = ColorDrawable(Color.WHITE)
                    container.postDelayed({ container.foreground = null }, ANIMATION_FAST_MILLIS)
                }, ANIMATION_SLOW_MILLIS)
            }
        }

        // Listener for button used to switch cameras
        controls.findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
            lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
                CameraX.LensFacing.BACK
            } else {
                CameraX.LensFacing.FRONT
            }
            try {
                // Only bind use cases if we can query a camera with this orientation
                CameraX.getCameraWithLensFacing(lensFacing)
                bindCameraUseCases()
            } catch (exc: Exception) {
                // Do nothing
            }
        }

        // Listener for button used to view last photo
        controls.findViewById<ImageButton>(R.id.flash_button).setOnClickListener {

            isFlash = !isFlash
            setFlashMode(isFlash)
//            bindCameraUseCases()

//            val arguments = Bundle().apply {
//                putString(KEY_ROOT_DIRECTORY, outputDirectory.absolutePath) }
//            Navigation.findNavController(requireActivity(), R.id.fragment_container)
//                .navigate(R.id.action_camera_to_gallery, arguments)
        }
    }


}
