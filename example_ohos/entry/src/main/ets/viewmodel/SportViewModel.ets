// Import the UI prompt dialog capability from the ArkUI framework
import { promptAction } from '@kit.ArkUI';

// Import all required modules from the mock SDK or the real SDK
// Note: for instanceof to work, these imports must be a class, not an interface
import {
  sdk,
  IDOExchangeDataDelegate,
  IDOExchangeBaseModel,
  IDOAppStartExchangeModel,
  IDOAppEndExchangeModel,
  IDOAppStartReplyExchangeModel,
  IDOAppEndReplyExchangeModel,
  IDOAppActivityDataV3ExchangeModel,
  IDOExchangeV2Model,
  IDOExchangeV3Model,
  IDOBleStartExchangeModel,
  IDOBleIngExchangeModel,
  IDOBleEndExchangeModel,
  IDOBlePauseExchangeModel,
  IDOBleRestoreExchangeModel,
  IDOAppBlePauseExchangeModel,
  IDOAppBleRestoreExchangeModel,
  IDOAppBleEndExchangeModel,
  IDOAppRestoreExchangeModel,
  IDOAppPauseExchangeModel,
  IDOAppIngV3ExchangeModel,
  IDOAppIngExchangeModel,
  IDOBleStartReplyExchangeModel,
  IDOBleIngReplyExchangeModel,
  IDOBleEndReplyExchangeModel,
  IDOBlePauseReplyExchangeModel,
  IDOBleRestoreReplyExchangeModel,
  IDOAppBlePauseReplyExchangeModel,
  IDOAppRestoreReplyExchangeModel,
  IDOAppBleEndReplyExchangeModel,
  IDOAppPauseReplyExchangeModel,
  IDOAppBleRestoreReplyExchangeModel
} from 'protocol_channel'; // Path points to your SDK module
import { locationManager } from '@kit.MDMKit';
import { LocationManager } from '../utils/LocationManager';
import { common } from '@kit.AbilityKit';


const INTERVAL_EXCHANGE_DATA = 2000;
const INTERVAL_EXCHANGE_COMPLETE_DATA = 40000;
const INTERVAL_EXCHANGE_HR_DATA = 30000;

export enum SportStatus {
  Stopped,
  Started,
  Paused,
  Resumed
}

/**
 * Sport page ViewModel
 *
 * - Marked with the @Observed decorator so it becomes an observable object.
 *   This means any change to its properties is automatically detected by the UI layer and triggers a refresh.
 *
 * - Implements the IDOExchangeDataDelegate interface and acts as the data exchange delegate,
 *   handling all communication callbacks from the Bluetooth device.
 *
 * - Encapsulates all business logic and state of the page so the UI layer (View) can stay purely declarative.
 */
@Observed
export class SportViewModel implements IDOExchangeDataDelegate {
  // =================================================================
  // MARK: - UI Interaction Callbacks
  // =================================================================

  /**
   * @description Used to ask the UI layer to show a message.
   *              The View layer injects the concrete implementation in aboutToAppear (e.g. showing a Toast).
   *              Convention: a non-empty string shows the message; an empty string or special command can be used to hide the prompt.
   */
  public showMessageCallback: (message: string) => void = () => {
  };
  // =================================================================
  // MARK: - State Properties
  // =================================================================

  /** @description Current sport state, bound directly to the UI to control button availability etc. */
  @Track public exchangeType: SportStatus = SportStatus.Stopped;
  /** @description Log text shown on the screen, for debugging and status display. */
  @Track public consoleLog: string = "Welcome! Tap 'Start' to begin the sport.";
  /** @description Stores the base info of the current sport (e.g. start time), used across multiple communications. */
  private baseModel: IDOExchangeBaseModel | null = null;
  /** @description Duration of the current sport (seconds). */
  private duration: number = 0;
  /** @description Calories burned in the current sport. */
  private calories: number = 0;
  /** @description Distance of the current sport (meters). */
  private distance: number = 0;
  private gpsSignal: number = 0;
  /** @description Marks whether the sport was ended by the App side, used to handle the final data sync. */
  private isSportEnd: boolean = false;
  /** @description Stores the IDs of all timers so they can be cleared when no longer needed (e.g. pause, stop, page destroy). */
  private timers: number[] = [];

