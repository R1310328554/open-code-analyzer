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
// system.go — 系统设置格式化与校验：将 entity.SystemSettings 转为 API 响应，并按数据类型验证配置值。

//

package common

import (
	"encoding/json"
	"fmt"
	"ragflow/internal/entity"
	"strconv"
	"strings"
)

// FormatSystemSetting 将单条系统设置转为前端可消费的 map 结构。
func FormatSystemSetting(setting entity.SystemSettings) map[string]interface{} {
	return map[string]interface{}{
		"data_type":    setting.DataType,
		"name":         setting.Name,
		"setting_type": "config",
		"value":        setting.Value,
	}
}

// FormatSystemSettings 批量格式化系统设置列表。
func FormatSystemSettings(settings []entity.SystemSettings) []map[string]interface{} {
	result := make([]map[string]interface{}, 0, len(settings))
	for _, setting := range settings {
		result = append(result, FormatSystemSetting(setting))
	}
	return result
}

// ValidateSystemSettingValue 按 setting.DataType 校验新值：支持 string/int/bool/json 等类型。
func ValidateSystemSettingValue(setting entity.SystemSettings, value string) error {
	dataType := strings.ToLower(setting.DataType)
	switch dataType {
	case "string": // 字符串类型直接通过

		return nil
	case "integer", "int": // 整数类型需可解析为 int

		if _, err := strconv.Atoi(value); err != nil {
			return fmt.Errorf("invalid integer value for %s: %s", setting.Name, value)
		}
	case "bool", "boolean": // 布尔类型仅接受 true/false

		if value != "true" && value != "false" {
			return fmt.Errorf("invalid bool value for %s: expected true or false", setting.Name)
		}
	case "json": // JSON 类型需通过 json.Valid 校验

		if !json.Valid([]byte(value)) {
			return fmt.Errorf("invalid JSON value for %s", setting.Name)
		}
	default:
		return fmt.Errorf("unsupported data type for %s: %s", setting.Name, setting.DataType)
	}
	return nil
}

// InferSystemSettingDataType 根据配置项名称推断数据类型：sandbox.* 为 json，*.enabled 为 bool，其余默认 string。
func InferSystemSettingDataType(name string) string {
	if strings.HasPrefix(name, "sandbox.") {
		return "json"
	}
	if strings.HasSuffix(name, ".enabled") {
		return "bool"
	}
	return "string"
}
