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

// mindmap.go — 检索增强思维导图生成：从 search_config 检索片段，调用 LLM 输出 Markdown 并解析为树形节点。

//

package handler

import (
	"encoding/json"
	"fmt"
	"regexp"
	"strings"

	"ragflow/internal/common"
	"ragflow/internal/entity"
	modelModule "ragflow/internal/entity/models"
	"ragflow/internal/service"
)

// mindMapRunConfig 思维导图流水线所需依赖与检索参数
type mindMapRunConfig struct {
	Question      string
	KbIDs         common.StringSlice
	SearchID      string
	SearchConfig  map[string]interface{}
	AuthUserID    string
	ModelTenantID string
	ChunkSvc      service.Retriever
	LLM           *service.ModelProviderService
	TenantSvc     *service.TenantService
}

// runMindMap 执行检索→LLM 生成→Markdown 解析的完整流程
func runMindMap(config mindMapRunConfig) (mindMapNode, error) {
	if config.ChunkSvc == nil {
		return mindMapNode{}, fmt.Errorf("chunk service not configured")
	}
	if config.LLM == nil {
		return mindMapNode{}, fmt.Errorf("LLM not configured")
	}
	modelTenantID := config.ModelTenantID
	if modelTenantID == "" {
		modelTenantID = config.AuthUserID
	}
	retrievalReq := mindMapRetrievalRequest(config.Question, config.KbIDs, config.SearchID, config.SearchConfig)
	ranks, err := config.ChunkSvc.RetrievalTest(retrievalReq, config.AuthUserID)
	if err != nil {
		return mindMapNode{}, err
	}
	sections := mindMapSections(ranks)
	if len(sections) == 0 {
		return mindMapNode{ID: "root", Children: []mindMapNode{}}, nil
	}
	modelID, _ := config.SearchConfig["chat_id"].(string)
	if modelID == "" && config.TenantSvc != nil {
		defaultModel, err := config.TenantSvc.GetDefaultModelName(modelTenantID, entity.ModelTypeChat)
		if err == nil {
			modelID = defaultModel
		}
	}
	response, err := config.LLM.Chat(modelTenantID, modelID, []modelModule.Message{{Role: "user", Content: mindMapPrompt(strings.Join(sections, "\n"))}, {Role: "user", Content: "Output:"}}, &modelModule.ChatConfig{})
	if err != nil {
		return mindMapNode{}, err
	}
	if response == nil || response.Answer == nil {
		return mindMapNode{ID: "root", Children: []mindMapNode{}}, nil
	}
	return parseMindMapMarkdown(*response.Answer), nil
}

// searchConfigFromDetail 从 search 详情中提取 search_config 映射
func searchConfigFromDetail(detail map[string]interface{}) map[string]interface{} {
	if sc, ok := detail["search_config"].(map[string]interface{}); ok && sc != nil {
		return sc
	}
	if sc, ok := detail["search_config"].(entity.JSONMap); ok && sc != nil {
		return map[string]interface{}(sc)
	}
	return map[string]interface{}{}
}

// mindMapRetrievalRequest 构造 RetrievalTest 请求（含 top_k、阈值等默认值）
func mindMapRetrievalRequest(question string, kbIDs common.StringSlice, searchID string, searchConfig map[string]interface{}) *service.RetrievalTestRequest {
	page := 1
	size := 12
	topK := intFromConfig(searchConfig, "top_k", 1024)
	similarityThreshold := floatFromConfig(searchConfig, "similarity_threshold", 0.2)
	vectorSimilarityWeight := floatFromConfig(searchConfig, "vector_similarity_weight", 0.3)
	req := &service.RetrievalTestRequest{
		Datasets:               kbIDs,
		Question:               question,
		Page:                   &page,
		Size:                   &size,
		TopK:                   &topK,
		SimilarityThreshold:    &similarityThreshold,
		VectorSimilarityWeight: &vectorSimilarityWeight,
		DocIDs:                 stringSliceFromConfig(searchConfig, "doc_ids"),
		Filter:                 mapFromConfig(searchConfig, "meta_data_filter"),
	}
	if searchID != "" {
		req.SearchID = &searchID
	}
	if rerankID, _ := searchConfig["rerank_id"].(string); rerankID != "" {
		req.RerankID = &rerankID
	}
	return req
}

// mindMapSections 从检索结果抽取 content_with_weight 文本片段
func mindMapSections(ranks *service.RetrievalTestResponse) []string {
	if ranks == nil {
		return nil
	}
	sections := make([]string, 0, len(ranks.Chunks))
	for _, chunk := range ranks.Chunks {
		if content, ok := chunk["content_with_weight"].(string); ok && strings.TrimSpace(content) != "" {
			sections = append(sections, content)
		}
	}
	return sections
}

// mergeMindMapKbIDs 合并已保存与请求中的知识库 ID 并去重
func mergeMindMapKbIDs(saved []string, requested common.StringSlice) common.StringSlice {
	seen := map[string]bool{}
	merged := make(common.StringSlice, 0, len(saved)+len(requested))
	for _, id := range saved {
		id = strings.TrimSpace(id)
		if id != "" && !seen[id] {
			seen[id] = true
			merged = append(merged, id)
		}
	}
	for _, id := range requested {
		id = strings.TrimSpace(id)
		if id != "" && !seen[id] {
			seen[id] = true
			merged = append(merged, id)
		}
	}
	return merged
}

