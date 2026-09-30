package com.example.example_android.data

import android.graphics.drawable.Drawable
import java.io.Serializable
/**
 * Custom app entity data class
 * evtType: event type, the unique app identifier; each app has its own event type
 * pkgName: package name
 * appName: app name
 * icon: app icon resource data
 * status: app notification status
 * */
data class TranIconBean(
    var evtType: Int? =null,
    var pkgName: String?=null,
    var appName: String?=null,
    var icon: Drawable?=null,
    var status: Int?=null,
    var iconState: Int?=null,
    var isUpdateAppIcon: Boolean?=null
):Serializable{

}
