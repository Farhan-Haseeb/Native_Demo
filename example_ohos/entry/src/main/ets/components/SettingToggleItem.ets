// entry/src/main/ets/components/SettingToggleItem.ets

@Component
export struct SettingToggleItem {
  // Its job is to render a row (Row) with an icon, text and a switch.
  @BuilderParam private icon: () => void;
  private label: string | Resource = '';
  private isOn: boolean = false;
  private onChange?: (isOn: boolean) => void;

  build() {
    // Fix: the root component is no longer a ListItem, but a Row.
    Row({ space: 12 }) {
      this.icon()

      Text(this.label)
        .fontSize($r('app.float.font_body'))
        .layoutWeight(1)

      Toggle({ type: ToggleType.Switch, isOn: this.isOn })
        .onChange((value: boolean) => {
          if (this.onChange) {
            this.onChange(value);
          }
        })
    }
    .width('100%')
    .padding({ top: 10, bottom: 10, left: 16, right: 16 })
    .alignItems(VerticalAlign.Center)
    // Styles such as the background color are controlled by the parent ListItem
  }
}