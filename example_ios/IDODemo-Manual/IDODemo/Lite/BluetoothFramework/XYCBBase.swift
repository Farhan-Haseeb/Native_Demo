//
//  XYCBBase.swift
//  XYCBCentralManager
//
//  Created by macvivi on 2020/12/30.
//

import UIKit
import CoreBluetooth

private let _servicesUUID : [CBUUID] = {
    let uuid1 = CBUUID.init(string: "00001530-1212-EFDE-1523-785FEABCD123")
    let uuid2 = CBUUID.init(string: "00000af0-0000-1000-8000-00805f9b34fb")
    let uuid3 = CBUUID.init(string: "FE59")
    return [uuid1,uuid2,uuid3]
}()

private let _option = [CBCentralManagerScanOptionAllowDuplicatesKey : NSNumber.init(value: false)]

class ScanInfo: NSObject {
    var peripheral: CBPeripheral
    var advertisementData: [String : Any]
    var rssi: NSNumber
    init(peripheral: CBPeripheral, advertisementData: [String : Any], rssi: NSNumber) {
        self.peripheral = peripheral
        self.advertisementData = advertisementData
        self.rssi = rssi
    }
    
    var peripheralName: String? {
        guard self.advertisementData["kCBAdvDataLocalName"] != nil else {
            return nil
        }
        return self.advertisementData["kCBAdvDataLocalName"] as? String
    }
    
    var uuid: String {
        return peripheral.identifier.uuidString
    }
    
    override func isEqual(_ object: Any?) -> Bool {
        let object: ScanInfo = object as! ScanInfo
        return self.peripheral.identifier == object.peripheral.identifier ? true : false
    }
}



class XYCBBase: NSObject {
    //Create the singleton
    static let shared = XYCBBase()
    override init() {
        super.init()
        xyCentralManager.delegate = self
        
    }
    var xyCentralManager: CBCentralManager = CBCentralManager()
    var scanInfoArr = [ScanInfo]()
    //Whether Bluetooth is available
    var managerStateIsOn = false
    typealias FindPeripheral = (CBPeripheral) -> Void
    var findPeripheralClosure: FindPeripheral = {_ in }
    
    
    //MARK: Step 1: Scan for peripherals
    func startScan(findPeripheral: @escaping (CBPeripheral) -> Void) {
        scanInfoArr = [ScanInfo]()
        findPeripheralClosure = findPeripheral
        xyCentralManager.scanForPeripherals(withServices: _servicesUUID, options: _option)
        managerStateIsOn = xyCentralManager.state != CBManagerState.poweredOn ? false : true
    }
    
    func stopScan() {
        xyCentralManager.stopScan()
        scanInfoArr = [ScanInfo]()
    }
    
    typealias Completion = (Bool,Error?) -> Void
    var connetCompletion: Completion?
    //MARK: Step 2: Connect to the peripheral
    func connect(peripheral: CBPeripheral, completion: @escaping (Bool,Error?) -> Void) {
        connetCompletion = completion
        xyCentralManager.connect(peripheral, options: nil)
    }
    
    typealias DisconnectCompletion = () -> Void
    var _dsconnectCompletion: DisconnectCompletion?
    //Whether disConnect was called; if it was, there is no auto-reconnect
    var isDisConnect = false
    func disConnect(_ peripheral: CBPeripheral, completion: @escaping () -> Void) {
        _dsconnectCompletion = completion
        isDisConnect = true
        xyCentralManager.cancelPeripheralConnection(peripheral)
    }
    
    //maximum transmission unit: MTU
    private var privateMtu = 0
    var mtu: Int {
        set {
            if newValue > 0 {
                privateMtu = newValue
            }
        }
        get {
            privateMtu
        }
    }
    var writeErrorClosure: ErrorClosure?
    //MARK: Write
    func write(peripheral: CBPeripheral, characteristic: CBCharacteristic, data: Data, errorClosure: ErrorClosure?){
        self.writeErrorClosure = errorClosure
        //printXY(peripheral.maximumWriteValueLength(for: .withResponse), obj: self, line: #line)
        //printXY("Send:\(NSData(data: data))", obj: self, line: #line)
        let bytes = [UInt8](data)
        var send:[UInt8] = [];
        let mode:Int = peripheral.maximumWriteValueLength(for: .withResponse)
        if bytes.count > mode {
            var i:Int = 1;
            let total = bytes.count/mode+1;
            while i<=total {
                if i==total{
                    if (i-1)*mode <= bytes.count-1 {
                        send = Array(bytes[(i-1)*mode...(bytes.count-1)]);
                    }else{
                        break
                    }
                }else{
                    send = Array(bytes[(i-1)*mode...(mode*i-1)]);
                }
                
                let data:Data = Data.init(send);
                let nsdata:NSData = NSData.init(bytes: send, length: send.count);
                peripheral.writeValue(data, for: characteristic, type: .withResponse)
                print("Send packet:\(nsdata)");
                //Wait 0.1 ms between two packets
                Thread.sleep(forTimeInterval: 0.0001)
                i+=1;
            }
        }else {
            peripheral.writeValue(data, for: characteristic, type: .withResponse)
        }
    }
    
