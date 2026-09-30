//
//  TransferFileVC.swift
//  IDODemo
//
//  Created by hc on 2023/10/31.
//

import Foundation
import UIKit
import UniformTypeIdentifiers

import RxCocoa
import RxSwift
import SnapKit
import SVProgressHUD

import protocol_channel

class TransferFileVC: UIViewController {
    private lazy var tableView: UITableView = {
        let v = UITableView()
        v.dataSource = self
        v.delegate = self
        return v
    }()
    
    private lazy var items: [TransType] = {
        var list: [TransType] = [
            .contact,
            .ota,
            .watchFace
        ]
        if sdk.funcTable.getSupportV3BleMusic {
            list.insert(.mp3, at: 0)
        }
        
        if (sdk.device.deviceId == 859 || sdk.device.deviceId == 7884) {
            list.append(.app)
        }
        
        if (sdk.device.deviceId == 7883 || sdk.device.deviceId == 7882) {
            list.append(.gps)
        }
        
        // gps fw
        if (sdk.funcTable.getSupportUpdateGps) {
            list.append(.gpsFw)
        }
        
        return list
    }()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Transfer File"
        view.backgroundColor = .white
        
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

extension TransferFileVC: UITableViewDelegate, UITableViewDataSource {
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return items.count
    }
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 60
    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let _cellID = "UITableViewCell"
        let cell = tableView.dequeueReusableCell(withIdentifier: _cellID) ?? UITableViewCell(style: .default, reuseIdentifier: _cellID)
        cell.accessoryType = .disclosureIndicator
        cell.selectionStyle = .default
        cell.textLabel?.textColor = .black
        cell.textLabel?.textAlignment = .left
        let cmd = items[indexPath.row]
        cell.textLabel?.text = cmd.title()
        return cell
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let cmd = items[indexPath.row]
        if (cmd == .watchFace) {
            let vc = WatchFaceVC();
            navigationController?.pushViewController(vc, animated: true)
            return
        }
        let vc = TransferFileDetailVC(cmd: cmd)
        navigationController?.pushViewController(vc, animated: true)
    }
}


// MARK: - TransType

enum TransType {
    case mp3
    case wallpaper
    case contact
    case watchFace
    case ota
    case app
    case gps
    case gpsFw
}

extension TransType {

    func title() -> String {
        switch self {
        case .mp3:
            return "Upload music"
        case .wallpaper:
            return "Wallpaper watch face"
        case .contact:
            return "Upload contacts"
        case .watchFace:
            return "Watch face upgrade"
        case .ota:
            return "Firmware upgrade"
        case .app:
            return "Applet"
        case .gps:
            return "GPS"
        case .gpsFw:
            return "GPS Firmware"
        }
    }

    /// Allowed file extensions
    func allowedExtensions() -> [String] {
        switch self {
        case .mp3:       return ["mp3"]
        case .wallpaper: return ["png", "jpg", "jpeg"]
        case .contact:   return ["json"]
        case .watchFace: return []
        case .ota:
            switch sdk.device.platform {
            case 98, 99: return ["zip"]
            default:
                return ["zip", "bin", "fw"]
            }
        case .app:       return ["app"]
        case .gps:       return ["dat"]
        case .gpsFw:     return ["gps"]
        }
    }

    /// File selection hint
    func fileHint() -> String {
        let exts = allowedExtensions().map { ".\($0)" }.joined(separator: " / ")
        switch self {
        case .mp3:
            return "Select music file (\(exts))\nNote: Only 44.1k sample rate"
        case .wallpaper:
            return "Select wallpaper image (\(exts))"
        case .contact:
            return "Select contacts file (\(exts))"
        case .ota:
            return "Select firmware file (\(exts))"
        case .app:
            return "Select applet (\(exts))"
        case .gps:
            return "Select EPO file (\(exts))"
        case .gpsFw:
            return "Select GPS firmware (\(exts))"
        case .watchFace:
            return ""
        }
    }

    /// OTA prominent warning
    func otaWarning() -> String {
        return "Note: use the correct firmware file. A wrong firmware package will fail to install and may even brick the device so it cannot boot!\nWARNING: Use the correct firmware file. An incorrect firmware package may cause installation failure or brick the device!"
    }
}

