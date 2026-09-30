package com.example.example_android.activity

import androidx.appcompat.app.AlertDialog
import com.example.example_android.base.BaseActivity
import com.example.example_android.R
import com.example.example_android.data.CustomEvtType
import com.example.example_android.data.GetFuntionData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.idosmart.pigeon_implement.Cmds
import com.idosmart.pigeon_implement.IDODeviceFileToAppTask
import com.idosmart.protocol_channel.sdk
import com.idosmart.protocol_sdk.IDOCancellable
import com.idosmart.protocol_sdk.IDOCmdPriority
import com.idosmart.model.IDOBatteryReminderSwitchModel
import com.idosmart.model.IDOBikeLockReplyModel
import com.idosmart.model.IDODefaultSportTypeModel
import com.idosmart.model.IDODownloadLanguageModel
import com.idosmart.model.IDOFindPhoneSwitchModel
import com.idosmart.model.IDOHeartModeModel
import com.idosmart.model.IDOHeartModeParamModel
import com.idosmart.model.IDOHeartRateModeSmartModel
import com.idosmart.model.IDOMenuListV3Model
import com.idosmart.model.IDOPetInfoModel
import com.idosmart.model.IDOSchedulerReminderParamModel
import com.idosmart.model.IDOSpo2SwitchModel
import com.idosmart.model.IDOSportingRemindSettingReplyModel
import com.idosmart.model.IDOStressSwitchModel
import com.idosmart.model.IDOUnitModel
import com.idosmart.model.IDOWatchListV2Model
import com.idosmart.model.IDOAppletControlModel
import kotlinx.android.synthetic.main.layout_comme_send_data.*

class GetFunctionDetailActivity : BaseActivity() {
    private var getFunctionData: GetFuntionData? = null
    private var mIDOCancellable: IDOCancellable? = null
    override fun getLayoutId(): Int {
        return R.layout.layout_comme_send_data
    }

