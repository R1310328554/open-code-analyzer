package skill

// filesystem_backend.go — 基于 OS 文件系统的技能定义读取后端。


import (
	"os"
	"path/filepath"
)

// OSFileSystemBackend 用本地文件系统实现 FileSystemBackend。
type OSFileSystemBackend struct {
	baseDir string
}

// NewOSFileSystemBackend 以 baseDir 为根目录创建后端。
func NewOSFileSystemBackend(baseDir string) *OSFileSystemBackend {
	return &OSFileSystemBackend{baseDir: baseDir}
}

// Read 读取相对 path 的技能 Markdown 全文。
func (b *OSFileSystemBackend) Read(path string) (string, error) {
	data, err := os.ReadFile(b.resolve(path))
	if err != nil {
		return "", err
	}
	return string(data), nil
}

// List 列出 baseDir 下全部 .md 技能文件名。
func (b *OSFileSystemBackend) List() ([]string, error) {
	entries, err := os.ReadDir(b.baseDir)
	if err != nil {
		return nil, err
	}
	var names []string
	for _, e := range entries {
		if !e.IsDir() && filepath.Ext(e.Name()) == ".md" {
			names = append(names, e.Name())
		}
	}
	return names, nil
}

// Exists 判断技能文件是否存在。
func (b *OSFileSystemBackend) Exists(path string) bool {
	_, err := os.Stat(b.resolve(path))
	return err == nil
}

// resolve 将相对路径拼接到 baseDir。
func (b *OSFileSystemBackend) resolve(path string) string {
	if b.baseDir == "" {
		return path
	}
	return b.baseDir + "/" + path
}

// baseDir 为空时 path 按原样使用，便于测试或绝对路径场景。
