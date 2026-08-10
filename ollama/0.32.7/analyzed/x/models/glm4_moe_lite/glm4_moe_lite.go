// Package glm4_moe_lite 提供 GLM4-MoE-Lite 的 MLX 实现（MLA 多头潜注意力 + MoE）。
// Package glm4_moe_lite provides the GLM4-MoE-Lite implementation for MLX.
// 本模型采用 MLA（多头潜注意力）与 MoE（混合专家）架构。
// This model uses Multi-head Latent Attention (MLA) and Mixture of Experts (MoE).
package glm4_moe_lite

import (
	"encoding/json"
	"fmt"
	"math"

	"github.com/ollama/ollama/x/mlxrunner/batch"
	"github.com/ollama/ollama/x/mlxrunner/cache"
	"github.com/ollama/ollama/x/mlxrunner/mlx"
	"github.com/ollama/ollama/x/mlxrunner/model"
	"github.com/ollama/ollama/x/mlxrunner/model/base"
	"github.com/ollama/ollama/x/models/nn"
	"github.com/ollama/ollama/x/tokenizer"
)

func init() {
	base.Register("Glm4MoeLiteForCausalLM", newModel)
	base.Register("GLM4MoeLite", newModel)
}

// RopeScaling 保存 RoPE 缩放配置。
// RopeScaling holds RoPE scaling configuration
type RopeScaling struct {
	Factor       float32 `json:"factor"`
	MscaleAllDim float32 `json:"mscale_all_dim"`
}

// Config 保存 GLM4-MoE-Lite 模型超参与量化运行时字段。
// Config holds GLM4-MoE-Lite model configuration
type Config struct {
	HiddenSize            int32   `json:"hidden_size"`
	NumHiddenLayers       int32   `json:"num_hidden_layers"`
	IntermediateSize      int32   `json:"intermediate_size"`
	MoEIntermediateSize   int32   `json:"moe_intermediate_size"`
	NumAttentionHeads     int32   `json:"num_attention_heads"`
	NumKeyValueHeads      int32   `json:"num_key_value_heads"`
	VocabSize             int32   `json:"vocab_size"`
	RMSNormEps            float32 `json:"rms_norm_eps"`
	RopeTheta             float32 `json:"rope_theta"`
	MaxPositionEmbeddings int32   `json:"max_position_embeddings"`
	AttentionBias         bool    `json:"attention_bias"`

	// MLA（多头潜注意力）低秩投影参数。
	// MLA (Multi-head Latent Attention) parameters
	QLoraRank     int32 `json:"q_lora_rank"`
	KVLoraRank    int32 `json:"kv_lora_rank"`
	QKRopeHeadDim int32 `json:"qk_rope_head_dim"`
	QKNopeHeadDim int32 `json:"qk_nope_head_dim"`
	VHeadDim      int32 `json:"v_head_dim"`

	// MoE 路由与专家相关参数。
	// MoE parameters
	NRoutedExperts      int32   `json:"n_routed_experts"`
	NSharedExperts      int32   `json:"n_shared_experts"`
	NumExpertsPerTok    int32   `json:"num_experts_per_tok"`
	RoutedScalingFactor float32 `json:"routed_scaling_factor"`
	NormTopKProb        bool    `json:"norm_topk_prob"`
	FirstKDenseReplace  int32   `json:"first_k_dense_replace"`
	NGroup              int32   `json:"n_group"`
	TopKGroup           int32   `json:"topk_group"`

	// RoPE 长度外推缩放。
	// RoPE scaling
	RopeScaling *RopeScaling `json:"rope_scaling"`

	// 加载时根据量化类型填充的量化参数。
	// Quantization parameters (set during load based on model quantization)
	QuantGroupSize int                               `json:"-"` // Group size for quantization (default 64)
	QuantBits      int                               `json:"-"` // Bits per weight (4 or 8)
	QuantMode      string                            `json:"-"` // Quantization mode ("affine", etc.)
	TensorQuant    map[string]*model.TensorQuantInfo `json:"-"`

	// 派生字段（头维、注意力缩放等）。
	// Computed fields
	QHeadDim int32   `json:"-"` // qk_nope_head_dim + qk_rope_head_dim
	Scale    float32 `json:"-"` // 1/sqrt(QHeadDim) with mscale adjustment
}

// MLAAttention 实现带权重吸收的 MLA 注意力。
// MLAAttention implements Multi-head Latent Attention with absorption.
type MLAAttention struct {
	QAProj      nn.LinearLayer
	QALayerNorm *nn.RMSNorm
	QBProj      nn.LinearLayer

	KVAProjWithMQA nn.LinearLayer
	KVALayerNorm   *nn.RMSNorm

	EmbedQ     *nn.MultiLinear
	UnembedOut *nn.MultiLinear

	OProj nn.LinearLayer
}

