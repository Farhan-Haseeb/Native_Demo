//
//  WatchFaceVC.swift
//  Runner
//
//  Created by hc on 2024/6/5.
//

import Foundation
import UIKit
import SwiftUI
import UniformTypeIdentifiers

import RxCocoa
import RxSwift
import SnapKit
import SVProgressHUD

import protocol_channel

/*
 Custom watch face:
 custom watch face setup steps:
 private var currentWatchFaceListModel: IDOWatchListModel?
 let watchName = "custom1" // Must match iwf.json's name value in the watch face package

 1. Get the watch face list:
     Cmds.getWatchListV3().send { [weak self] res in
         if case .success(let val) = res {
             self?.currentWatchFaceListModel = val
         }else if case .failure(_) = res {
             // Failed to obtain the watch face list
         }
     }

 2. Check whether the watch face to upload already exists in the watch face list; if it does, delete it first
     if (currentWatchFaceListModel?.items.contains { $0.name.hasPrefix(watchName) }) {
         let param = IDOWatchFaceParamModel(operate: 2, fileName: watchName, watchFileSize: 0)
         Cmds.setWatchFaceData(param).send { [weak self] res in
             if case .success(let val) = res {
                 if (val == nil || val!.errCode == 0) {
                    // successful
                    // Uninstall watch face successful
                 }
             }else if case .failure(let err) = res {
                 // Failed to obtain the watch face list err
             }
         }
     }
     

 3. Upload custom watch face
     let fileItem = IDOTransNormalModel(fileType: .iwfLz, filePath:"/xx/xx/**/watch.zip", fileName: watchName)
     sdk.transfer.transferFiles(fileItems: [fileItem], cancelPrevTranTask: true) { currentIndex, totalCount, currentProgress, totalProgress in
         SVProgressHUD.showProgress(totalProgress);
     } transStatus: { currentIndex, status, errorCode, finishingTime in
         // print("status: (status) errorCode: (errorCode)")
     } completion: { [weak self] rs in
         if (rs.first ?? false) {
             // success
         }else {
             // fail
         }
     }
 */

class WatchFaceVC: UIViewController {
    private lazy var bundlePath = Bundle.main.bundlePath + "/files_trans"
    private var testWatchModel: IDOTransNormalModel?
    private var isInstalled = false
    private let disposeBag = DisposeBag()
    private var currentWatchFaceListModel: IDOWatchListModel?
    
    private lazy var allowedExtensions: [String] = {
        // Configure the watch face file extension restrictions per platform
        switch sdk.device.platform {
        case 98, 99: return ["watch"]
        default:
            return ["watch", "zip", "iwf"]
        }
    }()
    
    // MARK: - UI Elements
    
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
        let extsString = allowedExtensions.map { ".\($0)" }.joined(separator: ", ")
        lbl.text = "Supported: \(extsString)"
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
    
    private lazy var btnGetWatchFaceList: UIButton = {
        let v = UIButton.createNormalButton(title: "Get watch list")
        v.isEnabled = true
        v.rx.tap.subscribe(onNext: { [weak self] in
            self?._getWatchList()
        }).disposed(by: disposeBag)
        return v
    }()
    
    private lazy var btnAction: UIButton = {
        let v = UIButton.createNormalButton(title: "Install watch face")
        v.isEnabled = true
        v.isHidden = true
        v.rx.tap.subscribe(onNext: { [weak self] in
            self?._installOrUninstallWatchFace()
        }).disposed(by: disposeBag)
        return v
    }()
    
    private lazy var lblTips: UITextView = {
        let v = UITextView()
        v.font = .systemFont(ofSize: 12)
        v.textColor = .gray
        v.isEditable = false
        v.text = """
Steps to add a watch face:
1. Download the corresponding watch face file from the cloud to the local
2. Get the watch face list of the current device
3. Determine whether the device watch face data and space are enough
4. Transfer the watch face to the device
5. If it is not displayed after installation, you need to call setWatchFaceData to set it as the first watch face (optional)
"""
        return v
    }()
    
    private var customDialZipPath: String?
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Watch face upgrade"
        view.backgroundColor = .white
        
        view.addSubview(btnSelectFile)
        view.addSubview(lblFileHint)
        view.addSubview(lblFileName)
        view.addSubview(btnGetWatchFaceList)
        view.addSubview(btnAction)
        view.addSubview(lblTips)
        
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
        
