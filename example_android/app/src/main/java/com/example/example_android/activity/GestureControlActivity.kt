package com.example.example_android.activity

import android.content.Intent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import com.example.example_android.R
import com.example.example_android.base.BaseActivity
import com.idosmart.model.IDOGestureControlModel
import com.idosmart.model.IDOGestureFunctionItemModel
import com.idosmart.model.IDOGestureSubFunctionItemModel
import com.idosmart.model.IDOGestureTypeItemModel
import com.idosmart.pigeon_implement.Cmds
import com.idosmart.protocol_channel.sdk

/**
 * 15.82 Gesture control integration-test page (aligned with PulseBand GestureActivity):
 * On entering: 03→02; dynamic capability tree; one function selected at a time; gestures of sub-functions within the same function are mutually exclusive; set via 01.
 */
class GestureControlActivity : BaseActivity() {

    private lateinit var switchMaster: Switch
    private lateinit var llTree: LinearLayout
    private lateinit var tvStatus: TextView
    private lateinit var tvSummary: TextView
    private lateinit var tvResult: TextView

    private val funcUis = mutableListOf<FuncUi>()
    private var supportModel: IDOGestureControlModel? = null
    private var currentModel: IDOGestureControlModel? = null
    private var awaitCurrentAfterSupport = false
    private var suppressSpinnerCallback = false
    private var suppressFuncSwitchCallback = false

    private data class SubUi(
        val subType: Int,
        val allowedGestures: List<Int>,
        val label: TextView,
        val spinner: Spinner,
        var selectedGesture: Int,
    )

    private data class FuncUi(
        val functionType: Int,
        val switch: Switch,
        val subs: List<SubUi>,
    )

    override fun getLayoutId(): Int = R.layout.activity_gesture_control

    override fun initView() {
        super.initView()
        supportActionBar?.title = getString(R.string.gesture_control)

        tvStatus = findViewById(R.id.tv_gesture_status)
        tvSummary = findViewById(R.id.tv_gesture_summary)
        tvResult = findViewById(R.id.tv_gesture_result)
        switchMaster = findViewById(R.id.switch_gesture_master)
        llTree = findViewById(R.id.ll_gesture_tree)

        findViewById<Button>(R.id.btn_gesture_support).setOnClickListener {
            if (!ensureSupport()) return@setOnClickListener
            awaitCurrentAfterSupport = false
            querySupport()
        }
        findViewById<Button>(R.id.btn_gesture_get).setOnClickListener {
            if (!ensureSupport()) return@setOnClickListener
            queryCurrent()
        }
        findViewById<Button>(R.id.btn_gesture_refresh).setOnClickListener {
            if (!ensureSupport()) return@setOnClickListener
            enterPage()
        }
        findViewById<Button>(R.id.btn_gesture_set).setOnClickListener {
            if (!ensureSupport()) return@setOnClickListener
            setFromUi()
        }
        findViewById<Button>(R.id.btn_gesture_call_notice).setOnClickListener {
            startActivity(Intent(this, CallNoticeActivity::class.java))
        }
        findViewById<Button>(R.id.btn_gesture_clear).setOnClickListener {
            clearDisplay()
        }

        switchMaster.setOnCheckedChangeListener { _, _ ->
            refreshSummaryFromUi()
        }

        if (!ensureSupport()) {
            return
        }
        enterPage()
    }

    private fun ensureSupport(): Boolean {
        if (!sdk.funcTable.supportOperateGestureControl) {
            tvStatus.text = getString(R.string.gesture_control_not_support)
            toast(getString(R.string.gesture_control_not_support))
            return false
        }
        return true
    }

    /** On entering: 03 → automatically followed by 02 */
    private fun enterPage() {
        tvStatus.text = getString(R.string.gesture_control_status_loading_support)
        awaitCurrentAfterSupport = true
        querySupport()
    }