    override fun initView() {
        super.initView()
        getFunctionData =
            intent.getSerializableExtra(GetFuntionActivity.FUNCTION_DATA) as GetFuntionData?
        supportActionBar?.title = getFunctionData?.title
        val myType = getFunctionData?.type ?: return
        send_btn.setOnClickListener {

            //Perform different operations based on the event type passed in from the function list
            //Different operations are performed depending on the type of event passed in from the function list
            when (myType) {
                CustomEvtType.GETDEVICEINFO -> getDeviceInfo()
                CustomEvtType.GETACTIVITYSWITCH -> getActivitySwitch()
                CustomEvtType.GETALARMV3 -> getAlarm()
                CustomEvtType.GETALLHEALTHSWITCHSTATE -> getAllHealthSwitchState()
                CustomEvtType.GETBATTERYINFO -> getBatteryInfo()
                CustomEvtType.GETBLEBEEPV3 -> getBleBeep()
                CustomEvtType.GETBLEMUSICINFO -> getBleMusicInfo()
                CustomEvtType.GETBPALGVERSION -> getBpAlgVersion()
                CustomEvtType.GETBTNOTICE -> getBtNotice()
                CustomEvtType.GETCONTACTREVISETIME -> getContactReviseTime()
                CustomEvtType.GETDEVICELOGSTATE -> getDeviceLogState()
                CustomEvtType.GETDOWNLANGUAGE -> getDownloadLanguage()
                CustomEvtType.GETERRORRECORD -> getErrorRecord()
                CustomEvtType.GETFLASHBININFO -> getFlashBinInfo()
                CustomEvtType.GETGPSINFO -> getGpsInfo()
                CustomEvtType.GETGPSSTATUS -> getGpsStatus()
                CustomEvtType.GETHABITINFOV3 -> getHabitInfo()
                CustomEvtType.GETHEARTRATEMODE -> getHeartRateMode()
                CustomEvtType.GETHOTSTARTPARAM -> getHotStartParam()
                CustomEvtType.GETLANGUAGELIBRARYDATAV3 -> getLanguageLibrary()
                CustomEvtType.GETMENULIST -> getMenuList()
                CustomEvtType.GETMTUINFO -> getMtuInfo()
                CustomEvtType.GETNOTDISTURBSTATUS -> getNotDisturbStatus()
                CustomEvtType.GETNOTICESTATUS -> getNoticeStatus()
                CustomEvtType.GETSCREENBRIGHTNESS -> getScreenBrightness()
                CustomEvtType.GETSTEPGOAL -> getStepGoal()
                CustomEvtType.GETSTRESSVAL -> getStressVal()
                CustomEvtType.GETSUPPORTMAXSETITEMSNUM -> getSupportMaxSetItemsNum()
                CustomEvtType.GETUNERASABLEMEUNLIST -> getUnerasableMeunList()
                CustomEvtType.GETUNREADAPPREMINDER -> getUnreadAppReminder()
                CustomEvtType.GETUPDATESTATUS -> getUpdateStatus()
                CustomEvtType.GETUPHANDGESTURE -> getUpHandGesture()
                CustomEvtType.GETVERSIONINFO -> getVersionInfo()
                CustomEvtType.GETWALKREMIND -> getWalkRemind()
                CustomEvtType.GETWATCHDIALID -> getWatchDialId()
                CustomEvtType.GETWATCHDIALINFO -> getWatchDialInfo()
                CustomEvtType.GETWATCHFACELIST -> getWatchListV2()
                CustomEvtType.GETWATCHLISTV3 -> getWatchListV3()
                CustomEvtType.GETLIVEDATA -> getLiveData(1)
                CustomEvtType.GETMAINSPORTGOAL -> getMainSportGoal(0)
                CustomEvtType.OTASTART -> otaStart()
                CustomEvtType.REBOOT -> reboot()
                CustomEvtType.FACTORYRESET -> getFactoryreset()
                CustomEvtType.FINDDEVICESTART -> getFindDeviceStart()
                CustomEvtType.FINDDEVICESTOP -> getFindDeviceStop()
                CustomEvtType.GETDEFAULTSPORTTYPE -> getDefaultSportType()
                CustomEvtType.GETSPORTTYPEV3 -> getSportTypeV3()
                //These methods differ slightly from the logic above; take care when using them and follow the provided code logic
                //These methods have a little difference from the above logic, please note when using, refer to the code logic written
                CustomEvtType.GETHIDINFO -> getHidInfo()
                CustomEvtType.GETSNINFO -> getSn()
                CustomEvtType.GETBTCONNECTPHONEMODEL -> getBtConnectPhoneModel()
                CustomEvtType.GETBTNAME -> getBtName()
                CustomEvtType.GETDEVICENAME -> getDeviceName()
                CustomEvtType.GETALGFILE-> getAlgFile()
                CustomEvtType.REQUESTALGFILE-> requestAlgFile()
                CustomEvtType.GETLEFTRIGHTWEARSETTINGS -> getLeftRightWearSettings()
                CustomEvtType.GETSETTINGSDURINGEXERCISE -> getSettingsDuringExercise()
                CustomEvtType.GETSIMPLEHEARTRATEZONE -> getSimpleHeartRateZone()
                CustomEvtType.SETSPORTINGREMINDSETTING -> getSportingRemindSetting()
                CustomEvtType.GETUSERINFO -> getUserInfo()
                            CustomEvtType.GETFUNCTIONTABLE -> getFunctionTable()
            CustomEvtType.GETBATTERYREMINDERSWITCH -> getBatteryReminderSwitch()
            CustomEvtType.GETAPPSLEEPMODE -> getAppSleepMode()
            CustomEvtType.GETFINDPHONESWITCH -> getFindPhoneSwitch()
            CustomEvtType.GETFINDMYDEVICENAME -> getFindMyDeviceName()
            CustomEvtType.GETSPORTAUTOPAUSEENDTYPES -> getSportAutoPauseEndTypes()
            CustomEvtType.GETPETINFO -> getPetInfo()
            CustomEvtType.GETDOWNLOADLANGUAGE -> getDownloadLanguage()
            CustomEvtType.GETUNIT -> getUnit()
            CustomEvtType.GETAPPLETCONTROL -> getAppletControl()
            CustomEvtType.GETSPORTSTYPEV3 -> getSportTypeV3()
            CustomEvtType.SETSCHEDULEREMINDER -> setScheduleReminder() 
            CustomEvtType.GETSPORTINGREMINDSETTING -> getSportingRemindSetting()
            CustomEvtType.GETSMARTHEARTRATEMODE -> getSmartHeartRateMode()
            CustomEvtType.GETSPO2SWITCH -> getSpo2Switch()
            CustomEvtType.GETSTRESSSWITCH -> getStressSwitch()
            CustomEvtType.GETBIKELOCKLIST -> getBikeLockList()
            CustomEvtType.GETMENULISTV3 -> getMenuListV3()
            CustomEvtType.GETWATCHLISTV2 -> getWatchListV2()
            CustomEvtType.GETHEARTMODE -> getHeartMode()
            else -> {}
            }
        }

        sdk.transfer.registerDeviceTranFileToApp {
            deviceTransFileToApp(it)
        }
    }

