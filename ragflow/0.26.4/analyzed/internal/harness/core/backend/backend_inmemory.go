// backend_inmemory.go — InMemoryBackend：内存 map 实现 Backend，用于测试与沙箱。

package backend

import (
	"fmt"
	"path/filepath"
	"strings"
	"sync"
	"time"
)

// InMemoryBackend 用内存 map 实现 Backend。
// 适用于单元测试与沙箱环境。
type InMemoryBackend struct {
	mu    sync.RWMutex
	files map[string]*memFile
}

type memFile struct {
	content string
	modTime time.Time
	isDir   bool
}

// NewInMemoryBackend 初始化根目录 ""/"." 的空后端。
func NewInMemoryBackend() *InMemoryBackend {
	root := &memFile{content: "", modTime: time.Now(), isDir: true}
	return &InMemoryBackend{files: map[string]*memFile{"": root, ".": root}}
}

// Read 读取文件内容；目录或不存在返回错误。
func (b *InMemoryBackend) Read(path string) (string, error) {
	b.mu.RLock()
	defer b.mu.RUnlock()
	f, ok := b.files[filepath.Clean(path)]
	if !ok {
		return "", fmt.Errorf("file not found: %s", path)
	}
	if f.isDir {
		return "", fmt.Errorf("is a directory: %s", path)
	}
	return f.content, nil
}

// Write 创建或覆盖文件。
func (b *InMemoryBackend) Write(path, content string) error {
	b.mu.Lock()
	defer b.mu.Unlock()
	b.files[filepath.Clean(path)] = &memFile{content: content, modTime: time.Now()}
	return nil
}

// Edit 替换文件中首次出现的 old 文本。
func (b *InMemoryBackend) Edit(path, old, new string) error {
	content, err := b.Read(path)
	if err != nil {
		return err
	}
	updated := strings.Replace(content, old, new, 1)
	if updated == content {
		return fmt.Errorf("text not found in %s", path)
	}
	return b.Write(path, updated)
}

// Glob 按 filepath.Match 模式匹配路径。
func (b *InMemoryBackend) Glob(pattern string) ([]string, error) {
	b.mu.RLock()
	defer b.mu.RUnlock()
	var matches []string
	for p := range b.files {
		if matched, _ := filepath.Match(pattern, p); matched {
			matches = append(matches, p)
		}
	}
	return matches, nil
}

// Grep 在文件中搜索子串并返回 路径:行号:内容 格式。
func (b *InMemoryBackend) Grep(pattern, path string) (string, error) {
	content, err := b.Read(path)
	if err != nil {
		return "", err
	}
	var results []string
	for i, line := range strings.Split(content, "\n") {
		if strings.Contains(line, pattern) {
			results = append(results, fmt.Sprintf("%s:%d: %s", path, i+1, line))
		}
	}
	return strings.Join(results, "\n"), nil
}

// Stat 返回文件或目录元信息。
func (b *InMemoryBackend) Stat(path string) (*FileInfo, error) {
	b.mu.RLock()
	defer b.mu.RUnlock()
	f, ok := b.files[filepath.Clean(path)]
	if !ok {
		return nil, fmt.Errorf("not found: %s", path)
	}
	return &FileInfo{Name: path, Size: int64(len(f.content)), IsDir: f.isDir, ModTime: f.modTime.Format(time.RFC3339)}, nil
}

func (b *InMemoryBackend) Mkdir(path string) error {
	b.mu.Lock()
	defer b.mu.Unlock()
	b.files[filepath.Clean(path)] = &memFile{modTime: time.Now(), isDir: true}
	return nil
}

func (b *InMemoryBackend) Remove(path string) error {
	b.mu.Lock()
	defer b.mu.Unlock()
	delete(b.files, filepath.Clean(path))
	return nil
}

func (b *InMemoryBackend) List(dir string) ([]FileInfo, error) {
	b.mu.RLock()
	defer b.mu.RUnlock()
	var results []FileInfo
	clean := filepath.Clean(dir)
	for p, f := range b.files {
		if filepath.Dir(p) == clean {
			results = append(results, FileInfo{Name: p, Size: int64(len(f.content)), IsDir: f.isDir, ModTime: f.modTime.Format(time.RFC3339)})
		}
	}
	return results, nil
}

// Execute 模拟 Shell 执行（返回占位字符串）。
func (b *InMemoryBackend) Execute(command string) (string, error) {
	return fmt.Sprintf("executed (in-memory): %s", command), nil
}

// ReadBytes 按 rune 偏移与长度读取字节切片。
func (b *InMemoryBackend) ReadBytes(path string, offset, limit int64) ([]byte, error) {
	content, err := b.Read(path)
	if err != nil {
		return nil, err
	}
	if offset < 0 {
		return nil, fmt.Errorf("negative offset %d", offset)
	}
	if limit < 0 {
		return nil, fmt.Errorf("negative limit %d", limit)
	}
	runes := []rune(content)
	if int(offset) >= len(runes) {
		return nil, fmt.Errorf("offset %d beyond content length %d", offset, len(runes))
	}
	end := int(offset) + int(limit)
	if end < int(offset) { // integer overflow
		end = len(runes)
	}
	if end > len(runes) {
		end = len(runes)
	}
	return []byte(string(runes[offset:end])), nil
}

// MimeType 按扩展名推断 MIME 类型。
func (b *InMemoryBackend) MimeType(path string) string {
	ext := strings.ToLower(filepath.Ext(path))
	switch ext {
	case ".txt", ".go", ".py", ".js", ".ts", ".html", ".css", ".md", ".json", ".xml", ".yaml", ".yml":
		return "text/plain"
	case ".png":
		return "image/png"
	case ".jpg", ".jpeg":
		return "image/jpeg"
	case ".gif":
		return "image/gif"
	case ".pdf":
		return "application/pdf"
	default:
		return "application/octet-stream"
	}
}
