package com.example.exomine.util

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class PermissionUtil {

    companion object {
        val READ_WRITE_REQUIRED_PERMISSIONS = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
        val NOTIFICATION_REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf()
            }


        fun isPermissionsGranted(context: Context, permissions: Map<String, Boolean>): Boolean {
            return permissions.toList().none { !isPermissionGranted(context, it.first) }
        }


        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            val selfPermission = ContextCompat.checkSelfPermission(context, permission)
            return selfPermission == PackageManager.PERMISSION_GRANTED
        }


        private fun isPermissionGranted(context: Context, permissionList: Array<String>): Boolean {
            return permissionList.none { !isPermissionGranted(context, it) }
        }

        fun checkPermissions(
            context: Context,
            launcher: ActivityResultLauncher<Array<String>>,
            permissionList: Array<String>
        ): Boolean {
            if (!isPermissionGranted(context, permissionList)) {
                launcher.launch(permissionList)
                return false
            }
            return true
        }

    }



}
fun getPendingIntentFlag(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_MUTABLE
    } else {
        PendingIntent.FLAG_UPDATE_CURRENT
    }
}