    //MARK: Write without response - writeWithoutResponse
    func writeWithoutResponse(peripheral: CBPeripheral, characteristic: CBCharacteristic, data: Data){
//        printXY(peripheral.maximumWriteValueLength(for: .withoutResponse), obj: self, line: #line)
//        printXY(peripheral.canSendWriteWithoutResponse, obj: self, line: #line)
        //printXY("Send:\(NSData(data: data))", obj: self, line: #line)
//        printXY(peripheral.canSendWriteWithoutResponse, obj: self, line: #line)
        let bytes = [UInt8](data)
        var send:[UInt8] = [];
        let mode:Int = peripheral.maximumWriteValueLength(for: .withoutResponse)
        if bytes.count > mode {
            var i:Int = 1;
            let total = bytes.count/mode+1;
            while i<=total {
                if i==total{
                    if (i-1)*mode <= bytes.count-1 {
                        send = Array(bytes[(i-1)*mode...(bytes.count-1)]);
                    } else {
                        break
                    }
                }else{
                    send = Array(bytes[(i-1)*mode...(mode*i-1)]);
                }
               
                let data:Data = Data.init(send);
                let nsdata:NSData = NSData.init(bytes: send, length: send.count);
                peripheral.writeValue(data, for: characteristic, type: .withoutResponse)
//                printXY(peripheral.canSendWriteWithoutResponse, obj: self, line: #line)
                printXY("Send packet:\(nsdata)", obj: self, line: #line)
//                printXY(peripheral.canSendWriteWithoutResponse, obj: self, line: #line)
                //Wait 0.1 ms between two packets
                Thread.sleep(forTimeInterval: 0.0001)
                i+=1;
            }
    }else {
        peripheral.writeValue(data, for: characteristic, type: .withoutResponse)
    }
    }
    
    var notifyErrorClosure: ErrorClosure?
    var notifyClosure: DataClosure?
    //MARK: Notify
    //Parameter errorClosure: when the parameter passed to errorClosure is nil the subscription succeeded, otherwise the Error parameter carries the reason the subscription failed
    //Parameter notifyClosure: notifyClosure receives the incoming Data, and notifyClosure is called whenever a notification is received
    func subscribeNotify(peripheral: CBPeripheral, characteristic: CBCharacteristic, errorClosure: ErrorClosure?, notifyClosure: @escaping DataClosure){
        self.notifyClosure = notifyClosure
        self.notifyErrorClosure = errorClosure
        //characteristic sends a notification when its value changes
        peripheral.setNotifyValue(true, for: characteristic)
    }
    
    var readCallBack: DataClosure?
    //MARK: Read
    //callBack returns the data that was read
    func read(peripheral: CBPeripheral, characteristic: CBCharacteristic, callBack: @escaping DataClosure){
        readCallBack = callBack
        peripheral.readValue(for: characteristic)
    }
}

extension XYCBBase: CBCentralManagerDelegate {
    //MARK: CBCentralManagerDelegate
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if managerStateIsOn == false {
            xyCentralManager.scanForPeripherals(withServices: _servicesUUID, options: _option)
        }
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        if peripheral.name == nil {
            return
        }
        let scanInfo = ScanInfo(peripheral: peripheral, advertisementData: advertisementData, rssi: RSSI)
        //MARK: Step 1: Peripheral found
        //remove first, then add: de-duplicates and keeps the newest entry in the array
        scanInfoArr.removeAll { $0 == scanInfo }
        scanInfoArr.append(scanInfo)
        
