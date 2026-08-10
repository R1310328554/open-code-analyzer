package explorer

// list 实现对象存储目录浏览 API：
// 按 path 前缀迭代 bucket，返回文件列表、子目录与父路径导航信息。

import (
	"encoding/json"
	"net/http"
	"path"
	"strings"
	"time"

	"github.com/go-kit/log/level"
)

type listResponse struct {
	Files   []fileInfo `json:"files"`
	Folders []string   `json:"folders"`
	Parent  string     `json:"parent"`
	Current string     `json:"current"`
}

// fileInfo 描述单个对象的相对名、大小与最后修改时间。
type fileInfo struct {
	Name         string    `json:"name"`
	Size         int64     `json:"size"`
	LastModified time.Time `json:"lastModified"`
}

func (s *Service) handleList(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	dir := r.URL.Query().Get("path")
	level.Debug(s.logger).Log("msg", "listing directory", "path", dir)

	files := make([]fileInfo, 0)
	folders := make([]string, 0)

	err := s.bucket.Iter(r.Context(), dir, func(name string) error {
		if name == dir {
			return nil
		}

		relativePath := strings.TrimPrefix(name, dir)
		relativePath = strings.TrimPrefix(relativePath, "/")

		if strings.HasSuffix(name, "/") {
			folderName := strings.TrimSuffix(relativePath, "/")
			if folderName != "" {
				folders = append(folders, folderName)
			}
		} else {
			attrs, err := s.bucket.Attributes(r.Context(), name)
			if err != nil {
				level.Error(s.logger).Log("msg", "failed to get object attributes", "name", name, "err", err)
				return nil
			}

			files = append(files, fileInfo{
				Name:         relativePath,
				Size:         attrs.Size,
				LastModified: attrs.LastModified.UTC(),
			})
		}
		return nil
	})
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to list objects", "err", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	parent := path.Dir(dir)
	if parent == "." || parent == "/" {
		parent = ""
	}

	current := dir
	if current == "/" {
		current = ""
	}

	resp := listResponse{
		Files:   files,
		Folders: folders,
		Parent:  parent,
		Current: current,
	}

	w.Header().Set("Content-Type", "application/json")
	if err := json.NewEncoder(w).Encode(resp); err != nil {
		level.Error(s.logger).Log("msg", "failed to encode response", "err", err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
}
// list 接口 path 为空时表示根目录，parent 在根级返回空字符串。
