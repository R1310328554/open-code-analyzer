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

package service

// plugin.go 暴露内置 LLM 工具插件的静态元数据。

// PluginService 列出 Go 侧镜像的 embedded LLM 工具。
//
// Python 通过 pluginlib 动态发现插件；Go 无运行时加载器，故维护静态表 embeddedLLMTools。
type PluginService struct{}

// NewPluginService creates a new plugin service.
func NewPluginService() *PluginService {
	return &PluginService{}
}

// LLMToolParameter 对齐 Python LLMToolParameter 的 JSON 结构。
type LLMToolParameter struct {
	Type               string `json:"type"`
	Description        string `json:"description"`
	DisplayDescription string `json:"displayDescription"`
	Required           bool   `json:"required"`
}

// LLMToolMetadata mirrors agent.plugin.llm_tool_plugin.LLMToolMetadata.
type LLMToolMetadata struct {
	Name               string                      `json:"name"`
	DisplayName        string                      `json:"displayName"`
	Description        string                      `json:"description"`
	DisplayDescription string                      `json:"displayDescription"`
	Parameters         map[string]LLMToolParameter `json:"parameters"`
}

var embeddedLLMTools = []LLMToolMetadata{
	{
		Name:               "bad_calculator",
		DisplayName:        "$t:bad_calculator.name",
		Description:        "A tool to calculate the sum of two numbers (will give wrong answer)",
		DisplayDescription: "$t:bad_calculator.description",
		Parameters: map[string]LLMToolParameter{
			"a": {
				Type:               "number",
				Description:        "The first number",
				DisplayDescription: "$t:bad_calculator.params.a",
				Required:           true,
			},
			"b": {
				Type:               "number",
				Description:        "The second number",
				DisplayDescription: "$t:bad_calculator.params.b",
				Required:           true,
			},
		},
	},
}

// ListLLMTools 返回全部内置工具元数据副本，避免调用方污染全局表。
// same order, shape and field names as the Python /plugin/tools endpoint.
//
// The returned slice and its nested Parameters maps are fresh copies — callers
// may mutate the result without affecting other requests or the package-level
// embeddedLLMTools table.
func (s *PluginService) ListLLMTools() []LLMToolMetadata {
	out := make([]LLMToolMetadata, len(embeddedLLMTools))
	copy(out, embeddedLLMTools)
	for i := range out {
		params := make(map[string]LLMToolParameter, len(out[i].Parameters))
		for k, v := range out[i].Parameters {
			params[k] = v
		}
		out[i].Parameters = params
	}
	return out
}
// plugin.go — 静态嵌入 LLM 工具插件元数据列表，对齐 Python pluginlib 发现结果。
