// Ollama 模型 manifest 解析、写入与层清理。
package manifest

import (
	"crypto/sha256"
	"encoding/hex"
	"encoding/json"
	"fmt"
	"io"
	"log/slog"
	"os"
	"path/filepath"

	"github.com/ollama/ollama/types/model"
)

// Manifest 表示 Docker v2 风格的 Ollama 模型 manifest。
type Manifest struct {
	SchemaVersion int     `json:"schemaVersion"`
	MediaType     string  `json:"mediaType"`
	Config        Layer   `json:"config"`
	Layers        []Layer `json:"layers"`

	filepath string
	fi       os.FileInfo
	digest   string
}

// Size 返回 config 与各 layer 字节总和。
func (m *Manifest) Size() (size int64) {
	for _, layer := range append(m.Layers, m.Config) {
		size += layer.Size
	}

	return
}

// Digest 返回 manifest JSON 内容的 sha256 十六进制摘要。
func (m *Manifest) Digest() string {
	return m.digest
}

// FileInfo 返回 manifest 文件的 os.FileInfo。
func (m *Manifest) FileInfo() os.FileInfo {
	return m.fi
}

// ReadConfigJSON 按 configPath 名称查找 JSON 层并反序列化。
// ReadConfigJSON reads and unmarshals a config layer as JSON.
func (m *Manifest) ReadConfigJSON(configPath string, v any) error {
	for _, layer := range m.Layers {
		if layer.MediaType == "application/vnd.ollama.image.json" && layer.Name == configPath {
			blobPath, err := BlobsPath(layer.Digest)
			if err != nil {
				return err
			}
			data, err := os.ReadFile(blobPath)
			if err != nil {
				return err
			}
			return json.Unmarshal(data, v)
		}
	}
	return fmt.Errorf("config %q not found in manifest", configPath)
}

// Remove 删除 manifest 文件并 prune 空目录。
func (m *Manifest) Remove() error {
	if err := os.Remove(m.filepath); err != nil {
		return err
	}

	manifests, err := Path()
	if err != nil {
		return err
	}

	return PruneDirectory(manifests)
}

// RemoveLayers 删除仅被本 manifest 引用的 orphan blob。
func (m *Manifest) RemoveLayers() error {
	ms, err := Manifests(true)
	if err != nil {
		return err
	}

	// 收集其他 manifest 仍引用的 digest 集合。
	// Build set of digests still in use by other manifests
	inUse := make(map[string]struct{})
	for _, other := range ms {
		for _, layer := range append(other.Layers, other.Config) {
			if layer.Digest != "" {
				inUse[layer.Digest] = struct{}{}
			}
		}
	}

	// 删除未被任何 manifest 引用的层 blob。
	// Remove layers not used by any other manifest
	for _, layer := range append(m.Layers, m.Config) {
		if layer.Digest == "" {
			continue
		}
		if _, used := inUse[layer.Digest]; used {
			continue
		}
		blob, err := BlobsPath(layer.Digest)
		if err != nil {
			return err
		}
		if err := os.Remove(blob); os.IsNotExist(err) {
			slog.Debug("layer does not exist", "digest", layer.Digest)
		} else if err != nil {
			return err
		}
	}

	return nil
}

// ParseNamedManifest 按完全限定模型名读取并解析 manifest。
func ParseNamedManifest(n model.Name) (*Manifest, error) {
	if !n.IsFullyQualified() {
		return nil, model.Unqualified(n)
	}

	manifests, err := Path()
	if err != nil {
		return nil, err
	}

	p := filepath.Join(manifests, n.Filepath())

	var m Manifest
	f, err := os.Open(p)
	if err != nil {
		return nil, err
	}
	defer f.Close()

	fi, err := f.Stat()
	if err != nil {
		return nil, err
	}

	sha256sum := sha256.New()
	if err := json.NewDecoder(io.TeeReader(f, sha256sum)).Decode(&m); err != nil {
		return nil, err
	}

	m.filepath = p
	m.fi = fi
	m.digest = hex.EncodeToString(sha256sum.Sum(nil))

	return &m, nil
}

// WriteManifest 将 manifest 写入模型名对应路径。
func WriteManifest(name model.Name, config Layer, layers []Layer) error {
	manifests, err := Path()
	if err != nil {
		return err
	}

	p := filepath.Join(manifests, name.Filepath())
	if err := os.MkdirAll(filepath.Dir(p), 0o755); err != nil {
		return err
	}

	f, err := os.Create(p)
	if err != nil {
		return err
	}
	defer f.Close()

	m := Manifest{
		SchemaVersion: 2,
		MediaType:     "application/vnd.docker.distribution.manifest.v2+json",
		Config:        config,
		Layers:        layers,
	}

	return json.NewEncoder(f).Encode(m)
}

// Manifests 扫描 manifests 目录，返回所有有效 manifest 映射。
func Manifests(continueOnError bool) (map[model.Name]*Manifest, error) {
	manifests, err := Path()
	if err != nil {
		return nil, err
	}

	// TODO(mxyng): use something less brittle
	matches, err := filepath.Glob(filepath.Join(manifests, "*", "*", "*", "*"))
	if err != nil {
		return nil, err
	}

	ms := make(map[model.Name]*Manifest)
	for _, match := range matches {
		fi, err := os.Stat(match)
		if err != nil {
			return nil, err
		}

		if !fi.IsDir() {
			rel, err := filepath.Rel(manifests, match)
			if err != nil {
				if !continueOnError {
					return nil, fmt.Errorf("%s %w", match, err)
				}
				slog.Warn("bad filepath", "path", match, "error", err)
				continue
			}

			n := model.ParseNameFromFilepath(rel)
			if !n.IsValid() {
				if !continueOnError {
					return nil, fmt.Errorf("%s %w", rel, err)
				}
				slog.Warn("bad manifest name", "path", rel)
				continue
			}

			m, err := ParseNamedManifest(n)
			if err != nil {
				if !continueOnError {
					return nil, fmt.Errorf("%s %w", n, err)
				}
				slog.Warn("bad manifest", "name", n, "error", err)
				continue
			}

			ms[n] = m
		}
	}

	return ms, nil
}
