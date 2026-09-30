package com.example.example_android.activity

import android.graphics.Typeface
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.example.example_android.R
import com.example.example_android.base.BaseActivity
import com.idosmart.enums.IDOSyncDataType
import com.idosmart.model.IDOAppDownloadStatusInfoModel
import com.idosmart.model.IDOAppInfoModel
import com.idosmart.model.IDOAppListStyleParamModel
import com.idosmart.model.IDOAppSleepModeParamModel
import com.idosmart.model.IDOAppletControlModel
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
import com.idosmart.model.IDODeviceVibrationRingtoneModel
import com.idosmart.pigeon_implement.Cmds
import com.idosmart.protocol_channel.sdk
import com.idosmart.protocol_sdk.IDOExchangeDataDelegate
import com.idosmart.model.IDOBleExecType
import com.idosmart.model.IDOBleReplyType
import kotlinx.android.synthetic.main.activity_sdk_feature_test.ll_buttons
import kotlinx.android.synthetic.main.activity_sdk_feature_test.sv_log
import kotlinx.android.synthetic.main.activity_sdk_feature_test.tv_log

/**
 * Integration test of the standardized SDK features already delivered (aligned with Flutter SdkFeatureTestPage / iOS SdkFeatureTestVC)
 */
class SdkFeatureTestActivity : BaseActivity() {

