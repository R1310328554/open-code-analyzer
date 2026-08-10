// MLX 扩展算子：量化、卷积、RoPE、归一化与工具函数。
package mlx

// #include "generated.h"
import "C"

import (
	"reflect"
	"unsafe"
)

// 量化相关算子
// Quantization operations

// Quantize 将浮点权重按 groupSize/bits/mode 量化，返回权重、scale 与可选 bias。
func Quantize(w *Array, groupSize, bits int, mode string) (weights, scales, biases *Array) {
	cMode := C.CString(mode)
	defer C.free(unsafe.Pointer(cMode))
	optGroupSize := C.mlx_optional_int{value: C.int(groupSize), has_value: true}
	optBits := C.mlx_optional_int{value: C.int(bits), has_value: true}
	res := C.mlx_vector_array_new()
	defer C.mlx_vector_array_free(res)
	var globalScale C.mlx_array
	C.mlx_quantize(&res, w.ctx, optGroupSize, optBits, cMode, globalScale, DefaultStream().ctx)

	vecSize := int(C.mlx_vector_array_size(res))
	w0 := New("QUANTIZE_W")
	C.mlx_vector_array_get(&w0.ctx, res, 0)
	w1 := New("QUANTIZE_S")
	C.mlx_vector_array_get(&w1.ctx, res, 1)
	if vecSize >= 3 {
		w2 := New("QUANTIZE_B")
		C.mlx_vector_array_get(&w2.ctx, res, 2)
		return w0, w1, w2
	}
	return w0, w1, nil
}

// FromFP8 将 FP8 张量反量化为目标 dtype。
func FromFP8(x *Array, dtype DType) *Array {
	out := New("FROM_FP8")
	C.mlx_from_fp8(&out.ctx, x.ctx, C.mlx_dtype(dtype), DefaultStream().ctx)
	return out
}

// ToFP8 将张量编码为 FP8。
func ToFP8(x *Array) *Array {
	out := New("TO_FP8")
	C.mlx_to_fp8(&out.ctx, x.ctx, DefaultStream().ctx)
	return out
}

// Dequantize 反量化权重；Metal 上 globalScale 在 Go 侧额外乘入。
func Dequantize(w, scales, biases *Array, groupSize, bits int, mode string, globalScale *Array) *Array {
	cMode := C.CString(mode)
	defer C.free(unsafe.Pointer(cMode))
	optGroupSize := C.mlx_optional_int{value: C.int(groupSize), has_value: true}
	optBits := C.mlx_optional_int{value: C.int(bits), has_value: true}
	optDtype := C.mlx_optional_dtype{has_value: false}

	var b C.mlx_array
	if biases != nil {
		b = biases.ctx
	}

	out := New("DEQUANTIZE")
	var noGlobalScale C.mlx_array
	C.mlx_dequantize(&out.ctx, w.ctx, scales.ctx, b, optGroupSize, optBits, cMode, noGlobalScale, optDtype, DefaultStream().ctx)
	if globalScale != nil {
		// C 层 global_scale 在 Metal 被拒绝，此处 Go 侧再乘。
		// The C-level global_scale argument is rejected on Metal; apply it on top.
		gs := globalScale
		if gs.Size() > 1 {
			// 向量 scale 为逐行，需绑定权重 leading axis。
			// A vector scale is per-row; bind it to the weight's leading axis.
			gs = Reshape(gs, int32(gs.Size()), 1)
		}
		outType := out.DType()
		out = Mul(out, gs).AsType(outType)
	}
	return out
}

