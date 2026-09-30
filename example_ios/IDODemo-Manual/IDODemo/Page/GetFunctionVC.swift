//
//  GetFunctionVC.swift
//  IDODemo
//
//  Created by hc on 2023/10/26.
//

import UIKit

import RxCocoa
import RxSwift
import SnapKit
import SVProgressHUD

import protocol_channel

class GetFunctionVC: UIViewController {
    private lazy var tableView: UITableView = {
        let v = UITableView()
        v.dataSource = self
        v.delegate = self
        return v
    }()
    
    private lazy var items: [GetCmd] = [
        GetCmd(type: .getDeviceInfo, title: "getDeviceInfo", desc: "Get device information", descCn: "Get device information"),
        GetCmd(type: .getFunctionTable, title: "getFunctionTable", desc: "Get function table information", descCn: "Get function table"),
        GetCmd(type: .getUnreadAppReminder, title: "getUnreadAppReminder", desc: "Get unread app reminder switch event number", descCn: "Get unread red dot reminder switch"),
        GetCmd(type: .getWatchDialInfo, title: "getWatchDialInfo", desc: "Get Screen Information", descCn: "Get screen information"),
    ]
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Get Function"
        view.backgroundColor = .white
        
        // !!!: Some APIs must be called only after checking the function table (the SDK is compatible with all devices; do not call APIs the function table does not support)
        
        // For internal full-coverage testing only (customers must keep this value false)
        let innerTest = false
        
        if (innerTest || sdk.funcTable.getNewWatchList) {
            items.append(GetCmd(type: .getWatchListV3, title: "getWatchListV3", desc: "Getting watch face list for V3 (New)", descCn: "Get watch face list"))
        }else {
            items.append(GetCmd(type: .getWatchListV2, title: "getWatchListV2", desc: "Get Watch Face List in V2", descCn: "Get watch face list"))
        }
        
        if (innerTest || sdk.funcTable.getBatteryInfo) {
            items.append(GetCmd(type: .getBatteryInfo, title: "getBatteryInfo", desc: "Get battery information event number", descCn: "Get battery information"))
        }
        if (innerTest || sdk.funcTable.getDeviceLogState) {
            items.append(GetCmd(type: .getDeviceLogState, title: "getDeviceLogState", desc: "Get device log state event number", descCn: "Get device log state"))
        }
        if (innerTest || sdk.funcTable.getMenuList) {
            items.append(GetCmd(type: .getMenuList, title: "getMenuList", desc: "Get Supported Menu List", descCn: "Get list supported by the device"))
        }
        if (innerTest || sdk.funcTable.reminderAncs) {
            items.append(GetCmd(type: .getNoticeStatus, title: "getNoticeStatus", desc: "Get notification center status event number"))
        }
        if (innerTest || sdk.funcTable.syncV3SyncAlarm) {
            items.append(GetCmd(type: .getAlarm, title: "getAlarm", desc: "Getting Alarms for V3APP Devices"))
        }
        
        if (innerTest || sdk.funcTable.reminderGetAllContact) {
            items.append(GetCmd(type: .getContactReviseTime, title: "getContactReviseTime", desc: "Get firmware local contact file modification time event number"))
        }
        
        if (innerTest || sdk.funcTable.getWalkReminderV3) {
            items.append(GetCmd(type: .getWalkRemind, title: "getWalkRemind", desc: "Get walk reminder event number"))
        }
        
        if (innerTest || sdk.funcTable.getSetMaxItemsNum) {
            items.append(GetCmd(type: .getSupportMaxSetItemsNum, title: "getSupportMaxSetItemsNum", desc: "Get maximum number of settings supported by firmware event number"))
        }
        
        if (innerTest || sdk.funcTable.getScreenBrightnessMain9) {
            items.append(GetCmd(type: .getScreenBrightness, title: "getScreenBrightness", desc: "Get screen brightness event number"))
        }
        
