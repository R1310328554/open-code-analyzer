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

package schema

import "fmt"

// errRequiredField 是 schema Validate() 在必填字段缺失或为空时返回的类型化错误，
// 携带字段名以便调用方构造结构化错误响应。
type errRequiredField struct {
	Field string
}

func (e errRequiredField) Error() string {
	return fmt.Sprintf("schema: required field %q is missing or empty", e.Field)
}

// errInvalidValue 是 schema Validate() 在字段值不在允许集合内时返回的类型化错误，
// 携带字段名与非法值。
type errInvalidValue struct {
	Field string
	Value string
}

func (e errInvalidValue) Error() string {
	return fmt.Sprintf("schema: field %q has invalid value %q", e.Field, e.Value)
}
// schema/errors.go — 组件 schema 校验错误类型定义。
