//go:build !windows

// editor_unix.go 定义非 Windows 平台默认外部编辑器。
package cmd

// defaultEditor 为 Unix/macOS 上打开 Modelfile 等时的默认编辑器。
const defaultEditor = "vi"
