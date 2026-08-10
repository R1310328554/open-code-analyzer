//go:build !windows
// +build !windows

package positions

// Unix 平台 positions 原子写盘：renameio 先写临时文件再 rename，权限 0600。
// 非 Windows 构建标签，与 positions.save 周期刷盘配合保证崩溃安全。

import (
	"os"
	"path/filepath"

	renameio "github.com/google/renameio/v2"
	yaml "gopkg.in/yaml.v2"
)

func writePositionFile(filename string, positions map[string]string) error {
	buf, err := yaml.Marshal(File{
		Positions: positions,
	})
	if err != nil {
		return err
	}

	target := filepath.Clean(filename)

	return renameio.WriteFile(target, buf, os.FileMode(positionFileMode))
}
