// LoadingDialog.ets
import promptAction from '@ohos.promptAction';

@Component
export struct LoadingDialog {
  // Dialog controller
  private controller: CustomDialogController | null = null;
  // Prompt text
  private message: string = 'Loading...';
  // Whether to show the percentage
  private showPercentage: boolean = false;
  // Current progress
  @State progressValue: number = 0;
  // Rotation animation
  @State rotateAngle: number = 0;

  // Constructor
  constructor(controller: CustomDialogController, message?: string, showPercentage?: boolean) {
    super();
    this.controller = controller;
    if (message) {
      this.message = message;
    }
    if (showPercentage !== undefined) {
      this.showPercentage = showPercentage;
    }
  }

  // Start the animation when the dialog is shown
  aboutToAppear() {
    this.startRotation();
  }

  // Start the rotation animation
  private startRotation() {
    setInterval(() => {
      this.rotateAngle = (this.rotateAngle + 10) % 360;
    }, 50);
  }

  // Update progress
  public updateProgress(value: number) {
    if (value < 0) {
      value = 0;
    }
    if (value > 100) {
      value = 100;
    }
    this.progressValue = value;

    // Close automatically when progress completes
    if (value === 100) {
      setTimeout(() => {
        this.controller?.close();
        promptAction.showToast({ message: 'Loading complete' });
      }, 500);
    }
  }

  // Build UI
  build() {
    Column() {
      // Rotating icon
      LoadingProgress()
        .color(Color.Blue)
        .width(24)
        .height(24)

      // Prompt text
      Text(this.message)
        .fontSize(18)
        .fontColor('#333333')
        .margin({ bottom: this.showPercentage ? 15 : 0 })

      // Progress bar
      if (this.showPercentage) {
        Row() {
          Progress({
            value: this.progressValue,
            total: 100,
            style: ProgressStyle.Linear
          })
            .width(200)
            .height(6)
            .color('#4A90E2')

          Text(`${this.progressValue}%`)
            .fontSize(14)
            .fontColor('#666666')
            .margin({ left: 10 })
        }
        .margin({ top: 5 })
      }
    }
    .padding(30)
    .backgroundColor('#FFFFFF')
    .borderRadius(12)
    .width(300)
    .justifyContent(FlexAlign.Center)
    .alignItems(HorizontalAlign.Center)
  }
}

// Loading dialog manager
export class LoadingManager {
  private static controller: CustomDialogController | null = null;

  // Show the loading dialog
  static showLoading(message: string = 'Loading...', showPercentage: boolean = false) {
    if (LoadingManager.controller != null) {
      LoadingManager.controller?.close();
    }

    LoadingManager.controller = new CustomDialogController({
      builder: LoadingDialog({
        controller: LoadingManager.controller!,
        message: message,
        showPercentage: showPercentage
      }),
      cancel: () => {
      },
      autoCancel: true,
      alignment: DialogAlignment.Center,
      customStyle: true
    });

    LoadingManager.controller.open();
  }

  // Hide the loading dialog
  static hideLoading() {
    if (LoadingManager.controller) {
      LoadingManager.controller.close();
      LoadingManager.controller = null;
    }
  }
  //
  // // Update progress
  // static updateProgress(value: number) {
  //   if (LoadingManager.controller && LoadingManager.controller?.isOpen()) {
  //     // Get the builder instance through the controller and update the progress
  //     const dialog = LoadingManager.controller?.builder as LoadingDialog;
  //     dialog?.updateProgress(value);
  //   }
  // }
}