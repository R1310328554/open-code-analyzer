//
// Copyright 2026 The InfiniFlow Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package chunk

// chenk_type.go — 分块流水线核心类型：Operator 接口、ChunkData 与 ChunkContext。


// Operator 定义分块流水线各阶段的统一接口（Prepare/Execute/Finish）。
type Operator interface {
	// Prepare 从 DSL 阶段配置映射初始化算子状态。
	Prepare(ctx *ChunkContext) error
	// Execute 在共享 ChunkContext 上执行本阶段变换。
	Execute(ctx *ChunkContext) error
	// Finish 执行收尾清理（当前各算子多为空实现）。
	Finish(ctx *ChunkContext) error

	String() string
}

// ChunkData 表示流水线产出的单个文本块（含 content/size/index/metadata）。
type ChunkData struct {
	Content  string                 `json:"content"`
	Size     int                    `json:"size"`
	Index    int                    `json:"index,omitempty"`
	Metadata map[string]interface{} `json:"metadata,omitempty"`
}

func (c *ChunkData) GetContent() string {
	if c == nil {
		return ""
	}
	return c.Content
}

// ChunkContext 在流水线各阶段间传递原文、预处理后文本与中间/最终 chunk。
type ChunkContext struct {
	Origin string // 原始输入文本

	TextAfterPreprocess string // 预处理算子输出文本

	SplitChunks []ChunkData // 切分算子产出的 chunk 列表

	ResultChunks []ChunkData // 后处理后的最终（或中间）chunk 列表
}

// 文件名保留历史拼写 chenk_type；ChunkContext 为 preprocess→split→postprocess 三阶段共享载体。
