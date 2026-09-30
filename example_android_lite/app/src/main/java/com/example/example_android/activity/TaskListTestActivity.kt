package com.example.example_android.activity

import android.content.Intent
import android.graphics.Color
import android.widget.ScrollView
import com.example.example_android.R
import com.example.example_android.base.BaseActivity
import com.idosmart.model.IDOTaskListModel
import com.idosmart.pigeon_implement.Cmds
import com.idosmart.protocol_channel.sdk
import kotlinx.android.synthetic.main.activity_task_list_test.btn_full_set
import kotlinx.android.synthetic.main.activity_task_list_test.btn_query_status
import kotlinx.android.synthetic.main.activity_task_list_test.tv_conn_chip
import kotlinx.android.synthetic.main.activity_task_list_test.tv_query_json
import kotlinx.android.synthetic.main.activity_task_list_test.tv_support_chip
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 15.114 Task list - standalone integration-test page entry
 * Aligned with iOS TaskListTestVC: query and show the request/reply log, without checking the function table
 */
class TaskListTestActivity : BaseActivity() {

    private val logBuilder = StringBuilder()

    override fun getLayoutId(): Int = R.layout.activity_task_list_test

    override fun initView() {
        super.initView()
        supportActionBar?.title = getString(R.string.task_list_title)
        refreshStatusBar()
        btn_full_set.setOnClickListener {
            startActivity(Intent(this, TaskListFullSetActivity::class.java))
        }
        btn_query_status.setOnClickListener { queryStatusChanges() }
    }

    override fun onResume() {
        super.onResume()
        refreshStatusBar()
    }

    private fun refreshStatusBar() {
        val connected = sdk.state.isConnected
        tv_conn_chip.text = if (connected) "Connected" else "Disconnected"
        tv_conn_chip.setBackgroundColor(
            if (connected) Color.parseColor("#C8E6C9") else Color.parseColor("#FFCDD2")
        )

        val supported = sdk.funcTable.supportTaskList
        tv_support_chip.text = if (supported) "supportTaskList" else "Not supported"
        tv_support_chip.setBackgroundColor(
            if (supported) Color.parseColor("#C8E6C9") else Color.parseColor("#FFE0B2")
        )
    }

    private fun ensureConnected(): Boolean {
        if (sdk.state.isConnected) return true
        toast("No device connected")
        return false
    }

    private fun appendLog(text: String) {
        val ts = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val line = "[$ts] $text"
        if (logBuilder.isEmpty()) {
            logBuilder.append(line)
        } else {
            logBuilder.append('\n').append(line)
        }
        tv_query_json.text = logBuilder.toString()
    }

    private fun prettyJson(raw: String): String {
        return try {
            when {
                raw.trimStart().startsWith("[") -> JSONArray(raw).toString(2)
                else -> JSONObject(raw).toString(2)
            }
        } catch (_: Exception) {
            raw
        }
    }

    private fun queryStatusChanges() {
        if (!ensureConnected()) return
        val param = IDOTaskListModel(operate = 2)
        val req = param.toJsonString() ?: """{"operate":2}"""
        appendLog("Query status request:\n${prettyJson(req)}")
        showProgressDialog("Querying status changes…")
        Cmds.taskList(param).send { res ->
            closeProgressDialog()
            if (res.error.code != 0) {
                appendLog("Query status reply: failed code=${res.error.code}")
                toast("Failed code=${res.error.code}")
                return@send
            }
            val model = res.res
            if (model == null) {
                appendLog("Query status reply: (empty)")
                toast("No JSON reply")
                return@send
            }
            appendLog("Query status reply:\n${formatReply(model)}")
            toast("Query done")
        }
    }

    private fun formatReply(model: IDOTaskListModel): String {
        val root = JSONObject()
        root.put("operate", model.operate)
        model.errorCode?.let { root.put("error_code", it) }
        if (model.operate == 2) {
            model.statusItemNum?.let { root.put("status_item_num", it) }
            val arr = JSONArray()
            model.statusItems?.forEach { item ->
                arr.put(
                    JSONObject()
                        .put("task_id", item.taskId)
                        .put("status", item.status)
                        .put("complete_timestamp", item.completeTimestamp)
                )
            }
            root.put("status_items", arr)
        }
        return root.toString(2)
    }
}
