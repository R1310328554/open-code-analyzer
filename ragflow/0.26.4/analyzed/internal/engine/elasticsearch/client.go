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

// client.go — Elasticsearch 引擎客户端：连接配置、健康检查、索引模板注册与集群/索引统计。

package elasticsearch

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"ragflow/internal/server"
	"ragflow/internal/utility"
	"time"

	"github.com/elastic/go-elasticsearch/v8"
	"github.com/elastic/go-elasticsearch/v8/esapi"
)

// elasticsearchEngine Elasticsearch 文档引擎实现
type elasticsearchEngine struct {
	client *elasticsearch.Client
	config *server.ElasticsearchConfig
}

// NewEngine 从配置创建 ES 客户端并注册索引模板
func NewEngine(cfg interface{}) (*elasticsearchEngine, error) {
	if cfg == nil {
		return nil, fmt.Errorf("elasticsearch config is nil, please check your configuration file for 'doc_engine.es' settings")
	}
	esConfig, ok := cfg.(*server.ElasticsearchConfig)
	if !ok {
		return nil, fmt.Errorf("invalid Elasticsearch config type, expected *config.ElasticsearchConfig")
	}
	if esConfig == nil {
		return nil, fmt.Errorf("elasticsearch config is nil, please check your configuration file for 'doc_engine.es' settings")
	}

	// 创建 go-elasticsearch 客户端
	client, err := elasticsearch.NewClient(elasticsearch.Config{
		Addresses: []string{esConfig.Hosts},
		Username:  esConfig.Username,
		Password:  esConfig.Password,
		Transport: &http.Transport{
			MaxIdleConnsPerHost:   10,
			ResponseHeaderTimeout: 30 * time.Second,
		},
	})
	if err != nil {
		return nil, fmt.Errorf("failed to create Elasticsearch client: %w", err)
	}

	// Ping 验证连接
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	req := esapi.InfoRequest{}
	res, err := req.Do(ctx, client)
	if err != nil {
		return nil, fmt.Errorf("failed to ping Elasticsearch: %w", err)
	}
	defer res.Body.Close()

	if res.IsError() {
		return nil, fmt.Errorf("Elasticsearch returned error: %s", res.Status())
	}

	engine := &elasticsearchEngine{
		client: client,
		config: esConfig,
	}

	// 注册 ragflow_* 分块索引模板（priority 1）
	if err = engine.CreateIndexTemplate(context.Background(), "ragflow_mapping", "ragflow_*", "mapping.json", 1); err != nil {
		return nil, fmt.Errorf("failed to create chunk index template: %w", err)
	}
	// 注册 ragflow_doc_meta_* 元数据模板（priority 2）
	if err = engine.CreateIndexTemplate(context.Background(), "ragflow_doc_meta_mapping", "ragflow_doc_meta_*", "doc_meta_es_mapping.json", 2); err != nil {
		return nil, fmt.Errorf("failed to create doc_meta index template: %w", err)
	}

	return engine, nil
}

// GetType 返回引擎类型标识 elasticsearch
func (e *elasticsearchEngine) GetType() string {
	return "elasticsearch"
}

// Ping 健康检查
func (e *elasticsearchEngine) Ping(ctx context.Context) error {
	req := esapi.InfoRequest{}
	res, err := req.Do(ctx, e.client)
	if err != nil {
		return err
	}
	defer res.Body.Close()
	if res.IsError() {
		return fmt.Errorf("elasticsearch ping failed: %s", res.Status())
	}
	return nil
}

// Close 关闭连接（Go 客户端由 Transport 管理）
func (e *elasticsearchEngine) Close() error {
	// Go-elasticsearch client doesn't have a Close method, connection is managed by the transport
	return nil
}

