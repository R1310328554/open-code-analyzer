// 嵌入层工厂：从张量映射构造 dense 或量化 Embedding。
package model

import (
	"github.com/ollama/ollama/x/mlxrunner/mlx"
	"github.com/ollama/ollama/x/models/nn"
)

// MakeEmbeddingLayer 从 tensors 构造嵌入层。
// MakeEmbeddingLayer constructs an embedding layer from a tensor map.
//
// 量化张量返回 QuantizedEmbedding；否则返回 dense Embedding。
// For quantized tensors (path.weight + path.weight_scale), it returns a
// QuantizedEmbedding using the same quant metadata path that linear layers use.
// For non-quantized tensors, it returns a standard dense embedding.
func MakeEmbeddingLayer(
	tensors map[string]*mlx.Array,
	path string,
	defaultGroupSize, defaultBits int,
	defaultMode string,
	tensorQuant map[string]*TensorQuantInfo,
) nn.EmbeddingLayer {
	w := tensors[path+".weight"]
	if w == nil {
		return nil
	}

	scales := tensors[path+".weight_scale"]
	if scales != nil {
		qbiases := tensors[path+".weight_qbias"]
		groupSize, bits, mode := ResolveLinearQuantParams(
			defaultGroupSize,
			defaultBits,
			defaultMode,
			tensorQuant,
			path+".weight",
			w,
			scales,
		)

		// 检查 per-tensor global scale（NVIDIA nvfp4 双 scale）。
		// Check for per-tensor global scale (NVIDIA double-scale nvfp4).
		// ModelOpt 存为 weight_scale_2，导入映射为 weight.global_scale。
		// NVIDIA ModelOpt stores this as "weight_scale_2"; our import
		// pipeline maps it to "weight.global_scale".
		globalScale := tensors[path+".weight.global_scale"]
		if globalScale == nil {
			globalScale = tensors[path+".weight_scale_2"]
		}

		return &nn.QuantizedEmbedding{
			Weight:      w,
			Scales:      scales,
			QBiases:     qbiases,
			GlobalScale: globalScale,
			GroupSize:   groupSize,
			Bits:        bits,
			Mode:        mode,
		}
	}

	return nn.NewEmbedding(w)
}
