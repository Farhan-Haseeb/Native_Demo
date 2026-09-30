package com.example.example_android.activity

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.example_android.base.BaseActivity
import com.example.example_android.R
import com.example.example_android.data.CmdSet
import com.example.example_android.data.CustomEvtType
import com.example.example_android.data.SetFuncData
import com.google.gson.GsonBuilder
import com.idosmart.model.IDOActivitySwitchParamModel
import com.idosmart.model.IDOAlarmItem
import com.idosmart.model.IDOAlarmModel
import com.idosmart.model.IDOAlarmStatus
import com.idosmart.model.IDOAlarmType
import com.idosmart.model.IDOAppletControlModel
import com.idosmart.model.IDOBaseModel
import com.idosmart.model.IDOBleVoiceParamModel
import com.idosmart.model.IDOBpCalControlModel
import com.idosmart.model.IDOBpCalibrationParamModel
import com.idosmart.model.IDOBpMeasurementParamModel
import com.idosmart.model.IDOContactItem
import com.idosmart.model.IDODateTimeParamModel
import com.idosmart.model.IDODisplayModeParamModel
import com.idosmart.model.IDODrinkWaterRemindModel
import com.idosmart.model.IDOFastMsgItem
import com.idosmart.model.IDOFastMsgSettingModel
import com.idosmart.model.IDOFastMsgUpdateParamModel
import com.idosmart.model.IDOFitnessGuidanceParamModel
import com.idosmart.model.IDOFutureItem
import com.idosmart.model.IDOGpsControlParamModel
import com.idosmart.model.IDOGpsInfoModelItem
import com.idosmart.model.IDOHandWashingReminderParamModel
import com.idosmart.model.IDOHeartModeParamModel
import com.idosmart.model.IDOHeartRateIntervalModel
import com.idosmart.model.IDOHeartRateModeSmartParamModel
import com.idosmart.model.IDOHistoricalMenstruationParamItem
import com.idosmart.model.IDOHistoricalMenstruationParamModel
import com.idosmart.model.IDOHoursWeatherItem
import com.idosmart.model.IDOItemItem
import com.idosmart.model.IDOLongSitParamModel
import com.idosmart.model.IDOLostFindParamModel
import com.idosmart.model.IDOMainSportGoalModel
import com.idosmart.model.IDOMainUISortParamModel
import com.idosmart.model.IDOMenstruationModel
import com.idosmart.model.IDOMenstruationRemindParamModel
import com.idosmart.model.IDOMenuListParamModel
import com.idosmart.model.IDOMusicControlParamModel
import com.idosmart.model.IDOMusicFolderItem
import com.idosmart.model.IDOMusicOnOffParamModel
import com.idosmart.model.IDOMusicOpearteParamModel
import com.idosmart.model.IDONotDisturbParamModel
import com.idosmart.model.IDONoticeMesaageParamItem
import com.idosmart.model.IDONoticeMesaageParamModel
import com.idosmart.model.IDONoticeMessageParamModel
import com.idosmart.model.IDONoticeMessageStateParamModel
import com.idosmart.model.IDONotificationStatusParamModel
import com.idosmart.model.IDORunPlanParamModel
import com.idosmart.model.IDOSchedulerReminderItem
import com.idosmart.model.IDOSchedulerReminderParamModel
import com.idosmart.model.IDOScientificSleepSwitchParamModel
import com.idosmart.model.IDOScreenBrightnessModel
import com.idosmart.model.IDOShortcutParamModel
import com.idosmart.model.IDOSleepPeriodParamModel
import com.idosmart.model.IDOSpo2SwitchParamModel
import com.idosmart.model.IDOSport100SortParamModel
import com.idosmart.model.IDOSportGoalParamModel
import com.idosmart.model.IDOSportModeSelectParamModel
import com.idosmart.model.IDOSportModeSortParamModel
import com.idosmart.model.IDOSportParamModel
import com.idosmart.model.IDOSportSortParamModel
import com.idosmart.model.IDOSportType
import com.idosmart.model.IDOStressCalibrationParamModel
import com.idosmart.model.IDOStressSwitchParamModel
import com.idosmart.model.IDOSunriseItem
import com.idosmart.model.IDOSyncContactParamModel
import com.idosmart.model.IDOTakingMedicineReminderParamModel
import com.idosmart.model.IDOTemperatureSwitchParamModel
import com.idosmart.model.IDOUnitParamModel
import com.idosmart.model.IDOUpHandGestureParamModel
import com.idosmart.model.IDOUserInfoPramModel
import com.idosmart.model.IDOV3NoiseParamModel
import com.idosmart.model.IDOVoiceReplyParamModel
import com.idosmart.model.IDOWalkRemindModel
import com.idosmart.model.IDOWalkRemindTimesParamModel
import com.idosmart.model.IDOWallpaperDialReplyV3ParamModel
import com.idosmart.model.IDOWatchDialParamModel
import com.idosmart.model.IDOWatchDialSortItem
import com.idosmart.model.IDOWatchDialSortParamModel
import com.idosmart.model.IDOWatchFaceParamModel
import com.idosmart.model.IDOWeatherDataFuture
import com.idosmart.model.IDOWeatherDataParamModel
import com.idosmart.model.IDOWeatherSunTimeParamModel
import com.idosmart.model.IDOWeatherV3ParamModel
import com.idosmart.model.IDOWeek
import com.idosmart.model.IDOWorldTimeParamModel
import com.idosmart.pigeon_implement.Cmds
import kotlinx.android.synthetic.main.layout_comme_send_data.*
import kotlinx.android.synthetic.main.layout_comme_send_data.view.*
import java.time.LocalDateTime

class SetFunctionDetailActivity : BaseActivity() {
    private var type: Int = SetFuntionActivity.setDateTime
    private var setFuncData: SetFuncData? = null
    override fun getLayoutId(): Int {
        return R.layout.layout_comme_send_data
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        super.initView()
        setFuncData =
            intent.getSerializableExtra(SetFuntionActivity.SET_FUNCTION_DATA) as SetFuncData
        supportActionBar?.setTitle(setFuncData?.title)
        val myType = setFuncData?.type

        send_btn.setOnClickListener {
            when (myType) {
                CustomEvtType.SETALARMV3 -> setAlarmV3()
                CustomEvtType.SETWATCHDIALSORT -> setWatchDialSort()
                CustomEvtType.SETSENDRUNPLAN -> setSendRunPlan()
                CustomEvtType.SETWALKREMIND -> setWalkReminder()
//                CustomEvtType.SETWALKREMINDTIMES -> Cmds.setWalkRemindTimes(idoBaseModel as IDOWalkRemindTimesParamModel)
                CustomEvtType.SETWEATHERV3 -> setWeatherV3()
//                CustomEvtType.SETNOTICEMESSAGESTATE -> Cmds.setNoticeMessageState(idoBaseModel as IDONoticeMessageStateParamModel)
                CustomEvtType.SETMAINUISORTV3 -> setMainUISortV3()
                CustomEvtType.SETWATCHFACEDATA -> setWatchFaceData()
                CustomEvtType.SETMUSICOPERATE -> setMusicOperate()
                CustomEvtType.SETNOTIFICATIONSTATUS -> setNotificationStatus()
                CustomEvtType.SETFITNESSGUIDANCE -> setFitnessGuidance()
                CustomEvtType.SETSCIENTIFICSLEEPSWITCH -> setScientificSleepSwitch()
                CustomEvtType.SETTEMPERATURESWITCH -> setTemperatureSwitch()
                CustomEvtType.SETV3NOISE -> setV3Noise()
                CustomEvtType.SETHEARTMODE -> setHeartMode()
                CustomEvtType.SETHEARTRATEMODESMART -> setHeartRateModeSmart()
                CustomEvtType.SETTAKINGMEDICINEREMINDER -> setTakingMedicineReminder()
                CustomEvtType.SETBLEVOICE -> setBleVoice()
                CustomEvtType.SETLONGSIT -> setLongSit()
                CustomEvtType.SETLOSTFIND -> setLostFind()
                CustomEvtType.SETSPORTGOAL -> setSportGoal()
                CustomEvtType.SETUNIT -> setUnit()
//            CustomEvtType.SETNOTIFICATIONCENTER -> Cmds.setNotificationCenter(idoBaseModel as IDONotificationCenterParamModel)
                CustomEvtType.SETUPHANDGESTURE -> setUpHandGesture()
                CustomEvtType.SETMUSICONOFF -> setMusicOnOff()
                CustomEvtType.SETDISPLAYMODE -> setDisplayMode()
                CustomEvtType.SETSPORTMODESELECT -> setSportModeSelect()
                CustomEvtType.SETSLEEPPERIOD -> setSleepPeriod()
                CustomEvtType.SETWEATHERDATA -> setWeatherData()
                CustomEvtType.SETWEATHERSUNTIME -> setWeatherSunTime()
                CustomEvtType.SETWATCHDIAL -> setWatchDial()
                CustomEvtType.SETSHORTCUT -> setShortcut()
                CustomEvtType.SETBPCALIBRATION -> setBpCalibration()
                CustomEvtType.SETBPMEASUREMENT -> setBpMeasurement()
                CustomEvtType.SETSTRESSCALIBRATION -> setStressCalibration()
                CustomEvtType.SETSPO2SWITCH -> setSpo2Switch()
                CustomEvtType.SETHANDWASHINGREMINDER -> setHandWashingReminder()
                CustomEvtType.SETNOTICEAPPNAME -> setNoticeAppName()
                CustomEvtType.SETSYNCCONTACT -> setSyncContact()
                CustomEvtType.MUSICCONTROL -> musicControl()
                CustomEvtType.SETBPCALCONTROLV3 -> setBpCalControlV3()
                CustomEvtType.SETAPPLETCONTROL -> setAppletControl()
                CustomEvtType.SETBASESPORTPARAMSORTV3 -> setSportParamSort()
                CustomEvtType.SETSCHEDULERREMINDERV3 -> setSchedulerReminder()
                CustomEvtType.SET100SPORTSORTV3 -> setSport100Sort()
                CustomEvtType.SETWALLPAPERDIALREPLYV3 -> setWallpaperDialReply()
                CustomEvtType.SETVOICEREPLYTXTV3 -> setVoiceReplyText()
                CustomEvtType.SETTIME -> setDateTime()
                CustomEvtType.SETFINDPHONE -> Cmds.setFindPhone(true)
                CustomEvtType.SETWEATHERSWITCH -> {
                    Cmds.setWeatherSwitch(true).apply {
                        paramter_tv.text=json
                    }.send {
                        if (it.error.code == 0) {
                            tv_response.text = it.res?.toJsonString()
                        } else {
                            tv_response.text = "Setup failure"
                        }
                    }
                }
                CustomEvtType.SETONEKEYSOS -> Cmds.setOnekeySOS(false, 0)
                CustomEvtType.SETUNREADAPPREMINDER -> Cmds.setUnreadAppReminder(true)
                CustomEvtType.SETWEATHERCITYNAME -> Cmds.setWeatherCityName("dsdgf")
                CustomEvtType.SETRRESPIRATETURN -> Cmds.setRRespiRateTurn(false)
                CustomEvtType.SETBODYPOWERTURN -> Cmds.setBodyPowerTurn(false)
                CustomEvtType.SETSCREENBRIGHTNESS -> setScreenBrightness()
                CustomEvtType.SETACTIVITYSWITCH -> setActivitySwitch()
                CustomEvtType.SETHEARTRATEINTERVAL -> setHeartRateInterval()
                CustomEvtType.SETMENSTRUATION -> setMenstruation()
                CustomEvtType.SETCALORIEDISTANCEGOAL -> setCalorieDistanceGoal()
                CustomEvtType.SETWORLDTIMEV3 -> setWorldTimeV3()
                CustomEvtType.SETHAND -> Cmds.setHand(false)
                CustomEvtType.SETLONGCITYNAMEV3 -> setLongCityNameV3()
                CustomEvtType.GETHISTORICALMENSTRUATION -> setHistoricalMenstruation()
                CustomEvtType.SETSPORTSORTV3 -> setSportSortV3()
                CustomEvtType.SETUSERINFO -> setUserInfo()
                CustomEvtType.SETNOTDISTURB -> setNotDisturb()
                CustomEvtType.SETMENSTRUATIONREMIND -> setMenstruationRemind()
                CustomEvtType.SETSTRESSSWITCH -> setStressSwitch()
                CustomEvtType.SETDRINKWATERREMIND -> setDrinkWaterRemind()
                CustomEvtType.SETOVERFINDPHONE -> Cmds.setOverFindPhone()
                CustomEvtType.SETMENULIST -> setMenuList()
                CustomEvtType.SETALARM -> setAlarmV3()
                CustomEvtType.MUSICSTART -> Cmds.musicStart()
                CustomEvtType.MUSICSTOP -> Cmds.musicStop()
                CustomEvtType.SETCALLQUICKREPLYONOFF -> setCallQuickReplyOnOff()
                CustomEvtType.SETVOICEASSISTANTONOFF -> setVoiceAssistantOnOff()
                CustomEvtType.SETFASTMSGUPDATE -> setFastMsgUpdate()
                CustomEvtType.SETFASTMSGV3 -> setDefaultQuickMsgReplyList()
                CustomEvtType.SETSPORTMODESORT -> setSportModeSort()
                CustomEvtType.NOTICEMESSAGEV3 -> noticeMessageV3()

                else -> {
                    null
                }
            }
        }
    }

