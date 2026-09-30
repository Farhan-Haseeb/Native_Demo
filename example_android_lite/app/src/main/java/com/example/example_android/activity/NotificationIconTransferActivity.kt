package com.example.example_android.activity

//import com.example.example_android.adapter.NotificationIconAdapter
import LoadingManager
import android.app.Notification
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.example_android.R
import com.example.example_android.adapter.NotificationIconAdapter
import com.example.example_android.base.BaseActivity
import com.example.example_android.data.MessageNotifyState
import com.example.example_android.data.TranIconBean
import com.google.gson.Gson
import com.idosmart.enums.IDOLogType
import com.idosmart.enums.IDOStatusNotification
import com.idosmart.model.IDOAppIconItemModel
import com.idosmart.model.IDOBleDataRequest
import com.idosmart.model.IDODeviceNotificationModel
import com.idosmart.model.IDOFastMsgItem
import com.idosmart.model.IDOFastMsgSettingModel
import com.idosmart.model.IDOFastMsgUpdateModel
import com.idosmart.model.IDOFastMsgUpdateParamModel
import com.idosmart.model.IDONoticeMesaageParamItem
import com.idosmart.model.IDONoticeMesaageParamModel
import com.idosmart.model.IDONoticeMessageStateItemItem
import com.idosmart.model.IDONoticeMessageStateParamModel
import com.idosmart.model.IDONotificationStatusParamModel
import com.idosmart.pigeon_implement.Cmds
import com.idosmart.protocol_channel.sdk
import com.idosmart.protocol_sdk.IDOBridgeDelegate
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_pop_allow
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_pop_clos
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_pop_clos_back
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_pop_content
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_pop_ll
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_pop_sendMsg
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_pop_sendMsg_quick
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_pop_silent
import kotlinx.android.synthetic.main.activity_notification_icon_transfer.app_notification_rv
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.log

class NotificationIconTransferActivity : BaseActivity() {
    private val TAG = "NotificationIconTransfer"
    var apps = mutableListOf<TranIconBean>()
    var mClickedApp: TranIconBean? = null
    lateinit var notificationIconAdapter: NotificationIconAdapter
    private lateinit var loadingManager: LoadingManager

    //Languages supported by the device
    private val languages: List<Int> = ArrayList()


    override fun getLayoutId(): Int {
        return R.layout.activity_notification_icon_transfer
    }


