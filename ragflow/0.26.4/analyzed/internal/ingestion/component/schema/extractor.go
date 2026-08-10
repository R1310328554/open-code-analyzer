//
//  Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//

package schema

// ExtractorFromUpstream 是 Extractor 组件消费的上游载荷。
//
// Python Extractor（rag/flow/extractor/extractor.py）不校验 Pydantic FromUpstream，
// 而是从 canvas 的 input-elements 映射拉取输入：
//
//	inputs = self.get_input_elements()
//	for k, v in inputs.items():
//	    args[k] = v["value"]
//	    if isinstance(args[k], list):
//	        chunks = deepcopy(args[k])
//	        chunks_key = k
//
// Go 版 FromUpstream 镜像该形状：自由命名的 inputs 映射 + 可选显式 chunks 列表。
type ExtractorFromUpstream struct {
	// CreatedTime / ElapsedTime follow the package-wide convention
	// from upstream components.
	CreatedTime *float64 `json:"_created_time,omitempty"`
	ElapsedTime *float64 `json:"_elapsed_time,omitempty"`

	// Inputs mirrors `get_input_elements()` output. Each entry holds a
	// free-form value (string for the LLM template, list of chunks
	// for the chunk-list binding). Keys are the input names; the
	// component selects the first list-typed value as the chunk
	// stream and passes the rest as scalar args.
	Inputs map[string]any `json:"inputs,omitempty"`

	// Chunks is the explicit chunk list when wired in a linear
	// pipeline. Optional — when Inputs contains a list-typed entry,
	// the component uses that instead.
	Chunks []map[string]any `json:"chunks,omitempty"`
}

// Validate 当前无必填字段；Python 组件可在空输入下运行（LLM 调用产出单条 chunk）。
func (ExtractorFromUpstream) Validate() error { return nil }

// ExtractorParam 是 Extractor 组件的静态配置，镜像 Python ExtractorParam。
// LLM 相关字段在 agent 侧 LLMParam；Go 版仅保留 Extractor 专有字段与 LLM 配置指针。
type ExtractorParam struct {
	// FieldName is the chunk key the LLM extraction result is written
	// to (Python: `self._param.field_name`). Required — `check()`
	// raises when empty. Mapped to "Result Destination" in the
	// frontend.
	FieldName string `json:"field_name"`

	// LLMID identifies the LLM model used for extraction. This is the
	// agent-side LLMParam.llm_id; on the ingestion side it is
	// resolved against the tenant's LLM provider registry.
	LLMID string `json:"llm_id,omitempty"`

	// SystemPrompt 可选系统提示词覆盖。
	SystemPrompt string `json:"system_prompt,omitempty"`

	// Prompt 传给 LLM 的用户侧模板。
	Prompt string `json:"prompt,omitempty"`
}

// Defaults 返回 Python 默认 ExtractorParam（FieldName 运行时填入）。
func (ExtractorParam) Defaults() ExtractorParam {
	return ExtractorParam{
		FieldName:    "",
		LLMID:        "",
		SystemPrompt: "",
		Prompt:       "",
	}
}

// Validate 强制 Python check() 不变量：FieldName 非空。
func (p *ExtractorParam) Validate() error {
	if p.FieldName == "" {
		return errRequiredField{Field: "field_name"}
	}
	return nil
}

// ExtractorOutputs 是 Extractor 组件调用结果，镜像 Python set_output 契约。
type ExtractorOutputs struct {
	// OutputFormat 恒为 "chunks"。
	OutputFormat string `json:"output_format,omitempty"`

	// Chunks is the enriched chunk list. When the Extractor ran over
	// a non-empty input list, each chunk gains a new key named after
	// FieldName (e.g., field_name="summary" -> chunk["summary"]). When
	// the Extractor ran over an empty input, Chunks contains a single
	// entry with one key (FieldName) holding the LLM result.
	Chunks []map[string]any `json:"chunks,omitempty"`

	// Error is set when the component short-circuits with an error
	// message (Python: set_output("_ERROR", ...)).
	Error string `json:"_ERROR,omitempty"`
}
// schema/extractor.go — Extractor 组件上下游 wire 类型。
