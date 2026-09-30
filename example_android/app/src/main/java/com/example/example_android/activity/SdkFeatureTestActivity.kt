package com.example.example_android.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.example_android.R
import com.example.example_android.base.BaseActivity
import com.idosmart.enums.IDOSyncDataType
import com.idosmart.model.IDOBleExecType
import com.idosmart.model.IDOBleReplyType
import com.idosmart.model.IDOAppDownloadStatusInfoModel
import com.idosmart.model.IDOAppInfoModel
import com.idosmart.model.IDOAppListStyleParamModel
import com.idosmart.model.IDOAppSleepModeParamModel
import com.idosmart.model.IDOAppletControlModel
import com.idosmart.model.IDOAppletListQueryType
import com.idosmart.model.IDODataTranConfigParamModel
import com.idosmart.model.IDOFallMonitoringSwitchModel
import com.idosmart.model.IDOFirmwarePositionInfoModel
import com.idosmart.model.IDOBaseModel
import com.idosmart.model.IDOHeartModeParamModel
import com.idosmart.model.IDOHeartRateModeParamModel
import com.idosmart.model.IDOLocationInfoNotifyModel
import com.idosmart.model.IDOPurchasedWatchFaceInfoModel
import com.idosmart.model.IDOPositionSwitchModeModel
import com.idosmart.model.IDOTakeMedicineRemindModel
import com.idosmart.model.IDOTakeMedicineRemindInfoItemModel
import com.idosmart.model.IDOTakeMedicineRemindTimeItemModel
import com.idosmart.pigeon_implement.IDOTransMedicModel
import com.idosmart.model.IDODeviceVibrationRingtoneModel
import com.idosmart.model.IDOPhoneCalendarSyncDeleteModel
import com.idosmart.model.IDOPhoneCalendarSyncSetModel
import com.idosmart.model.IDOHeartRateModeSmartModel
import com.idosmart.model.IDOHeartRateModeSmartParamModel
import com.idosmart.model.IDOSpo2SwitchModel
import com.idosmart.model.IDOSpo2SwitchParamModel
import com.idosmart.model.IDOStressSwitchModel
import com.idosmart.model.IDOStressSwitchParamModel
import com.idosmart.pigeon_implement.Cmds
import com.idosmart.protocol_channel.sdk
import com.idosmart.protocol_sdk.IDOExchangeDataDelegate
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlinx.android.synthetic.main.activity_sdk_feature_test.ll_buttons
import kotlinx.android.synthetic.main.activity_sdk_feature_test.sv_log
import kotlinx.android.synthetic.main.activity_sdk_feature_test.tv_log

/**
 * Integration test of the standardized SDK features already delivered (aligned with Flutter SdkFeatureTestPage / iOS SdkFeatureTestVC)
 */
class SdkFeatureTestActivity : BaseActivity() {

    private val logBuffer = StringBuilder()
    private var supportSyncTypesText = ""