// QuantizedMatmul 量化矩阵乘，可选转置权重。
func QuantizedMatmul(x, w, scales, biases *Array, transpose bool, groupSize, bits int, mode string) *Array {
	cMode := C.CString(mode)
	defer C.free(unsafe.Pointer(cMode))
	optGroupSize := C.mlx_optional_int{value: C.int(groupSize), has_value: true}
	optBits := C.mlx_optional_int{value: C.int(bits), has_value: true}

	var b C.mlx_array
	if biases != nil {
		b = biases.ctx
	}

	out := New("QUANTIZED_MATMUL")
	C.mlx_quantized_matmul(&out.ctx, x.ctx, w.ctx, scales.ctx, b, C.bool(transpose), optGroupSize, optBits, cMode, DefaultStream().ctx)
	return out
}

// GatherQMM 带索引的量化分组矩阵乘，用于 MoE。
func GatherQMM(x, w, scales *Array, biases, lhsIndices, rhsIndices *Array, transpose bool, groupSize, bits int, mode string, sortedIndices bool) *Array {
	cMode := C.CString(mode)
	defer C.free(unsafe.Pointer(cMode))
	optGroupSize := C.mlx_optional_int{value: C.int(groupSize), has_value: true}
	optBits := C.mlx_optional_int{value: C.int(bits), has_value: true}

	var b, lhs, rhs C.mlx_array
	if biases != nil {
		b = biases.ctx
	}
	if lhsIndices != nil {
		lhs = lhsIndices.ctx
	}
	if rhsIndices != nil {
		rhs = rhsIndices.ctx
	}

	out := New("GATHER_QMM")
	C.mlx_gather_qmm(&out.ctx, x.ctx, w.ctx, scales.ctx, b, lhs, rhs, C.bool(transpose), optGroupSize, optBits, cMode, C.bool(sortedIndices), DefaultStream().ctx)
	return out
}

// 补充张量算子
// Missing tensor ops

// Tile 沿各维按 reps 重复张量。
func Tile(a *Array, reps []int32) *Array {
	cReps := make([]C.int, len(reps))
	for i, r := range reps {
		cReps[i] = C.int(r)
	}
	out := New("TILE")
	C.mlx_tile(&out.ctx, a.ctx, unsafe.SliceData(cReps), C.size_t(len(reps)), DefaultStream().ctx)
	return out
}

// Tri 构造 float32 三角矩阵。
func Tri(n, m int32, k int) *Array {
	out := New("TRI")
	C.mlx_tri(&out.ctx, C.int(n), C.int(m), C.int(k), C.mlx_dtype(DTypeFloat32), DefaultStream().ctx)
	return out
}

// Where 按 condition 逐元素选择 a 或 b。
func Where(condition, a, b *Array) *Array {
	out := New("WHERE")
	C.mlx_where(&out.ctx, condition.ctx, a.ctx, b.ctx, DefaultStream().ctx)
	return out
}

// Conv1d 一维卷积，可选 bias。
func Conv1d(x, weight *Array, bias *Array, stride, padding, dilation, groups int32) *Array {
	out := New("CONV1D")
	C.mlx_conv1d(
		&out.ctx,
		x.ctx,
		weight.ctx,
		C.int(stride),
		C.int(padding),
		C.int(dilation),
		C.int(groups),
		DefaultStream().ctx,
	)
	if bias != nil && bias.Valid() {
		out = Add(out, bias)
	}
	return out
}

// Contiguous 物化为连续存储，可选允许列主序。
func Contiguous(a *Array, allowColMajor bool) *Array {
	out := New("CONTIGUOUS")
	C.mlx_contiguous(&out.ctx, a.ctx, C.bool(allowColMajor), DefaultStream().ctx)
	return out
}

// Conv2d 二维卷积：x 为 NHWC，weight 为 [C_out,kH,kW,C_in]。
// Conv2d performs 2D convolution: x [N,H,W,C_in], weight [C_out,kH,kW,C_in].
// MLX 使用 NHWC 内存布局。
// MLX uses NHWC layout.
func Conv2d(x, weight *Array, strideH, strideW, padH, padW, dilationH, dilationW, groups int32) *Array {
	out := New("CONV2D")
	C.mlx_conv2d(
		&out.ctx,
		x.ctx,
		weight.ctx,
		C.int(strideH), C.int(strideW),
		C.int(padH), C.int(padW),
		C.int(dilationH), C.int(dilationW),
		C.int(groups),
		DefaultStream().ctx,
	)
	return out
}

