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

// 内部 chunk 执行路径的类型化选项，映射生产调用点的常用配置子集。

package chunk

import (
	"fmt"
)

// ChunkOptions 为生产调用方提供的类型化配置结构体。
// 仅建模当前生产调用点实际使用的选项子集，
// 复杂 DSL 映射仍由各 Operator 的 map 配置路径承担。
type ChunkOptions struct {
	// 预处理标志：三者为 true 的组合均可；全 false 则跳过 preprocess 阶段。
	// false means "no preprocess stage" (the engine skips the stage
	// rather than running an identity preprocess).
	NormalizeNewlines bool
	StripWhitespace   bool
	RemoveEmptyLines  bool

	// 切分配置：strategy 未设时算子内部降级为 sentence。
	// strategy degrades to "sentence" inside the operator.
	SplitStrategy string

	// 后处理配置：零值表示不启用对应步骤（merge/filter）。
	// step"; non-zero values enable it.
	MergeTargetSize int

	// FilterMinLength>0 按 rune 数丢弃过短 chunk。
	FilterMinLength int
}

// validate 校验 MergeTargetSize/FilterMinLength 非负，失败则 Run 提前返回。
// The check is cheap; an option set that fails validation will not
// produce meaningful results at run time.
func (o ChunkOptions) validate() error {
	if o.MergeTargetSize < 0 {
		return fmt.Errorf("chunk: MergeTargetSize must be >= 0 (got %d)", o.MergeTargetSize)
	}
	if o.FilterMinLength < 0 {
		return fmt.Errorf("chunk: FilterMinLength must be >= 0 (got %d)", o.FilterMinLength)
	}
	return nil
}

// ChunkOptions 与 Run 配合，将 typed 配置转为各 Operator 的 map 构造参数。
