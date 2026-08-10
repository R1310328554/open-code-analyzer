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

// common.go — Infinity 表/列辅助：删表、表存在检查、mapping 有序解析、filter 构建与表名规则。
//

package infinity

import (
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"ragflow/internal/common"
	"strings"

	infinity "github.com/infiniflow/infinity-go-sdk"

	"go.uber.org/zap"
)

// dropTable 删除 Infinity 表（须已存在）
func (e *infinityEngine) dropTable(ctx context.Context, tableName string) error {
	if tableName == "" {
		return fmt.Errorf("table name cannot be empty")
	}

	// Check if table exists
	exists, err := e.tableExists(ctx, tableName)
	if err != nil {
		return fmt.Errorf("failed to check table existence: %w", err)
	}
	if !exists {
		return fmt.Errorf("table '%s' does not exist", tableName)
	}

	db, err := e.client.conn.GetDatabase(e.client.dbName)
	if err != nil {
		return fmt.Errorf("failed to get database: %w", err)
	}

	_, err = db.DropTable(tableName, infinity.ConflictTypeError)
	if err != nil {
		return fmt.Errorf("failed to drop table: %w", err)
	}

	common.Info("Infinity dropped table", zap.String("tableName", tableName))
	return nil
}

// tableExists 通过 GetTable 判断是否存在的表
func (e *infinityEngine) tableExists(ctx context.Context, tableName string) (bool, error) {
	if tableName == "" {
		return false, fmt.Errorf("table name cannot be empty")
	}

	db, err := e.client.conn.GetDatabase(e.client.dbName)
	if err != nil {
		return false, fmt.Errorf("failed to get database: %w", err)
	}

	// Try to get the table - if it exists, no error
	_, err = db.GetTable(tableName)
	if err != nil {
		errMsg := strings.ToLower(err.Error())
		if strings.Contains(errMsg, "not found") || strings.Contains(errMsg, "doesn't exist") {
			return false, nil
		}
		return false, fmt.Errorf("failed to check table existence: %w", err)
	}
	return true, nil
}

// fieldInfo mapping JSON 中单字段的类型、默认值、分词器与索引类型。
type fieldInfo struct {
	Type      string      `json:"type"`
	Default   interface{} `json:"default"`
	Analyzer  interface{} `json:"analyzer"`   // string or []string
	IndexType interface{} `json:"index_type"` // string or map
	Comment   string      `json:"comment"`
}

// orderedFields 保序解析 mapping，建表时列顺序与 JSON 一致。
type orderedFields struct {
	Keys   []string
	Fields map[string]fieldInfo
}

func (o *orderedFields) UnmarshalJSON(data []byte) error {
	// Parse JSON manually to preserve key order
	// Look for key names by scanning the JSON string
	// This is a simple approach: find {"key": value, "key2": value2...}
	o.Fields = make(map[string]fieldInfo)
	o.Keys = make([]string, 0)

	// Use a streaming JSON parser approach
	dec := json.NewDecoder(bytes.NewReader(data))
	tok, err := dec.Token()
	if err != nil {
		return err
	}
	if delim, ok := tok.(json.Delim); ok && delim == '{' {
		for dec.More() {
			// Read key
			tok, err := dec.Token()
			if err != nil {
				return err
			}
			key, ok := tok.(string)
			if !ok {
				continue
			}
			o.Keys = append(o.Keys, key)

			// Read value into fieldInfo
			var field fieldInfo
			if err := dec.Decode(&field); err != nil {
				return err
			}
			o.Fields[key] = field
		}
	}
	return nil
}

// fieldKeyword 判断是否为需 filter_fulltext 的 *_kwd 类字段
func fieldKeyword(fieldName string) bool {
	if fieldName == "source_id" {
		return true
	}
	if strings.HasSuffix(fieldName, "_kwd") &&
		fieldName != "knowledge_graph_kwd" &&
		fieldName != "docnm_kwd" &&
		fieldName != "important_kwd" &&
		fieldName != "question_kwd" {
		return true
	}
	return false
}

// existsCondition 按列类型生成非空判断（char 列用 !=''）
func existsCondition(field string, tableColumns map[string]struct {
	Type    string
	Default interface{}
}) string {
	col, colOk := tableColumns[field]
	if !colOk {
		common.Warn(fmt.Sprintf("Column '%s' not found in table columns", field))
		return fmt.Sprintf("%s!=null", field)
	}
	if strings.Contains(strings.ToLower(col.Type), "char") {
		if col.Default != nil {
			return fmt.Sprintf(" %s!='%v' ", field, col.Default)
		}
		return fmt.Sprintf(" %s!='' ", field)
	}
	if col.Default != nil {
		return fmt.Sprintf("%s!=%v", field, col.Default)
	}
	return fmt.Sprintf("%s!=null", field)
}