    /**
     *IDONoticeMessageParamModel
     * Attribute type declaration
     * evtType Int Indicates the message application type
     * msgID Int Message ID, which is used for quick reply. 0 indicates that quick reply is not supported
     * supportAnswering Bool Answering Answering: 1
     * Answer not supported: 0
     * supportMute Bool Mute support: 1
     * Mute: 0 is not supported
     * supportHangUp Bool HangUp support: 1
     * Hang-up: 0 is not supported
     * contact String Contact name (maximum 63 bytes)
     * phoneNumber String Phone number (maximum 31 bytes)
     * dataText String Message content (Max. 249 bytes)
     *
     * */
    private fun noticeMessageV3() {
        var param =
            IDONoticeMessageParamModel(0x01, 0,
                true, true, true,
                "Jeffry", "13200000000", "hello")


        var noticeMessageV3 = Cmds.noticeMessageV3(param)
        noticeMessageV3.send {

            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = noticeMessageV3.json
    }

    /**
     *Running plan issued by app (Exercise plan)
     * [IDORunPlanParamModel]
     * Attribute type declaration
     * verison Int Version number of the protocol library
     * operate Int Operate int
     * 1: Start the plan
     * 2: Send schedule data
     * 3: End the plan
     * 4: Query the running plan
     * type Int Type of the plan
     * 1:3km run program
     * 2:5km run program
     * 3:10km run program
     * 4: Half Marathon Training (Stage 2)
     * 5: Marathon Training (Stage 2)
     * dayNum Int Number of scheduled days
     * Applicable for 2 days of operation
     * year Int Start time of the exercise plan
     * month Int Start time of an exercise plan
     * day Int Start time of the exercise schedule
     * hour Int Indicates the start time of the exercise plan
     * min Int Start time of the exercise schedule
     * sec Int Start time of the exercise program
     * items	List    [IDOGpsInfoModelItem]
     * [IDOGpsInfoModelItem]
     * Attribute type declaration
     * type Int Training type
     * 186: Rest plan
     * 187: Outdoor running program
     * 188: Indoor running program
     * 189: Indoor fitness program
     * num Int Number of actions
     * Note: The number of movements at rest is zero, and the number of movements at other movements is non-zero
     * items List IDOItemItem collection
     * [IDOItemItem]
     * Attribute type declaration
     * type Int Action type
     * 1: Let's go
     * 2: Jogging
     * 3: Run at a moderate pace
     * 4: Run fast
     * time Int Time unit: second
     * heightHeart Int Heightheart int Low heart rate range
     * lowHeart Int High heart rate range
     *
     * */

    private fun setSendRunPlan() {
        var sendRunPlan = Cmds.setSendRunPlan(
            IDORunPlanParamModel(
                operate = 1,
                type = 1,
                year = 2020,
                month = 12,
                day = 26,
                hour = 17,
                min = 36,
                sec = 0,
                dayNum = 1,
                items = listOf(
                    IDOGpsInfoModelItem(
                        type = 186, num = 1, items = listOf(
                            IDOItemItem(
                                type = 1, time = 200, heightHeart = 110, lowHeart = 80
                            )
                        )
                    )
                )
            )
        )
        sendRunPlan.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = sendRunPlan.json

    }

    /**
     *
     * Set a walk alert
     * [IDOWalkRemindModel]
     * Attribute type declaration
     * onOff Int 0 off, 1 on
     * goalStep Int Number of steps per hour (deprecated)
     * startHour Int Start time (hours)
     * startMinute Int Start time (minutes)
     * endHour Int End time (hour)
     * endMinute Int End time (minutes)
     * repeats Set IDOWeek
     * isOpen Bool Repeat switch
     * goalTime Int Goal time (hours) (deprecated)
     * notifyFlag Int Indicates the notification type
     * 0: invalid
     * 1: Allow notifications
     * 2: Silent notification
     * 3: Disable notification
     * setWalkReminderAddNotify needs to be enabled in the firmware
     * doNotDisturbOnOff Int Do NotDisturb switch
     * 0 off
     * 1 Turn on
     * Need to enable getSupportSetGetNoReminderOnWalkReminderV2 firmware
     * noDisturbStartHour Int Do Not DisturbStart Time (hour)
     * Need to enable getSupportSetGetNoReminderOnWalkReminderV2 firmware
     * noDisturbStartMinute Int Do Not DisturbStart Time (minute)
     * Need to enable getSupportSetGetNoReminderOnWalkReminderV2 firmware
     * noDisturbEndHour Int Do Not DisturbEnd Time (hour)
     * Need to enable getSupportSetGetNoReminderOnWalkReminderV2 firmware
     * noDisturbEndMinute Int Do Not DisturbEnd Time (minute)
     * Need to enable getSupportSetGetNoReminderOnWalkReminderV2 firmware
     * */

    private fun setWalkReminder() {
        var walkReminder = Cmds.setWalkReminder(
            IDOWalkRemindModel(
                1,
                2000,
                14,
                0,
                20,
                0,
                hashSetOf(IDOWeek.MONDAY, IDOWeek.SUNDAY),
                60,
                1,
                0,
                0,
                0,
                0,
                0
            )
        )
        walkReminder.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = walkReminder.json


    }

    /**
     * Set the dial order
     * [IDOWatchDialSortParamModel]
     * Attribute type declaration
     * sortItemNumb Int Number of items in the dial sorting list
     * pSortItem [IDOWatchDialSortItem] Dial sorting list content
     * [IDOWatchDialSortItem]
     * Attribute type declaration
     * type Int Type 1: common dial, 2: wallpaper dial, 3: cloud dial
     * sortNumber Int Indicates the serial number starting from 0 and cannot exceed the total number of supported watch dials
     * name String Dial ID. The value is a maximum of 29 bytes
     *
     * */

    private fun setWatchDialSort() {

        Cmds.getWatchListV2().send { it ->
            var size = it.res?.items?.size
            var list = mutableListOf<IDOWatchDialSortItem>()
            var i = 0
            it.res?.items?.forEach {
                list.add(IDOWatchDialSortItem(1, i++, it.fileName))
            }
            var watchDialSort = Cmds.setWatchDialSort(
                IDOWatchDialSortParamModel(size!!, list)
            )
            watchDialSort.send {
                if (it.error.code == 0) {
                    tv_response.text = it.res?.toJsonString()
                } else {
                    tv_response.text = "Setup failure"
                }
            }
            paramter_tv.text = watchDialSort.json
        }


    }

    /**
     *
     * v3 Delivers the v3 weather protocol
     * [IDOWeatherV3ParamModel]
     * Attribute type declaration
     * month int Month of the latest synchronization on the server
     * day int Day of the last synchronization on the server
     * hour int Indicates the last synchronization time on the server
     * min int Minutes of the last synchronization on the server
     * sec int Seconds of the last synchronization on the server
     * week Int Day of the week
     * bit0: Monday
     * bit1: Tuesday, and so on until Sunday
     * weatherType Int Weather type
     * 0: other
     * 1: Clear
     * 2: Cloudy
     * 3: Yin
     * 4: Rain
     * 5: Heavy rain
     * 6: Thunderstorms
     * 7: Snow
     * 8: Sleet
     * 9: Typhoon
     * 10: Dust storm
     * 11: Clear at night
     * 12: Cloudy at night
     * 13: Hot
     * 14: Cold
     * 15: Light breeze
     * 16: Strong Wind
     * 17: Haze
     * 18: Shower
     * 19: Cloudy to sunny < br/>48: thunder
     * 49: Hail
     * 50: Wind and sand
     * 51: Tornado (realme Custom Weather Type idw02)
     * todayTmp Int Current temperature (degrees Celsius)
     * For negative temperatures, add 100 to the temperature and transmit
     * todayMaxTemp Int Maximum temperature (Celsius)
     * For negative temperatures, add 100 to the temperature and transmit
     * todayMinTemp Int Minimum Temperature (Celsius)
     * For negative temperatures, add 100 to the temperature and transmit
     * cityName String City name
     * Up to 74 bytes
     * airQuality Int Air quality
     * Multiply by 10 for transmission
     * precipitationProbability Int Precipitation probability
     * 0-100%
     * humidity int Humidity int
     * todayUvIntensity int UV intensity
     * Expand transmission by a factor of 10
     * windSpeed int Wind speed
     * sunriseHour int Sunrise time
     * sunriseMin int Sunrise time
     * sunsetHour int Sunrise time
     * sunsetMin int Sunrise time
     * sunriseItemNum Int Detailed number of sunrise and sunset times
     * The current maximum number of days is set to 7
     * airGradeItem String Air quality level content
     * hoursWeatherItems Collection A collection of weather data for the next 48 hours (optional)
     * [IDOHoursWeatherItem]
     * futureItems Collects weather data for the next seven days (optional). It is not necessary to include the current day's data
     * [IDOFutureItem]
     * sunriseItem Collection sunrise and sunset time details (optional), which must contain the current day data
     * [IDOSunriseItem]
     * [IDOHoursWeatherItem]
     * Attribute type declaration
     * weatherType int Weather type
     * temperature int Temperature
     * Temperature plus 100 transmission
     * probability int The probability that the temperature occurs
     * 0-100 percent
     * [IDOFutureItem]
     * Attribute type declaration
     * weatherType int Weather type
     * maxTemp int Maximum temperature
     * Temperature plus 100 transmission
     * minTemp int Minimum temperature
     * Temperature plus 100 transmission
     * [IDOSunriseItem]
     * Attribute type declaration
     * sunriseHour int Sunrise time
     * version 1 is invalid
     * sunriseMin int Sunrise time
     * version 1 is invalid
     * sunsetHour int Indicates the sunset time
     * version 1 is invalid
     * sunsetMin int Sunset time
     * version 1 is invalid
     */

    private fun setWeatherV3() {
        // Get the current time
        val now = LocalDateTime.now()
        var weatherV3 = Cmds.setWeatherV3(
            IDOWeatherV3ParamModel(
                now.monthValue,
                now.dayOfMonth,
                now.hour,
                now.minute,
                now.second,
                now.dayOfWeek.value,
                1,
                19+100,
                33+100,
                15+100,
                "shenzhen",
                7,
                40,
                32,
                10,
                5,
                15,
                5,
                37,
                18,
                49,
                3,
                "big",
                listOf(
                    IDOHoursWeatherItem(
                        weatherType = 7, temperature = 8+100, probability = 40
                    )
                ),
                listOf(
                    IDOFutureItem(
                        weatherType = 6, maxTemp = 33+100, minTemp = 8+100
                    )
                ),
                listOf(
                    IDOSunriseItem(
                        sunriseHour = 5,
                        sunriseMin = 35,
                        sunsetHour = 15,
                        sunsetMin = 35
                    ), IDOSunriseItem(
                        sunriseHour = 6,
                        sunriseMin = 36,
                        sunsetHour = 16,
                        sunsetMin = 36
                    ), IDOSunriseItem(
                        sunriseHour = 7,
                        sunriseMin = 37,
                        sunsetHour = 17,
                        sunsetMin = 37
                    )
                ),
                listOf(20, 30, 41, 51, 61,77,90),
                listOf(22,33,44,55,66,77,88)
            )
        )
        weatherV3.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }

        paramter_tv.text = weatherV3.json

    }