// Forward 计算吸收式 MLA 注意力输出。
// Forward computes absorbed MLA attention output.
func (a *MLAAttention) Forward(x *mlx.Array, b *batch.Batch, c cache.Cache, positions *mlx.Array, B, L int32, cfg *Config) *mlx.Array {
	q := a.QAProj.Forward(x)
	q = a.QALayerNorm.Forward(q, cfg.RMSNormEps)
	q = a.QBProj.Forward(q)

	q = mlx.Reshape(q, B, L, cfg.NumAttentionHeads, cfg.QHeadDim)
	q = mlx.Transpose(q, 0, 2, 1, 3)

	qNope := mlx.SliceStartStop(q, []int32{0, 0, 0, 0}, []int32{B, cfg.NumAttentionHeads, L, cfg.QKNopeHeadDim})
	qPE := mlx.SliceStartStop(q, []int32{0, 0, 0, cfg.QKNopeHeadDim}, []int32{B, cfg.NumAttentionHeads, L, cfg.QHeadDim})

	compressedKV := a.KVAProjWithMQA.Forward(x)

	kvCompressed := mlx.SliceStartStop(compressedKV, []int32{0, 0, 0}, []int32{B, L, cfg.KVLoraRank})
	kPE := mlx.SliceStartStop(compressedKV, []int32{0, 0, cfg.KVLoraRank}, []int32{B, L, cfg.KVLoraRank + cfg.QKRopeHeadDim})

	kPE = mlx.Reshape(kPE, B, L, 1, cfg.QKRopeHeadDim)
	kPE = mlx.Transpose(kPE, 0, 2, 1, 3)

	kvLatent := a.KVALayerNorm.Forward(kvCompressed, cfg.RMSNormEps)
	kvLatent = mlx.ExpandDims(kvLatent, 1)

	qPE = mlx.RoPEWithBase(qPE, int(cfg.QKRopeHeadDim), true, cfg.RopeTheta, 1.0, positions)
	kPE = mlx.RoPEWithBase(kPE, int(cfg.QKRopeHeadDim), true, cfg.RopeTheta, 1.0, positions)

	qLatent := a.EmbedQ.Forward(qNope)

	keys := mlx.Concatenate([]*mlx.Array{kvLatent, kPE}, 3)

	// MLA 将 K/V 压缩为单一张量：缓存沿末维存 [kvLatent,kPE]，
	// V 取 kvLatent 前缀；WithMLAHistory 负责历史切片。
	// MLA compresses K and V into a single tensor: the cache stores
	// [kvLatent, kPE] concatenated along the last dim as its keys,
	// and V is the kvLatent prefix (first KVLoraRank positions) of
	// that same tensor. WithMLAHistory handles the slice on our
	// behalf so the model never touches the history's K/V.
	var kv nn.SDPAOption
	if c != nil {
		placeholderValues := mlx.ZerosF32([]int32{B, 1, L, 0})
		history := c.(cache.Attention).Update(b, keys, placeholderValues)
		kv = nn.WithMLAHistory(history, int(cfg.KVLoraRank))
	} else {
		values := mlx.SliceStartStop(keys, []int32{0, 0, 0, 0}, []int32{B, 1, L, cfg.KVLoraRank})
		kv = nn.WithKV(keys, values, b.SeqQueryLens)
	}

	queries := mlx.Concatenate([]*mlx.Array{qLatent, qPE}, 3)

	out := nn.ScaledDotProductAttention(b, queries, cfg.Scale, kv, nn.WithMask(nn.CausalMask()))
	out = a.UnembedOut.Forward(out)

	out = mlx.Reshape(mlx.Transpose(out, 0, 2, 1, 3), B, L, cfg.NumAttentionHeads*cfg.VHeadDim)

	return a.OProj.Forward(out)
}

// DenseMLP 为稠密层实现标准 SwiGLU MLP。
// DenseMLP implements the standard SwiGLU MLP for dense layers
type DenseMLP struct {
	GateProj nn.LinearLayer
	UpProj   nn.LinearLayer
	DownProj nn.LinearLayer
}

// Forward 执行 SwiGLU 前馈。
// Forward applies the SwiGLU MLP
func (m *DenseMLP) Forward(x *mlx.Array) *mlx.Array {
	return m.DownProj.Forward(mlx.SwiGLU(m.GateProj.Forward(x), m.UpProj.Forward(x)))
}

// MoEGate 实现专家路由门控。
// MoEGate implements the expert gating mechanism
type MoEGate struct {
	Gate                 nn.LinearLayer
	EScoreCorrectionBias *mlx.Array
}

