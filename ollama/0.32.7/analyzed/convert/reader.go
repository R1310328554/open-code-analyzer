// 张量读取抽象：Tensor 接口、dtype 推断与 checkpoint 格式分发。
package convert

import (
	"errors"
	"io"
	"io/fs"
	"strings"
)

// Tensor 表示待写入 GGUF 的可读张量。
type Tensor interface {
	Name() string
	Shape() []uint64
	Kind() uint32
	SetRepacker(Repacker)
	WriteTo(io.Writer) (int64, error)
	Clone() Tensor
}

// tensorBase 实现 Tensor 的公共字段与默认方法。
type tensorBase struct {
	name     string
	shape    []uint64
	repacker Repacker
}

// Name 返回 GGUF 张量名。
func (t tensorBase) Name() string {
	return t.name
}

// Shape 返回张量维度。
func (t tensorBase) Shape() []uint64 {
	return t.shape
}

// 张量存储 dtype 常量（与 GGUF 类型码对应）。
const (
	tensorKindFP32 uint32 = iota
	tensorKindFP16
	tensorKindBF16  = 30
	tensorKindMXFP4 = 39
)

// Kind 据张量名与 shape 推断 GGUF 量化/存储类型（F32/F16/BF16 等）。
func (t tensorBase) Kind() uint32 {
	if strings.HasSuffix(t.name, ".ffn_gate_inp.weight") ||
		strings.HasSuffix(t.name, ".bias") ||
		strings.HasSuffix(t.name, ".shortconv.conv.weight") ||
		strings.HasSuffix(t.name, ".ssm_conv1d.weight") || // SSM conv kernel must be F32 for Metal
		strings.HasPrefix(t.name, "a.feature_extractor.") || // audio feature-extractor constants are read with BackendGet and must be real F32 values
		strings.HasPrefix(t.name, "a.conv1d.") || // audio SSCP conv weights are kept F32 for im2col; this likely slows audio and should be revisited
		strings.HasPrefix(t.name, "a.subsampling.") || // audio Parakeet subsampling weights are kept F32 for conv/linear stability; this likely slows audio and should be revisited
		strings.Contains(t.name, ".conv_dw.") || // audio depthwise conv weights are kept F32; this likely slows audio and should be revisited
		t.name == "token_types.weight" ||
		t.name == "v.positional_embedding_vlm" ||
		t.name == "v.position_embd.weight" ||
		t.name == "v.tile_position_embd.weight" ||
		t.name == "v.pre_tile_position_embd.weight" ||
		t.name == "v.post_tile_position_embd.weight" ||
		t.name == "s.position_embd" ||
		strings.HasSuffix(t.name, "rel_pos_h") ||
		strings.HasSuffix(t.name, "rel_pos_w") {
		// 以下张量始终保留 F32（Metal/BackendGet 等需求）。
		// these tensors are always F32
		return tensorKindFP32
	}

	switch len(t.shape) {
	case 0:
		panic("invalid tensor shape")
	case 1:
		return tensorKindFP32
	default:
		return tensorKindFP16
	}
}

// SetRepacker 设置写入前对 float32 数据的重打包函数。
func (t *tensorBase) SetRepacker(fn Repacker) {
	t.repacker = fn
}

// Repacker 在写入 GGUF 前变换张量数据与 shape。
type Repacker func(string, []float32, []uint64) ([]float32, error)

// parseTensors 按 glob 匹配 safetensors 或 PyTorch 权重并解析为 Tensor 列表。
func parseTensors(fsys fs.FS, replacer *strings.Replacer) ([]Tensor, error) {
	patterns := []struct {
		Pattern string
		Func    func(fs.FS, *strings.Replacer, ...string) ([]Tensor, error)
	}{
		{"*.safetensors", parseSafetensors},
		{"pytorch_model-*-of-*.bin", parseTorch},
		{"pytorch_model.bin", parseTorch},
		{"consolidated.*.pth", parseTorch},
	}

	for _, pattern := range patterns {
		matches, err := fs.Glob(fsys, pattern.Pattern)
		if err != nil {
			return nil, err
		}

		if len(matches) > 0 {
			return pattern.Func(fsys, replacer, matches...)
		}
	}

	return nil, errors.New("unknown tensor format")
}