    private val logBuffer = StringBuilder()
    private var supportSyncTypesText = ""

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
                "15.106 Vibration ringtone" to { getVibrationRingtone() },
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
                "15.90 Medication reminder query" to { takeMedicineRemindQuery() },
                "15.90 Medication reminder delete first" to { takeMedicineRemindDeleteFirst() },
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
                    [Health sync] spo2=${ft.syncV3Spo2} pressure=${ft.syncV3Pressure}
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
        Cmds.getFirmwareStatusInfo().send { closeProgressDialog(); logCmd("2.53 Firmware status", it) }
    }

    private fun getHeartRateMode() {
        if (!ensureConnected()) return
        log("--- 2.39 Heart rate monitoring mode (getHeartRateMode) ---")
        showProgressDialog("2.39 Heart rate monitoring")
        Cmds.getHeartRateMode().send { closeProgressDialog(); logCmd("2.39 Heart rate monitoring", it) }
    }

    private fun getAppSleepMode() {
        if (!ensureConnected()) return
        log("--- 2.63 Sleep mode (getAppSleepMode) ---")
        showProgressDialog("2.63 Sleep mode")
        Cmds.getAppSleepMode().send { closeProgressDialog(); logCmd("2.63 Sleep mode", it) }
    }

    private fun getFallMonitoringSwitch() {
        if (!ensureConnected()) return
        log("--- 2.54 Fall monitoring GET ---")
        showProgressDialog("2.54 Fall monitoring GET")
        Cmds.getFallMonitoringSwitch().send { closeProgressDialog(); logCmd("2.54 Fall monitoring GET", it) }
    }

    private fun getPositionSwitchMode() {
        if (!ensureConnected()) return
        log("--- 2.56 Positioning switch GET ---")
        showProgressDialog("2.56 Positioning switch GET")
        Cmds.getPositionSwitchMode().send { closeProgressDialog(); logCmd("2.56 Positioning switch GET", it) }
    }

    private fun getDataTranConfig() {
        if (!ensureConnected()) return
        val param = IDODataTranConfigParamModel(type = 0, medicineType = 1)
        log("--- 2.28 File transfer config --- request: ${param.toJsonString()}")
        showProgressDialog("2.28 File transfer config")
        Cmds.getDataTranConfig(param).send { closeProgressDialog(); logCmd("2.28 File transfer config", it) }
    }

    private fun getWatchListV3() {
        if (!ensureConnected()) return
        log("--- 15.26 Watch face list V3 ---")
        showProgressDialog("15.26 Watch face list")
        Cmds.getWatchListV3().send { closeProgressDialog(); logCmd("15.26 Watch face list", it) }
    }

    private fun setFallMonitoringSwitchSample() {
        if (!ensureConnected()) return
        val param = IDOFallMonitoringSwitchModel(fallMonitoringSwitch = 1)
        log("--- 2.54 Fall monitoring SET --- request: ${param.toJsonString()}")
        showProgressDialog("2.54 Fall monitoring SET")
        Cmds.setFallMonitoringSwitch(param).send { closeProgressDialog(); logCmd("2.54 Fall monitoring SET", it) }
    }

    private fun setPositionSwitchModeSample() {
        if (!ensureConnected()) return
        val param = IDOPositionSwitchModeModel(switchMode = 1, startHour = 0, startMinute = 0, endHour = 23, endMinute = 59)
        log("--- 2.56 Positioning switch SET --- request: ${param.toJsonString()}")
        showProgressDialog("2.56 Positioning switch SET")
        Cmds.setPositionSwitchMode(param).send { closeProgressDialog(); logCmd("2.56 Positioning switch SET", it) }
    }

    private fun setLocationInfoNotify() {
        if (!ensureConnected()) return
        val param = IDOLocationInfoNotifyModel(status = 1)
        log("--- 5.6 Location notification --- request: ${param.toJsonString()}")
        showProgressDialog("5.6 Location notification")
        Cmds.setLocationInfoNotify(param).send { closeProgressDialog(); logCmd("5.6 Location notification", it) }
    }

    private fun setHeartRateModeSample() {
        if (!ensureConnected()) return
        val param = IDOHeartRateModeParamModel(1, 1, 0, 0, 23, 59, 5)
        log("--- 2.39 Heart rate monitoring SET --- request: ${param.toJsonString()}")
        showProgressDialog("2.39 Heart rate monitoring SET")
        Cmds.setHeartRateMode(param).send { closeProgressDialog(); logCmd("2.39 Heart rate monitoring SET", it) }
    }

    private fun setAppSleepModeSample() {
        if (!ensureConnected()) return
        val param = IDOAppSleepModeParamModel(sleepModeSwitch = 1)
        log("--- 2.63 Sleep mode SET --- request: ${param.toJsonString()}")
        showProgressDialog("2.63 Sleep mode SET")
        Cmds.setAppSleepMode(param).send { closeProgressDialog(); logCmd("2.63 Sleep mode SET", it) }
    }

    private fun setHeartModeSample() {
        if (!ensureConnected()) return
        val param = IDOHeartModeParamModel(
            updateTime = 0, mode = 1, hasTimeRange = 1,
            startHour = 0, startMinute = 0, endHour = 23, endMinute = 59,
            measurementInterval = 300, notifyFlag = 1
        )
        log("--- 15.9 V3 heart rate mode --- request: ${param.toJsonString()}")
        showProgressDialog("15.9 V3 heart rate mode")
        Cmds.setHeartMode(param).send { closeProgressDialog(); logCmd("15.9 V3 heart rate mode", it) }
    }

    private fun getAppletList() {
        if (!ensureConnected()) return
        val param = IDOAppletControlModel(operate = 3)
        log("--- 15.54 Applet list --- request: ${param.toJsonString()}")
        showProgressDialog("15.54 Applet list")
        Cmds.setAppletControl(param).send { closeProgressDialog(); logCmd("15.54 Applet list", it) }
    }

    /** Query the list first, then delete the first applet returned (operate=2) */
    private fun deleteAppletFirst() {
        if (!ensureConnected()) return
        log("--- 15.54 Delete first applet (query first, then delete) ---")
        showProgressDialog("15.54 Query applet list")
        Cmds.setAppletControl(IDOAppletControlModel(operate = 3)).send { queryRes ->
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
        log("--- 15.79 APP basic info --- request: ${param.toJsonString()}")
        showProgressDialog("15.79 APP basic info")
        Cmds.setAppBaseInfo(param).send { closeProgressDialog(); logCmd("15.79 APP basic info", it) }
    }

    private fun takeMedicineRemindQuery() {
        if (!ensureConnected()) return
        val param = IDOTakeMedicineRemindModel(operate = 2)
        log("--- 15.90 Medication reminder query --- request: ${param.toJsonString()}")
        showProgressDialog("15.90 Medication reminder query")
        Cmds.takeMedicineRemind(param).send { closeProgressDialog(); logCmd("15.90 Medication reminder query", it) }
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
        log("--- 15.90 Medication reminder master switch --- request: ${param.toJsonString()}")
        showProgressDialog("15.90 Medication reminder set master switch")
        Cmds.takeMedicineRemind(param).send { closeProgressDialog(); logCmd("15.90 Medication reminder master switch", it) }
    }

    private fun setPurchasedWatchFaceInfoSample() {
        if (!ensureConnected()) return
        val param = IDOPurchasedWatchFaceInfoModel(paymentStatus = 3, userId = "user_123", watchId = "dial_001")
        log("--- 15.91 Purchased watch face --- request: ${param.toJsonString()}")
        showProgressDialog("15.91 Purchased watch faces")
        Cmds.setPurchasedWatchFaceInfo(param).send { closeProgressDialog(); logCmd("15.91 Purchased watch face", it) }
    }

    private fun setAppDownloadStatusInfoSample() {
        if (!ensureConnected()) return
        val param = IDOAppDownloadStatusInfoModel(type = 1, status = 1, id = "watch_face_001")
        log("--- 15.92 Download status --- request: ${param.toJsonString()}")
        showProgressDialog("15.92 Download status")
        Cmds.setAppDownloadStatusInfo(param).send { closeProgressDialog(); logCmd("15.92 Download status", it) }
    }

    private fun getFirmwarePositionInfoQuery() {
        if (!ensureConnected()) return
        val param = IDOFirmwarePositionInfoModel(operate = 1)
        log("--- 15.93 Firmware positioning query --- request: ${param.toJsonString()}")
        showProgressDialog("15.93 Firmware position query")
        Cmds.getFirmwarePositionInfo(param).send { closeProgressDialog(); logCmd("15.93 Firmware positioning query", it) }
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
            val ts = queryRes.res?.positionInfoItem?.timestamp
            val count = queryRes.res?.positionInfoCount ?: 0
            if (ts == null || count <= 0) {
                closeProgressDialog()
                log("  No position data or timestamp missing, skipping confirm")
                toast("No data to confirm")
                return@send
            }
            val ack = IDOFirmwarePositionInfoModel(operate = 2, timestamp = ts)
            log("  Confirm timestamp=$ts request: ${ack.toJsonString()}")
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
        log("--- 15.73 App list style query --- request: ${param.toJsonString()}")
        showProgressDialog("15.73 App list style query")
        Cmds.appListStyle(param).send {
            closeProgressDialog()
            it.res?.let { m ->
                log("  Used ${m.userApplicationListItemNum}/${m.applicationListTotalNum}, list=${m.listItems?.size ?: 0}")
            }
            logCmd("15.73 App list style query", it)
        }
    }

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
            val name = queryRes.res?.listItems?.firstOrNull()?.name
            if (name.isNullOrEmpty()) {
                closeProgressDialog()
                log("  List is empty or name is empty, skipping delete")
                toast("Nothing to delete")
                return@send
            }
            val del = IDOAppListStyleParamModel(operate = 3, name = name)
            log("  Delete name=$name request: ${del.toJsonString()}")
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
        private const val APP_BASE_INFO_SAMPLE_TOKEN =
            "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJkYXRldGltZSI6MTc4MzkyODcwNjkzMiwidXNlcl90eXBlIjoiVVNFUiIsInVzZXJfaWQiOiIyNzEzMTQ2MTQyNjI4MjkwNTYiLCJzb3VyY2UiOiJhcHAiLCJ0eXBlIjoiYXBwIiwiYXBwX2lkIjoiMTAwMDAiLCJhY2NvdW50IjoibXNzajUydUAxNjMuY29tIiwiaWF0IjoxNzgzOTI4NzA2LCJleHAiOjQ5Mzc1Mjg3MDZ9.-FPYg231BUTM2LzfihWdIeoStGJGNT6G1Oga0Ik6VWsivIhWTfQzH32C5to7C5txa5QhmVhW-aAD9q73phOeAw"
    }
}
