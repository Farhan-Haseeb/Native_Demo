import fs from '@ohos.file.fs';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { abilityAccessCtrl, common, bundleManager, Permissions, PermissionRequestResult } from '@kit.AbilityKit';
import { myPreferences } from '../entryability/EntryAbility';
import { notificationManager } from '@kit.NotificationKit';
import { IDOBleDeviceModel } from 'protocol_channel';
import { ArrayList } from '@kit.ArkTS';


export class Tools {
  private static instance: Tools;
  private atManager = abilityAccessCtrl.createAtManager();
  private keyBindState = "bindState_"
  private keyBindDevices = "keyBindDevices"

  // Private constructor, ensures instances are only created through getInstance
  private constructor() {
  }

  // Public accessor for the singleton
  public static getInstance(): Tools {
    if (!Tools.instance) {
      Tools.instance = new Tools();
    }
    return Tools.instance;
  }

  /**
   * Get the size of the specified file (bytes)
   * @param path Full path of the file
   */
  public getFileSize(path: string): number {
    let fileStat = fs.statSync(path); // Get the file status synchronously
    return fileStat.size; // Unit is bytes
  }

  public async requestNotificationPermission(context: common.UIAbilityContext): Promise<boolean> {
    const atManager = abilityAccessCtrl.createAtManager();
    const permission: Permissions = 'ohos.permission.ACCESS_NOTIFICATION_POLICY';
    // Get the current app accessTokenId
    const bundleInfo: bundleManager.BundleInfo =
      await bundleManager.getBundleInfoForSelf(bundleManager.BundleFlag.GET_BUNDLE_INFO_WITH_APPLICATION);
    const appInfo: bundleManager.ApplicationInfo = bundleInfo.appInfo;
    const tokenID: number = appInfo.accessTokenId;
    try {
      // Check whether the app has already been granted this permission
      const status = await atManager.checkAccessToken(tokenID, permission);
      if (status === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
        console.log('Notification permission has been granted.');
        return true;
      } else {
        // If not granted, request it from the user
        const data: PermissionRequestResult = await atManager.requestPermissionsFromUser(context, [permission]);
        if (data.authResults[0] === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
          console.log('Notification permission granted successfully.');
          return true;
        } else {
          console.log('Notification permission was denied.');
          return false;
        }
      }
    } catch (err) {
      console.error(`Failed to request notification permission: ${JSON.stringify(err)}`);
      return false;
    }
  }

  public async publishNotification(id: number, title: string, text: string, additionalText: string, context: common.UIAbilityContext) : Promise<boolean> {
    const notificationRequest: notificationManager.NotificationRequest = {
      id: id, // Each notification needs a unique ID
      content: {
        notificationContentType: notificationManager.ContentType.NOTIFICATION_CONTENT_BASIC_TEXT,
        normal: {
          title: title,
          text: text,
          additionalText: additionalText
        }
      }
    };

    try {
      await notificationManager.publish(notificationRequest);
      console.log('Notification published successfully.');
      return true
    } catch (err) {
      console.error(`Failed to publish notification: ${JSON.stringify(err)}`);
      return false
    }
  }

  /**
   * Check and request the permission to read media files.
   * @param context UIAbility context.
   * @returns Promise<boolean> - Whether the user granted the permission.
   */
  public async checkAndRequestReadMediaPermissions(context: common.UIAbilityContext): Promise<boolean> {
    let LOG_TAG = 'requestReadPermission'
    let filePermission: Permissions = 'ohos.permission.READ_MEDIA';
    // Get the current app accessTokenId
    const bundleInfo: bundleManager.BundleInfo =
      await bundleManager.getBundleInfoForSelf(bundleManager.BundleFlag.GET_BUNDLE_INFO_WITH_APPLICATION);
    const appInfo: bundleManager.ApplicationInfo = bundleInfo.appInfo;
    const tokenID: number = appInfo.accessTokenId;

    try {
      // 2. Check the current permission status
      const grantStatus = await this.atManager.checkAccessToken(tokenID, filePermission);

      if (grantStatus === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
        hilog.info(0x0000, LOG_TAG, 'Read media permission has already been granted.');
        return true; // Permission already granted
      }

      // 3. If not granted, request it from the user
      hilog.info(0x0000, LOG_TAG, 'Requesting read media permission from user...');
      const result = await this.atManager.requestPermissionsFromUser(context, [filePermission]);
      const authResult = result.authResults[0];

      if (authResult === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
        hilog.info(0x0000, LOG_TAG, 'User granted the read media permission.');
        return true; // The user just granted the permission
      } else {
        hilog.warn(0x0000, LOG_TAG, 'User denied the read media permission.');
        return false; // The user denied the permission
      }
    } catch (err) {
      const error = err as Error;
      hilog.error(0x0000, LOG_TAG, `Failed to request permission: ${error.message}`);
      return false; // An error occurred
    }
  }


