// MLX 激活函数：GELU/SiLU/SwiGLU 等编译融合核与 MoE 路由 sigmoid。
package mlx

import "math"

var geluCoeff = float32(math.Sqrt(2 / math.Pi))

// GELUApprox 返回 tanh 近似 GELU，编译为单融合核。
// GELUApprox returns 0.5 * x * (1 + tanh(sqrt(2/pi) * (x + 0.044715 * x^3)))
// as a fused kernel.
var GELUApprox = Compile1(
	"GELUApprox",
	func(x *Array) *Array {
		// 标量 dtype 与输入一致，避免 bf16 隐式上 cast。
		// Dtype-matched scalars avoid implicit upcasts on bf16 inputs.
		dt := x.DType()
		half := FromValue[float32](0.5).AsType(dt)
		coeff := FromValue(geluCoeff).AsType(dt)
		c := FromValue[float32](0.044715).AsType(dt)
		one := FromValue[float32](1.0).AsType(dt)

		// 用 x*x*x 代替通用 Power，更快。
		// x^3 via x*x*x (avoids general Power which is slower).
		x3 := x.Multiply(x).Multiply(x)
		inner := x.Add(c.Multiply(x3))
		tanh := coeff.Multiply(inner).Tanh()
		return half.Multiply(x).Multiply(one.Add(tanh))
	},
	Shapeless(),
)

// gelu 精确 erf 形式 GELU 的内部实现。
func gelu(x *Array) *Array {
	dt := x.DType()
	half := FromValue[float32](0.5).AsType(dt)
	one := FromValue[float32](1).AsType(dt)
	invSqrt2 := FromValue(float32(1 / math.Sqrt2)).AsType(dt)
	return half.Multiply(x).Multiply(one.Add(erf(x.Multiply(invSqrt2))))
}

// GELU 返回与 torch.nn.functional.gelu 一致的 erf 精确形式。
// GELU returns the exact erf formulation used by torch.nn.functional.gelu.
var GELU = Compile1("GELU", gelu, Shapeless())

// SiLU 返回 a*sigmoid(a) 融合核。
// SiLU returns a * sigmoid(a) as a fused kernel.
var SiLU = Compile1(
	"SiLU",
	func(a *Array) *Array {
		return a.Multiply(a.Sigmoid())
	},
	Shapeless(),
)

// SoftplusF32 在 float32 上算 softplus 再 cast 回原 dtype，匹配 laguna 门控公式。
// SoftplusF32 returns softplus(x) computed in float32 precision and cast back
// to x's original dtype, as a fused kernel. Matches the laguna attention
// output-gate formula: softplus(cast_f32(x)).cast(orig_dtype).
var SoftplusF32 = Compile1(
	"SoftplusF32",
	func(x *Array) *Array {
		dt := x.DType()
		zero := FromValue[float32](0)
		return Logaddexp(x.AsType(DTypeFloat32), zero).AsType(dt)
	},
	Shapeless(),
)

// SwiGLU 返回 silu(gate)*up 融合核。
// SwiGLU returns silu(gate) * up as a fused kernel.
var SwiGLU = Compile2(
	"SwiGLU",
	func(gate, up *Array) *Array {
		return SiLU(gate).Multiply(up)
	},
	Shapeless(),
)

// GeGLU 返回 gelu_approx(gate)*up，与 mlx_lm geglu 及 Gemma MLP/MoE 一致。
// GeGLU returns gelu_approx(gate) * up as a fused kernel. Matches mlx_lm's
// geglu, used by Gemma-family MLP and MoE paths.
var GeGLU = Compile2(
	"GeGLU",
	func(gate, up *Array) *Array {
		return GELUApprox(gate).Multiply(up)
	},
	Shapeless(),
)

// LogitSoftcap 返回 tanh(x/cap)*cap，与 mlx_lm logit_softcap 一致；cap 须与 x 同 dtype。
// LogitSoftcap returns tanh(x / cap) * cap as a fused kernel. Matches
// mlx_lm's logit_softcap. cap must have the same dtype as x.
var LogitSoftcap = Compile2(
	"LogitSoftcap",
	func(x, cap *Array) *Array {
		return x.Divide(cap).Tanh().Multiply(cap)
	},
	Shapeless(),
)

// sigmoidRouterFused 追踪 DeepSeek-V2/GLM-MoE 无 aux-loss 路由头，双输出共享单核。
// sigmoidRouterFused traces the DeepSeek-V2 / GLM-MoE aux-loss-free router
// head. Two outputs are returned so the pre-bias sigmoid (used to gather
// per-expert scores after top-k) and the post-bias negation (used as the
// argpartition key for top-k) share a single kernel.
var sigmoidRouterFused = Compile(
	"SigmoidRouter",
	func(in ...*Array) []*Array {
		gates, bias := in[0], in[1]
		orig := gates.Sigmoid()
		neg := orig.Add(bias).Negative()
		return []*Array{orig, neg}
	},
	Shapeless(),
)

// SigmoidRouter 返回 (sigmoid(gates), -(sigmoid(gates)+bias)) 融合核输出。
// SigmoidRouter returns (sigmoid(gates), -(sigmoid(gates)+bias)) as a fused
// kernel — the DeepSeek-V2 / GLM-MoE aux-loss-free router head.
func SigmoidRouter(gates, bias *Array) (origScores, negScores *Array) {
	out := sigmoidRouterFused(gates, bias)
	return out[0], out[1]
}