  private locationMgr: LocationManager | null = null;
  private abilityContext: common.UIAbilityContext;

  // =================================================================
  // MARK: - Lifecycle
  // =================================================================

  constructor(context: common.UIAbilityContext) {
    this.abilityContext = context;
    this.locationMgr = LocationManager.getInstance(this.abilityContext)
    sdk.dataExchange.addExchange(this);
    this.appendToConsole("ViewModel initialized and registered with the SDK.");
  }

  /**
   * Destroy method.
   * Called externally when the UI page is about to be destroyed, to clean up timers and other long-running resources and prevent memory leaks.
   */
  public destroy(): void {
    this.stopTimers();
    this.appendToConsole("ViewModel destroyed, all timers cleared.");
  }

  // =================================================================
  // MARK: - Public Actions - called by the UI layer
  // =================================================================

  /**
   * Start the sport.
   */
  public sportStart(): void {
    this.consoleLog = '';
    this.appendToConsole("Trying to start the sport...");
    // [Decoupling] Ask the UI to show a prompt via callback
    this.showMessageCallback('Starting the sport...');

    const currentDate = new Date();
    // Create the base model for this sport session
    const baseModel = new IDOExchangeBaseModel(currentDate.getDate(),
      currentDate.getHours(),
      currentDate.getMinutes(),
      currentDate.getSeconds(),
      0x32,
    )
    this.baseModel = baseModel;
    // Create and send the App start sport command object
    const obj = new IDOAppStartExchangeModel(baseModel, 0, 0, 1, 0, 0, 0); // forceStart: 1
    sdk.dataExchange.appExec(obj);
    this.exchangeType = SportStatus.Started;
  }

  /**
   * Pause the sport.
   */
  public sportPause(): void {
    if (!this.baseModel) {
      return;
    } // Do nothing if no sport is in progress
    this.appendToConsole("Pausing the sport...");

    const obj = new IDOAppPauseExchangeModel(this.baseModel)
    // Actually send
    sdk.dataExchange.appExec(obj); // Simulated send

    // Update the UI state and stop the data sync timers
    this.exchangeType = SportStatus.Paused;
    this.stopTimers();
  }

  /**
   * Resume the sport.
   */
  public sportResume(): void {
    if (!this.baseModel) {
      return;
    }
    this.appendToConsole("Resuming the sport...");

    const obj = new IDOAppRestoreExchangeModel(this.baseModel)
    // Actually send IDOAppRestoreExchangeModel
    sdk.dataExchange.appExec(obj); // Simulated send

    // Update the UI state and restart the data sync timers
    this.exchangeType = SportStatus.Resumed;
    this.startTimers();
  }

  /**
   * Stop the sport.
   */
  public sportStop(): void {
    if (!this.baseModel) {
      return;
    }
    this.appendToConsole("Stopping the sport...");

    // Create and send the App end sport command object, including the summary data of this sport session
    const obj = new IDOAppEndExchangeModel(this.baseModel, this.duration, this.calories, this.distance, 1); // isSave: 1
    sdk.dataExchange.appExec(obj);
  }

  // =================================================================
  // MARK: - Private Helper Methods
  // =================================================================

  /**
   * Prepend a new message to the top of the console log.
   * @param message The message to log.
   */
  private appendToConsole(message: string): void {
    const time = new Date().toLocaleTimeString();
    // New log lines go first, giving a scrolling effect
    this.consoleLog = `[${time}] ${message}\n${this.consoleLog}`;
  }

  /**
   * Start all timers used for data sync.
   */
  private startTimers(): void {
    this.appendToConsole("Timers started.");
    this.stopTimers(); // Clear all old timers first to avoid duplicates

    // Timer 1: sync brief sport data
    const dataTimer = setInterval(() => {
      if (this.exchangeType === SportStatus.Started || this.exchangeType === SportStatus.Resumed) {
        this.exchangeData();
      }
    }, INTERVAL_EXCHANGE_DATA);
    this.timers.push(dataTimer);

    // Timer 2: sync the full sport summary data
    const completeTimer = setInterval(() => {
      if (this.exchangeType === SportStatus.Started || this.exchangeType === SportStatus.Resumed) {
        this.appendToConsole("Requesting full sport report sync...");
        sdk.dataExchange.getLastActivityData();
      }
    }, INTERVAL_EXCHANGE_COMPLETE_DATA);
    this.timers.push(completeTimer);

    // Timer 3: sync V3 sport heart rate data
    const hrTimer = setInterval(() => {
      if (this.exchangeType === SportStatus.Started || this.exchangeType === SportStatus.Resumed) {
        if (sdk.dataExchange.supportV3ActivityExchange) {
          this.appendToConsole("Requesting heart rate data sync...");
          sdk.dataExchange.getActivityHrData()
        }
      }
    }, INTERVAL_EXCHANGE_HR_DATA);
    this.timers.push(hrTimer);
  }