// Pad 沿 axes 填充，mode 为 constant/edge/reflect。
// Pad pads array a along the given axes with specified low/high pad sizes.
// mode 取值 constant、edge 或 reflect。
// mode should be "constant", "edge", or "reflect".
func Pad(a *Array, axes []int, lowPad, highPad []int, padValue *Array, mode string) *Array {
	cAxes := make([]C.int, len(axes))
	cLow := make([]C.int, len(lowPad))
	cHigh := make([]C.int, len(highPad))
	for i := range axes {
		cAxes[i] = C.int(axes[i])
		cLow[i] = C.int(lowPad[i])
		cHigh[i] = C.int(highPad[i])
	}
	cMode := C.CString(mode)
	defer C.free(unsafe.Pointer(cMode))
	out := New("PAD")
	C.mlx_pad(
		&out.ctx,
		a.ctx,
		unsafe.SliceData(cAxes), C.size_t(len(cAxes)),
		unsafe.SliceData(cLow), C.size_t(len(cLow)),
		unsafe.SliceData(cHigh), C.size_t(len(cHigh)),
		padValue.ctx,
		cMode,
		DefaultStream().ctx,
	)
	return out
}

// PadConstant 沿 axes 以零填充。
// PadConstant pads with zeros along the given axes.
func PadConstant(a *Array, axes []int, lowPad, highPad []int) *Array {
	zero := NewScalarArray(float32(0))
	return Pad(a, axes, lowPad, highPad, zero, "constant")
}

// DepthwiseConv1d 深度可分离一维卷积。
func DepthwiseConv1d(x, weight *Array, bias *Array) *Array {
	groups := int32(x.Dim(x.NumDims() - 1))
	return Conv1d(x, weight, bias, 1, 0, 1, groups)
}

// Maximum 逐元素取较大值。
// Maximum returns element-wise maximum of two arrays.
func Maximum(a, b *Array) *Array {
	out := New("MAXIMUM")
	C.mlx_maximum(&out.ctx, a.ctx, b.ctx, DefaultStream().ctx)
	return out
}

// Minimum 逐元素取较小值。
// Minimum returns element-wise minimum of two arrays.
func Minimum(a, b *Array) *Array {
	out := New("MINIMUM")
	C.mlx_minimum(&out.ctx, a.ctx, b.ctx, DefaultStream().ctx)
	return out
}

// Softplus 用 logaddexp 稳定计算 log(1+exp(x))。
// Softplus computes log(1 + exp(x)) using logaddexp for numerical stability.
func Softplus(a *Array) *Array {
	return Logaddexp(a, Zeros(a.DType(), a.Dims()...))
}

// ReLU 计算 max(0,x)。
// ReLU computes max(0, x).
func ReLU(a *Array) *Array {
	return Maximum(a, NewScalarArray(float32(0)))
}

// GLU 门控线性单元：末维一分为二，返回 first*sigmoid(second)。
// GLU applies Gated Linear Unit: splits x along last dim into two halves,
// returns first * sigmoid(second).
func GLU(a *Array) *Array {
	lastDim := a.NumDims() - 1
	halfSize := a.Dim(lastDim) / 2
	first := SliceStartStop(a,
		make([]int32, lastDim+1), // all zeros for start
		appendDims(a, lastDim, int32(halfSize)),
	)
	second := SliceStartStop(a,
		appendDimsStart(a, lastDim, int32(halfSize)),
		appendDims(a, lastDim, int32(a.Dim(lastDim))),
	)
	return first.Multiply(second.Sigmoid())
}

