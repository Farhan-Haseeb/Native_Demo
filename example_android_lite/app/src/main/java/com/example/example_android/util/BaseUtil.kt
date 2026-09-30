package com.example.example_android.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

object BaseUtil {

    fun share(context: Context,path:String){
        if (path.isNotEmpty()) {
            // Set the dialog title
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "application/zip"

            val file = File(path)
            val fileUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Android version at or above Nougat (7.0)
                FileProvider.getUriForFile(
                    context,
                    "com.example.example_android.fileprovider",
                    file
                )
            } else {
                // Android version below Nougat (7.0)
                Uri.fromFile(file)
            }
            intent.putExtra(Intent.EXTRA_STREAM, fileUri)
            intent.putExtra(Intent.EXTRA_SUBJECT, "Share log output tool")
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(Intent.createChooser(intent, "Share log output tool"))
        }
    }
}