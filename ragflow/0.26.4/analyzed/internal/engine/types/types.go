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

// types.go — 检索引擎统一抽象：SearchRequest/Result、排序表达式、文本/向量/融合匹配表达式；ES 与 Infinity 共用同一请求/结果模型。

package types

import (
	"errors"
	"fmt"

	"go.uber.org/zap"

	"ragflow/internal/common"
)

var ErrDocumentNotFound = errors.New("document not found")

// SearchRequest 跨引擎统一检索请求（索引、分页、过滤、匹配表达式）
type SearchRequest struct {
	// 检索目标
	IndexNames []string // ES 为索引名；Infinity 视为表名前缀
	KbIDs      []string // 知识库 ID 过滤

	// 分页参数
	Offset int // 分页偏移（从 0 起）
	Limit  int // 每页条数上限

	// 返回字段列表（ES _source）
	SelectFields []string // 需返回的字段名

	// 结构化过滤条件
	Filter map[string]interface{} // 检索过滤 map

	// Match expressions
	MatchExprs []interface{} // 匹配表达式：MatchText/Dense/Fusion

	// 排序与 LTR 特征
	OrderBy     *OrderByExpr       // 多字段升/降序
	RankFeature map[string]float64 // 学习排序特征权重
}

// SearchResult 统一检索结果（分块列表 + 总数）
type SearchResult struct {
	Chunks []map[string]interface{} // 命中文档/分块记录
	Total  int64                    // 命中总数
}

// SearchMetadataResult 元数据索引检索结果
type SearchMetadataResult struct {
	MetadataRecords []map[string]interface{} // 元数据记录列表
	Total           int64                    // Total number of matches
}

// SearchMetadataRequest 租户元数据索引检索请求
type SearchMetadataRequest struct {
	TenantID     string                 // 租户 ID（索引名 ragflow_doc_meta_{tenantID}）
	Offset       int                    // Pagination offset
	Limit        int                    // Pagination limit
	SelectFields []string               // List of field names to return (nil means all fields)
	Filter       map[string]interface{} // Filters for search
	OrderBy      *OrderByExpr           // Order by expression
}

type OrderByExpr struct {
	Fields []OrderByField
}

// OrderByField 单字段排序项
type OrderByField struct {
	Field string
	Type  OrderByType
}

// OrderByType 升序或降序枚举
type OrderByType int

const (
	// SortAsc 升序
	SortAsc OrderByType = 0
	// SortDesc 降序
	SortDesc OrderByType = 1
)

// Asc 追加升序字段（链式调用）
func (o *OrderByExpr) Asc(field string) *OrderByExpr {
	o.Fields = append(o.Fields, OrderByField{Field: field, Type: SortAsc})
	return o
}

// Desc 追加降序字段（链式调用）
func (o *OrderByExpr) Desc(field string) *OrderByExpr {
	o.Fields = append(o.Fields, OrderByField{Field: field, Type: SortDesc})
	return o
}

// MatchTextExpr 全文/关键词匹配表达式
type MatchTextExpr struct {
	Fields       []string               // 检索字段（可带 boost，如 title_tks^10）
	MatchingText string                 // 待匹配查询文本
	TopN         int                    // 返回 TopN
	ExtraOptions map[string]interface{} // Additional options (e.g., minimum_should_match, filter)
}

// MatchDenseExpr 稠密向量相似度匹配
type MatchDenseExpr struct {
	VectorColumnName  string
	EmbeddingData     []float64
	EmbeddingDataType string
	DistanceType      string
	TopN              int
	ExtraOptions      map[string]interface{}
}

// FusionExpr 混合检索融合表达式（如 weighted_sum）
type FusionExpr struct {
	Method       string                 // 融合方法名
	TopN         int                    // TopK for fusion
	FusionParams map[string]interface{} // 融合参数（如 weights）
}

// LogSearchRequest 在 Debug 模式下结构化打印检索请求
func LogSearchRequest(engineName string, req *SearchRequest) {
	common.Info(fmt.Sprintf("Search in %s started", engineName), zap.Any("indexNames", req.IndexNames))

	if !common.IsDebugEnabled() {
		return
	}

	var matchExprsStr string
	for i, expr := range req.MatchExprs {
		switch e := expr.(type) {
		case *MatchTextExpr:
			matchExprsStr += fmt.Sprintf("    [%d] MatchTextExpr: fields=%v, matchingText=%s, topN=%d, extraOptions=%v\n", i, e.Fields, e.MatchingText, e.TopN, e.ExtraOptions)
		case *MatchDenseExpr:
			matchExprsStr += fmt.Sprintf("    [%d] MatchDenseExpr: vectorColumn=%s, vectorSize=%d, topN=%d, extraOptions=%v\n", i, e.VectorColumnName, len(e.EmbeddingData), e.TopN, e.ExtraOptions)
		case *FusionExpr:
			matchExprsStr += fmt.Sprintf("    [%d] FusionExpr: method=%s, topN=%d, fusionParams=%v\n", i, e.Method, e.TopN, e.FusionParams)
		default:
			matchExprsStr += fmt.Sprintf("    [%d] unknown type\n", i)
		}
	}

	common.Debug(fmt.Sprintf("Search request:\n"+
		"    indexNames=%v\n"+
		"    KbIDs=%v\n"+
		"    offset=%d, limit=%d\n"+
		"    SelectFields=%v\n"+
		"    Filter=%v\n"+
		"    MatchExprs:\n%s    orderBy=%v\n"+
		"    RankFeature=%v",
		req.IndexNames, req.KbIDs, req.Offset, req.Limit, req.SelectFields, req.Filter, matchExprsStr, req.OrderBy, req.RankFeature))
}

// 设计说明：本包屏蔽 ES/Infinity 方言差异，MatchExprs 以 interface{} 承载多态表达式，引擎实现层负责类型断言与 DSL 翻译。ErrDocumentNotFound 供文档级检索未命中时使用。