// 辅助：构造 SliceStartStop 的 stop，目标轴设为 val。
// helper: builds stop array for SliceStartStop where the target axis = val
func appendDims(a *Array, targetAxis int, val int32) []int32 {
	n := a.NumDims()
	out := make([]int32, n)
	for i := range n {
		if i == targetAxis {
			out[i] = val
		} else {
			out[i] = int32(a.Dim(i))
		}
	}
	return out
}

// 辅助：构造 SliceStartStop 的 start，目标轴设为 val。
// helper: builds start array for SliceStartStop where the target axis = val
func appendDimsStart(a *Array, targetAxis int, val int32) []int32 {
	n := a.NumDims()
	out := make([]int32, n)
	for i := range n {
		if i == targetAxis {
			out[i] = val
		}
	}
	return out
}

// Clamp 将值限制在 [min,max]。
// Clamp clamps array values to [min, max].
func Clamp(a *Array, minVal, maxVal float32) *Array {
	return Minimum(Maximum(a, NewScalarArray(minVal)), NewScalarArray(maxVal))
}

// 模型代码用的函数式便捷包装
// Convenience wrappers (function-style for the model code)

// Stack 沿 axis 堆叠多个 Array。
func Stack(arrays []*Array, axis int) *Array {
	vectorData := make([]C.mlx_array, len(arrays))
	for i := range arrays {
		vectorData[i] = arrays[i].ctx
	}
	vector := C.mlx_vector_array_new_data(unsafe.SliceData(vectorData), C.size_t(len(vectorData)))
	defer C.mlx_vector_array_free(vector)

	out := New("STACK")
	C.mlx_stack_axis(&out.ctx, vector, C.int(axis), DefaultStream().ctx)
	return out
}

// Neg 取负。
func Neg(a *Array) *Array {
	return a.Negative()
}

// Sum 沿轴求和。
func Sum(a *Array, axis int, keepDims bool) *Array {
	return a.SumAxis(axis, keepDims)
}

// Argsort 沿轴排序索引。
func Argsort(a *Array, axis int) *Array {
	return a.ArgsortAxis(axis)
}

// Take 沿轴按 indices 索引。
func Take(a *Array, indices *Array, axis int) *Array {
	return a.TakeAxis(indices, axis)
}

// RSqrt 逐元素 rsqrt。
func RSqrt(a *Array) *Array {
	out := New("RSQRT")
	C.mlx_rsqrt(&out.ctx, a.ctx, DefaultStream().ctx)
	return out
}

// Mean 沿轴求均值。
func Mean(a *Array, axis int, keepDims bool) *Array {
	out := New("MEAN_AXIS")
	C.mlx_mean_axis(&out.ctx, a.ctx, C.int(axis), C.bool(keepDims), DefaultStream().ctx)
	return out
}

// Argpartition 部分排序。
func Argpartition(a *Array, kth int, axis int) *Array {
	return a.ArgpartitionAxis(kth, axis)
}

// TakeAlongAxis 沿轴 gather。
func TakeAlongAxis(a, indices *Array, axis int) *Array {
	return a.TakeAlongAxis(indices, axis)
}

// 模型侧函数式包装
// Function-style wrappers for model code.

// Add 逐元素加。
func Add(a, b *Array) *Array {
	return a.Add(b)
}

// Sub 逐元素减。
func Sub(a, b *Array) *Array {
	return a.Subtract(b)
}

// Mul 逐元素乘。
func Mul(a, b *Array) *Array {
	return a.Multiply(b)
}

// Div 逐元素除。
func Div(a, b *Array) *Array {
	return a.Divide(b)
}

// Matmul 矩阵乘。
func Matmul(a, b *Array) *Array {
	return a.Matmul(b)
}

// Reshape 重塑 shape。
func Reshape(a *Array, shape ...int32) *Array {
	axes := make([]int, len(shape))
	for i, s := range shape {
		axes[i] = int(s)
	}
	return a.Reshape(axes...)
}

