# Coworker in Ears

耳机连接式的语音记忆与免手持数字员工项目。

## What We Are Building

首期只做 `Android`，只支持 `Huawei`、`Lenovo`、`Oppo`、`Honor` 耳机。

核心体验是：

- 耳机按键或手机快捷操作触发录音
- 自定义触发词在会话内触发录音
- 录音后转写、摘要、结构化
- 记忆以 Markdown 作为 source of truth 存储
- 记忆可检索、可删除、可导出

## Project Docs

- [产品范围与 MVP 边界](docs/product-scope.md)
- [Markdown 记忆格式设计](docs/memory-format.md)

## Current Principles

- 默认本地优先
- 默认会话级触发词
- 默认用户可控
- 默认不做 24/7 常驻监听
- 默认只允许最小权限工具调用
- 默认保留审计和删除能力
