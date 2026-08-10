// MLX 张量基础算子：Array 方法绑定 C MLX API。
package mlx

// #include "generated.h"
import "C"

// 以下方法均在 DefaultStream 上调度，返回惰性 Array。
import (
	"unsafe"
)

// Abs 返回逐元素绝对值。
func (t *Array) Abs() *Array {
	out := New("ABS")
	C.mlx_abs(&out.ctx, t.ctx, DefaultStream().ctx)
	return out
}

// Add 逐元素相加。
func (t *Array) Add(other *Array) *Array {
	out := New("ADD")
	C.mlx_add(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// Addmm 计算 beta*self + alpha*(a @ b)。
func (t *Array) Addmm(a, b *Array, alpha, beta float32) *Array {
	out := New("ADDMM")
	C.mlx_addmm(&out.ctx, t.ctx, a.ctx, b.ctx, C.float(alpha), C.float(beta), DefaultStream().ctx)
	return out
}

// Argmax 沿指定轴取最大值索引。
func (t *Array) Argmax(axis int, keepDims bool) *Array {
	out := New("ARGMAX")
	C.mlx_argmax_axis(&out.ctx, t.ctx, C.int(axis), C.bool(keepDims), DefaultStream().ctx)
	return out
}

// ArgpartitionAxis 沿轴做部分排序分区。
func (t *Array) ArgpartitionAxis(kth int, axis int) *Array {
	out := New("ARGPARTITION")
	C.mlx_argpartition_axis(&out.ctx, t.ctx, C.int(kth), C.int(axis), DefaultStream().ctx)
	return out
}

// ArgsortAxis 沿轴返回排序索引。
func (t *Array) ArgsortAxis(axis int) *Array {
	out := New("ARGSORT_AXIS")
	C.mlx_argsort_axis(&out.ctx, t.ctx, C.int(axis), DefaultStream().ctx)
	return out
}

// AsType 转换元素 dtype。
func (t *Array) AsType(dtype DType) *Array {
	out := New("AS_TYPE")
	C.mlx_astype(&out.ctx, t.ctx, C.mlx_dtype(dtype), DefaultStream().ctx)
	return out
}

// AsStrided 以给定 shape/strides/offset 构造视图。
func (t *Array) AsStrided(shape []int, strides []int, offset int) *Array {
	cShape := make([]C.int, len(shape))
	for i, s := range shape {
		cShape[i] = C.int(s)
	}

	cStrides := make([]C.int64_t, len(strides))
	for i, s := range strides {
		cStrides[i] = C.int64_t(s)
	}

	out := New("AS_STRIDED")
	C.mlx_as_strided(
		&out.ctx, t.ctx,
		unsafe.SliceData(cShape), C.size_t(len(shape)),
		unsafe.SliceData(cStrides), C.size_t(len(strides)),
		C.size_t(offset),
		DefaultStream().ctx,
	)
	return out
}

// Concatenate 沿 axis 拼接多个张量。
func (t *Array) Concatenate(axis int, others ...*Array) *Array {
	if len(others) == 0 {
		return t.Clone()
	}

	vector := C.mlx_vector_array_new()
	defer C.mlx_vector_array_free(vector)

	s := append([]*Array{t}, others...)
	for _, other := range s {
		C.mlx_vector_array_append_value(vector, other.ctx)
	}

	out := New("CONCATENATE")
	C.mlx_concatenate_axis(&out.ctx, vector, C.int(axis), DefaultStream().ctx)
	return out
}

// Cumsum 沿轴累加，可反向或非 inclusive。
func (t *Array) Cumsum(axis int, reverse, inclusive bool) *Array {
	out := New("CUMSUM")
	C.mlx_cumsum(&out.ctx, t.ctx, C.int(axis), C.bool(reverse), C.bool(inclusive), DefaultStream().ctx)
	return out
}

// Divide 逐元素相除。
func (t *Array) Divide(other *Array) *Array {
	out := New("DIVIDE")
	C.mlx_divide(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// ExpandDims 在 axis 插入长度为 1 的维。
func (t *Array) ExpandDims(axis int) *Array {
	out := New("EXPAND_DIMS")
	C.mlx_expand_dims(&out.ctx, t.ctx, C.int(axis), DefaultStream().ctx)
	return out
}

// Flatten 展平 [startAxis,endAxis] 区间。
func (t *Array) Flatten(startAxis, endAxis int) *Array {
	out := New("FLATTEN")
	C.mlx_flatten(&out.ctx, t.ctx, C.int(startAxis), C.int(endAxis), DefaultStream().ctx)
	return out
}

// FloorDivide 逐元素向下取整除。
func (t *Array) FloorDivide(other *Array) *Array {
	out := New("FLOOR_DIVIDE")
	C.mlx_floor_divide(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// GatherMM 对稀疏索引做分组矩阵乘。
func (t *Array) GatherMM(other, lhs, rhs *Array, sorted bool) *Array {
	if lhs == nil {
		lhs = New("")
	}
	if rhs == nil {
		rhs = New("")
	}
	out := New("GATHER_MM")
	C.mlx_gather_mm(&out.ctx, t.ctx, other.ctx, lhs.ctx, rhs.ctx, C.bool(sorted), DefaultStream().ctx)
	return out
}

// LogsumexpAxis 沿轴计算 log-sum-exp。
func (t *Array) LogsumexpAxis(axis int, keepDims bool) *Array {
	out := New("LOGSUMEXP_AXIS")
	C.mlx_logsumexp_axis(&out.ctx, t.ctx, C.int(axis), C.bool(keepDims), DefaultStream().ctx)
	return out
}

// Equal 逐元素相等比较。
func (t *Array) Equal(other *Array) *Array {
	out := New("EQUAL")
	C.mlx_equal(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// Greater 逐元素大于比较。
func (t *Array) Greater(other *Array) *Array {
	out := New("GREATER")
	C.mlx_greater(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// Less 逐元素小于比较。
func (t *Array) Less(other *Array) *Array {
	out := New("LESS")
	C.mlx_less(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// LessEqual 逐元素小于等于比较。
func (t *Array) LessEqual(other *Array) *Array {
	out := New("LESS_EQUAL")
	C.mlx_less_equal(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// MaxAxis 沿轴取最大值。
func (t *Array) MaxAxis(axis int, keepDims bool) *Array {
	out := New("MAX_AXIS")
	C.mlx_max_axis(&out.ctx, t.ctx, C.int(axis), C.bool(keepDims), DefaultStream().ctx)
	return out
}

// Matmul 矩阵乘法。
func (t *Array) Matmul(other *Array) *Array {
	out := New("MATMUL")
	C.mlx_matmul(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// Multiply 逐元素相乘。
func (t *Array) Multiply(other *Array) *Array {
	out := New("MULTIPLY")
	C.mlx_multiply(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// Negative 逐元素取负。
func (t *Array) Negative() *Array {
	out := New("NEGATIVE")
	C.mlx_negative(&out.ctx, t.ctx, DefaultStream().ctx)
	return out
}

// Power 逐元素幂运算。
func (t *Array) Power(exponent *Array) *Array {
	out := New("POWER")
	C.mlx_power(&out.ctx, t.ctx, exponent.ctx, DefaultStream().ctx)
	return out
}

// PutAlongAxis 沿轴按索引写入。
func (t *Array) PutAlongAxis(indices, values *Array, axis int) *Array {
	out := New("PUT_ALONG_AXIS")
	C.mlx_put_along_axis(&out.ctx, t.ctx, indices.ctx, values.ctx, C.int(axis), DefaultStream().ctx)
	return out
}

// ScatterAddAxis 沿轴 scatter-add。
func (t *Array) ScatterAddAxis(indices, values *Array, axis int) *Array {
	out := New("SCATTER_ADD_AXIS")
	C.mlx_scatter_add_axis(&out.ctx, t.ctx, indices.ctx, values.ctx, C.int(axis), DefaultStream().ctx)
	return out
}

// Reshape 重塑为给定 shape。
func (t *Array) Reshape(axes ...int) *Array {
	cAxes := make([]C.int, len(axes))
	for i := range axes {
		cAxes[i] = C.int(axes[i])
	}

	out := New("RESHAPE")
	C.mlx_reshape(&out.ctx, t.ctx, unsafe.SliceData(cAxes), C.size_t(len(cAxes)), DefaultStream().ctx)
	return out
}

// Sigmoid 逐元素 sigmoid。
func (t *Array) Sigmoid() *Array {
	out := New("SIGMOID")
	C.mlx_sigmoid(&out.ctx, t.ctx, DefaultStream().ctx)
	return out
}

// Sqrt 逐元素平方根。
func (t *Array) Sqrt() *Array {
	out := New("SQRT")
	C.mlx_sqrt(&out.ctx, t.ctx, DefaultStream().ctx)
	return out
}

// Squeeze 移除 axis 上长度为 1 的维。
func (t *Array) Squeeze(axis int) *Array {
	out := New("SQUEEZE")
	C.mlx_squeeze_axis(&out.ctx, t.ctx, C.int(axis), DefaultStream().ctx)
	return out
}

// StackAxis 沿 axis 堆叠张量。
func (t *Array) StackAxis(axis int, others ...*Array) *Array {
	vectorData := make([]C.mlx_array, len(others)+1)
	vectorData[0] = t.ctx
	for i := range others {
		vectorData[i+1] = others[i].ctx
	}

	vector := C.mlx_vector_array_new_data(unsafe.SliceData(vectorData), C.size_t(len(vectorData)))
	defer C.mlx_vector_array_free(vector)

	out := New("STACK_AXIS")
	C.mlx_stack_axis(&out.ctx, vector, C.int(axis), DefaultStream().ctx)
	return out
}

// Subtract 逐元素相减。
func (t *Array) Subtract(other *Array) *Array {
	out := New("SUBTRACT")
	C.mlx_subtract(&out.ctx, t.ctx, other.ctx, DefaultStream().ctx)
	return out
}

// SumAxis 沿轴求和。
func (t *Array) SumAxis(axis int, keepDims bool) *Array {
	out := New("SUM_AXIS")
	C.mlx_sum_axis(&out.ctx, t.ctx, C.int(axis), C.bool(keepDims), DefaultStream().ctx)
	return out
}

// TakeAxis 沿 axis 按 indices 取值。
func (t *Array) TakeAxis(indices *Array, axis int) *Array {
	out := New("TAKE_AXIS")
	C.mlx_take_axis(&out.ctx, t.ctx, indices.ctx, C.int(axis), DefaultStream().ctx)
	return out
}

// TakeAlongAxis 沿 axis 按 indices 取对应元素。
func (t *Array) TakeAlongAxis(indices *Array, axis int) *Array {
	out := New("TAKE_ALONG_AXIS")
	C.mlx_take_along_axis(&out.ctx, t.ctx, indices.ctx, C.int(axis), DefaultStream().ctx)
	return out
}

// Tanh 逐元素 tanh。
func (t *Array) Tanh() *Array {
	out := New("TANH")
	C.mlx_tanh(&out.ctx, t.ctx, DefaultStream().ctx)
	return out
}

// Transpose 按 axes 转置。
func (t *Array) Transpose(axes ...int) *Array {
	cAxes := make([]C.int, len(axes))
	for i, axis := range axes {
		cAxes[i] = C.int(axis)
	}

	out := New("TRANSPOSE")
	C.mlx_transpose_axes(&out.ctx, t.ctx, unsafe.SliceData(cAxes), C.size_t(len(cAxes)), DefaultStream().ctx)
	return out
}

// Zeros 创建指定 dtype 与 shape 的全零张量。
func Zeros(dtype DType, shape ...int) *Array {
	cAxes := make([]C.int, len(shape))
	for i := range shape {
		cAxes[i] = C.int(shape[i])
	}

	t := New("ZEROS")
	C.mlx_zeros(&t.ctx, unsafe.SliceData(cAxes), C.size_t(len(cAxes)), C.mlx_dtype(dtype), DefaultStream().ctx)
	return t
}
