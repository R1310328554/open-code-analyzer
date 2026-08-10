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

// dify_retrieval_handler.go — Dify 兼容检索 API：POST/GET /api/v1/dify/retrieval，对齐 Python dify_retrieval_api。

//

package handler

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"ragflow/internal/common"
	"ragflow/internal/engine"
	"ragflow/internal/entity"
	modelModule "ragflow/internal/entity/models"
	"ragflow/internal/service"
	"ragflow/internal/service/graph"
	"ragflow/internal/service/nlp"

	"go.uber.org/zap"
	"gorm.io/gorm"

	"github.com/gin-gonic/gin"
)

// --- Interfaces (for testability) ---

// KBServiceIface 知识库服务抽象（便于测试注入）。
type KBServiceIface interface {
	GetByID(kbID string) (*entity.Knowledgebase, error)
	Accessible(kbID, userID string) bool
}

// ModelServiceIface 模型服务抽象。
type ModelServiceIface interface {
	GetEmbeddingModel(tenantID, embdID string) (*modelModule.EmbeddingModel, error)
	GetChatModel(tenantID, compositeModelName string) (*modelModule.ChatModel, error)
}

// MetadataServiceIface 元数据服务抽象。
type MetadataServiceIface interface {
	GetFlattedMetaByKBs(kbIDs []string) (common.MetaData, error)
	LabelQuestion(question string, kbs []*entity.Knowledgebase) map[string]float64
}

// RetrievalServiceIface 检索服务抽象。
type RetrievalServiceIface interface {
	Retrieval(ctx context.Context, req *nlp.RetrievalRequest) (*nlp.RetrievalResult, error)
}

// DocumentDAOIface 文档 DAO 抽象。
type DocumentDAOIface interface {
	GetByIDs(ids []string) ([]*entity.Document, error)
}

// --- Request / Response types ---

// difyRetrievalRequest Dify 检索端点的 JSON/query 请求体。
type difyRetrievalRequest struct {
	KnowledgeID       string                 `json:"knowledge_id" form:"knowledge_id"`
	Query             string                 `json:"query" form:"query"`
	UseKG             bool                   `json:"use_kg" form:"use_kg"`
	RetrievalSetting  *difyRetrievalSetting  `json:"retrieval_setting"`
	MetadataCondition *difyMetadataCondition `json:"metadata_condition"`
}

type difyRetrievalSetting struct {
	TopK           *int     `json:"top_k" form:"top_k"`
	ScoreThreshold *float64 `json:"score_threshold" form:"score_threshold"`
}

// difyCondition Dify 格式元数据过滤条件（name/comparison_operator）。
// 字段名与内部 MetaFilterCondition 的 key/op 不同。
type difyCondition struct {
	Name               string      `json:"name"`
	ComparisonOperator string      `json:"comparison_operator"`
	Value              interface{} `json:"value"`
}

type difyMetadataCondition struct {
	Conditions []difyCondition `json:"conditions"`
	Logic      string          `json:"logic"`
}

// toMetaFilterConditions 将 Dify 条件转为内部 MetaFilterCondition。
func (c difyMetadataCondition) toMetaFilterConditions() []service.MetaFilterCondition {
	if len(c.Conditions) == 0 {
		return nil
	}
	result := make([]service.MetaFilterCondition, len(c.Conditions))
	for i, dc := range c.Conditions {
		v := ""
		if dc.Value != nil {
			v = fmt.Sprint(dc.Value)
		}
		result[i] = service.MetaFilterCondition{
			Key:   dc.Name,
			Op:    dc.ComparisonOperator,
			Value: v,
		}
	}
	return result
}

// difyRecord 响应 records 数组中的单条记录。
type difyRecord struct {
	Content  string                 `json:"content"`
	Score    float64                `json:"score"`
	Title    string                 `json:"title"`
	Metadata map[string]interface{} `json:"metadata"`
}

// --- Handler ---

// DifyRetrievalHandler Dify 兼容检索 HTTP 处理器。
type DifyRetrievalHandler struct {
	kbSvc        KBServiceIface
	modelSvc     ModelServiceIface
	metadataSvc  MetadataServiceIface
	retrievalSvc RetrievalServiceIface
	docDAO       DocumentDAOIface
	docEngine    engine.DocEngine
}

