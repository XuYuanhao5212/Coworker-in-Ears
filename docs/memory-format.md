# Markdown 记忆格式设计

## 设计目标

记忆层采用 Markdown 作为 source of truth，原因是：

- 人可读
- 便于审计
- 便于版本控制
- 便于同步与导出
- 便于后续建立索引和检索层

## 格式原则

- 一条记忆对应一个 Markdown 文件
- 文件顶部使用 YAML frontmatter 保存稳定元数据
- 正文使用明确的 sections 表达结构化内容
- 原始文本、摘要、待办、实体和标签都要可见
- 不把“记忆”隐藏在模型上下文里

## 推荐结构

```markdown
---
id: mem_20260318_001
created_at: 2026-03-18T09:30:00+08:00
source: earbud_session
mode: manual
title: 会议纪要草稿
tags:
  - meeting
  - follow-up
participants:
  - user
  - alice
  - bob
status: draft
---

# 会议纪要草稿

## Summary

今天讨论了版本范围、触发词策略和记忆格式。

## Raw Transcript

用户：我们先做 Android。
Alice：同意，先把录音和记忆闭环做稳。

## Key Entities

- Android
- 会话级触发词
- Markdown 记忆

## Action Items

- [ ] 确认首期品牌兼容范围
- [ ] 输出记忆格式模板

## Notes

触发词只在当前会话内有效，不做系统级常驻热词。
```

## 字段说明

frontmatter 建议包含：

- `id`：全局唯一 ID
- `created_at`：创建时间
- `source`：来源，例如录音会话、手动记录、导入
- `mode`：manual、session、import 等
- `title`：人类可读标题
- `tags`：标签数组
- `participants`：参与者列表
- `status`：draft、confirmed、archived

正文建议包含：

- `Summary`：可快速浏览的摘要
- `Raw Transcript`：原始转写
- `Key Entities`：关键实体
- `Action Items`：待办
- `Notes`：补充说明

## 检索建议

虽然 source of truth 是 Markdown，但我们后续应当为其配套：

- 文件索引
- 标签索引
- 时间索引
- 关键词索引

这样可以让 Markdown 既保留可读性，也保留机器检索效率。

## 后续演进

如果未来需要更强的同步或分析能力，可以在不改变 Markdown source of truth 的前提下，增加：

- sidecar JSON 元数据
- 全文索引库
- 向量索引
- 加密同步层

