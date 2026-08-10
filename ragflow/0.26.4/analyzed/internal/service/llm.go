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

// llm.go 提供租户 LLM 配置查询、列表与 API Key 写入能力。

import (
	"fmt"
	"ragflow/internal/entity"
	"strconv"
	"strings"

	"ragflow/internal/dao"
)

var DB = dao.DB

// LLMService 封装租户 LLM 与全局 LLM 目录的数据访问。
type LLMService struct {
	tenantLLMDAO *dao.TenantLLMDAO
	llmDAO       *dao.LLMDAO
}

// NewLLMService 构造 LLMService 实例。
func NewLLMService() *LLMService {
	return &LLMService{
		tenantLLMDAO: dao.NewTenantLLMDAO(),
		llmDAO:       dao.NewLLMDAO(),
	}
}

// MyLLMItem 表示「我的 LLM」响应中的单个模型条目。
type MyLLMItem struct {
	ID        string `json:"id"`
	Type      string `json:"type"`
	Name      string `json:"name"`
	UsedToken int64  `json:"used_token"`
	Status    string `json:"status"`
	APIBase   string `json:"api_base,omitempty"`
	MaxTokens int64  `json:"max_tokens,omitempty"`
}

// MyLLMFactory 按工厂分组展示租户已配置的 LLM 列表。
type MyLLMFactory struct {
	Tags string      `json:"tags"`
	LLM  []MyLLMItem `json:"llm"`
}

// GetMyLLMs 查询租户已添加的 LLM，可按工厂聚合并可选返回详情。
func (s *LLMService) GetMyLLMs(tenantID string, includeDetails bool) (map[string]MyLLMFactory, error) {
	result := make(map[string]MyLLMFactory)

	if includeDetails {
		objs, err := s.tenantLLMDAO.ListAllByTenant(tenantID)
		if err != nil {
			return nil, err
		}

		factoryDAO := dao.NewLLMFactoryDAO()
		factories, err := factoryDAO.GetAllValid()
		if err != nil {
			return nil, err
		}

		factoryTagsMap := make(map[string]string)
		for _, f := range factories {
			if f.Tags != "" {
				factoryTagsMap[f.Name] = f.Tags
			}
		}

		for _, o := range objs {
			llmFactory := o.LLMFactory
			if _, exists := result[llmFactory]; !exists {
				tags := factoryTagsMap[llmFactory]
				result[llmFactory] = MyLLMFactory{
					Tags: tags,
					LLM:  []MyLLMItem{},
				}
			}

			item := MyLLMItem{
				ID:        int64ToString(o.ID),
				Type:      getStringValue(o.ModelType),
				Name:      getStringValue(o.LLMName),
				UsedToken: o.UsedTokens,
				Status:    getValidStatus(o.Status),
			}

			if includeDetails {
				item.APIBase = getStringValueDefault(o.APIBase, "")
				item.MaxTokens = o.MaxTokens
			}

			factory := result[llmFactory]
			factory.LLM = append(factory.LLM, item)
			result[llmFactory] = factory
		}
	} else {
		objs, err := s.tenantLLMDAO.GetMyLLMs(tenantID)
		if err != nil {
			return nil, err
		}

		for _, o := range objs {
			llmFactory := o.LLMFactory
			if _, exists := result[llmFactory]; !exists {
				result[llmFactory] = MyLLMFactory{
					Tags: getStringValue(o.Tags),
					LLM:  []MyLLMItem{},
				}
			}

			item := MyLLMItem{
				ID:        o.ID,
				Type:      getStringValue(o.ModelType),
				Name:      getStringValue(o.LLMName),
				UsedToken: getInt64Value(o.UsedTokens),
				Status:    getStringValueDefault(o.Status, "1"),
			}

			factory := result[llmFactory]
			factory.LLM = append(factory.LLM, item)
			result[llmFactory] = factory
		}
	}

	return result, nil
}