        lblFileName.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(lblFileHint.snp.bottom).offset(8)
        }
        
        btnGetWatchFaceList.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(250)
            make.top.equalTo(lblFileName.snp.bottom).offset(30)
        }
        
        btnAction.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(250)
            make.top.equalTo(btnGetWatchFaceList.snp.bottom).offset(20)
        }
        
        lblTips.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(btnAction.snp.bottom).offset(20)
            if #available(iOS 11.0, *) {
                make.bottom.lessThanOrEqualTo(view.safeAreaLayoutGuide.snp.bottom).offset(-20)
            } else {
                make.bottom.lessThanOrEqualTo(-20)
            }
        }
        
        if [7892, 512].contains(sdk.device.deviceId) {
            let btnCustomDial = UIBarButtonItem(
                title: "Custom",
                style: .plain,
                target: self,
                action: #selector(_customDial)
            )
            navigationItem.rightBarButtonItem = btnCustomDial
        }
    }
}

// MARK: - File Selection
extension WatchFaceVC {
    private func selectFile() {
        let picker: UIDocumentPickerViewController
        if #available(iOS 14.0, *) {
            var utTypes: [UTType] = []
            for ext in allowedExtensions {
                if let t = UTType(filenameExtension: ext) {
                    utTypes.append(t)
                }
            }
            if utTypes.isEmpty { utTypes = [.data] }
            picker = UIDocumentPickerViewController(forOpeningContentTypes: utTypes, asCopy: true)
        } else {
            picker = UIDocumentPickerViewController(documentTypes: ["public.data"], in: .import)
        }
        
        picker.delegate = self
        picker.allowsMultipleSelection = false
        present(picker, animated: true)
    }
}

// MARK: - UIDocumentPickerDelegate
extension WatchFaceVC: UIDocumentPickerDelegate {
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let url = urls.first else { return }
        
        // Validate file extension
        let ext = url.pathExtension.lowercased()
        let allowedExts = allowedExtensions.map { $0.lowercased() }
        
        if !allowedExts.contains(ext) {
            let supported = allowedExts.map { ".\($0)" }.joined(separator: ", ")
            SVProgressHUD.showError(withStatus: "Unsupported: .\(ext)\n\nSupported: \(supported)")
            return
        }
        
        // Build the transfer model
        let fileType: IDOTransType = (ext == "zip" || ext == "iwf") ? .iwfLz : .watch
        var fileName = url.lastPathComponent
        if fileType == .iwfLz {
            // .zip / .iwf formats usually drop the extension or use a specific name for fileName during transfer
            fileName = url.deletingPathExtension().lastPathComponent
        }
        
        testWatchModel = IDOTransNormalModel(fileType: fileType, filePath: url.path, fileName: fileName)
        
        lblFileName.text = "✅ \(url.lastPathComponent)"
        lblFileName.textColor = UIColor(red: 34/255.0, green: 139/255.0, blue: 34/255.0, alpha: 1)
        
        // Reset state
        isInstalled = false
        btnAction.isHidden = true
        
        // After selection, automatically try to get the watch face list
        _getWatchList()
    }
    
    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        // ...
    }
}


// MARK: - Operations
extension WatchFaceVC {
    
    private func _refreshState() {
        guard let testWatchModel = testWatchModel else { return }
        guard let isInstalled = currentWatchFaceListModel?.isInstalled(watchName: testWatchModel.fileName) else {
            btnAction.isEnabled = false
            self.isInstalled = false
            return
        }
        self.isInstalled = isInstalled
        btnAction.setTitle(isInstalled ? "Uninstall" : "Install", for: .normal)
        btnAction.isEnabled = true
    }
    
    private func _getWatchList() {
        guard testWatchModel != nil else {
            SVProgressHUD.showInfo(withStatus: "Please select watchface file first")
            return
        }
        
        SVProgressHUD.show()
        Cmds.getWatchListV3().send { [weak self] res in
            if case .success(let val) = res {
                self?.currentWatchFaceListModel = val
                self?.btnAction.isHidden = false
                SVProgressHUD.dismiss()
                self?._refreshState()
            }else if case .failure(_) = res {
                SVProgressHUD.showError(withStatus: "Failed to obtain the watch face list")
            }
        }
    }
    
