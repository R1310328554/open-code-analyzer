// 字节级张量变换：非 MLX 路径的 repack、标量与 expert 堆叠。
package create

import (
	"bytes"
	"fmt"
	"io"

	"github.com/ollama/ollama/x/safetensors"
)

// applyByteTransform 仅用字节级操作从源张量生成 TensorSpec 输出；
// decode_fp8 与量化由 MLX writer 路径单独处理。
// applyByteTransform produces a TensorSpec's output tensor from its resolved
// source tensors using only byte-level (non-MLX) operations. The MLX transform
// (decode_fp8) and quantization are handled separately by the MLX writer path.
func applyByteTransform(ts TensorSpec, sources []*safetensors.TensorData) (*safetensors.TensorData, error) {
	switch ts.Transform {
	case TransformNone:
		if len(sources) != 1 {
			return nil, fmt.Errorf("transform none expects 1 source, got %d", len(sources))
		}
		return sources[0].WithName(ts.Name), nil

	case TransformRepackFP4, TransformRelabelU8:
		// 二者仅重标 header（dtype，fp4 repack 还改末维）；字节不变，复用 reader。
		// Both relabel the header (dtype, and for the fp4 repack the last
		// dimension); the bytes are unchanged, so the reader is reused.
		if len(sources) != 1 {
			return nil, fmt.Errorf("transform %s expects 1 source, got %d", ts.Transform, len(sources))
		}
		td := sources[0].WithName(ts.Name)
		if ts.OutDtype != "" {
			td.Dtype = ts.OutDtype
		}
		if ts.OutShape != nil {
			td.Shape = append([]int32(nil), ts.OutShape...)
		}
		return td, nil

	case TransformScalarF32:
		if len(sources) != 1 {
			return nil, fmt.Errorf("transform scalar_f32 expects 1 source, got %d", len(sources))
		}
		return validateScalarFloat32TensorData(sources[0], ts.Name)

	case TransformReciprocalF32:
		if len(sources) != 1 {
			return nil, fmt.Errorf("transform reciprocal_f32 expects 1 source, got %d", len(sources))
		}
		return invertScalarFloat32TensorData(sources[0], ts.Name)

	case TransformStackExperts:
		return stackExpertTensors(ts.Name, ts.OutDtype, ts.OutShape, sources)

	default:
		return nil, fmt.Errorf("transform %q requires the MLX writer path", ts.Transform)
	}
}

// stackExpertTensors 按给定顺序拼接各 expert 张量为 [experts,...]；
// 行主序布局下堆叠字节即各 expert 块首尾相接。
// stackExpertTensors concatenates per-expert tensors (in the given order) into
// one [experts, ...] tensor. Row-major layout means the stacked bytes are
// exactly the per-expert byte blocks back to back.
func stackExpertTensors(name, dtype string, shape []int32, sources []*safetensors.TensorData) (*safetensors.TensorData, error) {
	if len(sources) == 0 {
		return nil, fmt.Errorf("stack_experts expects at least one source")
	}
	var buf bytes.Buffer
	for i, s := range sources {
		if s.Dtype != sources[0].Dtype {
			return nil, fmt.Errorf("stack_experts source %d dtype %s != %s", i, s.Dtype, sources[0].Dtype)
		}
		if _, err := io.Copy(&buf, s.Reader()); err != nil {
			return nil, fmt.Errorf("stack_experts read source %d (%s): %w", i, s.Name, err)
		}
	}
	return safetensors.NewTensorDataFromBytes(name, dtype, append([]int32(nil), shape...), buf.Bytes()), nil
}
