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

// common.go — Elasticsearch 索引存在性检查、删除与元数据索引命名。

package elasticsearch

import (
	"context"
	"fmt"
	"io"

	"github.com/elastic/go-elasticsearch/v8/esapi"
)

// dropIndex 删除指定索引（须先存在）
func (e *elasticsearchEngine) dropIndex(ctx context.Context, indexName string) error {
	if indexName == "" {
		return fmt.Errorf("index name cannot be empty")
	}

	// 检查索引是否存在
	exists, err := e.indexExists(ctx, indexName)
	if err != nil {
		return fmt.Errorf("failed to check index existence: %w", err)
	}
	if !exists {
		return fmt.Errorf("index '%s' does not exist", indexName)
	}

	// 调用 IndicesDelete 删除
	req := esapi.IndicesDeleteRequest{
		Index: []string{indexName},
	}

	res, err := req.Do(ctx, e.client)
	if err != nil {
		return fmt.Errorf("failed to delete index: %w", err)
	}
	defer res.Body.Close()

	if res.IsError() {
		bodyBytes, _ := io.ReadAll(res.Body)
		reason := extractErrorReason(bodyBytes)
		if reason != "" {
			return fmt.Errorf("elasticsearch error: %s", reason)
		}
		return fmt.Errorf("elasticsearch returned error: %s", res.Status())
	}

	return nil
}

// indexExists 通过 HEAD 判断索引是否存在（200/404）
func (e *elasticsearchEngine) indexExists(ctx context.Context, indexName string) (bool, error) {
	if indexName == "" {
		return false, fmt.Errorf("index name cannot be empty")
	}

	req := esapi.IndicesExistsRequest{
		Index: []string{indexName},
	}

	res, err := req.Do(ctx, e.client)
	if err != nil {
		return false, fmt.Errorf("failed to check index existence: %w", err)
	}
	defer res.Body.Close()

	if res.StatusCode == 200 {
		return true, nil
	} else if res.StatusCode == 404 {
		return false, nil
	}

	bodyBytes, _ := io.ReadAll(res.Body)
	reason := extractErrorReason(bodyBytes)
	if reason != "" {
		return false, fmt.Errorf("elasticsearch error: %s", reason)
	}
	return false, fmt.Errorf("elasticsearch returned error: %s", res.Status())
}

// buildMetadataIndexName 生成租户元数据索引名 ragflow_doc_meta_{tenantID}
func buildMetadataIndexName(tenantID string) string {
	return fmt.Sprintf("ragflow_doc_meta_%s", tenantID)
}