    private fun getUserInfo() {
        Cmds.getUserInfo().send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getSportingRemindSetting(){
        val param = listOf(48)
        Cmds.getSportingRemindSetting(param).send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = GsonBuilder().create().toJson(param).toString()
    }

    private fun getSimpleHeartRateZone(){
        Cmds.getSimpleHeartRateZone().send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = "{}"
    }
    private fun getSettingsDuringExercise(){
        Cmds.getSettingsDuringExercise().send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getLeftRightWearSettings(){
        Cmds.getLeftRightWearSettings().send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun deviceTransFileToApp(task: IDODeviceFileToAppTask) {
        println("Received file transferred from device, name: ${task.deviceTransItem.fileName} size: ${task.deviceTransItem.fileSize}, to App")
                task.acceptReceiveFile(onProgress = { progress ->
                    // Handle progress updates here
                    println("Progress: $progress")
                    tv_response?.text ="Progress: $progress"
                },
                    onComplete = { isCompleted, receiveFilePath ->
                        // Handle completion here
                        if (isCompleted) {
                            println("File received successfully at: $receiveFilePath")
                            tv_response?.text ="File received successfully at: $receiveFilePath"
                        } else {
                            tv_response?.text ="File transfer failed."

                        }
                    })
                /*
                // Note: Receiving can be terminated at any time
                // Abort receiving file
                task.stopReceiveFile { rs ->
                    println("Stopped receiving file ${task.deviceTransItem.fileName)")
                }
                 */
            }

    /**
     * Request firmware algorithm file information (ACC/GPS)
     * type: 1:ACC file , 2:GPS file
     *
     * */
    private fun requestAlgFile() {
        var rquestAlgFile = Cmds.rquestAlgFile(2)
        rquestAlgFile.send {
            if (it.error.code==0){
                if (it.error.code == 0) {
                    val res = it.res?.toJsonString() ?: "{ok}"
                    tv_response?.text = res
                } else {
                    val res = it.res?.toJsonString() ?: "error code:${it.error.code} - ${it.error.message}"
                    tv_response?.text = "erro: $res"
                }
            }
        }
        paramter_tv?.text = rquestAlgFile?.json
    }

    /**
     * Get firmware algorithm file information (ACC/GPS)
     *
     * */
    private fun getAlgFile() {
        var algFileInfo = Cmds.getAlgFileInfo()
        algFileInfo.send {
            if (it.error.code==0){
                if (it.error.code == 0) {
                    val res = it.res?.toJsonString() ?: "{ok}"
                    tv_response?.text = res
                } else {
                    val res = it.res?.toJsonString() ?: "error code:${it.error.code} - ${it.error.message}"
                    tv_response?.text = "erro: $res"
                }
            }
        }
        paramter_tv?.text = algFileInfo?.json
    }

    /**
     *  v3 app obtains the ble alarm clock
     * */
    private fun getAlarm() {
        var alarm = Cmds.getAlarm(IDOCmdPriority.HIGH)
        alarm.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = alarm?.json
    }


    /**
     * Access all health monitoring switches
     * */
    private fun getAllHealthSwitchState() {
        var allHealthSwitchState = Cmds.getAllHealthSwitchState()
        allHealthSwitchState.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = allHealthSwitchState?.json
    }


    /**
     * Get Battery Information
     * */
    private fun getBatteryInfo() {
        var batteryInfo = Cmds.getBatteryInfo()
        batteryInfo.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = batteryInfo?.json
    }


    /**
     * v3 obtains firmware local tone file information
     * */
    private fun getBleBeep() {
        var bleBeep = Cmds.getBleBeep()
        bleBeep.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = bleBeep?.json
    }

    /**
     * Get firmware for song names and folders
     * */
    private fun getBleMusicInfo() {
        var bleMusicInfo = Cmds.getBleMusicInfo()
        bleMusicInfo.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = bleMusicInfo?.json
    }

    /**
     * Get blood pressure algorithm version 3 information event number
     * */
    private fun getBpAlgVersion() {
        var bpAlgVersion = Cmds.getBpAlgVersion()
        bpAlgVersion.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = bpAlgVersion?.json
    }


    /**
     * Query the status of bt pairing switch, connection, a2dp connection, hfp connection (only for devices with bt Bluetooth)
     * */
    private fun getBtNotice() {
        var btNotice = Cmds.getBtNotice()
        btNotice.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = btNotice?.json
    }

    /**
     * Obtain the modification time of the locally saved contact file in the firmware
     * */
    private fun getContactReviseTime() {
        var contactReviseTime = Cmds.getContactReviseTime()
        contactReviseTime.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = contactReviseTime?.json
    }

    /**
     * Obtain the log status of the device
     * */
    private fun getDeviceLogState() {
        var deviceLogState = Cmds.getDeviceLogState()
        deviceLogState.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = deviceLogState?.json
    }

    /**
     * Get download language support
     * */
    private fun getDownloadLanguage() {
        var downloadLanguage = Cmds.getDownloadLanguage()
        downloadLanguage.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = downloadLanguage?.json
    }

    /**
     * Get error record
     * */
    private fun getErrorRecord() {
        var errorRecord = Cmds.getErrorRecord()
        errorRecord.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = errorRecord?.json
    }

    /**
     * Get font information
     * */
    private fun getFlashBinInfo() {
        var flashBinInfo = Cmds.getFlashBinInfo()
        flashBinInfo.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = flashBinInfo?.json
    }


    /**
     * Get gps information
     * */
    private fun getGpsInfo() {
        var gpsInfo = Cmds.getGpsInfo()
        gpsInfo.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = gpsInfo?.json
    }

    /**
     * Get gps status
     * */
    private fun getGpsStatus() {
        var gpsStatus = Cmds.getGpsStatus()
        gpsStatus.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = gpsStatus?.json
    }

    /**
     * v3 obtains firmware local tone file information
     * */
    private fun getHabitInfo() {
        var habitInfo = Cmds.getHabitInfo()
        habitInfo.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = habitInfo?.json
    }


    /**
     * Get heart rate monitoring mode
     * */
    private fun getHeartRateMode() {
        var heartRateMode = Cmds.getHeartRateMode()
        heartRateMode.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = heartRateMode?.json
    }

    /**
     * Obtain hot start parameters
     * */
    private fun getHotStartParam() {
        var hotStartParam = Cmds.getHotStartParam()
        hotStartParam.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = hotStartParam?.json
    }

    /**
     * v3 Gets the device font list
     * */
    private fun getLanguageLibrary() {
        var languageLibrary = Cmds.getLanguageLibrary()
        languageLibrary.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = languageLibrary?.json
    }

    /**
     * Gets a list of supported devices
     * */
    private fun getMenuList() {
        var menuList = Cmds.getMenuList()
        menuList.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = menuList?.json
    }

    /**
     * Obtaining mtu Information
     * */
    private fun getMtuInfo() {
        var mtuInfo = Cmds.getMtuInfo()
        mtuInfo.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = mtuInfo?.json
    }

    /**
     * Gets the status of the notification center
     * */
    private fun getNoticeStatus() {
        var noticeStatus = Cmds.getNoticeStatus()
        noticeStatus.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = noticeStatus?.json
    }

    /**
     * Get the Do not disturb mode status
     * */
    private fun getNotDisturbStatus() {
        var notDisturbStatus = Cmds.getNotDisturbStatus()
        notDisturbStatus.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = notDisturbStatus?.json
    }

    /**
     * Get screen brightness
     * */
    private fun getScreenBrightness() {
        var screenBrightness = Cmds.getScreenBrightness()
        screenBrightness.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = screenBrightness?.json
    }

    /**
     * Get an all-day step goal
     * */
    private fun getStepGoal() {
        var stepGoal = Cmds.getStepGoal()
        stepGoal.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = stepGoal?.json
    }


    /**
     * Obtain pressure value
     * */
    private fun getStressVal() {
        var stressVal = Cmds.getStressVal()
        stressVal.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = stressVal?.json
    }

    /**
     * Get maximum number of Settings supported by firmware details
     * */
    private fun getSupportMaxSetItemsNum() {
        var supportMaxSetItemsNum = Cmds.getSupportMaxSetItemsNum()
        supportMaxSetItemsNum.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = supportMaxSetItemsNum?.json
    }


    /**
     * Obtain a list of shortcut applications whose firmware cannot be deleted
     * */
    private fun getUnerasableMeunList() {
        var unerasableMeunList = Cmds.getUnerasableMeunList()
        unerasableMeunList.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = unerasableMeunList?.json
    }

    /**
     * Get the red dot reminder switch
     * */
    private fun getUnreadAppReminder() {
        var unreadAppReminder = Cmds.getUnreadAppReminder()
        unreadAppReminder.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = unreadAppReminder?.json
    }


    /**
     * Obtain the upgrade status of the device
     * */
    private fun getUpdateStatus() {
        var updateStatus = Cmds.getUpdateStatus()
        updateStatus.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = updateStatus?.json
    }


    /**
     * Obtain wrist lift data
     * */
    private fun getUpHandGesture() {
        var upHandGesture = Cmds.getUpHandGesture()
        upHandGesture.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = upHandGesture?.json
    }

    /**
     * Get version information
     * */
    private fun getVersionInfo() {
        var versionInfo = Cmds.getVersionInfo()
        versionInfo.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = versionInfo?.json
    }

    /**
     * Get a walk alert
     * */
    private fun getWalkRemind() {
        var walkRemind = Cmds.getWalkRemind()
        walkRemind.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = walkRemind?.json
    }

    /**
     * Gets the dial id
     * */
    private fun getWatchDialId() {
        var watchDialId = Cmds.getWatchDialId()
        watchDialId.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = watchDialId?.json
    }

    /**
     * Get screen information
     * */
    private fun getWatchDialInfo() {
        var watchDialInfo = Cmds.getWatchDialInfo()
        watchDialInfo.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = watchDialInfo?.json
    }

    /**
     * V2 Obtains the dial list
     *
     * */
    private fun getWatchListV2() {
        var watchListV2 = Cmds.getWatchListV2()
        watchListV2.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = watchListV2?.json
    }

    /**
     * v3 Obtains the dial list
     * */
    private fun getWatchListV3() {
        var watchListV3 = Cmds.getWatchListV3()
        watchListV3.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = watchListV3?.json
    }

    /**
     * Get real-time data
     * flag :
     * */
    private fun getLiveData(flag: Int) {
        var liveData = Cmds.getLiveData(flag)
        liveData.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = liveData?.json
    }


    /**
     * Get the calorie/distance/medium/high exercise duration set main screen
     * timeGoalType:
     * */
    private fun getMainSportGoal(timeGoalType: Int) {
        var mainSportGoal = Cmds.getMainSportGoal(timeGoalType)
        mainSportGoal.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = mainSportGoal?.json
    }

    /**
     * Enter upgrade mode
     * */
    private fun otaStart() {
        var otaStart = Cmds.otaStart()
        otaStart.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = otaStart?.json
    }

    /**
     * Restart the device
     * */
    private fun reboot() {
        var reboot = Cmds.reboot()
        reboot.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = reboot?.json
    }

    /**
     * v3 Gets the default type of motion
     * */
    private fun getSportTypeV3() {
        var sportTypeV3 = Cmds.getSportTypeV3()
        sportTypeV3.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = sportTypeV3?.json
    }

    /**
     * get Activity Switch
     * */
    private fun getActivitySwitch() {
        var activitySwitch = Cmds.getActivitySwitch()
        activitySwitch.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = activitySwitch?.json
    }

    /**
     * get Activity Switch
     * */
    private fun getDefaultSportType() {
        var defaultSportType = Cmds.getDefaultSportType()
        defaultSportType.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = defaultSportType?.json
    }

    /**
     * factory data reset
     * */
    private fun getFactoryreset() {
        var factoryReset = Cmds.factoryReset()
        factoryReset.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = factoryReset?.json
    }

    /**
     * Start looking for equipment
     * */
    private fun getFindDeviceStart() {
        var findDeviceStart = Cmds.findDeviceStart()
        findDeviceStart.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ ok }"
                tv_response?.text = res
            } else {
                val res = it.res?.toJsonString() ?: "{ erro }"
                tv_response?.text = "erro: $res"
            }
        }
        paramter_tv?.text = findDeviceStart?.json
    }

