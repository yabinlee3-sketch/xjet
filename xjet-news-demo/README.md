# XJet News Demo（目录骨架说明）

这个 Demo 的包结构刻意按 **XDroid 官方 Demo（`cn.droidlover.xdroidmvp.demo`）** 的归类方式来组织，
让你开发新应用时可以直接照着这个骨架放文件，不用再想怎么分目录。

## 目录对照

| XDroid Demo 目录 | XJet 新闻 Demo 目录 | 放什么 |
| --- | --- | --- |
| `App.java` | `app/` | 应用入口：初始化 XJet、注册路由 |
| `kit/AppKit.java` | `kit/` | 应用级常量 / 小工具（如接口地址） |
| `model/` | `model/` | 数据模型（`NewsItem`） |
| `net/Api.java + GankService.java` | `net/` | 数据仓库（网络 + Repository + 离线兜底） |
| `present/PBasePager.java` | `present/` | 页面状态控制器（MVVM 里对应 ViewModel） |
| `ui/` | `ui/` | Activity + Compose 页面（列表页、详情页、Screen） |
| `adapter/` | `adapter/` | 列表项组件（对应 RecyclerView Adapter） |
| `widget/StateView.java` | 无（框架自带） | 四态/通用 UI 由 `XJetStateBox` 提供，不需要手写 |

## 推荐新增业务的做法

- 加一个页面：在 `ui/` 放 `XxxActivity` + `XxxScreen`，在 `present/` 放 `XxxViewModel`。
- 加一个接口/数据源：在 `net/` 加 Repository，在 `model/` 加对应 Model。
- 列表页复用：在 `adapter/` 加列表项组件。
- 应用级常量/工具：放 `kit/`。

这样“页面 → 状态 → 数据 → 模型”的入口是固定的，新业务直接往对应目录填即可。