        if (innerTest || sdk.funcTable.getSupportGetMainSportGoalV3) {
            items.append(GetCmd(type: .getMainSportGoal, title: "getMainSportGoal", desc: "Get Set Calorie/Distance/Mid-High Sport Time Goal event number"))
        }
        
        if (innerTest || sdk.funcTable.getDoNotDisturbMain3) {
            items.append(GetCmd(type: .getNotDisturbStatus, title: "getNotDisturbStatus", desc: "Get Do Not Disturb mode status event number"))
        }
        
        
        if (innerTest || sdk.funcTable.getUpHandGestureEx) {
            items.append(GetCmd(type: .getUpHandGesture, title: "getUpHandGesture", desc: "Get wrist up gesture data event number"))
        }
        
        if (innerTest || sdk.funcTable.getBleAndBtVersion) {
            items.append( GetCmd(type: .getBtNotice, title: "getBtNotice", desc: "Query BT pairing switch, connection, A2DP connection, HFP connection status (Only Supported on devices with BT Bluetooth) event number"))
        }
        
        if (innerTest || sdk.funcTable.getFlashLog) {
            items.append(GetCmd(type: .getFlashBinInfo, title: "getFlashBinInfo", desc: "Get Font Library Information event number"))
        }
        
        if (innerTest || sdk.funcTable.getActivitySwitch) {
            items.append(GetCmd(type: .getActivitySwitch, title: "getActivitySwitch", desc: "Get event number for activity switch"))
        }
        
        if (innerTest || sdk.funcTable.getBtAddrV2) {
            items.append(GetCmd(type: .getBtName, title: "getBtName", desc: "Get bt bluetooth name"))
        }
        if (innerTest || sdk.funcTable.getMtu) {
            items.append(GetCmd(type: .getMtuInfo, title: "getMtuInfo", desc: "Get MTU Information event number"))
        }
        if (innerTest || sdk.funcTable.getHealthSwitchStateSupportV3) {
            items.append(GetCmd(type: .getAllHealthSwitchState, title: "getAllHealthSwitchState", desc: "Get event number for all health monitoring switches"))
        }
        
//        if (innerTest || sdk.funcTable.getRealtimeData) {
//            items.append(GetCmd(type: .getLiveData, title: "getLiveData", desc: "Get Real-time Data event number")) // Deprecated
//        }
        if (innerTest || sdk.funcTable.getFlashLog) {
            items.append(GetCmd(type: .getErrorRecord, title: "getErrorRecord", desc: "Get error record"))
        }
        if (innerTest || sdk.funcTable.getSupportUpdateGps) {
            items.append(GetCmd(type: .getGpsInfo, title: "getGpsInfo", desc: "Get GPS Information event number"))
        }
//        if (innerTest || sdk.funcTable.getVersionInfo) {
//            items.append(GetCmd(type: .getVersionInfo, title: "getVersionInfo", desc: "Get version information event number"))
//        }
        if (innerTest || sdk.funcTable.getSupportUpdateGps) {
            items.append(GetCmd(type: .getGpsStatus, title: "getGpsStatus", desc: "Get GPS Status event number"))
        }
        if (innerTest || sdk.funcTable.getDeletableMenuListV2) {
            items.append(GetCmd(type: .getUnerasableMeunList, title: "getUnerasableMeunList", desc: "Get non-deletable menu list in firmware event number"))
        }
        if (innerTest || sdk.funcTable.setSupportV3Bp) {
            items.append(GetCmd(type: .getBpAlgVersion, title: "getBpAlgVersion", desc: "Get blood pressure algorithm version information event number"))
        }
        if (innerTest || sdk.funcTable.getDeviceUpdateState) {
            items.append(GetCmd(type: .getUpdateStatus, title: "getUpdateStatus", desc: "Get device update status event number"))
        }
        if (innerTest || sdk.funcTable.getDownloadLanguage) {
            items.append(GetCmd(type: .getDownloadLanguage, title: "getDownloadLanguage", desc: "Get Download Language Support"))
        }
        if (innerTest || (sdk.funcTable.getSupportV3BleMusic && sdk.funcTable.getSupportGetBleMusicInfoVerV3)) {
            items.append(GetCmd(type: .getBleMusicInfo, title: "getBleMusicInfo", desc: "Get Firmware Song Names and Folders"))
        }
        if (innerTest || sdk.funcTable.getLangLibraryV3) {
            items.append(GetCmd(type: .getLanguageLibrary, title: "getLanguageLibrary", desc: "Get Language Library List"))
        }
        if (innerTest || sdk.funcTable.getSupportGetBleBeepV3) {
            items.append(GetCmd(type: .getBleBeep, title: "getBleBeep", desc: "Getting firmware local beep file information for V3"))
        }
        if (innerTest || sdk.funcTable.getStepDataTypeV2) {
            items.append(GetCmd(type: .getStepGoal, title: "getStepGoal", desc: "Get daily step goal event number"))
        }
        if (innerTest || sdk.funcTable.getSupportGetV3DeviceBtConnectPhoneModel) {
            items.append(GetCmd(type: .getBtConnectPhoneModel, title: "getBtConnectPhoneModel", desc: "Get BT-connected phone model"))
        }
        
