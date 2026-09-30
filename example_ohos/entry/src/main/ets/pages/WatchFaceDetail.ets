// entry/src/main/ets/pages/WatchFace.ets
import { IDOBleDeviceModel, IDOTransType, IDOWatchItem, IDOWatchListModel } from 'protocol_channel';
import { NavStackModel } from '../viewmodel/NavStackModel';
import { WatchFaceViewModel } from '../viewmodel/WatchFaceViewModel';
import { common } from '@kit.AbilityKit';
import { PickerOption } from './TransferFileDetail';
import { Tools } from '../utils/Tools';
import { FilePicker } from '../utils/FilePicker';
import { promptAction } from '@kit.ArkUI';

@Builder
export function WatchFaceDetailPageBuilder(name: string, navInfo: NavStackModel) {
  WatchFaceDetailPage()
}

@Component
struct WatchFaceDetailPage {
  @State private viewModel: WatchFaceViewModel | null = null;
  private abilityContext: common.UIAbilityContext = getContext(this) as common.UIAbilityContext;
  // Used to display the final selection result URI
  @State selectedUri: string = '';
  @State fileName: string = '';
  // Define the options to show in the dialog
  private pickerOption: PickerOption = { label: 'fw', suffixes: ['.fw', '.zip', '.bin'] };

  private async doSelectFile() {
    try {
      // Step a: check and request permission
      const hasPermission = await Tools.getInstance().checkAndRequestReadMediaPermissions(this.abilityContext);

      if (hasPermission) {
        // Step b: permission granted, open the file picker
        const uri: string = await FilePicker.select({ fileSuffixes: this.pickerOption!.suffixes });
        // Step c: success callback, update the UI state
        this.selectedUri = uri;
        promptAction.showToast({ message: 'File selected!' });
      } else {
        // Step d: permission denied, notify the user
        promptAction.showToast({ message: 'You denied the file read permission, cannot select a file.' });
      }
    } catch (error) {
      // Step e: catch errors (for example the user cancelled the file picker)
      const errorMessage: string = (error instanceof Error) ? error.message : 'Unknown error';
      if (errorMessage.includes('cancel')) { // Decide whether the user cancelled, based on the FilePicker implementation
        console.info('File selection was cancelled by user.');
      } else {
        promptAction.showToast({ message: `Operation failed: ${errorMessage}` });
      }
    }
  }

  private onRemove(name: string) {
    promptAction.showDialog({
      title: 'Confirm uninstall',
      message: `Are you sure you want to uninstall: ${name}?`,
      buttons: [
        {
          text: 'Cancel',
          color: '#333333'
        },
        {
          text: 'OK',
          color: '#FF0000'
        }
      ]
    }).then(dialogResult => {
      if (dialogResult.index === 1) {
        this.viewModel?.uninstall(name);
        console.log('User confirmed the uninstall.');
      } else {
        console.log('User cancelled the uninstall.');
      }
    }).catch(() => {
      console.error('Failed to show the dialog');
    })
  }

  aboutToAppear(): void {
    this.viewModel = new WatchFaceViewModel(this.abilityContext)
  }

  build() {
    NavDestination() {
      Column({ space: 20 }) {

        List({ space: 10 }) {
          // ForEach iterates over the data source, creating for each data item one ListItem
          ForEach(
            this.viewModel?.watchList,
            (item: IDOWatchItem) => {
              ListItem() {
                // Use Row for a horizontal layout
                Row() {
                  // Left: watch face info (name and size)
                  Column({ space: 5 }) {
                    Text(item.name)
                      .fontSize(13)
                      .fontWeight(FontWeight.Bold)
                      .fontColor(this.viewModel?.checkNowShowWatchName(item.name) ? Color.Blue : Color.Black)
                    Text(`${(item.size / 1024).toFixed(2)} KB`) // Format the size as a string with a unit
                      .fontSize(12)
                      .fontColor($r('app.color.gray')) // Use the secondary color
                  }
                  .alignItems(HorizontalAlign.Start) // Left-align the elements in the column
                  .layoutWeight(1) // Take all remaining space, pushing the delete button to the far right

                  // Right: delete button
                  Button('Uninstall')
                    .type(ButtonType.Capsule)
                    .width(120)
                    .height(40)
                    .backgroundColor(Color.Red) // Set the button background to red
                    .fontColor(Color.White)
                    .onClick(() => {
                    this.onRemove(item.name);
                  }).enabled(!this.viewModel?.isProcessing)
                }
                .width('100%')
                .padding({ left: 16, right: 8, top: 12, bottom: 12 })
              }
            },
            (item: IDOWatchItem) => item.name // Provide a unique key for each list item, which helps performance
          )
        }
        .layoutWeight(1) // Let the list take most of the remaining space
        .width('90%')
        .backgroundColor($r('app.color.component_bg'))
        .borderRadius(12)
        .divider({ strokeWidth: 1, color: $r('app.color.divider_color'), startMargin: 15, endMargin: 15 })

        Button($r('app.string.refresh'))
          .width('90%')
          .height(48)
          .type(ButtonType.Capsule)
          .backgroundColor($r('app.color.primary_action_blue'))
          .onClick(() => {
            this.viewModel?.getWatchList();
          }).enabled(!this.viewModel?.isProcessing)

        Button('Select File')
          .width('90%')
          .height(48)
          .type(ButtonType.Capsule)
          .backgroundColor($r('app.color.primary_action_blue'))
          .onClick(() => {
            this.doSelectFile();
          }).enabled(!this.viewModel?.isProcessing)

        Scroll() {
          Text('file: ' + this.selectedUri)
            .fontSize(12).fontColor($r('app.color.gray'))
            .width('100%')
            .padding(10).textAlign(TextAlign.Start);
        }
        // This is a basic text input box
        TextInput({ placeholder: 'Enter the watch face name...' })
          .width('90%')
          .height(44)
          .placeholderFont({ size: 16, weight: FontWeight.Lighter })
          .fontSize(16)
          .fontColor(Color.Black)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .padding({ left: 15, right: 15 })
          .onChange((value: string) => {
            this.fileName = value;
          });

        Button($r('app.string.watch_face_add'))
          .width('90%')
          .height(48)
          .type(ButtonType.Capsule)
          .backgroundColor($r('app.color.primary_action_blue'))
          .onClick(() => {
            this.viewModel?.install(IDOTransType.iwfLz, this.selectedUri, this.fileName);
          }).enabled(!this.viewModel?.isProcessing)

        Scroll() {
          Text(this.viewModel?.resStr)
            .fontSize(11).fontColor($r('app.color.gray'))
            .width('100%').padding(10).textAlign(TextAlign.Start)
        }
        .layoutWeight(1)
        .width('90%')
        .backgroundColor($r('app.color.component_bg'))
        .borderRadius(12)
        .border({ width: 1, color: $r('app.color.divider_color') })
      }
      .width('100%').padding({ top: 20, bottom: 20 }).backgroundColor($r('app.color.page_bg'))
    }.title('Watch face')
  }
}