// Forward 返回 top-k 专家索引与路由分数。
// Forward computes expert selection indices and scores
func (g *MoEGate) Forward(x *mlx.Array, cfg *Config) (*mlx.Array, *mlx.Array) {
	gates := g.Gate.Forward(x)

	var origScores, negScores *mlx.Array
	if g.EScoreCorrectionBias != nil {
		origScores, negScores = mlx.SigmoidRouter(gates, g.EScoreCorrectionBias)
	} else {
		origScores = mlx.Sigmoid(gates)
		negScores = mlx.Neg(origScores)
	}

	topK := cfg.NumExpertsPerTok
	inds := mlx.Argpartition(negScores, int(topK)-1, -1)

	dims := inds.Dims()
	inds = mlx.SliceStartStop(inds, []int32{0, 0, 0}, []int32{int32(dims[0]), int32(dims[1]), topK})

	scores := mlx.TakeAlongAxis(origScores, inds, -1)

	if topK > 1 && cfg.NormTopKProb {
		sumScores := mlx.Sum(scores, -1, true)
		scores = mlx.Div(scores, sumScores)
	}

	scores = mlx.MulScalar(scores, cfg.RoutedScalingFactor)

	return inds, scores
}

// SwitchMLP 用堆叠权重执行路由专家 MLP（支持 GatherMM/QMM）。
// SwitchMLP implements the MoE expert computation using stacked weights
type SwitchMLP struct {
	GateWeight *mlx.Array
	UpWeight   *mlx.Array
	DownWeight *mlx.Array

	GateWeightQ, GateScales, GateBiases *mlx.Array
	UpWeightQ, UpScales, UpBiases       *mlx.Array
	DownWeightQ, DownScales, DownBiases *mlx.Array

	GateBits int
	UpBits   int
	DownBits int

	GateGroupSize int
	UpGroupSize   int
	DownGroupSize int

	UseQuantized bool
}

// Forward 对选中专家执行 SwiGLU 并返回 [B,L,topK,H]。
// Forward applies the switched expert MLP
func (s *SwitchMLP) Forward(x *mlx.Array, indices *mlx.Array, cfg *Config) *mlx.Array {
	dims := x.Dims()
	B, L := int32(dims[0]), int32(dims[1])
	topK := cfg.NumExpertsPerTok

	xExpanded := mlx.ExpandDims(mlx.ExpandDims(x, -2), -2)

	xFlat := mlx.Reshape(xExpanded, B*L, 1, 1, cfg.HiddenSize)

	idxFlat := mlx.Reshape(indices, B*L, topK)

	doSort := B*L >= 64
	var invOrder *mlx.Array
	n := B * L * topK

	if doSort {
		idxAll := mlx.Flatten(idxFlat)
		order := mlx.Argsort(idxAll, 0)
		invOrder = mlx.Argsort(order, 0)
		xFlat = mlx.ExpandDims(mlx.Take(mlx.Squeeze(xFlat, 1), mlx.FloorDivideScalar(order, topK), 0), 1)
		idxFlat = mlx.Reshape(mlx.Take(idxAll, order, 0), n, 1)
	}

	var gate, up, hidden, down *mlx.Array

	if s.UseQuantized {
		gate = mlx.GatherQMM(xFlat, s.GateWeightQ, s.GateScales, s.GateBiases,
			nil, idxFlat, true, s.GateGroupSize, s.GateBits, cfg.QuantMode, doSort)
		up = mlx.GatherQMM(xFlat, s.UpWeightQ, s.UpScales, s.UpBiases,
			nil, idxFlat, true, s.UpGroupSize, s.UpBits, cfg.QuantMode, doSort)

		hidden = mlx.SwiGLU(gate, up)

		down = mlx.GatherQMM(hidden, s.DownWeightQ, s.DownScales, s.DownBiases,
			nil, idxFlat, true, s.DownGroupSize, s.DownBits, cfg.QuantMode, doSort)
	} else {
		gate = mlx.GatherMM(xFlat, mlx.Transpose(s.GateWeight, 0, 2, 1), nil, idxFlat, doSort)
		up = mlx.GatherMM(xFlat, mlx.Transpose(s.UpWeight, 0, 2, 1), nil, idxFlat, doSort)

		hidden = mlx.SwiGLU(gate, up)

		down = mlx.GatherMM(hidden, mlx.Transpose(s.DownWeight, 0, 2, 1), nil, idxFlat, doSort)
	}

	if doSort {
		down = mlx.Reshape(mlx.Take(mlx.Squeeze(mlx.Squeeze(down, 2), 1), invOrder, 0), B*L, topK, cfg.HiddenSize)
	} else {
		down = mlx.Squeeze(down, 2)
	}

	return mlx.Reshape(down, B, L, topK, cfg.HiddenSize)
}

// SharedExperts 实现共享专家 SwiGLU MLP。
// SharedExperts implements the shared expert MLP
type SharedExperts struct {
	GateProj nn.LinearLayer
	UpProj   nn.LinearLayer
	DownProj nn.LinearLayer
}

