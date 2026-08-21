package app.tijario.data.remote

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object FeedbackImageEncoder {
    private const val maxDimension = 1440
    private const val maxEncodedBytes = 512 * 1024

    fun encode(contentResolver: ContentResolver, uri: Uri): MobileFeedbackImage? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
        }
        val decoded = contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: return null

        return try {
            compressToLimit(decoded)?.let { bytes ->
                MobileFeedbackImage(
                    mimeType = "image/jpeg",
                    dataBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
                )
            }
        } finally {
            decoded.recycle()
        }
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        var largest = maxOf(width, height)
        while (largest / 2 >= maxDimension) {
            sample *= 2
            largest /= 2
        }
        return sample
    }

    private fun compressToLimit(original: Bitmap): ByteArray? {
        var bitmap = original
        var ownsBitmap = false
        try {
            repeat(4) {
                for (quality in intArrayOf(85, 75, 65, 55)) {
                    val output = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, output)
                    val bytes = output.toByteArray()
                    if (bytes.size <= maxEncodedBytes) return bytes
                }

                val scaled = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * 0.75f).toInt().coerceAtLeast(1),
                    (bitmap.height * 0.75f).toInt().coerceAtLeast(1),
                    true,
                )
                if (ownsBitmap) bitmap.recycle()
                bitmap = scaled
                ownsBitmap = true
            }
            return null
        } finally {
            if (ownsBitmap && !bitmap.isRecycled) bitmap.recycle()
        }
    }
}