  /**
   * Stop and clear all running timers.
   */
  private stopTimers(): void {
    if (this.timers.length > 0) {
      this.appendToConsole("Timers stopped.");
      this.timers.forEach(timerId => clearInterval(timerId));
      this.timers = []; // Clear the array
    }
  }

  /// Synchronize brief sports data for app display
  /// Sync every 2 seconds
  private exchangeData() {
    if (this.baseModel == null) {
      return;
    }
    if (sdk.dataExchange.supportV3ActivityExchange) {
      let obj = new IDOAppIngV3ExchangeModel(this.baseModel, 2, this.gpsSignal,  this.distance, 0,  this.duration, this.calories)
      sdk.dataExchange.appExec(obj)
    } else {
      let obj = new IDOAppIngExchangeModel(this.baseModel, this.duration, this.calories, this.distance, 0)
      sdk.dataExchange.appExec(obj)
    }
  }

  // =================================================================
  // MARK: - IDOExchangeDataDelegate interface implementation
  // =================================================================

  /**
   * Listen for the device response after the App executes a command.
   * @param model The reply data model instance received from the SDK.
   */
  public appListenAppExec(model: object): void {
    this.appendToConsole(`Received reply to App command: ${JSON.stringify(model)}`);

    if (model instanceof IDOAppStartReplyExchangeModel) {
      // [Decoupling] Ask the UI to remove the loading prompt; an empty string is used as the signal here
      this.showMessageCallback('');
      if (model.retCode === 0) { // 0 means success
        this.appendToConsole("Sport started successfully!");
        this.exchangeType = SportStatus.Started;
        this.startTimers();
        // After a successful start, reset the sport data
        this.duration = 0;
        this.calories = 0;
        this.distance = 0;
        this.locationMgr?.startLocationUpdates((locationData) => {
          this.distance = this.locationMgr!.getTotalDistance();
          this.gpsSignal = locationData.accuracy;
          console.debug(`"this.distance:${this.distance} this.gpsSignal:${this.gpsSignal}"`)
        })
      } else {
        this.appendToConsole(`Failed to start sport, error code: ${model.retCode}.`);
        // [Decoupling] Ask the UI to show the error message
        this.showMessageCallback(`Failed to start sport, error code: ${model.retCode}`);
        this.baseModel = null; // Start failed, clear the base model
      }
    } else if (model instanceof IDOAppEndReplyExchangeModel) {
      this.appendToConsole("Received confirmation of sport end. Sport session done.");
      this.exchangeType = SportStatus.Stopped;
      this.baseModel = null;
      this.stopTimers();
      this.isSportEnd = true; // Mark the sport as ended
      // After ending, actively fetch the final heart rate and sport summary data
      sdk.dataExchange.getActivityHrData();
      sdk.dataExchange.getLastActivityData();
    }else if (model instanceof IDOAppPauseReplyExchangeModel) {
      let obj = model as IDOAppPauseReplyExchangeModel
      console.debug("reply for app's pause cmd: "+obj)
      this.exchangeType = SportStatus.Paused
    }
    else if (model instanceof IDOAppRestoreReplyExchangeModel) {
      let obj = model as IDOAppRestoreReplyExchangeModel
      console.debug("reply for app's restore cmd: "+obj)
      this.exchangeType = SportStatus.Resumed
    } else if (model instanceof IDOAppActivityDataV3ExchangeModel) {
      this.appendToConsole(`Received sport data update: ${JSON.stringify(model)}`);
      // Update local data, taking the max to prevent rollback
      this.duration = Math.max(this.duration, model.durations ?? 0);
      this.calories = Math.max(this.calories, model.calories ?? 0);
      this.distance = Math.max(this.distance, model.distance ?? 0);
      // If this data arrives after the sport was marked as ended, it is the final summary data
      if (this.isSportEnd) {
        this.isSportEnd = false; // Reset the flag
        this.appendToConsole("Final sport data received. Task done!");
      }
    }
  }

