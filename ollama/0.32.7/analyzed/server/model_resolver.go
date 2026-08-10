// 模型引用解析：:cloud 来源后缀、pull 规范化与 model.Name 校验。
package server

import (
	"github.com/ollama/ollama/internal/modelref"
	"github.com/ollama/ollama/types/model"
)

// modelSource 为 modelref 来源枚举别名。
type modelSource = modelref.ModelSource

const (
	modelSourceUnspecified modelSource = modelref.ModelSourceUnspecified
	modelSourceLocal       modelSource = modelref.ModelSourceLocal
	modelSourceCloud       modelSource = modelref.ModelSourceCloud
)

var (
	errConflictingModelSource = modelref.ErrConflictingSourceSuffix
	errModelRequired          = modelref.ErrModelRequired
)

// parsedModelRef 保存原始输入、规范化基名、解析后 Name 与来源意图。
type parsedModelRef struct {
	// Original 为解析来源后缀前的调用方原始模型字符串。
	// Example: "gpt-oss:20b:cloud".
	Original string
	// Base 为去掉 :cloud 等来源后缀后的模型字符串。
	// Example: "gpt-oss:20b:cloud" -> "gpt-oss:20b".
	Base string
	// Name 为应用默认 registry 后的完全限定 model.Name。
	// Example: "registry.ollama.ai/library/gpt-oss:20b".
	Name model.Name
	// Source 记录原始输入中的显式 local/cloud 来源意图。
	// Example: "gpt-oss:20b:cloud" -> modelSourceCloud.
	Source modelSource
}

// parseAndValidateModelRef 解析并校验推理/展示类 API 的模型引用。
func parseAndValidateModelRef(raw string) (parsedModelRef, error) {
	var zero parsedModelRef

	parsed, err := modelref.ParseRef(raw)
	if err != nil {
		return zero, err
	}

	name := model.ParseName(parsed.Base)
	if !name.IsValid() {
		return zero, model.Unqualified(name)
	}

	return parsedModelRef{
		Original: parsed.Original,
		Base:     parsed.Base,
		Name:     name,
		Source:   parsed.Source,
	}, nil
}

// parseNormalizePullModelRef 为 pull 路径应用 NormalizePullName 规则。
func parseNormalizePullModelRef(raw string) (parsedModelRef, error) {
	var zero parsedModelRef

	parsedRef, err := modelref.ParseRef(raw)
	if err != nil {
		return zero, err
	}

	normalizedName, _, err := modelref.NormalizePullName(raw)
	if err != nil {
		return zero, err
	}

	name := model.ParseName(normalizedName)
	if !name.IsValid() {
		return zero, model.Unqualified(name)
	}

	return parsedModelRef{
		Original: parsedRef.Original,
		Base:     normalizedName,
		Name:     name,
		Source:   parsedRef.Source,
	}, nil
}