// Forward applies the shared expert MLP
func (s *SharedExperts) Forward(x *mlx.Array) *mlx.Array {
	return s.DownProj.Forward(mlx.SwiGLU(s.GateProj.Forward(x), s.UpProj.Forward(x)))
}

// MoE 组合路由门、SwitchMLP 与可选共享专家。
// MoE implements the full Mixture of Experts layer
type MoE struct {
	Gate          *MoEGate
	SwitchMLP     *SwitchMLP
	SharedExperts *SharedExperts
}

// Forward applies the MoE layer
func (m *MoE) Forward(x *mlx.Array, cfg *Config) *mlx.Array {
	dims := x.Dims()
	B, L := int32(dims[0]), int32(dims[1])

	inds, scores := m.Gate.Forward(x, cfg)

	expertOut := m.SwitchMLP.Forward(x, inds, cfg)

	scoresExpanded := mlx.ExpandDims(scores, -1)
	y := mlx.Sum(mlx.Mul(expertOut, scoresExpanded), 2, false)

	if m.SharedExperts != nil {
		y = mlx.Add(y, m.SharedExperts.Forward(x))
	}

	return mlx.Reshape(y, B, L, cfg.HiddenSize)
}

// DenseBlock 为前若干层的稠密 Transformer 块。
// DenseBlock represents a dense transformer block (for first_k_dense_replace layers)
type DenseBlock struct {
	Attention              *MLAAttention
	MLP                    *DenseMLP
	InputLayerNorm         *nn.RMSNorm
	PostAttentionLayerNorm *nn.RMSNorm
}

// Forward applies the dense block
func (blk *DenseBlock) Forward(x *mlx.Array, b *batch.Batch, c cache.Cache, positions *mlx.Array, B, L int32, cfg *Config) *mlx.Array {
	r := blk.Attention.Forward(blk.InputLayerNorm.Forward(x, cfg.RMSNormEps), b, c, positions, B, L, cfg)
	h := mlx.Add(x, r)

	r = blk.MLP.Forward(blk.PostAttentionLayerNorm.Forward(h, cfg.RMSNormEps))
	return mlx.Add(h, r)
}

// MoEBlock 为 MoE Transformer 块。
// MoEBlock represents a MoE transformer block
type MoEBlock struct {
	Attention              *MLAAttention
	MoE                    *MoE
	InputLayerNorm         *nn.RMSNorm
	PostAttentionLayerNorm *nn.RMSNorm
}

// Forward applies the MoE block
func (blk *MoEBlock) Forward(x *mlx.Array, b *batch.Batch, c cache.Cache, positions *mlx.Array, B, L int32, cfg *Config) *mlx.Array {
	r := blk.Attention.Forward(blk.InputLayerNorm.Forward(x, cfg.RMSNormEps), b, c, positions, B, L, cfg)
	h := mlx.Add(x, r)

	r = blk.MoE.Forward(blk.PostAttentionLayerNorm.Forward(h, cfg.RMSNormEps), cfg)
	return mlx.Add(h, r)
}

// Block 为稠密块与 MoE 块的统一接口。
// Block interface for both dense and MoE blocks
type Block interface {
	Forward(x *mlx.Array, b *batch.Batch, c cache.Cache, positions *mlx.Array, B, L int32, cfg *Config) *mlx.Array
}

// Model 为完整 GLM4-MoE-Lite 因果语言模型。
// Model represents the complete GLM4-MoE-Lite model
type Model struct {
	EmbedTokens nn.EmbeddingLayer
	Layers      []Block
	Norm        *nn.RMSNorm
	LMHead      nn.LinearLayer

	tok *tokenizer.Tokenizer
	*Config
}

// computeScale 计算注意力缩放（含 YaRN mscale 修正）。
// computeScale computes the attention scale.
func computeScale(cfg *Config) float32 {
	keyLength := cfg.QKNopeHeadDim + cfg.QKRopeHeadDim
	scale := float32(1.0 / math.Sqrt(float64(keyLength)))
	if cfg.RopeScaling != nil && cfg.RopeScaling.MscaleAllDim > 0 && cfg.RopeScaling.Factor > 1 {
		s := 0.1*cfg.RopeScaling.MscaleAllDim*float32(math.Log(float64(cfg.RopeScaling.Factor))) + 1.0
		scale *= s * s
	}
	return scale
}

// supportsGatherQMM 判断量化模式是否支持 GatherQMM 内核。
// supportsGatherQMM returns true if the quantization mode has GatherQMM kernel support.
func supportsGatherQMM(mode string, bits int) bool {
	return mode == "affine" && (bits == 4 || bits == 8)
}

// ExpertWeight 保存单个专家的权重及可选量化分量。
// ExpertWeight holds a single expert's weight with optional quantization components.
type ExpertWeight struct {
	Weight    *mlx.Array
	Scales    *mlx.Array
	Biases    *mlx.Array
	Bits      int
	GroupSize int
}