// Transpose 转置。
func Transpose(a *Array, axes ...int) *Array {
	return a.Transpose(axes...)
}

// ExpandDims 扩维。
func ExpandDims(a *Array, axis int) *Array {
	return a.ExpandDims(axis)
}

// Squeeze .squeeze 维。
func Squeeze(a *Array, axis int) *Array {
	return a.Squeeze(axis)
}

// Flatten 展平全部维。
func Flatten(a *Array) *Array {
	return a.Flatten(0, -1)
}

// Concatenate 拼接。
func Concatenate(arrays []*Array, axis int) *Array {
	if len(arrays) == 0 {
		return nil
	}
	if len(arrays) == 1 {
		return arrays[0].Clone()
	}
	return arrays[0].Concatenate(axis, arrays[1:]...)
}

// SliceStartStop 按 start/stop 切片。
func SliceStartStop(a *Array, start, stop []int32) *Array {
	n := len(start)
	cStart := make([]C.int, n)
	cStop := make([]C.int, n)
	cStrides := make([]C.int, n)
	for i := range n {
		cStart[i] = C.int(start[i])
		cStop[i] = C.int(stop[i])
		cStrides[i] = 1
	}
	out := New("SLICE")
	C.mlx_slice(&out.ctx, a.ctx, unsafe.SliceData(cStart), C.size_t(n), unsafe.SliceData(cStop), C.size_t(n), unsafe.SliceData(cStrides), C.size_t(n), DefaultStream().ctx)
	return out
}

// GatherMM 分组矩阵乘。
func GatherMM(a, b *Array, lhsIndices, rhsIndices *Array, sortedIndices bool) *Array {
	if lhsIndices == nil {
		lhsIndices = New("")
	}
	if rhsIndices == nil {
		rhsIndices = New("")
	}
	return a.GatherMM(b, lhsIndices, rhsIndices, sortedIndices)
}

// RoPEWithBase 对 x 应用 RoPE；offsets 为每 batch 行起始位置。
// RoPEWithBase applies rotary position embeddings to x. offsets is an
// int32 array of shape [B] giving each batch row's starting position;
// the kernel applies positions offsets[b] + 0..T-1 per row.
func RoPEWithBase(x *Array, dims int, traditional bool, base, scale float32, offsets *Array) *Array {
	return RoPEWithFreqs(x, dims, traditional, base, scale, offsets, nil)
}

// RoPEWithFreqs 可选自定义频率；MLX 内部取 freqs 倒数。
// RoPEWithFreqs applies RoPE with optional custom frequencies.
// When freqs is non-nil, it is used instead of computing from base.
// 注意：传入实际频率 base^(2i/dim)，非逆频率。
// Note: MLX takes reciprocal(freqs) internally to get inv_freq, so pass
// the actual frequencies (base^(2i/dim)), not the inverse frequencies.
func RoPEWithFreqs(x *Array, dims int, traditional bool, base, scale float32, offsets *Array, freqs *Array) *Array {
	var freqsCtx C.mlx_array
	var optBase C.mlx_optional_float
	if freqs != nil {
		freqsCtx = freqs.ctx
		optBase = C.mlx_optional_float{has_value: C.bool(false)}
	} else {
		empty := New("")
		freqsCtx = empty.ctx
		optBase = C.mlx_optional_float{
			value:     C.float(base),
			has_value: C.bool(func() bool { return base != 0 }()),
		}
	}
	out := New("FAST_ROPE")
	C.mlx_fast_rope_dynamic(
		&out.ctx,
		x.ctx,
		C.int(dims),
		C.bool(traditional),
		optBase,
		C.float(scale),
		offsets.ctx,
		freqsCtx,
		DefaultStream().ctx,
	)
	return out
}

// Sigmoid sigmoid。
func Sigmoid(a *Array) *Array {
	return a.Sigmoid()
}

