//
//  WatchFaceVC.swift
//  Runner
//
//  Created by hc on 2024/6/5.
//

import Foundation
import UIKit

import RxCocoa
import RxSwift
import SnapKit
import SVProgressHUD

import protocol_channel


class WatchFaceVC: UIViewController {
    private lazy var bundlePath = Bundle.main.bundlePath + "/files_trans"
    private var testWatchModel: IDOTransNormalModel?
    private var isInstalled = false
    private let disposeBag = DisposeBag()
    private var currentWatchFaceListModel: IDOWatchListModel?
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
3. Determine whether the device watch face data and space are
4. Transfer the watch face to the device
5. If it is not displayed after installation, you need to call setWatchFaceData to set it as the first watch face (optional)
Note: Demo only processes 2, 3, and 4, and the watch face file is built into the project
"""
        return v
    }()
    
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        title = "Watch face upgrade"
        view.backgroundColor = .white
        
        view.addSubview(btnGetWatchFaceList)
        view.addSubview(btnAction)
        view.addSubview(lblTips)
        
        btnGetWatchFaceList.snp.makeConstraints { make in
            make.height.equalTo(44)
            make.centerX.equalTo(view)
            make.width.equalTo(200)
            if #available(iOS 11.0, *) {
                make.top.equalTo(view.safeAreaLayoutGuide.snp.top).offset(35)
            } else {
                make.top.equalTo(35)
            }
        }
        
        btnAction.snp.makeConstraints { make in
            make.size.equalTo(btnGetWatchFaceList)
            make.centerX.equalTo(btnGetWatchFaceList)
            make.top.equalTo(btnGetWatchFaceList.snp.bottom).offset(25)
        }
        
        lblTips.snp.makeConstraints { make in
            make.left.equalTo(20)
            make.right.equalTo(-20)
            make.top.equalTo(btnAction.snp.bottom).offset(20)
            if #available(iOS 11.0, *) {
                make.bottom.equalTo(view.safeAreaLayoutGuide.snp.bottom).offset(-35)
            } else {
                make.bottom.equalTo(-35)
            }
        }
        
        if(_initWatchTransModel()) {
            _getWatchList()
        }
    }
}

extension WatchFaceVC {
    
    private func _initWatchTransModel() -> Bool {
        // !!!: Watch face files and configs differ between device platforms and cannot be used interchangeably
        switch(sdk.device.deviceId) {
        case 7877:
            testWatchModel = IDOTransNormalModel(fileType: .watch, filePath: bundlePath + "/watch_face/7877/wf_w58.watch", fileName: "wf_w58.watch")
        case 7814:
            testWatchModel = IDOTransNormalModel(fileType: .iwfLz, filePath: bundlePath + "/watch_face/7814/w58.zip", fileName: "w58")
        case 537:
            testWatchModel = IDOTransNormalModel(fileType: .iwfLz, filePath: bundlePath + "/watch_face/537/w130.zip", fileName: "w130")
        case 517:
            testWatchModel = IDOTransNormalModel(fileType: .iwfLz, filePath: bundlePath + "/watch_face/517/w130.zip", fileName: "w130")
        case 543:
            testWatchModel = IDOTransNormalModel(fileType: .watch, filePath: bundlePath + "/watch_face/543/wf_w58.watch", fileName: "wf_w58.watch")
        case 7883:
            testWatchModel = IDOTransNormalModel(fileType: .iwfLz, filePath: bundlePath + "/watch_face/7883/GTX02_1.zip", fileName: "GTX02_1")
        default:
            SVProgressHUD.dismiss()
            let alert = UIAlertController(title: "Tips", message: "Device '\(sdk.device.deviceId)' related files not found, please configure in the demo code and try again", preferredStyle: .alert)
            alert.addAction(UIAlertAction(title: "OK", style: .cancel, handler: { [weak self] _ in
                guard let self = self else { return }
                self.navigationController?.popToRootViewController(animated: true)
            }))
            self.present(alert, animated: true, completion: nil)
            return false
        }
        return true
    }
    
    private func _refreshState() {
        guard let isInstalled = currentWatchFaceListModel?.isInstalled(watchName: testWatchModel!.fileName) else {
            btnAction.isEnabled = false
            self.isInstalled = false
            return
        }
        self.isInstalled = isInstalled
        btnAction.setTitle(isInstalled ? "Uninstall" : "Install", for: .normal)
        btnAction.isEnabled = true
        
    }
    
    private func _getWatchList() {
        
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
        if (isInstalled) {
            // uninstall
            SVProgressHUD.show()
            let param = IDOWatchFaceParamModel(operate: 2, fileName: testWatchModel!.fileName, watchFileSize: testWatchModel!.fileSize)
            Cmds.setWatchFaceData(param).send { [weak self] res in
                if case .success(let val) = res {
                    if (val == nil || val!.errCode == 0) {
                        // successful
                        SVProgressHUD.showSuccess(withStatus: "Uninstall watch face successful")
                        self?._getWatchList() // Get the watch face list again
                    }
                }else if case .failure(let err) = res {
                    SVProgressHUD.showError(withStatus: "Failed to obtain the watch face list err code: \(err)")
                }
            }
        }else {
            // install
            
            SVProgressHUD.showProgress(0.0);
            //1. Transfer the watch face file to the device
            sdk.transfer.transferFiles(fileItems: [testWatchModel!], cancelPrevTranTask: true) { currentIndex, totalCount, currentProgress, totalProgress in
                SVProgressHUD.showProgress(totalProgress);
            } transStatus: { currentIndex, status, errorCode, finishingTime in
                print("status: \(status) errorCode: \(errorCode)")
            } completion: { [weak self] rs in
                
                if (rs.first ?? false) {
                    // Transfer done
                    // The device needs time to install the watch face, so wait 2 seconds here before refreshing the watch face list
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                        self?._getWatchList() // Get the watch face list again
                    }
                }else{
                    SVProgressHUD.dismiss()
                }
            }

            
//            //2. Set it to take effect (optional)
//            let param = IDOWatchFaceParamModel(operate: 1, fileName: testWatchModel.fileName, watchFileSize: 0)
//            Cmds.setWatchFaceData(param).send { res in
//                if case .success(let val) = res {
//                    if (val == nil || val!.errCode == 0) {
//                        // successful
//                        
//                    }
//                }else if case .failure(let err) = res {
//                    SVProgressHUD.showError(withStatus: "Failed to obtain the watch face list")
//                }
//            }
        }
    }
    
}

extension IDOWatchListModel {
    
    /// Whether it is installed
    func isInstalled(watchName: String) -> Bool {
        return items.contains { $0.name == watchName.replacingOccurrences(of: ".watch", with: "") }
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