    /**
     * Stop looking for equipment
     * */
    private fun getFindDeviceStop() {
        var findDeviceStop = Cmds.findDeviceStop()
        findDeviceStop.send {
            if (it.error.code == 0) {
                val res = it.res?.toJsonString() ?: "{ok}"
                tv_response?.text = res
            } else {
                tv_response?.text = "err code:${it.error.code} - ${it.error.message?:""} ${it.res?.toJsonString()?:""}"
            }
        }
        paramter_tv?.text = findDeviceStop?.json
    }

    /**
     * get hid information
     * */
    private fun getHidInfo() {
        var hidInfo = Cmds.getHidInfo()
        hidInfo.send {
            if (it.error.code == 0) {
                val res = it.res?: "{ok}"
                tv_response?.text = res.toString()
            } else {
                val res = it.res?: "{erro}"
                tv_response?.text = "erro: $res"
            }
        }
        paramter_tv?.text = hidInfo?.json
    }
    /**
     * get sn
     * */
    private fun getSn() {
        var sn = Cmds.getSn()
        sn.send {
            if (it.error.code == 0) {
                val res = it.res?: "{ok}"
                tv_response?.text = res.toString()
            } else {
                val res = it.res?: "{erro}"
                tv_response?.text = "erro: $res"
            }
        }
        paramter_tv?.text = sn?.json
    }