        if (innerTest || sdk.funcTable.setScheduleReminder) {
            items.append(GetCmd(type: .setScheduleReminder, title: "getScheduleReminder", desc: "Get schedule reminder", descCn: "Get schedule reminder"))
        }
        
        if (innerTest || sdk.funcTable.getSupportGetUnit) {
            items.append(GetCmd(type: .getUnit, title: "getUnit", desc: "Get Unit event number", descCn: "Get unit"))
        }
        
        if (innerTest || sdk.funcTable.setSupportControlMiniProgram) {
            items.append(GetCmd(type: .getAppletControl, title: "getAppletControl", desc: "Operation of applet information (obtain, start, delete)", descCn: "Operate applet information (get, start, delete)"))
        }
        
        if (innerTest || sdk.funcTable.getSupportDeviceOperateAlgFile) {
            items.append(GetCmd(type: .getAlgFile, title: "getAlgFileInfo", desc: "Get firmware algorithm file information (ACC/GPS)", descCn: "Get firmware algorithm file information (ACC/GPS)"))
            items.append(GetCmd(type: .requestAlgFile, title: "requestAlgFile", desc: "Request firmware algorithm file information (ACC/GPS)", descCn: "Request firmware algorithm file information (ACC/GPS)"))
        }
        
        // NOTE:
        // Because the demo must support all devices, this example handles multiple cases.
        // When supporting only one device, handle just one of the following cases (use the function table to determine which one the device uses).
        if (sdk.funcTable.setSmartHeartRate) {
            if (sdk.funcTable.getSupportGetSmartHeartRate) {
                //https://idoosmart.github.io/Native_GitBook/en/doc/set/IDOSetHeartRateModeSmart.html?h=setHeartRateModeSmart
                print("getSupportGetSmartHeartRate")
                items.append(GetCmd(type: .getHeartMode, title: "getHeartMode", desc: "Get heart rate mode", descCn: "Get heart rate mode"))
            }else {
                // !!!: - This device does not support getting the heart rate mode; the app must record it itself
                print("This device does not support getting the heart rate mode; the app must record it itself")
            }
        }else if(sdk.funcTable.syncV3Hr) {
            // https://idoosmart.github.io/Native_GitBook/en/doc/set/IDOSetHeartMode.html
            print("syncV3Hr")
            items.append(GetCmd(type: .getHeartMode, title: "getHeartMode", desc: "Get heart rate mode", descCn: "Get heart rate mode"))
        }else if(sdk.funcTable.getHeartRateModeV2) {
            // https://idoosmart.github.io/Native_GitBook/en/doc/get/IDOGetHeartRateMode.html
            print("getHeartRateModeV2")
            items.append(GetCmd(type: .getHeartMode, title: "getHeartMode", desc: "Get heart rate mode", descCn: "Get heart rate mode"))
        }
        
        
        //items.append(GetCmd(type: .getHidInfo, title: "getHidInfo", desc: "Get HID Information event number")) // Not enabled
        //items.append(GetCmd(type: .getWatchDialId, title: "getWatchDialId", desc: "Get watch ID event number")) // Not enabled
        //items.append(GetCmd(type: .getHotStartParam, title: "getHotStartParam", desc: "Get Hot Start Parameters event number")) // Not enabled
        //items.append(GetCmd(type: .getDeviceName, title: "getDeviceName", desc: "Get device name event number")) // Deprecated
        //items.append(GetCmd(type: .getStressVal, title: "getStressVal", desc: "Get stress value event number"))
        //items.append(GetCmd(type: .getHabitInfo, title: "getHabitInfo", desc: "Get User Habit Information in V3")) // Not enabled
        
