package dev.bogibek.frames

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dev.bogibek.frames.utils.URIPathHelper


class MainActivity : AppCompatActivity() {
    private val PERMISSION_REQUESTS_CODE = 123
    private lateinit var videoView: VideoView
    private val DURATION_LIMIT = 10 //10second
    private var list: ArrayList<Frame> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }
        initViews()
    }

    private fun initViews() {
        videoView = findViewById(R.id.video_view)
        setController()
    }

    private fun setController() {
        val mediaController = MediaController(this)
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val dataUri = data?.data
                if (dataUri != null) {
                    val uriPathHelper = URIPathHelper()
                    val videoInputPath = uriPathHelper.getPath(this, dataUri).toString()
                    startVideo(dataUri)
                    getFrame(videoInputPath)
                } else {
                    Toast.makeText(this, "Video input error!", Toast.LENGTH_LONG).show()
                }
            }
        }

    private fun startVideo(dataUri: Uri) {
        videoView.setVideoURI(dataUri)
        setDimension()//this is optional
        videoView.start()
    }

    private fun setDimension() {
        // Adjust the size of the video
        // so it fits on the screen
        val videoProportion = getVideoProportion()
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val screenProportion = screenHeight.toFloat() / screenWidth.toFloat()
        val lp = videoView.layoutParams
        if (videoProportion < screenProportion) {
            lp.height = screenHeight
            lp.width = (screenHeight.toFloat() / videoProportion).toInt()
        } else {
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() * videoProportion).toInt()
        }
        videoView.layoutParams = lp
    }

    // This method gets the proportion of the video that you want to display.
    // I already know this ratio since my video is hardcoded, you can get the
    // height and width of your video and appropriately generate  the proportion
    //    as :height/width
    private fun getVideoProportion(): Float {
        return 1.5f
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
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).also {
            it.putExtra(MediaStore.EXTRA_DURATION_LIMIT, DURATION_LIMIT)
        }

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