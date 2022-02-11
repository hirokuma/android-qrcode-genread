package com.hiro99ma.qrgenread

import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions


//import com.google.zxing.integration

class MainActivity : AppCompatActivity() {
    class MyCaptureActivity : CaptureActivity()
    private var screenWidth: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.width()
        } else {
            val realSize = Point()
            wm.defaultDisplay.getRealSize(realSize)
            realSize.x
        }
    }

    // Register the launcher and result handler
    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents != null) {
            // https://developer.android.com/guide/topics/text/copy-paste#Copying
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("qrcode", result.contents)
            clipboard.setPrimaryClip(clip)

            val textView = findViewById<TextView>(R.id.textView)
            textView.text = result.contents
            val imageView = findViewById<ImageView>(R.id.imageView) as ImageView
            imageView.setImageResource(android.R.drawable.ic_menu_camera)

            Toast.makeText(this, "copy text to clipboard!", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "canceled!", Toast.LENGTH_LONG).show()
        }
    }

    fun onClickStartCamera(@Suppress("UNUSED_PARAMETER") view: View) {
        // https://qiita.com/tktktks10/items/3b327b2900d38e672996
        val options = ScanOptions().apply {
            captureActivity = MyCaptureActivity::class.java
        }
        barcodeLauncher.launch(options)
    }

    fun onClickReadClipboard(@Suppress("UNUSED_PARAMETER") view: View) {
        // https://developer.android.com/guide/topics/text/copy-paste#Pasting
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription!!.hasMimeType(
                MIMETYPE_TEXT_PLAIN
            )
        ) {
            val text = clipboard.primaryClip!!.getItemAt(0).text

            val textView = findViewById<TextView>(R.id.textView)
            textView.text = text

            // https://stackoverflow.com/questions/19337448/generate-qr-code-directly-into-imageview
            val matrix = MultiFormatWriter().encode(
                text.toString(),
                BarcodeFormat.QR_CODE, screenWidth, screenWidth
            )
            val height: Int = matrix.height
            val width: Int = matrix.width
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            val imageView = findViewById<ImageView>(R.id.imageView) as ImageView
            imageView.setImageBitmap(bmp)
        }
    }
}