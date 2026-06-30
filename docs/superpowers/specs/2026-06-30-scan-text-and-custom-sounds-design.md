# 探测文字 HUD 与自定义音效设计

## 目标

删除探测 PNG 序列、帧播放器、位置编辑器和内置自定义音频资产，保留服务端探测玩法。客户端改用持续 2 秒的顶部居中文字，并允许玩家导入 OGG、MP3 或 WAV 作为三类探测音效；没有可用自定义音效时始终安全回退到原版声音。

## 范围

保留以下服务端行为：光灵箭向上发射后触发探测、最多 3 次反弹、建筑遮挡检测、目标发光、是否探测玩家、探测范围及现有其他探测规则。

删除以下客户端行为和资源：扫描/被扫描 PNG 序列、动画资源重载监听器、帧时间线与纹理绘制、动画位置/缩放配置、动画位置编辑 Screen 和快捷键、内置扫描音效 OGG、旧 `sounds.json` 绑定及公共自定义声音注册。

## 客户端配置

`config/enhanced-bows-client.json` 只保留或新增以下展示/声音字段：

- `enableScanSounds`，默认 `true`
- `useCustomScanStartSound`，默认 `false`
- `useCustomDetectedSound`，默认 `false`
- `useCustomBounceSound`，默认 `false`
- `enableScanTextHud`，默认 `true`
- `scanTextHudY`，默认 `40`，保存时限制到非负安全范围

加载旧配置时忽略并在下一次保存时移除动画字段。Mod Menu / Cloth Config 提供文字 HUD 开关、Y 坐标、总声音开关、三个自定义声音开关，以及“打开自定义音效导入界面”按钮。

## 文字 HUD

客户端收到扫描开始消息后显示“正在探测”，收到被探测消息后显示“你已被探测”。两者均持续 40 tick，使用 `scanTextHudY`，按文字宽度在屏幕顶部水平居中。被探测提示优先于同时存在的扫描提示。状态、渲染和网络处理只存在于 `src/client`，公共入口不引用 Minecraft 客户端类。

## 声音分发与回退

服务端只决定何时产生声音提示，不直接播放受客户端配置控制的声音。扫描开始消息发给发射者，被探测消息发给首次被本次扫描发现的玩家；反弹和标记成功通过 S2C 声音提示消息发给相关追踪玩家。客户端收到消息后先检查 `enableScanSounds`。

默认原版声音为：

- 扫描开始：`SoundEvents.BLOCK_BEACON_ACTIVATE`
- 被探测：`SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP`
- 反弹：`SoundEvents.BLOCK_AMETHYST_BLOCK_HIT`
- 首次标记到目标：`SoundEvents.BLOCK_NOTE_BLOCK_PLING`

扫描、被探测、反弹三种提示按各自 `useCustom...` 开关选择自定义声音。只有声音管理器在当前已加载资源中解析到对应自定义声音事件时才播放它；否则捕获缺失或播放异常并回退到对应原版声音。标记成功始终使用原版 pling。一次探测会话对同一目标只产生一次标记成功提示，避免周期扫描造成声音刷屏。

## 自定义资源包

导入器生成：

```text
resourcepacks/EnhancedBows Custom Sounds/
  pack.mcmeta
  assets/enhanced_bows/sounds.json
  assets/enhanced_bows/sounds/custom/scan_start.ogg
  assets/enhanced_bows/sounds/custom/detected.ogg
  assets/enhanced_bows/sounds/custom/bounce.ogg
```

声音事件为：

- `enhanced_bows:custom.scan_start`
- `enhanced_bows:custom.detected`
- `enhanced_bows:custom.bounce`

`sounds.json` 使用这三个键映射到 `enhanced_bows:custom/<filename>`。`pack.mcmeta` 使用 Minecraft 1.21.1 的资源包格式 `34`。模组不自动改写玩家的资源包启用顺序；界面始终提示在资源包列表中启用 `EnhancedBows Custom Sounds`，并提供“重新加载资源”按钮调用客户端资源重载。

每次成功导入还将最终 OGG 备份到：

```text
config/enhanced_bows/custom_sounds/scan_start.ogg
config/enhanced_bows/custom_sounds/detected.ogg
config/enhanced_bows/custom_sounds/bounce.ogg
```

## 导入和 FFmpeg 转换

导入 Screen 显示扫描开始、被探测、反弹三个固定槽位。拖放位置决定目标槽位，最终文件名固定，且一次只处理对应槽位的第一个文件。

- `.ogg`：在后台直接复制到临时文件，成功后原子替换配置备份和资源包目标。
- `.mp3` / `.wav`：在后台线程通过 `ProcessBuilder` 调用 FFmpeg，参数为 `-y -i <input> -ac 1 -ar 44100 -c:a libvorbis -q:a 4 <temporary-output>`；成功后原子替换两个最终 OGG。
- 其他扩展名：客户端线程立即显示“只支持 .ogg、.mp3、.wav 音频文件”。

FFmpeg 先检查 `config/enhanced_bows/tools/ffmpeg.exe`，再用 `ffmpeg` 依赖系统 PATH。进程合并标准错误和标准输出，并在后台持续读取输出以避免管道阻塞。转换设置有限超时；超时会强制终止进程并删除临时输出。未找到 FFmpeg 时显示用户要求的完整安装提示，退出码非零、超时、空输出或 I/O 错误均显示简短错误信息且不覆盖已有可用声音。

所有文件工作在专用单线程后台执行器上。后台线程不访问 Screen 控件；结果通过 `MinecraftClient.execute(...)` 返回客户端线程，更新状态、保存对应 `useCustom... = true` 配置，并提示重新加载资源。

## 错误处理

导入采用先写临时文件、验证非空、再替换最终文件的流程。失败时保留旧文件和旧配置。资源包生成、文件复制、FFmpeg 启动或资源重载失败均转换为界面消息和日志，不向游戏线程抛出未处理异常。

## 验证

自动测试覆盖配置默认值/迁移、扩展名判断、固定目标文件名、资源包 JSON/目录生成、FFmpeg 查找顺序、命令参数和失败结果；通过可注入的进程启动边界测试转换协调逻辑，不依赖开发机一定安装 FFmpeg。

最终验证还包括：搜索确认无动画配置/加载器/PNG/MOV/旧 OGG 依赖；`gradlew test`；`gradlew build`；启动客户端检查配置界面、文字、三槽拖放、资源重载和声音回退；`gradlew runServer` 启动到可接受连接后退出，确认 dedicated server 不加载客户端 Screen、HUD 或 FFmpeg 类。
