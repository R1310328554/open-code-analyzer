// 导入规划器：由 Inventory 与 Classification 生成 BlobSpec/TensorSpec，writer 无决策。
package create

import (
	"fmt"
	"slices"
	"sort"
	"strconv"
	"strings"
)

// Transform 描述如何将源 tensor 组合或转换为输出 tensor；零值为直通复制。
// Transform names how a tensor's source(s) are turned into the output tensor.
// The zero value, TransformNone, copies a single source through unchanged.
type Transform string

const (
	TransformNone Transform = ""

	// TransformRepackFP4 将 U8 fp4 打包权重重标为 U32（8 值/字），字节不变。
	// as U32 words (8 values/word): the bytes are unchanged, only the dtype
	// and last dimension are relabeled.
	TransformRepackFP4 Transform = "repack_fp4"

	// TransformRelabelU8 将 F8_E4M3 scale 重标为 U8 供加载器按原始字节读取。
	// its raw bytes; the bytes themselves are unchanged.
	TransformRelabelU8 Transform = "relabel_u8"

	// TransformScalarF32 校验标量 F32 并原样复制（全局 scale）。
	// it through (a global scale stored as-is).
	TransformScalarF32 Transform = "scalar_f32"

	// TransformReciprocalF32 校验标量 F32 并存储其倒数。
	// (a global scale the producer stored inverted).
	TransformReciprocalF32 Transform = "reciprocal_f32"

	// TransformStackExperts 按专家索引顺序堆叠 N 个 per-expert 源为 [experts,...]。
	// expert-index order) into one [experts, ...] tensor.
	TransformStackExperts Transform = "stack_experts"

	// TransformDecodeFP8 用块 scale 反量化 block-FP8 为 BF16，再由 Quantize 重量化。
	// Its two sources are the F8_E4M3 weight and its scale companion; the
	// result is a BF16 tensor, which Quantize (if set) then re-quantizes.
	TransformDecodeFP8 Transform = "decode_fp8"

	// TransformDecodeStackFP8 堆叠 N 组 expert FP8 权重与 scale 并反量化为 BF16。
	// block scales) into one [experts, out, in] tensor and dequantizes it. Its
	// sources are the N weights followed by the N scales, in expert-index order;
	// the result is a BF16 tensor, which Quantize (if set) then re-quantizes.
	TransformDecodeStackFP8 Transform = "decode_stack_fp8"
)

// TensorSpec 描述 blob 内一个输出 tensor 的源、变换、名称与可选量化。
// TensorSpec describes one output tensor within a blob: the source tensor(s)
// it is built from, the transform that combines or converts them, the name it
// takes in the blob, and an optional quantization to apply. When Quantize is
// set the writer runs MLX quantization, which generates the tensor's scale and
// bias sub-tensors; otherwise the (transformed) bytes are stored as-is.
type TensorSpec struct {
	Name      string
	Sources   []SourceTensor
	Transform Transform
	Quantize  string
	OutDtype  string  // 变换后 dtype；"" 表示与单一源相同
	OutShape  []int32 // 变换后 shape；nil 表示与单一源相同
}

// BlobSpec 描述一个输出 blob：层名、所含 tensor 与 safetensors 元数据。
// BlobSpec describes one output blob: its layer name, the tensors it contains,
// and its safetensors metadata. The planner builds these purely from the
// inventory and classification; the writer executes them and makes no
// decisions of its own.
type BlobSpec struct {
	Name     string
	Tensors  []TensorSpec
	Metadata map[string]string
}

// quantizePolicy 为每个 tensor 决定量化类型；"" 保持源精度，可返回高于请求的类型。
// quantizePolicy decides the quantization type for each tensor of a model,
// returning "" to keep it at source precision. A policy may return a higher-
// precision type than requested for sensitive tensors. The per-architecture
// import transforms implement it; defaultQuantPolicy provides the generic
// default (GetTensorQuantization).
type quantizePolicy interface {
	quantizationType(name string, shape []int32, requested string) string
}

