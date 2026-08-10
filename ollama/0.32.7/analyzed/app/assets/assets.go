//go:build windows || darwin

// assets 包嵌入并提供 Windows/macOS 桌面应用使用的图标资源。
package assets

import (
	"embed"
	"io/fs"
)

//go:embed *.ico
var icons embed.FS

// ListIcons 列出嵌入的全部 .ico 图标文件名。
func ListIcons() ([]string, error) {
	return fs.Glob(icons, "*")
}

// GetIcon 按文件名读取嵌入的图标字节内容。
func GetIcon(filename string) ([]byte, error) {
	return icons.ReadFile(filename)
}
