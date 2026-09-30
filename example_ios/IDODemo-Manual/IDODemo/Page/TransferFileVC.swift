//
//  TransferFileVC.swift
//  IDODemo
//
//  Created by hc on 2023/10/31.
//

import Foundation
import UIKit

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
//        if sdk.device.deviceId != 7877 {
//            // 7877 does not support wallpaper watch faces yet
//            list.append(.wallpaper)
//        }
        
        if (sdk.device.deviceId == 859 || sdk.device.deviceId == 7884) {
            list.append(.app)
        }
        
        if (sdk.device.deviceId == 7883 || sdk.device.deviceId == 7882) {
            list.append(.gps)
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
        }
    }
}

// MARK: - TransferFileDetailVC

class TransferFileDetailVC: UIViewController {
    private var cmd: TransType
    private let disposeBag = DisposeBag()
    private var cancellable: IDOCancellable?
    private lazy var bundlePath = Bundle.main.bundlePath + "/files_trans"
    @objc dynamic private var isTransferring = false
    
    private lazy var btnSend: UIButton = {
        let btn = UIButton.createNormalButton(title: "Send")
        btn.isEnabled = true
        btn.rx.tap.subscribe(onNext: { [weak self] in
            self?.send()
        }).disposed(by: disposeBag)
        return btn
    }()
    
    private lazy var btnStop: UIButton = {
        let btn = UIButton.createNormalButton(title: "Stop")
        btn.isEnabled = true
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
    
    required init(cmd: TransType) {
        self.cmd = cmd
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = cmd.title()
        view.backgroundColor = .white
        
        view.addSubview(btnSend)
        view.addSubview(btnStop)
        view.addSubview(textConsole)
        
        btnSend.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(200)
            if #available(iOS 11.0, *) {
                make.top.equalTo(view.safeAreaLayoutGuide.snp.top).offset(35)
            } else {
                make.top.equalTo(35)
            }
        }
        