// Plan 将 Inventory 与 Classification 转为待写 blob 列表；不读权重数据。
// Plan turns an inventory and its classification into the ordered list of
// blobs to write. It reads no weight data and makes every decision here, so
// the writer that follows has nothing left to decide. The policy decides which
// weights are quantized and to what; pass defaultQuantPolicy{} for the generic
// policy.
// Plan 按 SourceFloat/Prequantized/BlockFP8 分支规划并检测输出名冲突。
func Plan(inv Inventory, class Classification, policy quantizePolicy) ([]BlobSpec, error) {
	var (
		specs []BlobSpec
		err   error
	)
	switch class.Kind {
	case SourceFloat:
		specs, err = planFloat(inv, class.Quantize, policy)
	case SourcePrequantized:
		specs, err = planPrequantized(inv)
	case SourceBlockFP8:
		specs, err = planBlockFP8(inv, class.Quantize, policy)
	default:
		return nil, fmt.Errorf("plan: source kind %q is not yet supported", class.Kind)
	}
	if err != nil {
		return nil, err
	}
	if err := checkOutputCollisions(specs); err != nil {
		return nil, err
	}
	return specs, nil
}

// checkOutputCollisions 拒绝两个源 tensor 规范化后输出名冲突的计划。
// checkOutputCollisions rejects a plan in which two source tensors normalized
// to the same output name — for example a source shipping both foo.weight and
// foo.weight_packed, which would both fuse to foo.weight. Writing such a plan
// would produce blobs that silently shadow each other at load time.
func checkOutputCollisions(specs []BlobSpec) error {
	blobs := make(map[string]bool, len(specs))
	tensors := make(map[string]string)
	for _, spec := range specs {
		if blobs[spec.Name] {
			return fmt.Errorf("plan: two blobs named %s (source tensors normalize to a clashing name)", spec.Name)
		}
		blobs[spec.Name] = true
		for _, ts := range spec.Tensors {
			if prev, ok := tensors[ts.Name]; ok {
				return fmt.Errorf("plan: output tensor %s planned in both blob %s and blob %s (source tensors normalize to a clashing name)", ts.Name, prev, spec.Name)
			}
			tensors[ts.Name] = spec.Name
		}
	}
	return nil
}

// planFloat 规划浮点源：per-expert 堆叠为组 blob，其余各成独立 blob。
// planFloat plans a float model: per-expert tensors are packed into one blob
// per layer's expert group; every other tensor becomes its own blob, with the
// quantization policy deciding which weights are quantized and to what.
func planFloat(inv Inventory, quantize string, policy quantizePolicy) ([]BlobSpec, error) {
	groups := make(map[string][]SourceTensor)
	var plain []string
	for _, name := range sortedTensorNames(inv) {
		if gp, ok := perExpertGroup(name); ok {
			groups[gp] = append(groups[gp], inv.Tensors[name])
		} else {
			plain = append(plain, name)
		}
	}

	specs := make([]BlobSpec, 0, len(plain)+len(groups))
	for _, name := range plain {
		t := inv.Tensors[name]
		q := ""
		if quantize != "" {
			q = policy.quantizationType(name, t.Shape, quantize)
		}
		specs = append(specs, BlobSpec{
			Name:    name,
			Tensors: []TensorSpec{{Name: name, Sources: []SourceTensor{t}, Quantize: q}},
		})
	}

	for _, gp := range sortedKeys(groups) {
		groupSpecs, err := planExpertGroup(gp, groups[gp], quantize, policy)
		if err != nil {
			return nil, err
		}
		specs = append(specs, groupSpecs...)
	}
	return specs, nil
}

// planExpertGroup 将各投影的 per-expert 权重堆叠为 [experts,out,in]；混合精度时分 blob。
// planExpertGroup stacks each projection's per-expert weights into an
// [experts, out, in] tensor. Uniform projections share one blob; mixed
// precisions use one blob per projection so safetensors quantization metadata
// always describes the entire blob.
func planExpertGroup(groupPrefix string, tensors []SourceTensor, quantize string, policy quantizePolicy) ([]BlobSpec, error) {
	type expert struct {
		idx int
		t   SourceTensor
	}
	byProj := make(map[string][]expert)
	for _, t := range tensors {
		idx, proj, err := parseExpertTensor(groupPrefix, t.Name)
		if err != nil {
			return nil, err
		}
		byProj[proj] = append(byProj[proj], expert{idx: idx, t: t})
	}

	var tensorSpecs []TensorSpec
	for _, proj := range sortedKeys(byProj) {
		experts := byProj[proj]
		sort.Slice(experts, func(i, j int) bool { return experts[i].idx < experts[j].idx })

		base := experts[0].t
		sources := make([]SourceTensor, len(experts))
		for i, e := range experts {
			if e.t.Dtype != base.Dtype || !slices.Equal(e.t.Shape, base.Shape) {
				return nil, fmt.Errorf("expert group %s projection %s has mismatched expert layout (%s %v vs %s %v)",
					groupPrefix, proj, base.Dtype, base.Shape, e.t.Dtype, e.t.Shape)
			}
			sources[i] = e.t
		}

		stackedName := groupPrefix + "." + proj + ".weight"
		stackedShape := append([]int32{int32(len(experts))}, base.Shape...)
		q := ""
		if quantize != "" {
			q = policy.quantizationType(stackedName, stackedShape, quantize)
		}
		tensorSpecs = append(tensorSpecs, TensorSpec{
			Name:      stackedName,
			Sources:   sources,
			Transform: TransformStackExperts,
			Quantize:  q,
			OutDtype:  base.Dtype,
			OutShape:  stackedShape,
		})
	}
	return homogeneousExpertBlobs(groupPrefix, tensorSpecs), nil
}

