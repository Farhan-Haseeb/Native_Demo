//
//  XYCentralManager.swift
//  XYCBCentralManager
//
//  Created by macvivi on 2020/12/30.
//

import UIKit
import CoreBluetooth

typealias ErrorClosure = (Error?)->Void
typealias DataClosure = (Data?,Error?)->Void
typealias VoidCompletion = () -> Void

class XYCentralManager: NSObject {

    //Create the singleton
    static let shared = XYCentralManager()
    
    //After calling startScan, the information found so far is available through this property
    var scanInfoArray: [ScanInfo] {
        XYCBBase.shared.scanInfoArr
    }
    
    var scanInfoListSorted: [ScanInfo] {
        XYCBBase.shared.scanInfoArr.sorted { $0.rssi.intValue > $1.rssi.intValue }
    }
    
    //MARK: Start scanning
    func startScan(findPeripheral: @escaping (CBPeripheral) -> Void) {
        XYCBBase.shared.startScan { (peripheral) in
            findPeripheral(peripheral)
        }
    }
    
    func stopScan() {
        XYCBBase.shared.stopScan()
    }

    /// After Bluetooth is ready, use the UUID to restore ScanInfo (for entering the function page from the bound list).
    func retrieveScanInfo(uuid: String, name: String, completion: @escaping (ScanInfo?) -> Void) {
        XYCBBase.shared.retrieveScanInfo(uuid: uuid, name: name, completion: completion)
    }
    
    //MARK: Connect
    //The connected callback fires only after the connection succeeds and all peripheral characteristics have been discovered
    //If the connection drops after connecting, it reconnects automatically, unless disConnect is called; after calling disConnect there is no auto-reconnect
    func connect(peripheral: CBPeripheral, completions: @escaping (Bool,Error?) -> Void) {
        XYCBBase.shared.connect(peripheral: peripheral) { (isConnect, error) in
            completions(isConnect,error)
        }
    }
    
    func disConnect(peripheral: CBPeripheral, completions: @escaping () -> Void) {
        XYCBBase.shared.disConnect(peripheral, completion: completions)
    }
    
    //MARK: mtu: maximum transmission unit
    //maximum transmission unit
    //Maximum bytes per packet; if the data exceeds the mtu, the framework automatically splits it into packets.
    //Defaults to peripheral.maximumWriteValueLength; normally the mtu does not need to be set manually, the framework handles it for you
    //Only set it manually if the peripheral's Bluetooth module is non-standard and sending larger data fails when mtu is set to peripheral.maximumWriteValueLength
    var mtu: Int {
        set {
            XYCBBase.shared.mtu = newValue
        }
        get {
            XYCBBase.shared.mtu
        }
    }
    
    //MARK: Write
    //errorClosure: nil means the data was written successfully, otherwise it carries the error reason
    func write(peripheral: CBPeripheral, characteristic: CBCharacteristic, data: Data, errorClosure: ErrorClosure?){
        XYCBBase.shared.write(peripheral: peripheral, characteristic: characteristic, data: data, errorClosure: errorClosure)
    }
    
    //MARK: Write without response
    func writeWithoutResponse(peripheral: CBPeripheral, characteristic: CBCharacteristic, data: Data){
        XYCBBase.shared.writeWithoutResponse(peripheral: peripheral, characteristic: characteristic, data: data)
    }

    //MARK: Subscribe to notifications
    //Parameter errorClosure: when the parameter passed to errorClosure is nil the subscription succeeded, otherwise the Error parameter carries the reason the subscription failed
    //Parameter notifyClosure: notifyClosure receives the incoming Data, and notifyClosure is called whenever a notification is received
    func subscribeNotify(peripheral: CBPeripheral, characteristic: CBCharacteristic, errorClosure: ErrorClosure?, notifyClosure: @escaping DataClosure){
        XYCBBase.shared.subscribeNotify(peripheral: peripheral, characteristic: characteristic, errorClosure: errorClosure, notifyClosure: notifyClosure)
    }
    
    //MARK: Read
    //callBack returns the data that was read
    func read(peripheral: CBPeripheral, characteristic: CBCharacteristic, callBack: @escaping DataClosure){
        XYCBBase.shared.read(peripheral: peripheral, characteristic: characteristic, callBack: callBack)
    }
    



}