// MARK: - TransferFileDetailVC

class TransferFileDetailVC: UIViewController {
    private var cmd: TransType
    private let disposeBag = DisposeBag()
    private var cancellable: IDOCancellable?
    @objc dynamic private var isTransferring = false

    /// User-selected file URL
    private var selectedFileURL: URL?

    // 98/99 platform: the OTA process reboots the device and then continues the transfer, so OTA mode must be flagged
    var _isSiceOta: Bool = false
    var _devInfo: (address: String, uuid: String, platform: Int, deviceId: Int)

    // MARK: - UI

    private lazy var btnSelectFile: UIButton = {
        let btn = UIButton(type: .system)
        btn.setTitle("📂 Select File", for: .normal)
        btn.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        btn.backgroundColor = UIColor(red: 0/255.0, green: 122/255.0, blue: 255/255.0, alpha: 1)
        btn.setTitleColor(.white, for: .normal)
        btn.layer.cornerRadius = 8
        btn.rx.tap.subscribe(onNext: { [weak self] in
            self?.selectFile()
        }).disposed(by: disposeBag)
        return btn
    }()

    private lazy var lblFileHint: UILabel = {
        let lbl = UILabel()
        lbl.font = .systemFont(ofSize: 12)
        lbl.textColor = .gray
        lbl.textAlignment = .center
        lbl.numberOfLines = 0
        lbl.text = cmd.fileHint()
        return lbl
    }()

    private lazy var lblOtaWarning: UILabel = {
        let lbl = UILabel()
        lbl.font = .systemFont(ofSize: 13, weight: .bold)
        lbl.textColor = .white
        lbl.textAlignment = .center
        lbl.numberOfLines = 0
        lbl.text = cmd.otaWarning()
        lbl.backgroundColor = UIColor(red: 204/255.0, green: 0/255.0, blue: 0/255.0, alpha: 1.0)
        lbl.layer.cornerRadius = 8
        lbl.clipsToBounds = true
        lbl.isHidden = true
        return lbl
    }()

    private lazy var lblFileName: UILabel = {
        let lbl = UILabel()
        lbl.font = .systemFont(ofSize: 14, weight: .medium)
        lbl.textColor = .darkGray
        lbl.textAlignment = .center
        lbl.numberOfLines = 2
        lbl.text = "No file selected"
        return lbl
    }()

    private lazy var btnSend: UIButton = {
        let btn = UIButton.createNormalButton(title: "Send")
        btn.isEnabled = false
        btn.rx.tap.subscribe(onNext: { [weak self] in
            self?.send()
        }).disposed(by: disposeBag)
        return btn
    }()

    private lazy var btnStop: UIButton = {
        let btn = UIButton.createNormalButton(title: "Stop")
        btn.isEnabled = false
        btn.rx.tap.subscribe(onNext: { [weak self] in
            self?.stop()
        }).disposed(by: disposeBag)
        return btn
    }()

    private lazy var textConsole: UITextView = {
        let v = UITextView()
        v.font = .systemFont(ofSize: 13)
        v.textColor = .lightGray
        v.textAlignment = .left
        v.isEditable = false
        v.backgroundColor = UIColor(red: 233/255.0, green: 233/255.0, blue: 233/255.0, alpha: 1)
        return v
    }()

    // MARK: - Init

    required init(cmd: TransType) {
        self.cmd = cmd
        self._devInfo = ("","",0,0)
        super.init(nibName: nil, bundle: nil)
    }

    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    // MARK: - Lifecycle

