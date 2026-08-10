// Package internal 提供 core 包内部共享辅助（多语言提示词）。
package internal

// Language 表示 Agent 提示词语言。
type Language string

const (
	LanguageEnglish Language = "en"
	LanguageChinese Language = "zh"
)

var currentLanguage Language = LanguageEnglish

// SetLanguage 设置全局提示词语言
func SetLanguage(lang Language) { currentLanguage = lang }
// GetLanguage 返回当前提示词语言
func GetLanguage() Language     { return currentLanguage }

// GetPrompt 按当前语言返回英文或中文提示
func GetPrompt(en, zh string) string {
	if currentLanguage == LanguageChinese {
		return zh
	}
	return en
}

var (
	DefaultSystemPrompt = GetPrompt(
		"You are a helpful assistant. Use available tools to accomplish tasks.",
		"你是一个有用的助手。使用可用工具完成任务。",
	)
	TransferPrompt = GetPrompt(
		"You can transfer to the following agents: ",
		"你可以转移到以下助手：",
	)
	ExitPrompt = GetPrompt(
		"Say 'FINISH' when the task is complete.",
		"完成任务后请说'完成'。",
	)
)

// DefaultSystemPrompt/TransferPrompt/ExitPrompt 随 Language 切换中英文。