  /**
   * Check and request the specified permission.
   * @param context UIAbility context.
   * @param permissions The required permission.
   * @param sendRequest Whether to send a request when not granted.
   * @returns Promise<boolean> - Whether the user granted the permission.
   */
  public async checkAndRequestPermissionsWith(context: common.UIAbilityContext, permissions: Permissions, sendRequest: boolean): Promise<boolean> {
    let LOG_TAG = 'checkAndRequestPermissions'
    // Get the current app accessTokenId
    const bundleInfo: bundleManager.BundleInfo =
      await bundleManager.getBundleInfoForSelf(bundleManager.BundleFlag.GET_BUNDLE_INFO_WITH_APPLICATION);
    const appInfo: bundleManager.ApplicationInfo = bundleInfo.appInfo;
    const tokenID: number = appInfo.accessTokenId;

    try {
      // 2. Check the current permission status
      const grantStatus = await this.atManager.checkAccessToken(tokenID, permissions);

      if (grantStatus === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
        hilog.info(0x0000, LOG_TAG, permissions+' permission has already been granted.');
        return true; // Permission already granted
      }

      // No request needed, return directly
      if (!sendRequest) {
        return false;
      }

      // 3. If not granted, request it from the user
      hilog.info(0x0000, LOG_TAG, 'Requesting permission from user...');
      const result = await this.atManager.requestPermissionsFromUser(context, [permissions]);
      const authResult = result.authResults[0];

      if (authResult === abilityAccessCtrl.GrantStatus.PERMISSION_GRANTED) {
        hilog.info(0x0000, LOG_TAG, 'User granted the  permission.');
        return true; // The user just granted the permission
      } else {
        hilog.warn(0x0000, LOG_TAG, 'User denied the  permission.');
        return false; // The user denied the permission
      }
    } catch (err) {
      const error = err as Error;
      hilog.error(0x0000, LOG_TAG, `Failed to request permission: ${error.message}`);
      return false; // An error occurred
    }
  }


