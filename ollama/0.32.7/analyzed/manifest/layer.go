// 模型层 blob 创建、引用与生命周期管理。
package manifest

import (
	"crypto/sha256"
	"errors"
	"fmt"
	"io"
	"os"
	"time"
)

// Layer 表示 manifest 中的单层（张量/draft/配置等）。
type Layer struct {
	MediaType string `json:"mediaType"`
	Digest    string `json:"digest"`
	Size      int64  `json:"size"`
	From      string `json:"from,omitempty"`
	Name      string `json:"name,omitempty"` // 张量名称，如 text_encoder/model.embed_tokens.weight
	Status    string `json:"-"`
}

// 层媒体类型常量。
const (
	MediaTypeImageTensor = "application/vnd.ollama.image.tensor"
	MediaTypeImageDraft  = "application/vnd.ollama.image.draft"
)

// NewLayer 从 reader 流式写入 blob 存储并返回 sha256 摘要层。
func NewLayer(r io.Reader, mediatype string) (Layer, error) {
	blobs, err := BlobsPath("")
	if err != nil {
		return Layer{}, err
	}

	temp, err := os.CreateTemp(blobs, "sha256-")
	if err != nil {
		return Layer{}, err
	}
	defer temp.Close()
	defer os.Remove(temp.Name())

	sha256sum := sha256.New()
	n, err := io.Copy(io.MultiWriter(temp, sha256sum), r)
	if err != nil {
		return Layer{}, err
	}

	if err := temp.Close(); err != nil {
		return Layer{}, err
	}

	digest := fmt.Sprintf("sha256:%x", sha256sum.Sum(nil))
	blob, err := BlobsPath(digest)
	if err != nil {
		return Layer{}, err
	}

	status := "using existing layer"
	if _, err := os.Stat(blob); err != nil {
		status = "creating new layer"
		if err := os.Rename(temp.Name(), blob); err != nil {
			return Layer{}, err
		}
		if err := os.Chmod(blob, 0o644); err != nil {
			return Layer{}, err
		}
	}
	if err := touchLayer(blob); err != nil {
		return Layer{}, err
	}

	return Layer{
		MediaType: mediatype,
		Digest:    digest,
		Size:      n,
		Status:    fmt.Sprintf("%s %s", status, digest),
	}, nil
}

// NewLayerFromLayer 引用已有 digest 创建层（不复制 blob）。
func NewLayerFromLayer(digest, mediatype, from string) (Layer, error) {
	if digest == "" {
		return Layer{}, errors.New("creating new layer from layer with empty digest")
	}

	blob, err := BlobsPath(digest)
	if err != nil {
		return Layer{}, err
	}

	fi, err := os.Stat(blob)
	if err != nil {
		return Layer{}, err
	}
	if err := touchLayer(blob); err != nil {
		return Layer{}, err
	}

	return Layer{
		MediaType: mediatype,
		Digest:    digest,
		Size:      fi.Size(),
		From:      from,
		Status:    fmt.Sprintf("using existing layer %s", digest),
	}, nil
}

// touchLayer 更新 blob 访问时间，用于 LRU 清理。
func touchLayer(path string) error {
	now := time.Now()
	return os.Chtimes(path, now, now)
}

// Open 按 digest 打开 blob 文件供读取。
func (l *Layer) Open() (io.ReadSeekCloser, error) {
	if l.Digest == "" {
		return nil, errors.New("opening layer with empty digest")
	}

	blob, err := BlobsPath(l.Digest)
	if err != nil {
		return nil, err
	}

	return os.Open(blob)
}

// Remove 若无其他 manifest 引用则删除 blob。
func (l *Layer) Remove() error {
	if l.Digest == "" {
		return nil
	}

	// 忽略损坏 manifest，避免阻塞刚 orphaned 层的删除。
	// Ignore corrupt manifests to avoid blocking deletion of layers that are freshly orphaned
	ms, err := Manifests(true)
	if err != nil {
		return err
	}

	for _, m := range ms {
		for _, layer := range append(m.Layers, m.Config) {
			if layer.Digest == l.Digest {
				// 仍有 manifest 引用此层，跳过删除。
				// something is using this layer
				return nil
			}
		}
	}

	blob, err := BlobsPath(l.Digest)
	if err != nil {
		return err
	}

	return os.Remove(blob)
}