// loadExpertWeight 从张量表加载单个专家投影权重。
// loadExpertWeight loads an expert weight from the tensor map.
func loadExpertWeight(tensors map[string]*mlx.Array, path string, useQuantized bool, cfg *Config) *ExpertWeight {
	w := tensors[path+".weight"]
	if w == nil {
		return nil
	}

	scales := tensors[path+".weight_scale"]
	if scales != nil {
		qbiases := tensors[path+".weight_qbias"]

		groupSize, bits, mode := model.ResolveLinearQuantParams(
			cfg.QuantGroupSize,
			cfg.QuantBits,
			cfg.QuantMode,
			cfg.TensorQuant,
			path+".weight",
			w,
			scales,
		)

		if useQuantized && supportsGatherQMM(mode, bits) {
			return &ExpertWeight{Weight: w, Scales: scales, Biases: qbiases, Bits: bits, GroupSize: groupSize}
		}

		return &ExpertWeight{Weight: mlx.Dequantize(w, scales, qbiases, groupSize, bits, mode, nil)}
	}

	return &ExpertWeight{Weight: w}
}

// StackedExpertWeights 保存堆叠后的全专家权重。
// StackedExpertWeights holds stacked weights for all experts.
type StackedExpertWeights struct {
	Weight    *mlx.Array
	Scales    *mlx.Array
	Biases    *mlx.Array
	Bits      int
	GroupSize int
}

// loadStackedProjection 加载已堆叠的 3D 专家投影张量。
// loadStackedProjection loads an expert projection stored as a single stacked
// 3D tensor, or nil if base isn't present.
func loadStackedProjection(tensors map[string]*mlx.Array, base string, useQuantized bool, cfg *Config) *StackedExpertWeights {
	key := base + ".weight"
	w := tensors[key]
	if w == nil {
		return nil
	}

	scales := tensors[key+"_scale"]
	if scales == nil {
		return &StackedExpertWeights{Weight: w}
	}

	qbiases := tensors[key+"_qbias"]
	groupSize, bits, mode := model.ResolveLinearQuantParams(
		cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode, cfg.TensorQuant,
		key, w, scales,
	)
	if useQuantized && supportsGatherQMM(mode, bits) {
		return &StackedExpertWeights{Weight: w, Scales: scales, Biases: qbiases, Bits: bits, GroupSize: groupSize}
	}

	return &StackedExpertWeights{Weight: mlx.Dequantize(w, scales, qbiases, groupSize, bits, mode, nil)}
}

// loadStackedExperts 优先 .experts 路径，回退 switch_mlp 旧命名。
// loadStackedExperts loads a stacked expert projection by its .experts name,
// falling back to the switch_mlp name older imports use.
func loadStackedExperts(tensors map[string]*mlx.Array, prefix, projName string, useQuantized bool, cfg *Config) *StackedExpertWeights {
	if w := loadStackedProjection(tensors, prefix+".mlp.experts."+projName, useQuantized, cfg); w != nil {
		return w
	}
	return loadStackedProjection(tensors, prefix+".mlp.switch_mlp."+projName, useQuantized, cfg)
}

// collectAndStackExpertWeights 逐专家收集并沿 axis 0 堆叠。
// collectAndStackExpertWeights loads and stacks expert weights for one projection type.
func collectAndStackExpertWeights(
	tensors map[string]*mlx.Array,
	prefix string,
	projName string,
	numExperts int32,
	useQuantized bool,
	cfg *Config,
) *StackedExpertWeights {
	var w, s, b []*mlx.Array
	var bits, groupSize int

	for e := range numExperts {
		path := fmt.Sprintf("%s.mlp.experts.%d.%s", prefix, e, projName)
		ew := loadExpertWeight(tensors, path, useQuantized, cfg)
		if ew == nil {
			continue
		}
		w = append(w, ew.Weight)
		if ew.Scales != nil {
			s = append(s, ew.Scales)
		}
		if ew.Biases != nil {
			b = append(b, ew.Biases)
		}
		if e == 0 {
			bits = ew.Bits
			groupSize = ew.GroupSize
		}
	}

	result := &StackedExpertWeights{Bits: bits, GroupSize: groupSize}
	if len(w) > 0 {
		result.Weight = mlx.Stack(w, 0)
		if len(s) > 0 {
			result.Scales = mlx.Stack(s, 0)
		}
		if len(b) > 0 {
			result.Biases = mlx.Stack(b, 0)
		}
	}
	return result
}

