//go:build windows || darwin

// version 包暴露 Ollama 桌面应用的构建版本字符串。
package version

// Version 当前应用版本，构建时由 ldflags 注入；默认 "0.0.0" 供开发模式使用。
var Version string = "0.0.0"