// LLMListItem 模型列表 API 的单条记录结构。
type LLMListItem struct {
	ID         string  `json:"id"`
	LLMName    string  `json:"llm_name"`
	ModelType  string  `json:"model_type"`
	FID        string  `json:"fid"`
	Available  bool    `json:"available"`
	Status     string  `json:"status"`
	MaxTokens  int64   `json:"max_tokens,omitempty"`
	CreateDate *string `json:"create_date,omitempty"`
	CreateTime *int64  `json:"create_time,omitempty"`
	UpdateDate *string `json:"update_date,omitempty"`
	UpdateTime *int64  `json:"update_time,omitempty"`
	IsTools    bool    `json:"is_tools"`
	Tags       string  `json:"tags,omitempty"`
}

// ListLLMsResponse 按工厂分组的 LLM 列表响应。
type ListLLMsResponse map[string][]LLMListItem

// ListLLMs 列出租户可见模型并标注是否已配置密钥/可自部署可用。
func (s *LLMService) ListLLMs(tenantID string, modelType string) (ListLLMsResponse, error) {
	selfDeployed := map[string]bool{
		"FastEmbed":  true,
		"Ollama":     true,
		"Xinference": true,
		"LocalAI":    true,
		"LM-Studio":  true,
		"GPUStack":   true,
		"ModelScope": true,
	}

	objs, err := s.tenantLLMDAO.ListAllByTenant(tenantID)
	if err != nil {
		return nil, err
	}

	facts := make(map[string]bool)
	status := make(map[string]bool)
	tenantLLMMapping := make(map[string]string)

	for _, o := range objs {
		if o.APIKey != nil && *o.APIKey != "" && getValidStatus(o.Status) == "1" {
			facts[o.LLMFactory] = true
		}
		llmName := getStringValue(o.LLMName)
		key := llmName + "@" + o.LLMFactory
		if getValidStatus(o.Status) == "1" {
			status[key] = true
		}
		tenantLLMMapping[key] = int64ToString(o.ID)
	}

	allLLMs, err := s.llmDAO.GetAllValid()
	if err != nil {
		return nil, err
	}

	llmSet := make(map[string]bool)
	result := make(ListLLMsResponse)

	for _, llm := range allLLMs {
		if llm.Status == nil || *llm.Status != "1" {
			continue
		}

		key := llm.LLMName + "@" + llm.FID

		if llm.FID != "Builtin" && !status[key] {
			continue
		}

		if modelType != "" && !strings.Contains(llm.ModelType, modelType) {
			continue
		}

		available := facts[llm.FID] || selfDeployed[llm.FID] || strings.ToLower(llm.LLMName) == "flag-embedding"

		item := LLMListItem{
			ID:        tenantLLMMapping[key],
			LLMName:   llm.LLMName,
			ModelType: llm.ModelType,
			FID:       llm.FID,
			Available: available,
			Status:    "1",
			MaxTokens: llm.MaxTokens,
			IsTools:   llm.IsTools,
			Tags:      llm.Tags,
		}

		if llm.CreateDate != nil {
			createDateStr := llm.CreateDate.Format("2006-01-02T15:04:05")
			item.CreateDate = &createDateStr
		}
		item.CreateTime = llm.CreateTime
		if llm.UpdateDate != nil {
			updateDateStr := llm.UpdateDate.Format("2006-01-02T15:04:05")
			item.UpdateDate = &updateDateStr
		}
		if llm.UpdateTime != nil {
			item.UpdateTime = llm.UpdateTime
		}

		result[llm.FID] = append(result[llm.FID], item)
		llmSet[key] = true
	}

	for _, o := range objs {
		llmName := getStringValue(o.LLMName)
		key := llmName + "@" + o.LLMFactory
		if llmSet[key] {
			continue
		}

		modelTypeValue := getStringValue(o.ModelType)
		if modelType != "" && !strings.Contains(modelTypeValue, modelType) {
			continue
		}

		item := LLMListItem{
			ID:        int64ToString(o.ID),
			LLMName:   llmName,
			ModelType: modelTypeValue,
			FID:       o.LLMFactory,
			Available: true,
			Status:    getValidStatus(o.Status),
		}

		result[o.LLMFactory] = append(result[o.LLMFactory], item)
	}

	return result, nil
}