// Erf 误差函数。
func Erf(a *Array) *Array {
	out := New("ERF")
	C.mlx_erf(&out.ctx, a.ctx, DefaultStream().ctx)
	return out
}

// Exp 指数。
func Exp(a *Array) *Array {
	out := New("EXP")
	C.mlx_exp(&out.ctx, a.ctx, DefaultStream().ctx)
	return out
}

// Log 对数。
func Log(a *Array) *Array {
	out := New("LOG")
	C.mlx_log(&out.ctx, a.ctx, DefaultStream().ctx)
	return out
}

// Sin 正弦。
func Sin(a *Array) *Array {
	out := New("SIN")
	C.mlx_sin(&out.ctx, a.ctx, DefaultStream().ctx)
	return out
}

// Cos 余弦。
func Cos(a *Array) *Array {
	out := New("COS")
	C.mlx_cos(&out.ctx, a.ctx, DefaultStream().ctx)
	return out
}

func erf(a *Array) *Array {
	out := New("ERF")
	C.mlx_erf(&out.ctx, a.ctx, DefaultStream().ctx)
	return out
}

// Clip 裁剪到区间。
func Clip(a, aMin, aMax *Array) *Array {
	out := New("CLIP")
	C.mlx_clip(&out.ctx, a.ctx, aMin.ctx, aMax.ctx, DefaultStream().ctx)
	return out
}

// Logaddexp 稳定 log(exp(a)+exp(b))。
func Logaddexp(a, b *Array) *Array {
	out := New("LOGADDEXP")
	C.mlx_logaddexp(&out.ctx, a.ctx, b.ctx, DefaultStream().ctx)
	return out
}

// SoftmaxAxis 沿轴 softmax。
func SoftmaxAxis(a *Array, axis int, precise bool) *Array {
	out := New("SOFTMAX_AXIS")
	C.mlx_softmax_axis(&out.ctx, a.ctx, C.int(axis), C.bool(precise), DefaultStream().ctx)
	return out
}

// LayerNormFn 快速 LayerNorm。
func LayerNormFn(x, weight, bias *Array, eps float32) *Array {
	out := New("FAST_LAYERNORM")
	var w, b C.mlx_array
	if weight != nil {
		w = weight.ctx
	}
	if bias != nil {
		b = bias.ctx
	}
	C.mlx_fast_layer_norm(&out.ctx, x.ctx, w, b, C.float(eps), DefaultStream().ctx)
	return out
}

// RMSNormFn 快速 RMSNorm。
func RMSNormFn(x, weight *Array, eps float32) *Array {
	out := New("FAST_RMSNORM")
	var w C.mlx_array
	if weight != nil {
		w = weight.ctx
	}
	C.mlx_fast_rms_norm(&out.ctx, x.ctx, w, C.float(eps), DefaultStream().ctx)
	return out
}

// AddMM 融合 addmm。
func AddMM(c, a, b *Array, alpha, beta float32) *Array {
	return c.Addmm(a, b, alpha, beta)
}

// 标量运算辅助
// Scalar helpers

// scalarWithDtype 创建与 a 同 dtype 的标量，利于算子融合。
// scalarWithDtype creates a scalar array matching the dtype of a.
// 匹配 dtype 可避免隐式 cast 并利于图融合。
// Matching dtype is important for graph fusion and avoiding implicit casts.
func scalarWithDtype(s float32, a *Array) C.mlx_array {
	f32 := C.mlx_array_new_float(C.float(s))
	dtype := a.DType()
	if dtype == DTypeFloat32 {
		return f32
	}
	casted := C.mlx_array_new()
	C.mlx_astype(&casted, f32, C.mlx_dtype(dtype), DefaultStream().ctx)
	C.mlx_array_free(f32)
	return casted
}

// AddScalar 加标量。
func AddScalar(a *Array, s float32) *Array {
	scalar := scalarWithDtype(s, a)
	out := New("ADD_SCALAR")
	C.mlx_add(&out.ctx, a.ctx, scalar, DefaultStream().ctx)
	C.mlx_array_free(scalar)
	return out
}

