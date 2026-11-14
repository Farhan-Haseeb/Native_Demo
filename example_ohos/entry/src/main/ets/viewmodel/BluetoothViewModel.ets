// State management class
import {
  IDOBleDelegate,
  IDOBleDeviceModel,
  IDOBluetoothStateModel,
  IDODeviceNotificationModel,
  IDODeviceStateModel,
  IDOOtaDeviceModel,
  IDOReceiveData,
  IDOStatusNotification
} from 'protocol_channel';
import { abilityAccessCtrl, bundleManager, Permissions } from '@kit.AbilityKit';
import { BusinessError } from '@kit.BasicServicesKit';
import { bluetooth } from '@kit.ConnectivityKit';
import { sdk } from 'protocol_channel';
import BleManager, { AppDelegate } from '../bluetooth/BleManager';
import { Tools } from '../utils/Tools';

export class BluetoothViewModel {
  private static instance: BluetoothViewModel;
  // Use @State to manage app state
  bluetoothState: BluetoothState = new BluetoothState();
  // Bluetooth service instance
  private bluetoothService: BluetoothService = BluetoothService.getInstance(this.bluetoothState);

  private constructor() {
  }

  static getInstance(): BluetoothViewModel {
    if (!BluetoothViewModel.instance) {
      BluetoothViewModel.instance = new BluetoothViewModel();
    }
    return BluetoothViewModel.instance;
  }

  // Initialize the app
  async init() {
    // Check permissions
    await this.bluetoothService.checkBluetoothPermission();

    // If permission is granted, check the Bluetooth state
    if (this.bluetoothState.hasPermission) {
      await this.bluetoothService.checkBluetoothState();
      this.bluetoothState.updateStatus('Ready');
    } else {
      this.bluetoothState.updateStatus('Bluetooth permission required');
    }
    this.refreshBindDeviceList();
  }

  refreshBindDeviceList() {
    let devices = Tools.getInstance().getBindDevices();
    console.info("Bound device list: " + devices.length)
    devices?.forEach((d) => {
      console.info("device = " + d.macAddress);
    })
    this.bluetoothState.updateBindDeviceList(devices);
  }

  // Request permission
  async requestPermission(context: Context) {
    await this.bluetoothService.requestBluetoothPermission(context);
    if (this.bluetoothState.hasPermission) {
      await this.bluetoothService.checkBluetoothState();
      this.bluetoothState.updateStatus('Ready');
    }
  }

  // Start scan
  async startScan(context: Context) {
    await this.bluetoothService.startScan(context);
  }

  // Stop scan
  async stopScan() {
    await this.bluetoothService.stopScan();
  }

  // Get the device name
  getDeviceName(device: IDOBleDeviceModel): string {
    return device.name || `Unknown device (${device.macAddress})`;
  }

  // Clear the error message
  clearError() {
    this.bluetoothState.errorMessage = '';
  }
}

@ObservedV2
export class BluetoothState {
  // Permission state
  @Trace hasPermission: boolean = false;
  // Bluetooth state
  @Trace bluetoothEnabled: boolean = false;
  // Scan state
  @Trace isScanning: boolean = false;
  // Device list
  @Trace deviceList: IDOBleDeviceModel[] = [];
  // Status message
  @Trace statusMessage: string = 'Checking permission...';
  // Error message
  @Trace errorMessage: string = '';
  //ota devices
  @Trace otaDeviceList: IDOBleDeviceModel[] = [];
  //app bound device list
  @Trace bindDeviceList: IDOBleDeviceModel[] = [];

  // Update the device list
  updateDeviceList(devices: IDOBleDeviceModel[]) {
    this.deviceList = [...devices];
  }

  //Bound device list
  updateBindDeviceList(devices: IDOBleDeviceModel[]) {
    this.bindDeviceList = [...devices];
  }

  updateOtaDeviceList(devices: IDOBleDeviceModel[]) {
    this.otaDeviceList = devices;
  }

  // Update the status message
  updateStatus(message: string) {
    this.statusMessage = message;
  }

  // Set the error message
  setError(message: string) {
    this.errorMessage = message;
  }
}

// Bluetooth service class
class BluetoothService {
  private static instance: BluetoothService;
  private state: BluetoothState;
  private atManager: abilityAccessCtrl.AtManager;
  private bluetoothPermission: Permissions = 'ohos.permission.USE_BLUETOOTH';
  private isScanning: boolean = false;
  private scanDevices: Map<string, IDOBleDeviceModel> = new Map();
  private otaDevices: IDOBleDeviceModel[] = [];

  private constructor(state: BluetoothState) {
    this.state = state;
    this.atManager = abilityAccessCtrl.createAtManager();
  }

  public static getInstance(state: BluetoothState): BluetoothService {
    if (!BluetoothService.instance) {
      BluetoothService.instance = new BluetoothService(state);
    }
    return BluetoothService.instance;
  }

