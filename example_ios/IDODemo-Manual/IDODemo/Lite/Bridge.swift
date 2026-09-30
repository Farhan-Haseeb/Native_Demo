//
//  Bridge.swift
//  IDODemoLite
//
//  Created by hc on 12/22/23.
//

import Foundation

import protocol_channel
import CoreBluetooth

let bleMgr = XYCentralManager.shared


// MARK: - Actions

extension MainPageVC {
    
    // Register sdk, run only once
    func registerSDK() {
        // protocol library registration (required) (!!!important)
        sdk.bridge.setupBridge(delegate: self, logType: .debug)
        
        // To use Alexa, register it first
        sdk.alexa.setupAlexa(delegate: self, clientId: clientId)
        
        // epo upgrade
        IDOEpoManager.shared.enableAutoUpgrade = true
        IDOEpoManager.shared.delegateGetGps = self
        IDOEpoManager.shared.listenEpoUpgrade { status in
            print("epo---- status:\(status)")
        } downProgress: { progress in
            print("epo---- down progress:\(progress)")
        } sendProgress: { progress in
            print("epo---- send progress:\(progress)")
        } funcComplete: { errCode in
            print("epo---- complete:\(errCode)")
        }
    }
    
    func stopScan() {
        bleMgr.stopScan()
        print("stopScan")
    }
    
    
    
    @objc func _reload() {
        print("reload")
        bleMgr.stopScan()
        scan()
    }
    
    @objc func scan() {
        bleMgr.startScan { [weak self] obj in
            //print("list count: \(String(describing: bleMgr.scanInfoArray.count))")
            self?.dataList.removeAll()
            self?.dataList.append(contentsOf: bleMgr.scanInfoListSorted)
            self?.tableView.reloadData()
        }
    }
}


// MARK: - IDOBridgeDelegate

extension MainPageVC: IDOBridgeDelegate {
    func listenWaitingOtaDevice(otaDevice: protocol_channel.IDOOtaDeviceModel) {
        
    }
    
    
    // The command data must be sent to the Bluetooth device (!!!important)
    func writeDataToBle(bleData: protocol_channel.IDOBleData) {
        guard let device = currentDeviceModel else { return }
        
        guard let data = bleData.data else { return }
        guard data.count >= 2 else {
            print("Empty data")
            return
        }
        let data0 = data[0]
        let data1 = data[1]
        let isTrans = (((data0 == 0xD1 || data0 == 0x13) && data1 == 0x02) || (data0 == 0x02 && data1 == 0x03))
        guard let characteristic = device.peripheral.writeCharacteristic() else { return }
        if (!isTrans) {
            bleMgr.write(peripheral: characteristic.0, characteristic: characteristic.1, data: data) { err in
                if err != nil {
                    print("Write failed: \(err.debugDescription)")
                }else {
                    // Write complete (!!!important)
                    sdk.bridge.writeDataComplete()
                }
            }
        }else {
            // No response needed
            bleMgr.writeWithoutResponse(peripheral: characteristic.0, characteristic: characteristic.1, data: data)
            // Write complete (!!!important)
            sdk.bridge.writeDataComplete()
        }
    }
    
    
    func listenStatusNotification(status: IDOStatusNotification) {
        print("StatusNotification: \(status)")
        NotificationCenter.default.post(name: Notify.onSdkStatusChanged, object: status)
    }
    
    func listenDeviceNotification(model: IDODeviceNotificationModel) {
        print("DeviceNotification: \(model)")
        NotificationCenter.default.post(name: Notify.onSdkDeviceStateChanged, object: model)
    }
    
    func checkDeviceBindState(macAddress: String) -> Bool {
        let isBinded = UserDefaults.standard.isBind(macAddress)
        print("checkDeviceBindState mac\(macAddress) isBinded:\(isBinded)")
        return isBinded
    }
}

// MARK: - IDOAlexaDelegate

extension MainPageVC: IDOAlexaDelegate {
    func getHealthValue(valueType: IDOGetValueType) -> Int {
        return 0
    }
    
    func getHrValue(dataType: Int, timeType: Int) -> Int {
        return 0
    }
    
    func functionControl(funType: Int) {}
}


// MARK: - IDOEpoManagerDelegate
extension MainPageVC: IDOEpoManagerDelegate{
    
    func getAppGpsInfo() -> protocol_channel.IDOOtaGpsInfo? {
        // !!!: The latitude and longitude here are pseudocode
        return IDOOtaGpsInfo(longitude: 114.0579, latitude: 22.5431, altitude: 10)
    }
    
    
}
