package com.coworkerinears.feature.home

import com.coworkerinears.app.AppLanguage
import com.coworkerinears.core.data.catalog.DefaultEarphoneCatalog
import com.coworkerinears.core.data.memory.MemoryMarkdownSerializer
import com.coworkerinears.core.data.resolver.DefaultEarphoneBrandResolver
import com.coworkerinears.core.data.trigger.InMemorySessionScopedHotwordDetector
import com.coworkerinears.core.domain.brand.EarphoneBrand
import com.coworkerinears.core.domain.device.EarphoneDeviceSnapshot
import com.coworkerinears.core.domain.memory.MemoryActionItem
import com.coworkerinears.core.domain.memory.MemorySamples
import com.coworkerinears.core.domain.trigger.EarphoneTriggerMode
import com.coworkerinears.feature.compatibility.EarphoneCompatibilityNotes
import com.coworkerinears.feature.trigger.TriggerCoordinator

object HomeStateFactory {
    fun create(language: AppLanguage = AppLanguage.EN): HomeUiState {
        val sampleEntry = localizedSampleEntry(language)
        val coordinator = TriggerCoordinator(
            brandResolver = DefaultEarphoneBrandResolver,
            hotwordDetector = InMemorySessionScopedHotwordDetector(),
        )
        val activationPlan = coordinator.buildActivationPlan(
            snapshot = EarphoneDeviceSnapshot(
                advertisedName = "HUAWEI FreeBuds Pro 3",
                manufacturerName = "HUAWEI",
                modelName = "FreeBuds Pro 3",
                bondedTags = setOf("freebuds", "buds"),
                supportsMediaKeyEvents = true,
                supportsHandsFreeProfile = true,
                isCurrentlyConnected = true,
            ),
        )

        return HomeUiState(
            heroEyebrow = if (language == AppLanguage.ZH) "Android MVP 控制台" else "Android MVP Console",
            heroTitle = "Coworker in Ears",
            heroSubtitle = if (language == AppLanguage.ZH) {
                "一个本地优先的耳机控制界面，用来完成记录、回想和轻量 Agent 动作。"
            } else {
                "A local-first earphone control surface for capture, recall, and lightweight agent actions."
            },
            releaseStatus = if (language == AppLanguage.ZH) "先做手动模式" else "Manual mode first",
            releaseSummary = if (language == AppLanguage.ZH) {
                "当前版本有意把范围收敛到四个品牌、三种触发通道和 Markdown 记忆，保证产品先可交付。"
            } else {
                "The current release intentionally narrows scope to four brands, three trigger channels, and Markdown memory so the product stays shippable."
            },
            readinessScore = 0.72f,
            readinessStats = listOf(
                ConsoleStatUiModel(
                    label = if (language == AppLanguage.ZH) "品牌范围" else "Brand scope",
                    value = "4",
                    supportingText = if (language == AppLanguage.ZH) "华为、联想、OPPO、荣耀" else "Huawei, Lenovo, OPPO, Honor",
                ),
                ConsoleStatUiModel(
                    label = if (language == AppLanguage.ZH) "触发路径" else "Trigger paths",
                    value = "3",
                    supportingText = if (language == AppLanguage.ZH) "媒体键、按住说话、会话触发词" else "Media key, PTT, session hotword",
                ),
                ConsoleStatUiModel(
                    label = if (language == AppLanguage.ZH) "记忆来源" else "Memory source",
                    value = "MD",
                    supportingText = if (language == AppLanguage.ZH) "可读可审计的 Markdown 归档" else "Human-readable Markdown archive",
                ),
                ConsoleStatUiModel(
                    label = if (language == AppLanguage.ZH) "当前模式" else "Current mode",
                    value = if (language == AppLanguage.ZH) "本地" else "Local",
                    supportingText = if (language == AppLanguage.ZH) "云同步前先完成手动采集" else "Manual capture before cloud sync",
                ),
            ),
            supportedBrands = DefaultEarphoneCatalog.profiles.map { profile ->
                BrandSupportUiModel(
                    name = localizedBrandName(profile.brand, language),
                    supportState = if (language == AppLanguage.ZH) "MVP 计划支持" else "Planned for MVP",
                    notes = localizedBrandNote(profile.brand, language),
                    triggerModes = profile.capability.supportedTriggerModes
                        .sortedBy { it.ordinal }
                        .map { localizedTriggerDisplayName(it, language) },
                )
            },
            triggerChannels = activationPlan.preferredModes
                .sortedBy { it.ordinal }
                .map { mode ->
                    TriggerChannelUiModel(
                        title = localizedTriggerDisplayName(mode, language),
                        availability = if (mode == EarphoneTriggerMode.SESSION_HOTWORD) {
                            if (language == AppLanguage.ZH) "仅会话内" else "Session only"
                        } else {
                            if (language == AppLanguage.ZH) "MVP 就绪" else "MVP ready"
                        },
                        summary = localizedTriggerSummary(mode, language),
                    )
                },
            memoryPathHint = MemoryMarkdownSerializer.suggestedRelativePath(sampleEntry),
            memoryHeadline = if (language == AppLanguage.ZH) {
                "Markdown 继续作为事实源，保证采集历史可检查、可导出、也易于比对。"
            } else {
                "Markdown remains the source of truth so capture history stays inspectable, exportable, and easy to diff."
            },
            memoryPreview = MemoryMarkdownSerializer.serialize(sampleEntry)
                .lineSequence()
                .take(18)
                .joinToString(separator = "\n"),
            guardrails = if (language == AppLanguage.ZH) {
                listOf(
                    "MVP 不做 24/7 常驻触发词",
                    "高风险 Agent 动作必须二次确认",
                    "删除和导出必须始终是一等能力",
                    "触发词仅限会话内，不承诺系统级常驻唤醒。",
                )
            } else {
                listOf(
                    "No 24/7 always-on hotword in MVP",
                    "High-risk agent actions require a second confirmation",
                    "Delete and export stay first-class product paths",
                    EarphoneCompatibilityNotes.sessionHotwordLimitations,
                )
            },
            nextActions = if (language == AppLanguage.ZH) {
                listOf(
                    ActionPanelUiModel(
                        title = "接入真实耳机事件",
                        detail = "把媒体键和蓝牙设备快照接到现有的 trigger coordinator 上。",
                        actionHint = "优先级：硬件验证",
                    ),
                    ActionPanelUiModel(
                        title = "替换为真实转写提供方",
                        detail = "把启发式 transcript structuring 替换成真实 ASR 和结构化提取。",
                        actionHint = "优先级：采集质量",
                    ),
                    ActionPanelUiModel(
                        title = "准备云端迁移",
                        detail = "本地脚本稳定后，把环境要求同步到线上 Codex 环境。",
                        actionHint = "优先级：可复现构建",
                    ),
                )
            } else {
                listOf(
                    ActionPanelUiModel(
                        title = "Wire real earphone events",
                        detail = "Connect media button and Bluetooth snapshots to the existing trigger coordinator.",
                        actionHint = "Priority: hardware validation",
                    ),
                    ActionPanelUiModel(
                        title = "Swap in real transcript providers",
                        detail = "Replace heuristic transcript shaping with actual ASR and structured extraction.",
                        actionHint = "Priority: capture quality",
                    ),
                    ActionPanelUiModel(
                        title = "Prepare cloud migration",
                        detail = "Mirror local environment expectations into the online Codex environment once local scripts are stable.",
                        actionHint = "Priority: reproducible builds",
                    ),
                )
            },
            debugChecklist = if (language == AppLanguage.ZH) {
                listOf(
                    "Gradle wrapper 必须保留在仓库内",
                    "SDK 路径应保持本地且不进入版本控制",
                    "会话触发词绝不能被表述成系统级常驻监听",
                    "即使后端未接完，主面板也要保持可读可演示",
                )
            } else {
                listOf(
                    "Gradle wrapper is expected in-repo",
                    "SDK path should stay local and outside source control",
                    "Session hotword must never be framed as system-wide listening",
                    "Main dashboard should remain readable without backend wiring",
                )
            },
        )
    }

