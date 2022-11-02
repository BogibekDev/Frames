package dev.bogibek.frames

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Frame(val time: String?, val imgBitmap: Bitmap?) : Parcelable