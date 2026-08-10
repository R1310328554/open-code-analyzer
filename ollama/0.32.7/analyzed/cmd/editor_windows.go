//go:build windows

// Windows 平台下 cmd 包的默认外部编辑器配置。
package cmd

// defaultEditor 为未设置 OLLAMA_EDITOR/EDITOR 时 Windows 默认使用的编辑器命令。
const defaultEditor = "edit"