        findPeripheralClosure(peripheral)
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        printXY(#function, obj: self, line: #line)
        if isDisConnect {
            isDisConnect = false
            if let closure = self._dsconnectCompletion {
                closure()
            }
            return
        }
        connect(peripheral: peripheral) { (bool, error) in
            if bool {
//                printXY("Reconnected", obj: self, line: #line)
            }else {
                printXY(error, obj: self, line: #line)
            }
        }
    }
    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        //MARK: Step 2: Peripheral connected
//        printXY(#function, obj: self, line: #line)
        //MARK: Step 3: Discover the peripheral's services
        peripheral.delegate = self
        //Services must be discovered; only then does the peripheral.sevices array get values, otherwise it is empty. Each service discovered adds one more service
        //At this point po peripheral.services is nil
        peripheral.discoverServices(nil)
    }
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        if let closure = connetCompletion {
            closure(false,error)
        }
    }
}

extension XYCBBase: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
//        print("Discovered peripheral services: \(String(describing: peripheral.services))")
        guard let services = peripheral.services else {
            return
        }
        for service in services {
            //MARK: Step 4: Discover the peripheral's characteristics
            peripheral.discoverCharacteristics(nil, for: service)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
//        print("Discovered service characteristics: \(String(describing: service.characteristics))")
        if peripheral.services?.last == service {
            printXY("All peripheral characteristics discovered, ready", obj: self, line: #line)
            guard let closure = connetCompletion else {
                return
            }
            closure(true,nil)
        }
        guard let characteristics = service.characteristics else {
            return
        }
    }
    
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        print(#function)
        if let closure = self.writeErrorClosure {
            closure(error)
        }
    }
    
    func peripheralDidUpdateRSSI(_ peripheral: CBPeripheral, error: Error?) {
        print(#function)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didReadRSSI RSSI: NSNumber, error: Error?) {
        print(#function)
    }
    
    //readValue
    //setNotifyValue
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        //print(#function)
        if characteristic.isNotifying {
            if let closure = self.notifyClosure {
                closure(characteristic.value,error)
            }
        }else {
            if let closure = self.readCallBack {
                closure(characteristic.value,error)
            }
        }
    }
    
    //setNotifyValue
    func peripheral(_ peripheral: CBPeripheral, didUpdateNotificationStateFor characteristic: CBCharacteristic, error: Error?) {
        print(#function)
        if let closure = self.notifyErrorClosure {
            closure(error)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {
        print(#function)
        print(invalidatedServices)
    }
    
    func peripheralIsReady(toSendWriteWithoutResponse peripheral: CBPeripheral) {
        //printXY(#function, obj: self, line: #line)
        if #available(iOS 11.0, *) {
            //printXY(peripheral.canSendWriteWithoutResponse, obj: self, line: #line)
        } else {
            // Fallback on earlier versions
        }
    }
    
    
}

// MARK: - CBCharacteristic

extension CBCharacteristic {
    func dealCharacteristicProperties() -> String {
        var str = "Properties:"
        if self.properties.contains(.write) {
            str += "/writable"
        }
        if self.properties.contains(.read) {
            str += "/readable"
        }
        if self.properties.contains(.notify) {
            str += "/notify"
        }
        if self.properties.contains(.writeWithoutResponse) {
            str += "/write without response"
        }
        return str
    }
}

// MARK: - CBPeripheral

extension CBPeripheral {
    
    func writeCharacteristic() -> (CBPeripheral, CBCharacteristic)? {
        let service = services?.first(where: { $0.uuid.uuidString == "0AF0" })
        let characteristic = service?.characteristics?.first(where: {  $0.uuid.uuidString == "0AF6" })
        guard (service?.peripheral != nil && characteristic != nil) else {  return nil }
        //        print("write service.uuid: \(service?.uuid.uuidString ?? "")  service.peripheral?.name:\(service?.peripheral?.name ?? "")")
        //        print("characteristic.uuid.uuidString: \(characteristic?.uuid.uuidString ?? "")")
        //        print("characteristic.dealCharacteristicProperties():\(characteristic?.dealCharacteristicProperties()  ?? "")")
        return (service!.peripheral!, characteristic!)
    }
    
    
    func notifyCharacteristic() -> (CBPeripheral, CBCharacteristic)? {
        let service = services?.first(where: { $0.uuid.uuidString == "0AF0" })
        let characteristic = service?.characteristics?.first(where: {  $0.uuid.uuidString == "0AF7" })
        guard (service?.peripheral != nil && characteristic != nil) else {  return nil }
        //        print("notify service.uuid: \(service?.uuid.uuidString ?? "")  service.peripheral?.name:\(service?.peripheral?.name ?? "")")
        //        print("characteristic.uuid.uuidString: \(characteristic?.uuid.uuidString ?? "")")
        //        print("characteristic.dealCharacteristicProperties():\(characteristic?.dealCharacteristicProperties()  ?? "")")
        return (service!.peripheral!, characteristic!)
    }
    
}