  /**
   * Listen for events initiated by the device.
   * @param model The data model instance reported by the device, received from the SDK.
   */
  public appListenBleExec(model: object): void {
    this.appendToConsole(`Received device-initiated event: ${JSON.stringify(model)}`);

    // Strictly follows the Swift code logic, responding to sport state changes initiated by the device
    if (model instanceof IDOBleStartExchangeModel) {
      let obj = model as IDOBleStartExchangeModel
      let sendModel = new IDOBleStartReplyExchangeModel(obj.baseModel, obj.operate, 0)
      // !!!: Device-started sports are not yet linked with the app
      // this.baseModel = obj.baseModel
      this.exchangeType = SportStatus.Started;
      sdk.dataExchange.appReplyExec(sendModel)
    }else if (model instanceof  IDOBleIngExchangeModel) {
      let obj = model as IDOBleIngExchangeModel
      let sendModel = new IDOBleIngReplyExchangeModel(obj.baseModel, obj.distance ?? 0)
      sdk.dataExchange.appReplyExec(sendModel)
    } else if (model instanceof IDOBleEndExchangeModel) {
      let obj = model as IDOBleEndExchangeModel
      let sendModel = new IDOBleEndReplyExchangeModel(obj.baseModel, 0)
      sdk.dataExchange.appReplyExec(sendModel)
      this.exchangeType = SportStatus.Stopped;
    } else if (model instanceof IDOBlePauseExchangeModel) {
      let obj = model as IDOBlePauseExchangeModel
      let sendModel = new IDOBlePauseReplyExchangeModel(obj.baseModel, 0)
      sdk.dataExchange.appReplyExec(sendModel)
      this.exchangeType = SportStatus.Paused;
    } else if (model instanceof IDOBleRestoreExchangeModel) {
      let obj = model as IDOBleRestoreExchangeModel
      let sendModel = new IDOBleRestoreReplyExchangeModel(obj.baseModel, 0)
      sdk.dataExchange.appReplyExec(sendModel)
      this.exchangeType = SportStatus.Resumed;
    } else if (model instanceof IDOAppBlePauseExchangeModel) {
      let obj = model as IDOAppBlePauseExchangeModel
      let sendModel = new IDOAppBlePauseReplyExchangeModel(obj.baseModel, 0)
      sdk.dataExchange.appReplyExec(sendModel)
      this.exchangeType = SportStatus.Paused;
    } else if (model instanceof IDOAppBleRestoreExchangeModel) {
      let obj = model as IDOAppBleRestoreExchangeModel
      let sendModel = new IDOAppBleRestoreReplyExchangeModel(obj?.baseModel, 0)
      sdk.dataExchange.appReplyExec(sendModel)
      this.exchangeType = SportStatus.Resumed;
    } else if (model instanceof IDOAppBleEndExchangeModel) {
      let obj = model as IDOAppBleEndExchangeModel
      let sendModel = new IDOAppBleEndReplyExchangeModel(this.baseModel!, 0, obj.duration, obj.calories, obj.distance)
      sdk.dataExchange.appReplyExec(sendModel)
      this.exchangeType = SportStatus.Stopped;
      sdk.dataExchange.getActivityHrData()
      sdk.dataExchange.getLastActivityData()
      this.isSportEnd = true
    }
  }

  /**
   * Listen for V2 generic data exchange.
   */
  public exchangeV2Data(model: IDOExchangeV2Model): void {
    this.appendToConsole(`Received V2 data: ${JSON.stringify(model)}`);
    // No concrete logic implemented yet
  }

  /**
   * Listen for V3 generic data exchange.
   */
  public exchangeV3Data(model: IDOExchangeV3Model): void {
    this.appendToConsole(`Received V3 data: ${JSON.stringify(model)}`);
    // No concrete logic implemented yet
  }
}