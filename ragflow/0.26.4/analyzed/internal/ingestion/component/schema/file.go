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

// Package schema 定义 ingestion 流水线组件间流转的 *FromUpstream / *Param / *Outputs wire 类型。
// 各文件镜像 rag/flow/<component>/schema.py 的 Pydantic schema（或 _invoke 运行时契约）。
//
// 本包为纯数据定义，仅依赖 stdlib；业务逻辑在 internal/ingestion/component/。
package schema

// FileFromUpstream 是 File 组件消费的上游载荷，镜像 rag/flow/file.py:File._invoke。
// Python 无专用 Pydantic schema，字段从运行时 kwargs 推导；doc_id 来自 canvas 而非 kwargs。
type FileFromUpstream struct {
	// CreatedTime 上游组件墙上时钟起始时间（秒）。
	CreatedTime *float64 `json:"_created_time,omitempty"`
	// ElapsedTime 上游组件已耗时间（秒）。
	ElapsedTime *float64 `json:"_elapsed_time,omitempty"`

	// DocID is the canvas-bound document ID. When non-empty the File
	// component resolves the binary via the document service; otherwise
	// the File payload below is used. Optional in wire terms — the
	// Python code branches on truthiness, so we use *string.
	DocID *string `json:"doc_id,omitempty"`

	// File is the optional list of file descriptors passed when no
	// doc_id is bound. In Python: `file = kwargs.get("file")[0]`.
	// Shape: `[]map[string]any` — same as rag/flow/parser's `file` field.
	File []map[string]any `json:"file,omitempty"`
}

// Validate 要求 DocID 与 File 至少其一非空，对应 Python 两条上游路径之一已接入。
func (f *FileFromUpstream) Validate() error {
	if (f.DocID == nil || *f.DocID == "") && len(f.File) == 0 {
		return errRequiredField{Field: "doc_id|file"}
	}
	return nil
}

// FileParam 是 File 组件静态配置；Python FileParam 无额外字段，Go 结构体 intentionally 为空以预留扩展。
type FileParam struct{}

// Defaults 返回 Python 默认值（当前无字段，满足包内 Defaults() 约定）。
func (FileParam) Defaults() FileParam { return FileParam{} }

// Validate 返回 nil；FileParam 无必填字段。
func (FileParam) Validate() error { return nil }

// FileOutputs 是 File 组件调用结果；二进制 blob 不在 wire schema 中，由存储层按路径引用。
type FileOutputs struct {
	// Name 为解析后的文档/文件名。
	Name string `json:"name"`
	// File is the upstream file descriptor (dict in Python). Optional —
	// when invoked via doc_id the Python code does not re-emit it.
	File map[string]any `json:"file,omitempty"`
	// Error is set when the component short-circuits with an error
	// message (Python: set_output("_ERROR", ...)).
	Error string `json:"_ERROR,omitempty"`
}
// schema/file.go — File 组件上下游 wire 类型。