// CreateIndexTemplate 创建索引模板，匹配 index_patterns 的新索引自动应用 mapping。
func (e *elasticsearchEngine) CreateIndexTemplate(ctx context.Context, templateName, indexPattern, mappingFileName string, priority ...int) error {
	if templateName == "" || indexPattern == "" {
		return fmt.Errorf("template name and index pattern cannot be empty")
	}

	p := 1
	if len(priority) > 0 {
		p = priority[0]
	}

	if mappingFileName == "" {
		mappingFileName = "mapping.json"
	}

	mappingPath, err := utility.FindConfFileInProject(mappingFileName)
	if err != nil {
		return err
	}

	// 从 conf 读取 mapping JSON
	data, err := os.ReadFile(*mappingPath)
	if err != nil {
		return fmt.Errorf("failed to read mapping file %q: %w", *mappingPath, err)
	}

	var mapping map[string]interface{}
	if err = json.Unmarshal(data, &mapping); err != nil {
		return fmt.Errorf("failed to parse mapping file %q: %w", *mappingPath, err)
	}

	// 拆分 settings 与 mappings 写入 template
	templateSettings := mapping["settings"]
	templateMappings := mapping["mappings"]

	// 组装 put_index_template 请求体
	templateBody := map[string]interface{}{
		"index_patterns": []string{indexPattern},
		"priority":       p, // Configurable priority to override existing templates
		"template": map[string]interface{}{
			"settings": templateSettings,
			"mappings": templateMappings,
		},
	}

	templateBytes, err := json.Marshal(templateBody)
	if err != nil {
		return fmt.Errorf("failed to marshal template: %w", err)
	}

	// 创建或更新模板
	req := esapi.IndicesPutIndexTemplateRequest{
		Name: templateName,
		Body: bytes.NewReader(templateBytes),
	}

	res, err := req.Do(ctx, e.client)
	if err != nil {
		return fmt.Errorf("failed to create index template: %w", err)
	}
	defer res.Body.Close()

	if res.IsError() {
		bodyBytes, _ := io.ReadAll(res.Body)
		return fmt.Errorf("failed to create index template: %s, body: %s", res.Status(), string(bodyBytes))
	}

	var result map[string]interface{}
	if err = json.NewDecoder(res.Body).Decode(&result); err != nil {
		return fmt.Errorf("failed to parse response: %w", err)
	}

	if acknowledged, ok := result["acknowledged"].(bool); !ok || !acknowledged {
		return fmt.Errorf("index template creation not acknowledged")
	}

	return nil
}

// GetClusterStats 获取集群统计并格式化为可读字段（索引数、文档数、节点/JVM 等）。 -H "kbn-xsrf: reporting"
func (e *elasticsearchEngine) GetClusterStats() (map[string]interface{}, error) {
	req := esapi.ClusterStatsRequest{}
	res, err := req.Do(context.Background(), e.client)
	if err != nil {
		return nil, fmt.Errorf("failed to get cluster stats: %w", err)
	}
	defer res.Body.Close()

	if res.IsError() {
		return nil, fmt.Errorf("elasticsearch cluster stats returned error: %s", res.Status())
	}

	var rawStats map[string]interface{}
	if err = json.NewDecoder(res.Body).Decode(&rawStats); err != nil {
		return nil, fmt.Errorf("failed to decode cluster stats: %w", err)
	}

	result := make(map[string]interface{})

	// 集群名与 status
	if clusterName, ok := rawStats["cluster_name"].(string); ok {
		result["cluster_name"] = clusterName
	}
	if status, ok := rawStats["status"].(string); ok {
		result["status"] = status
	}

	// 索引/分片/文档/store 统计
	if indices, ok := rawStats["indices"].(map[string]interface{}); ok {
		if count, ok := indices["count"].(float64); ok {
			result["indices"] = int(count)
		}
		if shards, ok := indices["shards"].(map[string]interface{}); ok {
			if total, ok := shards["total"].(float64); ok {
				result["indices_shards"] = int(total)
			}
		}
		if docs, ok := indices["docs"].(map[string]interface{}); ok {
			if docCount, ok := docs["count"].(float64); ok {
				result["docs"] = int64(docCount)
			}
			if deleted, ok := docs["deleted"].(float64); ok {
				result["docs_deleted"] = int64(deleted)
			}
		}
		if store, ok := indices["store"].(map[string]interface{}); ok {
			if sizeInBytes, ok := store["size_in_bytes"].(float64); ok {
				result["store_size"] = convertBytes(int64(sizeInBytes))
			}
			if totalDataSetSize, ok := store["total_data_set_size_in_bytes"].(float64); ok {
				result["total_dataset_size"] = convertBytes(int64(totalDataSetSize))
			}
		}
		if mappings, ok := indices["mappings"].(map[string]interface{}); ok {
			if fieldCount, ok := mappings["total_field_count"].(float64); ok {
				result["mappings_fields"] = int(fieldCount)
			}
			if dedupFieldCount, ok := mappings["total_deduplicated_field_count"].(float64); ok {
				result["mappings_deduplicated_fields"] = int(dedupFieldCount)
			}
			if dedupSize, ok := mappings["total_deduplicated_mapping_size_in_bytes"].(float64); ok {
				result["mappings_deduplicated_size"] = convertBytes(int64(dedupSize))
			}
		}
	}

	// 节点数、OS/JVM 内存等
	if nodes, ok := rawStats["nodes"].(map[string]interface{}); ok {
		if count, ok := nodes["count"].(map[string]interface{}); ok {
			if total, ok := count["total"].(float64); ok {
				result["nodes"] = int(total)
			}
		}
		if versions, ok := nodes["versions"].([]interface{}); ok {
			result["nodes_version"] = versions
		}
		if operatingSystem, ok := nodes["os"].(map[string]interface{}); ok {
			if mem, ok := operatingSystem["mem"].(map[string]interface{}); ok {
				if totalInBytes, ok := mem["total_in_bytes"].(float64); ok {
					result["os_mem"] = convertBytes(int64(totalInBytes))
				}
				if usedInBytes, ok := mem["used_in_bytes"].(float64); ok {
					result["os_mem_used"] = convertBytes(int64(usedInBytes))
				}
				if usedPercent, ok := mem["used_percent"].(float64); ok {
					result["os_mem_used_percent"] = usedPercent
				}
			}
		}
		if jvm, ok := nodes["jvm"].(map[string]interface{}); ok {
			if versions, ok := jvm["versions"].([]interface{}); ok && len(versions) > 0 {
				if version0, ok := versions[0].(map[string]interface{}); ok {
					if vmVersion, ok := version0["vm_version"].(string); ok {
						result["jvm_versions"] = vmVersion
					}
				}
			}
			if mem, ok := jvm["mem"].(map[string]interface{}); ok {
				if heapUsed, ok := mem["heap_used_in_bytes"].(float64); ok {
					result["jvm_heap_used"] = convertBytes(int64(heapUsed))
				}
				if heapMax, ok := mem["heap_max_in_bytes"].(float64); ok {
					result["jvm_heap_max"] = convertBytes(int64(heapMax))
				}
			}
		}
	}

	return result, nil
}