// sanitizeExpertWeights 解析 gate/up/down，优先堆叠布局。
// sanitizeExpertWeights resolves the three MoE projections, preferring the
// stacked on-disk layout and falling back to per-expert tensors.
func sanitizeExpertWeights(tensors map[string]*mlx.Array, prefix string, numExperts int32, useQuantized bool, cfg *Config) (gate, up, down *StackedExpertWeights) {
	gate = loadStackedExperts(tensors, prefix, "gate_proj", useQuantized, cfg)
	up = loadStackedExperts(tensors, prefix, "up_proj", useQuantized, cfg)
	down = loadStackedExperts(tensors, prefix, "down_proj", useQuantized, cfg)
	if gate != nil && up != nil && down != nil {
		return gate, up, down
	}
	gate = collectAndStackExpertWeights(tensors, prefix, "gate_proj", numExperts, useQuantized, cfg)
	up = collectAndStackExpertWeights(tensors, prefix, "up_proj", numExperts, useQuantized, cfg)
	down = collectAndStackExpertWeights(tensors, prefix, "down_proj", numExperts, useQuantized, cfg)
	return gate, up, down
}

// sanitizeMLAWeights 将 kv_b_proj 拆为 embedQ/unembedOut 吸收格式。
// sanitizeMLAWeights transforms kv_b_proj weights into absorbed MLA format.
func sanitizeMLAWeights(tensors map[string]*mlx.Array, prefix string, cfg *Config) (*mlx.Array, *mlx.Array) {
	path := prefix + ".self_attn.kv_b_proj"
	w := tensors[path+".weight"]
	if w == nil {
		return nil, nil
	}

	// 若已量化则先反量化。
	// Check if quantized and dequantize
	if scales := tensors[path+".weight_scale"]; scales != nil {
		qbiases := tensors[path+".weight_qbias"]
		groupSize, bits, mode := model.ResolveLinearQuantParams(
			cfg.QuantGroupSize,
			cfg.QuantBits,
			cfg.QuantMode,
			cfg.TensorQuant,
			path+".weight",
			w,
			scales,
		)
		w = mlx.Dequantize(w, scales, qbiases, groupSize, bits, mode, nil)
	}

	headDim := cfg.QKNopeHeadDim + cfg.VHeadDim
	w = mlx.Reshape(w, cfg.NumAttentionHeads, headDim, cfg.KVLoraRank)

	wk := mlx.SliceStartStop(w, []int32{0, 0, 0}, []int32{cfg.NumAttentionHeads, cfg.QKNopeHeadDim, cfg.KVLoraRank})
	wv := mlx.SliceStartStop(w, []int32{0, cfg.QKNopeHeadDim, 0}, []int32{cfg.NumAttentionHeads, headDim, cfg.KVLoraRank})

	embedQ := mlx.Transpose(wk, 0, 2, 1)
	unembedOut := wv

	return embedQ, unembedOut
}

// newModel 从 Root 创建模型（读 config 与 tokenizer，尚未加载权重）。
// newModel creates a new GLM4-MoE-Lite model from a Root (config + tokenizer,
// no weights loaded yet). Called by the registry via base.New().
func newModel(root *model.Root) (base.Model, error) {
	configData, err := root.Manifest.ReadConfig("config.json")
	if err != nil {
		return nil, fmt.Errorf("load config: %w", err)
	}

	var cfg Config
	if err := json.Unmarshal(configData, &cfg); err != nil {
		return nil, fmt.Errorf("parse config: %w", err)
	}

	cfg.QHeadDim = cfg.QKNopeHeadDim + cfg.QKRopeHeadDim
	cfg.Scale = computeScale(&cfg)

	// 从 manifest 预扫描元数据设置量化参数。
	// Set up quantization parameters from pre-scanned metadata
	if qt := root.QuantType(); qt != "" {
		cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode = model.QuantizationParams(qt)
		if gs := root.GroupSize(); gs > 0 {
			cfg.QuantGroupSize = gs
		}
	} else {
		cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode = model.QuantizationParams("")
	}
	cfg.TensorQuant = root.AllTensorQuant()

	// 加载 tokenizer.json 及相关配置。
	// Load tokenizer
	tokData, err := root.Manifest.ReadConfig("tokenizer.json")
	if err != nil {
		return nil, fmt.Errorf("load tokenizer config: %w", err)
	}

	tokConfig := &tokenizer.TokenizerConfig{
		ConfigJSON: configData,
	}

	if genConfigData, err := root.Manifest.ReadConfig("generation_config.json"); err == nil {
		tokConfig.GenerationConfigJSON = genConfigData
	}

	if tokConfigData, err := root.Manifest.ReadConfig("tokenizer_config.json"); err == nil {
		tokConfig.TokenizerConfigJSON = tokConfigData
	}

	tok, err := tokenizer.LoadFromBytesWithConfig(tokData, tokConfig)
	if err != nil {
		return nil, fmt.Errorf("parse tokenizer: %w", err)
	}

	m := &Model{
		Layers: make([]Block, cfg.NumHiddenLayers),
		Config: &cfg,
		tok:    tok,
	}

	return m, nil
}