        btnStop.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(200)
            make.top.equalTo(btnSend.snp.bottom).offset(25)
        }
        
        textConsole.snp.makeConstraints { make in
            make.left.equalTo(15)
            make.right.equalTo(-15)
            make.top.equalTo(btnStop.snp.bottom).offset(25)
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
    
    private func send() {
        cancellable?.cancel()
        cancellable = nil
        textConsole.text = ""
        switch cmd {
        case .mp3:
            _mp3()
            break
        case .wallpaper:
            _wallpaper()
            break
        case .contact:
            _contact()
            break
        case .watchFace:
            break
        case .ota:
            _ota()
            break;
        case .app:
            _app()
        case .gps:
            _gps();
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
    
    private func _mp3() {
        let mp3Path = bundlePath + "/mp3"
        /*
         !!!: Music transfer notes:
         1. Only mp3 files with a 44.1k sample rate are supported
         2. For the same music (name), delete it from the device first, then create it again
         3. Before transferring music, call the API to create the music first, providing musicID + file size + name
         4. When performing the music transfer, provide musicID + file size + name
         */
        
        // Get music file size
        let musicFilePath = "\(mp3Path)/3.mp3"
        var musicFileSize = 0
        do {
            let attributes = try FileManager.default.attributesOfItem(atPath: musicFilePath)
            if let fileSize = attributes[.size] as? Int64 {
                musicFileSize = Int(fileSize)
            }
        } catch {
            SVProgressHUD.showError(withStatus: "Error getting music size")
            return
        }
        
        let musicItem = IDOMusicItem(musicID: 1, musicMemory: musicFileSize, musicName: "m1.mp3", singerName: "singer1")
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
                            IDOTransMusicModel(filePath: musicFilePath, fileName: musicItem.musicName, musicId: musicItem.musicID)
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
    
    private func _contact() {
        let aPath = bundlePath + "/contact/a.json"
        _trans([
            IDOTransNormalModel(fileType: .ml, filePath: aPath, fileName: "a")
        ])
    }
    
    private func _wallpaper() {
        let picFilePath = bundlePath + "/imgs/c.png"
        _trans([
            IDOTransNormalModel(fileType: .wallpaperZ, filePath: picFilePath, fileName: "wallpaper.z")
        ])
    }
    
    private func _ota() {
        // !!!: Watch face files and configs differ between device platforms and cannot be used interchangeably
        switch(sdk.device.deviceId) {
        case 7877:
            /*
             ota_7877_V01.01.00.zip
             ota_7877_full_V01.01.00_all.zip
             ota_7877_V01.01.01.zip
             */
            let aPath = bundlePath + "/ota/7877/ota_7877_V01.01.01.zip"
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: aPath, fileName: "test")
            ])
        case 7814:
            // ota_7814_V1.00.07.bin
            // ota_full_7814_V1.01.05_0716.bin
            let aPath = bundlePath + "/ota/7814/ota_full_7814_V1.01.05_0716.bin"
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: aPath, fileName: "test")
            ])
        case 537:
            let aPath = bundlePath + "/ota/537/ota_full_537_V1.01.03.bin"
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: aPath, fileName: "test")
            ])
        case 517:
            let aPath = bundlePath + "/ota/517/ota_full_517_V1.01.01.bin"
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: aPath, fileName: "test")
            ])
        case 543:
            /*
             ota_543_v01.61.89.zip
             ota_543_v01.61.99.zip
             */
            let aPath = bundlePath + "/ota/543/ota_543_v01.61.89.zip"
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: aPath, fileName: "test")
            ])
        case 7902:
            let aPath = bundlePath + "/ota/7902/gtx13_ota_packet_v01.61.89_all.zip"
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: aPath, fileName: "test")
            ])
        case 7883:
            let aPath = bundlePath + "/ota/7883/ota_full_7883-V1.0.0.bin"
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: aPath, fileName: "test")
            ])
        case 850:
            let aPath = bundlePath + "/ota/850/205GPRO_ota_1.01.01_2024.06.27_16-56.zip"
            _trans([
                IDOTransNormalModel(fileType: .fw, filePath: aPath, fileName: "test")
            ])
        default:
            SVProgressHUD.dismiss()
            let alert = UIAlertController(title: "Tips", message: "Device '\(sdk.device.deviceId)' related files not found, please configure in the demo code and try again", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { [weak self] _ in
                guard let self = self else { return }
                self.navigationController?.popToRootViewController(animated: true)
            }))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    private func _app() {
        switch(sdk.device.deviceId) {
        case 859, 7884:
            let aPath = bundlePath + "/app/dyn_plane.app"
            _trans([
                IDOTransNormalModel(fileType: .app, filePath: aPath, fileName: "app0.app")
            ])
        default:
            SVProgressHUD.dismiss()
            let alert = UIAlertController(title: "Tips", message: "Device '\(sdk.device.deviceId)' related files not found, please configure in the demo code and try again", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { [weak self] _ in
                guard let self = self else { return }
                self.navigationController?.popToRootViewController(animated: true)
            }))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    
    private func _gps() {
        // !!!:  epo transfer must wait for the device to report that installation is complete, so after the progress reaches 100% there is a pause (about 40 seconds)
        switch(sdk.device.deviceId) {
        case 7883, 7882:
            let aPath = bundlePath + "/gps/7883/EPO.DAT"
            _trans([
                IDOTransNormalModel(fileType: .epo, filePath: aPath, fileName: "EPO.DAT")
            ])
        default:
            SVProgressHUD.dismiss()
            let alert = UIAlertController(title: "Tips", message: "Device '\(sdk.device.deviceId)' related files not found, please configure in the demo code and try again", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { [weak self] _ in
                guard let self = self else { return }
                self.navigationController?.popToRootViewController(animated: true)
            }))
            self.present(alert, animated: true, completion: nil)
        }
    }
    
    
    private func _trans<T: IDOTransBaseModel>(_ items: [T]) {
        isTransferring = true
        let startTime = CFAbsoluteTimeGetCurrent()
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
        }
    }
    
    private func refreshState() {
        self.btnSend.isEnabled = !self.isTransferring
        self.btnStop.isEnabled = self.isTransferring
    }
}