// convertBytes 字节数转 kb/mb/gb 可读字符串
func convertBytes(bytes int64) string {
	const (
		KB = 1024
		MB = 1024 * KB
		GB = 1024 * MB
		TB = 1024 * GB
		PB = 1024 * TB
	)

	if bytes >= PB {
		return fmt.Sprintf("%.2f pb", float64(bytes)/float64(PB))
	}
	if bytes >= TB {
		return fmt.Sprintf("%.2f tb", float64(bytes)/float64(TB))
	}
	if bytes >= GB {
		return fmt.Sprintf("%.2f gb", float64(bytes)/float64(GB))
	}
	if bytes >= MB {
		return fmt.Sprintf("%.2f mb", float64(bytes)/float64(MB))
	}
	if bytes >= KB {
		return fmt.Sprintf("%.2f kb", float64(bytes)/float64(KB))
	}
	return fmt.Sprintf("%d b", bytes)
}

// extractErrorReason 从 ES 错误 JSON 提取 root_cause 或 reason 文本。
func extractErrorReason(bodyBytes []byte) string {
	var errResp map[string]interface{}
	if err := json.Unmarshal(bodyBytes, &errResp); err != nil {
		return ""
	}

	// Try to get error from root_cause
	if errorObj, ok := errResp["error"].(map[string]interface{}); ok {
		if rootCauses, ok := errorObj["root_cause"].([]interface{}); ok && len(rootCauses) > 0 {
			if rootCause, ok := rootCauses[0].(map[string]interface{}); ok {
				if reason, ok := rootCause["reason"].(string); ok && reason != "" {
					return reason
				}
			}
		}
		// Fallback to main error reason
		if reason, ok := errorObj["reason"].(string); ok && reason != "" {
			return reason
		}
		// Try failed_shards
		if failedShards, ok := errorObj["failed_shards"].([]interface{}); ok && len(failedShards) > 0 {
			if shard, ok := failedShards[0].(map[string]interface{}); ok {
				if reason, ok := shard["reason"].(map[string]interface{}); ok {
					if r, ok := reason["reason"].(string); ok && r != "" {
						return r
					}
				}
			}
		}
	}

	return ""
}

// GetIndexStats 通过 _cat/indices 返回指定索引的健康、文档数、store 大小等。
func (e *elasticsearchEngine) GetIndexStats(indices []string) ([]map[string]interface{}, error) {
	if len(indices) == 0 {
		return []map[string]interface{}{}, nil
	}

	req := esapi.CatIndicesRequest{
		Index:  indices,
		Format: "json",
		H:      []string{"index", "health", "status", "docs.count", "store.size", "dataset.size"},
	}

	res, err := req.Do(context.Background(), e.client)
	if err != nil {
		return nil, fmt.Errorf("failed to get index stats: %w", err)
	}
	defer res.Body.Close()

	if res.IsError() {
		if res.StatusCode == 404 {
			return []map[string]interface{}{}, nil
		}
		bodyBytes, _ := io.ReadAll(res.Body)
		return nil, fmt.Errorf("elasticsearch cat indices error: %s, body: %s", res.Status(), string(bodyBytes))
	}

	var results []map[string]interface{}
	if err := json.NewDecoder(res.Body).Decode(&results); err != nil {
		return nil, fmt.Errorf("failed to decode index stats: %w", err)
	}

	return results, nil
}
