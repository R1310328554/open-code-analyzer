// ~/.ollama/history 持久化命令历史。
package readline

import (
	"bufio"
	"errors"
	"fmt"
	"io"
	"os"
	"path/filepath"
	"strings"

	"github.com/emirpasic/gods/v2/lists/arraylist"
)

// History 管理内存历史与可选自动保存。
type History struct {
	Buf      *arraylist.List[string]
	Autosave bool
	Pos      int
	Limit    int
	Filename string
	Enabled  bool
}

// NewHistory 创建历史并从 ~/.ollama/history 加载。
func NewHistory() (*History, error) {
	h := &History{
		Buf:      arraylist.New[string](),
		Limit:    100, // resizeme
		Autosave: true,
		Enabled:  true,
	}

	err := h.Init()
	if err != nil {
		return nil, err
	}

	return h, nil
}

// Init 确定历史文件路径并读取已有条目。
func (h *History) Init() error {
	home, err := os.UserHomeDir()
	if err != nil {
		return err
	}

	path := filepath.Join(home, ".ollama", "history")
	if err := os.MkdirAll(filepath.Dir(path), 0o755); err != nil {
		return err
	}

	h.Filename = path

	f, err := os.OpenFile(path, os.O_CREATE|os.O_RDONLY, 0o600)
	if err != nil {
		if errors.Is(err, os.ErrNotExist) {
			return nil
		}
		return err
	}
	defer f.Close()

	r := bufio.NewReader(f)
	for {
		line, err := r.ReadString('\n')
		if err != nil {
			if errors.Is(err, io.EOF) {
				break
			}
			return err
		}

		line = strings.TrimSpace(line)
		if len(line) == 0 {
			continue
		}

		h.Add(line)
	}

	return nil
}

// Add 追加条目、压缩超限并可选自动保存。
func (h *History) Add(s string) {
	h.Buf.Add(s)
	h.Compact()
	h.Pos = h.Size()
	if h.Autosave {
		_ = h.Save()
	}
}

// Compact 丢弃超出 Limit 的最旧条目。
func (h *History) Compact() {
	s := h.Buf.Size()
	if s > h.Limit {
		for range s - h.Limit {
			h.Buf.Remove(0)
		}
	}
}

func (h *History) Clear() {
	h.Buf.Clear()
}

// Prev 上移历史位置并返回条目。
func (h *History) Prev() (line string) {
	if h.Pos > 0 {
		h.Pos -= 1
	}
	line, _ = h.Buf.Get(h.Pos)
	return line
}

// Next 下移历史位置并返回条目。
func (h *History) Next() (line string) {
	if h.Pos < h.Buf.Size() {
		h.Pos += 1
		line, _ = h.Buf.Get(h.Pos)
	}
	return line
}

func (h *History) Size() int {
	return h.Buf.Size()
}

// Save 原子写入历史文件（tmp+rename）。
func (h *History) Save() error {
	if !h.Enabled {
		return nil
	}

	tmpFile := h.Filename + ".tmp"

	f, err := os.OpenFile(tmpFile, os.O_CREATE|os.O_WRONLY|os.O_TRUNC|os.O_APPEND, 0o600)
	if err != nil {
		return err
	}
	defer f.Close()

	buf := bufio.NewWriter(f)
	for cnt := range h.Size() {
		line, _ := h.Buf.Get(cnt)
		fmt.Fprintln(buf, line)
	}
	buf.Flush()
	f.Close()

	if err = os.Rename(tmpFile, h.Filename); err != nil {
		return err
	}

	return nil
}
