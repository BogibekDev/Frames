package dev.bogibek.frames

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dev.bogibek.frames.utils.URIPathHelper
import java.io.File

class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUESTS_CODE = 123
    private val VIDEO_RECORD_CODE = 125
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }

    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val dataUri = data?.data
                if (dataUri != null) {
                    val uriPathHelper = URIPathHelper()
                    val videoInputPath = uriPathHelper.getPath(this, dataUri).toString()
                    val videoInputFile = File(videoInputPath).absolutePath

                    Log.d("@@@@", "dataUri : $dataUri")
                    Log.d("@@@@", "videoInputPath : $videoInputPath")
                    Log.d("@@@@", "absolute path : $videoInputFile")
//                    TODO Code Here

                } else {
                    Toast.makeText(this, "Video input error!", Toast.LENGTH_LONG).show()
                }
            }
        }


    fun recordVideo(view: View) {
        startRecord()
    }

    private fun startRecord() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        resultLauncher.launch(intent)
    }

    private fun hasCamera() = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info = this.packageManager
                .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
            val ps = info.requestedPermissions
            if (ps != null && ps.isNotEmpty()) {
                ps
            } else {
                arrayOfNulls(0)
            }
        } catch (e: Exception) {
            arrayOfNulls(0)
        }
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    return false
                }
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in getRequiredPermissions()) {
            permission?.let {
                if (!isPermissionGranted(this, it)) {
                    allNeededPermissions.add(permission)
                }
            }
        }

        if (allNeededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS_CODE
            )
        }
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        if (ContextCompat.checkSelfPermission(context, permission)
            == PackageManager.PERMISSION_GRANTED
        ) {
            Log.i("@@@", "Permission granted: $permission")
            return true
        }
        Log.i("@@@", "Permission NOT granted: $permission")
        return false
    }


}