  // Check Bluetooth permission
  async checkBluetoothPermission(): Promise<boolean> {
    try {
      let bundleInfo: bundleManager.BundleInfo =
        await bundleManager.getBundleInfoForSelf(bundleManager.BundleFlag.GET_BUNDLE_INFO_WITH_APPLICATION);
      let appInfo: bundleManager.ApplicationInfo = bundleInfo.appInfo;
      let tokenID: number = appInfo.accessTokenId;
      const status = await this.atManager.checkAccessToken(tokenID, this.bluetoothPermission);
      this.state.hasPermission = status === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED;
      return this.state.hasPermission;
    } catch (error) {
      this.state.setError(`Permission check failed: ${(error as BusinessError).message}`);
      return false;
    }
  }

  // Request Bluetooth permission
  async requestBluetoothPermission(context: Context): Promise<boolean> {
    try {
      this.state.updateStatus('Requesting permission...');
      const result = await this.atManager.requestPermissionsFromUser(
        context,
        [this.bluetoothPermission]
      );
      this.state.hasPermission = result.authResults[0] === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED;
      return this.state.hasPermission;
    } catch (error) {
      this.state.setError(`Permission request failed: ${(error as BusinessError).message}`);
      return false;
    }
  }

  // Check Bluetooth state
  async checkBluetoothState(): Promise<boolean> {
    try {
      const state = await bluetooth.getState();
      this.state.bluetoothEnabled = state === bluetooth.BluetoothState.STATE_ON;
      return this.state.bluetoothEnabled;
    } catch (error) {
      this.state.setError(`Bluetooth state check failed: ${(error as BusinessError).message}`);
      return false;
    }
  }

  // Internal delegate implementation
  private bleDelegateImp: AppDelegate = {
    scanResult: (list: IDOBleDeviceModel[] | null): void => this.onScanResult(list),
    listenDeviceNotification:(model: IDODeviceNotificationModel):void=>this.onReceiveDeviceNotificationEvent(model)
  };

  onReceiveDeviceNotificationEvent(model: IDODeviceNotificationModel) {
    console.info("Received watch event notification:" + model.dataType);
  }

  onScanResult(devices: IDOBleDeviceModel[] | null) {
    console.info("Scan result: " + devices)
    if (devices == null || devices.length == 0) {
      return;
    }
    for (let device of devices) {
      const mac = device.macAddress ?? '';
      if (mac.length === 0) {
        continue;
      }
      // Bound device: refresh the snapshot, do not add it to the add-device scan list (aligned with iOS ScanPageVC.applyScanResult)
      if (Tools.getInstance().getBindState(mac)) {
        Tools.getInstance().setBindDevice(device);
        this.state.updateBindDeviceList(Tools.getInstance().getBindDevices());
        continue;
      }
      if (device.isOta) {
        console.info("Device is in OTA: " + device.macAddress)
      }
      if (!this.scanDevices.has(mac)) {
        this.scanDevices.set(mac, device);
        let arr = Array.from(this.scanDevices.values()).sort((a, b) => {
          return Math.abs(a.rssi) - Math.abs(b.rssi)
        });
        if (device.isOta) {
          this.otaDevices.push(device);
          this.state.updateOtaDeviceList(this.otaDevices);
        } else {
          this.state.updateDeviceList(arr);
        }
      }
    }
  }

  // Start scanning for devices
  async startScan(context: Context): Promise<void> {
    if (this.isScanning) {
      return;
    }

    try {
      // Make sure we have permission
      if (!this.state.hasPermission) {
        const hasPermission = await this.requestBluetoothPermission(context);
        if (!hasPermission) {
          this.state.updateStatus('Bluetooth permission is required to scan');
          return;
        }
      }

      // Make sure Bluetooth is on
      if (!this.state.bluetoothEnabled) {
        const enabled = await this.checkBluetoothState();
        if (!enabled) {
          this.state.updateStatus('Please turn on Bluetooth first');
          return;
        }
      }

      // Reset the device list
      this.scanDevices.clear();
      this.otaDevices = [];
      this.state.updateDeviceList([]);
      this.state.updateOtaDeviceList([]);
      this.isScanning = true;
      this.state.isScanning = true;
      this.state.updateStatus('Scanning for devices...');
      BleManager.unregisterBleDelegate(this.bleDelegateImp);
      BleManager.registerBleDelegate(this.bleDelegateImp);
      sdk.ble.startScan(null, (devices: IDOBleDeviceModel[] | null) => {
      });
      this.state.updateStatus('Scanning...');

      // Set a timeout to stop automatically
      setTimeout(async () => {
        if (this.isScanning) {
          await this.stopScan();
          this.state.updateStatus('Scan done');
        }
      }, 30000);

    } catch (error) {
      this.isScanning = false;
      this.state.isScanning = false;
      this.state.setError(`Failed to start scan: ${(error as BusinessError).message}`);
    }
  }

  // Stop scan
  async stopScan(): Promise<void> {
    if (!this.isScanning) {
      return;
    }

    try {
      sdk.ble.stopScan();
      this.isScanning = false;
      this.state.isScanning = false;
      this.state.updateStatus('Scan stopped');
    } catch (error) {
      this.state.setError(`Failed to stop scan: ${(error as BusinessError).message}`);
    }
  }
}