    private fun querySupport() {
        showProgressDialog(getString(R.string.gesture_control_btn_support))
        Cmds.getGestureControlSupportConfigs().send { resp ->
            closeProgressDialog()
            runOnUiThread {
                if (resp.error.code != 0 || resp.res == null) {
                    tvStatus.text = getString(
                        R.string.gesture_control_status_fail,
                        "03",
                        resp.error.code
                    )
                    tvResult.text = resp.res?.toJsonString() ?: "error=${resp.error.code}"
                    awaitCurrentAfterSupport = false
                    return@runOnUiThread
                }
                supportModel = resp.res
                tvResult.text = resp.res!!.toJsonString()
                tvStatus.text = getString(R.string.gesture_control_status_support_ok)
                buildTreeFromSupport(resp.res!!)
                currentModel?.let { applyCurrentToUi(it) }
                if (awaitCurrentAfterSupport) {
                    awaitCurrentAfterSupport = false
                    queryCurrent()
                }
            }
        }
    }

    private fun queryCurrent() {
        showProgressDialog(getString(R.string.gesture_control_btn_get))
        Cmds.getGestureControl().send { resp ->
            closeProgressDialog()
            runOnUiThread {
                if (resp.error.code != 0 || resp.res == null) {
                    tvStatus.text = getString(
                        R.string.gesture_control_status_fail,
                        "02",
                        resp.error.code
                    )
                    tvResult.text = resp.res?.toJsonString() ?: "error=${resp.error.code}"
                    return@runOnUiThread
                }
                currentModel = resp.res
                tvResult.text = resp.res!!.toJsonString()
                tvStatus.text = getString(R.string.gesture_control_status_current_ok)
                if (funcUis.isNotEmpty()) {
                    applyCurrentToUi(resp.res!!)
                }
            }
        }
    }

    private fun setFromUi() {
        val support = supportModel
        if (support == null) {
            tvStatus.text = getString(R.string.gesture_control_need_support_first)
            return
        }
        val model = buildSetModelFromUi(support)
        if (model == null) {
            tvStatus.text = getString(R.string.gesture_control_build_fail)
            return
        }
        tvStatus.text = getString(R.string.gesture_control_status_setting)
        showProgressDialog(getString(R.string.gesture_control_btn_set))
        Cmds.setGestureControl(model).send { resp ->
            closeProgressDialog()
            runOnUiThread {
                tvResult.text = resp.res?.toJsonString()
                    ?: "error=${resp.error.code} setReq=${model.toJsonString()}"
                tvStatus.text = if (resp.error.code == 0) {
                    getString(R.string.gesture_control_status_set_ok)
                } else {
                    getString(R.string.gesture_control_status_fail, "01", resp.error.code)
                }
            }
        }
    }

    private fun clearDisplay() {
        supportModel = null
        currentModel = null
        clearTreeUi()
        tvResult.text = getString(R.string.gesture_control_empty)
        tvSummary.text = getString(R.string.gesture_control_flow_hint)
        tvStatus.text = getString(R.string.gesture_control_status_idle)
    }

    private fun clearTreeUi() {
        llTree.removeAllViews()
        funcUis.clear()
        switchMaster.isChecked = false
    }

    private fun buildTreeFromSupport(support: IDOGestureControlModel) {
        val items = support.gestureFunctionItems.orEmpty()
        llTree.removeAllViews()
        funcUis.clear()

        for (item in items) {
            val funcType = item.functionType
            val funcSwitch = Switch(this).apply {
                text = gestureFuncLabel(funcType)
                textSize = 15f
                isChecked = false
            }
            llTree.addView(funcSwitch)

            val subUis = mutableListOf<SubUi>()
            for (sub in item.gestureSubFunctionItems.orEmpty()) {
                val allowed = sub.gestureTypeItems.orEmpty()
                    .map { it.gestureType }
                    .filter { it > 0 }
                if (allowed.isEmpty()) continue

                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(24, 4, 0, 4)
                }
                val label = TextView(this).apply {
                    text = gestureSubLabel(funcType, sub.subFunctionType)
                    textSize = 14f
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val spinner = Spinner(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.2f)
                }
                val defaultGesture = allowed.first()
                val subUi = SubUi(sub.subFunctionType, allowed, label, spinner, defaultGesture)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long,
                    ) {
                        if (suppressSpinnerCallback) return
                        val options = spinner.tag as? List<*> ?: return
                        val gesture = options.getOrNull(position) as? Int ?: return
                        subUi.selectedGesture = gesture
                        val parentFunc = funcUis.find { it.subs.contains(subUi) } ?: return
                        refreshMutualExclusiveSpinners(parentFunc)
                        refreshSummaryFromUi()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                }
                row.addView(label)
                row.addView(spinner)
                llTree.addView(row)
                subUis.add(subUi)
            }

