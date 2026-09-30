//
//  LiteBoundStore.swift
//  IDODemoLite
//
//  Lite bound-device snapshot (mac / name / uuid). Isolated from the Full snapshot.
//

import Foundation
import CoreBluetooth

struct LiteBoundSnapshot: Codable {
    var mac: String
    var name: String
    var uuid: String

    /// After Central poweredOn, restore ScanInfo for the function page to connect.
    func restoreScanInfo(completion: @escaping (ScanInfo?) -> Void) {
        bleMgr.retrieveScanInfo(uuid: uuid, name: name, completion: completion)
    }
}

extension UserDefaults {
    private static let liteBoundDevicesKey = "lite-bound-devices"

    /// Lite: only writes the bind flag; the display list uses saveLiteBoundDevice / removeLiteBoundDevice.
    func setBind(_ macAddress: String, isBind: Bool) {
        let key = Constant.bindKey + "-" + macAddress
        setValue(isBind, forKey: key)
        synchronize()
    }

    func loadLiteBoundDevices() -> [LiteBoundSnapshot] {
        guard let data = data(forKey: Self.liteBoundDevicesKey) else { return [] }
        return (try? JSONDecoder().decode([LiteBoundSnapshot].self, from: data)) ?? []
    }

    func isLiteBound(uuid: String) -> Bool {
        loadLiteBoundDevices().contains { $0.uuid == uuid }
    }

    func saveLiteBoundDevice(mac: String, name: String, uuid: String) {
        // uuid is empty so retrieve is impossible; refuse to write to the list (the bind flag can still be written separately via setBind)
        guard !mac.isEmpty, !uuid.isEmpty, UUID(uuidString: uuid) != nil else { return }
        var list = loadLiteBoundDevices()
        list.removeAll { $0.mac == mac || $0.uuid == uuid }
        list.insert(LiteBoundSnapshot(mac: mac, name: name.isEmpty ? mac : name, uuid: uuid), at: 0)
        persistLiteBoundDevices(list)
    }

    func removeLiteBoundDevice(mac: String) {
        var list = loadLiteBoundDevices()
        list.removeAll { $0.mac == mac }
        persistLiteBoundDevices(list)
    }

    private func persistLiteBoundDevices(_ list: [LiteBoundSnapshot]) {
        let data = (try? JSONEncoder().encode(list)) ?? Data()
        set(data, forKey: Self.liteBoundDevicesKey)
        synchronize()
    }
}
