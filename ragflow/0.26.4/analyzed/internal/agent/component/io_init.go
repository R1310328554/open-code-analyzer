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
// io_init.go — 通过 blank import 触发 io 子包 init，确保 DocsGenerator 等 T5 重 I/O 组件注册进父包 registry。

//

// Package component — io subpackage wiring.
//
// The T5 heavy-I/O components (DocsGenerator, plus the writers used
// internally) live in the io/ subpackage. Their init() functions
// register themselves with the parent component registry. This file
// exists to make sure the io/ subpackage is linked into the parent
// component package via a blank import, so that the registrations
// actually run on package init.
//
// Without this blank import, the io/ subpackage's init() functions
// would be dead-code-eliminated and the DocsGenerator factory would
// not be visible to the canvas engine.
package component

// Blank import — runs init() in the io subpackage which calls
// component.Register("DocsGenerator", …).
// blank import 运行 io 子包 init，执行 Register("DocsGenerator", …)。
import _ "ragflow/internal/agent/component/io"
