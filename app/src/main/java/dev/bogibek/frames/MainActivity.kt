package dev.bogibek.frames

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.bogibek.frames.utils.URIPathHelper


class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUESTS_CODE = 123
    private var list: ArrayList<Frame> = ArrayList()

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
                    getFrame(videoInputPath)
                } else {
                    Toast.makeText(this, "Video input error!", Toast.LENGTH_LONG).show()
                }
            }
        }


    fun recordVideo(view: View) {
        list = ArrayList()
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        } else {
            startRecord()
        }
    }

    private fun startRecord() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        resultLauncher.launch(intent)
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
        if (ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun getFrame(absolutPath: String) {
        var retriever = MediaMetadataRetriever()
        retriever.setDataSource(absolutPath)
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val frameAmount = time!!.toInt() / 1000

        Toast.makeText(this, "$frameAmount", Toast.LENGTH_SHORT).show()



        for (i in 1..frameAmount) {
            try {
                retriever = MediaMetadataRetriever()
                retriever.setDataSource(absolutPath)
                val imgBitmap = retriever.getFrameAtTime(
                    i * 1000000L, MediaMetadataRetriever.OPTION_CLOSEST
                )
                list.add(Frame("second: $i", imgBitmap))
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            } catch (ex: RuntimeException) {
                ex.printStackTrace()
            } finally {
                try {
                    retriever.release()
                } catch (_: RuntimeException) {
                }
            }
        }

        findViewById<RecyclerView>(R.id.rvFrames).adapter = RVFramesAdapter(list)
    }

    private fun getRequiredPermissions(): Array<String?> {
        return try {
            val info =
                this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
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

}