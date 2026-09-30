# Glossary (Chinese -> English) for the Native_Demo translation

English terms are taken from the repo's own English resources (`res/values/strings.xml`,
`Localizable.xcstrings`, OHOS `base/element/string.json`) so code, comments and UI agree.

## Product / device
| 中文 | English | Notes |
|---|---|---|
| 手环 | band | |
| 手表 | watch | |
| 设备 | device | |
| 表盘 | watch face | `表盘定制工具` UI key stays "Dial Customization Tool" (existing xcstrings key) |
| 照片表盘 / 壁纸表盘 | photo watch face / wallpaper watch face | |
| 固件 | firmware | 固件升级 = firmware upgrade; OTA stays OTA |
| 功能表 | function table | 功能表 扩展 = function table (extended) |
| 协议 / 协议库 | protocol / protocol library | v2/v3 protocol |
| 指令 / 命令 | command | |
| 事件编号 | event number | |
| 快速配置 | fast config | matches xcstrings "Fast config failed" |
| 绑定 / 解绑 | bind / unbind | 已绑定设备 = bound device |
| 配对 | pair / pairing | |
| 连接 / 断开 / 已连接 / 未连接 | connect / disconnect / connected / disconnected | |
| 扫描 | scan | |
| 蓝牙 | Bluetooth | BT / BLE unchanged |
| 下发 | send (to the device) | e.g. "app sends X to the device" |
| 上报 | report (from the device) | |
| 同步 | sync | |
| 传输 | transfer | 文件传输 = file transfer |
| 接入方 | integrator | |
| 打包态 | packaged (AAR) mode | |
| 联调 | integration test | |

## Health / sport
| 中文 | English |
|---|---|
| 心率 | heart rate |
| 血氧 | SpO2 |
| 压力 | stress |
| 血糖 | blood glucose |
| 生理期 / 月经 | menstrual cycle |
| 睡眠 / 科学睡眠 | sleep / scientific sleep |
| 步数 | steps |
| 卡路里 | calories |
| 距离 | distance |
| 目标 | goal |
| 三环 | three rings |
| 运动 | sport (运动类型 sport type, 运动模式 sport mode, 运动小结 sport summary, 跑步计划 running plan) |
| 实时数据 | real-time data |
| 数据交换 | data exchange |
| 抬腕 | wrist lift |

## Features / reminders
| 中文 | English |
|---|---|
| 走动提醒 | walk reminder |
| 久坐提醒 | sedentary reminder |
| 吃药提醒 | medication reminder |
| 喝水提醒 | drink-water reminder |
| 日程提醒 | schedule reminder |
| 任务清单 | task list |
| 消息通知 | message notification |
| 通知中心 | notification center |
| 来电 | incoming call |
| 快捷回复 | quick reply |
| 红点 | unread red dot |
| 世界时钟 | world clock |
| 天气 | weather |
| 闹钟 | alarm |
| 联系人 / 常用联系人 | contacts / frequently used contacts |
| 勿扰 | Do Not Disturb |
| 恢复出厂 | factory reset |
| 重启 | reboot |
| 查找手机 | find phone |
| 小程序 | applet |
| 提示音 | prompt sound |
| 歌单 | playlist |
| 日志 | log |

## Generic words
| 中文 | English |
|---|---|
| 设置 | set (verb) / settings (noun) |
| 获取 | get |
| 查询 | query |
| 操作 | operation |
| 成功 / 失败 / 完成 / 取消 | succeeded / failed / done / cancel |
| 开 / 关 / 开关 | on / off / switch |
| 有效 / 无效 | valid / invalid |
| 预留 | reserved |
| 属性 / 类型 / 说明 | Field / Type / Description (KDoc table headers) |
| 单位 | unit |
| 请求 / 响应 | request / response |
| 城市 | city |
| 备注 | remarks |
| 注 / 注意 | Note |
| 说明 | description |

## Sample data sent to the watch (translate)
北京 Beijing · 伦敦 London · 纽约 New York · 东京 Tokyo · 深圳 Shenzhen · 阿司匹林 Aspirin ·
提醒事项1 "Reminder 1" · 记得完成工作 "Remember to finish work" · 任务提醒$taskId "Task reminder $taskId".

## App brand names (MessageNoticeVC and friends)
抖音 Douyin (a separate TikTok entry exists) · 微信 WeChat · 淘宝 Taobao · 钉钉 DingTalk · 支付宝 Alipay ·
今日头条 Toutiao · 天猫 Tmall · 京东 JD · 拼多多 Pinduoduo · 百度 Baidu · 美团 Meituan · 饿了么 Ele.me · 滴滴 DiDi ·
短信 SMS · 日历 Calendar · 邮件 Mail · 未接来电 Missed call.

## Keep in Chinese (never translate)
The `"中文"` language-switch label and every `{language: 1, name: "中文"}` entry (language code 1 = Chinese).
