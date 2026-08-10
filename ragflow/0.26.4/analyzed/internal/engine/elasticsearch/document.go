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

// document.go — Elasticsearch 文档 CRUD 与批量索引：封装 Index/Get/Delete 及 Bulk API，供技能索引与普通文档写入。
//

package elasticsearch

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"

	"github.com/elastic/go-elasticsearch/v8/esapi"
)

// IndexDocument 向指定索引写入单条文档，Refresh=true 立即可搜。
func (e *elasticsearchEngine) IndexDocument(ctx context.Context, indexName, docID string, doc interface{}) error {
	if indexName == "" {
		return fmt.Errorf("index name cannot be empty")
	}
	if docID == "" {
		return fmt.Errorf("document id cannot be empty")
	}
	if doc == nil {
		return fmt.Errorf("document cannot be nil")
	}

	// 序列化文档为 JSON
	data, err := json.Marshal(doc)
	if err != nil {
		return fmt.Errorf("failed to marshal document: %w", err)
	}

	// 构造 IndexRequest 并执行
	req := esapi.IndexRequest{
		Index:      indexName,
		DocumentID: docID,
		Body:       bytes.NewReader(data),
		Refresh:    "true",
	}

	res, err := req.Do(ctx, e.client)
	if err != nil {
		return fmt.Errorf("failed to index document: %w", err)
	}
	defer res.Body.Close()

	if res.IsError() {
		body, _ := io.ReadAll(res.Body)
		reason := extractErrorReason(body)
		if reason != "" {
			return fmt.Errorf("elasticsearch error: %s", reason)
		}
		return fmt.Errorf("elasticsearch returned error: %s, body: %s", res.Status(), string(body))
	}

	return nil
}

// BulkIndex 批量索引文档；每条须含 _id，写入前会从 body 中移除 _id 字段。
func (e *elasticsearchEngine) BulkIndex(ctx context.Context, indexName string, docs []interface{}) (interface{}, error) {
	if indexName == "" {
		return nil, fmt.Errorf("index name cannot be empty")
	}
	if len(docs) == 0 {
		return nil, fmt.Errorf("documents cannot be empty")
	}

	// 组装 NDJSON 格式的 bulk 请求体
	var buf bytes.Buffer
	for _, doc := range docs {
		docMap, ok := doc.(map[string]interface{})
		if !ok {
			return nil, fmt.Errorf("document must be map[string]interface{}")
		}

		docID, hasID := docMap["_id"]
		if !hasID {
			return nil, fmt.Errorf("document missing _id field")
		}

		// 删除 _id 避免与 meta 行重复
		delete(docMap, "_id")

		// 写入 index 操作行
		meta := map[string]interface{}{
			"_index": indexName,
			"_id":    docID,
		}
		metaData, _ := json.Marshal(meta)
		docData, _ := json.Marshal(docMap)

		buf.Write(metaData)
		buf.WriteByte('\n')
		buf.Write(docData)
		buf.WriteByte('\n')
	}

	// 执行 bulk 请求
	req := esapi.BulkRequest{
		Body:    &buf,
		Refresh: "true",
	}

	res, err := req.Do(ctx, e.client)
	if err != nil {
		return nil, fmt.Errorf("bulk index failed: %w", err)
	}
	defer res.Body.Close()

	if res.IsError() {
		body, _ := io.ReadAll(res.Body)
		reason := extractErrorReason(body)
		if reason != "" {
			return nil, fmt.Errorf("elasticsearch error: %s", reason)
		}
		return nil, fmt.Errorf("elasticsearch returned error: %s", res.Status())
	}

	// 解析 bulk 响应
	var result map[string]interface{}
	if err := json.NewDecoder(res.Body).Decode(&result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	// 检查 items 中是否有单条失败
	if errors, ok := result["errors"].(bool); ok && errors {
		// 提取首条错误 reason
		if items, ok := result["items"].([]interface{}); ok && len(items) > 0 {
			for _, item := range items {
				if itemMap, ok := item.(map[string]interface{}); ok {
					for _, op := range itemMap {
						if opMap, ok := op.(map[string]interface{}); ok {
							if errInfo, ok := opMap["error"].(map[string]interface{}); ok {
								if reason, ok := errInfo["reason"].(string); ok {
									return nil, fmt.Errorf("bulk index error: %s", reason)
								}
							}
						}
					}
				}
			}
		}
		return nil, fmt.Errorf("bulk index has errors")
	}

	response := &BulkResponse{
		Took:    int64(result["took"].(float64)),
		Errors:  result["errors"].(bool),
		Indexed: len(docs),
	}

	return response, nil
}

// BulkResponse 批量写入响应摘要。
type BulkResponse struct {
	Took    int64
	Errors  bool
	Indexed int
}

// GetDocument 按索引名与文档 ID 读取 _source。
func (e *elasticsearchEngine) GetDocument(ctx context.Context, indexName, docID string) (interface{}, error) {
	if indexName == "" {
		return nil, fmt.Errorf("index name cannot be empty")
	}
	if docID == "" {
		return nil, fmt.Errorf("document id cannot be empty")
	}

	// 执行 GetRequest
	req := esapi.GetRequest{
		Index:      indexName,
		DocumentID: docID,
	}

	res, err := req.Do(ctx, e.client)
	if err != nil {
		return nil, fmt.Errorf("failed to get document: %w", err)
	}
	defer res.Body.Close()

	if res.StatusCode == 404 {
		return nil, fmt.Errorf("document not found")
	}

	if res.IsError() {
		body, _ := io.ReadAll(res.Body)
		reason := extractErrorReason(body)
		if reason != "" {
			return nil, fmt.Errorf("elasticsearch error: %s", reason)
		}
		return nil, fmt.Errorf("elasticsearch returned error: %s", res.Status())
	}

	// Parse response
	var result map[string]interface{}
	if err := json.NewDecoder(res.Body).Decode(&result); err != nil {
		return nil, fmt.Errorf("failed to parse response: %w", err)
	}

	if found, ok := result["found"].(bool); !ok || !found {
		return nil, fmt.Errorf("document not found")
	}

	return result["_source"], nil
}

// DeleteDocument 按 ID 删除文档，404 视为未找到。
func (e *elasticsearchEngine) DeleteDocument(ctx context.Context, indexName, docID string) error {
	if indexName == "" {
		return fmt.Errorf("index name cannot be empty")
	}
	if docID == "" {
		return fmt.Errorf("document id cannot be empty")
	}

	// 执行 DeleteRequest
	req := esapi.DeleteRequest{
		Index:      indexName,
		DocumentID: docID,
		Refresh:    "true",
	}

	res, err := req.Do(ctx, e.client)
	if err != nil {
		return fmt.Errorf("failed to delete document: %w", err)
	}
	defer res.Body.Close()

	if res.StatusCode == 404 {
		return fmt.Errorf("document not found")
	}

	if res.IsError() {
		body, _ := io.ReadAll(res.Body)
		reason := extractErrorReason(body)
		if reason != "" {
			return fmt.Errorf("elasticsearch error: %s", reason)
		}
		return fmt.Errorf("elasticsearch returned error: %s, body: %s", res.Status(), string(body))
	}

	return nil
}
