//go:build windows

// Package wintray（messages）定义托盘通知与菜单的显示文案。
package wintray

// 首次运行与更新通知的标题/正文，以及托盘菜单项标题字符串。
const (
	firstTimeTitle   = "Ollama is running"
	firstTimeMessage = "Click here to get started"
	updateTitle      = "Update available"
	updateMessage    = "Ollama version %s is ready to install"

	quitMenuTitle            = "Quit Ollama"
	updateAvailableMenuTitle = "An update is available"
	updateMenuTitle          = "Restart to update"
	diagLogsMenuTitle        = "View logs"
	openUIMenuTitle          = "Open Ollama"
	settingsUIMenuTitle      = "Settings..."
)
