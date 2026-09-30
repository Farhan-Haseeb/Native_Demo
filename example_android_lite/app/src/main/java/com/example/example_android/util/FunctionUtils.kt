package com.example.example_android.util

import android.bluetooth.BluetoothAdapter
import com.clj.fastble.data.BleDevice
import com.example.example_android.appContext
import org.json.JSONArray
import org.json.JSONObject

/** Snapshot of the bound device shown on the home page. On tap, restore the [BleDevice] via system Bluetooth. */
data class BoundDeviceInfo(val mac: String, val name: String) {
    fun toBleDevice(): BleDevice? {
        if (mac.isEmpty()) {
            return null
        }
        return try {
            val remote = BluetoothAdapter.getDefaultAdapter()?.getRemoteDevice(mac) ?: return null
            BleDevice(remote)
        } catch (_: Exception) {
            null
        }
    }
}

object FunctionUtils {
    private const val BIND_KEY = "bindkey"
    private const val BOUND_DEVICES_KEY = "bound_devices"
    /** Matches the [SPUtil] default file `idodemo`; the actual file name is `sp_idodemo` */
    private const val SP_FILE = "sp_idodemo"

    /**
     * Get the device bind status
     * */
    fun isBind(macAddress: String): Boolean {
        return SPUtil.getAValue(BIND_KEY + macAddress, false) as Boolean
    }

    /**
     * Save the device bind status
     * */
    fun saveBindState(macAddress: String) {
        SPUtil.putAValue(BIND_KEY + macAddress, true)
        upsertBoundDeviceIfAbsent(macAddress)
    }

    /**
     * Save a snapshot of the bound device (mac + name) for the home page list
     */
    fun saveBoundDevice(device: BleDevice) {
        val mac = device.mac ?: return
        if (mac.isEmpty()) {
            return
        }
        val name = device.name?.takeIf { it.isNotEmpty() } ?: mac
        SPUtil.putAValue(BIND_KEY + mac, true)
        upsertBoundDevice(BoundDeviceInfo(mac, name))
    }

    /**
     * Change the device bind status
     * */
    fun removeBindState(macAddress: String) {
        SPUtil.putAValue(BIND_KEY + macAddress, false)
        removeBoundDevice(macAddress)
    }

    fun loadBoundDevices(): MutableList<BoundDeviceInfo> {
        migrateBoundFlagsIfNeeded()
        return loadBoundDeviceList()
    }

    private fun upsertBoundDevice(info: BoundDeviceInfo) {
        val list = loadBoundDeviceList()
        list.removeAll { it.mac == info.mac }
        list.add(0, info)
        persistBoundDevices(list)
    }

    private fun upsertBoundDeviceIfAbsent(macAddress: String) {
        if (macAddress.isEmpty()) {
            return
        }
        val list = loadBoundDeviceList()
        if (list.any { it.mac == macAddress }) {
            return
        }
        list.add(0, BoundDeviceInfo(macAddress, macAddress))
        persistBoundDevices(list)
    }

    private fun removeBoundDevice(macAddress: String) {
        val list = loadBoundDeviceList()
        list.removeAll { it.mac == macAddress }
        persistBoundDevices(list)
    }

    private fun loadBoundDeviceList(): MutableList<BoundDeviceInfo> {
        val json = SPUtil.getAValue(BOUND_DEVICES_KEY, "") as? String ?: ""
        if (json.isEmpty()) {
            return mutableListOf()
        }
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<BoundDeviceInfo>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val mac = obj.optString("mac")
                if (mac.isEmpty()) {
                    continue
                }
                val name = obj.optString("name").ifEmpty { mac }
                list.add(BoundDeviceInfo(mac, name))
            }
            list
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    private fun persistBoundDevices(list: List<BoundDeviceInfo>) {
        val array = JSONArray()
        list.forEach {
            array.put(JSONObject().apply {
                put("mac", it.mac)
                put("name", it.name)
            })
        }
        SPUtil.putAValue(BOUND_DEVICES_KEY, array.toString())
    }

    /**
     * Add devices that only have a bind flag but no snapshot yet into the list
     */
    private fun migrateBoundFlagsIfNeeded() {
        val existing = loadBoundDeviceList()
        val existingMacs = existing.map { it.mac }.toSet()
        val prefs = appContext.getSharedPreferences(SP_FILE, android.content.Context.MODE_PRIVATE)
        var changed = false
        for ((key, value) in prefs.all) {
            if (key == BOUND_DEVICES_KEY || !key.startsWith(BIND_KEY) || value != true) {
                continue
            }
            val mac = key.removePrefix(BIND_KEY)
            if (mac.isNotEmpty() && !existingMacs.contains(mac)) {
                existing.add(BoundDeviceInfo(mac, mac))
                changed = true
            }
        }
        if (changed) {
            persistBoundDevices(existing)
        }
    }
}
