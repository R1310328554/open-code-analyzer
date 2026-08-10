// 线性层工厂：共享量化默认值与张量映射构造 Linear。
package model

import (
	"github.com/ollama/ollama/x/mlxrunner/mlx"
	"github.com/ollama/ollama/x/models/nn"
)

// LinearFactory 用共享 tensors 与量化默认值批量构造线性层。
// LinearFactory builds linear layers using shared tensor maps and quant defaults.
// LinearFactory 持有构造线性层所需的共享上下文。
type LinearFactory struct {
	tensors          map[string]*mlx.Array
	defaultGroupSize int
	defaultBits      int
	defaultMode      string
	tensorQuant      map[string]*TensorQuantInfo
}

// NewLinearFactory 创建可复用的线性层工厂。
// NewLinearFactory creates a reusable constructor for model linear layers.
func NewLinearFactory(
	tensors map[string]*mlx.Array,
	defaultGroupSize, defaultBits int,
	defaultMode string,
	tensorQuant map[string]*TensorQuantInfo,
) LinearFactory {
	return LinearFactory{
		tensors:          tensors,
		defaultGroupSize: defaultGroupSize,
		defaultBits:      defaultBits,
		defaultMode:      defaultMode,
		tensorQuant:      tensorQuant,
	}
}

// Make 在 path 处构造线性层。
// Make constructs a linear layer at path.
func (f LinearFactory) Make(path string) nn.LinearLayer {
	return MakeLinearLayer(
		f.tensors,
		path,
		f.defaultGroupSize,
		f.defaultBits,
		f.defaultMode,
		f.tensorQuant,
	)
}

// MakeLinearLayer 从 tensor 映射构造线性层。
// MakeLinearLayer constructs a linear layer from a tensor map.
//
// 量化路径通过 TensorQuant 或 shape 仿射推断解析 groupSize/bits/mode。
// For quantized tensors (path.weight + path.weight_scale), it resolves per-tensor
// quant params via TensorQuant metadata (with shape-based affine fallback).
// For non-quantized tensors, it returns a standard nn.Linear.
func MakeLinearLayer(
	tensors map[string]*mlx.Array,
	path string,
	defaultGroupSize, defaultBits int,
	defaultMode string,
	tensorQuant map[string]*TensorQuantInfo,
) nn.LinearLayer {
	w := tensors[path+".weight"]
	if w == nil {
		return nil
	}

	scales := tensors[path+".weight_scale"]
	if scales != nil {
		qbiases := tensors[path+".weight_qbias"]
		bias := tensors[path+".bias"]

		groupSize, bits, mode := ResolveLinearQuantParams(
			defaultGroupSize,
			defaultBits,
			defaultMode,
			tensorQuant,
			path+".weight",
			w,
			scales,
		)

		// 检查 per-tensor global scale（NVIDIA nvfp4）。
		// Check for per-tensor global scale (NVIDIA double-scale nvfp4).
		// NVIDIA ModelOpt stores this as "weight_scale_2"; our import
		// pipeline maps it to "weight.global_scale".
		globalScale := tensors[path+".weight.global_scale"]
		if globalScale == nil {
			globalScale = tensors[path+".weight_scale_2"]
		}

		return &nn.QuantizedLinear{
			Weight:      w,
			Scales:      scales,
			QBiases:     qbiases,
			Bias:        bias,
			GlobalScale: globalScale,
			GroupSize:   groupSize,
			Bits:        bits,
			Mode:        mode,
		}
	}

	bias := tensors[path+".bias"]
	return nn.NewLinear(w, bias)
}
