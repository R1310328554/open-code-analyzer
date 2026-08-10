// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// PromQL 求值结果类型定义：Value 接口与 instant/range vector、scalar、string 等 ValueType 常量及文档用语映射。

package parser

// Value 为查询求值结果的通用接口，提供类型标识与字符串化。
// Value is a generic interface for values resulting from a query evaluation.
type Value interface {
	Type() ValueType
	String() string
}

// ValueType 枚举 PromQL 表达式静态/动态值类别。
// ValueType describes a type of a value.
type ValueType string

// The valid value types.
const (
	ValueTypeNone   ValueType = "none"
	ValueTypeVector ValueType = "vector"
	ValueTypeScalar ValueType = "scalar"
	ValueTypeMatrix ValueType = "matrix"
	ValueTypeString ValueType = "string"
)

// DocumentedType 将内部 ValueType 映射为用户文档中的术语（如 instant vector）。
// DocumentedType returns the internal type to the equivalent
// user facing terminology as defined in the documentation.
func DocumentedType(t ValueType) string {
	switch t {
	case ValueTypeVector:
		return "instant vector"
	case ValueTypeMatrix:
		return "range vector"
	default:
		return string(t)
	}
}
