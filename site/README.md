# Pitch Site

这是一个零依赖静态展示页，用来向评委、投资人或合作伙伴展示 `Coworker in Ears` 的产品愿景、当前 MVP 与未来路线。

## 预览方式

直接双击打开以下文件即可：

- `site/index.html`

如果需要本地起一个简单静态服务，也可以在仓库根目录运行：

```powershell
cd site
python -m http.server 8080
```

然后访问：

- `http://localhost:8080`

## 页面结构

- `index.html`: 页面骨架与内容
- `styles.css`: 视觉系统与响应式布局
- `script.js`: 场景切换与评委打分模拟器