    /**
     * v3 Sets the home screen control sort
     * [IDOMainUISortParamModel]
     * Attribute type declaration
     * operate Int Operate int
     * 0: invalid 1: query 2: set
     * locationX Int The X-axis, starting at 1
     * locationY Int coordinates on the Y-axis, starting at 1
     * A y axis represents a horizontal grid
     * sizeType Int 0: invalid 1: large icon 2: small icon
     * widgetsType Int Control type
     * 0: invalid
     * 1: Week/date
     * 2: number of steps
     * 3: Distance
     * 4: Calories
     * 5: Heart rate
     * 6: Battery
     * items List int collection
     * */
    private fun setMainUISortV3() {
        var mainUISortV3 = Cmds.setMainUISortV3(
            IDOMainUISortParamModel(
                2, listOf(1, 2, 3), 1, 1, 2, 1
            )
        )
        mainUISortV3.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = mainUISortV3.json

    }

    /**
     * Dial setting
     * [IDOWatchFaceParamModel]
     * Attribute type declaration
     * operate Int Operation:
     * 0 - Queries the dial currently in use
     * 1 - Set the dial
     * 2 - Remove the dial
     * 3 - Dynamic Request Space Sets the corresponding space size
     * fileName String Dial name. The value is a maximum of 29 bytes
     * watchFileSize Int Uncompressed file length
     * After the firmware opens the menu getWatchDailSetAddSize, the application needs to send this field
     * Before the dial is transferred, the firmware needs to allocate the corresponding space to save, and the uncompressed file length needs to be the file length of the transfer
     * */
    private fun setWatchFaceData() {
        var watchFaceData = Cmds.setWatchFaceData(
            IDOWatchFaceParamModel(
                1, "w63.iwf", 2024
            )
        )
        watchFaceData.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = watchFaceData.json

    }

    /**
     * Mobile app through this command switch to achieve notification application status Settings
     * [IDONotificationStatusParamModel]
     * Attribute type declaration
     * notifyFlag Int Notification type:
     * 1: Allow notifications
     * 2: Silent notification
     * 3: Disable notification
     * */
    private fun setNotificationStatus() {
        var notificationStatus = Cmds.setNotificationStatus(IDONotificationStatusParamModel(1))
        notificationStatus.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }

