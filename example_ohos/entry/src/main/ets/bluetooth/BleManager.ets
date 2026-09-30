import {
  IDOBleDelegate,
  IDOBleDeviceModel,
  IDOBluetoothStateModel,
  IDOBridgeDelegate,
  IDODeviceNotificationModel,
  IDODeviceStateModel,
  IDOOtaDeviceModel,
  IDOReceiveData,
  IDOStatusNotification
} from 'protocol_channel';

import { Tools } from '../utils/Tools';

/**
 * app-side business logic handles the corresponding events; listen to the corresponding callbacks as needed
 */
export class AppDelegate {
  listenStatusNotification?: (status: IDOStatusNotification) => void
  listenWaitingOtaDevice?: (otaDevice: IDOOtaDeviceModel) => void
  listenDeviceNotification?: (model: IDODeviceNotificationModel) => void
  scanResult?: (list: IDOBleDeviceModel[] | null) => void
  bluetoothState?: (state: IDOBluetoothStateModel) => void
  deviceState?: (state: IDODeviceStateModel) => void
}

class BleManager {
  // Private fields
  private mBleState: IDODeviceStateModel | null = null;
  private delegates: AppDelegate[] = [];

  // Get the connection state
  getConnectState(): IDODeviceStateModel | null {
    return this.mBleState;
  }

  // Register a delegate
  registerBleDelegate(delegate: AppDelegate): void {
    if (!this.delegates.includes(delegate)) {
      this.delegates.push(delegate);
    }
  }

  // Unregister a delegate
  unregisterBleDelegate(delegate: AppDelegate): void {
    const index = this.delegates.indexOf(delegate);
    if (index !== -1) {
      this.delegates.splice(index, 1);
    }
  }

  public  idoBridgeImpl: IDOBridgeDelegate = {
    listenStatusNotification: (status: IDOStatusNotification): void => {
      console.debug("listenStatusNotification: " + status)
      this.delegates.forEach((delegate) => {
        if (delegate.listenStatusNotification != null) {
          delegate.listenStatusNotification(status)
        }
      });
    },
    listenDeviceNotification: (model: IDODeviceNotificationModel): void => {
      console.debug("listenDeviceNotification: " + model)
      this.delegates.forEach((delegate) => {
        if (delegate.listenDeviceNotification != null) {
          delegate.listenDeviceNotification(model)
        }
      });

    },
    checkDeviceBindState: (macAddress: string): boolean => {
      //app-side cached bind state of this device, provided by the app
      let isBind = Tools.getInstance().getBindState(macAddress);
      console.debug("checkDeviceBindState: " + macAddress + ", isBind: " + isBind)
      return isBind;
    },
    listenWaitingOtaDevice: (otaDevice: IDOOtaDeviceModel): void => {
      console.debug("listenWaitingOtaDevice: " + otaDevice)
      this.delegates.forEach((delegate) => {
        if (delegate.listenWaitingOtaDevice != null) {
          delegate.listenWaitingOtaDevice(otaDevice)
        }
      });

    }
  }
  // Internal delegate implementation
  public  bleDelegateImp: IDOBleDelegate = {
    // writeState: (state) => {
    //   this.delegates.forEach(it => it.writeState?.(state));
    // },
    receiveData: (data: IDOReceiveData) => {
    } /*this.receiveData(data)*/,

    scanResult: (list: IDOBleDeviceModel[] | null) => this.scanResult(list),

    bluetoothState: (state: IDOBluetoothStateModel) => this.bluetoothState(state),

    deviceState: (state: IDODeviceStateModel) => this.deviceState(state)
  };

  /**
   * Device scan result callback
   * @param list Array of found device models; when there are no devices, null
   */
  scanResult(list: IDOBleDeviceModel[] | null): void {
    this.delegates.forEach(it => {
      if (it.scanResult != null) {
        it.scanResult(list)
      }
    });
  }

  /**
   * Bluetooth state change callback
   * @param state The current Bluetooth state model
   */
  bluetoothState(state: IDOBluetoothStateModel): void {
    this.delegates.forEach(it => {
      if (it.bluetoothState != null) {
        it.bluetoothState(state)
      }
    });
  }

  /**
   * Device connection state change callback
   * @param state The current device state model
   */
  deviceState(state: IDODeviceStateModel): void {
    this.mBleState = state;
    this.delegates.forEach(it => {
      if (it.deviceState != null) {
        it.deviceState(state)
      }
    });
  }

  private static instance: BleManager;

  private constructor() {
  }

  public static getInstance(): BleManager {
    if (!BleManager.instance) {
      BleManager.instance = new BleManager();
    }
    return BleManager.instance;
  }
}

// Export the singleton access point
export default BleManager.getInstance();