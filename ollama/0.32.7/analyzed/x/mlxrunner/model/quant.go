// 量化参数解析：与 x/quant 及 per-tensor 元数据对齐。
package model

import (
	"github.com/ollama/ollama/x/mlxrunner/mlx"
	"github.com/ollama/ollama/x/quant"
)

// QuantizationParams 返回量化类型的默认 groupSize/bits/mode。
// QuantizationParams returns default groupSize, bits, and mode for a
// 值来自 x/quant，与导入器、运行时、ollama show 一致。
// quantization type. The values live in the shared x/quant package so the
// importer, the runtime loader, and `ollama show` agree on them.
func QuantizationParams(quantization string) (groupSize, bits int, mode string) {
	return quant.Params(quantization)
}

// TensorQuantParams 优先 per-tensor 元数据，否则用模型默认。
// TensorQuantParams resolves quant params for a tensor using per-tensor metadata
// when available, otherwise falling back to the provided model defaults.
func TensorQuantParams(
	defaultGroupSize, defaultBits int,
	defaultMode string,
	tensorQuant map[string]*TensorQuantInfo,
	tensorName string,
) (groupSize, bits int, mode string, fromTensor bool) {
	if tensorQuant != nil {
		if tq := tensorQuant[tensorName]; tq != nil {
			groupSize, bits, mode = QuantizationParams(tq.QuantType)
			if tq.GroupSize > 0 {
				groupSize = tq.GroupSize
			}
			return groupSize, bits, mode, true
		}
	}
	return defaultGroupSize, defaultBits, defaultMode, false
}

// ResolveLinearQuantParams 为量化线性层解析参数，affine 时可 shape 推断。
// ResolveLinearQuantParams resolves quantization params for a quantized linear
// tensor, preferring per-tensor metadata and falling back to shape-based
// inference for affine packed tensors.
func ResolveLinearQuantParams(
	defaultGroupSize, defaultBits int,
	defaultMode string,
	tensorQuant map[string]*TensorQuantInfo,
	tensorName string,
	weight, scales *mlx.Array,
) (groupSize, bits int, mode string) {
	groupSize, bits, mode, fromTensor := TensorQuantParams(
		defaultGroupSize,
		defaultBits,
		defaultMode,
		tensorQuant,
		tensorName,
	)

	if mode == "affine" {
		if inferredGroupSize, inferredBits, ok := InferAffineQuantParamsFromShapes(weight, scales, bits); ok {
			if !fromTensor || groupSize == 0 || bits == 0 {
				groupSize = inferredGroupSize
				bits = inferredBits
			}
		}
	}

	return groupSize, bits, mode
}

// InferAffineQuantParamsFromShapes 从 packed weight/scale shape 推断仿射量化参数。
// InferAffineQuantParamsFromShapes infers (groupSize,bits) for affine quantized
// tensors from packed weight and scale shapes.
func InferAffineQuantParamsFromShapes(weight, scales *mlx.Array, hintBits int) (groupSize, bits int, ok bool) {
	if weight == nil || scales == nil {
		return 0, 0, false
	}

	weightShape := weight.Dims()
	scaleShape := scales.Dims()
	if len(weightShape) == 0 || len(scaleShape) == 0 {
		return 0, 0, false
	}

	weightCols := weightShape[len(weightShape)-1]
	scalesCols := scaleShape[len(scaleShape)-1]
	if weightCols <= 0 || scalesCols <= 0 {
		return 0, 0, false
	}

	groupSize4 := weightCols * 8 / scalesCols
	groupSize8 := weightCols * 4 / scalesCols

	switch {
	case groupSize4 == 32:
		return 32, 4, true
	case groupSize8 == 64:
		return 64, 8, true
	case groupSize4 == 64 && groupSize8 == 32:
		if hintBits == 8 {
			return 32, 8, true
		}
		if hintBits == 4 {
			return 64, 4, true
		}
	}

	if isCommonGroupSize(groupSize4) && !isCommonGroupSize(groupSize8) {
		return groupSize4, 4, true
	}
	if isCommonGroupSize(groupSize8) && !isCommonGroupSize(groupSize4) {
		return groupSize8, 8, true
	}

	return 0, 0, false
}

// isCommonGroupSize 判断是否为常见 group size（16/32/64/128）。
func isCommonGroupSize(v int) bool {
	switch v {
	case 16, 32, 64, 128:
		return true
	default:
		return false
	}
}