// homogeneousExpertBlobs 量化类型一致时合并为单 blob，否则每 tensor 独立 blob。
func homogeneousExpertBlobs(groupPrefix string, tensors []TensorSpec) []BlobSpec {
	if len(tensors) == 0 {
		return nil
	}

	quantize := tensors[0].Quantize
	for _, tensor := range tensors[1:] {
		if tensor.Quantize != quantize {
			blobs := make([]BlobSpec, len(tensors))
			for i, tensor := range tensors {
				blobs[i] = BlobSpec{Name: tensor.Name, Tensors: []TensorSpec{tensor}}
			}
			return blobs
		}
	}

	return []BlobSpec{{Name: groupPrefix, Tensors: tensors}}
}

// parseExpertTensor 解析 <groupPrefix>.<index>.<projection>.weight 为索引与投影名。
// parseExpertTensor splits a per-expert weight name of the form
// "<groupPrefix>.<index>.<projection>.weight" into its expert index and
// projection name.
func parseExpertTensor(groupPrefix, name string) (idx int, proj string, err error) {
	rest, ok := strings.CutPrefix(name, groupPrefix+".")
	if !ok {
		return 0, "", fmt.Errorf("expert tensor %q is not under group %q", name, groupPrefix)
	}
	rest, ok = strings.CutSuffix(rest, ".weight")
	if !ok {
		return 0, "", fmt.Errorf("expert tensor %q does not end in .weight", name)
	}
	idxStr, proj, ok := strings.Cut(rest, ".")
	if !ok {
		return 0, "", fmt.Errorf("expert tensor %q is not <index>.<projection>.weight", name)
	}
	idx, err = strconv.Atoi(idxStr)
	if err != nil {
		return 0, "", fmt.Errorf("expert tensor %q has a non-numeric expert index %q", name, idxStr)
	}
	return idx, proj, nil
}

// perExpertGroup 判断是否为需堆叠的 per-expert 权重并返回组前缀。
// perExpertGroup reports whether name is a per-expert weight that must be
// stacked — e.g. "<layer>.mlp.experts.3.gate_proj.weight" — and returns its
// group prefix. An already-stacked expert tensor (one tensor covering all
// experts, e.g. "<layer>.mlp.experts.gate_up_proj.weight" as qwen3.5 and
// gemma4 ship it) is not per-expert: it is quantized as an ordinary 3D tensor
// and the runtime splits/uses it directly.
func perExpertGroup(name string) (string, bool) {
	gp := ExpertGroupPrefix(name)
	if gp == "" {
		return "", false
	}
	rest, ok := strings.CutPrefix(name, gp+".")
	if !ok {
		return "", false
	}
	idx, _, ok := strings.Cut(rest, ".")
	if !ok {
		return "", false
	}
	if _, err := strconv.Atoi(idx); err != nil {
		return "", false
	}
	return gp, true
}

// sortedTensorNames 返回 Inventory 中按名称排序的 tensor 名列表。
func sortedTensorNames(inv Inventory) []string {
	return sortedKeys(inv.Tensors)
}

// sortedKeys 返回 map 键的字典序切片。
func sortedKeys[V any](m map[string]V) []string {
	keys := make([]string, 0, len(m))
	for k := range m {
		keys = append(keys, k)
	}
	sort.Strings(keys)
	return keys
}