// LoadWeights 加载全部权重：MLA 吸收、专家堆叠与量化层。
// LoadWeights receives all tensors loaded from the manifest and assigns them
// to model fields. Handles MLA absorption, expert stacking, and quantized
// layer creation.
func (m *Model) LoadWeights(tensors map[string]*mlx.Array) error {
	cfg := m.Config
	linears := model.NewLinearFactory(tensors, cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode, cfg.TensorQuant)
	useQuantized := supportsGatherQMM(cfg.QuantMode, cfg.QuantBits)
	if !useQuantized && cfg.TensorQuant != nil {
		for _, tq := range cfg.TensorQuant {
			if tq == nil {
				continue
			}
			_, bits, mode := model.QuantizationParams(tq.QuantType)
			if supportsGatherQMM(mode, bits) {
				useQuantized = true
				break
			}
		}
	}

	// 加载词嵌入。
	// Load embedding
	m.EmbedTokens = model.MakeEmbeddingLayer(tensors, "model.embed_tokens", cfg.QuantGroupSize, cfg.QuantBits, cfg.QuantMode, cfg.TensorQuant)

	// 加载最终 RMSNorm。
	// Load final norm
	if w := tensors["model.norm.weight"]; w != nil {
		m.Norm = nn.NewRMSNorm(w, cfg.RMSNormEps)
	}

	// 加载语言模型输出头。
	// Load LM head
	m.LMHead = linears.Make("lm_head")

	// 逐层加载注意力与 MLP/MoE。
	// Load layers
	for i := range cfg.NumHiddenLayers {
		prefix := fmt.Sprintf("model.layers.%d", i)

		// 注意力结构对稠密/MoE 块相同。
		// Load attention (same for both block types)
		attn := &MLAAttention{}
		attn.QAProj = linears.Make(prefix + ".self_attn.q_a_proj")
		if w := tensors[prefix+".self_attn.q_a_layernorm.weight"]; w != nil {
			attn.QALayerNorm = nn.NewRMSNorm(w, cfg.RMSNormEps)
		}
		attn.QBProj = linears.Make(prefix + ".self_attn.q_b_proj")
		attn.KVAProjWithMQA = linears.Make(prefix + ".self_attn.kv_a_proj_with_mqa")
		if w := tensors[prefix+".self_attn.kv_a_layernorm.weight"]; w != nil {
			attn.KVALayerNorm = nn.NewRMSNorm(w, cfg.RMSNormEps)
		}
		attn.OProj = linears.Make(prefix + ".self_attn.o_proj")

		// 将 kv_b 转为吸收式 MLA 权重。
		// Sanitize MLA weights for absorbed attention
		embedQ, unembedOut := sanitizeMLAWeights(tensors, prefix, cfg)
		attn.EmbedQ = nn.NewMultiLinear(embedQ)
		attn.UnembedOut = nn.NewMultiLinear(unembedOut)

		inputLN := tensors[prefix+".input_layernorm.weight"]
		postAttnLN := tensors[prefix+".post_attention_layernorm.weight"]

		if i < cfg.FirstKDenseReplace {
			// 前 k 层使用稠密 MLP。
			// Dense block
			block := &DenseBlock{Attention: attn}
			if inputLN != nil {
				block.InputLayerNorm = nn.NewRMSNorm(inputLN, cfg.RMSNormEps)
			}
			if postAttnLN != nil {
				block.PostAttentionLayerNorm = nn.NewRMSNorm(postAttnLN, cfg.RMSNormEps)
			}

			block.MLP = &DenseMLP{
				GateProj: linears.Make(prefix + ".mlp.gate_proj"),
				UpProj:   linears.Make(prefix + ".mlp.up_proj"),
				DownProj: linears.Make(prefix + ".mlp.down_proj"),
			}

			m.Layers[i] = block
		} else {
			// 其余层使用 MoE。
			// MoE block
			block := &MoEBlock{Attention: attn}
			if inputLN != nil {
				block.InputLayerNorm = nn.NewRMSNorm(inputLN, cfg.RMSNormEps)
			}
			if postAttnLN != nil {
				block.PostAttentionLayerNorm = nn.NewRMSNorm(postAttnLN, cfg.RMSNormEps)
			}

			// 堆叠或加载专家 gate/up/down。
			// Stack expert weights
			gate, up, down := sanitizeExpertWeights(tensors, prefix, cfg.NRoutedExperts, useQuantized, cfg)

			switchMLP := &SwitchMLP{UseQuantized: useQuantized}
			if useQuantized {
				switchMLP.GateWeightQ = gate.Weight
				switchMLP.GateScales = gate.Scales
				switchMLP.GateBiases = gate.Biases
				switchMLP.GateBits = gate.Bits
				switchMLP.GateGroupSize = gate.GroupSize
				switchMLP.UpWeightQ = up.Weight
				switchMLP.UpScales = up.Scales
				switchMLP.UpBiases = up.Biases
				switchMLP.UpBits = up.Bits
				switchMLP.UpGroupSize = up.GroupSize
				switchMLP.DownWeightQ = down.Weight
				switchMLP.DownScales = down.Scales
				switchMLP.DownBiases = down.Biases
				switchMLP.DownBits = down.Bits
				switchMLP.DownGroupSize = down.GroupSize
			} else {
				switchMLP.GateWeight = gate.Weight
				switchMLP.UpWeight = up.Weight
				switchMLP.DownWeight = down.Weight
			}

			moeGate := &MoEGate{}
			moeGate.Gate = linears.Make(prefix + ".mlp.gate")
			if bias := tensors[prefix+".mlp.gate.e_score_correction_bias"]; bias != nil {
				moeGate.EScoreCorrectionBias = bias
			}

			block.MoE = &MoE{
				Gate:      moeGate,
				SwitchMLP: switchMLP,
			}

			// 若配置有共享专家则加载。
			// Load shared experts if present
			if cfg.NSharedExperts > 0 {
				block.MoE.SharedExperts = &SharedExperts{
					GateProj: linears.Make(prefix + ".mlp.shared_experts.gate_proj"),
					UpProj:   linears.Make(prefix + ".mlp.shared_experts.up_proj"),
					DownProj: linears.Make(prefix + ".mlp.shared_experts.down_proj"),
				}
			}

			m.Layers[i] = block
		}
	}

	return nil
}