    /**
     * Get BT connected phone model
     * */
    private fun getBtConnectPhoneModel() {
        var btConnectPhoneModel = Cmds.getBtConnectPhoneModel()
        btConnectPhoneModel.send {
            if (it.error.code == 0) {
                val res = it.res?: "{ok}"
                tv_response?.text = res.toString()
            } else {
                val res = it.res?: "{erro}"
                tv_response?.text = "erro: $res"
            }
        }
        paramter_tv?.text = btConnectPhoneModel?.json
    }


    /**
     * Get the bt Bluetooth name
     * */
    private fun getBtName() {
        var btName = Cmds.getBtName()
        btName.send {
            if (it.error.code == 0) {
                val res = it.res?: "{ok}"
                tv_response?.text = res.toString()
            } else {
                val res = it.res?: "{erro}"
                tv_response?.text = "erro: $res"
            }
        }
        paramter_tv?.text = btName?.json
    }



    /**
     * get watch name
     * */
    private fun getDeviceName() {
        var deviceName = Cmds.getDeviceName()
        deviceName.send {
            if (it.error.code == 0) {
                val res = it.res?: "{ok}"
                tv_response?.text = res.toString()
            } else {
                val res = it.res?: "{erro}"
                tv_response?.text = "erro: $res"
            }
        }
        paramter_tv?.text = deviceName?.json
    }


