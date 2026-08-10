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

// common.go — 模型响应解析公共工具：按模型族归一化名称，从 Qwen3 回复中提取 <think> 思维链与正文。

package models

import "strings"

// GetThinkingAndAnswer 按模型族拆分思维链与最终答案（当前支持 qwen3）
func GetThinkingAndAnswer(modelType *string, content *string) (*string, *string) {
	if content == nil {
		return nil, nil
	}

	switch NormalizeModelFamily(modelType) {
	case "qwen3":
		return extractThinkContent(content)
	}
	return nil, content
}

// NormalizeModelFamily 归一化带厂商前缀的 modelType 字符串，供共享响应解析逻辑使用
func NormalizeModelFamily(modelType *string) string {
	if modelType == nil {
		return ""
	}

	family := strings.ToLower(strings.TrimSpace(*modelType))
	if family == "" {
		return ""
	}

	if slash := strings.LastIndex(family, "/"); slash >= 0 && slash < len(family)-1 {
		family = family[slash+1:]
	}

	if family == "qwen3" || strings.HasPrefix(family, "qwen3-") || strings.HasPrefix(family, "qwen3.") {
		return "qwen3"
	}

	if dash := strings.Index(family, "-"); dash >= 0 {
		family = family[:dash]
	}

	return family
}

// extractThinkContent 从 Qwen3 回复中提取 <think> 块与后续正文
func extractThinkContent(content *string) (*string, *string) {
	if content == nil {
		return nil, nil
	}

	startTag := "<think>"
	endTag := "</think>"

	startIdx := strings.Index(*content, startTag)
	endIdx := strings.Index(*content, endTag)

	if startIdx == -1 || endIdx == -1 || endIdx <= startIdx {
		return nil, content
	}

	thinking := (*content)[startIdx+len(startTag) : endIdx]
	answer := (*content)[endIdx+len(endTag):]

	thinking = strings.TrimLeft(thinking, "\n")
	answer = strings.TrimLeft(answer, "\n")

	return &thinking, &answer
}

// NormalizeModelFamily 去除 vendor/ 前缀并按连字符截断族名；qwen3/qwen3-* 统一映射为 qwen3。extractThinkContent 在标签缺失时返回 nil 思维链与原始 content。