    /** After picking an image, crop it to 160×160 PNG, then transfer it via `.medic` (aligned with iOS SdkFeatureTestVC) */
    private val pickMedicIconLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) {
                log("Medication reminder icon selection cancelled")
                return@registerForActivityResult
            }
            val imgPath = makeMedicIconPngFile(uri)
            if (imgPath == null) {
                toast("Failed to crop to 160×160")
                return@registerForActivityResult
            }
            log("  Cropped to 160×160 PNG: $imgPath")
            startMedicIconTransfer(imgPath)
        }

    private val exchangeDelegate = object : IDOExchangeDataDelegate {
        override fun appListenBleExec(type: IDOBleExecType) {
            // This page only exercises app-initiated requests
        }

        override fun appListenAppExec(type: IDOBleReplyType) {
            when (type) {
                is IDOBleReplyType.appActivityDataReply -> {
                    val model = type.model
                    log(
                        "[exchangeData] 15.20 Sport summary step=${model?.step} distance=${model?.distance}"
                    )
                    log(
                        "  bodyAge=${model?.bodyAge} swimmingPoolDistance=${model?.swimmingPoolDistance} actType=${model?.actType} gpsStatus=${model?.gpsStatus}"
                    )
                }
                else -> log("[exchangeData] ${type.javaClass.simpleName}")
            }
        }

        override fun exchangeV2Data(model: com.idosmart.model.IDOExchangeV2Model) {
            log("[exchangeV2Data] $model")
        }

        override fun exchangeV3Data(model: com.idosmart.model.IDOExchangeV3Model) {
            log("[exchangeV3Data] $model")
        }
    }

    override fun getLayoutId(): Int = R.layout.activity_sdk_feature_test

    override fun initView() {
        super.initView()
        supportActionBar?.title = getString(R.string.sdk_feature_test)
        sdk.dataExchange.addExchange(exchangeDelegate)
        setupButtons()
        refreshFuncTableSummary()
    }

    override fun onDestroy() {
        sdk.dataExchange.addExchange(null)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sdk_feature_test, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_refresh -> {
                clearLog()
                refreshFuncTableSummary()
                true
            }
            R.id.menu_clear -> {
                clearLog()
                renderLog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupButtons() {
        appendSection(
            "Basic get Cmd",
            listOf(
                "2.61 Device status" to { getDeviceStatus() },
                "15.104 Wrist-side sports" to { getSportTypesWristSide() },
                "15.106 Vibration & ringtone (query)" to { getVibrationRingtone() },
                "Phone calendar sync switch" to { getPhoneCalendarSyncSwitch() },
                "Phone calendar pending-delete list" to { getPhoneCalendarDeleteList() },
            )
        )
        appendSection(
            "V2 switch GET/SET",
            listOf(
                "2.47 SpO2 GET" to { getSpo2Switch() },
                "2.47 SpO2 SET" to { setSpo2SwitchSample() },
                "2.47 SpO2 write-back" to { roundtripSpo2() },
                "2.46 Stress GET" to { getStressSwitch() },
                "2.46 Stress SET" to { setStressSwitchSample() },
                "2.46 Stress write-back" to { roundtripStress() },
                "2.45 Smart heart rate GET" to { getSmartHrSwitch() },
                "2.45 Smart heart rate SET" to { setSmartHrSwitchSample() },
                "2.45 Smart heart rate write-back" to { roundtripSmartHr() },
            )
        )
        appendSection(
            "V3 set Cmd",
            listOf(
                "15.106 Vibration & ringtone (set)" to { setVibrationRingtone() },
                "15.106 Vibration & ringtone write-back" to { roundtripVibrationRingtone() },
                "Phone calendar sync (set)" to { setPhoneCalendarSync() },
                "Phone calendar incremental delete (example)" to { setPhoneCalendarSyncDeleteSample() },
            )
        )
        appendSection(
            "Data exchange exchangeData",
            listOf(
                "15.20 Sport summary" to { getActivitySummaryExchange() },
                "15.103 Full snapshot" to { getFullSnapshotExchange() },
            )
        )
        appendSection(
            "Health sync syncData",
            listOf(
                "15.4.1 SpO2" to { syncSpo2() },
                "15.4.2 Stress" to { syncPressure() },
                "15.4.23 Activity amount" to { syncActivityAmount() },
            )
        )
        appendSection(
            "2026-07-06 New GET",
            listOf(
                "2.53 Firmware status" to { getFirmwareStatusInfo() },
                "2.39 Heart rate monitoring mode" to { getHeartRateMode() },
                "2.63 Sleep mode GET" to { getAppSleepMode() },
                "2.54 Fall monitoring GET" to { getFallMonitoringSwitch() },
                "2.56 Positioning switch GET" to { getPositionSwitchMode() },
                "2.28 File transfer config" to { getDataTranConfig() },
                "15.26 Watch face list V3" to { getWatchListV3() },
            )
        )
        appendSection(
            "2026-07-06 New SET/query",
            listOf(
                "2.54 Fall monitoring SET" to { setFallMonitoringSwitchSample() },
                "2.56 Positioning switch SET" to { setPositionSwitchModeSample() },
                "5.6 Location notification" to { setLocationInfoNotify() },
                "2.39 Heart rate monitoring SET(example)" to { setHeartRateModeSample() },
                "2.63 Sleep mode SET(example)" to { setAppSleepModeSample() },
                "15.9 V3 heart rate mode (example)" to { setHeartModeSample() },
                "15.54 Applet list" to { getAppletList() },
                "15.54 Delete first applet" to { deleteAppletFirst() },
                "15.79 APP basic info" to { setAppBaseInfoSample() },
                "15.90 Medication reminder set" to { takeMedicineRemindSet() },
                "15.90 Medication reminder query" to { takeMedicineRemindQuery() },
                "15.90 Medication reminder delete first" to { takeMedicineRemindDeleteFirst() },
                "15.90 Medication reminder icon" to { takeMedicineRemindIconTransfer() },
                "15.90 Medication reminder set master switch" to { takeMedicineRemindSetSwitch() },
                "15.91 Purchased watch faces" to { setPurchasedWatchFaceInfoSample() },
                "15.92 Download status" to { setAppDownloadStatusInfoSample() },
                "15.93 Firmware position query" to { getFirmwarePositionInfoQuery() },
                "15.93 Firmware position confirm" to { getFirmwarePositionInfoConfirm() },
                "15.73 App list style query" to { appListStyleQuery() },
                "15.73 App list style delete" to { appListStyleDeleteFirst() },
            )
        )
    }

    private fun appendSection(title: String, buttons: List<Pair<String, () -> Unit>>) {
        val titleView = TextView(this).apply {
            text = title
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF757575.toInt())
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = dp(8)
            layoutParams = lp
        }
        ll_buttons.addView(titleView)

        buttons.forEach { (label, action) ->
            val btn = TextView(this).apply {
                text = label
                gravity = Gravity.CENTER
                textSize = 14f
                setTextColor(resources.getColor(android.R.color.white, theme))
                setBackgroundResource(R.drawable.bg_btn_send)
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(40)
                )
                lp.topMargin = dp(8)
                layoutParams = lp
                setOnClickListener { action() }
            }
            ll_buttons.addView(btn)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun clearLog() {
        logBuffer.clear()
    }

    private fun log(msg: String) {
        logBuffer.append(msg).append('\n')
        android.util.Log.d("SdkFeatureTest", msg)
        renderLog()
    }

    private fun renderLog() {
        tv_log.text = if (logBuffer.isEmpty()) "(no log)" else logBuffer.toString()
        sv_log.post { sv_log.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun refreshFuncTableSummary() {
        val ft = sdk.funcTable
        sdk.syncData.getSupportSyncDataTypeList { list ->
            runOnUiThread {
                supportSyncTypesText = list.joinToString { it.name }
                val summary = """
                    [Connection] ${if (sdk.state.isConnected) "Connected" else "Disconnected"}
                    [V2 switch] spo2Get=${ft.getSupportGetSpo2SwitchInfo} pressureGet=${ft.getSupportGetPressureSwitchInfo} smartHrGet=${ft.getSupportGetSmartHeartRate}
                    [Health sync] spo2=${ft.syncV3Spo2} pressure=${ft.syncV3Pressure} activityAmount=${ft.syncV3ActivityAmount}
                      SpO2 version compatible=${ft.getSupportSyncSpo2UseVersionCompatible}
                      Stress version compatible=${ft.getSupportSyncPressureUseVersionCompatible}
                    [Data exchange] syncV3ActivityExchangeData=${ft.syncV3ActivityExchangeData}
                      supportV3ActivityExchange=${sdk.dataExchange.supportV3ActivityExchange}
                    Supported sync types: $supportSyncTypesText
                """.trimIndent()
                log(summary)
            }
        }
    }

    private fun ensureConnected(): Boolean {
        if (sdk.state.isConnected) return true
        toast("No device connected")
        log("Error: device not connected")
        return false
    }

    private fun preview(text: String?, limit: Int = 800): String {
        if (text.isNullOrEmpty()) return "NULL"
        return if (text.length <= limit) text else text.substring(0, limit) + "..."
    }

    private fun getDeviceStatus() {
        if (!ensureConnected()) return
        log("--- 2.61 Device status (getDeviceStatusInfo) ---")
        log("  Request: {\"get_type\":1}")
        showProgressDialog("2.61 Device status")
        Cmds.getDeviceStatusInfo(1).send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0")
                log("  json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun getSportTypesWristSide() {
        if (!ensureConnected()) return
        log("--- 15.104 Wrist-side sport types (getSportTypesRequiringWristSideSetting) ---")
        log("  Request: {}")
        showProgressDialog("15.104 Wrist-side sports")
        Cmds.getSportTypesRequiringWristSideSetting().send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0")
                log("  json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun getVibrationRingtone() {
        if (!ensureConnected()) return
        log("--- 15.106 Vibration & ringtone (query) (deviceVibrationRingtone) ---")
        log("  Request: {\"operate\":1}")
        showProgressDialog("15.106 Vibration & ringtone")
        Cmds.deviceVibrationRingtone(IDODeviceVibrationRingtoneModel(operate = 1)).send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0")
                log("  json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun getPhoneCalendarSyncSwitch() {
        if (!ensureConnected()) return
        log("--- Phone calendar sync switch (getPhoneCalendarSyncSwitch) ---")
        showProgressDialog("Phone calendar sync switch")
        Cmds.getPhoneCalendarSyncSwitch().send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0")
                log("  json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun setPhoneCalendarSync() {
        if (!ensureConnected()) return
        val model = IDOPhoneCalendarSyncSetModel(syncOnOff = 1, calendarPermissionStatus = 1)
        log("--- Phone calendar sync (set) (setPhoneCalendarSync) ---")
        showProgressDialog("Phone calendar sync (set)")
        Cmds.setPhoneCalendarSync(model).send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun getPhoneCalendarDeleteList() {
        if (!ensureConnected()) return
        log("--- Phone calendar pending-delete list (getPhoneCalendarDeleteList) ---")
        showProgressDialog("Phone calendar pending-delete list")
        Cmds.getPhoneCalendarDeleteList().send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun setPhoneCalendarSyncDeleteSample() {
        if (!ensureConnected()) return
        val model = IDOPhoneCalendarSyncDeleteModel(items = emptyList(), itemCount = 0)
        log("--- Phone calendar incremental delete (example) (setPhoneCalendarSyncDelete) ---")
        showProgressDialog("Phone calendar incremental delete")
        Cmds.setPhoneCalendarSyncDelete(model).send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun setVibrationRingtone() {
        if (!ensureConnected()) return
        val model = IDODeviceVibrationRingtoneModel(
            operate = 2,
            type = 3,
            vibrationIntensity = 2,
            alarmVolume = 5,
        )
        log("--- 15.106 Vibration & ringtone (set) ---")
        log("  Request: ${model.toJsonString()}")
        showProgressDialog("15.106 Vibration & ringtone (set)")
        Cmds.deviceVibrationRingtone(model).send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun roundtripVibrationRingtone() {
        if (!ensureConnected()) return
        log("--- 15.106 Vibration & ringtone GET→SET write-back ---")
        showProgressDialog("Vibration & ringtone write-back...")
        Cmds.deviceVibrationRingtone(IDODeviceVibrationRingtoneModel(operate = 1)).send { getRes ->
            if (getRes.error.code != 0 || getRes.res == null) {
                closeProgressDialog()
                log("  GET code=${getRes.error.code}, aborting")
                toast("GET failed")
                return@send
            }
            val getModel = getRes.res!!
            log("  GET: ${preview(getModel.toJsonString())}")
            val type = getModel.type ?: 0
            if (type == 0) {
                closeProgressDialog()
                log("  type=0, skipping SET")
                toast("No settable sub-items")
                return@send
            }
            val setModel = IDODeviceVibrationRingtoneModel(
                operate = 2,
                type = type,
                vibrationIntensity = if (type and 0x01 != 0) getModel.vibrationIntensity else null,
                alarmVolume = if (type and 0x02 != 0) getModel.alarmVolume else null,
                callReminderVolume = if (type and 0x04 != 0) getModel.callReminderVolume else null,
            )
            log("  SET request: ${setModel.toJsonString()}")
            Cmds.deviceVibrationRingtone(setModel).send { setRes ->
                closeProgressDialog()
                if (setRes.error.code == 0) {
                    log("  SET code=0 json: ${preview(setRes.res?.toJsonString())}")
                    toast("Write-back succeeded")
                } else {
                    log("  SET code=${setRes.error.code} msg=${setRes.error.message}")
                    toast("SET failed")
                }
            }
        }
    }

    private fun switchValue(v: Int): Int = if (v == 1) 1 else 0

    private fun spo2ModelToParam(model: IDOSpo2SwitchModel) = IDOSpo2SwitchParamModel(
        switchValue(model.onOff),
        model.startHour,
        model.startMinute,
        model.endHour,
        model.endMinute,
        model.lowSpo2OnOff?.let { switchValue(it) },
        model.lowSpo2Value,
        model.notifyFlag,
        model.measurementInterval,
    )

    private fun stressModelToParam(model: IDOStressSwitchModel) = IDOStressSwitchParamModel(
        switchValue(model.onOff),
        model.startHour,
        model.startMinute,
        model.endHour,
        model.endMinute,
        switchValue(model.remindOnOff),
        model.interval,
        model.highThreshold,
        model.stressThreshold,
        model.notifyFlag,
        HashSet(model.repeats),
        model.measurementInterval,
    )

    private fun smartHrModelToParam(model: IDOHeartRateModeSmartModel) = IDOHeartRateModeSmartParamModel(
        switchValue(model.mode),
        model.notifyFlag,
        switchValue(model.highHeartMode),
        switchValue(model.lowHeartMode),
        model.highHeartValue,
        model.lowHeartValue,
        model.startHour,
        model.startMinute,
        model.endHour,
        model.endMinute,
        model.measurementInterval,
    )

    private fun getSpo2Switch() {
        if (!ensureConnected()) return
        log("--- 2.47 SpO2 switch (query) (getSpo2Switch) ---")
        showProgressDialog("2.47 SpO2 GET")
        Cmds.getSpo2Switch().send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                it.res?.let { m -> log("  measurement_interval=${m.measurementInterval}") }
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun setSpo2SwitchSample() {
        if (!ensureConnected()) return
        val param = IDOSpo2SwitchParamModel(
            1, 0, 0, 23, 59, 1, 90, 1, 5
        )
        log("--- 2.47 SpO2 switch (set) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("2.47 SpO2 SET")
        Cmds.setSpo2Switch(param).send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun roundtripSpo2() {
        if (!ensureConnected()) return
        log("--- 2.47 SpO2 switch GET→SET write-back ---")
        showProgressDialog("2.47 SpO2 write-back")
        Cmds.getSpo2Switch().send { getRes ->
            if (getRes.error.code != 0 || getRes.res == null) {
                closeProgressDialog()
                log("  GET failed, aborting")
                toast("GET failed")
                return@send
            }
            val getModel = getRes.res!!
            log("  GET: ${preview(getModel.toJsonString())}")
            log("  measurement_interval=${getModel.measurementInterval}")
            val param = spo2ModelToParam(getModel)
            log("  SET request: ${param.toJsonString()}")
            Cmds.setSpo2Switch(param).send { setRes ->
                closeProgressDialog()
                if (setRes.error.code == 0) {
                    log("  SET code=0 json: ${preview(setRes.res?.toJsonString())}")
                    toast("Write-back succeeded")
                } else {
                    log("  SET code=${setRes.error.code} msg=${setRes.error.message}")
                    toast("SET failed")
                }
            }
        }
    }

    private fun getStressSwitch() {
        if (!ensureConnected()) return
        log("--- 2.46 Stress switch (query) (getStressSwitch) ---")
        showProgressDialog("2.46 Stress GET")
        Cmds.getStressSwitch().send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                it.res?.let { m -> log("  measurement_interval=${m.measurementInterval}") }
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun setStressSwitchSample() {
        if (!ensureConnected()) return
        val param = IDOStressSwitchParamModel(
            1, 0, 0, 23, 59, 1, 60, 80, 80, 1, hashSetOf(), 5
        )
        log("--- 2.46 Stress switch (set) ---")
        log("  Request: $param")
        showProgressDialog("2.46 Stress SET")
        Cmds.setStressSwitch(param).send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun roundtripStress() {
        if (!ensureConnected()) return
        log("--- 2.46 Stress switch GET→SET write-back ---")
        showProgressDialog("2.46 Stress write-back")
        Cmds.getStressSwitch().send { getRes ->
            if (getRes.error.code != 0 || getRes.res == null) {
                closeProgressDialog()
                log("  GET failed, aborting")
                toast("GET failed")
                return@send
            }
            val getModel = getRes.res!!
            log("  GET: ${preview(getModel.toJsonString())}")
            log("  measurement_interval=${getModel.measurementInterval}")
            val param = stressModelToParam(getModel)
            log("  SET request: $param")
            Cmds.setStressSwitch(param).send { setRes ->
                closeProgressDialog()
                if (setRes.error.code == 0) {
                    log("  SET code=0 json: ${preview(setRes.res?.toJsonString())}")
                    toast("Write-back succeeded")
                } else {
                    log("  SET code=${setRes.error.code} msg=${setRes.error.message}")
                    toast("SET failed")
                }
            }
        }
    }

    private fun getSmartHrSwitch() {
        if (!ensureConnected()) return
        log("--- 2.45 Smart heart rate (query) (getSmartHeartRateMode) ---")
        showProgressDialog("2.45 Smart heart rate GET")
        Cmds.getSmartHeartRateMode().send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                it.res?.let { m -> log("  measurement_interval=${m.measurementInterval}") }
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun setSmartHrSwitchSample() {
        if (!ensureConnected()) return
        val param = IDOHeartRateModeSmartParamModel(
            1, 1, 1, 1, 100, 60, 0, 0, 23, 59, 300
        )
        log("--- 2.45 Smart heart rate (set) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("2.45 Smart heart rate SET")
        Cmds.setHeartRateModeSmart(param).send {
            closeProgressDialog()
            if (it.error.code == 0) {
                log("  code=0 json: ${preview(it.res?.toJsonString())}")
                toast("Succeeded")
            } else {
                log("  code=${it.error.code} msg=${it.error.message}")
                toast("Failed code=${it.error.code}")
            }
        }
    }

    private fun roundtripSmartHr() {
        if (!ensureConnected()) return
        log("--- 2.45 Smart heart rate GET→SET write-back ---")
        showProgressDialog("2.45 Smart heart rate write-back")
        Cmds.getSmartHeartRateMode().send { getRes ->
            if (getRes.error.code != 0 || getRes.res == null) {
                closeProgressDialog()
                log("  GET failed, aborting")
                toast("GET failed")
                return@send
            }
            val getModel = getRes.res!!
            log("  GET: ${preview(getModel.toJsonString())}")
            log("  measurement_interval=${getModel.measurementInterval}")
            val param = smartHrModelToParam(getModel)
            log("  SET request: ${param.toJsonString()}")
            Cmds.setHeartRateModeSmart(param).send { setRes ->
                closeProgressDialog()
                if (setRes.error.code == 0) {
                    log("  SET code=0 json: ${preview(setRes.res?.toJsonString())}")
                    toast("Write-back succeeded")
                } else {
                    log("  SET code=${setRes.error.code} msg=${setRes.error.message}")
                    toast("SET failed")
                }
            }
        }
    }

    private fun getActivitySummaryExchange() {
        if (!ensureConnected()) return
        if (!sdk.dataExchange.supportV3ActivityExchange) {
            log("Skipped: V3 multi-sport data exchange not supported")
            toast("Data exchange not supported")
            return
        }
        log("--- 15.20 Sport summary exchangeData.getLastActivityData() ---")
        showProgressDialog("Getting sport summary...")
        sdk.dataExchange.getLastActivityData()
        closeProgressDialog()
        log("getLastActivityData triggered (see the exchangeData callback for details)")
        toast("Triggered")
    }

    private fun getFullSnapshotExchange() {
        if (!ensureConnected()) return
        log("--- 15.103 Full snapshot exchangeData.getActivityExchangeFullSnapshot() ---")
        showProgressDialog("Full snapshot...")
        sdk.dataExchange.getActivityExchangeFullSnapshot { json ->
            runOnUiThread {
                closeProgressDialog()
                if (!json.isNullOrEmpty()) {
                    log("json: ${preview(json)}")
                    toast("Succeeded")
                } else {
                    log("Empty result or failed")
                    toast("Failed")
                }
            }
        }
    }

    private fun syncSpo2() {
        if (!ensureConnected()) return
        if (!sdk.funcTable.syncV3Spo2) {
            log("Skipped: syncV3Spo2=false")
            toast("V3 SpO2 not supported")
            return
        }
        log("--- 15.4.1 Sync SpO2 ---")
        showProgressDialog("Syncing SpO2...")
        sdk.syncData.startSync(
            types = listOf(IDOSyncDataType.BLOODOXYGEN),
            funcData = { type, jsonStr, errorCode ->
                if (type != IDOSyncDataType.BLOODOXYGEN) return@startSync
                runOnUiThread {
                    log("SpO2 errorCode=$errorCode")
                    if (errorCode == 0 && jsonStr.isNotEmpty()) {
                        log("  payload: ${preview(jsonStr)}")
                    }
                }
            },
            funcCompleted = { errorCode ->
                runOnUiThread {
                    closeProgressDialog()
                    log("SpO2 done errorCode=$errorCode")
                    toast(if (errorCode == 0) "Done" else "Failed $errorCode")
                }
            }
        )
    }

    private fun syncPressure() {
        if (!ensureConnected()) return
        if (!sdk.funcTable.syncV3Pressure) {
            log("Skipped: syncV3Pressure=false")
            toast("V3 stress not supported")
            return
        }
        log("--- 15.4.2 Sync stress ---")
        showProgressDialog("Syncing stress...")
        sdk.syncData.startSync(
            types = listOf(IDOSyncDataType.PRESSURE),
            funcData = { type, jsonStr, errorCode ->
                if (type != IDOSyncDataType.PRESSURE) return@startSync
                runOnUiThread {
                    log("Stress errorCode=$errorCode")
                    if (errorCode == 0 && jsonStr.isNotEmpty()) {
                        log("  payload: ${preview(jsonStr)}")
                    }
                }
            },
            funcCompleted = { errorCode ->
                runOnUiThread {
                    closeProgressDialog()
                    log("Stress done errorCode=$errorCode")
                    toast(if (errorCode == 0) "Done" else "Failed $errorCode")
                }
            }
        )
    }

    private fun syncActivityAmount() {
        if (!ensureConnected()) return
        if (!sdk.funcTable.syncV3ActivityAmount) {
            log("Skipped: syncV3ActivityAmount=false")
            toast("V3 activity amount not supported")
            return
        }
        log("--- 15.4.23 Sync activity amount ---")
        showProgressDialog("Syncing activity amount...")
        sdk.syncData.startSync(
            types = listOf(IDOSyncDataType.ACTIVITYAMOUNT),
            funcData = { type, jsonStr, errorCode ->
                if (type != IDOSyncDataType.ACTIVITYAMOUNT) return@startSync
                runOnUiThread {
                    log("Activity amount errorCode=$errorCode")
                    if (errorCode == 0 && jsonStr.isNotEmpty()) {
                        log("  payload: ${preview(jsonStr)}")
                    }
                }
            },
            funcCompleted = { errorCode ->
                runOnUiThread {
                    closeProgressDialog()
                    log("Activity amount done errorCode=$errorCode")
                    toast(if (errorCode == 0) "Done" else "Failed $errorCode")
                }
            }
        )
    }

    // MARK: - 2026-07-06 New feature integration test

    private fun logCmd(title: String, res: com.idosmart.pigeon_implement.CmdResponse<*>) {
        if (res.error.code == 0) {
            // CmdResponse<*> uses a star projection for res; it must be cast to IDOBaseModel before calling toJsonString
            log("  code=0 json: ${preview((res.res as? IDOBaseModel)?.toJsonString())}")
            toast("Succeeded")
        } else {
            log("  code=${res.error.code} msg=${res.error.message}")
            toast("Failed code=${res.error.code}")
        }
    }

    private fun getFirmwareStatusInfo() {
        if (!ensureConnected()) return
        log("--- 2.53 Firmware status (getFirmwareStatusInfo) ---")
        showProgressDialog("2.53 Firmware status")
        Cmds.getFirmwareStatusInfo().send {
            closeProgressDialog()
            logCmd("2.53 Firmware status", it)
        }
    }

    private fun getHeartRateMode() {
        if (!ensureConnected()) return
        log("--- 2.39 Heart rate monitoring mode (getHeartRateMode) ---")
        showProgressDialog("2.39 Heart rate monitoring")
        Cmds.getHeartRateMode().send {
            closeProgressDialog()
            logCmd("2.39 Heart rate monitoring", it)
        }
    }

    private fun getAppSleepMode() {
        if (!ensureConnected()) return
        log("--- 2.63 Sleep mode (getAppSleepMode) ---")
        showProgressDialog("2.63 Sleep mode")
        Cmds.getAppSleepMode().send {
            closeProgressDialog()
            logCmd("2.63 Sleep mode", it)
        }
    }

    private fun getFallMonitoringSwitch() {
        if (!ensureConnected()) return
        log("--- 2.54 Fall detection GET (getFallMonitoringSwitch) ---")
        showProgressDialog("2.54 Fall monitoring GET")
        Cmds.getFallMonitoringSwitch().send {
            closeProgressDialog()
            logCmd("2.54 Fall monitoring GET", it)
        }
    }

    private fun getPositionSwitchMode() {
        if (!ensureConnected()) return
        log("--- 2.56 Positioning switch GET (getPositionSwitchMode) ---")
        showProgressDialog("2.56 Positioning switch GET")
        Cmds.getPositionSwitchMode().send {
            closeProgressDialog()
            logCmd("2.56 Positioning switch GET", it)
        }
    }

    private fun getDataTranConfig() {
        if (!ensureConnected()) return
        val param = IDODataTranConfigParamModel(type = 0, medicineType = 1)
        log("--- 2.28 File transfer config (getDataTranConfig) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("2.28 File transfer config")
        Cmds.getDataTranConfig(param).send {
            closeProgressDialog()
            logCmd("2.28 File transfer config", it)
        }
    }

    private fun getWatchListV3() {
        if (!ensureConnected()) return
        log("--- 15.26 Watch face list V3 (getWatchListV3) ---")
        showProgressDialog("15.26 Watch face list")
        Cmds.getWatchListV3().send {
            closeProgressDialog()
            logCmd("15.26 Watch face list", it)
        }
    }

    private fun setFallMonitoringSwitchSample() {
        if (!ensureConnected()) return
        val param = IDOFallMonitoringSwitchModel(fallMonitoringSwitch = 1)
        log("--- 2.54 Fall detection SET ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("2.54 Fall monitoring SET")
        Cmds.setFallMonitoringSwitch(param).send {
            closeProgressDialog()
            logCmd("2.54 Fall monitoring SET", it)
        }
    }

    private fun setPositionSwitchModeSample() {
        if (!ensureConnected()) return
        val param = IDOPositionSwitchModeModel(
            switchMode = 1, startHour = 0, startMinute = 0, endHour = 23, endMinute = 59
        )
        log("--- 2.56 Positioning switch SET ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("2.56 Positioning switch SET")
        Cmds.setPositionSwitchMode(param).send {
            closeProgressDialog()
            logCmd("2.56 Positioning switch SET", it)
        }
    }

    private fun setLocationInfoNotify() {
        if (!ensureConnected()) return
        val param = IDOLocationInfoNotifyModel(status = 1)
        log("--- 5.6 Location notification (setLocationInfoNotify) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("5.6 Location notification")
        Cmds.setLocationInfoNotify(param).send {
            closeProgressDialog()
            logCmd("5.6 Location notification", it)
        }
    }

    private fun setHeartRateModeSample() {
        if (!ensureConnected()) return
        val param = IDOHeartRateModeParamModel(1, 1, 0, 0, 23, 59, 5)
        log("--- 2.39 Heart rate monitoring SET(example) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("2.39 Heart rate monitoring SET")
        Cmds.setHeartRateMode(param).send {
            closeProgressDialog()
            logCmd("2.39 Heart rate monitoring SET", it)
        }
    }

    private fun setAppSleepModeSample() {
        if (!ensureConnected()) return
        val param = IDOAppSleepModeParamModel(sleepModeSwitch = 1)
        log("--- 2.63 Sleep mode SET(example) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("2.63 Sleep mode SET")
        Cmds.setAppSleepMode(param).send {
            closeProgressDialog()
            logCmd("2.63 Sleep mode SET", it)
        }
    }

    private fun setHeartModeSample() {
        if (!ensureConnected()) return
        val param = IDOHeartModeParamModel(
            updateTime = 0, mode = 1, hasTimeRange = 1,
            startHour = 0, startMinute = 0, endHour = 23, endMinute = 59,
            measurementInterval = 300, notifyFlag = 1
        )
        log("--- 15.9 V3 heart rate mode (example) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.9 V3 heart rate mode")
        Cmds.setHeartMode(param).send {
            closeProgressDialog()
            logCmd("15.9 V3 heart rate mode", it)
        }
    }

    private fun getAppletList() {
        if (!ensureConnected()) return
        log("--- 15.54 Applet list (setAppletControl queryTypes) ---")
        showProgressDialog("15.54 Applet list")
        Cmds.setAppletControl(
            IDOAppletListQueryType.DOWNLOADING,
            IDOAppletListQueryType.INSTALLING,
            IDOAppletListQueryType.INSTALLED,
        ).send {
            closeProgressDialog()
            logCmd("15.54 Applet list", it)
        }
    }

    /** Query the list first, then delete the first applet returned (operate=2) */
    private fun deleteAppletFirst() {
        if (!ensureConnected()) return
        log("--- 15.54 Delete first applet (query first, then delete) ---")
        showProgressDialog("15.54 Query applet list")
        Cmds.setAppletControl(
            IDOAppletListQueryType.DOWNLOADING,
            IDOAppletListQueryType.INSTALLING,
            IDOAppletListQueryType.INSTALLED,
        ).send { queryRes ->
            if (queryRes.error.code != 0) {
                closeProgressDialog()
                logCmd("15.54 Applet list (pre-delete query)", queryRes)
                return@send
            }
            logCmd("15.54 Applet list (pre-delete query)", queryRes)
            val appName = queryRes.res?.infoItem?.firstOrNull()?.appName
            if (appName.isNullOrEmpty()) {
                closeProgressDialog()
                log("  List is empty or appName is empty, skipping delete")
                toast("Nothing to delete")
                return@send
            }
            val param = IDOAppletControlModel(operate = 2, appName = appName)
            log("  Deleting first item: $appName")
            log("  Request: ${param.toJsonString()}")
            showProgressDialog("15.54 Delete $appName")
            Cmds.setAppletControl(param).send { delRes ->
                closeProgressDialog()
                logCmd("15.54 Delete first applet", delRes)
            }
        }
    }

    private fun setAppBaseInfoSample() {
        if (!ensureConnected()) return
        val param = appBaseInfoSampleParam()
        log("--- 15.79 APP basic info (setAppBaseInfo) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.79 APP basic info")
        Cmds.setAppBaseInfo(param).send {
            closeProgressDialog()
            logCmd("15.79 APP basic info", it)
        }
    }

    private fun takeMedicineRemindQuery() {
        if (!ensureConnected()) return
        val param = IDOTakeMedicineRemindModel(operate = 2)
        log("--- 15.90 Medication reminder query (takeMedicineRemind operate=2) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.90 Medication reminder query")
        Cmds.takeMedicineRemind(param).send {
            closeProgressDialog()
            logCmd("15.90 Medication reminder query", it)
        }
    }

    /** Query first, then delete the first item of the returned list (operate=3, index of the first item) */
    private fun takeMedicineRemindDeleteFirst() {
        if (!ensureConnected()) return
        log("--- 15.90 Medication reminder delete first (query first, then operate=3) ---")
        showProgressDialog("15.90 Query medication reminders")
        Cmds.takeMedicineRemind(IDOTakeMedicineRemindModel(operate = 2)).send { queryRes ->
            if (queryRes.error.code != 0) {
                closeProgressDialog()
                logCmd("15.90 Medication reminder (pre-delete query)", queryRes)
                return@send
            }
            logCmd("15.90 Medication reminder (pre-delete query)", queryRes)
            val first = queryRes.res?.takeMedicineInfoItems?.firstOrNull()
            val index = first?.index
            if (index == null || index <= 0) {
                closeProgressDialog()
                log("  List is empty or index is invalid, skipping delete")
                toast("Nothing to delete")
                return@send
            }
            val del = IDOTakeMedicineRemindModel(operate = 3, index = index)
            log("  Deleting first item index=$index name=${first.name}")
            log("  Request: ${del.toJsonString()}")
            showProgressDialog("15.90 Medication reminder delete")
            Cmds.takeMedicineRemind(del).send { delRes ->
                closeProgressDialog()
                logCmd("15.90 Medication reminder delete first", delRes)
            }
        }
    }

    private fun takeMedicineRemindSetSwitch() {
        if (!ensureConnected()) return
        val param = IDOTakeMedicineRemindModel(operate = 4, medicineShowOnOff = 1)
        log("--- 15.90 Medication reminder set master switch (takeMedicineRemind operate=4) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.90 Medication reminder set master switch")
        Cmds.takeMedicineRemind(param).send {
            closeProgressDialog()
            logCmd("15.90 Medication reminder set master switch", it)
        }
    }

    private fun takeMedicineRemindSet() {
        if (!ensureConnected()) return
        val cal1 = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MINUTE, 1) }
        val cal2 = java.util.Calendar.getInstance().apply { add(java.util.Calendar.MINUTE, 2) }
        val t1 = IDOTakeMedicineRemindTimeItemModel().apply {
            hour = cal1.get(java.util.Calendar.HOUR_OF_DAY)
            minute = cal1.get(java.util.Calendar.MINUTE)
        }
        val t2 = IDOTakeMedicineRemindTimeItemModel().apply {
            hour = cal2.get(java.util.Calendar.HOUR_OF_DAY)
            minute = cal2.get(java.util.Calendar.MINUTE)
        }
        val infoItem = IDOTakeMedicineRemindInfoItemModel().apply {
            index = 1
            year = cal1.get(java.util.Calendar.YEAR)
            month = cal1.get(java.util.Calendar.MONTH) + 1
            day = cal1.get(java.util.Calendar.DAY_OF_MONTH)
            repeatValue = 127
            name = "Aspirin"
            dailyTimes = 2
            timeCount = 2
            timeItems = listOf(t1, t2)
        }
        val param = IDOTakeMedicineRemindModel(
            operate = 1,
            medicineShowOnOff = 1,
            takeMedicineInfoCount = 1,
            takeMedicineInfoItem = infoItem,
        )
        log("--- 15.90 Medication reminder set (takeMedicineRemind operate=1) ---")
        log("  Reminder times: ${t1.hour}:${t1.minute.toString().padStart(2, '0')}(+1min), ${t2.hour}:${t2.minute.toString().padStart(2, '0')}(+2min)")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.90 Medication reminder set")
        Cmds.takeMedicineRemind(param).send {
            closeProgressDialog()
            logCmd("15.90 Medication reminder set", it)
        }
    }

    private fun takeMedicineRemindIconTransfer() {
        if (!ensureConnected()) return
        val ft = sdk.funcTable
        if (!ft.getNotifyIconAdaptive) {
            log("Skipped: support_v3_notify_icon_adaptive=false")
            toast("Icon adaptation not supported")
            return
        }
        if (!ft.supportTakeMedicineReminder) {
            log("Skipped: support_take_medicine_reminder=false")
            toast("Medication reminder not supported")
            return
        }
        log("--- 15.90 Medication reminder icon transfer: select an image, it will be cropped to 160×160 PNG ---")
        pickMedicIconLauncher.launch("image/*")
    }

    /** Center-crop to a square, scale to 160×160 and write a temporary PNG (aligned with iOS makeMedicIconPNGFile) */
    private fun makeMedicIconPngFile(uri: Uri): String? {
        val src = try {
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        } catch (e: Exception) {
            log("Failed to read gallery image: ${e.message}")
            null
        } ?: return null
        if (src.width <= 0 || src.height <= 0) {
            src.recycle()
            return null
        }
        val side = MEDIC_ICON_PIXEL_SIZE
        val scale = max(side.toFloat() / src.width, side.toFloat() / src.height)
        val drawnW = src.width * scale
        val drawnH = src.height * scale
        val out = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val left = (side - drawnW) / 2f
        val top = (side - drawnH) / 2f
        canvas.drawBitmap(
            src,
            null,
            RectF(left, top, left + drawnW, top + drawnH),
            Paint(Paint.FILTER_BITMAP_FLAG),
        )
        src.recycle()
        val file = File(cacheDir, "medic_icon_160.png")
        return try {
            FileOutputStream(file).use { fos ->
                if (!out.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                    out.recycle()
                    return null
                }
            }
            out.recycle()
            file.absolutePath
        } catch (e: Exception) {
            out.recycle()
            log("Failed to write temporary PNG: ${e.message}")
            null
        }
    }

    private fun startMedicIconTransfer(imgPath: String) {
        log("--- 15.90 Medication reminder icon transfer (.medic / 0x1C) ---")
        log("  Source image: $imgPath (160×160 PNG)")
        showProgressDialog("Transferring icon...")
        val fileSize = File(imgPath).length().toInt()
        val item = IDOTransMedicModel(imgPath, "1", fileSize)
        sdk.transfer.transferFiles(
            fileItems = listOf(item),
            cancelPrevTranTask = true,
            transProgress = { curIdx, total, _, totalProg ->
                log("  Progress: ${curIdx + 1}/$total ${(totalProg * 100).toInt()}%")
            },
            transStatus = { curIdx, status, errCode, _ ->
                log("  Status[$curIdx]: $status err=$errCode")
            },
        ) { results ->
            closeProgressDialog()
            val ok = results.isNotEmpty() && results.all { it }
            log("Transfer done: $results")
            runOnUiThread {
                toast(if (ok) "Icon transfer succeeded" else "Icon transfer failed")
            }
        }
    }

    private fun setPurchasedWatchFaceInfoSample() {
        if (!ensureConnected()) return
        val param = IDOPurchasedWatchFaceInfoModel(
            paymentStatus = 3, userId = "user_123", watchId = "dial_001"
        )
        log("--- 15.91 Purchased watch faces (setPurchasedWatchFaceInfo) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.91 Purchased watch faces")
        Cmds.setPurchasedWatchFaceInfo(param).send {
            closeProgressDialog()
            logCmd("15.91 Purchased watch faces", it)
        }
    }

    private fun setAppDownloadStatusInfoSample() {
        if (!ensureConnected()) return
        val param = IDOAppDownloadStatusInfoModel(type = 1, status = 1, id = "watch_face_001")
        log("--- 15.92 Download status (setAppDownloadStatusInfo) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.92 Download status")
        Cmds.setAppDownloadStatusInfo(param).send {
            closeProgressDialog()
            logCmd("15.92 Download status", it)
        }
    }

    private fun getFirmwarePositionInfoQuery() {
        if (!ensureConnected()) return
        val param = IDOFirmwarePositionInfoModel(operate = 1)
        log("--- 15.93 Firmware positioning query (getFirmwarePositionInfo operate=1) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.93 Firmware position query")
        Cmds.getFirmwarePositionInfo(param).send {
            closeProgressDialog()
            logCmd("15.93 Firmware position query", it)
        }
    }

    /** Query first, then confirm receipt of the returned record's timestamp with operate=2 */
    private fun getFirmwarePositionInfoConfirm() {
        if (!ensureConnected()) return
        log("--- 15.93 Firmware positioning confirm (query first, then operate=2) ---")
        showProgressDialog("15.93 Query position data")
        Cmds.getFirmwarePositionInfo(IDOFirmwarePositionInfoModel(operate = 1)).send { queryRes ->
            if (queryRes.error.code != 0) {
                closeProgressDialog()
                logCmd("15.93 Firmware position (pre-confirm query)", queryRes)
                return@send
            }
            logCmd("15.93 Firmware position (pre-confirm query)", queryRes)
            val model = queryRes.res
            val ts = model?.positionInfoItem?.timestamp
            val count = model?.positionInfoCount ?: 0
            if (ts == null || count <= 0) {
                closeProgressDialog()
                log("  No position data or timestamp missing, skipping confirm")
                toast("No data to confirm")
                return@send
            }
            val ack = IDOFirmwarePositionInfoModel(operate = 2, timestamp = ts)
            log("  Confirm timestamp=$ts")
            log("  Request: ${ack.toJsonString()}")
            showProgressDialog("15.93 Confirm receipt")
            Cmds.getFirmwarePositionInfo(ack).send { ackRes ->
                closeProgressDialog()
                logCmd("15.93 Firmware position confirm", ackRes)
            }
        }
    }

    private fun appListStyleQuery() {
        if (!ensureConnected()) return
        val param = IDOAppListStyleParamModel(operate = 2)
        log("--- 15.73 App list style query (appListStyle operate=2) ---")
        log("  Request: ${param.toJsonString()}")
        showProgressDialog("15.73 App list style query")
        Cmds.appListStyle(param).send {
            closeProgressDialog()
            it.res?.let { m ->
                log("  Used ${m.userApplicationListItemNum}/${m.applicationListTotalNum}, list=${m.listItems?.size ?: 0}")
            }
            logCmd("15.73 App list style query", it)
        }
    }

    /** Query first, then delete the first item of the returned list (operate=3) */
    private fun appListStyleDeleteFirst() {
        if (!ensureConnected()) return
        log("--- 15.73 App list style delete (query first, then operate=3) ---")
        showProgressDialog("15.73 Query app list style")
        Cmds.appListStyle(IDOAppListStyleParamModel(operate = 2)).send { queryRes ->
            if (queryRes.error.code != 0) {
                closeProgressDialog()
                logCmd("15.73 App list style (pre-delete query)", queryRes)
                return@send
            }
            logCmd("15.73 App list style (pre-delete query)", queryRes)
            val first = queryRes.res?.listItems?.firstOrNull()
            val name = first?.name
            if (name.isNullOrEmpty()) {
                closeProgressDialog()
                log("  List is empty or name is empty, skipping delete")
                toast("Nothing to delete")
                return@send
            }
            val del = IDOAppListStyleParamModel(operate = 3, name = name)
            log("  Deleting name=$name")
            log("  Request: ${del.toJsonString()}")
            showProgressDialog("15.73 App list style delete")
            Cmds.appListStyle(del).send { delRes ->
                closeProgressDialog()
                logCmd("15.73 App list style delete", delRes)
            }
        }
    }

    private fun appBaseInfoSampleParam() = IDOAppInfoModel(
        operate = 1,
        userName = "mssj52u@163.com",
        userId = "271314614262829056",
        token = APP_BASE_INFO_SAMPLE_TOKEN,
        domainName = "ali",
        appVersion = "3.5.0",
        appKey = "548a50bc9f0a45d0bdfcdb5d194641d8",
        phoneSystem = 1,
        isFilterWatch = 2,
        appFaceVersion = "6",
    )

    companion object {
        /** Medication reminder icon side length (pixels); the `.medic` protocol requires 160×160 PNG */
        private const val MEDIC_ICON_PIXEL_SIZE = 160

        private const val APP_BASE_INFO_SAMPLE_TOKEN =
            "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJkYXRldGltZSI6MTc4MzkyODcwNjkzMiwidXNlcl90eXBlIjoiVVNFUiIsInVzZXJfaWQiOiIyNzEzMTQ2MTQyNjI4MjkwNTYiLCJzb3VyY2UiOiJhcHAiLCJ0eXBlIjoiYXBwIiwiYXBwX2lkIjoiMTAwMDAiLCJhY2NvdW50IjoibXNzajUydUAxNjMuY29tIiwiaWF0IjoxNzgzOTI4NzA2LCJleHAiOjQ5Mzc1Mjg3MDZ9.-FPYg231BUTM2LzfihWdIeoStGJGNT6G1Oga0Ik6VWsivIhWTfQzH32C5to7C5txa5QhmVhW-aAD9q73phOeAw"
    }
}