        items = items.sorted { $0.title < $1.title }
        
        view.addSubview(tableView)
        tableView.snp.makeConstraints { make in
            if #available(iOS 11.0, *) {
                make.top.equalTo(view.safeAreaLayoutGuide.snp.top).offset(15)
                make.bottom.equalTo(view.safeAreaLayoutGuide.snp.bottom)
            } else {
                make.top.equalTo(0)
                make.bottom.equalTo(0)
            }
            make.left.right.equalTo(0)
        }
    }
}


// MARK: - UITableViewDelegate, UITableViewDataSource

extension GetFunctionVC: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items.count
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 60
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let _cellID = "UITableViewCell"
        let cell = tableView.dequeueReusableCell(withIdentifier: _cellID) ?? UITableViewCell(style: .subtitle, reuseIdentifier: _cellID)
        cell.accessoryType = .disclosureIndicator
        cell.selectionStyle = .default
        cell.textLabel?.textColor = .black
        cell.textLabel?.textAlignment = .left
        cell.detailTextLabel?.textAlignment = .left
        cell.detailTextLabel?.textColor = .gray
        cell.detailTextLabel?.numberOfLines = 2
        let cmd = items[indexPath.row]
        cell.textLabel?.text = cmd.title
        cell.detailTextLabel?.text = cmd.desc
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let cmd = items[indexPath.row]
        let vc = GetFunctionDetailVC(cmd: cmd)
        navigationController?.pushViewController(vc, animated: true)
    }
}


// MARK: - CmdType

struct OtherParamModel: IDOBaseModel {
    
    let dic: Dictionary<String, Any>?
    
    private enum CodingKeys: String, CodingKey {
            case indexPath
            case locationInText
        }
    
    init(dic: Dictionary<String, Any>) {
        self.dic = dic
    }
    
    init(from decoder: Decoder) throws {
        dic = nil
    }
    
    func encode(to encoder: Encoder) throws {
        
    }
    
    func toJsonString() -> String? {
        guard let aDic = dic else { return nil }
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: aDic, options: [])
            if let jsonString = String(data: jsonData, encoding: .utf8) {
                return jsonString
            }
        } catch {
            print("Error converting dictionary to JSON string: \(error.localizedDescription)")
        }
        return nil
    }
}

fileprivate struct GetCmd {
    let type: CmdType
    let title: String
    let desc: String
    let descCn: String
    
    init(type: CmdType, title: String, desc: String, descCn: String = "") {
        self.title = title
        self.desc = desc
        self.type = type
        self.descCn = descCn
    }
}