// intFromConfig 从 search_config 读取整型，支持 json.Number
func intFromConfig(config map[string]interface{}, key string, fallback int) int {
	switch v := config[key].(type) {
	case int:
		return v
	case int64:
		return int(v)
	case float64:
		return int(v)
	case json.Number:
		if n, err := v.Int64(); err == nil {
			return int(n)
		}
	}
	return fallback
}

// floatFromConfig 从 search_config 读取浮点型
func floatFromConfig(config map[string]interface{}, key string, fallback float64) float64 {
	switch v := config[key].(type) {
	case float64:
		return v
	case float32:
		return float64(v)
	case int:
		return float64(v)
	case int64:
		return float64(v)
	case json.Number:
		if n, err := v.Float64(); err == nil {
			return n
		}
	}
	return fallback
}

// stringSliceFromConfig 从 search_config 读取字符串切片
func stringSliceFromConfig(config map[string]interface{}, key string) []string {
	switch v := config[key].(type) {
	case []string:
		return v
	case []interface{}:
		out := make([]string, 0, len(v))
		for _, item := range v {
			if s, ok := item.(string); ok && s != "" {
				out = append(out, s)
			}
		}
		return out
	}
	return nil
}

// mapFromConfig 从 search_config 读取 map/JSONMap
func mapFromConfig(config map[string]interface{}, key string) map[string]interface{} {
	if m, ok := config[key].(map[string]interface{}); ok {
		return m
	}
	if m, ok := config[key].(entity.JSONMap); ok {
		return map[string]interface{}(m)
	}
	return nil
}

// mindMapPrompt 构造要求 LLM 输出多级 Markdown 思维导图的提示词
func mindMapPrompt(inputText string) string {
	return `- Role: You're a talent text processor to summarize a piece of text into a mind map.

- Step of task:
  1. Generate a title for user's 'TEXT'.
  2. Classify the 'TEXT' into sections of a mind map.
  3. If the subject matter is really complex, split them into sub-sections and sub-subsections.
  4. Add a shot content summary of the bottom level section.

- Output requirement:
  - Generate at least 4 levels.
  - Always try to maximize the number of sub-sections.
  - In language of 'Text'
  - MUST IN FORMAT OF MARKDOWN

-TEXT-
` + inputText + "\n"
}

// mindMapNode 思维导图 JSON 树节点（id + children）
type mindMapNode struct {
	ID       string        `json:"id"`
	Children []mindMapNode `json:"children"`
}

// mindMapHeadingRe 匹配 Markdown 标题行
var mindMapHeadingRe = regexp.MustCompile(`^(#{1,6})\s+(.+)$`)
// mindMapListRe 匹配 Markdown 列表项并推断层级
var mindMapListRe = regexp.MustCompile(`^(\s*)(?:[-*+]|\d+\.)\s+(.+)$`)

// parseMindMapMarkdown 将 LLM 输出的 Markdown 解析为 mindMapNode 树
func parseMindMapMarkdown(text string) mindMapNode {
	lines := strings.Split(strings.ReplaceAll(text, "\r\n", "\n"), "\n")
	root := mindMapNode{ID: "root", Children: []mindMapNode{}}
	stack := []*mindMapNode{&root}
	inFence := false
	listBaseLevel := 1
	lastWasList := false
	for _, line := range lines {
		trimmed := strings.TrimSpace(line)
		if strings.HasPrefix(trimmed, "```") {
			inFence = !inFence
			lastWasList = false
			continue
		}
		if inFence || trimmed == "" {
			lastWasList = false
			continue
		}
		level := 0
		title := ""
		if m := mindMapHeadingRe.FindStringSubmatch(trimmed); len(m) == 3 {
			level = len(m[1])
			title = cleanMindMapText(m[2])
			lastWasList = false
		} else if m := mindMapListRe.FindStringSubmatch(line); len(m) == 3 {
			rawLevel := len(m[1])/2 + 1
			if !lastWasList {
				listBaseLevel = len(stack)
			}
			level = listBaseLevel + rawLevel - 1
			title = cleanMindMapText(m[2])
			lastWasList = true
		}
		if title == "" {
			lastWasList = false
			continue
		}
		for len(stack) > level {
			stack = stack[:len(stack)-1]
		}
		parent := stack[len(stack)-1]
		parent.Children = append(parent.Children, mindMapNode{ID: title, Children: []mindMapNode{}})
		stack = append(stack, &parent.Children[len(parent.Children)-1])
	}
	if len(root.Children) == 1 {
		return root.Children[0]
	}
	return root
}

// cleanMindMapText 清理节点标题中的 Markdown 装饰符
func cleanMindMapText(text string) string {
	text = strings.TrimSpace(text)
	text = strings.Trim(text, "`")
	text = strings.Trim(text, "*_ ")
	return strings.TrimSpace(text)
}

// 无检索片段时返回空 root；单根节点时提升为唯一子节点以简化前端渲染。
