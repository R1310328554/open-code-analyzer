// Package backend 为 Agent 文件操作提供文件系统抽象。
package backend

// FileInfo 文件元信息（名称、大小、是否目录、修改时间）。
type FileInfo struct {
	Name    string `json:"name"`
	Size    int64  `json:"size"`
	IsDir   bool   `json:"is_dir"`
	ModTime string `json:"mod_time"`
}

// Backend 文件系统操作接口（读/写/编辑/Glob/Grep/Stat 等）。
type Backend interface {
	Read(path string) (string, error)
	Write(path, content string) error
	Edit(path, old, new string) error
	Glob(pattern string) ([]string, error)
	Grep(pattern, path string) (string, error)
	Stat(path string) (*FileInfo, error)
	Mkdir(path string) error
	Remove(path string) error
	List(dir string) ([]FileInfo, error)
}

// Shell Shell 命令执行接口（同步与流式）。
type Shell interface {
	Execute(command string) (string, error)
	ExecuteStreaming(command string) (<-chan string, error)
}

// MultiModalReader 可选接口：按 offset/limit 读字节并推断 MIME
// to support reading with offset/limit and multi-modal content detection.
type MultiModalReader interface {
	ReadBytes(path string, offset, limit int64) ([]byte, error)
	MimeType(path string) string // MimeType 返回内容类型提示（如 text/plain、image/png）
}
