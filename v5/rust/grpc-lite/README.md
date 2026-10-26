# Rust Lite Topic 示例

> **注意：本示例依赖未发布的 SDK 代码。**
> crates.io 上最新的 `rocketmq` 5.0.0 不支持 Lite Topic。本工程通过 git 地址依赖
> [apache/rocketmq-clients](https://github.com/apache/rocketmq-clients) master 分支，实际使用的 commit
> 由仓库中的 `Cargo.lock` 锁定（当前为 `e90ece7a`，2026-10-25）。该代码未经正式发布，接口可能变化，生产使用前请自行评估。
> 其 Cargo 包版本号同样是 `5.0.0`，但与 crates.io 上的 5.0.0 不是同一份代码。
> 待支持 Lite Topic 的版本发布到 crates.io 后，将 `Cargo.toml` 中的依赖改为对应版本号即可。

## 前提

- 控制台创建「轻量消息」类型的一级 Topic。
- 控制台创建消费组，选择「Lite Topic 消费」模式并绑定该 Topic。
- 首次编译需要能访问 GitHub 拉取 SDK 源码；不需要安装 `protoc`。

## 示例

| 文件 | 说明 |
|:------|:------|
| `examples/lite_producer.rs` | 发送消息时指定 lite topic |
| `examples/lite_push_consumer.rs` | LitePushConsumer 绑定一级 Topic，动态订阅 lite topic 并指定起始位点 |

该版本 Rust SDK 暂未提供 LiteSimpleConsumer。

## 运行

1. 在 `src/util.rs` 中填写接入点、AccessKey、SecretKey。
2. 在示例中填写 Topic 和消费组。
3. 运行：

```bash
cargo run --example lite_push_consumer
cargo run --example lite_producer
```

## 更新 SDK 版本

`Cargo.toml` 中的依赖跟踪 master 分支（`branch = "master"`），但 Cargo 只在首次解析时取分支最新 commit，
之后始终使用 `Cargo.lock` 中记录的 commit，不会自动跟随上游。因此 `Cargo.lock` 需要提交到仓库，
保证所有人编译的是同一份 SDK 代码。

需要升级到上游 master 最新代码时：

```bash
cargo update -p rocketmq          # 将 Cargo.lock 中的 rocketmq 更新到 master 最新 commit
cargo build --examples            # 确认示例仍能编译
```

如需锁定到某个指定 commit，而不是 master 最新：

```bash
cargo update -p rocketmq --precise <commit-sha>
```

更新后同时提交 `Cargo.lock`，并同步修改上文注意事项中的 commit 与日期。