    override fun onDestroy() {
        super.onDestroy()
        if (mIDOCancellable != null && !mIDOCancellable!!.isCancelled) {
            mIDOCancellable?.cancel()
        }
    }
    private fun getFunctionTable() {
        tv_response?.text = sdk.funcTable.toJsonString()
        paramter_tv?.text = "{}"
    }

    private fun getDeviceInfo() {
        tv_response?.text = sdk.device.toJsonString()
        paramter_tv?.text = "{}"
    }

    private fun getBatteryReminderSwitch() {
        Cmds.getBatteryReminderSwitch().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getAppSleepMode() {
        Cmds.getAppSleepMode().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getFindPhoneSwitch() {
        Cmds.getFindPhoneSwitch().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getFindMyDeviceName() {
        Cmds.getFindMyDeviceName().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{\"operate\":2}"
    }

    private fun getSportAutoPauseEndTypes() {
        Cmds.getSportAutoPauseEndTypes().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{\"operate\":3}"
    }

    private fun getPetInfo() {
        Cmds.getPetInfo().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getUnit() {
        Cmds.getUnit().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getAppletControl() {
        val model = IDOAppletControlModel(operate = 3, "")
        val cmd = Cmds.setAppletControl(model)
        cmd.send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = cmd.json
    }

    private fun setScheduleReminder() {
        val model = IDOSchedulerReminderParamModel(operate = 3, items = listOf())
        val cmd = Cmds.setSchedulerReminder(model)
        cmd.send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = cmd.json
    }

    private fun getSmartHeartRateMode() {
        Cmds.getSmartHeartRateMode().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getSpo2Switch() {
        Cmds.getSpo2Switch().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getStressSwitch() {
        Cmds.getStressSwitch().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getBikeLockList() {
        Cmds.getBikeLockList().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getMenuListV3() {
        Cmds.getMenuListV3().send {
            if (it.error.code == 0) {
                tv_response?.text = it.res?.toJsonString() ?: "{ok}"
            } else {
                tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
            }
        }
        paramter_tv?.text = "{}"
    }

    private fun getHeartMode() {
        if (sdk.funcTable.setSmartHeartRate) {
            if (sdk.funcTable.getSupportGetSmartHeartRate) {
                val model = IDOHeartModeParamModel(updateTime = 0)
                val cmd = Cmds.setHeartMode(model)
                cmd.send {
                    if (it.error.code == 0) {
                        tv_response?.text = it.res?.toJsonString() ?: "{ok}"
                    } else {
                        tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
                    }
                }
                paramter_tv?.text = cmd.json
            } else {
                tv_response?.text = "Device not support get heart rate mode"
            }
        } else if (sdk.funcTable.syncV3Hr) {
            val model = IDOHeartModeParamModel(updateTime = 0)
            val cmd = Cmds.setHeartMode(model)
            cmd.send {
                if (it.error.code == 0) {
                    tv_response?.text = it.res?.toJsonString() ?: "{ok}"
                } else {
                    tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
                }
            }
            paramter_tv?.text = cmd.json
        } else if (sdk.funcTable.getHeartRateModeV2) {
            Cmds.getHeartRateMode().send {
                if (it.error.code == 0) {
                    tv_response?.text = it.res?.toJsonString() ?: "{ok}"
                } else {
                    tv_response?.text = "error code: ${it.error.code} - ${it.error.message}"
                }
            }
            paramter_tv?.text = "{}"
        }
    }

}