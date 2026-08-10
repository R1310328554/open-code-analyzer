// MLX 神经网络层：Linear 与 Embedding 的前向封装。
package mlx

// Linear 线性层，含权重与可选偏置。
type Linear struct {
	Weight *Array `weight:"weight"`
	Bias   *Array `weight:"bias"`
}

// Forward 计算线性变换 x @ Weight.T + Bias。
// Forward computes the linear transformation: x @ Weight.T + Bias
func (m *Linear) Forward(x *Array) *Array {
	w := m.Weight.Transpose(1, 0)
	if m.Bias.Valid() {
		return m.Bias.Addmm(x, w, 1.0, 1.0)
	}

	return x.Matmul(w)
}

// Gather 对转置权重执行 GatherMM，用于 MoE 路由。
func (m *Linear) Gather(x, lhs, rhs *Array, sorted bool) *Array {
	w := m.Weight.Transpose(0, 2, 1)
	// TODO: 暂未融合 bias
	// TODO: bias
	return x.GatherMM(w, lhs, rhs, sorted)
}

// Embedding 嵌入层，按索引查表。
type Embedding struct {
	Weight *Array `weight:"weight"`
}

// Forward 沿 axis 0 对 indices 做 Take。
func (e *Embedding) Forward(indices *Array) *Array {
	return e.Weight.TakeAxis(indices, 0)
}

// AsLinear 将嵌入权重视为无偏置线性层。
func (e *Embedding) AsLinear() Linear {
	return Linear{
		Weight: e.Weight,
	}
}
