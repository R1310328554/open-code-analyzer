// 共享神经网络层：Linear、Embedding、RMSNorm 与量化变体。
package nn

import "github.com/ollama/ollama/x/mlxrunner/mlx"

// Layer 为带 Forward 的神经网络层接口。
// Layer is the interface for neural network layers with a Forward method.
type Layer interface {
	Forward(x *mlx.Array) *mlx.Array
}

// LinearLayer 为线性层接口（含量化实现）。
// LinearLayer is an interface for linear layers (both regular and quantized).
type LinearLayer interface {
	Forward(x *mlx.Array) *mlx.Array
	OutputDim() int32
}

// EmbeddingLayer 为嵌入层接口，可 AsLinear 复用为 tied LM head。
// EmbeddingLayer is an interface for embedding layers that can also expose a
// tied-output projection when the model reuses embedding weights as the LM head.
type EmbeddingLayer interface {
	Forward(indices *mlx.Array) *mlx.Array
	AsLinear() LinearLayer
}

// Conv1d 对 NLC 布局输入做一维卷积。
// Conv1d applies 1D convolution over NLC input.
type Conv1d struct {
	Weight   *mlx.Array
	Bias     *mlx.Array
	Stride   int32
	Padding  int32
	Dilation int32
	Groups   int32
}

// NewConv1d 构造一维卷积层。
func NewConv1d(weight, bias *mlx.Array, stride, padding, dilation, groups int32) *Conv1d {
	if stride <= 0 {
		stride = 1
	}
	if dilation <= 0 {
		dilation = 1
	}
	if groups <= 0 {
		groups = 1
	}
	return &Conv1d{
		Weight:   weight,
		Bias:     bias,
		Stride:   stride,
		Padding:  padding,
		Dilation: dilation,
		Groups:   groups,
	}
}

func (c *Conv1d) Forward(x *mlx.Array) *mlx.Array {
	return mlx.Conv1d(x, c.Weight, c.Bias, c.Stride, c.Padding, c.Dilation, c.Groups)
}

// Linear 仿射变换 y = x @ W.T + b。
// Linear applies an affine transformation: y = x @ W.T + b
type Linear struct {
	Weight *mlx.Array
	Bias   *mlx.Array
}

// NewLinear 构造全精度线性层。
func NewLinear(weight *mlx.Array, bias *mlx.Array) *Linear {
	if bias != nil && bias.Valid() && bias.DType() != weight.DType() {
		bias = bias.AsType(weight.DType())
	}
	return &Linear{Weight: weight, Bias: bias}
}

func (l *Linear) Forward(x *mlx.Array) *mlx.Array {
	w := l.Weight.Transpose(1, 0)
	if l.Bias != nil && l.Bias.Valid() {
		return l.Bias.Addmm(x, w, 1.0, 1.0)
	}
	return x.Matmul(w)
}

func (l *Linear) OutputDim() int32 {
	return int32(l.Weight.Dim(0))
}

// QuantizedLinear 使用量化权重做仿射变换。
// QuantizedLinear applies an affine transformation using quantized weights.
type QuantizedLinear struct {
	Weight      *mlx.Array // 量化权重数据
	// Quantized weight data
	Scales      *mlx.Array // 反量化 scale
	// Scale factors for dequantization
	QBiases     *mlx.Array // 量化 bias（nvfp4 可为 nil）
	// Quantization biases (nil for nvfp4)
	Bias        *mlx.Array // Layer bias [output_dims] or nil
	GlobalScale *mlx.Array // 双 scale nvfp4 的全局 scale
	// Per-tensor or per-row global scale for double-scale nvfp4 (nil for standard)
	GroupSize   int
	Bits        int
	Mode        string
}

// NewQuantizedLinear 量化权重并构造 QuantizedLinear。
func NewQuantizedLinear(weight *mlx.Array, bias *mlx.Array, groupSize, bits int, mode string) *QuantizedLinear {
	qw, scales, qbiases := mlx.Quantize(weight, groupSize, bits, mode)
	if qbiases != nil {
		mlx.Eval(qw, scales, qbiases)
	} else {
		mlx.Eval(qw, scales)
	}
	if bias != nil && bias.Valid() && bias.DType() != weight.DType() {
		bias = bias.AsType(weight.DType())
	}
	return &QuantizedLinear{
		Weight:    qw,
		Scales:    scales,
		QBiases:   qbiases,
		Bias:      bias,
		GroupSize: groupSize,
		Bits:      bits,
		Mode:      mode,
	}
}

func (ql *QuantizedLinear) Forward(x *mlx.Array) *mlx.Array {
	out := mlx.QuantizedMatmul(x, ql.Weight, ql.Scales, ql.QBiases, true, ql.GroupSize, ql.Bits, ql.Mode)
	if ql.GlobalScale != nil {
		// 双 scale nvfp4：标准 quantized_matmul 后再乘 global_scale。
		// Double-scale nvfp4 (e.g., NVIDIA ModelOpt): standard quantized_matmul
		// followed by global_scale multiply. The global_scale is F32, per-tensor
		// (weight_scale_2 in NVIDIA's format) or per-row.
		// TODO: MLX 有融合内核后改用 fused double-scale matmul。
		// TODO: switch to a fused double-scale matmul once MLX has kernel
		// coverage for this path.
		outDType := out.DType()
		out = mlx.Mul(out, ql.GlobalScale).AsType(outDType)
	}
	if ql.Bias != nil && ql.Bias.Valid() {
		bias := ql.Bias
		if bias.DType() != out.DType() {
			bias = bias.AsType(out.DType())
		}
		out = out.Add(bias)
	}
	return out
}

