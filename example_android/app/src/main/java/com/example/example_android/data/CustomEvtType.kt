package com.example.example_android.data

// !!! Demo defines the corresponding event enum here just to list the related APIs conveniently;
// it is not needed in real scenarios (just call the corresponding API as required)
enum class CustomEvtType {
    /**  Get MAC address*/
    GETMAC,
    /**  Get device info*/
    GETDEVICEINFO,
    /**  Get function table*/
    GETFUNCTABLE,
    /**  Get function table (extended)*/
    GETFUNCTABLEEX,
    /**  Get sn*/
    GETSNINFO,
    /**  Get unit*/
    GETUNIT,
    /**  Get BT Bluetooth name*/
    GETBTNAME,
    /**  Get real-time data*/
    GETLIVEDATA,
    /**  Get notification center status*/
    GETNOTICESTATUS,
    /**  Get heart rate sensor parameters*/
    GETHRSENSORPARAM,
    /**  Get accelerometer sensor parameters*/
    GETGSENSORPARAM,
    /**  Get sync timeline to calculate percentage*/
    GETACTIVITYCOUNT,
    /**  Get HID info*/
    GETHIDINFO,
    /**  Get GPS info*/
    GETGPSINFO,
    /**  Get GPS status*/
    GETGPSSTATUS,
    /**  Get hot start parameters*/
    GETHOTSTARTPARAM,
    /**  Get version info*/
    GETVERSIONINFO,
    /**  Get MTU info*/
    GETMTUINFO,
    /**  Get Do Not Disturb mode status*/
    GETNOTDISTURBSTATUS,
    /**  Get default sport types*/
    GETDEFAULTSPORTTYPE,
    /**  Get supported download languages*/
    GETDOWNLANGUAGE,
    /**  Get error records*/
    GETERRORRECORD,
    /**  Get the set calorie/distance/medium-high intensity sport duration goals (main screen)*/
    GETMAINSPORTGOAL,
    /**  Get walk reminder*/
    GETWALKREMIND,
    /**  Get battery info*/
    GETBATTERYINFO,
    /**  Get font library info*/
    GETFLASHBININFO,
    /**  Get the list supported by the device*/
    GETMENULIST,
    /**  Get hardware info for the realme project*/
    GETREALMEDEVINFO,
    /**  Get screen brightness*/
    GETSCREENBRIGHTNESS,
    /**  Get wrist lift data*/
    GETUPHANDGESTURE,
    /**  Get authorization data*/
    GETENCRYPTEDCODE,
    /**  Get device upgrade status*/
    GETUPDATESTATUS,
    /**  Get watch face id*/
    GETWATCHDIALID,
    /**  Get screen info*/
    GETWATCHDIALINFO,
    /**  Get watch name*/
    GETDEVICENAME,
    /**  Get device log status*/
    GETDEVICELOGSTATE,
    /**  Get all health monitoring switches*/
    GETALLHEALTHSWITCHSTATE,
    /**  Get file transfer config*/
    GETDATATRANCONFIG,
    /**  Get sport mode auto-recognition switch*/
    GETACTIVITYSWITCH,
    /**  Get firmware 3-level version and BT 3-level version*/
    GETFIRMWAREBTVERSION,
    /**  Get stress value*/
    GETSTRESSVAL,
    /**  Event number for getting blood pressure algorithm 3-level version info*/
    GETBPALGVERSION,
    /**  Get heart rate monitoring mode*/
    GETHEARTRATEMODE,
    /**  Get all-day step goal*/
    GETSTEPGOAL,
    /**  Get lib version number*/
    GETLIBVERSION,
    /**  Get appid ios only*/
    GETAPPID,
    /**  Get watch face list*/
    GETWATCHFACELIST,
    /**  Query BT pairing switch, connection, a2dp connection, hfp connection status (only devices with BT)*/
    GETBTNOTICE,
    /**  v3 Get default sport types*/
    GETSPORTTYPEV3,
    /**  v3 app gets ble alarms*/
    GETALARMV3,
    /**  v3 Get device font library list*/
    GETLANGUAGELIBRARYDATAV3,
    /**  v3 Get watch face list*/
    GETWATCHLISTV3,
    /**  v3 alexa gets v3 alarms*/
    GETALEXAALARMV3,
    /**  v3 Get GPS data for a time period during multi-sport data exchange*/
    GETACTIVITYEXCHANGEGPSDATA,
    /**  Get song names and folders from the firmware*/
    GETBLEMUSICINFO,
    /**  Send menstrual cycle history data*/
    GETHISTORICALMENSTRUATION,
    /**  Get app package name*/
    GETPACKNAME,
    /**  v3 Get firmware local prompt sound file info*/
    GETBLEBEEPV3,
    /**  v3 Get firmware local prompt sound file info*/
    GETHABITINFOV3,
    /**  Get the max number of detail items the firmware supports*/
    GETSUPPORTMAXSETITEMSNUM,
    /**  Get the list of quick apps that cannot be deleted from the firmware*/
    GETUNERASABLEMEUNLIST,
    /**  Get firmware SPP MTU length (new, SPP Bluetooth only)*/
    GETMTULENGTHSPP,
    /**  Get unread red dot reminder switch*/
    GETUNREADAPPREMINDER,
    /**  Get the modification time of the contacts file saved on the firmware*/
    GETCONTACTREVISETIME,
    /**  Start getting flash log*/
    GETFLASHLOGSTART,
    /**  Get battery log*/
    GETBATTERYLOG,
    /**  app sends running plan (sport plan)*/
    SETSENDRUNPLAN,
    /**  Set watch face order*/
    SETWATCHDIALSORT,
    /**  v3 Blood pressure calibration control*/
    SETBPCALCONTROLV3,
    /**  Set multiple walk reminder times*/
    SETWALKREMINDTIMES,
    /**  v3 Send v3 world time*/
    SETWORLDTIMEV3,
    /**  v3 Set GPS hot start parameters*/
    SETHOTSTARTPARAMV3,
    /**  v3 Send v3 weather protocol*/
    SETWEATHERV3,
    /**  v3 Set message notification status*/
    SETNOTICEMESSAGESTATE,
    /**  v3 Set sport city name*/
    SETLONGCITYNAMEV3,
    /**  v3 Set sport sub-item data order*/
    SETBASESPORTPARAMSORTV3,
    /**  v3 Set main screen widget order*/
    SETMAINUISORTV3,
    /**  v3 Set schedule reminder*/
    SETSCHEDULERREMINDERV3,
    /**  v3 New order for the 100 sport types*/
    SET100SPORTSORTV3,
    /**  v3 Set wallpaper watch face list*/
    SETWALLPAPERDIALREPLYV3,
    /**  v3 alexa sets v3 alarms*/
    SETALEXAALARMV3,
    /**  v3 Set reminder by voice*/
    SETVOICEALARMREMINDERV3,
    /**  v3 Set alarm data by voice*/
    SETVOICEALARMV3,
    /**  v3 Voice reply text*/
    SETVOICEREPLYTXTV3,
    /**  v3 app sets quick reply messages*/
    SETFASTMSGV3,
    /**  Set sport type order (v3 protocol)*/
    SETSPORTSORTV3,
    /**  v3 app sets ble alarms*/
    SETALARMV3,
    /**  Set watch face*/
    SETWATCHFACEDATA,
    /**  Operate on songs or folders*/
    SETMUSICOPERATE,
    /**  Quick SMS reply*/
    SETFASTMSGUPDATE,
    /**  Send file transfer config*/
    SETDATATRANCONFIGURE,
    /**  gt01_pro app new requirement: unread message red dot switch*/
    SETUNREADAPPREMINDER,
    /**  The phone app uses this command switch to set the notification app status*/
    SETNOTIFICATIONSTATUS,
    /**  Fitness guidance*/
    SETFITNESSGUIDANCE,
    /**  Set scientific sleep switch*/
    SETSCIENTIFICSLEEPSWITCH,
    /**  Set night body temperature switch*/
    SETTEMPERATURESWITCH,
    /**  Ambient volume switch and threshold*/
    SETV3NOISE,
    /**  Set heart rate mode*/
    SETHEARTMODE,
    /**  Set smart heart rate mode*/
    SETHEARTRATEMODESMART,
    /**  Measure SpO2/stress once*/
    SETONCETESTSPO2PRESSURE,
    /**  Set medication reminder*/
    SETTAKINGMEDICINEREMINDER,
    /**  Set device name*/
    SETDEVICESNAME,
    /**  Delete log*/
    SETCLEAROPERATION,
    /**  Send phone volume to ble*/
    SETBLEVOICE,
    /**  Phone sets screen color (not in function table)*/
    SETBLESCREENCOLOR,
    /**  app controls the watch vibration intensity*/
    SETBLESHARKINGGRADE,
    /**  Set sedentary reminder*/
    SETLONGSIT,
    /**  Set anti-lost*/
    SETLOSTFIND,
    /**  Set time*/
    SETTIME,
    /**  Set sport goal*/
    SETSPORTGOAL,
    /**  Set sleep goal*/
    SETSLEEPGOAL,
    /**  Set user info*/
    SETUSERINFO,
    /**  Set unit*/
    SETUNIT,
    /**  Set left/right hand*/
    SETHAND,
    /**  Set app system*/
    SETAPPOS,
    /**  Set notification center*/
    SETNOTIFICATIONCENTER,
    /**  Set heart rate zones*/
    SETHEARTRATEINTERVAL,
    /**  Set heart rate mode*/
    SETHEARTRATEMODE,
    /**  Restore default config*/
    SETDEFAULTCONFIG,
    /**  Bind*/
    SETBINDSTART,
    /**  Unbind*/
    SETBINDREMOVE,
    /**  Bind with auth code*/
    SETAUTHCODE,
    /**  Send computed authorization data*/
    SETENCRYPTEDAUTH,
    /**  Wrist lift to wake screen*/
    SETUPHANDGESTURE,
    /**  Set screen brightness*/
    SETSCREENBRIGHTNESS,
    /**  Set find phone*/
    SETFINDPHONE,
    /**  Do Not Disturb mode*/
    SETNOTDISTURB,
    /**  Music switch*/
    SETMUSICONOFF,
    /**  Display mode*/
    SETDISPLAYMODE,
    /**  Set heart rate sensor parameters*/
    SETHRSENSORPARAM,
    /**  Set accelerometer sensor parameters*/
    SETGSENSORPARAM,
    /**  Set sensor real-time data*/
    SETREALTIMESENSORDATA,
    /**  Set motor*/
    SETSTARTMOTOR,
    /**  Set one-key SOS call*/
    SETONEKEYSOS,
    /**  Set weather switch*/
    SETWEATHERSWITCH,
    /**  Set sport mode selection*/
    SETSPORTMODESELECT,
    /**  Set sleep period*/
    SETSLEEPPERIOD,
    /**  Set weather data*/
    SETWEATHERDATA,
    /**  Set weather city name*/
    SETWEATHERCITYNAME,
    /**  Set today's 0~8h real-time weather + temperature*/
    SETWEATHERREALTIMEONE,
    /**  Set today's 9~16h real-time weather + temperature*/
    SETWEATHERREALTIMETWO,
    /**  Set today's 17~24h real-time weather + temperature*/
    SETWEATHERREALTIMETHREE,
    /**  Set sunrise/sunset time*/
    SETWEATHERSUNTIME,
    /**  Firmware test*/
    SETDEVICESTESTTYPE,
    /**  Set IoT button*/
    SETIOTBUTTONNAME,
    /**  IoT button command*/
    SETIOTCOMMAND,
    /**  Set watch face*/
    SETWATCHDIAL,
    /**  Set shortcuts*/
    SETSHORTCUT,
    /**  Blood pressure calibration*/
    SETBPCALIBRATION,
    /**  Blood pressure measurement*/
    SETBPMEASUREMENT,
    /**  Stress calibration*/
    SETSTRESSCALIBRATION,
    /**  Set GPS info*/
    SETGPSCONFIG,
    /**  ```*/
    SETGPSCONTROL,
    /**  Control connection parameters*/
    SETCONNPARAM,
    /**  Set hot start parameters*/
    SETHOTSTARTPARAM,
    /**  Set menstrual cycle*/
    SETMENSTRUATION,
    /**  Set menstrual cycle reminder*/
    SETMENSTRUATIONREMIND,
    /**  Calorie and distance goals*/
    SETCALORIEDISTANCEGOAL,
    /**  Set SpO2 switch*/
    SETSPO2SWITCH,
    /**  Set stress switch*/
    SETSTRESSSWITCH,
    /**  End incoming call*/
    SETNOTICESTOPCALL,
    /**  Missed call*/
    SETNOTICEMISSEDCALL,
    /**  Hang up during call*/
    SETNOTICEHANGUPINCALL,
    /**  Send call duration to the firmware after the incoming call is answered*/
    SETNOTICECALLTIME,
    /**  Set sport mode order*/
    SETSPORTMODESORT,
    /**  Set walk reminder*/
    SETWALKREMIND,
    /**  Breathing training*/
    SETBREATHETRAIN,
    /**  Set sport switch*/
    SETACTIVITYSWITCH,
    /**  Set drink-water reminder*/
    SETDRINKWATERREMIND,
    /**  Band requests version check*/
    SETREQUESTCHECKUPDATE,
    /**  Band requests ota*/
    SETREQUESTSTARTOTA,
    /**  Stop find phone*/
    SETOVERFINDPHONE,
    /**  Set hand-washing reminder*/
    SETHANDWASHINGREMINDER,
    /**  Set respiratory rate switch*/
    SETRRESPIRATETURN,
    /**  Set body battery switch*/
    SETBODYPOWERTURN,
    /**  v3 alexa sets weather*/
    SETALEXAWEATHER,
    /**  V3 dynamic message notification*/
    SETNOTICEAPPNAME,
    /**  app transfers sport icon info and status to notify the firmware*/
    SETNOTICEICONINFORMATION,
    /**  app notifies the firmware to enable BT broadcast*/
    SETNOTICEOPENBROADCASTN,
    /**  app permissions were disabled so some functions cannot be enabled; the firmware must also be told that the function is disabled*/
    SETNOTICEDISABLEFUNC,
    /**  Sync frequently used contacts*/
    SETSYNCCONTACT,
    /**  app starts sport*/
    EXCHANGEAPPSTART,
    /**  app ends sport*/
    EXCHANGEAPPEND,
    /**  app pauses sport*/
    EXCHANGEAPPPAUSE,
    /**  app resumes sport*/
    EXCHANGEAPPRESTORE,
    /**  app sport plan operation*/
    EXCHANGEAPPPLAN,
    /**  app initiates v2 data exchange process*/
    EXCHANGEAPPV2ING,
    /**  app initiates v3 data exchange process*/
    EXCHANGEAPPV3ING,
    /**  app gets v3 heart rate data*/
    EXCHANGEAPPGETV3HRDATA,
    /**  app gets v3 sport data*/
    EXCHANGEAPPGETACTIVITYDATA,
    /**  ble starts sport or operates the sport plan, app replies*/
    EXCHANGEAPPBLEPLAN,
    /**  Bluetooth device gets phone login status*/
    ALEXAVOICEBLEGETPHONELOGINSTATE,
    /**  Simple file operation*/
    FUNCSIMPLEFILEOPT,
    /**  Set menu list*/
    SETMENULIST,
    /**  Control taking photos*/
    SETTAKEPICTURE,
    /**  Connected*/
    CONNECTED,
    /**  Disconnect*/
    DISCONNECT,
    /**  Enter upgrade mode*/
    OTASTART,
    /**  ota authorization*/
    OTAAUTH,
    /**  Enter upgrade mode directly (ignore battery level)*/
    OTADIRECTSTART,
    /**  Enter shutdown mode*/
    SYSTEMOFF,
    /**  Reboot device*/
    REBOOT,
    /**  Control disconnect*/
    CONTROLDISCONNECT,
    /**  Clear binding info*/
    CLEANBINDINFO,
    /**  Shut down device*/
    SHUTDOWN,
    /**  Factory reset*/
    FACTORYRESET,
    /**  Clear band cache*/
    CLEARCACHE,
    /**  Set alarm*/
    SETALARM,
    /**  Set alarm*/
    SETADDALARM,
    /**  Start (app -> ble)*/
    MUSICSTART,
    /**  Stop (app -> ble)*/
    MUSICSTOP,
    /**  Start taking photos (app -> ble)*/
    PHOTOSTART,
    /**  Stop taking photos (app -> ble)*/
    PHOTOSTOP,
    /**  Find device (app -> ble)*/
    FINDDEVICESTART,
    /**  Stop finding device (app -> ble)*/
    FINDDEVICESTOP,
    /**  Enable ancs*/
    OPENANCS,
    /**  Disable ancs*/
    CLOSEANCS,
    /**  APP sets alexa notification status*/
    SETALEXAINDECATOR,
    /**  APP sets the post-recognition status*/
    SETRECOGNITIONSTATE,
    /**  APP sends alexa control command*/
    SETVOICECONTROLUI,
    /**  APP sends alexa countdown control*/
    SETCONTROLLCOUNTDOWN,
    /**  APP sends alexa multi-sport screen*/
    SETCONTROLLSPORTS,
    /**  APP sends alexa command to jump to a ui screen*/
    SETCONTROLLUIJUMP,
    /**  APP sends alexa music control screen*/
    SETCONTROLLALEXAMUSIC,
    /**  APP sets alexa on/off command*/
    SETCONTROLLALEXAONOFF,
    /**  APP starts the timer function*/
    SETCONTROLLALEXASTOPWATCH,
    /**  Voice control jumps to the alarm screen*/
    SETCONTROLLALEXAALARM,
    /**  Control music (v3 protocol)*/
    MUSICCONTROL,
    /**  Notification message reminder (v3 protocol)*/
    NOTICEMESSAGEV3,
    /**  Internal test*/
    INNERTESTCMD1,
    /**  Set incoming call quick reply switch*/
    SETCALLQUICKREPLYONOFF,
    /**  Set phone voice assistant switch*/
    SETVOICEASSISTANTONOFF,
    /** Get BT-connected phone model*/
    GETBTCONNECTPHONEMODEL,
    /** Operate applet info*/
    SETAPPLETCONTROL,
    /**Get firmware algorithm file info (ACC/GPS)*/
    GETALGFILE,
    /**Request firmware algorithm file info (ACC/GPS)*/
    REQUESTALGFILE,
        /**  Set prompt sound switch during sport*/
    SETDURINGEXERCISE,
    /**  Get left/right hand wearing setting*/
    GETLEFTRIGHTWEARSETTINGS,
    /**  Get prompt sound switch during sport*/
    GETSETTINGSDURINGEXERCISE,
    /**  Set reminders during sport*/
    SETSPORTINGREMINDSETTING,
    /**  Set simple heart rate zones*/
    SETSIMPLEHEARTRATEZONE,
    /**  Get user info*/
    GETUSERINFO,
    /**  Get simple heart rate zone settings*/
    GETSIMPLEHEARTRATEZONE,
    GETBIKELOCKLIST,
    SETHISTORICALMENSTRUATION,
    GETFINDPHONESWITCH,
    /**  Get Find My device name (V3 15.113)*/
    GETFINDMYDEVICENAME,
    /**  Query sport types that support auto pause/end (V3 15.111)*/
    GETSPORTAUTOPAUSEENDTYPES,
    GETHEARTMODE,
    SETSPORTSTYPEV3,
    GETSMARTHEARTRATEMODE,
    GETSPORTINGREMINDSETTING,
    GETMENULISTV3,
    SETPETINFO,
    SETDATETIME,
    SETSPORTPARAMSORT,
    SETBATTERYREMINDERSWITCH,
    SETAPPSLEEPMODE,
    GETSPORTSTYPEV3,
    SETSPORT100SORT,
    GETWATCHLISTV2,
    SETSCHEDULEREMINDER,
    GETPETINFO,
    GETFUNCTIONTABLE,
    SETDEFAULTMSGLIST,
    GETSTRESSSWITCH,
    GETAPPLETCONTROL,
    GETDOWNLOADLANGUAGE,
    SETMENULISTV3,
    GETSPO2SWITCH,
    GETBATTERYREMINDERSWITCH,
    GETAPPSLEEPMODE,
    SETBIKELOCKLIST,
    SETCGMKEYANDDEVICE,
    DELETECGMKEYANDDEVICE,
    CONNECTCGM,
    DISCONNECTCGM,
    SETBLOODGLUCOSEDATAV01,
    SETALGORITHMRAWDATASENSORCONFIG,
}