  /**
   * Copy the file pointed to by an external URI into the app sandbox cache directory.
   * This is the recommended bridge method between the native file system and Flutter.
   *
   * @param context UIAbility context, used to get the sandbox path.
   * @param sourceUri The source URI of the file (for example 'file://docs/storage/.../a.json').
   * @returns Promise<string | null> - On success returns the sandbox file path (string), on failure returns null.
   */
  async copyFileToAppCache(context: common.UIAbilityContext, sourceUri: string): Promise<string | null> {
    const LOG_TAG = 'FileBridge'; // Define a log tag for easy filtering

    try {
      // 1. Get the path of the app-specific cache directory. This is the standard place for temporary files.
      const tempDir: string = context.tempDir;
      hilog.info(0x0000, LOG_TAG, 'App temp directory is: %{public}s', tempDir);

      // Check and create the directory
      if (!await fs.access(tempDir)) {
        hilog.info(0x000, LOG_TAG, 'create dir :'+tempDir);
        await fs.mkdir(tempDir)
      }else {
        hilog.info(0x000, LOG_TAG, 'exists dir :'+tempDir);
      }

      // 2. Parse the original file name from the source URI.
      const fileName = sourceUri.substring(sourceUri.lastIndexOf('/') + 1);
      if (!fileName) {
        hilog.error(0x0000, LOG_TAG, 'Could not extract file name from URI: %{public}s', sourceUri);
        return null;
      }

      // 3. Build the full path of the target file (cache directory + tmp_file name).
      const destinationPath = `${tempDir}/tmp_${fileName}`;
      hilog.info(0x0000, LOG_TAG, 'Destination path in sandbox will be: %{public}s', destinationPath);

      // Open the file using its URI to get a file descriptor (fd)
      let sourceFile = await fs.open(sourceUri, fs.OpenMode.READ_ONLY);

      // Get file stats to determine its size
      const sourceStat: fs.Stat = await fs.stat(sourceFile.fd);
      const size: number = sourceStat.size;

      hilog.info(0x0000, LOG_TAG, 'file size: %{public}d', size);

      // let hasPermission = await this.checkAndRequestReadMediaPermissions(context);
      //
      // if (hasPermission) {
      //   hilog.info(0x0000, LOG_TAG, 'hasPermission: '+hasPermission);
      // }

      // The copy method fails and returns error code 2, probably a permission issue; temporarily switched to writing the file manually
      // // 4. [Core step] Perform the copy operation.
      // //    fs.copyFile is a powerful API that can copy directly using a URI as the source.
      // await fs.copyFile(sourceUri, destinationPath);

      // 4. Copy the file
      // --- Read the source file manually ---
      const stat = await fs.stat(sourceFile.fd);
      const buffer = new ArrayBuffer(stat.size);
      await fs.read(sourceFile.fd, buffer); // Read the file contents into buffer
      hilog.info(0x0000, LOG_TAG, 'Source file read into buffer successfully.');

      // --- Write the target file manually ---
      let destFile = await fs.open(destinationPath, fs.OpenMode.READ_WRITE | fs.OpenMode.CREATE);
      await fs.write(destFile.fd, buffer); // Write the buffer contents to the new file
      hilog.info(0x0000, LOG_TAG, 'Buffer written to destination file successfully!');

      // 5. Return this sandbox path, which Flutter can use directly.
      return destinationPath;

    } catch (err) {
      hilog.error(0x0000, LOG_TAG, 'Failed to copy file. Err:'+err);
      return null;
    }
  }

  getBindState(macAddress: string): boolean {
    let key = this.keyBindState+"#"+macAddress;
    let rs = (myPreferences?.getSync(key, false) as boolean) ?? false
    console.debug("myPreferences: "+myPreferences)
    console.debug("call getBindState rs: " + rs)
    return rs
  }

  setBindState(macAddress: string, isBind: boolean) {
    let key = this.keyBindState+"#"+macAddress;
    console.debug("myPreferences: "+myPreferences)
    console.debug("call setBindState macAddress: "+macAddress+", isBind: " + isBind)
    myPreferences?.putSync(key, isBind);
    myPreferences?.flushSync()
  }


  setBindDevice(device: IDOBleDeviceModel) {
    let devices = this.getBindDevices();
    devices.push(device);
    let json = JSON.stringify(devices)
    console.info("setBindDevice: "+json);
    myPreferences?.putSync(this.keyBindDevices, json)
    myPreferences?.flushSync();
  }

  getBindDevices(): IDOBleDeviceModel[] {
    let rs = (myPreferences?.getSync(this.keyBindDevices, null) as string | null) ?? ""
    if (rs == null || rs.length == 0) {
      return [];
    }
    console.info("getBindDevices: "+rs)
    let devices = JSON.parse(rs) as IDOBleDeviceModel[];
    console.info("getBindDevices, devices = "+devices.length)
    return devices;
  }

  removeBindDevice(mac: string) {
    let devices = this.getBindDevices();
    if (devices.length==0) {
      return;
    }

    let newList = new ArrayList<IDOBleDeviceModel>();
    for (let i = 0; i < devices.length; i++) {
      let device = devices[i];
      if (device.macAddress == mac) {
        continue;
      }
      newList.add(device);
    }
    myPreferences?.putSync(this.keyBindDevices, JSON.stringify(newList.convertToArray()))
    myPreferences?.flushSync();
  }
}