        paramter_tv.text = notificationStatus.json

    }

    /**
     * Fitness instruction
     * [IDOFitnessGuidanceParamModel]
     * Attribute type declaration
     * mode Int Fitness instruction mode switch
     * 1: On
     * 0: Off
     * startHour Int Start time
     * startMinute Int Start minute
     * endHour Int End time
     * endMinute Int Indicates the end minute
     * notifyFlag Int Indicates the notification type
     * 0: invalid
     * 1: Allow
     * 2: Silence
     * 3: Disable
     * goMode Int Mobile switch reminder
     * 1: On
     * 0: Off
     * repeats Set IDOWeek sets
     * isOpen Bool Main switch
     * targetSteps Int Number of target steps
     * */
    private fun setFitnessGuidance() {
        var fitnessGuidance = Cmds.setFitnessGuidance(
            IDOFitnessGuidanceParamModel(
                1, 9, 0, 18, 0, 1, 0, hashSetOf(
                    IDOWeek.MONDAY,
                    IDOWeek.TUESDAY,
                    IDOWeek.WEDNESDAY,
                    IDOWeek.THURSDAY,
                    IDOWeek.FRIDAY,
                    IDOWeek.SATURDAY,
                    IDOWeek.SUNDAY
                ), 2000
            )
        )
        fitnessGuidance.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }

        }
        paramter_tv.text = fitnessGuidance.json

    }

    /**
     *
     * Set the scientific sleep switch
     * [IDOScientificSleepSwitchParamModel]
     * Attribute type declaration
     * mode Int mode
     * 2: Scientific sleep
     * 1: Normal sleep
     * startHour Int Start time - hour
     * startMinute Int Start time - minutes
     * endHour Int End time - hour
     * endMinute Int End time - minute
     * */
    private fun setScientificSleepSwitch() {
        var scientificSleepSwitch = Cmds.setScientificSleepSwitch(
            IDOScientificSleepSwitchParamModel(
                1, 23, 23, 9, 0
            )
        )
        scientificSleepSwitch.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = scientificSleepSwitch.json

    }

    /**
     * Set the night temperature switch
     * [IDOTemperatureSwitchParamModel]
     * Attribute type declaration
     * mode Int mode:
     * 1: On
     * 0: Off
     * startHour Int Start time and hour
     * startMinute Int Start time (minutes)
     * endHour Int End time, hour
     * endMinute Int End time (minutes)
     * unit Int Temperature unit setting:
     * 1: Degree Celsius
     * 2: Degrees Fahrenheit
     * */
    private fun setTemperatureSwitch() {
        var temperatureSwitch = Cmds.setTemperatureSwitch(
            IDOTemperatureSwitchParamModel(
                1, 19, 0, 23, 0, 1
            )
        )
        temperatureSwitch.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = temperatureSwitch.json

    }

    /**
     * Ambient volume switch and threshold
     * [IDOV3NoiseParamModel]
     * Attribute type declaration
     * mode Int All-day ambient noise volume switch
     * 1: On
     * 0: Off
     * startHour Int Start time (hours)
     * startMinute Int Start time (minutes)
     * endHour Int End time (hour)
     * endMinute Int End time (minutes)
     * highNoiseOnOff Int Threshold switch
     * 1: On
     * 0: Off
     * highNoiseValue Int Threshold
     * */
    private fun setV3Noise() {
        var v3Noise = Cmds.setV3Noise(
            IDOV3NoiseParamModel(
                1, 15, 0, 20, 0, 1, 100
            )
        )
        v3Noise.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = v3Noise.json

    }

    /**
     * Set heart rate mode
     * [IDOHeartModeParamModel]
     * Attribute type declaration
     * updateTime Int The update time is the Unix timestamp in seconds. If the value is 0, the current UTC timestamp is obtained.
     * mode Int mode
     * 0: Off
     * 1: Automatic (5 minutes)
     * 2: Continuous monitoring (5 seconds)
     * 3: Manual mode (auto disabled)
     * 4: Default mode, which is automatically set to the default mode
     * 5: Set the corresponding measurement interval
     * 6: Intelligent Heart Rate Mode (ID206)
     * Attention:
     * 1. If the setSetV3HeartInterval function is configured, modes 0, 1, and 2 are invalid.
     * 2. When configuring with Quick Settings, setting setSetV3HeartInterval activates mode 5
     * 3. If the setSetV3HeartInterval function is configured when setting the continuous heart rate, the corresponding mode is mode 5.
     * hasTimeRange Int Specifies whether a time range exists. 0: no, 1: yes
     * measurementInterval Int Measurement interval in seconds
     * startHour Int Start time, when
     * startMinute Int Start time (minutes)
     * endHour Int End time, when
     * endMinute Int End time
     * notifyFlag Int Notification type:
     * 0: invalid
     * 1: Allow notifications
     * 2: Silent notification
     * 3: Disable notification
     * Note: If the firmware does not have v3HeartSetRateModeCustom enabled, this feature is invalid
     * highHeartMode Int 1: Enables the intelligent high heart rate notification
     * 0: Off
     * Note: This feature is invalid if the firmware does not have v3HeartSetRateModeCustom enabled
     * lowHeartMode Int 1: Enables the smart low heart rate alert
     * 0: Off
     * Note: This feature is invalid if the firmware does not have v3HeartSetRateModeCustom enabled
     * highHeartValue Int Intelligent high heart rate alert threshold
     * Note: If the firmware does not have v3HeartSetRateModeCustom enabled, this feature is invalid
     * lowHeartValue Int Intelligent low heart rate alert threshold
     * Note: If the firmware does not have v3HeartSetRateModeCustom enabled, this feature is invalid
     * */
    private fun setHeartMode() {
        var heartMode = Cmds.setHeartMode(
            IDOHeartModeParamModel(
                8, 0, 0, 23, 59, 1, 4, 4, 49, 98, 99, 100, 101
            )
        )
        heartMode.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = heartMode.json


    }

    /**
     * Smart heart rate mode setting
     * [IDOHeartRateModeSmartParamModel]
     * Attribute type declaration
     * mode Int switch
     * 0: Off
     * 1: On
     * notifyFlag Int Indicates the notification type
     * 0: invalid
     * 1: Allow notifications
     * 2: Silent notification
     * 3: Disable notification
     * highHeartMode Int 1: Enables intelligent high heart rate alert
     * 0: disable
     * lowHeartMode Int 1: Enables intelligent low heart rate alert
     * 0: disable
     * highHeartValue Int Intelligent high heart rate alarm threshold
     * lowHeartValue Int Intelligent low heart rate alarm threshold
     * startHour Int Heart Rate monitoring start time (hours)
     * startMinute Int Start time of heart rate monitoring (minutes)
     * endHour Int Heart rate monitoring end time (hour)
     * endMinute Int End time of Heart rate monitoring (minutes)
     * */
    private fun setHeartRateModeSmart() {
        var heartRateModeSmart = Cmds.setHeartRateModeSmart(
            IDOHeartRateModeSmartParamModel(
                1,
                1,
                1,
                0,
                120,
                50,
                0,
                0,
                23,
                59,
            )
        )
        runOnUiThread(Runnable {
            heartRateModeSmart.send {
                if (it.error.code == 0) {
                    tv_response.text = it.res?.toJsonString()
                } else {
                    tv_response.text = "Setup failure"
                }
            }
        })
        paramter_tv.text = heartRateModeSmart.json

    }

    /**
     * Set a pill reminder
     * [IDOTakingMedicineReminderParamModel]
     * Attribute type declaration
     * takingMedicineId Int The ID ranges from 1 to 5
     * onOff Int 0 Indicates off
     * 1 indicates enable.
     * startHour Int Indicates the start time of the reminder
     * startMinute Int Indicates the start minute of the reminder
     * endHour Int Indicates the end time of reminding
     * endMinute Int Indicates the end minute of the reminder
     * repeats Set IDOWeek
     * interval Int Reminder interval (minutes)
     * The default value is 60 minutes
     * doNotDisturbOnOff Int Do not Disturb Time range switch
     * 0 is Off
     * 1 is on
     * Default to off
     * Donotdisturb starthour Int Do not disturb the start time
     * Donotdisturb startminute Int Do not disturb the start minute
     * Donotdisturb Endhour Int Do not disturb the end time
     * Donotdisturb endminute Int doNotDisturbEndMinute Int
     * */
    private fun setTakingMedicineReminder() {
        var takingMedicineReminder = Cmds.setTakingMedicineReminder(
            IDOTakingMedicineReminderParamModel(
                1, 0, 0, 23, 59, 1, hashSetOf(
                    IDOWeek.MONDAY,
                    IDOWeek.TUESDAY,
                    IDOWeek.WEDNESDAY,
                    IDOWeek.THURSDAY,
                    IDOWeek.FRIDAY,
                    IDOWeek.SATURDAY,
                    IDOWeek.SUNDAY
                ), 49, 98, 99, 100, 101, 9
            )
        )
        takingMedicineReminder.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = takingMedicineReminder.json

    }

    /**
     * Send ble at phone volume
     * [IDOBleVoiceParamModel]
     * Attribute type declaration
     * totalVolume Int Total volume
     * currentVolume Int Current volume
     * */
    private fun setBleVoice() {
        var bleVoice = Cmds.setBleVoice(IDOBleVoiceParamModel(100, 85))
        bleVoice.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = bleVoice.json


    }

    /**
     * sedentary
     * [IDOLongSitParamModel]
     * Attribute type declaration
     * startHour Int Sitting Reminder Start time (hours)
     * startMinute Int Sitting Reminder start time (minutes)
     * endHour Int Sitting Reminder End time (hours)
     * endMinute Int End time of Sitting reminder (minutes)
     * interval Int Interval in minutes
     * The value should be greater than 15 minutes
     * repetitions Int Repetitions and switches
     * bit0:0 indicates off, 1 indicates on
     * bit1-7:0 means no repetition, and 1 means repetition
     * */
    private fun setLongSit() {
        var longSit = Cmds.setLongSit(
            IDOLongSitParamModel(
                15, 40, 23, 26, 15, 254
            )
        )
        longSit.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = longSit.json

    }

    /**
     * Set up anti-lose
     * [IDOLostFindParamModel]
     * Attribute type declaration
     * mode Int mode
     * 0: no loss prevention
     * 1: Close to prevent loss
     * 2: Medium-distance anti-loss
     * 3: Long distance to prevent loss
     * */
    private fun setLostFind() {
        var lostFind = Cmds.setLostFind(IDOLostFindParamModel(0))
        lostFind.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = lostFind.json

    }

    /**
     * Set moving goals
     * [IDOSportGoalParamModel]
     * Attribute type declaration
     * sportStep Int The number of steps for the exercise goal
     * walkGoalSteps Int Set the target number of steps per hour
     * targetType Int Set the target type
     * 0: invalid
     * 1: Daily goals
     * 2: Weekly goals
     * This parameter is supported by getStepDataTypeV2
     * */
    private fun setSportGoal() {
        var sportGoal = Cmds.setSportGoal(
            IDOSportGoalParamModel(
                2000,
                15,
                1,
            )
        )
        sportGoal.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = sportGoal.json

    }

    /**
     *
     * Set unit
     * [IDOUnitParamModel]
     * Attribute type declaration
     * distUnit Int Distance unit:
     * 0: invalid
     * 1: km (metric)
     * 2: miles (imperial)
     * weightUnit Int A unit of weight:
     * 0: invalid
     * 1: kg
     * 2: lb
     * temp Int Temperature unit:
     * 0: invalid
     * 1: ° C
     * 2: ℉
     * stride Int: Stride int:
     * 0: invalid
     * 0: cm
     * language Int Language
     * is12HourFormat Int Time format:
     * 0: invalid
     * 1:24 hour format
     * 2:12 hour format
     * strideRun Int Running stride:
     * 0: invalid
     * 1: cm
     * Default value for males: 90cm
     * strideGpsCal Int Calibrates on/off via GPS stride length:
     * 0: invalid
     * 1: On
     * 2: Off
     * weekStartDate Int Start date of a week:
     * 0: Monday
     * 1: Sunday
     * 3: Saturday
     * calorieUnit Int Calorieunit setting:
     * 0: invalid
     * 1: indicates the default kCal
     * 2: Cal
     * 3: kJ
     * swimPoolUnit Int PoolUnit Settings:
     * 0: invalid
     * 1: default meter
     * 2: code
     * cyclingUnit Int Cycling unit:
     * 0: invalid
     * 1: km
     * 2: miles
     * walkingRunningUnit Int Walking or running unit (km/mile) setting:
     * 0: invalid
     * 1: km
     * 2: miles
     * The device firmware setSupportWalkRunUnit support is required
     * */
    private fun setUnit() {
        var unit = Cmds.setUnit(
            IDOUnitParamModel(
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
                1,
            )
        )
        unit.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = unit.json

    }

    /**
     * Raise your hand to light the screen
     * [IDOUpHandGestureParamModel]
     * Attribute type declaration
     * onOff Int Switch
     * 1: On
     * 0: Off
     * showSecond Int Duration for which the screen remains on (in seconds)
     * hasTimeRange Int Specifies whether a time range exists
     * 1: Yes
     * 0: None
     * startHour Int Specifies the start time of the time range
     * startMinute Int Specifies the start minute of the time range
     * endHour Int End hour of the time range
     * endMinute Int Indicates the end minute of the time range
     * */
    private fun setUpHandGesture() {
        var upHandGesture = Cmds.setUpHandGesture(
            IDOUpHandGestureParamModel(
                1,
                10,
                1,
                8,
                0,
                18,
                0,
            )
        )
        upHandGesture.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = upHandGesture.json

    }

    /**
     * Music switch
     * [IDOMusicOnOffParamModel]
     * Attribute type declaration
     * onOff Int 1: On
     * 0: Off
     * showInfoStatus Int Switch of displaying song information
     * 1: On
     * 0: Off
     * Need firmware support menu: supportV2SetShowMusicInfoSwitch
     * */
    private fun setMusicOnOff() {
        var musicOnOff = Cmds.setMusicOnOff(
            IDOMusicOnOffParamModel(
                10,
                4,
            )
        )
        musicOnOff.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = musicOnOff.json

    }

    /**
     * Display mode
     * [IDODisplayModeParamModel]
     * Attribute type declaration
     * mode Int mode
     * 0: default
     * 1: Horizontal
     * 2: Vertical
     * 3: Flip (180 degrees)
     * */
    private fun setDisplayMode() {
        var displayMode = Cmds.setDisplayMode(
            IDODisplayModeParamModel(
                10,
            )
        )
        displayMode.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = displayMode.json

    }

    /**
     * Set motion mode selection
     * [IDOSportModeSelectParamModel]
     * Attribute type declaration
     * flag Int 0: invalid
     * 1: Set the fast sport type - sport_type1 & sport_type2 & sport_type3 & sport_type4
     * 2: Set the specific exercise type
     * sportType1 Int Fast sport type 1
     * Flag: 1 Valid
     * sportType2 Int Fast sport type 2
     * Flag: 1 Valid
     * sportType3 Int Fast sport type 3
     * Flag: 1 Valid
     * sportType4 Int Fast Sport type 4
     * Flag: 1 Valid
     * sportType0Walk Bool Type: walk. 0 is not supported and 1 is supported
     * Flag: 2 Valid
     * sportType0Run Bool Type: Running. 0 is not supported and 1 is supported
     * Flag: 2 Valid
     * sportType0ByBike Bool Type: ride, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType0OnFoot Bool Type: Walk (walk), 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType0Swim Bool Type: swim. 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * SportType0MountainClimbing Bool type: mountain, 0 does not support, 1 support
     * flag: 2 Valid
     * sportType0Badminton Bool Type: badminton, 0 not supported, 1 supported
     * Flag: 2 Valid
     * sportType0Other Bool Type: other. 0 is not supported, and 1 is supported
     * Flag: 2 Valid
     * sportType1Fitness Bool Type: Fitness. 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType1Spinning Bool Type: spinning, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType1Ellipsoid Bool Type: ellipsoid. 0 is not supported, and 1 is supported
     * Flag: 2 Valid
     * Sporttype1bool Treadmill type: treadmill, with 0 not supported and 1 supported
     * Flag: 2 Valid
     * sportType1SitUp Bool Type: sit-ups. 0 is not supported and 1 is supported
     * Flag: 2 Valid
     * sportType1PushUp Bool Type: push-up. 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType1Dumbbell Bool Type: dumbbell. 0 is not supported, and 1 is supported
     * Flag: 2 Valid
     * sportType1Weightlifting Bool Type: weightlifting. 0 is not supported, and 1 is supported
     * Flag: 2 Valid
     * SportType2BodybuildingExercise Bool types: exercise, 0 is not supported, 1 support
     * Flag: 2 Valid
     * sportType2Yoga Bool Type: yoga. 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType2RopeSkipping Bool Type: Skip rope. 0 is not supported and 1 is supported
     * Flag: 2 Valid
     * sportType2TableTennis Bool Type: table tennis. 0 is not supported, and 1 is supported
     * Flag: 2 Valid
     * sportType2Basketball Bool Type: basketball, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType2Football Bool Type: football, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType2Volleyball Bool Type: volleyball, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType2Tennis Bool Type: tennis, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType3Golf Bool Type: golf, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType3Baseball Bool Type: baseball, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType3Skiing Bool Type: skiing, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType3RollerSkating Bool Type: Roller skating. 0 is not supported and 1 is supported
     * Flag: 2 Valid
     * sportType3Dance Bool Type: dance. 0 is not supported, and 1 is supported
     * Flag: 2 Valid
     * SportType3StrengthTraining Bool types: strength training, 0 is not supported, 1 support
     * Flag: 2 Valid
     * sportType3CoreTraining Bool Type: core training, 0 is not supported, 1 is supported
     * Flag: 2 Valid
     * sportType3TidyUpRelax Bool Type: Collated relaxed. 0 is not supported, and 1 is supported
     * Flag: 2 Valid
     * */
    private fun setSportModeSelect() {
        var sportModeSelect = Cmds.setSportModeSelect(
            IDOSportModeSelectParamModel(
                10,
                4,
                10,
                19,
                10,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
            )
        )
        sportModeSelect.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }

        paramter_tv.text = sportModeSelect.json

    }

    /**
     * Set a sleep schedule
     * [IDOSleepPeriodParamModel]
     * Attribute type declaration
     * onOff Int Switch
     * 1-mo
     * 0 stage
     * startHour Int Start time (hours)
     * startMinute Int Start time (minutes)
     * endHour Int End time (hour)
     * endMinute Int End time (minutes)
     * */
    private fun setSleepPeriod() {
        var sleepPeriod = Cmds.setSleepPeriod(
            IDOSleepPeriodParamModel(
                1,
                23,
                0,
                8,
                0,
            )
        )
        sleepPeriod.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = sleepPeriod.json

    }

    /**
     * Set sunrise and sunset times
     * [IDOWeatherSunTimeParamModel]
     * Attribute type declaration
     * sunriseHour Int Sunrise time
     * sunriseMin Int Sunrise time
     * sunsetHour Int. Sunsethour int
     * sunsetMin Int One minute after sunset
     * */
    private fun setWeatherSunTime() {
        var weatherSunTime = Cmds.setWeatherSunTime(
            IDOWeatherSunTimeParamModel(
                6,
                12,
                18,
                30,
            )
        )
        weatherSunTime.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = weatherSunTime.json

    }

    /**
     * Dial setting
     * [IDOWatchDialParamModel]
     * Attribute type declaration
     * dialId Int Dial ID to be set
     * Dial id
     * 0 is invalid. Currently 1 to 4 is supported
     * */
    private fun setWatchDial() {
        var watchDial = Cmds.setWatchDial(
            IDOWatchDialParamModel(
                1,
            )
        )
        watchDial.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = watchDial.json

    }

    /**
     * Set a shortcut
     * [IDOShortcutParamModel]
     * Attribute type declaration
     * mode Int Function of shortcut key 1
     * 0: invalid
     * 1: Quickly enter the camera control
     * 2: Quickly enter the exercise mode
     * 3: Enter do not Disturb quickly
     * */
    private fun setShortcut() {
        var shortcut = Cmds.setShortcut(
            IDOShortcutParamModel(
                2
            )
        )
        shortcut.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = shortcut.json

    }


    /**
     * Set the blood oxygen switch
     * [IDOSpo2SwitchParamModel]
     * Attribute type declaration
     * onOff Int SpO2 all-day switch
     * 1-mo
     * 0 stage
     * startHour Int Start time (hours)
     * startMinute Int Start time (minutes)
     * endHour Int End time (hour)
     * endMinute Int End time (minutes)
     * lowSpo2OnOff Int Low SpO2 switch
     * 1-mo
     * 0 stage
     * Requires support for menu setSpo2AllDayOnOff
     * lowSpo2Value Int Low SpO2 threshold
     * Need the support of "v3SupportSetSpo2LowValueRemind" menu
     * notifyFlag Int Indicates the notification type
     * 0: invalid
     * 1: Allow notifications
     * 2: Silent notification
     * 3: Disable notification
     * The "getSpo2NotifyFlag" menu is required
     * */
    private fun setSpo2Switch() {
        var spo2Switch = Cmds.setSpo2Switch(
            IDOSpo2SwitchParamModel(
                1, 14, 0, 20, 0, 1, 20, 1
            )
        )
        spo2Switch.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = spo2Switch.json

    }

    /**
     * V3 Dynamic message notification
     * [IDONoticeMesaageParamModel]
     * Attribute type declaration
     * verison Int Version number of the protocol library
     * osPlatform Int System 0: invalid, 1: Android, 2: iOS
     * evtType Int Indicates the current mode. 0: invalid, 1: message alert
     * notifyType Int Indicates the maximum message enumeration type: 20000
     * msgID Int Message ID This parameter is valid only when evt_type is message alert and msg_ID is not 0
     * appItemsLen Int Number of countries and language details
     * contact String Contact name (maximum 63 bytes)
     * phoneNumber String Phone number (maximum 31 bytes)
     * msgData String Message content (Max. 249 bytes)
     * [IDONoticeMesaageParamItem]
     * Attribute type declaration
     * language Int Indicates the language type
     * name String Application name corresponding to the country (maximum 49 bytes)
     * */
    private fun setNoticeAppName() {
        var noticeAppName = Cmds.setNoticeAppName(
            IDONoticeMesaageParamModel(
                1, 1, 19, 1, 7, "ido", "ido_demo", "ido_demo", listOf(
                    IDONoticeMesaageParamItem(
                        1, "china"
                    )
                )
            )
        )
        noticeAppName.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = noticeAppName.json

    }

    /**
     * Synchronize frequent contacts
     * [IDOSyncContactParamModel]
     * Attribute type declaration
     * operat Int Indicates the operation
     * 0: invalid
     * 1: Set a contact
     * 2: Query contacts
     * 3: set up an emergency contact getSupportSetGetEmergencyContactV3 support (menu)
     * 4: query emergency contact (need menu support getSupportSetGetEmergencyContactV3)
     * version int Protocol version (no assignment required)
     * itemsNum Int Number of items (no assignment required)
     * items List IDOContactItem collection
     * */
    private fun setSyncContact() {
        var syncContact = Cmds.setSyncContact(
            IDOSyncContactParamModel(
                1, listOf(
                    IDOContactItem(
                        "18888888888", "ido"
                    )
                )
            )
        )
        syncContact.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = syncContact.json

    }

    /**
     * String	singerName
     *
     * Control music v3 protocol
     * [IDOMusicControlParamModel]
     * Type attribute declaration
     * Int status Status: 0: invalid 1: play 2: pause 3: stopped
     * Int curTimeSecond Current playback time Unit: second
     * Int totalTimeSecond Total play time Unit: second
     * String musicName Music name (Max. 63 bytes)
     * String	singerName
     * */
    private fun musicControl() {
        var musicControl = Cmds.musicControl(
            IDOMusicControlParamModel(
                1, 5, 360, "ido.mp3", "ido"

            )
        )
        musicControl.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = musicControl.json

    }


    /**
     * v3 Set schedule notification
     * [IDOSchedulerReminderParamModel]
     * Attribute	Type	Description
     * operate	Int	Operation type
     * 0: Invalid
     * 1: Add
     * 2: Delete
     * 3: Query
     * 4: Modify
     * items [IDOSchedulerReminderItem] Reminder event details
     * A maximum of one data can be set
     * [IDOSchedulerReminderItem]
     * Attribute type declaration
     * id Int ID of a reminder event. The incremental value sent by the application, starting from 0
     * repeatType Int Repeat time
     * If used
     * GetSupportSetRepeatWeekTypeOnScheduleReminderV3 is enabled, the repeated bit1 - bit7 set based on the week (Monday to Sunday, 0 as the general switch)
     * Set the type of repetition (0: invalid, 1: once, 2: daily, 3: weekly, 4: monthly, 5: annual) (if passed
     * GetSupportSetRepeatTypeOnScheduleReminderV3 enabled)
     * remindOnOff Int Daily reminder switch
     * 0: off, 1: on
     * state Int Status code
     * 0: invalid, 1: deleted, 2: enabled
     * title String Title content. Maximum 74 bytes
     * note String Reminder content. Up to 149 bytes
     * */
    private fun setSchedulerReminder() {
        var schedulerReminder = Cmds.setSchedulerReminder(
            IDOSchedulerReminderParamModel(
                1, listOf(
                    IDOSchedulerReminderItem(
                        0,
                        2022,
                        12,
                        26,
                        15,
                        51,
                        20,
                        255,
                        1,
                        2,
                        "Reminder 1",
                        "Remember to finish work",
                    )
                )
            )
        )
        schedulerReminder.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = schedulerReminder.json

    }

    /**
     * v3 new 100 sports sort
     * [IDOSport100SortParamModel]
     * Attribute type declaration
     * operate Int Operate int
     * 0: invalid 1: query 2: set
     * nowUserLocation Int Displays the current location of the add movement
     * items [Int] The maximum number of items in the motion sorting list is 150
     * */
    private fun setSport100Sort() {
        var sport100Sort = Cmds.setSport100Sort(
            IDOSport100SortParamModel(
                2, 2, listOf(
                    1, 2
                )
            )
        )
        sport100Sort.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = sport100Sort.json

    }

    /**
     * v3 Specifies the wallpaper dial list
     * [IDOWallpaperDialReplyV3ParamModel]
     * Attribute type declaration
     * operate Int Operation: 0 is used to query, 1 is used to set, and 2 is used to delete the wallpaper dial
     * location Int Sets the location information. For details, see the 9-space layout
     * hideType Int Hidden type: 0 indicates that all controls are displayed, 1 indicates that child controls (ICONS and numbers) are hidden.
     * timeColor Int Time control color (reserved 1 byte +R (1 byte) +G (1 byte) +B (1 byte))
     * widgetType Int Control type: 1 indicates week/day, 2 indicates steps, 3 indicates distance, 4 indicates calories, 5 indicates heart rate, and 6 indicates battery
     * widgetIconColor Int Widget icon color (reserved 1 byte + R (1 byte) + G (1 byte) + B (1 byte))
     * widgetNumColor Int Color of the widget number (Reserve 1 byte + R (1 byte) + G (1 byte) + B (1 byte))
     * */
    private fun setWallpaperDialReply() {
        var wallpaperDialReply = Cmds.setWallpaperDialReply(
            IDOWallpaperDialReplyV3ParamModel(
                1,
                2,
                3,
                4,
                5,
                6,
                7,
            )
        )
        wallpaperDialReply.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = wallpaperDialReply.json

    }

    /**
     * v3 Voice reply text
     * [IDOVoiceReplyParamModel]
     * Attribute type declaration
     * flagIsContinue Int Indicates that recording continues
     * 0: stops recording. 1: continues recording
     * title String Title data. Maximum 31 bytes
     * textContent String Content data. The value is a maximum of 511 bytes
     * */
    private fun setVoiceReplyText() {
        var voiceReplyText = Cmds.setVoiceReplyText(
            IDOVoiceReplyParamModel(
                1, "test", "test"
            )
        )
        voiceReplyText.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = voiceReplyText.json

    }

    /**
     * Set time
     * [IDODateTimeParamModel]
     * Attribute type declaration
     * year Int Year
     * monuth Int Month
     * day Int Day
     * hour Int Hour
     * minute Int Minute
     * second Int second
     * week Int Week: Monday to Sunday 0-6
     * timeZone Int Time zone in 24-hour format: 0-12 for east, 13-24 for west (not enabled, set to 0)
     * */
    private fun setDateTime() {
        var dateTime = Cmds.setDateTime(
            IDODateTimeParamModel(
                2022, 10, 3, 5, 19, 16, 10, 29
            )
        )
        dateTime.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = dateTime.json

    }

    /**
     *
     * Set screen brightness
     * [IDOScreenBrightnessModel]
     * Attribute type declaration
     * level Int Brightness level
     * (0-100)
     * opera Int 0 Automatic
     * 1 Manual
     * For automatic synchronization, send 00; If it is a user adjustment, send 01
     * mode Int 0 Specifies the level
     * 1 Use an ambient light sensor
     * Level 2 is irrelevant
     * autoAdjustNight Int Adjustnight int Automatic brightness adjustment
     * 0 Invalid, defined by firmware
     * 1 Close
     * 2 Automatic brightness adjustment at night
     * 3 Night brightness reduction using the set time
     * startHour Int Start time Hour
     * startMinute Int Start time minutes
     * endHour Int End time hour
     * endMinute Int End time minute
     * nightLevel Int Night brightness
     * showInterval Int Display interval
     * */
    private fun setScreenBrightness() {
        var screenBrightness = Cmds.setScreenBrightness(
            IDOScreenBrightnessModel(
                20,
                0,
                0,
                3,
                18,
                0,
                23,
                0,
                20,
                0,
            )
        )
        screenBrightness.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = screenBrightness.json

    }

    /**
     * Motion switch setting
     * [IDOActivitySwitchParamModel]
     * Attribute type declaration
     * autoIdentifySportWalk Int Automatic identification walk switch
     * 0 off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * autoIdentifySportRun Int Automatically identifies the run switch
     * 0 off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * autoIdentifySportBicycle Int Automatic bicycle identification switch
     * 0 off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * autoPauseOnOff Int Indicates the automatic suspension of the movement
     * 0 off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * Autoendremindonoffoff Int End reminder
     * 0 off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * Automatic identification of autoIdentifySportElliptical Int elliptical machine switch
     * 0 off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * autoIdentifySportRowing Int Automatic identification rowing machine switch
     * 0 off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * autoIdentifySportSwim Int Automatic identification of swim switch
     * 0 off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * Automatic identification of autoIdentifySportSmartRope Int intelligent jump rope switch
     * 0 Off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * Automatic identification of autoIdentifyGeneralActivity Int? general activity switch (optional; omit to leave unchanged)
     * 0 Off
     * 1-mo
     * Menu getAutoActivitySetGetUseNewStructExchange
     * */
    private fun setActivitySwitch() {
        var activitySwitch = Cmds.setActivitySwitch(
            IDOActivitySwitchParamModel(
                0,
                0, 0,
                0, 0,
                0, 0,
                0,
                0
            )
        )
        activitySwitch.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = activitySwitch.json

    }


    /**
     * Set heart rate interval
     * [IDOHeartRateIntervalModel]
     * Attribute type declaration
     * burnFatThreshold int Heart rate interval for fat training
     * Calculation rule: 50% to 69% of the maximum heart rate
     * Unit: times/minute
     * aerobicThreshold int Heart rate interval of aerobicthreshold int Cardiopulmonary training
     * Calculation rule: 70% to 84% of the maximum heart rate
     * Unit: times/minute
     * limitThreshold int Peak training heart rate interval
     * Calculation rule: 85-100% of the maximum heart rate
     * Unit: times/minute
     * userMaxHr int Heart rate upper limit, maximum heart rate alert
     * Unit: times/minute
     * range1 int Warm-up heart rate range
     * Calculation rules: (220- age) * 50
     * Unit: times/minute
     * range2 int Fat burning heart rate range
     * Calculation rules: (220- age) * 60
     * Unit: times/minute
     * range3 int Aerobic heart rate range
     * Calculation rules: (220- age) * 70
     * Unit: times/minute
     * range4 int Heart rate range of anaerobic exercise
     * Calculation rules: (220- age) * 80
     * Unit: times/minute
     * range5 int Extreme exercise heart rate range
     * Calculation rules: (220- age) * 90
     * Unit: times/minute
     * minHr int Minimum heart rate
     * Unit: times/minute
     * maxHrRemind int Max heart rate reminder
     * 0 is off,1 is on
     * minHrRemind int Minimum heart rate reminder
     * 0 is off,1 is on
     * remindStartHour int when the reminder starts
     * remindStartMinute int Indicates the remindstartminute
     * remindStopHour int End of remindstophour int
     * remindStopMinute int Remindstopminute of remindstopminute
     * */
    private fun setHeartRateInterval() {
        var heartRateInterval = Cmds.setHeartRateInterval(
            IDOHeartRateIntervalModel(
                113,
                132,
                170,
                220,
                94,
                113,
                132,
                151,
                170,
                20,
                1, 0,
                0, 0, 23,
                59
            )
        )
        heartRateInterval.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }

        paramter_tv.text = heartRateInterval.json

    }

    /**
     * Set period
     * [IDOMenstruationModel]
     * Attribute type declaration
     * onOff int Period switch 1 On 0 Off
     * menstrualLength int Period length
     * menstrualCycle int Menstrual cycle
     * lastMenstrualYear int Last period start year
     * lastMenstrualMonth int Last period start month
     * lastMenstrualDay int Date when the last period started
     * ovulationIntervalDay int The interval between the start of the next period and the date of ovulation, usually 14 days
     * ovulationBeforeDay int Number of days before the ovulation date. The value is usually 5
     * ovulationAfterDay int The number of days after ovulation that the pregnancy is easy, usually 5
     * notifyFlag int Indicates the notification type
     * 0: invalid
     * 1: Allow notifications
     * 2: Silent notification
     * 3: close the notification needs to open firmware support getMenstrualAddNotifyFlagV3 table
     * menstrualReminderOnOff int Menstrual reminder switch Switch
     * 1: On
     * 0: Off
     * Need to open firmware table support getSupportSetMenstrualReminderOnOff when the switch was invalid, open will default to remind function.
     * */
    private fun setMenstruation() {
        var menstruation = Cmds.setMenstruation(
            IDOMenstruationModel(
                1, 7, 21, 2022, 12, 19, 15,
                5, 5, 1, 1
            )
        )
        menstruation.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = menstruation.json

    }


    /**
     * Calorie and distance goals
     * [IDOMainSportGoalModel]
     * Attribute type declaration
     * calorie Int Activity calorie target (in kilocalories)
     * Requires firmware to enable the "setCalorieGoal" function sheet
     * distance Int Target distance (m)
     * calorieMin Int Minimum active heat value
     * calorieMax Int Maximum active heat value
     * midHighTimeGoal Int Mid-high motion time goal (in seconds)
     * Requires firmware enable menu "setMidHighTimeGoal"
     * walkGoalTime Int Target Time (hours)
     * timeGoalType Int 0: invalid
     * 1: Daily goals
     * 2: Weekly goals
     * Need to enable menu getSupportSetGetTimeGoalTypeV2 firmware
     * */
    private fun setCalorieDistanceGoal() {
        var calorieDistanceGoal = Cmds.setCalorieDistanceGoal(
            IDOMainSportGoalModel(
                500, 200, 200, 666, 600, 600, 0,
            )
        )
        calorieDistanceGoal.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = calorieDistanceGoal.json

    }

    /**
     * [IDOWorldTimeParamModel]
     * Attribute type declaration
     * id Int Detailed ID, unique
     * minOffset Int Specifies the minute offset from the current time to UTC 0
     * cityName String City name. The value is a maximum of 59 bytes
     * longitudeFlag Int 1: East longitude 2: West longitude
     * longitude Int Longitude, multiplied by 100, keeping 2 decimal places
     * latitudeFlag Int 1: North latitude 2: south latitude
     * latitude Int Latitude, multiplied by 100, keeping 2 decimal places
     * */
    private fun setWorldTimeV3() {
        var worldTimeV3 = Cmds.setWorldTimeV3(
            listOf(
                IDOWorldTimeParamModel(
                    1,
                    2,
                    "Tokyo",
                    3,
                    4,
                    5,
                    6,
                    1,
                    (22.55*100).toInt(),
                    1,
                    (144.52*100).toInt()
                )
            )
        )
        worldTimeV3.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = worldTimeV3.json

    }

    /**
     * v3 Specifies the sports city name
     * cityName String City name
     * */
    private fun setLongCityNameV3() {
        var longCityNameV3 = Cmds.setLongCityNameV3("Shenzhen")
        longCityNameV3.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = longCityNameV3.json

    }

    /**
     * Menstrual history data delivery
     * [IDOHistoricalMenstruationParamModel]
     * Attribute type declaration
     * avgMenstrualDay Int Average menstrual cycle length unit: day
     * avgCycleDay Int Average menstrual cycle length unit: day
     * The items List IDOHistoricalMenstruationParamItem collection
     * [IDOHistoricalMenstruationParamItem]
     * Attribute type declaration
     * year Int The year in which the period begins
     * mon Int The month in which your period begins
     * day Int Day when your period starts
     * menstrualDay Int Menstrual Length (days)
     * cycleDay Int Cycle length (days)
     * ovulationIntervalDay Int The interval between the start of the next period and the day of ovulation is usually 14 days.
     * ovulationBeforeDay Int The number of days before ovulation day is usually 5 days at this time
     * ovulationAfterDay Int The number of days of conception after ovulation is usually 5 days
     * */
    private fun setHistoricalMenstruation() {
        var historicalMenstruation = Cmds.setHistoricalMenstruation(
            IDOHistoricalMenstruationParamModel(
                7, 29, listOf(
                    IDOHistoricalMenstruationParamItem(2023, 8, 1, 7, 30, 14, 5, 5)
                )
            )
        )
        historicalMenstruation.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = historicalMenstruation.json

    }

    /**
     * Set user information
     * [IDOUserInfoPramModel]
     * Attribute type declaration
     * year int Year of birth date
     * month int Date of birth month
     * day int Date of birth day
     * height int Height in centimeters
     * weight int Weight in kilograms requires x100
     * gender int Gender
     * 1: Female
     * 0: Male
     * */
    private fun setUserInfo() {
        var userInfo = Cmds.setUserInfo(IDOUserInfoPramModel(2022, 12, 16, 173, 7400, 0))
        userInfo.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = userInfo.json

    }

    /**
     * Do not disturb mode
     * [IDONotDisturbParamModel]
     * Attribute	Type	Description
     * switchFlag int Switch
     * 1 Turn on
     * 0 off
     * startHour int Start time
     * when
     * startMinute int Start time
     * points
     * endHour int End time
     * when
     * endMinute int End time
     * points
     * haveTimeRange int Specifies whether a time range exists
     * 0 invalid
     * 1 indicates that there is no time range
     * 2 indicates a time range
     * Open menu getSupportDisturbHaveRangRepeat effectively
     * noontimeRESTOnOff int Do not disturb switch during the day
     * 1 Turn on
     * 0 off
     * noontimeRESTStartHour int Start time
     * when
     * noontimeRESTStartMinute int Start time
     * points
     * noontimeRESTEndHour int End time
     * when
     * noontimeRESTEndMinute int End time
     * points
     * allDayOnOff int All day
     * 1 Turn on
     * 0 off
     * Open menu setOnlyNoDisturbAllDayOnOff effectively
     * intelligentOnOff int Intelligent do not disturb switch
     * 1 Turn on
     * 0 off
     * Open menu setOnlyNoDisturbSmartOnOff effectively
     * */
    private fun setNotDisturb() {
        var notDisturb = Cmds.setNotDisturb(
            IDONotDisturbParamModel(1, 15, 23, 30, 2, 127, 1, 9, 0, 12, 0, 0, 0)
        )
        notDisturb.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = notDisturb.json

    }

    /**
     * Set period reminders
     * [IDOMenstruationRemindParamModel]
     * Attribute type declaration
     * startDay int Indicates the number of days before the start date
     * ovulationDay int Number of days before ovulation day
     * hour int Indicates the reminder time
     * minute int Indicates the reminding time
     * pregnancyDayBeforeRemind int Indicates how many days in advance of the start of pregnancy
     * pregnancyDayEndRemind int How many days in advance of the end of pregnancy
     * menstrualDayEndRemind int Reminder of how many days before your period ends
     * */
    private fun setMenstruationRemind() {
        var menstruationRemind = Cmds.setMenstruationRemind(
            IDOMenstruationRemindParamModel(8, 8, 21, 0, 5, 5, 1)
        )
        menstruationRemind.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = menstruationRemind.json

    }


    /**
     *
     * Set pressure switch
     * [IDOStressSwitchParamModel]
     * Attribute	Type	Description
     * onOff int Main switch
     * 1 on 0 off
     * startHour int Indicates the start time
     * startMinute int Start time
     * endHour int Indicates the end time
     * endMinute int Indicates the end time
     * remindOnOff int Pressure reminder switch
     * 1 on, 0 off
     * If on_off is off, the notification does not take effect
     * interval int Alert interval. The unit minute is 60 minutes by default
     * highThreshold int High pressure threshold
     * stressThreshold int Pressure calibration threshold. The default value is 80
     * Need to open firmware support setSendCalibrationThreshold table
     * notifyFlag int Indicates the notification type
     * 0: invalid
     * 1: Allow notifications
     * 2: Silent notification
     * 3: Disable notification
     * Need to open firmware support getPressureNotifyFlagMode table
     * repeats Set IDOWeek
     * */
    private fun setStressSwitch() {
        var stressSwitch = Cmds.setStressSwitch(
            IDOStressSwitchParamModel(
                1, 14, 3, 20, 5, 1, 60, 170, 80, 1, hashSetOf(
                    IDOWeek.MONDAY,
                    IDOWeek.TUESDAY,
                    IDOWeek.WEDNESDAY,
                    IDOWeek.THURSDAY,
                    IDOWeek.FRIDAY,
                    IDOWeek.SATURDAY,
                    IDOWeek.SUNDAY
                )
            )
        )
        stressSwitch.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = stressSwitch.json

    }

    /**
     * Set a drink reminder
     * [IDODrinkWaterRemindModel]
     * Attribute	Type	Description
     * onOff int Switch
     * 0 stage
     * 1-mo
     * startHour int Indicates the start time
     * startMinute int Start time
     * endHour int Indicates the end time
     * endMinute int Indicates the end time
     * repeats Set IDOWeek sets
     * interval int Alert interval
     * Unit minute
     * notifyFlag int Indicates the notification type
     * 0: invalid
     * 1: Allow notifications
     * 2: Silent notification
     * 3: Disable notification
     * Need to open firmware support setDrinkWaterAddNotifyFlag table
     * doNotDisturbOnOff int No-reminder switch 00 Off 01 On
     * Need to open firmware support setNoReminderOnDrinkReminder table
     * noDisturbStartHour int Do not disturb the start time
     * Need to open firmware support setNoReminderOnDrinkReminder table
     * noDisturbStartMinute int Indicates the start time of the Disturb start-Minute int
     * Need to open firmware support setNoReminderOnDrinkReminder table
     * Nodo not Disturb Endhour int Indicates the end time of the do not remind
     * Need to open firmware support setNoReminderOnDrinkReminder table
     * Nodo not Disturb endMinute int Indicates the end time of the alarm
     * Need to open firmware support setNoReminderOnDrinkReminder table
     * */
    private fun setDrinkWaterRemind() {
        var drinkWaterRemind = Cmds.setDrinkWaterRemind(
            IDODrinkWaterRemindModel(
                1, 18, 7, 23, 12, hashSetOf(
                    IDOWeek.MONDAY,
                    IDOWeek.TUESDAY,
                    IDOWeek.WEDNESDAY,
                    IDOWeek.THURSDAY,
                    IDOWeek.FRIDAY,
                    IDOWeek.SATURDAY,
                ), 1, 1, 9, 0, 12, 0, 0
            )
        )
        drinkWaterRemind.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = drinkWaterRemind.json

    }

    /**
     * Set menu list
     * [IDOMenuListParamModel]
     * Attribute type declaration
     * items [int] Menu list with its own sorting
     * No sorting case, if there is a value, it is displayed, and no value is unrealistic
     * In the case of sorting, you need to start from 0 in the array
     * 0 Invalid 1 steps 2 Heart rate 3 Sleep 4 Photos 5 Alarm clock 6 Music 7 stopwatch 8 timer 9 Exercise mode 10 Weather
     * 11 Breathing exercises 12 Find phone 13 Pressure 14 data 3 15 Time interface 16 Last activity 17 Health data 18 Blood oxygen 19 menu Settings
     * 20 (20)alexa Voice display in sequence 21 X screen (gt01pro-X added) 22 calories (Doro Watch added) 23 Distance (Doro Watch added)
     * 24 One-click Measurement (New IDW05) 25 renpho health(New IDW12) 26 Compass (New mp01) 27 Barometric altimeter (new mp01)
     * */
    private fun setMenuList() {
        var menuList = Cmds.setMenuList(IDOMenuListParamModel(listOf(1, 2, 3, 4, 5, 6, 7)))
        menuList.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = menuList.json

    }

    /**
     * Set alarm
     * [IDOAlarmModel]
     * Attribute type declaration
     * items List IDOAlarmItem collection
     * [IDOAlarmItem]
     * Attribute type declaration
     * alarmID Int Alarm ID. The value starts from 1 and ranges from 1 to the maximum number of alarms supported
     * delayMin Int Delay a few minutes
     * hour Int Indicates the alarm clock
     * minute Int Indicates the minutes of the alarm clock
     * name String Alarm name. The value is a maximum of 23 bytes
     * isOpen Bool Switch
     * repeats	HashSet
     * Set() repeats the IDOWeek set
     * repeatTimes Int Specifies the number of times that the alarm clock repeats
     * The number of times the alarm clock is repeated, the delay switch, and the setting is 0.
     * Set to a certain number to repeat the number of times
     * status IDOAlarmStatus 0: hidden (deleted) 1: displayed -1: invalid
     * type IDOAlarmType Alarm type
     * */
    private fun setAlarmV3() {
        Cmds.getAlarm().send {
            var idoAlarmModel: IDOAlarmItem = it.res?.items?.get(0) as IDOAlarmItem
            idoAlarmModel.hour = 9
            idoAlarmModel.alarmId =1
            idoAlarmModel.minute = 10
            idoAlarmModel.name = "dddd"
            idoAlarmModel.repeatTimes = 1;
            idoAlarmModel.isOpen = true
            Log.d("TAG", "setAlarmV3: ${it.res?.items.toString()}")
            var idoAlarmModel1: IDOAlarmItem = it.res?.items?.get(1) as IDOAlarmItem
            idoAlarmModel1.isOpen = false
            idoAlarmModel1.alarmId =2
            idoAlarmModel1.status = IDOAlarmStatus.HIDDEN;
            var idoAlarmModel2: IDOAlarmItem = it.res?.items?.get(2) as IDOAlarmItem
            idoAlarmModel2.isOpen = false
            idoAlarmModel2.status = IDOAlarmStatus.HIDDEN;
            idoAlarmModel2.alarmId =3

            var idoAlarmModel3: IDOAlarmItem = it.res?.items?.get(3) as IDOAlarmItem
            idoAlarmModel3.isOpen = false
            idoAlarmModel3.status = IDOAlarmStatus.HIDDEN;
            idoAlarmModel3.alarmId =4

            var idoAlarmModel4: IDOAlarmItem = it.res?.items?.get(4) as IDOAlarmItem
            idoAlarmModel4.isOpen = false
            idoAlarmModel4.status = IDOAlarmStatus.HIDDEN;
            idoAlarmModel4.alarmId =5

            var idoAlarmModel5: IDOAlarmItem = it.res?.items?.get(5) as IDOAlarmItem
            idoAlarmModel5.isOpen = false
            idoAlarmModel5.status = IDOAlarmStatus.HIDDEN;
            idoAlarmModel5.alarmId =6


            var idoAlarmModel6: IDOAlarmItem = it.res?.items?.get(6) as IDOAlarmItem
            idoAlarmModel6.isOpen = false
            idoAlarmModel6.status = IDOAlarmStatus.HIDDEN;
            idoAlarmModel6.alarmId =7

            var idoAlarmModel7: IDOAlarmItem = it.res?.items?.get(7) as IDOAlarmItem
            idoAlarmModel7.isOpen = false
            idoAlarmModel7.status = IDOAlarmStatus.HIDDEN;
            idoAlarmModel7.alarmId =8

            var idoAlarmModel8: IDOAlarmItem = it.res?.items?.get(8) as IDOAlarmItem
            idoAlarmModel8.isOpen = false
            idoAlarmModel8.status = IDOAlarmStatus.HIDDEN;
            idoAlarmModel8.alarmId =9

            var idoAlarmModel9: IDOAlarmItem = it.res?.items?.get(9) as IDOAlarmItem
            idoAlarmModel9.isOpen = false
            idoAlarmModel9.status = IDOAlarmStatus.HIDDEN;
            idoAlarmModel9.alarmId =10

            var alarmV3 = Cmds.setAlarmV3(
                IDOAlarmModel(
                    listOf(
                        idoAlarmModel,
                        idoAlarmModel1,
                        idoAlarmModel2,
                        idoAlarmModel3,
                        idoAlarmModel4,
                        idoAlarmModel5,
                        idoAlarmModel6,
                        idoAlarmModel7,
                        idoAlarmModel8,
                        idoAlarmModel9
                    )
                )
            )



            alarmV3.send {
                if (it.error.code == 0) {
                    tv_response.text = it.res?.toJsonString()
                } else {
                    tv_response.text = "Setup failure"
                }
            }
            paramter_tv.text = alarmV3.json
        }
    }

    /**
     * Set the motion mode sort
     * [IDOSportModeSortParamModel]
     * Attribute type declaration
     * index Int Sort index (starting from 1, 0 is invalid)
     * type IDOSportType Sports type
     * */
    private fun setSportModeSort() {
        var sportModeSort = Cmds.setSportModeSort(
            listOf(
                IDOSportModeSortParamModel(
                    1,
                    IDOSportType.SPORTTYPEAEROBICS
                )
            )
        )
        sportModeSort.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = sportModeSort.json

    }

    /**
     * v3 app set the quick reply message
     * [IDOFastMsgSettingModel]
     * Attribute type declaration
     * type Int 0 Other (default), 1 Version of the incoming call message
     * fastItems List Quick message details
     * [IDOFastMsgItem]
     * Attribute type declaration
     * msgId Int Fast message id 1 Start
     * msgData String Fast message content contains a maximum of 68 bytes
     * */
    private fun setDefaultQuickMsgReplyList() {
        val param = IDOFastMsgSettingModel(
            0, listOf(
                IDOFastMsgItem(1, "test1"),
                IDOFastMsgItem(2, "test2"),
                IDOFastMsgItem(3, "test3"),
                IDOFastMsgItem(4, "test4"),
                IDOFastMsgItem(5, "test5")
            )
        )
        Cmds.setDefaultQuickMsgReplyList(param).send {
            if (it.error.code == 0) {
                // Succeeded
                // it.res is IDOCmdSetResponseModel
            } else {
                // Failed
            }
        }
    }

    /**
     * Quick reply message with details reference NotificationIconTransferActivity. Kt
     * [IDOFastMsgUpdateParamModel]
     * Attribute type declaration
     * isSuccess int 0app failed to send the message, 1app succeeded in sending the message
     * msgID int Reply ID: Each message corresponds to an ID
     * msgType int Indicates the message type
     * msgNotice int 0 indicates that there is no corresponding SMS reply, corresponding to the reply list
     * */
    private fun setFastMsgUpdate() {
        var fastMsgUpdate = Cmds.setFastMsgUpdate(IDOFastMsgUpdateParamModel(1, 1, 1, 1))
        fastMsgUpdate.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = fastMsgUpdate.json

    }

    /**
     * [IDOMusicOpearteParamModel]
     * [IDOMusicOpearteParamModel]
     * Attribute type declaration
     * musicOperate Int Indicates the music operation
     * 0: invalid operation
     * 1: Delete music
     * 2: Add music
     * folderOperate Int The operation of the folder (playlist)
     * 0: invalid operation
     * 1: Delete the folder
     * 2: Add a folder
     * 3: Modify the playlist
     * 4: Import the playlist
     * 5: Delete music
     * folderItems IDOMusicFolderItem Details about music
     * musicItems IDOMusicItem Folder details
     * [DOMusicOperateModel]
     * Attribute type declaration
     * operateType Int Operation type:
     * 0: invalid operation
     * 1: Delete music
     * 2: Add music
     * 3: Delete the folder
     * 4: Add a folder
     * 5: Modify the playlist
     * 6: Import the playlist
     * 7: Delete music from the playlist
     * version Int Firmware SDK card information
     * Total space
     * errCode Int 0: Success; Non-zero: failure
     * musicId Int The music id is returned after adding music successfully
     * folderItems List IDOMusicFolderItem collection
     * [DOMusicFolderItem]
     * Attribute type declaration
     * folderID Int Playlist (folder) id. The value ranges from 1 to 10
     * musicNum Int Number of songs in a playlist, up to 100
     * folderName String Name of the playlist (folder). The value is a maximum of 19 bytes
     * musicIndex List Indicates the ids of the songs in the list. The ids are listed in the order in which the songs are added
     */
    private fun setMusicOperate() {
        var musicOperate = Cmds.setMusicOperate(
            IDOMusicOpearteParamModel(
                0, 2,
                IDOMusicFolderItem(
                    folderId = 1, musicNum = 0, folderName = "kd", musicIndex = listOf()
                ), null
            )
        )
        musicOperate.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = musicOperate.json

    }

    /**
     *
     * Set weather data
     * [IDOWeatherDataParamModel]
     * type Int Weather type
     * 0x00 Other
     * 0x01 Sunny
     * 0x02 Negative
     * 0x03 negative
     * 0x04 Rain
     * 0x05 Heavy rain
     * 0x06 Thunderstorm
     * 0x07 Snow
     * 0x08 Sleet
     * 0x09 Typhoon
     * 0x0A Dust storm
     * 0x0B Clear night
     * 0x0C Cloudy night
     * 0x0D Hot
     * 0x0E Cold
     * 0x0F Breeze
     * 0x10 Strong Wind
     * 0x11 Haze
     * 0x12 Shower
     * 0x13 Cloudy to sunny
     * 0x30 Thunder
     * 0x31 Hail
     * 0x32 Dust
     * 0x33 Tornado
     * temp Int Indicates the current temperature
     * maxTemp Int The maximum temperature of the day
     * minTemp Int The minimum temperature of the day
     * humidity Int Current humidity
     * uvIntensity Int Current UV intensity
     * aqi Int Current Air Quality Index (AQI)
     * future [IDOWeatherDataFuture] The weather for the next three days
     *
     * */
    private fun setWeatherData() {
        var weatherData = Cmds.setWeatherData(
            IDOWeatherDataParamModel(
                10, 4, 10, 19, 10, 4, 5, listOf(
                    IDOWeatherDataFuture(
                        0, 0, 0
                    )
                )
            )
        )
        weatherData.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = weatherData.json

    }


    /**
     * Blood pressure measurement
     * [IDOBpMeasurementParamModel]
     * flag Int 1: Start measurement
     * 2: End the measurement
     * 3: Obtain blood pressure data
     * */
    private fun setBpMeasurement() {
        var bpMeasurement = Cmds.setBpMeasurement(
            IDOBpMeasurementParamModel(
                1,
            )
        )
        bpMeasurement.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }

        paramter_tv.text = bpMeasurement.json

    }

    /**
     * Blood pressure calibration
     * [IDOBpCalibrationParamModel]
     * flag Int 1: Blood pressure calibration Settings
     * 2: Blood pressure calibration query result
     * diastolic Int Systolic blood pressure
     * systolic Int diastolic blood pressure
     * */
    private fun setBpCalibration() {
        var bpCalibration = Cmds.setBpCalibration(
            IDOBpCalibrationParamModel(
                1, 0, 0
            )
        )
        bpCalibration.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = bpCalibration.json

    }

    /**
     * Set reminders to wash your hands
     * [IDOHandWashingReminderParamModel]
     * onOff Int 0: off
     * 1: Open
     * Default off
     * startHour Int Indicates the start time of the reminder
     * startMinute Int Indicates the start minute of the reminder
     * endHour Int Indicates the end time of reminding
     * endMinute Int Indicates the minute when the reminder ends
     * repeats Set IDOWeek sets
     * interval Int Reminder interval (minutes)
     * The default value is 60 minutes
     *
     * */
    private fun setHandWashingReminder() {
        var handWashingReminder = Cmds.setHandWashingReminder(
            IDOHandWashingReminderParamModel(
                0, 0, 0, 23, 59, hashSetOf(
                    IDOWeek.MONDAY,
                    IDOWeek.TUESDAY,
                    IDOWeek.WEDNESDAY,
                    IDOWeek.THURSDAY,
                    IDOWeek.FRIDAY,
                    IDOWeek.SATURDAY,
                ), 2
            )
        )
        handWashingReminder.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = handWashingReminder.json

    }

    /**
     * v3 Blood pressure calibration control
     * [IDOBpCalControlModel]
     * errorCode Int Error codes: 0 indicates success, and non-0 indicates failure
     * operate Int Operate int
     * 0: invalid
     * 1: Start blood pressure calibration
     * 2: Stop blood pressure calibration
     * 3: Obtain the feature vector
     * sbpPpgFeatureNum Int Number of PPG feature vectors in hypertension
     * This operation takes effect when operate=3
     * dbpPpgFeatureNum Int Number of hypotensive PPG feature vectors
     * This operation takes effect when operate=3
     * sbpPpgFeatureItems List A collection of PPG feature vectors for hypertension
     * dbpPpgFeatureItems List A collection of hypotensive PPG feature vectors
     * */
    private fun setBpCalControlV3() {
        var bpCalControlV3 = Cmds.setBpCalControlV3(
            IDOBpCalControlModel(
                1, 0, 0, 23, listOf(
                    0, 0, 1
                ), listOf(
                    0, 0, 1
                )
            )
        )
        bpCalControlV3.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = bpCalControlV3.json

    }


    /**
     * -------------------------------------
     * v3 Sets the motion subitem data arrangement
     * operate Int Operate int
     * 0: invalid 1: query 2: set
     * sportType IDOSportType Sports type
     * nowUserLocation Int Displays the current location of the add movement
     * items [Int] The maximum number of items in the motion sorting list is 150
     * */
    private fun setSportParamSort() {
        var sportParamSort = Cmds.setSportParamSort(
            IDOSportSortParamModel(
                1,
                IDOSportType.SPORTTYPEAEROBICS,
                1,
                listOf(
                    1,
                    2,
                )
            )
        )
        sportParamSort.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = sportParamSort.json

    }


    /**
     * Set the shortcut return switch for incoming calls
     * */
    private fun setCallQuickReplyOnOff() {
        var callQuickReplyOnOff = Cmds.setCallQuickReplyOnOff(true)
        callQuickReplyOnOff.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = callQuickReplyOnOff.json

    }


    /**
     * Set the phone voice assistant switch
     * */
    private fun setVoiceAssistantOnOff() {
        var voiceAssistantOnOff = Cmds.setVoiceAssistantOnOff(true)
        voiceAssistantOnOff.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = voiceAssistantOnOff.json

    }


    /**
     * Set motion sort
     * [IDOSportModeSortParamModel]
     * index Int Sort index (starting from 1, 0 is invalid)
     * type IDOSportType Sports type
     *
     * [IDOSportParamModel]
     * num number
     * Items: List < IDOSportModeSortParamModel > movement type sorting details (30)
     * */
    private fun setSportSortV3() {
        var sportSortV3 = Cmds.setSportSortV3(
            IDOSportParamModel(
                listOf(IDOSportModeSortParamModel(1, IDOSportType.SPORTTYPEBURPEE))
            )
        )
        sportSortV3.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }
        paramter_tv.text = sportSortV3.json

    }


    /**
     * Pressure calibration
     * [IDOStressCalibrationParamModel]
     * stressScore Int Stressscore, which ranges from 1 to 10
     * status Int 0: Start calibration Settings 1: cancel calibration Settings
     *
     * [IDOStressCalibrationModel]
     * retCode Int 0: Into 1: failed - Calibrating 2: failed - Charging failed - not wearing 4: failed - Sports scenario
     *
     * */
    private fun setStressCalibration() {
        var stressCalibration = Cmds.setStressCalibration(
            IDOStressCalibrationParamModel(
                1,
                0,
            )
        )
        stressCalibration.send {
            if (it.error.code == 0) {
                tv_response.text = it.res?.toJsonString()
            } else {
                tv_response.text = "Setup failure"
            }
        }

        paramter_tv.text = stressCalibration.json
    }


    /**
     * Send a small program operation
     * operate Int 0: invalid 1: Starts the applet 2: deletes the applet 3: Gets the list of installed applet
     * appName String Applet name operate=0/operate=3 Invalid. No name is required to obtain the operation. The value contains a maximum of 29 bytes
     */
    private fun setAppletControl() {
        Cmds.setAppletControl(IDOAppletControlModel(3, "")).send { data ->
            if (data.error.code == 0) {

                var appName = data.res?.infoItem?.get(0)?.appName
                Cmds.setAppletControl(IDOAppletControlModel(1, appName)).send {
                    paramter_tv.text = IDOAppletControlModel(1, appName).toJsonString()
                    if (it.error.code == 0) {
                        tv_response.text =
                            it.res?.toJsonString() + "  \n" + data.res?.toJsonString()
                    }
                }
            }
        }
    }


}
