//
//  SdkFeatureTestVC.swift
//  IDODemo
//
//  Temporary standalone integration test page, aligned with protocol_lib/example SdkFeatureTestPage
//

import UIKit
import SnapKit
import SVProgressHUD
import protocol_channel

/// Integration test of features already implemented in the standardized SDK (protocol_c_doc / SDK_FEATURE_PROGRESS)
class SdkFeatureTestVC: UIViewController {

    private var logBuffer = ""
    private var supportSyncTypesText = ""

    private lazy var buttonScrollView: UIScrollView = {
        let v = UIScrollView()
        v.showsVerticalScrollIndicator = false
        v.alwaysBounceVertical = true
        return v
    }()

    private lazy var buttonStack: UIStackView = {
        let v = UIStackView()
        v.axis = .vertical
        v.spacing = 10
        v.alignment = .fill
        return v
    }()

    private lazy var textConsole: UITextView = {
        let v = UITextView()
        v.font = .monospacedSystemFont(ofSize: 12, weight: .regular)
        v.textColor = .darkGray
        v.textAlignment = .left
        v.isEditable = false
        v.backgroundColor = UIColor(red: 233 / 255.0, green: 233 / 255.0, blue: 233 / 255.0, alpha: 1)
        return v
    }()

    override func viewDidLoad() {
        super.viewDidLoad()
        title = "SDK Feature Test"
        view.backgroundColor = .white
        setupNavigationItems()
        setupLayout()
        setupActions()
        sdk.dataExchange.addExchange(delegate: self)
        refreshFuncTableSummary()
    }

    private func setupNavigationItems() {
        navigationItem.rightBarButtonItems = [
            UIBarButtonItem(
                title: "Refresh",
                style: .plain,
                target: self,
                action: #selector(onRefreshFuncTable)
            ),
            UIBarButtonItem(
                title: "Clear",
                style: .plain,
                target: self,
                action: #selector(onClearLog)
            ),
        ]
    }

    private func setupLayout() {
        view.addSubview(buttonScrollView)
        view.addSubview(textConsole)
        buttonScrollView.addSubview(buttonStack)

        buttonScrollView.snp.makeConstraints { make in
            if #available(iOS 11.0, *) {
                make.top.equalTo(view.safeAreaLayoutGuide.snp.top).offset(8)
            } else {
                make.top.equalTo(8)
            }
            make.left.right.equalToSuperview()
            make.height.equalTo(300)
        }

        buttonStack.snp.makeConstraints { make in
            make.edges.equalToSuperview().inset(UIEdgeInsets(top: 0, left: 12, bottom: 0, right: 12))
            make.width.equalTo(buttonScrollView.snp.width).offset(-24)
        }

