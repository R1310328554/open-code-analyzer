// Draft 模型导入：推测解码/MTP 助手 tensor 前缀命名空间，合并进目标 manifest。
package create

import (
	"fmt"
	"strings"
)

// CreateDraftLayers 导入 draft safetensors：tensor 加前缀、config 加目录前缀，不写独立 manifest。
// CreateDraftLayers imports a draft (speculative-decoding / MTP assistant)
// safetensors model into prefixed tensor and config blobs and returns the
// layers WITHOUT writing a manifest — the caller folds them into the target
// model's manifest. A draft never stands alone; it always accompanies a target
// model named on the Modelfile's FROM line.
//
// It runs the same read → classify → plan → write pipeline as Create. Output
// tensor names keep their source form, namespaced by tensorPrefix (e.g.
// "draft.") so they cannot collide with the target's tensors; config blobs are
// named under configPrefix (e.g. "draft/").
// CreateDraftLayers 执行读→分类→规划→写流水线，输出层供目标模型 manifest 合并。
func CreateDraftLayers(modelDir, tensorPrefix, configPrefix, quantize string, store BlobStore, fn func(status string)) ([]LayerInfo, error) {
	if tensorPrefix == "" {
		return nil, fmt.Errorf("draft tensor prefix must not be empty")
	}
	if configPrefix == "" {
		return nil, fmt.Errorf("draft config prefix must not be empty")
	}
	defer sweepMLX()

	inv, err := ReadInventory(modelDir)
	if err != nil {
		return nil, fmt.Errorf("read draft model: %w", err)
	}
	class, err := Classify(inv, quantize)
	if err != nil {
		return nil, err
	}
	policy, err := newTensorImportTransform(inv)
	if err != nil {
		return nil, fmt.Errorf("build draft quantization policy for %q: %w", inv.Config.Architecture(), err)
	}
	specs, err := Plan(inv, class, draftPolicy{policy})
	if err != nil {
		return nil, fmt.Errorf("plan draft model: %w", err)
	}
	specs = prefixSpecs(specs, tensorPrefix)

	fn(fmt.Sprintf("importing draft (%d tensors%s)", len(inv.Tensors), quantizeStatus(class)))
	layers, err := WriteBlobs(specs, modelDir, store)
	if err != nil {
		return nil, err
	}

	configLayers, _, err := importConfigBlobs(modelDir, configPrefix, store, fn)
	if err != nil {
		return nil, err
	}
	return append(layers, configLayers...), nil
}

// prefixSpecs 为每个 BlobSpec/TensorSpec 输出名加前缀，源引用不变。
// prefixSpecs returns specs with prefix prepended to every output blob name and
// output tensor name, leaving the source references (which point at the source
// files) untouched. Scale/bias keys derive from the tensor name, so they inherit
// the prefix automatically.
func prefixSpecs(specs []BlobSpec, prefix string) []BlobSpec {
	out := make([]BlobSpec, len(specs))
	for i, spec := range specs {
		tensors := make([]TensorSpec, len(spec.Tensors))
		for j, ts := range spec.Tensors {
			ts.Name = prefix + ts.Name
			tensors[j] = ts
		}
		out[i] = BlobSpec{Name: prefix + spec.Name, Tensors: tensors, Metadata: spec.Metadata}
	}
	return out
}

// draftPolicy 包装架构策略：draft 的 embed/lm_head 直接使用请求量化类型（无 8-bit 提升）。
// draftPolicy wraps an architecture policy to give a draft model's output head
// (tied token embedding or separate lm_head) the requested type directly: draft
// quality only affects acceptance, so the target head's 8-bit promotion buys
// nothing. It is given unprefixed source names; planning runs before prefixSpecs.
// draftPolicy 在规划阶段作用于未加前缀的源 tensor 名。
type draftPolicy struct{ inner quantizePolicy }

// quantizationType 对 embed_tokens/lm_head 直接 normalize 请求类型，其余走 inner。
func (p draftPolicy) quantizationType(name string, shape []int32, requested string) string {
	if isEmbedTokensWeight(name) || strings.HasSuffix(name, "lm_head.weight") {
		if q := normalizeQuantType(requested); isAligned(shape, q) {
			return q
		}
		return ""
	}
	return p.inner.quantizationType(name, shape, requested)
}