    private func _installOrUninstallWatchFace() {
        guard let watchModel = testWatchModel else { return }
        
        if (isInstalled) {
            // uninstall
            SVProgressHUD.show()
            let param = IDOWatchFaceParamModel(operate: 2, fileName: watchModel.fileName, watchFileSize: watchModel.fileSize)
            Cmds.setWatchFaceData(param).send { [weak self] res in
                if case .success(let val) = res {
                    if (val == nil || val!.errCode == 0) {
                        // successful
                        SVProgressHUD.showSuccess(withStatus: "Uninstall watch face successful")
                        self?._getWatchList() // Get the watch face list again
                    }else {
                        SVProgressHUD.showError(withStatus: "Uninstall failed")
                    }
                }else if case .failure(let err) = res {
                    SVProgressHUD.showError(withStatus: "Failed to obtain the watch face list err code: \(err)")
                }
            }
        }else {
            // install
            
            SVProgressHUD.showProgress(0.0);
            //1. Transfer the watch face file to the device
            sdk.transfer.transferFiles(fileItems: [watchModel], cancelPrevTranTask: true) { currentIndex, totalCount, currentProgress, totalProgress in
                SVProgressHUD.showProgress(totalProgress);
            } transStatus: { currentIndex, status, errorCode, finishingTime in
                print("status: \(status) errorCode: \(errorCode)")
            } completion: { [weak self] rs in
                
                if (rs.first ?? false) {
                    // Transfer done
                    // The device needs time to install the watch face, so wait 2 seconds here before refreshing the watch face list
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        
//                        // 2. Set it to take effect (optional)
//                        let param = IDOWatchFaceParamModel(operate: 1, fileName: self?.testWatchModel?.fileName ?? "", watchFileSize: 0)
//                        Cmds.setWatchFaceData(param).send { res in
//                            if case .success(let val) = res {
//                                if (val == nil || val!.errCode == 0) {
//                                    // successful
//                                    
//                                }
//                            }else if case .failure(let err) = res {
//                                SVProgressHUD.showError(withStatus: "Failed to obtain the watch face list")
//                            }
//                        }
                        
                        // Get the watch face list again
                        self?._getWatchList()
                    }
                }else{
                    SVProgressHUD.dismiss()
                }
            }
        }
    }
    
    @objc private func _customDial() {
        SVProgressHUD.show()
        // Get device screen information
        Cmds.getWatchDialInfo().send { [weak self] res in
            //sdk.device.deviceShapeType
            if case .success(let o) = res, o != nil {
                self?.gotoCustomDialVC(o!)
            }else {
                SVProgressHUD.showError(withStatus: "Failed to obtain device screen information, please try again")
            }
        }
    }
    
    private func gotoCustomDialVC(_ watchDialInfo: IDOWatchDialInfoModel) {
        
        // Device screen information
        let sizeDial = CGSizeMake(CGFloat(watchDialInfo.width), CGFloat(watchDialInfo.height))
        /// Watch shape type: 1: round; 2: square; 3: oval
        let shapeType = sdk.device.deviceShapeType
        let dialCustomDialInfo = DialCustomDialInfo(sizeDial: sizeDial, shapeType: shapeType)
        let customDialView = DialCustomizationView(customDialInfo: dialCustomDialInfo) { [weak self] zipPath in
            guard let zipPath = zipPath else { return }
            print("customDialZipPath:\(zipPath)")
            self?.customDialZipPath = zipPath
            self?.testWatchModel = IDOTransNormalModel(fileType: .iwfLz, filePath: zipPath, fileName: "custom1")
            
            // Update UI state
            self?.lblFileName.text = "✅ custom dial"
            self?.lblFileName.textColor = UIColor(red: 34/255.0, green: 139/255.0, blue: 34/255.0, alpha: 1)
            self?.isInstalled = false
            self?.btnAction.isHidden = true
            self?._getWatchList()
        }
        let hostingController = UIHostingController(rootView: customDialView)
        self.navigationController?.pushViewController(hostingController, animated: true)
        SVProgressHUD.dismiss()
    }
    
}

extension IDOWatchListModel {
    
    /// Whether it is installed
    func isInstalled(watchName: String) -> Bool {
        let name = watchName.replacingOccurrences(of: ".watch", with: "")
        return items.contains {
            print("isInstalled:\($0.name) name:\(name) \($0.name.hasPrefix(name))")
            return $0.name.hasPrefix(name)
        }
    }
    
    /// Whether the device watch face storage is full
    func isCapacityFull(willInstallBytes: Int) -> Bool {
        var showFullAlertFalse = false
        let invalidCloudCount = cloudWatchNum <= userCloudWatchNum
        
        if (!sdk.funcTable.setWatchCapacitySizeDisplay) {
            showFullAlertFalse = invalidCloudCount
        }else {
            let invalidCapacity = (userWatchCapacitySize + willInstallBytes) > watchCapacitySize;
            showFullAlertFalse = invalidCloudCount || invalidCapacity;
        }
        return !showFullAlertFalse
    }
    
}
