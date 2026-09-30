package com.example.example_android.data

import com.idosmart.protocol_channel.sdk

/**
 * File transfer type enum, mirrors iOS TransferFileVC.swift and its TransType
 * Centrally manages the allowed file extensions and hint messages for each transfer type
 */
enum class TransType {
    MP3,
    WALLPAPER,
    CONTACT,
    WATCH_FACE,
    OTA,
    APP,
    GPS,
    GPS_FW;

    fun title(): String = when (this) {
        MP3 -> "Transfer Music Upload music"
        WALLPAPER -> "Wallpaper watch face"
        CONTACT -> "Upload contacts"
        WATCH_FACE -> "Watch face upgrade"
        OTA -> "Firmware upgrade"
        APP -> "App"
        GPS -> "GPS"
        GPS_FW -> "GPS Firmware"
    }

    /** List of allowed file extensions */
    fun allowedExtensions(): List<String> = when (this) {
        MP3 -> listOf("mp3")
        WALLPAPER -> listOf("png", "jpg", "jpeg")
        CONTACT -> listOf("json")
        WATCH_FACE -> emptyList()
        OTA -> when (sdk.device.platform) {
            98, 99 -> listOf("zip")
            else -> listOf("zip", "bin", "fw")
        }
        APP -> listOf("app")
        GPS -> listOf("dat")
        GPS_FW -> listOf("gps")
    }

    /** File selection hint */
    fun fileHint(): String {
        val exts = allowedExtensions().joinToString(" / ") { ".$it" }
        return when (this) {
            MP3 -> "Select music file ($exts)\nNote: Only 44.1k sample rate"
            WALLPAPER -> "Select wallpaper image ($exts)"
            CONTACT -> "Select contact file ($exts)"
            OTA -> "Select firmware file ($exts)"
            APP -> "Select app file ($exts)"
            GPS -> "Select EPO file ($exts)"
            GPS_FW -> "Select GPS firmware ($exts)"
            WATCH_FACE -> ""
        }
    }

    /** OTA prominent warning message */
    fun otaWarning(): String {
        return "Note: use the correct firmware file. A wrong firmware package will fail to install and may even brick the device so it cannot boot!\nWARNING: Use the correct firmware file. An incorrect firmware package may cause installation failure or brick the device!"
    }

    /** Check whether the file extension is valid */
    fun isExtensionValid(fileName: String): Boolean {
        val allowed = allowedExtensions()
        if (allowed.isEmpty()) return true
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return allowed.any { it.equals(ext, ignoreCase = true) }
    }
}
