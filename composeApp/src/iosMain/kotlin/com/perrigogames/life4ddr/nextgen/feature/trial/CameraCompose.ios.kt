package com.perrigogames.life4ddr.nextgen.feature.trial

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.perrigogames.life4ddr.nextgen.util.currentDateTimeFilename
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.io.files.Path
import platform.AVFoundation.*
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSMutableArray
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.writeToFile
import platform.Photos.*
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIImage
import platform.UIKit.UIView

// Thanks to ProAndroidDev: https://proandroiddev.com/compose-multi-platform-custom-camera-with-common-capture-design-386dbc2aa03e
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CameraView(callback: CameraCallback) {
    val session = AVCaptureSession()
    session.sessionPreset = AVCaptureSessionPresetPhoto
    val output = AVCaptureStillImageOutput().apply {
        outputSettings = mapOf(AVVideoCodecKey to AVVideoCodecJPEG)
    }
    session.addOutput(output)
    LaunchedEffect(Unit) {
        val backCamera =
            AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo).firstOrNull { device ->
                (device as AVCaptureDevice).position == AVCaptureDevicePositionBack
            } as? AVCaptureDevice ?: return@LaunchedEffect

        val frontCamera =
            AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo).firstOrNull { device ->
                (device as AVCaptureDevice).position == AVCaptureDevicePositionFront
            } as? AVCaptureDevice ?: return@LaunchedEffect

        var currentCamera = backCamera
        var currentInput =
            AVCaptureDeviceInput.deviceInputWithDevice(currentCamera, null) as AVCaptureDeviceInput
        session.addInput(currentInput)
//        session.addOutput(output)
        session.startRunning()

        callback.eventFlow.collect {
            when (it) {
                CameraEvent.CaptureImage -> {
                    val connection = output.connectionWithMediaType(AVMediaTypeVideo)
                    if (connection != null) {
                        output.captureStillImageAsynchronouslyFromConnection(connection) { sampleBuffer, error ->
                            if (sampleBuffer != null && error == null) {
                                val imageData = AVCaptureStillImageOutput.jpegStillImageNSDataRepresentation(sampleBuffer)
                                if (imageData != null) {
                                    val image = UIImage(data = imageData)
                                    val fileName = "IMG_${currentDateTimeFilename()}"
                                    val filePath = NSTemporaryDirectory() + fileName + ".jpg"
                                    imageData.writeToFile(filePath, true)

                                    PHPhotoLibrary.requestAuthorization { status ->
                                        val library = PHPhotoLibrary.sharedPhotoLibrary()
                                        library.performChanges(
                                            changeBlock = {
                                                var life4Album: PHAssetCollection? = null

                                                // Find existing "LIFE4" album
                                                PHAssetCollection.fetchAssetCollectionsWithType(
                                                    PHAssetCollectionTypeAlbum,
                                                    PHAssetCollectionSubtypeAny,
                                                    null
                                                ).let {  fetchResult ->
                                                    for (i in 0 until fetchResult.count.toInt()) {
                                                        val album = fetchResult.objectAtIndex(i.toULong()) as PHAssetCollection
                                                        if (album.localizedTitle == "LIFE4") {
                                                            life4Album = album
                                                            return@let
                                                        }
                                                    }
                                                }

                                                val assetPlaceholder = PHAssetChangeRequest.creationRequestForAssetFromImage(image).placeholderForCreatedAsset

                                                if (life4Album != null) {
                                                    val albumRequest = PHAssetCollectionChangeRequest.changeRequestForAssetCollection(life4Album)
                                                    if (assetPlaceholder != null) {
                                                        albumRequest?.addAssets(
                                                            NSMutableArray().apply {
                                                                addObject(assetPlaceholder)
                                                            }
                                                        )
                                                    }
                                                } else {
                                                    PHAssetCollectionChangeRequest.creationRequestForAssetCollectionWithTitle("LIFE4")
                                                }
                                            },
                                            completionHandler = { success, err ->
                                                if (success) {
                                                    callback.onCaptureImage(Path(filePath))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                CameraEvent.SwitchCamera -> {
                    session.stopRunning()
                    session.removeInput(currentInput)
                    currentCamera = if (currentCamera == backCamera) frontCamera else backCamera
                    currentInput = AVCaptureDeviceInput.deviceInputWithDevice(
                        currentCamera,
                        null
                    ) as AVCaptureDeviceInput
                    session.addInput(currentInput)
                    session.startRunning()
                }
            }
        }
    }

    val cameraPreviewLayer = AVCaptureVideoPreviewLayer(session = session)
    UIKitView(
        factory = {
            val container = object : UIView(frame = CGRectZero.readValue()) {
                override fun layoutSubviews() {
                    CATransaction.begin()
                    CATransaction.setValue(true, kCATransactionDisableActions)
                    cameraPreviewLayer.setFrame(frame)
                    CATransaction.commit()
                }
            }
            container.layer.addSublayer(cameraPreviewLayer)
            cameraPreviewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            container
        },
        modifier = Modifier.fillMaxSize(),
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        ),
        update = {},
        onRelease = {}
    )
}
