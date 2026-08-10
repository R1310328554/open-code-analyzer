package explorer

// download 处理 dataobj explorer 的文件下载 HTTP 接口：
// 从对象存储按 query 参数 file 流式返回原始对象字节。

import (
	"fmt"
	"io"
	"net/http"
	"path"

	"github.com/go-kit/log/level"
)

func (s *Service) handleDownload(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	filename := r.URL.Query().Get("file")
	if filename == "" {
		http.Error(w, "file parameter is required", http.StatusBadRequest)
		return
	}

	attrs, err := s.bucket.Attributes(r.Context(), filename)
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to get file attributes", "file", filename, "err", err)
		http.Error(w, fmt.Sprintf("failed to get file: %v", err), http.StatusInternalServerError)
		return
	}

	reader, err := s.bucket.Get(r.Context(), filename)
	if err != nil {
		level.Error(s.logger).Log("msg", "failed to get file", "file", filename, "err", err)
		http.Error(w, fmt.Sprintf("failed to get file: %v", err), http.StatusInternalServerError)
		return
	}
	defer reader.Close()

	w.Header().Set("Content-Type", "application/octet-stream")
	w.Header().Set("Content-Disposition", fmt.Sprintf(`attachment; filename="%s"`, path.Base(filename)))
	w.Header().Set("Content-Length", fmt.Sprintf("%d", attrs.Size))

	if _, err := io.Copy(w, reader); err != nil {
		level.Error(s.logger).Log("msg", "failed to stream file", "file", filename, "err", err)
		return
	}
}
// 下载接口仅支持 GET，Content-Length 取自 bucket Attributes 的 Size。
