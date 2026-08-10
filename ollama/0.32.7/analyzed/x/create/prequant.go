// 预量化源规划：按 producer 模式融合 weight/scale/bias 为 loader 可读的单 blob。
package create

import "strings"

// prequantPattern 描述某一 producer 如何打包已量化权重及 companion，并如何融合为单 blob。
// prequantPattern describes how one producer packs an already-quantized weight
// and its scale companions into safetensors files, and how to fuse them into
// the single blob our loader reads. Producers differ only in tensor names and a
// few per-field transforms; expressing them as table rows keeps those
// differences visible and prevents the per-producer drift the old separate code
// paths suffered (for example the global scale being stored as-is by one
// producer and inverted by another).
//
// All suffixes are relative to the base — the source weight name minus its
// weight suffix. The fused blob is always named "<base>.weight", with
// companions "<base>.weight.scale", ".bias", and ".global_scale".
// prequantPattern 以表驱动方式表达各 producer 后缀与变换差异。
type prequantPattern struct {
	name string

	weightSuffix string // 识别权重的源后缀（".weight" 或 ".weight_packed"）
	repackWeight bool   // 是否将 U8 fp4 权重重打包为 U32

	scaleSuffix    string // 必需的 per-block/affine scale companion 后缀
	scaleRelabelU8 bool   // 是否将 F8_E4M3 scale 重标为 U8

	biasSuffix string // 可选 bias/零点 companion 后缀

	globalSuffix     string // 可选全局 scale companion 后缀
	globalReciprocal bool   // 是否存储全局 scale 的倒数

	ignoreSuffixes []string // 消费但不写入的 companion（如 activation scale）

	forceQuantType   string // 覆盖 blob quant_type 元数据
	defaultGroupSize string // config 未提供时默认 group_size
}

// prequantPatterns 按序匹配；首个 weight 后缀与必需 scale 均命中者生效。
// prequantPatterns is consulted in order; the first whose weight suffix matches
// and whose required scale companion is present wins. MLX and ModelOpt both use
// a ".weight" weight, but their scale companions (".scales" vs ".weight_scale")
// are mutually exclusive, so the order between them does not matter.
// prequantPatterns 注册 MLX、compressed-tensors-nvfp4、modelopt-nvfp4 等模式。
var prequantPatterns = []prequantPattern{
	{
		name:         "mlx",
		weightSuffix: ".weight",
		scaleSuffix:  ".scales",
		biasSuffix:   ".biases",
	},
	{
		name:             "compressed-tensors-nvfp4",
		weightSuffix:     ".weight_packed",
		repackWeight:     true,
		scaleSuffix:      ".weight_scale",
		scaleRelabelU8:   true,
		globalSuffix:     ".weight_global_scale",
		globalReciprocal: true,
		ignoreSuffixes:   []string{".input_scale", ".input_global_scale"},
		forceQuantType:   "nvfp4",
		defaultGroupSize: "16",
	},
	{
		name:           "modelopt-nvfp4",
		weightSuffix:   ".weight",
		repackWeight:   true,
		scaleSuffix:    ".weight_scale",
		scaleRelabelU8: true,
		globalSuffix:   ".weight_scale_2",
		ignoreSuffixes: []string{".input_scale", ".input_global_scale"},
		forceQuantType: "nvfp4",
	},
}

// planPrequantized 规划预量化源：融合 weight 与 scale companion，norm/嵌入等直通。
// planPrequantized plans an already-quantized source: each weight is fused with
// its scale companions into one blob, companions are not emitted on their own,
// and any remaining tensors (norms, embeddings) pass through at source
// precision.
// planPrequantized 遍历 tensor 名，匹配预量化模式或原样输出。
func planPrequantized(inv Inventory) ([]BlobSpec, error) {
	fused := make(map[string]BlobSpec)
	consumed := make(map[string]bool)
	for _, name := range sortedTensorNames(inv) {
		spec, sources, ok := matchPrequant(name, inv)
		if !ok {
			continue
		}
		fused[name] = spec
		for _, s := range sources {
			consumed[s] = true
		}
	}

	specs := make([]BlobSpec, 0, len(inv.Tensors))
	for _, name := range sortedTensorNames(inv) {
		if spec, ok := fused[name]; ok {
			specs = append(specs, spec)
			continue
		}
		if consumed[name] {
			continue
		}
		t := inv.Tensors[name]
		specs = append(specs, BlobSpec{Name: name, Tensors: []TensorSpec{{Name: name, Sources: []SourceTensor{t}}}})
	}
	return specs, nil
}

