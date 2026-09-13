# 将本包合并到 Ab's Mod

> 修订：`2026-09-12.repository-progress.1`  
> 目标本地目录（用户之前提供）：`D:\Ab'sMod`  
> 本包不自动写入该目录，不自动提交或推送，不改变GitHub权限。

## 内容

`docs/`是完整合并后文档，包含全部器灵/故事/美术/数值/长按规格与最新状态。
`AGENTS_APPEND.md`只作为根AGENTS的合并建议，不直接覆盖原文件。
压缩包不含源码、贴图、模型、运行目录或JAR。

## 导入顺序

1. 在当前项目检查Git分支、HEAD与未提交改动；比对包内REPOSITORY_AUDIT。远端基线只是快照，不覆盖本地更先进的状态。
2. 将本包先解压到项目外临时目录，与目标docs比较；缺失文件再创建，已有文件逐项合并。
3. 特别处理STATUS：按当前实际代码核对，未提交实现也要保留并注明来源；不要仅因为包内写未实现就删除本地代码。
4. CONTINUOUS_COMBAT是主规格；COMBAT_INPUT为旧名称入口，原有链接无需同时维护两套正文。
5. 将AGENTS_APPEND相关规则合并到现有AGENTS，保留命名、双语README、许可和验证约定。
6. 只导入文档时不修改游戏代码，不宣称测试通过。提交/推送由用户明确授权后再做。

检查命令（仅供在用户电脑运行）：

```powershell
git -C "D:\Ab'sMod" status --short
git -C "D:\Ab'sMod" branch --show-current
git -C "D:\Ab'sMod" rev-parse HEAD
git -C "D:\Ab'sMod" diff --check
```

## 可直接给 Codex

```text
请读取本次附带的AbsMod文档压缩包，先确认文件完整并核对DOCUMENT_MANIFEST的SHA-256。
目标项目D:\Ab'sMod。先检查本地分支和未提交内容，与docs做差异合并，不直接覆盖。
按实际代码更新STATUS；本包采用远端main=2e42020，不能代表我的未推送代码。
把AGENTS_APPEND.md中的阅读/状态规则合并到已有AGENTS，不丢失原有开发约定。
保留PROPOSED/TBD边界、固定物品ID和历史测试的真实范围。
本次只导入/整理文档，不实现功能、不自动提交或推送。
最后报告导入文件、冲突处理、仍需确认的差异和是否改动游戏代码。
```


## 2026-09-12 导入提醒

本包 main 基线仍为 `2e420203619b5d9cf0f1af7fbc5135ff8858f34f`，没有带入游戏源码更改。
优先比较 STATUS/CURRENT_TASK/DEVELOPMENT/REPOSITORY_AUDIT 与本地版本。若本地已开发长按、连击或器灵，按实际源码和日志更新进度，不把本包的远端待办覆盖回去。
BALANCE_PROFILE.json 的候选数字及专项玩法与 9/10 包一致；不要为了文档复核重复生成贴图或改动依赖。当前源码/测试记录与包内旧状态冲突时，先记录 commit 和验证环境，再合并。