        textConsole.snp.makeConstraints { make in
            make.left.equalTo(12)
            make.right.equalTo(-12)
            make.top.equalTo(buttonScrollView.snp.bottom).offset(8)
            if #available(iOS 11.0, *) {
                make.bottom.equalTo(view.safeAreaLayoutGuide.snp.bottom).offset(-8)
            } else {
                make.bottom.equalTo(-8)
            }
        }
    }

    private func setupActions() {
        appendSection(
            title: "Basic get Cmd",
            buttons: [
                ("2.61 Device status", { [weak self] in self?.getDeviceStatus() }),
                ("15.104 Wrist-side sports", { [weak self] in self?.getSportTypesWristSide() }),
                ("15.106 Vibration & ringtone (query)", { [weak self] in self?.getVibrationRingtone() }),
                ("Phone calendar sync switch", { [weak self] in self?.getPhoneCalendarSyncSwitch() }),
                ("Phone calendar pending delete list", { [weak self] in self?.getPhoneCalendarDeleteList() }),
            ]
        )
        appendSection(
            title: "V2 switch GET/SET",
            buttons: [
                ("2.47 SpO2 GET", { [weak self] in self?.getSpo2Switch() }),
                ("2.47 SpO2 SET", { [weak self] in self?.setSpo2SwitchSample() }),
                ("2.47 SpO2 write-back", { [weak self] in self?.roundtripSpo2() }),
                ("2.46 Stress GET", { [weak self] in self?.getStressSwitch() }),
                ("2.46 Stress SET", { [weak self] in self?.setStressSwitchSample() }),
                ("2.46 Stress write-back", { [weak self] in self?.roundtripStress() }),
                ("2.45 Smart heart rate GET", { [weak self] in self?.getSmartHrSwitch() }),
                ("2.45 Smart heart rate SET", { [weak self] in self?.setSmartHrSwitchSample() }),
                ("2.45 Smart heart rate write-back", { [weak self] in self?.roundtripSmartHr() }),
            ]
        )
        appendSection(
            title: "V3 set Cmd",
            buttons: [
                ("15.106 Vibration & ringtone (set)", { [weak self] in self?.setVibrationRingtone() }),
                ("15.106 Vibration & ringtone write-back", { [weak self] in self?.roundtripVibrationRingtone() }),
                ("Phone calendar sync (set)", { [weak self] in self?.setPhoneCalendarSync() }),
                ("Phone calendar incremental delete (example)", { [weak self] in self?.setPhoneCalendarSyncDeleteSample() }),
            ]
        )
        appendSection(
            title: "Data exchange exchangeData",
            buttons: [
                ("15.20 Sport summary", { [weak self] in self?.getActivitySummaryExchange() }),
                ("15.103 Full snapshot", { [weak self] in self?.getFullSnapshotExchange() }),
            ]
        )
        appendSection(
            title: "Health sync syncData",
            buttons: [
                ("15.4.1 SpO2", { [weak self] in self?.syncSpo2() }),
                ("15.4.2 Stress", { [weak self] in self?.syncPressure() }),
                ("15.4.23 Activity amount", { [weak self] in self?.syncActivityAmount() }),
            ]
        )
        appendSection(
            title: "2026-07-06 New GET",
            buttons: [
                ("2.53 Firmware status", { [weak self] in self?.getFirmwareStatusInfo() }),
                ("2.39 Heart rate monitoring mode", { [weak self] in self?.getHeartRateMode() }),
                ("2.63 Sleep mode GET", { [weak self] in self?.getAppSleepMode() }),
                ("2.54 Fall detection GET", { [weak self] in self?.getFallMonitoringSwitch() }),
                ("2.56 Positioning switch GET", { [weak self] in self?.getPositionSwitchMode() }),
                ("2.28 File transfer config", { [weak self] in self?.getDataTranConfig() }),
                ("15.26 Watch face list V3", { [weak self] in self?.getWatchListV3() }),
            ]
        )
        appendSection(
            title: "2026-07-06 New SET/query",
            buttons: [
                ("2.54 Fall detection SET", { [weak self] in self?.setFallMonitoringSwitchSample() }),
                ("2.56 Positioning switch SET", { [weak self] in self?.setPositionSwitchModeSample() }),
                ("5.6 Location notification", { [weak self] in self?.setLocationInfoNotify() }),
                ("2.39 Heart rate monitoring SET(example)", { [weak self] in self?.setHeartRateModeSample() }),
                ("2.63 Sleep mode SET(example)", { [weak self] in self?.setAppSleepModeSample() }),
                ("15.9 V3 heart rate mode (example)", { [weak self] in self?.setHeartModeSample() }),
                ("15.54 Applet list", { [weak self] in self?.getAppletList() }),
                ("15.54 Applet delete first item", { [weak self] in self?.deleteAppletFirst() }),
                ("15.79 APP basic info", { [weak self] in self?.setAppBaseInfoSample() }),
                ("15.90 Medication reminder set", { [weak self] in self?.takeMedicineRemindSet() }),
                ("15.90 Medication reminder query", { [weak self] in self?.takeMedicineRemindQuery() }),
                ("15.90 Medication reminder delete first item", { [weak self] in self?.takeMedicineRemindDeleteFirst() }),
                ("15.90 Medication reminder icon", { [weak self] in self?.takeMedicineRemindIconTransfer() }),
                ("15.90 Medication reminder master switch", { [weak self] in self?.takeMedicineRemindSetSwitch() }),
                ("15.91 Purchased watch faces", { [weak self] in self?.setPurchasedWatchFaceInfoSample() }),
                ("15.92 Download status", { [weak self] in self?.setAppDownloadStatusInfoSample() }),
                ("15.93 Firmware positioning query", { [weak self] in self?.getFirmwarePositionInfoQuery() }),
                ("15.93 Firmware positioning confirm", { [weak self] in self?.getFirmwarePositionInfoConfirm() }),
                ("15.73 App list style query", { [weak self] in self?.appListStyleQuery() }),
                ("15.73 App list style delete", { [weak self] in self?.appListStyleDeleteFirst() }),
            ]
        )
    }

    private func appendSection(title: String, buttons: [(String, () -> Void)]) {
        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = .systemFont(ofSize: 13, weight: .semibold)
        titleLabel.textColor = .gray
        buttonStack.addArrangedSubview(titleLabel)

        let row = UIStackView()
        row.axis = .vertical
        row.spacing = 8
        row.alignment = .fill
        buttons.forEach { title, action in
            let btn = UIButton.createNormalButton(title: title)
            btn.snp.makeConstraints { make in
                make.height.equalTo(40)
            }
            btn.addAction(UIAction { _ in action() }, for: .touchUpInside)
            row.addArrangedSubview(btn)
        }
        buttonStack.addArrangedSubview(row)
    }

    @objc private func onRefreshFuncTable() {
        clearLog()
        refreshFuncTableSummary()
    }

    @objc private func onClearLog() {
        clearLog()
        renderLog()
    }

    private func clearLog() {
        logBuffer = ""
    }

    private func log(_ msg: String) {
        logBuffer += msg + "\n"
        print("[SdkFeatureTest] \(msg)")
        renderLog()
    }

    private func renderLog() {
        textConsole.text = logBuffer.isEmpty ? "(No logs)" : logBuffer
        textConsole.scrollToBottom()
    }

    private func refreshFuncTableSummary() {
        let ft = sdk.funcTable
        sdk.syncData.getSupportSyncDataTypeList { [weak self] list in
            guard let self else { return }
            self.supportSyncTypesText = list.map { $0.syncDataType.description }.joined(separator: ", ")
            let summary = """
            [Connection]\(sdk.state.isConnected ? "Connected" : "Disconnected")
            [V2 switch]spo2Get=\(ft.getSupportGetSpo2SwitchInfo) pressureGet=\(ft.getSupportGetPressureSwitchInfo) smartHrGet=\(ft.getSupportGetSmartHeartRate)
            [Health sync]spo2=\(ft.syncV3Spo2) pressure=\(ft.syncV3Pressure) activityAmount=\(ft.syncV3ActivityAmount)
              SpO2 version compatible=\(ft.getSupportSyncSpo2UseVersionCompatible)
              Stress version compatible=\(ft.getSupportSyncPressureUseVersionCompatible)
            [Data exchange]syncV3ActivityExchangeData=\(ft.syncV3ActivityExchangeData)
              supportV3ActivityExchange=\(sdk.dataExchange.supportV3ActivityExchange)
            Supported sync types: \(self.supportSyncTypesText)
            """
            self.log(summary)
        }
    }

    private func ensureConnected() -> Bool {
        guard sdk.state.isConnected else {
            SVProgressHUD.showError(withStatus: "Device not connected")
            log("Error: device not connected")
            return false
        }
        return true
    }

    private func logCmdResult<T>(_ title: String, _ res: Result<T?, CmdError>) {
        switch res {
        case .success(let val):
            log("  code=0")
            if let model = val as? IDOBaseModel {
                let json = model.toJsonString() ?? "NULL"
                log("  json: \(Self.preview(json))")
            } else if let text = val as? String {
                log("  json: \(Self.preview(text))")
            } else if let val {
                log("  json: \(Self.preview("\(val)"))")
            } else {
                log("  json: NULL")
            }
            SVProgressHUD.showSuccess(withStatus: "Succeeded")
        case .failure(let err):
            log("  code=\(err.code) msg=\(err.message ?? "")")
            SVProgressHUD.showError(withStatus: "Failed code=\(err.code)")
        }
    }

    private static func preview(_ text: String, limit: Int = 9800) -> String {
        guard text.count > limit else { return text }
        let end = text.index(text.startIndex, offsetBy: limit)
        return String(text[..<end]) + "..."
    }

    private func getDeviceStatus() {
        guard ensureConnected() else { return }
        log("--- 2.61 Device status (getDeviceStatusInfo) ---")
        log("  Request: {\"get_type\":1}")
        SVProgressHUD.show(withStatus: "2.61 Device status")
        Cmds.getDeviceStatusInfo(getType: 1).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.61 Device status", res)
        }
    }

    private func getSportTypesWristSide() {
        guard ensureConnected() else { return }
        log("--- 15.104 Wrist-side sport types (getSportTypesRequiringWristSideSetting) ---")
        log("  Request: {}")
        SVProgressHUD.show(withStatus: "15.104 Wrist-side sports")
        Cmds.getSportTypesRequiringWristSideSetting().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.104 Wrist-side sports", res)
        }
    }

    private static func switchValue(_ v: Int) -> Int { v == 1 ? 1 : 0 }

    private func spo2ModelToParam(_ model: IDOSpo2SwitchModel) -> IDOSpo2SwitchParamModel {
        IDOSpo2SwitchParamModel(
            onOff: Self.switchValue(model.onOff),
            startHour: model.startHour,
            startMinute: model.startMinute,
            endHour: model.endHour,
            endMinute: model.endMinute,
            lowSpo2OnOff: Self.switchValue(model.lowSpo2OnOff),
            lowSpo2Value: model.lowSpo2Value,
            notifyFlag: model.notifyFlag,
            measurementInterval: model.measurementInterval
        )
    }

    private func stressModelToParam(_ model: IDOStressSwitchModel) -> IDOStressSwitchParamModel {
        IDOStressSwitchParamModel(
            onOff: Self.switchValue(model.onOff),
            startHour: model.startHour,
            startMinute: model.startMinute,
            endHour: model.endHour,
            endMinute: model.endMinute,
            remindOnOff: Self.switchValue(model.remindOnOff),
            interval: model.interval,
            highThreshold: model.highThreshold,
            stressThreshold: model.stressThreshold,
            notifyFlag: model.notifyFlag,
            repeats: model.repeats,
            measurementInterval: model.measurementInterval
        )
    }

    private func smartHrModelToParam(_ model: IDOHeartRateModeSmartModel) -> IDOHeartRateModeSmartParamModel {
        IDOHeartRateModeSmartParamModel(
            mode: Self.switchValue(model.mode),
            notifyFlag: model.notifyFlag,
            highHeartMode: Self.switchValue(model.highHeartMode),
            lowHeartMode: Self.switchValue(model.lowHeartMode),
            highHeartValue: model.highHeartValue,
            lowHeartValue: model.lowHeartValue,
            startHour: model.startHour,
            startMinute: model.startMinute,
            endHour: model.endHour,
            endMinute: model.endMinute,
            measurementInterval: model.measurementInterval
        )
    }

    private func getVibrationRingtone() {
        guard ensureConnected() else { return }
        log("--- 15.106 Vibration & ringtone (query) (deviceVibrationRingtone) ---")
        log("  Request: {\"operate\":1}")
        SVProgressHUD.show(withStatus: "15.106 Vibration & ringtone")
        Cmds.deviceVibrationRingtone().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.106 Vibration & ringtone", res)
        }
    }

    private func getPhoneCalendarSyncSwitch() {
        guard ensureConnected() else { return }
        log("--- Phone calendar sync switch (getPhoneCalendarSyncSwitch) ---")
        SVProgressHUD.show(withStatus: "Phone calendar sync switch")
        Cmds.getPhoneCalendarSyncSwitch().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("Phone calendar sync switch", res)
        }
    }

    private func getPhoneCalendarDeleteList() {
        guard ensureConnected() else { return }
        log("--- Phone calendar pending-delete list (getPhoneCalendarDeleteList) ---")
        SVProgressHUD.show(withStatus: "Phone calendar pending delete list")
        Cmds.getPhoneCalendarDeleteList().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("Phone calendar pending delete list", res)
        }
    }

    private func setPhoneCalendarSync() {
        guard ensureConnected() else { return }
        let model = IDOPhoneCalendarSyncSetModel(syncOnOff: 1, calendarPermissionStatus: 1)
        log("--- Phone calendar sync (set) (setPhoneCalendarSync) ---")
        SVProgressHUD.show(withStatus: "Phone calendar sync (set)")
        Cmds.setPhoneCalendarSync(model).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("Phone calendar sync (set)", res)
        }
    }

    private func setPhoneCalendarSyncDeleteSample() {
        guard ensureConnected() else { return }
        let model = IDOPhoneCalendarSyncDeleteModel(items: [])
        log("--- Phone calendar incremental delete (example) ---")
        SVProgressHUD.show(withStatus: "Phone calendar incremental delete")
        Cmds.setPhoneCalendarSyncDelete(model).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("Phone calendar incremental delete", res)
        }
    }

    private func setVibrationRingtone() {
        guard ensureConnected() else { return }
        let model = IDODeviceVibrationRingtoneModel(
            operate: 2,
            type: 3,
            vibrationIntensity: 2,
            alarmVolume: 5
        )
        log("--- 15.106 Vibration & ringtone (set) ---")
        log("  Request: \(model.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.106 Vibration & ringtone (set)")
        Cmds.deviceVibrationRingtone(model).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.106 Vibration & ringtone (set)", res)
        }
    }

    private func roundtripVibrationRingtone() {
        guard ensureConnected() else { return }
        log("--- 15.106 Vibration & ringtone GET→SET write-back ---")
        SVProgressHUD.show(withStatus: "Vibration & ringtone write-back...")
        Cmds.deviceVibrationRingtone().send { [weak self] getRes in
            guard let self else { return }
            switch getRes {
            case .success(let getModel):
                guard let getModel else {
                    SVProgressHUD.dismiss()
                    self.log("  GET failed, aborting")
                    SVProgressHUD.showError(withStatus: "GET failed")
                    return
                }
                self.log("  GET: \(Self.preview(getModel.toJsonString() ?? "NULL"))")
                let type = getModel.type ?? 0
                if type == 0 {
                    SVProgressHUD.dismiss()
                    self.log("  type=0, skipping SET")
                    SVProgressHUD.showInfo(withStatus: "No settable sub-items")
                    return
                }
                let setModel = IDODeviceVibrationRingtoneModel(operate: 2, type: type)
                if type & 0x01 != 0 { setModel.vibrationIntensity = getModel.vibrationIntensity }
                if type & 0x02 != 0 { setModel.alarmVolume = getModel.alarmVolume }
                if type & 0x04 != 0 { setModel.callReminderVolume = getModel.callReminderVolume }
                self.log("  SET request: \(setModel.toJsonString() ?? "{}")")
                Cmds.deviceVibrationRingtone(setModel).send { setRes in
                    SVProgressHUD.dismiss()
                    self.logCmdResult("15.106 Vibration & ringtone SET", setRes)
                }
            case .failure(let err):
                SVProgressHUD.dismiss()
                self.log("  GET code=\(err.code) msg=\(err.message ?? "")")
                SVProgressHUD.showError(withStatus: "GET failed")
            }
        }
    }

    private func getSpo2Switch() {
        guard ensureConnected() else { return }
        log("--- 2.47 SpO2 switch (query) (getSpo2Switch) ---")
        SVProgressHUD.show(withStatus: "2.47 SpO2 GET")
        Cmds.getSpo2Switch().send { [weak self] res in
            SVProgressHUD.dismiss()
            if case .success(let model) = res, let model {
                self?.log("  measurement_interval=\(model.measurementInterval)")
            }
            self?.logCmdResult("2.47 SpO2 GET", res)
        }
    }

    private func setSpo2SwitchSample() {
        guard ensureConnected() else { return }
        let param = IDOSpo2SwitchParamModel(
            onOff: 1,
            startHour: 0,
            startMinute: 0,
            endHour: 23,
            endMinute: 59,
            lowSpo2OnOff: 1,
            lowSpo2Value: 90,
            notifyFlag: 1,
            measurementInterval: 5
        )
        log("--- 2.47 SpO2 switch (set) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "2.47 SpO2 SET")
        Cmds.setSpo2Switch(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.47 SpO2 SET", res)
        }
    }

    private func roundtripSpo2() {
        guard ensureConnected() else { return }
        log("--- 2.47 SpO2 switch GET→SET write-back ---")
        SVProgressHUD.show(withStatus: "2.47 SpO2 write-back")
        Cmds.getSpo2Switch().send { [weak self] getRes in
            guard let self else { return }
            switch getRes {
            case .success(let getModel):
                guard let getModel else {
                    SVProgressHUD.dismiss()
                    self.log("  GET failed, aborting")
                    SVProgressHUD.showError(withStatus: "GET failed")
                    return
                }
                self.log("  GET: \(Self.preview(getModel.toJsonString() ?? "NULL"))")
                self.log("  measurement_interval=\(getModel.measurementInterval)")
                let param = self.spo2ModelToParam(getModel)
                self.log("  SET request: \(param.toJsonString() ?? "{}")")
                Cmds.setSpo2Switch(param).send { setRes in
                    SVProgressHUD.dismiss()
                    self.logCmdResult("2.47 SpO2 SET", setRes)
                }
            case .failure(let err):
                SVProgressHUD.dismiss()
                self.log("  GET code=\(err.code) msg=\(err.message ?? "")")
                SVProgressHUD.showError(withStatus: "GET failed")
            }
        }
    }

    private func getStressSwitch() {
        guard ensureConnected() else { return }
        log("--- 2.46 Stress switch (query) (getStressSwitch) ---")
        SVProgressHUD.show(withStatus: "2.46 Stress GET")
        Cmds.getStressSwitch().send { [weak self] res in
            SVProgressHUD.dismiss()
            if case .success(let model) = res, let model {
                self?.log("  measurement_interval=\(model.measurementInterval)")
            }
            self?.logCmdResult("2.46 Stress GET", res)
        }
    }

    private func setStressSwitchSample() {
        guard ensureConnected() else { return }
        let param = IDOStressSwitchParamModel(
            onOff: 1,
            startHour: 0,
            startMinute: 0,
            endHour: 23,
            endMinute: 59,
            remindOnOff: 1,
            interval: 60,
            highThreshold: 80,
            stressThreshold: 80,
            notifyFlag: 1,
            repeats: [],
            measurementInterval: 5
        )
        log("--- 2.46 Stress switch (set) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "2.46 Stress SET")
        Cmds.setStressSwitch(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.46 Stress SET", res)
        }
    }

    private func roundtripStress() {
        guard ensureConnected() else { return }
        log("--- 2.46 Stress switch GET→SET write-back ---")
        SVProgressHUD.show(withStatus: "2.46 Stress write-back")
        Cmds.getStressSwitch().send { [weak self] getRes in
            guard let self else { return }
            switch getRes {
            case .success(let getModel):
                guard let getModel else {
                    SVProgressHUD.dismiss()
                    self.log("  GET failed, aborting")
                    SVProgressHUD.showError(withStatus: "GET failed")
                    return
                }
                self.log("  GET: \(Self.preview(getModel.toJsonString() ?? "NULL"))")
                self.log("  measurement_interval=\(getModel.measurementInterval)")
                let param = self.stressModelToParam(getModel)
                self.log("  SET request: \(param.toJsonString() ?? "{}")")
                Cmds.setStressSwitch(param).send { setRes in
                    SVProgressHUD.dismiss()
                    self.logCmdResult("2.46 Stress SET", setRes)
                }
            case .failure(let err):
                SVProgressHUD.dismiss()
                self.log("  GET code=\(err.code) msg=\(err.message ?? "")")
                SVProgressHUD.showError(withStatus: "GET failed")
            }
        }
    }

    private func getSmartHrSwitch() {
        guard ensureConnected() else { return }
        log("--- 2.45 Smart heart rate (query) (getSmartHeartRateMode) ---")
        SVProgressHUD.show(withStatus: "2.45 Smart heart rate GET")
        Cmds.getSmartHeartRateMode().send { [weak self] res in
            SVProgressHUD.dismiss()
            if case .success(let model) = res, let model {
                self?.log("  measurement_interval=\(model.measurementInterval)")
            }
            self?.logCmdResult("2.45 Smart heart rate GET", res)
        }
    }

    private func setSmartHrSwitchSample() {
        guard ensureConnected() else { return }
        let param = IDOHeartRateModeSmartParamModel(
            mode: 1,
            notifyFlag: 1,
            highHeartMode: 1,
            lowHeartMode: 1,
            highHeartValue: 100,
            lowHeartValue: 60,
            startHour: 0,
            startMinute: 0,
            endHour: 23,
            endMinute: 59,
            measurementInterval: 300
        )
        log("--- 2.45 Smart heart rate (set) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "2.45 Smart heart rate SET")
        Cmds.setHeartRateModeSmart(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.45 Smart heart rate SET", res)
        }
    }

    private func roundtripSmartHr() {
        guard ensureConnected() else { return }
        log("--- 2.45 Smart heart rate GET→SET write-back ---")
        SVProgressHUD.show(withStatus: "2.45 Smart heart rate write-back")
        Cmds.getSmartHeartRateMode().send { [weak self] getRes in
            guard let self else { return }
            switch getRes {
            case .success(let getModel):
                guard let getModel else {
                    SVProgressHUD.dismiss()
                    self.log("  GET failed, aborting")
                    SVProgressHUD.showError(withStatus: "GET failed")
                    return
                }
                self.log("  GET: \(Self.preview(getModel.toJsonString() ?? "NULL"))")
                self.log("  measurement_interval=\(getModel.measurementInterval)")
                let param = self.smartHrModelToParam(getModel)
                self.log("  SET request: \(param.toJsonString() ?? "{}")")
                Cmds.setHeartRateModeSmart(param).send { setRes in
                    SVProgressHUD.dismiss()
                    self.logCmdResult("2.45 Smart heart rate SET", setRes)
                }
            case .failure(let err):
                SVProgressHUD.dismiss()
                self.log("  GET code=\(err.code) msg=\(err.message ?? "")")
                SVProgressHUD.showError(withStatus: "GET failed")
            }
        }
    }

    private func getActivitySummaryExchange() {
        guard ensureConnected() else { return }
        guard sdk.dataExchange.supportV3ActivityExchange else {
            log("Skipped: V3 multi-sport data exchange not supported")
            SVProgressHUD.showInfo(withStatus: "Data exchange not supported")
            return
        }
        log("--- 15.20 Sport summary exchangeData.getLastActivityData() ---")
        SVProgressHUD.show(withStatus: "Getting sport summary...")
        sdk.dataExchange.getLastActivityData()
        SVProgressHUD.dismiss()
        log("getLastActivityData triggered (see the exchangeData callback for details)")
        SVProgressHUD.showSuccess(withStatus: "Triggered")
    }

    private func getFullSnapshotExchange() {
        guard ensureConnected() else { return }
        log("--- 15.103 Full snapshot exchangeData.getActivityExchangeFullSnapshot() ---")
        SVProgressHUD.show(withStatus: "Full snapshot...")
        sdk.dataExchange.getActivityExchangeFullSnapshot { [weak self] json in
            SVProgressHUD.dismiss()
            guard let self else { return }
            if let json, !json.isEmpty {
                self.log("json: \(Self.preview(json))")
                SVProgressHUD.showSuccess(withStatus: "Succeeded")
            } else {
                self.log("Empty result or failed")
                SVProgressHUD.showError(withStatus: "Failed")
            }
        }
    }

    private func syncSpo2() {
        guard ensureConnected() else { return }
        guard sdk.funcTable.syncV3Spo2 else {
            log("Skipped: syncV3Spo2=false")
            SVProgressHUD.showInfo(withStatus: "V3 SpO2 not supported")
            return
        }
        log("--- 15.4.1 Sync SpO2 ---")
        SVProgressHUD.show(withStatus: "Syncing SpO2...")
        let types = [IDOSyncDataTypeClass(type: .bloodOxygen)]
        sdk.syncData.startSync(types: types, funcData: { [weak self] type, json, errorCode in
            guard type == .bloodOxygen else { return }
            self?.log("SpO2 errorCode=\(errorCode)")
            if errorCode == 0, !json.isEmpty {
                self?.log("  payload: \(Self.preview(json))")
            }
        }, funcCompleted: { [weak self] errorCode in
            SVProgressHUD.dismiss()
            self?.log("SpO2 done errorCode=\(errorCode)")
            if errorCode == 0 {
                SVProgressHUD.showSuccess(withStatus: "Done")
            } else {
                SVProgressHUD.showError(withStatus: "Failed \(errorCode)")
            }
        })
    }

    private func syncPressure() {
        guard ensureConnected() else { return }
        guard sdk.funcTable.syncV3Pressure else {
            log("Skipped: syncV3Pressure=false")
            SVProgressHUD.showInfo(withStatus: "V3 stress not supported")
            return
        }
        log("--- 15.4.2 Sync stress ---")
        SVProgressHUD.show(withStatus: "Syncing stress...")
        let types = [IDOSyncDataTypeClass(type: .pressure)]
        sdk.syncData.startSync(types: types, funcData: { [weak self] type, json, errorCode in
            guard type == .pressure else { return }
            self?.log("Stress errorCode=\(errorCode)")
            if errorCode == 0, !json.isEmpty {
                self?.log("  payload: \(Self.preview(json))")
            }
        }, funcCompleted: { [weak self] errorCode in
            SVProgressHUD.dismiss()
            self?.log("Stress done errorCode=\(errorCode)")
            if errorCode == 0 {
                SVProgressHUD.showSuccess(withStatus: "Done")
            } else {
                SVProgressHUD.showError(withStatus: "Failed \(errorCode)")
            }
        })
    }

    private func syncActivityAmount() {
        guard ensureConnected() else { return }
        guard sdk.funcTable.syncV3ActivityAmount else {
            log("Skipped: syncV3ActivityAmount=false")
            SVProgressHUD.showInfo(withStatus: "V3 activity amount not supported")
            return
        }
        log("--- 15.4.23 Sync activity amount ---")
        SVProgressHUD.show(withStatus: "Syncing activity amount...")
        let types = [IDOSyncDataTypeClass(type: .activityAmount)]
        sdk.syncData.startSync(types: types, funcData: { [weak self] type, json, errorCode in
            guard type == .activityAmount else { return }
            self?.log("Activity amount errorCode=\(errorCode)")
            if errorCode == 0, !json.isEmpty {
                self?.log("  payload: \(Self.preview(json))")
            }
        }, funcCompleted: { [weak self] errorCode in
            SVProgressHUD.dismiss()
            self?.log("Activity amount done errorCode=\(errorCode)")
            if errorCode == 0 {
                SVProgressHUD.showSuccess(withStatus: "Done")
            } else {
                SVProgressHUD.showError(withStatus: "Failed \(errorCode)")
            }
        })
    }

    // MARK: - 2026-07-06 New feature integration test

    private func getFirmwareStatusInfo() {
        guard ensureConnected() else { return }
        log("--- 2.53 Firmware status (getFirmwareStatusInfo) ---")
        SVProgressHUD.show(withStatus: "2.53 Firmware status")
        Cmds.getFirmwareStatusInfo().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.53 Firmware status", res)
        }
    }

    private func getHeartRateMode() {
        guard ensureConnected() else { return }
        log("--- 2.39 Heart rate monitoring mode (getHeartRateMode) ---")
        SVProgressHUD.show(withStatus: "2.39 Heart rate monitoring")
        Cmds.getHeartRateMode().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.39 Heart rate monitoring", res)
        }
    }

    private func getAppSleepMode() {
        guard ensureConnected() else { return }
        log("--- 2.63 Sleep mode (getAppSleepMode) ---")
        SVProgressHUD.show(withStatus: "2.63 Sleep mode")
        Cmds.getAppSleepMode().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.63 Sleep mode", res)
        }
    }

    private func getFallMonitoringSwitch() {
        guard ensureConnected() else { return }
        log("--- 2.54 Fall detection GET (getFallMonitoringSwitch) ---")
        SVProgressHUD.show(withStatus: "2.54 Fall detection GET")
        Cmds.getFallMonitoringSwitch().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.54 Fall detection GET", res)
        }
    }

    private func getPositionSwitchMode() {
        guard ensureConnected() else { return }
        log("--- 2.56 Positioning switch GET (getPositionSwitchMode) ---")
        SVProgressHUD.show(withStatus: "2.56 Positioning switch GET")
        Cmds.getPositionSwitchMode().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.56 Positioning switch GET", res)
        }
    }

    private func getDataTranConfig() {
        guard ensureConnected() else { return }
        let param = IDODataTranConfigParamModel(type: 0, medicineType: 1)
        log("--- 2.28 File transfer config (getDataTranConfig) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "2.28 File transfer config")
        Cmds.getDataTranConfig(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.28 File transfer config", res)
        }
    }

    private func getWatchListV3() {
        guard ensureConnected() else { return }
        log("--- 15.26 Watch face list V3 (getWatchListV3) ---")
        SVProgressHUD.show(withStatus: "15.26 Watch face list")
        Cmds.getWatchListV3().send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.26 Watch face list", res)
        }
    }

    private func setFallMonitoringSwitchSample() {
        guard ensureConnected() else { return }
        let param = IDOFallMonitoringSwitchModel(fallMonitoringSwitch: 1)
        log("--- 2.54 Fall detection SET ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "2.54 Fall detection SET")
        Cmds.setFallMonitoringSwitch(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.54 Fall detection SET", res)
        }
    }

    private func setPositionSwitchModeSample() {
        guard ensureConnected() else { return }
        let param = IDOPositionSwitchModeModel(switchMode: 1, startHour: 0, startMinute: 0, endHour: 23, endMinute: 59)
        log("--- 2.56 Positioning switch SET ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "2.56 Positioning switch SET")
        Cmds.setPositionSwitchMode(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.56 Positioning switch SET", res)
        }
    }

    private func setLocationInfoNotify() {
        guard ensureConnected() else { return }
        let param = IDOLocationInfoNotifyModel(status: 1)
        log("--- 5.6 Location notification (setLocationInfoNotify) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "5.6 Location notification")
        Cmds.setLocationInfoNotify(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("5.6 Location notification", res)
        }
    }

    private func setHeartRateModeSample() {
        guard ensureConnected() else { return }
        let param = IDOHeartRateModeParamModel(
            mode: 1, hasTimeRange: 1, startHour: 0, startMinute: 0,
            endHour: 23, endMinute: 59, measurementInterval: 5
        )
        log("--- 2.39 Heart rate monitoring SET(example) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "2.39 Heart rate monitoring SET")
        Cmds.setHeartRateMode(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.39 Heart rate monitoring SET", res)
        }
    }

    private func setAppSleepModeSample() {
        guard ensureConnected() else { return }
        let param = IDOAppSleepModeParamModel(sleepModeSwitch: 1)
        log("--- 2.63 Sleep mode SET(example) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "2.63 Sleep mode SET")
        Cmds.setAppSleepMode(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("2.63 Sleep mode SET", res)
        }
    }

    private func setHeartModeSample() {
        guard ensureConnected() else { return }
        let param = IDOHeartModeParamModel(
            updateTime: 0, mode: 1, hasTimeRange: 1,
            startHour: 0, startMinute: 0, endHour: 23, endMinute: 59,
            measurementInterval: 300, notifyFlag: 1
        )
        log("--- 15.9 V3 heart rate mode (example) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.9 V3 heart rate mode")
        Cmds.setHeartMode(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.9 V3 heart rate mode", res)
        }
    }

    private func getAppletList() {
        guard ensureConnected() else { return }
        log("--- 15.54 Applet list (setAppleControl queryTypes) ---")
        SVProgressHUD.show(withStatus: "15.54 Applet list")
        Cmds.setAppleControl(queryTypes: [.downloading, .installing, .installed]).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.54 Applet list", res)
        }
    }

    /// Query the list first, then delete the first applet returned (operate=2)
    private func deleteAppletFirst() {
        guard ensureConnected() else { return }
        log("--- 15.54 Delete first applet (query first, then delete) ---")
        SVProgressHUD.show(withStatus: "15.54 Query applet list")
        Cmds.setAppleControl(queryTypes: [.downloading, .installing, .installed]).send { [weak self] res in
            guard let self = self else { return }
            switch res {
            case .failure:
                SVProgressHUD.dismiss()
                self.logCmdResult("15.54 Applet list (query before delete)", res)
                return
            case .success(let info):
                self.logCmdResult("15.54 Applet list (query before delete)", res)
                guard let first = info?.infoItem?.first else {
                    SVProgressHUD.dismiss()
                    self.log("  List is empty, skipping delete")
                    SVProgressHUD.showInfo(withStatus: "List is empty")
                    return
                }
                let appName = first.appName
                guard !appName.isEmpty else {
                    SVProgressHUD.dismiss()
                    self.log("  First item appName is empty, skipping delete")
                    SVProgressHUD.showInfo(withStatus: "appName is empty")
                    return
                }
                let param = IDOAppletControlModel(operate: 2, appName: appName)
                self.log("  Deleting first item: \(appName)")
                self.log("  Request: \(param.toJsonString() ?? "{}")")
                SVProgressHUD.show(withStatus: "15.54 Delete \(appName)")
                Cmds.setAppleControl(param).send { [weak self] delRes in
                    SVProgressHUD.dismiss()
                    self?.logCmdResult("15.54 Applet delete first item", delRes)
                }
            }
        }
    }

    private func setAppBaseInfoSample() {
        guard ensureConnected() else { return }
        let param = appBaseInfoSampleParam()
        log("--- 15.79 APP basic info (setAppBaseInfo) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.79 APP basic info")
        Cmds.setAppBaseInfo(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.79 APP basic info", res)
        }
    }

    private func takeMedicineRemindSet() {
        guard ensureConnected() else { return }
        let cal = Calendar.current
        let now = Date()
        let t1Date = cal.date(byAdding: .minute, value: 1, to: now) ?? now
        let t2Date = cal.date(byAdding: .minute, value: 2, to: now) ?? now
        let t1 = IDOTakeMedicineRemindTimeItemModel()
        t1.hour = cal.component(.hour, from: t1Date)
        t1.minute = cal.component(.minute, from: t1Date)
        let t2 = IDOTakeMedicineRemindTimeItemModel()
        t2.hour = cal.component(.hour, from: t2Date)
        t2.minute = cal.component(.minute, from: t2Date)
        let infoItem = IDOTakeMedicineRemindInfoItemModel()
        infoItem.index = 1
        infoItem.year = cal.component(.year, from: t1Date)
        infoItem.month = cal.component(.month, from: t1Date)
        infoItem.day = cal.component(.day, from: t1Date)
        infoItem.repeat = 127
        infoItem.name = "Aspirin"
        infoItem.dailyTimes = 2
        infoItem.timeCount = 2
        infoItem.timeItems = [t1, t2]
        let param = IDOTakeMedicineRemindModel(
            operate: 1,
            medicineShowOnOff: 1,
            takeMedicineInfoCount: 1,
            takeMedicineInfoItem: infoItem
        )
        log("--- 15.90 Medication reminder set (takeMedicineRemind operate=1) ---")
        log(String(format: "  Reminder time: %02d:%02d(+1min), %02d:%02d(+2min)", t1.hour ?? 0, t1.minute ?? 0, t2.hour ?? 0, t2.minute ?? 0))
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.90 Medication reminder set")
        Cmds.takeMedicineRemind(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.90 Medication reminder set", res)
        }
    }

    private func takeMedicineRemindQuery() {
        guard ensureConnected() else { return }
        let param = IDOTakeMedicineRemindModel(operate: 2)
        log("--- 15.90 Medication reminder query (takeMedicineRemind operate=2) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.90 Medication reminder query")
        Cmds.takeMedicineRemind(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.90 Medication reminder query", res)
        }
    }

    /// Query first, then delete the first item of the returned list (operate=3, index of the first item)
    private func takeMedicineRemindDeleteFirst() {
        guard ensureConnected() else { return }
        log("--- 15.90 Medication reminder delete first (query first, then operate=3) ---")
        SVProgressHUD.show(withStatus: "15.90 Query medication reminder")
        Cmds.takeMedicineRemind(IDOTakeMedicineRemindModel(operate: 2)).send { [weak self] res in
            guard let self = self else { return }
            switch res {
            case .failure:
                SVProgressHUD.dismiss()
                self.logCmdResult("15.90 Medication reminder (query before delete)", res)
                return
            case .success(let model):
                self.logCmdResult("15.90 Medication reminder (query before delete)", res)
                guard let first = model?.takeMedicineInfoItems?.first,
                      let index = first.index,
                      index > 0 else {
                    SVProgressHUD.dismiss()
                    self.log("  List is empty or index invalid, skipping delete")
                    SVProgressHUD.showInfo(withStatus: "Nothing to delete")
                    return
                }
                let del = IDOTakeMedicineRemindModel(operate: 3, index: index)
                self.log("  Deleting first item index=\(index) name=\(first.name ?? "")")
                self.log("  Request: \(del.toJsonString() ?? "{}")")
                SVProgressHUD.show(withStatus: "15.90 Medication reminder delete")
                Cmds.takeMedicineRemind(del).send { [weak self] delRes in
                    SVProgressHUD.dismiss()
                    self?.logCmdResult("15.90 Medication reminder delete first item", delRes)
                }
            }
        }
    }

    private func takeMedicineRemindSetSwitch() {
        guard ensureConnected() else { return }
        let param = IDOTakeMedicineRemindModel(operate: 4, medicineShowOnOff: 1)
        log("--- 15.90 Medication reminder set master switch (takeMedicineRemind operate=4) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.90 Medication reminder master switch")
        Cmds.takeMedicineRemind(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.90 Medication reminder master switch", res)
        }
    }

    /// Medication reminder icon side length (px); the `.medic` protocol requires 160×160 PNG
    private static let medicIconPixelSize: CGFloat = 160

    private func takeMedicineRemindIconTransfer() {
        guard ensureConnected() else { return }
        let ft = sdk.funcTable
        guard ft.getNotifyIconAdaptive else {
            log("Skipped: support_v3_notify_icon_adaptive=false")
            SVProgressHUD.showInfo(withStatus: "Adaptive icon not supported")
            return
        }
        guard ft.supportTakeMedicineReminder else {
            log("Skipped: support_take_medicine_reminder=false")
            SVProgressHUD.showInfo(withStatus: "Medication reminder not supported")
            return
        }
        guard UIImagePickerController.isSourceTypeAvailable(.photoLibrary) else {
            log("Photo library unavailable")
            SVProgressHUD.showError(withStatus: "Photo library unavailable")
            return
        }
        log("--- 15.90 Medication reminder icon transfer: select an image, it will be cropped to 160×160 PNG ---")
        let picker = UIImagePickerController()
        picker.sourceType = .photoLibrary
        picker.allowsEditing = true
        picker.delegate = self
        present(picker, animated: true)
    }

    /// Center-crop the selected image to a square, scale it to 160×160, and write a temporary PNG
    private func makeMedicIconPNGFile(from image: UIImage) -> String? {
        let side = Self.medicIconPixelSize
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1
        format.opaque = false
        let renderer = UIGraphicsImageRenderer(size: CGSize(width: side, height: side), format: format)
        let scaled = renderer.image { _ in
            let imgSize = image.size
            guard imgSize.width > 0, imgSize.height > 0 else { return }
            let scale = max(side / imgSize.width, side / imgSize.height)
            let drawn = CGSize(width: imgSize.width * scale, height: imgSize.height * scale)
            let origin = CGPoint(x: (side - drawn.width) / 2, y: (side - drawn.height) / 2)
            image.draw(in: CGRect(origin: origin, size: drawn))
        }
        guard let pngData = scaled.pngData() else { return nil }
        let dir = NSTemporaryDirectory()
        let path = (dir as NSString).appendingPathComponent("medic_icon_160.png")
        do {
            try pngData.write(to: URL(fileURLWithPath: path), options: .atomic)
            return path
        } catch {
            log("Failed to write temporary PNG: \(error.localizedDescription)")
            return nil
        }
    }

    private func startMedicIconTransfer(imgPath: String) {
        log("--- 15.90 Medication reminder icon transfer (.medic / 0x1C) ---")
        log("  Source image: \(imgPath) (160×160 PNG)")
        SVProgressHUD.show(withStatus: "Transferring icon...")
        let fileSize = (try? FileManager.default.attributesOfItem(atPath: imgPath)[.size] as? NSNumber)?.intValue ?? 0
        let item = IDOTransMedicModel(filePath: imgPath, fileName: "1", fileSize: fileSize)
        sdk.transfer.transferFiles(fileItems: [item], cancelPrevTranTask: true) { [weak self] curIdx, total, _, totalProg in
            self?.log("  Progress: \(curIdx + 1)/\(total) \(Int(totalProg * 100))%")
        } transStatus: { [weak self] curIdx, status, errCode, _ in
            self?.log("  Status[\(curIdx)]: \(status.rawValue) err=\(errCode)")
        } completion: { [weak self] results in
            SVProgressHUD.dismiss()
            let ok = !results.isEmpty && results.allSatisfy { $0 }
            self?.log("Transfer done: \(results)")
            SVProgressHUD.showInfo(withStatus: ok ? "Icon transfer succeeded" : "Icon transfer failed")
        }
    }

    private func setPurchasedWatchFaceInfoSample() {
        guard ensureConnected() else { return }
        let param = IDOPurchasedWatchFaceInfoModel(
            paymentStatus: 3, userId: "user_123", watchId: "dial_001"
        )
        log("--- 15.91 Purchased watch faces (setPurchasedWatchFaceInfo) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.91 Purchased watch faces")
        Cmds.setPurchasedWatchFaceInfo(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.91 Purchased watch faces", res)
        }
    }

    private func setAppDownloadStatusInfoSample() {
        guard ensureConnected() else { return }
        let param = IDOAppDownloadStatusInfoModel(type: 1, status: 1, id: "watch_face_001")
        log("--- 15.92 Download status (setAppDownloadStatusInfo) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.92 Download status")
        Cmds.setAppDownloadStatusInfo(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.92 Download status", res)
        }
    }

    private func getFirmwarePositionInfoQuery() {
        guard ensureConnected() else { return }
        let param = IDOFirmwarePositionInfoModel(operate: 1)
        log("--- 15.93 Firmware positioning query (getFirmwarePositionInfo operate=1) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.93 Firmware positioning query")
        Cmds.getFirmwarePositionInfo(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            self?.logCmdResult("15.93 Firmware positioning query", res)
        }
    }

    /// Query first, then confirm receipt of the returned record timestamp with operate=2
    private func getFirmwarePositionInfoConfirm() {
        guard ensureConnected() else { return }
        log("--- 15.93 Firmware positioning confirm (query first, then operate=2) ---")
        SVProgressHUD.show(withStatus: "15.93 Query positioning data")
        let query = IDOFirmwarePositionInfoModel(operate: 1)
        Cmds.getFirmwarePositionInfo(query).send { [weak self] res in
            guard let self else { return }
            switch res {
            case .failure:
                SVProgressHUD.dismiss()
                self.logCmdResult("15.93 Firmware positioning (query before confirm)", res)
            case .success(let model):
                self.logCmdResult("15.93 Firmware positioning (query before confirm)", res)
                guard let ts = model?.positionInfoItem?.timestamp, (model?.positionInfoCount ?? 0) > 0 else {
                    SVProgressHUD.dismiss()
                    self.log("  No positioning data or timestamp missing, skipping confirm")
                    SVProgressHUD.showInfo(withStatus: "No data to confirm")
                    return
                }
                let ack = IDOFirmwarePositionInfoModel(operate: 2, timestamp: ts)
                self.log("  Confirming timestamp=\(ts)")
                self.log("  Request: \(ack.toJsonString() ?? "{}")")
                SVProgressHUD.show(withStatus: "15.93 Confirm receipt")
                Cmds.getFirmwarePositionInfo(ack).send { [weak self] ackRes in
                    SVProgressHUD.dismiss()
                    self?.logCmdResult("15.93 Firmware positioning confirm", ackRes)
                }
            }
        }
    }

    /// App list style query (operate=2)
    private func appListStyleQuery() {
        guard ensureConnected() else { return }
        let param = IDOAppListStyleParamModel(operate: 2)
        log("--- 15.73 App list style query (appListStyle operate=2) ---")
        log("  Request: \(param.toJsonString() ?? "{}")")
        SVProgressHUD.show(withStatus: "15.73 App list style query")
        Cmds.appListStyle(param).send { [weak self] res in
            SVProgressHUD.dismiss()
            if case .success(let model) = res, let model {
                self?.log("  Used \(model.userApplicationListItemNum)/\(model.applicationListTotalNum), list=\(model.listItems?.count ?? 0)")
            }
            self?.logCmdResult("15.73 App list style query", res)
        }
    }

    /// Query first, then delete the first item of the returned list (operate=3)
    private func appListStyleDeleteFirst() {
        guard ensureConnected() else { return }
        log("--- 15.73 App list style delete (query first, then operate=3) ---")
        SVProgressHUD.show(withStatus: "15.73 Query app list style")
        Cmds.appListStyle(IDOAppListStyleParamModel(operate: 2)).send { [weak self] res in
            guard let self else { return }
            switch res {
            case .failure:
                SVProgressHUD.dismiss()
                self.logCmdResult("15.73 App list style (query before delete)", res)
            case .success(let model):
                self.logCmdResult("15.73 App list style (query before delete)", res)
                guard let first = model?.listItems?.first, !first.name.isEmpty else {
                    SVProgressHUD.dismiss()
                    self.log("  List is empty or name is empty, skipping delete")
                    SVProgressHUD.showInfo(withStatus: "Nothing to delete")
                    return
                }
                let del = IDOAppListStyleParamModel(operate: 3, name: first.name)
                self.log("  Deleting name=\(first.name)")
                self.log("  Request: \(del.toJsonString() ?? "{}")")
                SVProgressHUD.show(withStatus: "15.73 App list style delete")
                Cmds.appListStyle(del).send { [weak self] delRes in
                    SVProgressHUD.dismiss()
                    self?.logCmdResult("15.73 App list style delete", delRes)
                }
            }
        }
    }

    private func appBaseInfoSampleParam() -> IDOAppInfoModel {
        let model = IDOAppInfoModel(userName: "mssj52u@163.com", operate: 1)
        model.userId = "271314614262829056"
        model.token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJkYXRldGltZSI6MTc4MzkyODcwNjkzMiwidXNlcl90eXBlIjoiVVNFUiIsInVzZXJfaWQiOiIyNzEzMTQ2MTQyNjI4MjkwNTYiLCJzb3VyY2UiOiJhcHAiLCJ0eXBlIjoiYXBwIiwiYXBwX2lkIjoiMTAwMDAiLCJhY2NvdW50IjoibXNzajUydUAxNjMuY29tIiwiaWF0IjoxNzgzOTI4NzA2LCJleHAiOjQ5Mzc1Mjg3MDZ9.-FPYg231BUTM2LzfihWdIeoStGJGNT6G1Oga0Ik6VWsivIhWTfQzH32C5to7C5txa5QhmVhW-aAD9q73phOeAw"
        model.domainName = "ali"
        model.appVersion = "3.5.0"
        model.appKey = "548a50bc9f0a45d0bdfcdb5d194641d8"
        model.phoneSystem = 1
        model.isFilterWatch = 2
        model.appFaceVersion = "6"
        return model
    }
}

extension SdkFeatureTestVC: UIImagePickerControllerDelegate, UINavigationControllerDelegate {
    func imagePickerController(
        _ picker: UIImagePickerController,
        didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]
    ) {
        picker.dismiss(animated: true)
        let picked = (info[.editedImage] as? UIImage) ?? (info[.originalImage] as? UIImage)
        guard let image = picked else {
            log("Failed to get image")
            SVProgressHUD.showError(withStatus: "Failed to get image")
            return
        }
        guard let imgPath = makeMedicIconPNGFile(from: image) else {
            SVProgressHUD.showError(withStatus: "Crop to 160×160 failed")
            return
        }
        log("  Cropped to 160×160 PNG: \(imgPath)")
        startMedicIconTransfer(imgPath: imgPath)
    }

    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true)
        log("Medication reminder icon selection cancelled")
    }
}

extension SdkFeatureTestVC: IDOExchangeDataOCDelegate {

    func appListenBleExec(model: NSObject) {
        // This page only tests requests initiated by the app side; sports started by the ble side are not handled here
    }

    func appListenAppExec(model: NSObject) {
        if let obj = model as? IDOAppActivityDataV3ExchangeModel {
            log("[exchangeData] 15.20 Sport summary step=\(obj.step) distance=\(obj.distance)")
            log("  bodyAge=\(obj.bodyAge) swimmingPoolDistance=\(obj.swimmingPoolDistance) actType=\(obj.actType) gpsStatus=\(obj.gpsStatus)")
        } else {
            log("[exchangeData] model=\(type(of: model))")
        }
    }

    func exchangeV2Data(model: IDOExchangeV2Model) {
        log("[exchangeV2Data] \(model)")
    }

    func exchangeV3Data(model: IDOExchangeV3Model) {
        log("[exchangeV3Data] \(model)")
    }
}