fileprivate enum CmdType: CaseIterable { // Allows getting the number of enum cases
    /// Get Device Information
    case getDeviceInfo
    /// Get Function Table
    case getFunctionTable
    /// Get bt bluetooth name
     case getBtName
    /// Get Real-time Data event number
     case getLiveData
    /// Get error record
     case getErrorRecord
    /// Get HID Information event number
     case getHidInfo
    /// Get GPS Information event number
     case getGpsInfo
    /// Get version information event number
     case getVersionInfo
    /// Get MTU Information event number
     case getMtuInfo
    /// Get event number for all health monitoring switches
     case getAllHealthSwitchState
    /// Get GPS Status event number
     case getGpsStatus
    /// Get non-deletable menu list in firmware event number
     case getUnerasableMeunList
    /// Get event number for activity switch
     case getActivitySwitch
    /// Get unread app reminder switch event number
     case getUnreadAppReminder
    /// Get Font Library Information event number
     case getFlashBinInfo
    /// Query BT pairing switch, connection, A2DP connection, HFP connection status (Only Supported on devices with BT Bluetooth) event number
     case getBtNotice
    /// Get wrist up gesture data event number
     case getUpHandGesture
    /// Get watch ID event number
     case getWatchDialId
    /// Get Do Not Disturb mode status event number
     case getNotDisturbStatus
    /// Get Set Calorie/Distance/Mid-High Sport Time Goal event number
     case getMainSportGoal
    /// Get blood pressure algorithm version information event number
     case getBpAlgVersion
    /// Get screen brightness event number
     case getScreenBrightness
    /// Get Hot Start Parameters event number
     case getHotStartParam
    /// Get maximum number of settings supported by firmware event number
     case getSupportMaxSetItemsNum
    /// Get walk reminder event number
     case getWalkRemind
    /// Get daily step goal event number
     case getStepGoal
    /// Get device name event number
     case getDeviceName
    /// Get firmware local contact file modification time event number
     case getContactReviseTime
    /// Get device update status event number
     case getUpdateStatus
    /// Get stress value event number
     case getStressVal
    /// Get battery information event number
     case getBatteryInfo
    /// Get device log state event number
     case getDeviceLogState
    /// Get Download Language Support
     case getDownloadLanguage
    /// Get Supported Menu List
     case getMenuList
    /// Get notification center status event number
     case getNoticeStatus
    /// Getting Alarms for V3APP Devices
     case getAlarm
    /// Get User Habit Information in V3
     case getHabitInfo
    /// Get Firmware Song Names and Folders
     case getBleMusicInfo
    /// Get Screen Information
     case getWatchDialInfo
    /// Getting watch face list for V3 (New)
     case getWatchListV3
    /// Get Watch Face List in V2
     case getWatchListV2
    /// Get Language Library List
     case getLanguageLibrary
    /// Getting firmware local beep file information for V3
     case getBleBeep
    /// Get BT connected mobile phone model
     case getBtConnectPhoneModel
    /// Get schedule reminder
    case setScheduleReminder
    /// Get unit
    case getUnit
    /// Get applet information
    case getAppletControl
    ///  Get firmware algorithm file information (ACC/GPS)
    case getAlgFile
    ///  Request firmware algorithm file information (ACC/GPS)
    case requestAlgFile
    ///  Heart rate mode
    case getHeartMode
    
}

extension CmdType {
    func param() -> IDOBaseModel? {
        switch self {
        case .getMainSportGoal:
            return OtherParamModel(dic: ["timeGoalType": 1])
        case .getLiveData:
            return OtherParamModel(dic: ["flag": 1])
        case .getErrorRecord:
            return OtherParamModel(dic: ["type": 0])
        case .getAlarm:
            return OtherParamModel(dic: ["flag": 0])
        case .setScheduleReminder:
            return IDOSchedulerReminderParamModel(operate: 3, items: [])
        case .getAppletControl:
            return IDOAppletControlModel(operate: 3)
        case .getHeartMode:
            if(sdk.funcTable.syncV3Hr) {
                return IDOHeartModeParamModel(updateTime: 0) // 0 means get
            }
            return nil
        default:
            return nil
        }
    }
    
}

// MARK: - GetFunctionDetailVC

