//go:build windows
// +build windows

package positions

// Windows 平台 positions 原子写盘：先写 *-new 临时文件再 os.Rename 替换目标。
// renameio 不支持 Windows，此为 fallback；权限与 Unix 版 positionFileMode 一致。

import (
	"os"
	"path/filepath"

	yaml "gopkg.in/yaml.v2"
)

// Windows 专用 positions 持久化：YAML 序列化后写临时文件再 rename 覆盖。
// writePositionFile is a fall back for Windows because renameio does not support Windows.
// See https://github.com/google/renameio#windows-support
func writePositionFile(filename string, positions map[string]string) error {
	buf, err := yaml.Marshal(File{
		Positions: positions,
	})
	if err != nil {
		return err
	}

// 规范化目标路径，临时文件名为 target-new。
	target := filepath.Clean(filename)
	temp := target + "-new"

	err = os.WriteFile(temp, buf, os.FileMode(positionFileMode))
	if err != nil {
		return err
	}

	return os.Rename(temp, target)
}