// NewDifyRetrievalHandler 构造处理器；KG 管道在 use_kg=true 时按需创建。
// The KG pipeline is created inline when use_kg=true to avoid injecting
// a pipeline that depends on per-request model configuration.
func NewDifyRetrievalHandler(
	kbSvc KBServiceIface,
	modelSvc ModelServiceIface,
	metadataSvc MetadataServiceIface,
	retrievalSvc RetrievalServiceIface,
	docDAO DocumentDAOIface,
	docEngine engine.DocEngine,
) *DifyRetrievalHandler {
	return &DifyRetrievalHandler{
		kbSvc:        kbSvc,
		modelSvc:     modelSvc,
		metadataSvc:  metadataSvc,
		retrievalSvc: retrievalSvc,
		docDAO:       docDAO,
		docEngine:    docEngine,
	}
}

// Retrieval 处理 POST/GET /api/v1/dify/retrieval（对齐 Python retrieval()）。
// Matches Python: api/apps/restful_apis/dify_retrieval_api.py::retrieval()
func (h *DifyRetrievalHandler) Retrieval(c *gin.Context) {
	user, errCode, errMsg := GetUser(c)
	if errCode != common.CodeSuccess {
		common.ResponseWithHttpCodeData(c, http.StatusUnauthorized, errCode, nil, errMsg)
		return
	}

	var req difyRetrievalRequest
	if c.Request.Method == http.MethodGet {
		if err := c.ShouldBindQuery(&req); err != nil {
			common.ResponseWithHttpCodeData(c, http.StatusBadRequest, common.CodeArgumentError, nil, "invalid query parameters")
			return
		}
		// GET 时从扁平 query 手动提取 top_k 与 score_threshold
		if v := c.Query("top_k"); v != "" {
			if parsed, err := strconv.Atoi(v); err == nil {
				if req.RetrievalSetting == nil {
					req.RetrievalSetting = &difyRetrievalSetting{}
				}
				req.RetrievalSetting.TopK = &parsed
			}
		}
		if v := c.Query("score_threshold"); v != "" {
			if parsed, err := strconv.ParseFloat(v, 64); err == nil {
				if req.RetrievalSetting == nil {
					req.RetrievalSetting = &difyRetrievalSetting{}
				}
				req.RetrievalSetting.ScoreThreshold = &parsed
			}
		}
	} else {
		if err := c.ShouldBindJSON(&req); err != nil {
			common.ResponseWithHttpCodeData(c, http.StatusBadRequest, common.CodeArgumentError, nil, "invalid request body")
			return
		}
	}

	if req.KnowledgeID == "" || req.Query == "" {
		common.ResponseWithHttpCodeData(c, http.StatusBadRequest, common.CodeArgumentError, nil, "knowledge_id and query are required")
		return
	}

	kb, err := h.kbSvc.GetByID(req.KnowledgeID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			common.ResponseWithHttpCodeData(c, http.StatusNotFound, common.CodeNotFound, nil, "Knowledge base not found!")
		} else {
			common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, common.CodeServerError, nil, "failed to query knowledge base")
		}
		return
	}

	if !h.kbSvc.Accessible(req.KnowledgeID, user.ID) {
		common.ResponseWithHttpCodeData(c, http.StatusUnauthorized, common.CodeAuthenticationError, nil, "No authorization")
		return
	}

	// 解析检索选项（nil 表示使用服务默认值）
	var topK *int
	if req.RetrievalSetting != nil && req.RetrievalSetting.TopK != nil {
		topK = req.RetrievalSetting.TopK
	}
	var scoreThreshold *float64
	if req.RetrievalSetting != nil && req.RetrievalSetting.ScoreThreshold != nil {
		scoreThreshold = req.RetrievalSetting.ScoreThreshold
	}
	pageSize := 1024
	if topK != nil {
		pageSize = *topK
	}

	// 获取嵌入模型
	embModel, err := h.modelSvc.GetEmbeddingModel(kb.TenantID, kb.EmbdID)
	if err != nil {
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, common.CodeServerError, nil, fmt.Sprintf("failed to get embedding model: %v", err))
		return
	}

	// 元数据过滤：ApplyMetaFilter 得到 docIDs
	metas, metaErr := h.metadataSvc.GetFlattedMetaByKBs([]string{req.KnowledgeID})
	docIDs := make([]string, 0)
	if metaErr == nil && req.MetadataCondition != nil {
		logic := req.MetadataCondition.Logic
		if logic == "" {
			logic = "and"
		}
		filteredIDs := service.ApplyMetaFilter(metas, req.MetadataCondition.toMetaFilterConditions(), logic)
		docIDs = append(docIDs, filteredIDs...)
	}
	if len(docIDs) == 0 && req.MetadataCondition != nil {
		docIDs = []string{service.NoMatchDocIDSentinel}
	}

	// 为排序特征标注问题
	kbs := []*entity.Knowledgebase{kb}
	rankFeature := h.metadataSvc.LabelQuestion(req.Query, kbs)

	// 向量分块检索
	sr := &nlp.RetrievalRequest{
		Question:            req.Query,
		TenantIDs:           []string{kb.TenantID},
		KbIDs:               []string{req.KnowledgeID},
		DocIDs:              docIDs,
		Page:                1,
		PageSize:            pageSize,
		Top:                 topK,
		SimilarityThreshold: scoreThreshold,
		EmbeddingModel:      embModel,
	}
	if rankFeature != nil {
		sr.RankFeature = &rankFeature
	}

	result, err := h.retrievalSvc.Retrieval(c.Request.Context(), sr)
	if err != nil {
		if strings.Contains(err.Error(), "not_found") {
			common.ResponseWithHttpCodeData(c, http.StatusNotFound, common.CodeNotFound, nil, "No chunk found! Check the chunk status please!")
			return
		}
		common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, common.CodeServerError, nil, err.Error())
		return
	}

	// 展开子分块 enrich
	chunks := nlp.RetrievalByChildren(result.Chunks, []string{kb.TenantID}, h.docEngine, c.Request.Context())

	// 可选知识图谱检索（use_kg）
	if req.UseKG {
		chatModel, kgErr := h.modelSvc.GetChatModel(kb.TenantID, "")
		if kgErr != nil {
			common.Warn("KG retrieval: failed to get chat model", zap.String("kbID", req.KnowledgeID), zap.Error(kgErr))
		} else if chatModel != nil {
			kgPipeline := graph.NewPipeline(
				h.docEngine,
				[]string{req.KnowledgeID},
				[]string{kb.TenantID},
				req.Query,
			)
			kgPipeline.SetChatModel(chatModel)
			kgPipeline.SetEmbModel(embModel)
			if kgResult, kgErr := kgPipeline.Retrieval(c.Request.Context()); kgErr == nil {
				if content, ok := kgResult["content_with_weight"].(string); ok && content != "" {
					chunks = append([]map[string]interface{}{kgResult}, chunks...)
				}
			}
		}
	}

	// 收集 doc_id 并批量加载文档
	docIDSet := make(map[string]struct{})
	for _, ch := range chunks {
		if docID, ok := ch["doc_id"].(string); ok && docID != "" {
			docIDSet[docID] = struct{}{}
		}
	}
	allDocIDs := make([]string, 0, len(docIDSet))
	for id := range docIDSet {
		allDocIDs = append(allDocIDs, id)
	}

	docMap := make(map[string]*entity.Document)
	if len(allDocIDs) > 0 {
		var docs []*entity.Document
		docs, err = h.docDAO.GetByIDs(allDocIDs)
		if err != nil {
			common.ResponseWithHttpCodeData(c, http.StatusInternalServerError, common.CodeServerError, nil, fmt.Sprintf("failed to load documents: %v", err))
			return
		}
		for _, d := range docs {
			docMap[d.ID] = d
		}
	}

	// 组装 Dify records 响应
	records := make([]difyRecord, 0, len(chunks))
	for _, ch := range chunks {
		docID, _ := ch["doc_id"].(string)
		doc := docMap[docID]
		if doc == nil {
			continue
		}

		// 移除 vector 字段减小响应体积
		delete(ch, "vector")

		meta := make(map[string]interface{})
		if doc.MetaFields != nil {
			for k, v := range *doc.MetaFields {
				meta[k] = v
			}
		}
		meta["doc_id"] = docID
		meta["document_id"] = docID

		score, _ := ch["similarity"].(float64)
		title, _ := ch["docnm_kwd"].(string)
		content, _ := ch["content_with_weight"].(string)

		records = append(records, difyRecord{
			Content:  content,
			Score:    score,
			Title:    title,
			Metadata: meta,
		})
	}

	c.JSON(http.StatusOK, gin.H{"records": records})
}

// HealthCheck 健康检查，返回简单成功响应。
func (h *DifyRetrievalHandler) HealthCheck(c *gin.Context) {
	common.SuccessNoMessage(c, true)
}