// Forward 执行嵌入、逐层块与最终归一化。
// Forward computes the forward pass of the model
func (m *Model) Forward(b *batch.Batch, caches []cache.Cache) (hidden, auxHidden *mlx.Array) {
	dims := b.InputIDs.Dims()
	B, L := int32(dims[0]), int32(dims[1])
	positions := mlx.FromValues(b.SeqOffsets, len(b.SeqOffsets))

	h := m.EmbedTokens.Forward(b.InputIDs)

	for i, layer := range m.Layers {
		var c cache.Cache
		if caches != nil {
			c = caches[i]
		}
		h = layer.Forward(h, b, c, positions, B, L, m.Config)
	}

	h = m.Norm.Forward(h, m.RMSNormEps)
	return h, h
}

// Unembed 经 LM head 得到 logits。
// Unembed applies the LM head to get logits.
func (m *Model) Unembed(x *mlx.Array) *mlx.Array {
	return m.LMHead.Forward(x)
}

// NewCaches 为每层创建 KV 缓存。
// NewCaches builds a KV cache per layer.
func (m *Model) NewCaches() []cache.Cache {
	caches := make([]cache.Cache, len(m.Layers))
	for i := range caches {
		caches[i] = cache.NewKVCache()
	}
	return caches
}

// MaxContextLength 返回最大上下文长度。
// MaxContextLength returns the maximum context length
func (m *Model) MaxContextLength() int { return int(m.MaxPositionEmbeddings) }

// VocabSize 返回词表大小。
// VocabSize returns the vocabulary size
func (m *Model) VocabSize() int32 { return m.Config.VocabSize }

// Tokenizer 返回模型分词器。
// Tokenizer returns the model's tokenizer
func (m *Model) Tokenizer() *tokenizer.Tokenizer { return m.tok }

// NewCache 创建 KV 缓存切片（兼容旧接口）。
// NewCache creates a new KV cache for the model
func (m *Model) NewCache(maxSeqLen int32) []cache.Cache {
	caches := make([]cache.Cache, len(m.Layers))
	for i := range caches {
		caches[i] = cache.NewKVCache()
	}
	return caches
}

// FormatPrompt 应用 GLM-4 聊天模板（默认开启思考）。
// FormatPrompt applies the GLM-4 chat template with thinking enabled by default.
func (m *Model) FormatPrompt(prompt string) string {
	return "[gMASK]<sop><|user|>" + prompt + "<|assistant|><think>"
}

// FormatPromptWithThinking 显式控制是否输出思考块。
// FormatPromptWithThinking applies the GLM-4 chat template with explicit thinking control.
func (m *Model) FormatPromptWithThinking(prompt string, think bool) string {
	if think {
		return "[gMASK]<sop><|user|>" + prompt + "<|assistant|><think>"
	}
	return "[gMASK]<sop><|user|>" + prompt + "<|assistant|></think>"
}

// NewRenderer 返回多轮对话渲染器。
// NewRenderer returns a new Renderer for formatting multi-turn conversations.
func (m *Model) NewRenderer() *Renderer {
	return &Renderer{}
}

// NewParser 返回解析思考与工具调用的 Parser。
// NewParser returns a new Parser for extracting thinking and tool calls from output.
func (m *Model) NewParser() *Parser {
	return &Parser{}
}
