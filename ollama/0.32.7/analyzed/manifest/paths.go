// 模型 manifest 与 blob 存储路径解析。
package manifest

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"regexp"
	"strings"

	"github.com/ollama/ollama/envconfig"
	"github.com/ollama/ollama/types/model"
)

// ErrInvalidDigestFormat 表示 digest 不符合 sha256 格式。
var ErrInvalidDigestFormat = errors.New("invalid digest format")

// Path 返回并确保 models/manifests 目录存在。
func Path() (string, error) {
	path := filepath.Join(envconfig.Models(), "manifests")
	if err := os.MkdirAll(path, 0o755); err != nil {
		return "", fmt.Errorf("%w: ensure path elements are traversable", err)
	}

	return path, nil
}

// PathForName 返回指定模型名的 manifest 文件路径。
// PathForName returns the path to the manifest file for a specific model name.
func PathForName(n model.Name) (string, error) {
	if !n.IsValid() {
		return "", os.ErrNotExist
	}

	manifests, err := Path()
	if err != nil {
		return "", err
	}

	return filepath.Join(manifests, n.Filepath()), nil
}

// BlobsPath 校验 digest 并返回 models/blobs 下 blob 路径。
func BlobsPath(digest string) (string, error) {
	// 仅接受 sha256: 或 sha256- 前缀的 64 位十六进制 digest。
	// only accept actual sha256 digests
	pattern := "^sha256[:-][0-9a-fA-F]{64}$"
	re := regexp.MustCompile(pattern)

	if digest != "" && !re.MatchString(digest) {
		return "", ErrInvalidDigestFormat
	}

	digest = strings.ReplaceAll(digest, ":", "-")
	path := filepath.Join(envconfig.Models(), "blobs", digest)
	dirPath := filepath.Dir(path)
	if digest == "" {
		dirPath = path
	}

	if err := os.MkdirAll(dirPath, 0o755); err != nil {
		return "", fmt.Errorf("%w: ensure path elements are traversable", err)
	}

	return path, nil
}

// PruneDirectory 递归删除空目录（跳过符号链接目录）。
// PruneDirectory removes empty directories recursively.
func PruneDirectory(path string) error {
	info, err := os.Lstat(path)
	if err != nil {
		return err
	}

	if info.IsDir() && info.Mode()&os.ModeSymlink == 0 {
		entries, err := os.ReadDir(path)
		if err != nil {
			return err
		}

		for _, entry := range entries {
			if err := PruneDirectory(filepath.Join(path, entry.Name())); err != nil {
				return err
			}
		}

		entries, err = os.ReadDir(path)
		if err != nil {
			return err
		}

		if len(entries) > 0 {
			return nil
		}

		return os.Remove(path)
	}

	return nil
}
