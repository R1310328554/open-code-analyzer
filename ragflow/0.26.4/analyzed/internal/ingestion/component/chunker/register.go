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

// Package chunker 定义 ingestion 分块组件：TokenChunker、
// TitleChunker、GroupTitleChunker、HierarchyTitleChunker。四种组件共享
// 相同上游载荷（schema.ChunkerFromUpstream）与输出形状（schema.ChunkerOutputs）。
// and the same output shape (schema.ChunkerOutputs).
//
// The package is intentionally separate from internal/agent/component/
// (the agent canvas) and from internal/ingestion/component/schema/
// (the wire types). Wiring it as a separate package keeps the
// registry tidy.
package chunker

import (
	"ragflow/internal/agent/runtime"
)

// MustRegisterChunker 在 CategoryIngestion 下注册单个分块组件；
// CategoryIngestion. The four chunker files each carry exactly one
// init() that calls this with the registered component's name; the
// factory body resolves the typed constructor via newChunkerByName
// (in common.go).
// One helper call per file keeps the registration surface flat.
func MustRegisterChunker(name string) {
	factory := func(_ string, params map[string]any) (runtime.Component, error) {
		comp, err := newChunkerByName(name, params)
		if err != nil {
			return nil, err
		}
		// newChunkerByName returns runtime.Component directly (each
		// NewXxxChunker constructor satisfies the interface, so no
		// intermediate type assertion is needed).
		return comp, nil
	}
	runtime.MustRegister(name, runtime.CategoryIngestion, factory, runtime.Metadata{
		Version: "1.0.0",
		Inputs:  ChunkerInputs,
		Outputs: ChunkerOutputs,
	})
}

// ChunkerInputs 为四种变体共享的静态注册输入描述符。
// by all four chunker variants.
var ChunkerInputs = map[string]string{
	"text":          "Plain-text input. The chunker slices this into downstream chunks.",
	"content":       "Alias for \"text\".",
	"chunks":        "Optional upstream chunk list (structured JSON form).",
	"name":          "Source document name. Required by the upstream payload convention.",
	"_created_time": "Optional upstream timestamp (RFC3339Nano, s).",
	"_elapsed_time": "Optional upstream elapsed time (s).",
}

// ChunkerOutputs 为四种变体共享的静态注册输出描述符。
// by all four chunker variants.
var ChunkerOutputs = map[string]string{
	"output_format": "Always \"chunks\" on success.",
	"chunks":        "list[object]: per-chunk map (text + optional meta keys).",
	"_ERROR":        "Set only on validation failure.",
}

// 各 chunker 文件 init() 调用 MustRegisterChunker，工厂经 newChunkerByName 解析具体构造器。
