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
// types.go — 技能源共享类型：HTTP 客户端接口、SKILL.md 元数据与下载后的 SkillBundle 结构。

//

package source

import "net/http"

// HTTPClientInterface defines the interface for HTTP operations
// This is duplicated here to avoid circular imports
// HTTPClientInterface 定义技能源所需的 HTTP Do/Get 能力（避免循环引用）。
type HTTPClientInterface interface {
	Do(req *http.Request) (*http.Response, error)
	Get(url string) (*http.Response, error)
}

// SkillMetadata represents the metadata from SKILL.md frontmatter
// This is duplicated here to avoid circular imports
// SkillMetadata 对应 SKILL.md YAML 前置区中的 name/description 等字段。
type SkillMetadata struct {
	Name        string      `yaml:"name"`
	Description string      `yaml:"description"`
	Version     string      `yaml:"version"`
	Author      string      `yaml:"author"`
	Tags        []string    `yaml:"tags"`
	Tools       interface{} `yaml:"tools"`
}

// SkillBundle represents a downloaded skill package
// SkillBundle 表示从任意源下载的完整技能包及其信任信息。
type SkillBundle struct {
	Name       string
	Files      map[string][]byte
	Source     string
	Identifier string
	TrustLevel string
	Metadata   *SkillMetadata
}