// MulScalar 乘标量。
func MulScalar(a *Array, s float32) *Array {
	scalar := scalarWithDtype(s, a)
	out := New("MUL_SCALAR")
	C.mlx_multiply(&out.ctx, a.ctx, scalar, DefaultStream().ctx)
	C.mlx_array_free(scalar)
	return out
}

// DivScalar 除标量。
func DivScalar(a *Array, s float32) *Array {
	scalar := scalarWithDtype(s, a)
	out := New("DIV_SCALAR")
	C.mlx_divide(&out.ctx, a.ctx, scalar, DefaultStream().ctx)
	C.mlx_array_free(scalar)
	return out
}

// FloorDivideScalar 整除标量。
func FloorDivideScalar(a *Array, s int32) *Array {
	scalar := FromValue(int(s))
	return a.FloorDivide(scalar)
}

// 张量构造
// Array constructors

// NewArrayInt32 从 int32 数据创建张量。
func NewArrayInt32(data []int32, shape []int32) *Array {
	cShape := make([]C.int, len(shape))
	for i, s := range shape {
		cShape[i] = C.int(s)
	}
	out := New("NEW_ARRAY_INT32")
	out.ctx = C.mlx_array_new_data(unsafe.Pointer(&data[0]), unsafe.SliceData(cShape), C.int(len(shape)), C.mlx_dtype(DTypeInt32))
	return out
}

// NewScalarArray 创建 float32 标量张量。
func NewScalarArray(value float32) *Array {
	out := New("SCALAR")
	out.ctx = C.mlx_array_new_float32(C.float(value))
	return out
}

// ZerosF32 创建 float32 零张量。
func ZerosF32(shape []int32) *Array {
	return Zeros(DTypeFloat32, func() []int {
		ints := make([]int, len(shape))
		for i, s := range shape {
			ints[i] = int(s)
		}
		return ints
	}()...)
}

// 反射收集与编译开关
// Utility

// Collect 递归收集结构体中所有有效 *Array。
func Collect(v any) []*Array {
	var arrays []*Array
	seen := make(map[uintptr]bool)
	collect(reflect.ValueOf(v), &arrays, seen)
	return arrays
}

func collect(v reflect.Value, arrays *[]*Array, seen map[uintptr]bool) {
	if !v.IsValid() {
		return
	}

	if v.Kind() == reflect.Pointer {
		if v.IsNil() {
			return
		}
		ptr := v.Pointer()
		if seen[ptr] {
			return
		}
		seen[ptr] = true

		if arr, ok := v.Interface().(*Array); ok {
			if arr != nil && arr.Valid() {
				*arrays = append(*arrays, arr)
			}
			return
		}
		collect(v.Elem(), arrays, seen)
		return
	}

	switch v.Kind() {
	case reflect.Struct:
		// Check if this struct IS an Array (not a pointer to one)
		if arr, ok := v.Addr().Interface().(*Array); ok {
			if arr != nil && arr.Valid() {
				*arrays = append(*arrays, arr)
			}
			return
		}
		for i := range v.NumField() {
			field := v.Field(i)
			if field.CanInterface() {
				collect(field, arrays, seen)
			}
		}
	case reflect.Slice:
		for i := range v.Len() {
			collect(v.Index(i), arrays, seen)
		}
	case reflect.Map:
		for _, key := range v.MapKeys() {
			collect(v.MapIndex(key), arrays, seen)
		}
	case reflect.Interface:
		if !v.IsNil() {
			collect(v.Elem(), arrays, seen)
		}
	}
}

// EnableCompile 开启 MLX 图编译。
func EnableCompile() {
	C.mlx_enable_compile()
}

// DisableCompile 关闭 MLX 图编译。
func DisableCompile() {
	C.mlx_disable_compile()
}
