const scenarios = {
  creator: {
    title: "创作者：灵感不再逃走",
    line: "戴着耳机走路时，说一句“记一下：把播客里的那段观点写成短视频脚本”。",
    trigger: "耳机按键 / 自定义会话级触发词",
    feedback: "立即落成一条 Markdown 记忆，并自动抽出创作任务。",
    outcome: "灵感不再只是“记住了”，而是自动进入创作工作流。",
    memory: `---
id: mem_creator_001
mode: manual
title: 播客观点短视频脚本
tags: [creator, script, inspiration]
---

## Summary
把播客中的核心观点改写成 60 秒短视频脚本。

## Action Items
- [ ] 写 3 个开头钩子
- [ ] 补一个真实案例
- [ ] 今晚回家扩写成口播稿`,
  },
  founder: {
    title: "创业者：脑中的想法开始有秩序",
    line: "会后出门时，说一句“记一下：下周 demo 要突出本地优先和耳机入口，不要再讲太多模型”。",
    trigger: "手机快捷操作 / 耳机单击",
    feedback: "自动生成可检索纪要，把零散想法变成下一步行动。",
    outcome: "创始人的思考不再消散在通勤和会后碎片里。",
    memory: `---
id: mem_founder_014
mode: manual
title: Demo 叙事调整
tags: [founder, pitch, mvp]
---

## Summary
下周 demo 要少讲模型，多讲入口、控制感与本地优先。

## Action Items
- [ ] 重写开场第一页
- [ ] 增加投资人网页演示
- [ ] 强调 Android MVP 已验证`,
  },
  sales: {
    title: "销售/顾问：拜访后的价值自动沉淀",
    line: "离开客户办公室后，说一句“记一下：客户最关心合规和导出，不要先推复杂功能”。",
    trigger: "耳机按键 + 会话级触发词",
    feedback: "自动生成客户画像、顾虑清单和下一次跟进建议。",
    outcome: "每次拜访都留下可复用的销售资产，而不是只留在脑海里。",
    memory: `---
id: mem_sales_203
mode: session
title: 客户顾虑沉淀
tags: [sales, compliance, follow-up]
---

## Summary
客户优先关心合规、导出和最小部署成本。

## Action Items
- [ ] 准备合规 FAQ
- [ ] 发送导出样例
- [ ] 一周后回访采购负责人`,
  },
  student: {
    title: "学习者：把被动听见变成主动创作",
    line: "听课或走路时，说一句“记一下：这段定义我要换成自己的话，再举一个生活例子”。",
    trigger: "手机快捷操作 / 自定义会话级触发词",
    feedback: "不只保存原句，还会提醒用户做复述、类比和延伸。",
    outcome: "知识不只是被记录，而是变成更容易记住的个人表达。",
    memory: `---
id: mem_student_071
mode: manual
title: 概念复述练习
tags: [study, memory, expression]
---

## Summary
把课堂上的定义改写成自己的语言，并补一个生活例子。

## Action Items
- [ ] 今晚做一次口头复述
- [ ] 写一个生活化案例
- [ ] 明天再回听这条记忆`,
  },
};

const scoreBands = [
  {
    min: 85,
    summary: "这是一个“现在就值得继续加码”的项目：MVP 边界清楚，未来想象空间足够大。",
    tags: ["高频入口", "高留存潜力", "平台延展性"],
  },
  {
    min: 70,
    summary: "这是一个值得重点跟进的项目：先投 MVP 验证，再加速场景化扩张，会很有机会。",
    tags: ["产品清晰", "可验证路径", "增长飞轮可见"],
  },
  {
    min: 0,
    summary: "如果你还在犹豫，真正要看的不是“能不能做”，而是“用户会不会形成习惯”。这正是 Coworker 的优势。",
    tags: ["入口价值", "习惯设计", "记忆复利"],
  },
];

const scenarioTabs = document.querySelectorAll("[data-scenario]");
const scenarioTitle = document.querySelector("#scenario-title");
const scenarioLine = document.querySelector("#scenario-line");
const scenarioTrigger = document.querySelector("#scenario-trigger");
const scenarioFeedback = document.querySelector("#scenario-feedback");
const scenarioOutcome = document.querySelector("#scenario-outcome");
const memoryOutput = document.querySelector("#memory-output");

function renderScenario(key) {
  const next = scenarios[key];
  if (!next) return;

  scenarioTitle.textContent = next.title;
  scenarioLine.textContent = next.line;
  scenarioTrigger.textContent = next.trigger;
  scenarioFeedback.textContent = next.feedback;
  scenarioOutcome.textContent = next.outcome;
  memoryOutput.textContent = next.memory;

  scenarioTabs.forEach((tab) => {
    const active = tab.dataset.scenario === key;
    tab.classList.toggle("is-active", active);
    tab.setAttribute("aria-selected", String(active));
  });
}

scenarioTabs.forEach((tab) => {
  tab.addEventListener("click", () => renderScenario(tab.dataset.scenario));
});

const scoreInputs = document.querySelectorAll("[data-score]");
const scoreValue = document.querySelector("#score-value");
const scoreSummary = document.querySelector("#score-summary");
const scoreTags = document.querySelector("#score-tags");
const scoreRing = document.querySelector(".score-ring");

function renderScore() {
  const score = [...scoreInputs].reduce((sum, input) => {
    const weight = Number(input.dataset.weight || 0);
    return sum + Number(input.value) * weight;
  }, 0);

  const rounded = Math.round(score);
  const band = scoreBands.find((item) => rounded >= item.min) ?? scoreBands[scoreBands.length - 1];

  scoreValue.textContent = String(rounded);
  scoreSummary.textContent = band.summary;
  scoreTags.innerHTML = band.tags.map((tag) => `<span>${tag}</span>`).join("");
  scoreRing.style.background = `
    radial-gradient(circle at center, white 0 58%, transparent 58% 100%),
    conic-gradient(var(--accent) 0 ${rounded}%, rgba(15, 118, 110, 0.16) ${rounded}% 100%)
  `;
}

scoreInputs.forEach((input) => input.addEventListener("input", renderScore));

renderScenario("creator");
renderScore();