// matchPrequant 若 name 匹配某预量化 producer 则返回融合 BlobSpec 与消费的源名。
// matchPrequant returns the fused blob for a weight tensor if it matches a
// prequantized producer, along with the source names it consumes. It returns
// ok=false when name is not a prequantized weight (a companion or a plain
// tensor).
// matchPrequant 构建含 weight/scale/bias/global_scale 的 TensorSpec 列表。
func matchPrequant(name string, inv Inventory) (BlobSpec, []string, bool) {
	for _, p := range prequantPatterns {
		base, ok := strings.CutSuffix(name, p.weightSuffix)
		if !ok {
			continue
		}
		scaleSrc := base + p.scaleSuffix
		if !inv.Has(scaleSrc) {
			continue
		}

		outWeight := base + ".weight"
		weight := inv.Tensors[name]
		var tensors []TensorSpec
		var consumed []string

		weightTensor := TensorSpec{Name: outWeight, Sources: []SourceTensor{weight}}
		if p.repackWeight && strings.EqualFold(weight.Dtype, "U8") && len(weight.Shape) == 2 {
			weightTensor.Transform = TransformRepackFP4
			weightTensor.OutDtype = "U32"
			weightTensor.OutShape = []int32{weight.Shape[0], weight.Shape[1] / 4}
		}
		tensors = append(tensors, weightTensor)

		scale := inv.Tensors[scaleSrc]
		scaleTensor := TensorSpec{Name: outWeight + ".scale", Sources: []SourceTensor{scale}}
		if p.scaleRelabelU8 && isE4M3Dtype(scale.Dtype) {
			scaleTensor.Transform = TransformRelabelU8
			scaleTensor.OutDtype = "U8"
		}
		tensors = append(tensors, scaleTensor)
		consumed = append(consumed, scaleSrc)

		if p.biasSuffix != "" {
			if biasSrc := base + p.biasSuffix; inv.Has(biasSrc) {
				tensors = append(tensors, TensorSpec{Name: outWeight + ".bias", Sources: []SourceTensor{inv.Tensors[biasSrc]}})
				consumed = append(consumed, biasSrc)
			}
		}

		if p.globalSuffix != "" {
			if gSrc := base + p.globalSuffix; inv.Has(gSrc) {
				global := TensorSpec{Name: outWeight + ".global_scale", Sources: []SourceTensor{inv.Tensors[gSrc]}, Transform: TransformScalarF32}
				if p.globalReciprocal {
					global.Transform = TransformReciprocalF32
				}
				tensors = append(tensors, global)
				consumed = append(consumed, gSrc)
			}
		}

		for _, suf := range p.ignoreSuffixes {
			if s := base + suf; inv.Has(s) {
				consumed = append(consumed, s)
			}
		}

		return BlobSpec{Name: outWeight, Tensors: tensors, Metadata: prequantMetadata(inv, p)}, consumed, true
	}
	return BlobSpec{}, nil, false
}

// prequantMetadata 合并源 config 量化元数据与 pattern 的 type/group_size 覆盖。
// prequantMetadata builds the fused blob's metadata: the source config's quant
// metadata, with the pattern's quant_type override and group_size default
// applied. Returns nil when there is nothing to record.
// prequantMetadata 无内容可记录时返回 nil。
func prequantMetadata(inv Inventory, p prequantPattern) map[string]string {
	md := make(map[string]string)
	for k, v := range inv.Config.QuantMetadata() {
		md[k] = v
	}
	if p.forceQuantType != "" {
		md["quant_type"] = p.forceQuantType
	}
	if p.defaultGroupSize != "" {
		if _, ok := md["group_size"]; !ok {
			md["group_size"] = p.defaultGroupSize
		}
	}
	if len(md) == 0 {
		return nil
	}
	return md
}
