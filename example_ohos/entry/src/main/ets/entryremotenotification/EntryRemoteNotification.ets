import { RemoteNotificationExtensionAbility, pushCommon, RemoteNotificationExtensionContext } from '@kit.PushKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { CmdError, Cmds, IDONoticeMessageParamModel, IFailureResult, sdk } from 'protocol_channel';

const DOMAIN = 0x0000;

// Remote push capability extension (only useful for the app itself), purpose:
// When your app receives a push through Huawei Push Service (HMS Push), before the system shows it in the notification bar, you get a chance to intercept and process the push content.
// Requires remote push, not tested yet
export default class EntryRemoteNotification extends RemoteNotificationExtensionAbility {

  async onReceiveMessage(remoteNotificationInfo: pushCommon.RemoteNotificationInfo):
    Promise<pushCommon.RemoteNotificationContent> {
    hilog.info(DOMAIN, 'testTag', 'remoteNotificationInfo: %{public}s', JSON.stringify(remoteNotificationInfo));
    if (sdk.state.isConnected) {
      // sdk.ble.getBluetoothState((state) => {
      //   hilog.info(DOMAIN, 'testTag', 'state: %{public}s', JSON.stringify(state));
      //   const param = IDONoticeMessageParamModel.fromJson({evt_type: 1,
      //     msg_id: remoteNotificationInfo.id,
      //     support_answering: false,
      //     support_mute: false,
      //     support_hang_up: false,
      //     contact: "xx00"+attempt,
      //     phone_number: "33003"+remoteNotificationInfo.title,
      //     data_text: "data text"+attempt});
      //   Cmds.noticeMessageV3(param, (res) => {
      //     if (res.success) {
      //       const value = res.value;
      //       console.debug("sendNotificationMsg successful "+value?.toJsonString())
      //     } else {
      //       const err = res as IFailureResult<CmdError>;
      //       console.debug(`sendNotificationMsg err code: ${err.error.code} msg: ${err.error.message}`);
      //     }
      //   });
      // });
    }
    await Promise.resolve();
    return {
      title: 'Default replace title.',
      text: 'Default replace text.',
      badgeNumber: 1,
      wantAgent: {
        abilityName: 'DemoAbility',
        parameters: {
          key: 'Default value'
        }
      }
    };
  }

  onDestroy(): void {
    hilog.info(0x0000, 'testTag', 'RemoteNotificationExtAbility onDestroy.');
  }

}