    override fun initView() {
        super.initView()

        //When unbinding, call the sdk.messageIcon.resetIconInfoData method to clear the local icon cache

        loadingManager = LoadingManager(this)
        Log.d(TAG, "getFastMsgDataV3: ${sdk.funcTable.getFastMsgDataV3}")
        //Check the function table to see whether the device supports message quick reply
        if (!sdk.funcTable.getFastMsgDataV3) {
            app_notification_pop_sendMsg_quick.visibility = View.GONE
        }
        loadingManager.show()
        //Before getting the app list, grant the app-list read permission and declare the corresponding permission in the manifest, otherwise not all apps can be retrieved
        sdk.messageIcon.firstGetAppInfo(force = true) { it ->
            loadingManager.dismiss()

            it.forEach {
                val file = File(it.iconLocalPath)
                if (it.isDefault!!) {
                    apps.add(
                        TranIconBean(
                            it.evtType,
                            it.packName,
                            it.appName,
                            Drawable.createFromPath(file.path),
                            1,
                            it.state,
                            it.isUpdateAppIcon
                        )
                    )
                } else {
                    apps.add(
                        TranIconBean(
                            it.evtType,
                            it.packName,
                            it.appName,
                            Drawable.createFromPath(file.path),
                            3,
                            it.state,
                            it.isUpdateAppIcon
                        )
                    )
                }
            }

            //Add default apps
            setNotificationState(1, true)
            //Refresh the app list
            setNotificationState(3, true)

            notificationIconAdapter = NotificationIconAdapter(this, apps)
            app_notification_rv.adapter = notificationIconAdapter
            app_notification_rv.layoutManager = LinearLayoutManager(this)
            notificationIconAdapter.setOnItemClickListener(object :
                NotificationIconAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    mClickedApp = apps[position]
                    app_notification_pop_ll.visibility = View.VISIBLE

                    //Allow notifications
                    app_notification_pop_allow.setOnClickListener {
                        loadingManager.show()
                        mClickedApp?.status = 1
                        setNotificationState(2, false)
                    }
                    //Silent notifications
                    app_notification_pop_silent.setOnClickListener {
                        loadingManager.show()
                        mClickedApp?.status = 2
                        setNotificationState(2, false)
                    }
                    //Turn off notifications
                    app_notification_pop_clos.setOnClickListener {
                        loadingManager.show()
                        mClickedApp?.status = 3
                        setNotificationState(2, false)
                    }
                    //Quick reply notification
                    app_notification_pop_sendMsg_quick.setOnClickListener {
                        loadingManager.show()
                        sendMsg(app_notification_pop_content.text.toString(), 1)
                    }
                    //Send a normal notification
                    app_notification_pop_sendMsg.setOnClickListener {
                        loadingManager.show()
                        sendMsg(app_notification_pop_content.text.toString(), 0)
                    }

                }
            })
        }

        app_notification_pop_clos_back.setOnClickListener {
            app_notification_pop_ll.visibility = View.GONE
        }

        //Get the language library supported by the device
        if (sdk.funcTable.getSupportGetUnit) {
            getLanguageLibrary()
        }
    }


    private fun getLanguageLibrary() {
        Cmds.getUnit().send { it ->
            if (it.error.code == 0) {
                // Succeeded
                Log.d(TAG, "getLanguageLibrary: ${it.res?.language}")

            } else {
                // Failed
                Log.d(TAG, "getLanguageLibrary: ${it.res?.toJsonString()}")
            }
        }
    }




    /**
     * msg: content to send
     * msgID: 0->normal notification  1:quick reply message
     *
     * */
    private fun sendMsg(msg: String, msgID: Int) {

        if (msgID != 0) {

            val param = IDOFastMsgSettingModel(
                1, listOf(
                    IDOFastMsgItem(1, "ok"),
                )
            )
            //Set the default quick message reply list
            Cmds.setDefaultQuickMsgReplyList(param).send {
                if (it.error.code == 0) {
                    // Succeeded
                    // it.res is IDOCmdSetResponseModel

                } else {
                    // Failed
                    toast("Failed to set the quick message reply list")
                }
            }
        }

        /**
         * Each time a notification message is sent, execute the event type
         * The event type is the one obtained from items above; each event type is associated with an APP, so the event type tells which app the notification belongs to
         * Send this event type to check whether the APP icon has been updated; if not, the SDK sends the corresponding APP icon
         * For the entity class parameters, refer to the development documentation
         *
         * verison	Int	Protocol library version number
         * osPlatform	Int	System 0: invalid, 1: Android, 2: iOS
         * evtType	Int	Current mode 0: invalid, 1: message reminder
         * notifyType	Int	Message enum type, max: 20000
         * msgID	Int	Message ID, valid only when evt_type is message reminder and msg_ID is not 0
         * appItemsLen	Int	Number of countries/regions and language details
         * contact	String	Contact name (max 63 bytes)
         * phoneNumber	String	Phone number (max 31 bytes)
         * msgData	String	Message content (max 249 bytes)
         * items: IDONoticeMesaageParamItem language list
         *      language	Int	Language type
         *      name	String	App name for the country (max 49 bytes)
         * */
        sdk.messageIcon.androidSendMessageIconToDevice(eventType = mClickedApp?.evtType!!) { success ->
            Cmds.setNoticeAppName(
                IDONoticeMesaageParamModel(
                    1,
                    1,
                    mClickedApp?.evtType!!,
                    msgID,
                    1,
                    mClickedApp?.appName!!,
                    "",
                    msg,
                    listOf(
                        //The language type and app name here must be changed according to the actual situation
                        IDONoticeMesaageParamItem(2, mClickedApp?.appName!!)
                    )
                )
            ).send {
                if (it.error.code == 0) {
                    loadingManager.dismiss()
                    app_notification_pop_ll.visibility = View.GONE
                    toast("Send succeeded")
                } else {
                    toast("Send failed")
                }
            }
        }

    }


    private fun setNotificationState(operat: Int, first: Boolean) {
        var idoNoticeMessageStateItemItem = mutableListOf<IDONoticeMessageStateItemItem>()
        //Whether entering for the first time
        if (first) {
            //Change the status of default supported apps
            apps.forEach {
                if (it.status == 1) {
                    idoNoticeMessageStateItemItem.add(
                        IDONoticeMessageStateItemItem(it.evtType!!, it.status!!, 0)
                    )
                }
            }
        } else {
            //Update the status of a single selected app
            idoNoticeMessageStateItemItem.add(
                IDONoticeMessageStateItemItem(mClickedApp?.evtType!!, mClickedApp?.status!!, 0)
            )
        }
        /**
         * Dynamic notification operation
         * operat: 1: add 2:modify 3:query
         * allOnOff:
         *      Add and modify only
         *      Master notification switch
         *      1: turn on all notifications
         *      0: turn off all notifications
         * allSendNum:
         *      Total number of packets to send
         *      Sent in batches if more than 100 packets
         *      all_send_num = now_send_index means sending is complete
         * nowSendIndex:current send index
         * items:	IDONoticeMessageStateItem collection
         *          evtType: event number
         *          notifyState: notification status  1: allow notifications 2: silent notifications 3: turn off notifications
         *          picFlag: applies when replying, set this parameter to 0  0: invalid  1: download the corresponding image  2: no corresponding image
         * */
        Cmds.setNoticeMessageState(
            IDONoticeMessageStateParamModel(
                idoNoticeMessageStateItemItem.size,
                operat,
                3,
                idoNoticeMessageStateItemItem.size,
                1,
                1,
                idoNoticeMessageStateItemItem
            )
        ).send { it ->
            loadingManager.dismiss()
            if (it.res?.errCode == 0) {
                //Query the app list once after each status change to refresh the list status
                if (operat != 3) {
                    setNotificationState(3, false)
                } else {
                    //Refresh the current app notification status from the status list
                    for (item1 in apps) {
                        val matchingItem = it.res?.items?.find { it.evtType == item1.evtType }
                        if (matchingItem != null) {
                            item1.status = matchingItem.notifyState
                        }
                    }
                    notificationIconAdapter.notifyDataSetChanged()
                    app_notification_pop_ll.visibility = View.GONE
                    toast("Status change succeeded")

                }
            } else {
                toast("Status change failed")
                app_notification_pop_ll.visibility = View.GONE
            }
        }


    }
}