            val funcUi = FuncUi(funcType, funcSwitch, subUis)
            funcUis.add(funcUi)
            assignInitialGestures(funcUi, item)
            refreshMutualExclusiveSpinners(funcUi)

            funcSwitch.setOnCheckedChangeListener { _, checked ->
                if (suppressFuncSwitchCallback) return@setOnCheckedChangeListener
                if (checked) {
                    selectOnlyFunction(funcType)
                }
                refreshSummaryFromUi()
            }
        }

        val onOff = support.gestureControlOnOff
        if (onOff == 0 || onOff == 1) {
            switchMaster.isChecked = onOff == 1
        }
        tvSummary.text = getString(R.string.gesture_control_support_loaded, funcUis.size)
    }

    private fun selectOnlyFunction(functionType: Int) {
        suppressFuncSwitchCallback = true
        for (f in funcUis) {
            f.switch.isChecked = f.functionType == functionType
        }
        suppressFuncSwitchCallback = false
    }

    private fun refreshSummaryFromUi() {
        val masterText = if (switchMaster.isChecked) "ON" else "OFF"
        val enabled = funcUis.firstOrNull { it.switch.isChecked }
        tvSummary.text = buildString {
            append("master=$masterText")
            for (f in funcUis) {
                append(" | ")
                append(gestureFuncLabel(f.functionType))
                append("=")
                append(if (f.switch.isChecked) "ON" else "OFF")
            }
            append(" | enabled=")
            append(enabled?.let { gestureFuncLabel(it.functionType) } ?: "none")
        }
    }

    private fun assignInitialGestures(funcUi: FuncUi, item: IDOGestureFunctionItemModel) {
        val used = mutableSetOf<Int>()
        val subs = item.gestureSubFunctionItems.orEmpty()
        for (subUi in funcUi.subs) {
            val types = subs.firstOrNull { it.subFunctionType == subUi.subType }
                ?.gestureTypeItems.orEmpty()
                .map { it.gestureType }
                .filter { it > 0 }
            val chosen = types.firstOrNull { it !in used }
                ?: subUi.allowedGestures.firstOrNull { it !in used }
                ?: subUi.allowedGestures.first()
            subUi.selectedGesture = chosen
            used.add(chosen)
        }
    }

    private fun applyCurrentToUi(current: IDOGestureControlModel) {
        val onOff = current.gestureControlOnOff
        if (onOff == 0 || onOff == 1) {
            switchMaster.isChecked = onOff == 1
        }

        val byType = current.gestureFunctionItems.orEmpty()
            .associateBy { it.functionType }

        var selectedType: Int? = null
        for (funcUi in funcUis) {
            val item = byType[funcUi.functionType] ?: continue
            if (item.functionSwitch == 1 && selectedType == null) {
                selectedType = funcUi.functionType
            }
        }

        suppressFuncSwitchCallback = true
        for (funcUi in funcUis) {
            val item = byType[funcUi.functionType]
            funcUi.switch.isChecked = selectedType != null && funcUi.functionType == selectedType
            if (item != null) {
                val used = mutableSetOf<Int>()
                val subs = item.gestureSubFunctionItems.orEmpty()
                for (subUi in funcUi.subs) {
                    val picked = subs.firstOrNull { it.subFunctionType == subUi.subType }
                        ?.gestureTypeItems.orEmpty()
                        .map { it.gestureType }
                        .firstOrNull { it in subUi.allowedGestures }
                    val g = when {
                        picked != null && picked !in used -> picked
                        else -> subUi.allowedGestures.firstOrNull { it !in used }
                            ?: subUi.selectedGesture
                    }
                    subUi.selectedGesture = g
                    used.add(g)
                }
            }
            refreshMutualExclusiveSpinners(funcUi)
        }
        suppressFuncSwitchCallback = false
        refreshSummaryFromUi()
    }

    private fun refreshMutualExclusiveSpinners(funcUi: FuncUi) {
        suppressSpinnerCallback = true
        for (subUi in funcUi.subs) {
            val takenByOthers = funcUi.subs
                .filter { it !== subUi }
                .map { it.selectedGesture }
                .toSet()
            var options = subUi.allowedGestures.filter { it !in takenByOthers }
            if (options.isEmpty()) {
                options = subUi.allowedGestures
            }
            if (subUi.selectedGesture !in options) {
                subUi.selectedGesture = options.first()
            }
            val labels = options.map { gestureTypeLabel(it) }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            subUi.spinner.adapter = adapter
            subUi.spinner.tag = options
            subUi.spinner.setSelection(options.indexOf(subUi.selectedGesture).coerceAtLeast(0), false)
        }
        suppressSpinnerCallback = false
    }

    /** Use the support tree as the skeleton, overlay the UI state, and assemble the settings Model */
    private fun buildSetModelFromUi(support: IDOGestureControlModel): IDOGestureControlModel? {
        val enabledType = funcUis.firstOrNull { it.switch.isChecked }?.functionType
        val items = support.gestureFunctionItems ?: return null
        val outItems = items.map { supportItem ->
            val funcUi = funcUis.find { it.functionType == supportItem.functionType }
            val subs = supportItem.gestureSubFunctionItems.orEmpty().map { supportSub ->
                val subUi = funcUi?.subs?.find { it.subType == supportSub.subFunctionType }
                val gesture = subUi?.selectedGesture
                    ?: supportSub.gestureTypeItems?.firstOrNull()?.gestureType
                    ?: 0
                IDOGestureSubFunctionItemModel(
                    subFunctionType = supportSub.subFunctionType,
                    gestureTypeItems = listOf(IDOGestureTypeItemModel(gestureType = gesture)),
                )
            }
            IDOGestureFunctionItemModel(
                functionSwitch = if (enabledType != null && supportItem.functionType == enabledType) 1 else 0,
                functionType = supportItem.functionType,
                gestureSubFunctionItems = subs,
            )
        }
        return IDOGestureControlModel(
            operate = 1,
            gestureControlOnOff = if (switchMaster.isChecked) 1 else 0,
            gestureFunctionItems = outItems,
        )
    }

    companion object {
        private const val FUNC_CALL = 1
        private const val FUNC_PHOTO = 2
        private const val FUNC_MUSIC = 3
        private const val SUB_ANSWER = 1
        private const val SUB_HANG_UP = 2
        private const val SUB_PHOTO = 1
        private const val SUB_MUSIC_NEXT = 1
        private const val TYPE_FLIP_PALM = 1
        private const val TYPE_PLAY_TWICE = 2
        private const val TYPE_ARM_HORIZONTAL = 3
        private const val TYPE_ARM_VERTICAL = 4

        fun gestureFuncLabel(type: Int): String = when (type) {
            FUNC_CALL -> "Call control"
            FUNC_PHOTO -> "Remote photo capture"
            FUNC_MUSIC -> "Music control"
            else -> "Function $type"
        }

        fun gestureSubLabel(funcType: Int, subType: Int): String = when (funcType) {
            FUNC_CALL -> when (subType) {
                SUB_ANSWER -> "Answer call"
                SUB_HANG_UP -> "Hang up call"
                else -> "Sub-function $subType"
            }
            FUNC_PHOTO -> when (subType) {
                SUB_PHOTO -> "Remote photo capture"
                else -> "Sub-function $subType"
            }
            FUNC_MUSIC -> when (subType) {
                SUB_MUSIC_NEXT -> "Next song"
                else -> "Sub-function $subType"
            }
            else -> "Sub-function $subType"
        }

        fun gestureTypeLabel(type: Int): String = when (type) {
            TYPE_FLIP_PALM -> "Palm flip 90°×2"
            TYPE_PLAY_TWICE -> "Flick twice"
            TYPE_ARM_HORIZONTAL -> "Arm horizontal rotation 90°×2"
            TYPE_ARM_VERTICAL -> "Arm vertical rotation 90°×2"
            else -> "Gesture $type"
        }
    }
}
