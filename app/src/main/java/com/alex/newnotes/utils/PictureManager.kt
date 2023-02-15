package com.alex.newnotes.utils

import android.Manifest
import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Environment.DIRECTORY_PICTURES
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.alex.newnotes.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PictureManager(
    activityResultRegistry: ActivityResultRegistry,
    @ApplicationContext private val application: Application,
    private val callback: (pictureUri: Uri?) -> Unit
) {
    private lateinit var photoUri: Uri

    fun pickPhoto() {
        getContentLauncher.launch("image/*")
    }

    private val getContentLauncher = activityResultRegistry.register(
        REGISTRY_KEY_GET_CONTENT,
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = getTmpFileUri()
            saveCopyImage(application, uri, photoUri)
            callback.invoke(photoUri)
        }
    }

    fun takePhoto() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private val requestPermissionLauncher = activityResultRegistry.register(
        REGISTRY_KEY_PERMISSION,
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            photoUri = getTmpFileUri()
            takePhotoLauncher.launch(photoUri)
        }
    }

    private val takePhotoLauncher = activityResultRegistry.register(
        REGISTRY_KEY_TAKE_PHOTO,
        ActivityResultContracts.TakePicture()
    ) { result ->
        if (result && this::photoUri.isInitialized) callback.invoke(photoUri)
    }

    private fun getTmpFileUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = application.getExternalFilesDir(DIRECTORY_PICTURES)
        val tmpFile = File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
        return FileProvider.getUriForFile(
            application,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            tmpFile
        )
    }

    private fun saveCopyImage(context: Context, pathFrom: Uri, pathTo: Uri?) {
        context.contentResolver.openInputStream(pathFrom).use { inputStream: InputStream? ->
            if (pathTo == null || inputStream == null) return
            context.contentResolver.openOutputStream(pathTo).use { out ->
                if (out == null) return
                val buf = ByteArray(1024)
                var len: Int
                while (inputStream.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
    }

    private companion object {
        private const val REGISTRY_KEY_GET_CONTENT = "PictureWorkContent"
        private const val REGISTRY_KEY_TAKE_PHOTO = "PictureWorkPhoto"
        private const val REGISTRY_KEY_PERMISSION = "PictureWorkPermission"
    }
}