    private fun localizedSampleEntry(language: AppLanguage) = when (language) {
        AppLanguage.ZH -> MemorySamples.kickoffEntry().copy(
            title = "MVP 启动纪要",
            summary = "首期 Android MVP 聚焦四个耳机品牌、会话级触发词和 Markdown 记忆。",
            rawTranscript = """
                用户：首发阶段先只做 Android。
                助手：第一期先支持华为、联想、OPPO 和荣耀。
                用户：记忆先写成 Markdown，这样更方便检查和导出。
            """.trimIndent(),
            keyEntities = listOf("Android", "华为", "联想", "OPPO", "荣耀", "Markdown"),
            actionItems = listOf(
                MemoryActionItem("实现品牌兼容规则"),
                MemoryActionItem("增加会话级自定义触发词"),
                MemoryActionItem("完成 Markdown 记忆序列化"),
            ),
            notes = "触发词能力明确限定在用户主动开启的会话内，不承诺系统级 24/7 常驻唤醒。",
        )

        AppLanguage.EN -> MemorySamples.kickoffEntry()
    }

    private fun localizedBrandName(brand: EarphoneBrand, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ZH -> when (brand) {
                EarphoneBrand.HUAWEI -> "华为"
                EarphoneBrand.LENOVO -> "联想"
                EarphoneBrand.OPPO -> "OPPO"
                EarphoneBrand.HONOR -> "荣耀"
            }

            AppLanguage.EN -> brand.displayName
        }
    }

    private fun localizedBrandNote(brand: EarphoneBrand, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ZH -> when (brand) {
                EarphoneBrand.HUAWEI -> "华为耳机优先通过品牌名和产品名线索做首轮识别。"
                EarphoneBrand.LENOVO -> "联想耳机先采用保守的关键字识别，后续再用真机扩充。"
                EarphoneBrand.OPPO -> "OPPO 耳机命名随市场变化较大，当前兼容策略以名称匹配为主。"
                EarphoneBrand.HONOR -> "荣耀耳机当前使用较宽但保守的命名线索进行识别。"
            }

            AppLanguage.EN -> when (brand) {
                EarphoneBrand.HUAWEI -> "Huawei-branded devices are matched by brand and product-name hints first."
                EarphoneBrand.LENOVO -> "Lenovo support starts with conservative token matching and can be expanded by device lab validation."
                EarphoneBrand.OPPO -> "OPPO support is intentionally name-driven because accessory naming varies by market."
                EarphoneBrand.HONOR -> "Honor support uses a broad but conservative set of name hints."
            }
        }
    }

    private fun localizedTriggerDisplayName(mode: EarphoneTriggerMode, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ZH -> when (mode) {
                EarphoneTriggerMode.MEDIA_KEY -> "耳机媒体键"
                EarphoneTriggerMode.PUSH_TO_TALK -> "按住说话"
                EarphoneTriggerMode.SESSION_HOTWORD -> "会话触发词"
            }

            AppLanguage.EN -> mode.displayName
        }
    }

    private fun localizedTriggerSummary(mode: EarphoneTriggerMode, language: AppLanguage): String {
        return when (language) {
            AppLanguage.ZH -> when (mode) {
                EarphoneTriggerMode.MEDIA_KEY -> "使用已连接耳机发出的媒体按键事件来开始采集。"
                EarphoneTriggerMode.PUSH_TO_TALK -> "由用户明确按住或点击控制项后再开始录音。"
                EarphoneTriggerMode.SESSION_HOTWORD -> "只有在用户显式开启的 App 会话内，才匹配自定义触发词。"
            }

            AppLanguage.EN -> mode.description
        }
    }
}