func getStringValue(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

func getStringValueDefault(s *string, defaultVal string) string {
	if s == nil || *s == "" {
		return defaultVal
	}
	return *s
}

func getValidStatus(status string) string {
	if status == "" {
		return "1"
	}
	return status
}

func getInt64Value(i *int64) int64 {
	if i == nil {
		return 0
	}
	return *i
}

func getInt64ValueDefault(i *int64, defaultVal int64) int64 {
	if i == nil || *i == 0 {
		return defaultVal
	}
	return *i
}

func getBoolValue(b *bool) bool {
	if b == nil {
		return false
	}
	return *b
}

func int64ToString(n int64) string {
	return strconv.FormatInt(n, 10)
}

// SetAPIKeyRequest 为某 LLM 工厂批量写入 API Key 的请求体。
type SetAPIKeyRequest struct {
	LLMFactory string `json:"llm_factory"`
	APIKey     string `json:"api_key"`
	BaseURL    string `json:"base_url"`
	SourceFID  string `json:"source_fid"`
	ModelType  string `json:"model_type"`
	LLMName    string `json:"llm_name"`
	Verify     bool   `json:"verify"`
	MaxTokens  int64  `json:"max_tokens"`
}

// SetAPIKeyResult 设置 API Key 的操作结果。
type SetAPIKeyResult struct {
	Message string `json:"message"`
	Success bool   `json:"success"`
}

// SetAPIKey 为指定工厂下全部模型创建或更新 tenant_llm 记录。
func (s *LLMService) SetAPIKey(tenantID string, req *SetAPIKeyRequest) (*SetAPIKeyResult, error) {
	factory := req.LLMFactory
	baseURL := req.BaseURL
	sourceFactory := req.SourceFID
	if sourceFactory == "" {
		sourceFactory = factory
	}

	sourceLLMs, err := s.llmDAO.GetByFactory(sourceFactory)
	if err != nil || len(sourceLLMs) == 0 {
		msg := "No models configured for " + factory + " (source: " + sourceFactory + ")."
		if req.Verify {
			return &SetAPIKeyResult{Message: msg, Success: false}, nil
		}
		return nil, fmt.Errorf("%s", msg)
	}

	llmConfig := map[string]interface{}{
		"api_key":  req.APIKey,
		"api_base": baseURL,
	}

	if req.ModelType != "" {
		llmConfig["model_type"] = req.ModelType
	}
	if req.LLMName != "" {
		llmConfig["llm_name"] = req.LLMName
	}

	for _, llm := range sourceLLMs {
		maxTokens := llm.MaxTokens
		if maxTokens == 0 {
			maxTokens = 8192
		}
		llmConfig["max_tokens"] = maxTokens

		existingLLM, _ := s.tenantLLMDAO.GetByTenantFactoryAndModelName(tenantID, factory, llm.LLMName)
		if existingLLM != nil {
			updates := map[string]interface{}{
				"api_key":    req.APIKey,
				"api_base":   baseURL,
				"max_tokens": maxTokens,
			}
			DB.Model(&entity.TenantLLM{}).
				Where("tenant_id = ? AND llm_factory = ? AND llm_name = ?", tenantID, factory, llm.LLMName).
				Updates(updates)
		} else {
			modelType := llm.ModelType
			llmName := llm.LLMName
			tenantLLM := &entity.TenantLLM{
				TenantID:   tenantID,
				LLMFactory: factory,
				ModelType:  &modelType,
				LLMName:    &llmName,
				APIKey:     &req.APIKey,
				APIBase:    &baseURL,
				MaxTokens:  maxTokens,
				Status:     "1",
			}
			s.tenantLLMDAO.Create(tenantLLM)
		}
	}

	return &SetAPIKeyResult{Message: "", Success: true}, nil
}
// llm.go — 租户 LLM 工厂/模型列表、可用性与 API Key 绑定管理。