func (ql *QuantizedLinear) OutputDim() int32 {
	return int32(ql.Weight.Dim(0))
}

// RMSNorm 为 RMS 归一化层。
// RMSNorm represents an RMS normalization layer.
type RMSNorm struct {
	Weight *mlx.Array
	Eps    float32
}

// NewRMSNorm 构造 RMSNorm。
func NewRMSNorm(weight *mlx.Array, eps float32) *RMSNorm {
	return &RMSNorm{Weight: weight, Eps: eps}
}

func (rn *RMSNorm) Forward(x *mlx.Array, eps float32) *mlx.Array {
	if eps == 0 {
		eps = rn.Eps
	}
	return mlx.RMSNormFn(x, rn.Weight, eps)
}

// Embedding 为全精度嵌入查表层。
// Embedding represents an embedding layer.
type Embedding struct {
	Weight *mlx.Array
}

func NewEmbedding(weight *mlx.Array) *Embedding {
	return &Embedding{Weight: weight}
}

func (e *Embedding) Forward(indices *mlx.Array) *mlx.Array {
	return e.Weight.TakeAxis(indices, 0)
}

func (e *Embedding) AsLinear() LinearLayer {
	return NewLinear(e.Weight, nil)
}

// QuantizedEmbedding 从量化权重按行查表并反量化选中行。
// QuantizedEmbedding performs row-wise embedding lookup from affine/nvfp4/etc.
// packed weights and dequantizes only the selected rows.
type QuantizedEmbedding struct {
	Weight      *mlx.Array
	Scales      *mlx.Array
	QBiases     *mlx.Array
	GlobalScale *mlx.Array // Per-tensor global scale for double-scale nvfp4 (nil for standard)
	GroupSize   int
	Bits        int
	Mode        string
}

func (qe *QuantizedEmbedding) Forward(indices *mlx.Array) *mlx.Array {
	weight := qe.Weight.TakeAxis(indices, 0)
	scales := qe.Scales.TakeAxis(indices, 0)
	var qbiases *mlx.Array
	if qe.QBiases != nil && qe.QBiases.Valid() {
		qbiases = qe.QBiases.TakeAxis(indices, 0)
	}
	return mlx.Dequantize(weight, scales, qbiases, qe.GroupSize, qe.Bits, qe.Mode, qe.GlobalScale)
}

func (qe *QuantizedEmbedding) AsLinear() LinearLayer {
	return &QuantizedLinear{
		Weight:      qe.Weight,
		Scales:      qe.Scales,
		QBiases:     qe.QBiases,
		GlobalScale: qe.GlobalScale,
		GroupSize:   qe.GroupSize,
		Bits:        qe.Bits,
		Mode:        qe.Mode,
	}
}

// LayerNorm 为标准 LayerNorm（含 bias）。
// LayerNorm represents a standard layer normalization layer (with bias).
type LayerNorm struct {
	Weight *mlx.Array
	Bias   *mlx.Array
	Eps    float32
}

func (ln *LayerNorm) Forward(x *mlx.Array) *mlx.Array {
	eps := ln.Eps
	if eps == 0 {
		eps = 1e-5
	}
	return mlx.LayerNormFn(x, ln.Weight, ln.Bias, eps)
}

// MultiLinearLayer 为逐头线性层接口（MLA 等）。
// MultiLinearLayer is an interface for per-head linear layers.
type MultiLinearLayer interface {
	Forward(x *mlx.Array) *mlx.Array
}

// MultiLinear 执行逐头线性投影；权重形状 [num_heads, out, in]。
// MultiLinear performs per-head linear projections.
// Weight shape: [num_heads, output_dims, input_dims]
type MultiLinear struct {
	Weight *mlx.Array
}

// NewMultiLinear 构造 MultiLinear。
func NewMultiLinear(weight *mlx.Array) *MultiLinear {
	return &MultiLinear{Weight: weight}
}

func (ml *MultiLinear) Forward(x *mlx.Array) *mlx.Array {
	wT := ml.Weight.Transpose(0, 2, 1)
	return x.Matmul(wT)
}

// ApplyCausalMask 对注意力分数应用因果下三角 mask。
// ApplyCausalMask applies causal (lower triangular) mask to attention scores.
func ApplyCausalMask(scores *mlx.Array) *mlx.Array {
	shape := scores.Dims()
	seqLen := int32(shape[2])
	mask := mlx.Tri(seqLen, seqLen, 0)
	negInf := mlx.NewScalarArray(float32(-1e9))
	mask = mask.ExpandDims(0).ExpandDims(0)
	return mlx.Where(mask, scores, negInf)
}

// ApplyCausalMaskWithOffset 为带 KV 缓存的注意力应用偏移因果 mask。
// ApplyCausalMaskWithOffset applies causal mask for cached attention.
func ApplyCausalMaskWithOffset(scores *mlx.Array, offset int32) *mlx.Array {
	if offset == 0 {
		return ApplyCausalMask(scores)
	}
	shape := scores.Dims()
	queryLen := int32(shape[2])
	keyLen := int32(shape[3])
	mask := mlx.Tri(queryLen, keyLen, int(offset))
	negInf := mlx.NewScalarArray(float32(-1e9))
	mask = mask.ExpandDims(0).ExpandDims(0)
	return mlx.Where(mask, scores, negInf)
}