private class GetFunctionDetailVC: UIViewController {
    private var cmd: GetCmd
    private let disposeBag = DisposeBag()
    private var cancellable: IDOCancellable?
    private lazy var lblParam: UILabel = {
        let v = UILabel()
        v.font = .systemFont(ofSize: 14)
        v.textColor = .gray
        v.textAlignment = .left
        v.text = "Parameter"
        return v
    }()
    
    private lazy var lblResponse: UILabel = {
        let v = UILabel()
        v.font = .systemFont(ofSize: 14)
        v.textColor = .gray
        v.textAlignment = .left
        v.text = "Response"
        return v
    }()
    
    private lazy var textParam: UITextView = {
        let v = UITextView()
        v.font = .systemFont(ofSize: 13)
        v.textColor = .darkGray
        v.textAlignment = .left
        v.isEditable = false
        v.backgroundColor = UIColor(red: 233/255.0, green: 233/255.0, blue: 233/255.0, alpha: 1)
        return v
    }()
    
    private lazy var textResponse: UITextView = {
        let v = UITextView()
        v.font = .systemFont(ofSize: 13)
        v.textColor = .darkGray
        v.textAlignment = .left
        v.isEditable = false
        v.backgroundColor = UIColor(red: 233/255.0, green: 233/255.0, blue: 233/255.0, alpha: 1)
        return v
    }()
    
    private lazy var btnCall: UIButton = {
        let btn = UIButton.createNormalButton(title: "Send")
        btn.isEnabled = true
        btn.rx.tap.subscribe(onNext: { [weak self] in
            self?.doCall()
        }).disposed(by: disposeBag)
        return btn
    }()
    
