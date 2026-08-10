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
// json_types.go — 兼容 Python 端点的 JSON 类型适配：StringSlice 同时接受 JSON 字符串或字符串数组。

//

package common

import "encoding/json"

// StringSlice is a []string that unmarshals from either a JSON string or
// a JSON array of strings.  This matches Python endpoints that accept
// both "kb1" and ["kb1", "kb2"] for list-valued parameters.
// StringSlice 在反序列化时兼容 "kb1" 与 ["kb1","kb2"] 两种形态。
type StringSlice []string

// UnmarshalJSON implements json.Unmarshaler.
// UnmarshalJSON 优先解析数组，失败则回退为单元素切片。
func (s *StringSlice) UnmarshalJSON(data []byte) error {
	// Try array first.
	var arr []string
	if err := json.Unmarshal(data, &arr); err == nil {
		*s = arr
		return nil
	}

	// Fall back to a single string → wrap as one-element slice.
	var single string
	if err := json.Unmarshal(data, &single); err != nil {
		return err
	}
	*s = StringSlice{single}
	return nil
}

// MarshalJSON implements json.Marshaler.
// MarshalJSON 始终序列化为 JSON 字符串数组。
func (s StringSlice) MarshalJSON() ([]byte, error) {
	return json.Marshal([]string(s))
}
