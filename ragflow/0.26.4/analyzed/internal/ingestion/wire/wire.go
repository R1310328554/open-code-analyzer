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

// ingestion 组件注册面的唯一所有者。
//
// Plan AD-2 要求所有组件共用 agent canvas 注册表；cmd 入口须 blank-import 组件包以触发 init()。
//
// 本包集中 import 列表；cmd 应 blank-import 并在启动时调用 RegisterComponents（运行时 no-op）。
//
// 位置说明：置于 ingestion/ 而非 pipeline/ 以避免 import 环（pipeline↔chunker↔ingestion）。
package wire

import (
	// Component registration: the blank imports trigger each
	// package's init() which calls runtime.DefaultRegistry.Register.
	_ "ragflow/internal/ingestion/component"         // File / Parser / Tokenizer / Extractor
	_ "ragflow/internal/ingestion/component/chunker" // 4 chunker variants
)

// RegisterComponents 为启动引导入口，保证流水线解析组件名前已全部注册；运行时 no-op。
//
// Usage from a cmd entry:
//
//	import _ "ragflow/internal/ingestion/wire"
//
// The blank import alone is sufficient — the Go toolchain will
// evaluate the blank imports at link time. The function is
// exported for callers that prefer a function-call style over a
// blank import:
//
//	import "ragflow/internal/ingestion/wire"
//	...
//	wire.RegisterComponents()
//
// Both styles are equivalent; the blank-import form is the
// convention in this repo today.
func RegisterComponents() {
	// Intentional no-op. The init() functions in the blank-imported
	// packages above have already registered the components by
	// the time this function is callable.
}
// wire/wire.go — ingestion 组件注册集中入口，避免 cmd 遗漏 blank import。