// buildFilterFromCondition 将 condition map 转为 Infinity filter SQL（含 must_not、IN、exists）。
func buildFilterFromCondition(condition map[string]interface{}, tableColumns map[string]struct {
	Type    string
	Default interface{}
}) string {
	var conditions []string

	for k, v := range condition {
		if v == nil {
			continue
		}
		if strVal, ok := v.(string); ok && strVal == "" {
			continue
		}

		// must_not.exists 转为 NOT (existsCondition)
		if k == "must_not" {
			if mustNotMap, ok := v.(map[string]interface{}); ok {
				for kk, vv := range mustNotMap {
					if kk == "exists" {
						if existsField, ok := vv.(string); ok {
							conditions = append(conditions, fmt.Sprintf("NOT (%s)", existsCondition(existsField, tableColumns)))
						}
					}
				}
			}
			continue
		}

		// 关键词字段用 filter_fulltext 与 convertMatchingField
		if fieldKeyword(k) {
			var orConds []string
			addFullText := func(item string) {
				item = strings.ReplaceAll(item, "'", "''")
				orConds = append(orConds, fmt.Sprintf("filter_fulltext('%s', '%s')", convertMatchingField(k), item))
			}

			switch val := v.(type) {
			case []string:
				for _, item := range val {
					addFullText(item)
				}
			case []interface{}:
				for _, item := range val {
					addFullText(fmt.Sprintf("%v", item))
				}
			case string:
				addFullText(val)
			default:
				addFullText(fmt.Sprintf("%v", val))
			}

			if len(orConds) > 0 {
				conditions = append(conditions, "("+strings.Join(orConds, " OR ")+")")
			}
			continue
		}

		// Handle list values (IN condition)
		if listVal, ok := v.([]interface{}); ok {
			var inVals []string
			for _, item := range listVal {
				if strItem, ok := item.(string); ok {
					strItem = strings.ReplaceAll(strItem, "'", "''")
					inVals = append(inVals, fmt.Sprintf("'%s'", strItem))
				} else {
					inVals = append(inVals, fmt.Sprintf("%v", item))
				}
			}
			if len(inVals) > 0 {
				conditions = append(conditions, fmt.Sprintf("%s IN (%s)", k, strings.Join(inVals, ", ")))
			}
			continue
		}
		if strListVal, ok := v.([]string); ok {
			var inVals []string
			for _, item := range strListVal {
				item = strings.ReplaceAll(item, "'", "''")
				inVals = append(inVals, fmt.Sprintf("'%s'", item))
			}
			if len(inVals) > 0 {
				conditions = append(conditions, fmt.Sprintf("%s IN (%s)", k, strings.Join(inVals, ", ")))
			}
			continue
		}

		// Handle exists condition
		if k == "exists" {
			if existsField, ok := v.(string); ok {
				conditions = append(conditions, existsCondition(existsField, tableColumns))
			}
			continue
		}

		// Handle string values
		if strVal, ok := v.(string); ok {
			strVal = strings.ReplaceAll(strVal, "'", "''")
			conditions = append(conditions, fmt.Sprintf("%s='%s'", k, strVal))
			continue
		}

		// Handle other values
		conditions = append(conditions, fmt.Sprintf("%s=%v", k, v))
	}

	if len(conditions) == 0 {
		return "1=1"
	}
	return strings.Join(conditions, " AND ")
}

// columnExists 检查表中是否存在指定列
func (e *infinityEngine) columnExists(table *infinity.Table, columnName string) (bool, error) {
	colsResp, err := table.ShowColumns()
	if err != nil {
		return false, err
	}

	result, ok := colsResp.(*infinity.QueryResult)
	if !ok {
		return false, fmt.Errorf("unexpected response type: %T", colsResp)
	}

	// ShowColumns returns a result set where Data contains arrays of column values
	if nameArr, ok := result.Data["name"]; ok {
		for i := 0; i < len(nameArr); i++ {
			colName, _ := nameArr[i].(string)
			if colName == columnName {
				return true, nil
			}
		}
	}
	return false, nil
}

// buildChunkTableName skill 索引仅 baseName，普通表为 {baseName}_{datasetID}。
// Skill Table: table name is just baseName (e.g., "skill_abc123_def456")
// Regular chunk Table: table name is {baseName}_{datasetID}
func buildChunkTableName(baseName, datasetID string) string {
	if datasetID == "skill" {
		return baseName
	}
	return fmt.Sprintf("%s_%s", baseName, datasetID)
}

// buildMetadataTableName 租户元数据表 ragflow_doc_meta_{tenantID}。
func buildMetadataTableName(tenantID string) string {
	return fmt.Sprintf("ragflow_doc_meta_%s", tenantID)
}