    override func viewDidLoad() {
        super.viewDidLoad()

        title = cmd.title()
        view.backgroundColor = .white

        view.addSubview(btnSelectFile)
        view.addSubview(lblFileHint)
        view.addSubview(lblOtaWarning)
        view.addSubview(lblFileName)
        view.addSubview(btnSend)
        view.addSubview(btnStop)
        view.addSubview(textConsole)

        // OTA scenario shows a prominent warning
        if cmd == .ota {
            lblOtaWarning.isHidden = false
        }

        btnSelectFile.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(250)
            if #available(iOS 11.0, *) {
                make.top.equalTo(view.safeAreaLayoutGuide.snp.top).offset(20)
            } else {
                make.top.equalTo(20)
            }
        }

        lblFileHint.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(btnSelectFile.snp.bottom).offset(8)
        }

        lblOtaWarning.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(lblFileHint.snp.bottom).offset(8)
        }

        lblFileName.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(lblOtaWarning.snp.bottom).offset(8)
        }

        btnSend.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(200)
            make.top.equalTo(lblFileName.snp.bottom).offset(20)
        }

        btnStop.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(200)
            make.top.equalTo(btnSend.snp.bottom).offset(15)
        }

        textConsole.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.right.equalTo(-15)
            make.top.equalTo(btnStop.snp.bottom).offset(15)
            if #available(iOS 11.0, *) {
                make.bottom.equalTo(view.safeAreaLayoutGuide.snp.bottom)
            }else {
                make.bottom.equalTo(0)
            }
        }

        self.rx.observeWeakly(Bool.self, "isTransferring").subscribe(onNext: { [weak self] _ in
            self?.refreshState()
        }).disposed(by: disposeBag)
    }

    override func viewDidDisappear(_ animated: Bool) {
        cancellable?.cancel()
        super.viewDidDisappear(animated)
    }

    // MARK: - File Selection

    private func selectFile() {
        if isTransferring {
            SVProgressHUD.showInfo(withStatus: "Please stop transfer first")
            return
        }

        let picker: UIDocumentPickerViewController
        if #available(iOS 14.0, *) {
            var utTypes: [UTType] = []
            for ext in cmd.allowedExtensions() {
                if let t = UTType(filenameExtension: ext) {
                    utTypes.append(t)
                }
            }
            if utTypes.isEmpty { utTypes = [.data] }
            picker = UIDocumentPickerViewController(forOpeningContentTypes: utTypes, asCopy: true)
        } else {
            // iOS 12/13 fall back to public.data, then validate the extension after selection
            picker = UIDocumentPickerViewController(documentTypes: ["public.data"], in: .import)
        }

        picker.delegate = self
        picker.allowsMultipleSelection = false
        present(picker, animated: true)
    }

    // MARK: - Send / Stop

    private func send() {
        guard let fileURL = selectedFileURL else {
            SVProgressHUD.showInfo(withStatus: "Please select a file first")
            return
        }

        cancellable?.cancel()
        cancellable = nil
        textConsole.text = "📄 \(fileURL.lastPathComponent)\n"

        let path = fileURL.path
        switch cmd {
        case .mp3:
            _mp3(filePath: path, fileName: fileURL.lastPathComponent)
        case .wallpaper:
            _wallpaper(filePath: path)
        case .contact:
            _contact(filePath: path, fileName: fileURL.deletingPathExtension().lastPathComponent)
        case .watchFace:
            break
        case .ota:
            _ota(filePath: path)
        case .app:
            _app(filePath: path, fileName: fileURL.lastPathComponent)
        case .gps:
            _gps(filePath: path, fileName: fileURL.lastPathComponent)
        case .gpsFw:
            _gpsFw(filePath: path)
        }
    }

    private func stop() {
        if (cmd == .ota) {
            SVProgressHUD.showInfo(withStatus: "The upgrade process cannot be canceled")
            return
        }
        cancellable?.cancel()
        isTransferring = false
    }

    // MARK: - Transfer Implementations

    private func _mp3(filePath: String, fileName: String) {
        /*
         !!!: Music transfer notes:
         1. Only mp3 files with a 44.1k sample rate are supported
         2. For the same music (name), delete it from the device first, then create it again
         3. Before transferring music, call the API to create the music first, providing musicID + file size + name
         4. When performing the music transfer, provide musicID + file size + name
         */
        var musicFileSize = 0
        do {
            let attributes = try FileManager.default.attributesOfItem(atPath: filePath)
            if let fileSize = attributes[.size] as? Int64 {
                musicFileSize = Int(fileSize)
            }
        } catch {
            SVProgressHUD.showError(withStatus: "Error getting music size")
            return
        }

        let musicItem = IDOMusicItem(musicID: 1, musicMemory: musicFileSize, musicName: fileName, singerName: "")
        // 1. Delete the music from the device first (if it exists)
        let param = IDOMusicOpearteParamModel(musicOperate: 1, folderOperate: 0, folderItem: nil, musicItem: musicItem)
        Cmds.setMusicOperate(param).send { [weak self] res in
            // 2. Create the music
            let param = IDOMusicOpearteParamModel(musicOperate: 2, folderOperate: 0, folderItem: nil, musicItem: musicItem)
            Cmds.setMusicOperate(param).send { [weak self] res in
                if case .success(let val) = res {
                    if (val?.errCode == 0) {
                        // 3. Perform the transfer
                        self?._trans([
                            IDOTransMusicModel(filePath: filePath, fileName: musicItem.musicName, musicId: musicItem.musicID)
                        ])
                    }else {
                        SVProgressHUD.showError(withStatus: "Error creating song information\ncode:\(String(describing: val?.errCode))")
                    }
                } else if case .failure(let err) = res {
                    SVProgressHUD.showError(withStatus: err.message)
                }
            }
        }
    }

    private func _contact(filePath: String, fileName: String) {
        _trans([
            IDOTransNormalModel(fileType: .ml, filePath: filePath, fileName: fileName)
        ])
    }

    private func _wallpaper(filePath: String) {
        _trans([
            IDOTransNormalModel(fileType: .wallpaperZ, filePath: filePath, fileName: "wallpaper.z")
        ])
    }

    private func _ota(filePath: String) {
        _isSiceOta = false
        switch(sdk.device.deviceId) {
        case 8170, 8155:
            // 98 platform needs to flag OTA mode
            self._isSiceOta = true
            self._devInfo = (sdk.device.macAddressFull,
                             sdk.device.uuid,
                             sdk.device.platform,
                             sdk.device.deviceId)
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: filePath, fileName: "test")
            ])
        case 877:
            // !!!: Ring OTA needs special handling
            _otaRing(filePath: filePath)
        default:
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: filePath, fileName: "test")
            ])
        }
    }

    private func _app(filePath: String, fileName: String) {
        _trans([
            IDOTransNormalModel(fileType: .app, filePath: filePath, fileName: fileName)
        ])
    }

    private func _gps(filePath: String, fileName: String) {
        // !!!: epo transfer must wait for the device to report installation complete; after progress reaches 100% wait about 40 more seconds
        _trans([
            IDOTransNormalModel(fileType: .epo, filePath: filePath, fileName: fileName)
        ])
    }

    private func _gpsFw(filePath: String) {
        // 1. Get version
        Cmds.getGpsInfo().send { res in
            if case .success(let gpsInfoModel) = res {
                print("gpsInfo version:\(gpsInfoModel?.fwVersionString)")
            }
        }
        // 2. Perform the transfer (note: the file extension must be .gps)
        _trans([
            IDOTransNormalModel(fileType: .gps, filePath: filePath, fileName: "gps")
        ])
    }

    private func _otaRing(filePath: String) {
        Task { [weak self] in
            guard let self else { return }

            let macAddress = sdk.device.macAddressFull
            let uuid = sdk.device.uuid
            let platform = sdk.device.platform
            let did = sdk.device.deviceId

            // 1. Disconnect (Full: sdk.ble; Lite: bleMgr / XYCentralManager)
            #if IDOSDK_FULL
            _ = await withCheckedContinuation { continuation in
                sdk.ble.cancelConnect(macAddress: macAddress) { rs in
                    continuation.resume(returning: rs)
                }
            }
            #else
            _ = await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
                guard let id = UUID(uuidString: uuid),
                      let peripheral = XYCBBase.shared.xyCentralManager
                        .retrievePeripherals(withIdentifiers: [id]).first else {
                    continuation.resume()
                    return
                }
                bleMgr.disConnect(peripheral: peripheral) {
                    continuation.resume()
                }
            }
            #endif

            // 2. Flag OTA mode
            _ = await withCheckedContinuation { continuation in
                sdk.bridge.markOtaMode?(macAddress: macAddress, iosUUID: uuid, platform: platform, deviceId: did) { rs in
                    continuation.resume(returning: rs)
                }
            }

            // 3. Transfer OTA
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: filePath, fileName: "test")
            ])
        }
    }

    // MARK: - Core Transfer

    private func _trans<T: IDOTransBaseModel>(_ items: [T]) {
        isTransferring = true
        cancellable = sdk.transfer.transferFiles(fileItems: items, cancelPrevTranTask: true) { [weak self] currentIndex, totalCount, currentProgress, totalProgress in
            let txt = "index: \(currentIndex + 1)/\(totalCount) "
            + String(format: "%.2f% / %.2f%\n", currentProgress * 100, totalProgress * 100)
            self?.textConsole.text = txt
            self?.textConsole.scrollToBottom()
        } transStatus: { [weak self] currentIndex, status, errorCode, _ in
            if status != .finished || errorCode != 0 {
                let txt = (self?.textConsole.text ?? "") + "failure, currentIndex:\(currentIndex) errorCode:\(errorCode)\n"
                self?.textConsole.text = txt
                self?.textConsole.scrollToBottom()
            }
        } completion: { [weak self] rs in
            self?.isTransferring = false
            let txt = (self?.textConsole.text ?? "") + "result: \(rs)\n"
            self?.textConsole.text = txt
            self?.textConsole.scrollToBottom()

            // 98 platform device transfer failed (e.g. Bluetooth turned off midway); add a retry here
            if(self?._isSiceOta ?? false && rs.last == false) {
                self?._retryOtaAlert()
            }
        }
    }

    // MARK: - State

    private func refreshState() {
        btnSelectFile.isEnabled = !isTransferring
        btnSelectFile.alpha = isTransferring ? 0.5 : 1.0
        btnSend.isEnabled = !isTransferring && selectedFileURL != nil
        btnStop.isEnabled = isTransferring
    }

    // MARK: - OTA Retry (SICE)

    private func _retryOtaAlert() {
        let alert = UIAlertController(title: "OTA", message: "Upgrade failed. Please confirm that Bluetooth is enabled and try again.", preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "YES", style: .default, handler: { [weak self] _ in
            guard let self = self else { return }
            self._retrySiceOta()
        }))
        alert.addAction(UIAlertAction(title: "NO", style: .cancel, handler: { [weak self] _ in
            guard let self = self else { return }
            self.navigationController?.popToRootViewController(animated: true)
        }))
        self.present(alert, animated: true, completion: nil)
    }

    // Add a retry operation for some OTA (98 platform devices)
    private func _retrySiceOta() {
        guard let fileURL = selectedFileURL else { return }

        Task { [weak self] in
            guard let self else { return }

            let macAddress = _devInfo.address
            let uuid = _devInfo.uuid
            let platform = _devInfo.platform
            let did = _devInfo.deviceId

            // 1. Flag OTA mode
            _ = await withCheckedContinuation { continuation in
                sdk.bridge.markOtaMode?(macAddress: macAddress, iosUUID: uuid, platform: platform, deviceId: did) { rs in
                    continuation.resume(returning: rs)
                }
            }

            // 2. Perform the OTA upgrade again
            _ota(filePath: fileURL.path)
        }
    }
}

// MARK: - UIDocumentPickerDelegate

extension TransferFileDetailVC: UIDocumentPickerDelegate {
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let url = urls.first else { return }

        // Validate file extension
        let ext = url.pathExtension.lowercased()
        let allowedExts = cmd.allowedExtensions().map { $0.lowercased() }

        if !allowedExts.isEmpty && !allowedExts.contains(ext) {
            let supported = allowedExts.map { ".\($0)" }.joined(separator: ", ")
            SVProgressHUD.showError(withStatus:
                "Unsupported: .\(ext)\n\nSupported: \(supported)")
            return
        }

        selectedFileURL = url
        lblFileName.text = "✅ \(url.lastPathComponent)"
        lblFileName.textColor = UIColor(red: 34/255.0, green: 139/255.0, blue: 34/255.0, alpha: 1)
        btnSend.isEnabled = !isTransferring
        textConsole.text = ""
    }

    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        // User cancelled
    }
}