    required init(cmd: GetCmd) {
        self.cmd = cmd
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // Register listener for device-to-app file transfer tasks
        sdk.transfer.registerDeviceTranFileToApp { [weak self] task in
            self?.deviceTransFileToApp(task)
        }
        
        title = cmd.title
        view.backgroundColor = .white
        
        view.addSubview(btnCall)
        view.addSubview(lblParam)
        view.addSubview(textParam)
        view.addSubview(lblResponse)
        view.addSubview(textResponse)
        
        btnCall.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(200)
            if #available(iOS 11.0, *) {
                make.top.equalTo(view.safeAreaLayoutGuide.snp.top).offset(35)
            } else {
                make.top.equalTo(35)
            }
        }
        
        lblParam.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(btnCall.snp.bottom).offset(15)
        }
        textParam.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.right.equalTo(-15)
            make.top.equalTo(lblParam.snp.bottom).offset(5)
            make.height.equalTo(160)
        }
        lblResponse.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(textParam.snp.bottom).offset(35)
        }
        textResponse.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.right.equalTo(-15)
            make.top.equalTo(lblResponse.snp.bottom).offset(5)
            if #available(iOS 11.0, *) {
                make.bottom.equalTo(view.safeAreaLayoutGuide.snp.bottom)
            } else {
                make.bottom.equalTo(0)
            }
        }
        
        textParam.text = cmd.type.param()?.toJsonString() ?? "none"
    }
    
    override func viewDidDisappear(_ animated: Bool) {
        cancellable?.cancel()
        super.viewDidDisappear(animated)
    }
    
    deinit {
        sdk.transfer.unregisterDeviceTranFileToApp()
    }
    
    private func doCall() {
        cancellable?.cancel()
        cancellable = nil
        textResponse.text = ""
        btnCall.isEnabled = false
        switch cmd.type {
        case .getBtName:
            cancellable = Cmds.getBtName().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getLiveData:
            let flag = (cmd.type.param() as! OtherParamModel).dic!["flag"] as! Int
            cancellable = Cmds.getLiveData(flag: flag).send { [weak self] res in
                self?.doPrint(res)
            }
        case .getErrorRecord:
            let type = (cmd.type.param() as! OtherParamModel).dic!["type"] as! Int
            cancellable = Cmds.getErrorRecord(type: type).send { [weak self] res in
                self?.doPrint(res)
            }
        case .getHidInfo:
            cancellable = Cmds.getHidInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getGpsInfo:
            cancellable = Cmds.getGpsInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getVersionInfo:
            cancellable = Cmds.getVersionInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getMtuInfo:
            cancellable = Cmds.getMtuInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getAllHealthSwitchState:
            cancellable = Cmds.getAllHealthSwitchState().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getGpsStatus:
            cancellable = Cmds.getGpsStatus().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getUnerasableMeunList:
            cancellable = Cmds.getUnerasableMeunList().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getActivitySwitch:
            cancellable = Cmds.getActivitySwitch().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getUnreadAppReminder:
            cancellable = Cmds.getUnreadAppReminder().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getFlashBinInfo:
            cancellable = Cmds.getFlashBinInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getBtNotice:
            cancellable = Cmds.getBtNotice().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getUpHandGesture:
            cancellable = Cmds.getUpHandGesture().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getWatchDialId:
            cancellable = Cmds.getWatchDialId().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getNotDisturbStatus:
            cancellable = Cmds.getNotDisturbStatus().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getMainSportGoal:
            let timeGoalType = (cmd.type.param() as! OtherParamModel).dic!["timeGoalType"] as! Int
            cancellable = Cmds.getMainSportGoal(timeGoalType: timeGoalType).send { [weak self] res in
                self?.doPrint(res)
            }
        case .getBpAlgVersion:
            cancellable = Cmds.getBpAlgVersion().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getScreenBrightness:
            cancellable = Cmds.getScreenBrightness().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getHotStartParam:
            cancellable = Cmds.getHotStartParam().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getSupportMaxSetItemsNum:
            cancellable = Cmds.getSupportMaxSetItemsNum().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getWalkRemind:
            cancellable = Cmds.getWalkRemind().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getStepGoal:
            cancellable = Cmds.getStepGoal().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getDeviceName:
            cancellable = Cmds.getDeviceName().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getContactReviseTime:
            cancellable = Cmds.getContactReviseTime().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getUpdateStatus:
            cancellable = Cmds.getUpdateStatus().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getStressVal:
            cancellable = Cmds.getStressVal().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getBatteryInfo:
            cancellable = Cmds.getBatteryInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getDeviceLogState:
            cancellable = Cmds.getDeviceLogState().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getDownloadLanguage:
            cancellable = Cmds.getDownloadLanguage().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getMenuList:
            cancellable = Cmds.getMenuList().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getNoticeStatus:
            cancellable = Cmds.getNoticeStatus().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getAlarm:
            let flag = (cmd.type.param() as! OtherParamModel).dic!["flag"] as! Int
            cancellable = Cmds.getAlarm(flag: flag, priority: .high).send { [weak self] res in
                self?.btnCall.isEnabled = true
                if case .success(let obj) = res {
                    self?.textResponse.text = "\(obj?.toJsonString() ?? "")\n\n\n" + "\(printProperties(obj) ?? "")"
                }else if case .failure(let err) = res {
                    self?.textResponse.text = "Error code: \(err.code)\nMessage: \(err.message ?? "")"
                }
                //self?.doPrint(res)
            }
        case .getHabitInfo:
            cancellable = Cmds.getHabitInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getBleMusicInfo:
            cancellable = Cmds.getBleMusicInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getWatchDialInfo:
            cancellable = Cmds.getWatchDialInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getWatchListV3:
            cancellable = Cmds.getWatchListV3().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getWatchListV2:
            cancellable = Cmds.getWatchListV2().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getLanguageLibrary:
            cancellable = Cmds.getLanguageLibrary().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getBleBeep:
            cancellable = Cmds.getBleBeep().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getDeviceInfo:
            btnCall.isEnabled = true
            textResponse.text = sdk.device.printProperties()
            break
        case .getFunctionTable:
            btnCall.isEnabled = true
            textResponse.text = sdk.funcTable.printProperties()
            break
        case .getBtConnectPhoneModel:
            cancellable = Cmds.getBtConnectPhoneModel().send { [weak self] res in
                self?.doPrint(res)
            }
        case .setScheduleReminder:
            let param = cmd.type.param() as! IDOSchedulerReminderParamModel
            cancellable = Cmds.setSchedulerReminder(param).send { [weak self] res in
                self?.doPrint(res)
            }
        case .getUnit:
            cancellable = Cmds.getUnit().send { [weak self] res in
                self?.doPrint(res)
            }
        case .getAppletControl:
            let param = cmd.type.param() as! IDOAppletControlModel
            cancellable = Cmds.setAppleControl(param).send { [weak self] res in
                self?.doPrint(res)
            }
        case .getAlgFile:
            cancellable = Cmds.getAlgFileInfo().send { [weak self] res in
                self?.doPrint(res)
            }
        case .requestAlgFile:
            cancellable = Cmds.getAlgFileInfo().send { [weak self] res in
                self?.doPrint(res)
                if case .success(let val) = res {
                    guard let item = val?.items?.filter({ $0.totalQuantity > 0 }).first else {
                        SVProgressHUD.show(withStatus: "There is no file to request download")
                        return
                    }
                    self?.cancellable = Cmds.rquestAlgFile(type: item.type).send { [weak self] res in
                        self?.doPrint(res)
                    }
                }else if case .failure(let err) = res {
                    self?.textResponse.text = "Error code: \(err.code)\nMessage: \(err.message ?? "")"
                }
            }
            
        case .getHeartMode:
            guard let obj = cmd.type.param() else {
                print("Parameters need to be configured")
                return
            }
            if (sdk.funcTable.setSmartHeartRate) {
                if (sdk.funcTable.getSupportGetSmartHeartRate) {
                    let param = IDOHeartModeParamModel(updateTime: 0)
                    cancellable = Cmds.setHeartMode(param).send { [weak self] res in
                        self?.doPrint(res)
                    }
                }
            }else if(sdk.funcTable.syncV3Hr) {
                let param = cmd.type.param() as! IDOHeartModeParamModel
                cancellable = Cmds.setHeartMode(param).send { [weak self] res in
                    self?.doPrint(res)
                }
            }else if(sdk.funcTable.getHeartRateModeV2) {
                cancellable = Cmds.getHeartRateMode().send { [weak self] res in
                    self?.doPrint(res)
                }
            }
        }
        
    }
    
    private func doPrint<T>(_ res: Result<T?, CmdError>) {
        btnCall.isEnabled = true
        if case .success(let val) = res {
            if T.self is String.Type {
                textResponse.text = val as? String
            }else if T.self is IDOBaseModel.Type {
                let obj = val as? IDOBaseModel
                textResponse.text = "\(obj?.toJsonString() ?? "")\n\n\n" + "\(printProperties(obj) ?? "")"
            }
        }else if case .failure(let err) = res {
            textResponse.text = "Error code: \(err.code)\nMessage: \(err.message ?? "")"
        }
    }
    
    private func deviceTransFileToApp(_ task: IDODeviceFileToAppTask) {
        let txt = "\n\nReceived file transferred from device to App - name: \(task.deviceTransItem.fileName) size: \(task.deviceTransItem.fileSize)"
        print(txt)
        textResponse.text += "\n\(txt)"
        // Receive file transferred from device to app
        task.acceptReceiveFile { [weak self] progress in
            let s = "Receive progress: \(progress * 100)%"
            print(s)
            self?.textResponse.text += "\n\(s)"
        } onComplete: { [weak self] isCompleted, receiveFilePath in
            let s = "Receive result: \(isCompleted) filePath:\(receiveFilePath ?? "")"
            print(s)
            self?.textResponse.text += "\n\(s)"
        }
        
        /*
        // Note: receiving can be stopped at any time
        // Stop receiving the file
        task.stopReceiveFile { rs in
            print("Stopped receiving file \(task.deviceTransItem.fileName)")
        }
         */
    }
}

