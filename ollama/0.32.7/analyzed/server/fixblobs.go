package server

import (
	"os"
	"path/filepath"
	"strings"
)

// blob 目录迁移：将 sha256: 前缀文件名重命名为 sha256-。
// fixBlobs 遍历目录，将 blob 文件名中的 sha256: 替换为 sha256-。
// fixBlobs walks the provided dir and replaces (":") to ("-") in the file
// prefix. (e.g. sha256:1234 -> sha256-1234)
func fixBlobs(dir string) error {
	return filepath.Walk(dir, func(path string, info os.FileInfo, err error) error {
		if err != nil {
			return err
		}
		baseName := filepath.Base(path)
		typ, sha, ok := strings.Cut(baseName, ":")
		if ok && typ == "sha256" {
			newPath := filepath.Join(filepath.Dir(path), typ+"-"+sha)
			if err := os.Rename(path, newPath); err != nil {
				return err
			}
		}
		return nil
	})
}
