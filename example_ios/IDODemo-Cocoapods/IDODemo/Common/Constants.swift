//
//  Constants.swift
//  IDODemo
//
//  Created by hc on 2023/10/26.
//

import Foundation

struct Word {
    static let deviceConnect = "Device Connect"
    static let deviceDisconnect = "Device Disconnect"
    static let deviceBind = "Device Bind"
    static let deviceUnbind = "Device Unbind"
    static let setFunction = "Set Function"
    static let getFunction = "Get Function"
    static let syncData = "Sync Data"
    static let alexa = "Alexa"
    static let transFile = "Transfer File"
    static let sport = "Sport"
    static let testOC = "Test Objective-c (Console only)"
    static let exportLog = "Share log"
    static let testBleChannel = "Test ble channel"
    static let epoUpgrade = "Epo Upgrade"
    static let messageNotice = "Message notice"
    static let editSportScreen = "EditSportScreen"
    static let measure = "Measure"
    static let sdkFeatureTest = "SDK Feature Test"
    /// Temporary: synced from the channel example to Full (integration testing)
    static let internalFeatureTest = "Internal feature test"
    static let ida01FuncTest = "IDA01 Func Test"
    static let gestureControl = "Gesture Control"
    static let taskList = "Task List"
    static let walkRemind = "Walk Reminder"

}


struct Constant {
    static let bindKey = "bind-state"
    
    /// 2-second interval for exchanging brief sport data
    static let intervalExchangeData = 2

    /// 40-second interval for exchanging complete sport data
    static let intervalExchangeCompleteData = 40

    /// 30-second interval for exchanging V3 heart rate data
    static let intervalExchangeHrData = 30
}


struct Notify {
    static let onSdkStatusChanged = Notification.Name("ido-sdk-onSdkStatusChanged")
    static let onSdkDeviceStateChanged = Notification.Name("ido-sdk-onSdkDeviceStateChanged")
    static let onBleDeviceStateChanged = Notification.Name("ido-sdk-onBleDeviceStateChanged")
    static let onBleStateChanged = Notification.Name("ido-sdk-onBleStateChanged")
    static let onBleReceiveDataChanged = Notification.Name("ido-sdk-onBleReceiveDataChanged")
    static let onTaskListCacheCleared = Notification.Name("ido-sdk-onTaskListCacheCleared")
}
