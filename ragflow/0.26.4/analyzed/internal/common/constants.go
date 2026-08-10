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
// constants.go — 全局共享常量：检索字段名、任务状态与对话有效状态枚举。

//

package common

// 检索与 Elasticsearch 相关字段/窗口常量。
const (
	// PAGERANK_FLD is the field name for pagerank score
	PAGERANK_FLD = "pagerank_fea"
	// TAG_FLD is the field name for tag features
	TAG_FLD = "tag_feas"
	// MAX_RESULT_WINDOW is the maximum result window for ES
	MAX_RESULT_WINDOW = 10000
	// SearchAfterBatchSize caps how many hits one Elasticsearch
	// request can return per search_after iteration.
	SearchAfterBatchSize = 1000
)

// task status — 异步任务生命周期状态字符串。

const (
	CREATED   = "CREATED"
	RUNNING   = "RUNNING"
	COMPLETED = "COMPLETED"
	FAILED    = "FAILED"
	STOPPED   = "STOPPED"
	STOPPING  = "STOPPING"
)

// StatusDialogValid 为对外 Bot 可用的 dialog.status 值（对应 Python StatusEnum.VALID）。
// StatusDialogValid is the dialog.status value that gates public bot
// access. Mirrors Python's StatusEnum.VALID.value at
// api/common/constants.py (the string "1"). All chatbot/agentbot
// authorization paths must use this constant instead of the literal.
const StatusDialogValid = "1"

// DialogStatus is a typed alias for dialog.status to avoid raw string
// comparisons in call sites.
// DialogStatus 为 dialog.status 的类型别名，避免裸字符串比较。
type DialogStatus string
