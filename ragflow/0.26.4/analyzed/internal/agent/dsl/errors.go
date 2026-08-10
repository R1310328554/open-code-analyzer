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
// errors.go — DSL 提取器哨兵错误，handler 用 errors.Is 映射为 102 信封。

//

package dsl

import "errors"

// 提取器返回的哨兵错误；handler 用 errors.Is 映射为 102 DataError，不暴露原始文本。
// Sentinel errors returned by the extractors. Handlers use
// errors.Is to map them to 102 (DataError) envelopes without
// embedding the raw error text in the response.
var (
	// ErrComponentNotFound 当 componentID 不在 dsl["components"] 中时返回。
	// ErrComponentNotFound is returned when the supplied
	// componentID does not exist in dsl["components"].
	ErrComponentNotFound = errors.New("dsl: component not found")

	// ErrMissingInputForm 组件存在但无 obj.input_form 时返回。
	// ErrMissingInputForm is returned when the component exists
	// but has no `obj.input_form` dict. The python Canvas returns
	// None in this case; we surface 102 "component has no
	// input_form" instead.
	ErrMissingInputForm = errors.New("dsl: component has no input_form")

	// ErrMalformedDSL DSL 结构异常（nil dsl、缺 components、类型错误）时返回。
	// ErrMalformedDSL is returned for structural problems — nil
	// dsl, missing components map, wrong types. Distinct from
	// ErrComponentNotFound so the handler can phrase the error
	// more clearly when the dsl is broken.
	ErrMalformedDSL = errors.New("dsl: malformed")
)
