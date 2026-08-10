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

// 标签匹配器：封装 PromQL/selector 中的 =、!=、=~、!~ 四种匹配类型，
// 正则匹配委托 FastRegexMatcher 并暴露前缀/集合优化等元信息。

package labels

import (
	"bytes"
	"strconv"
)

// MatchType 枚举标签匹配运算符：等于、不等、正则、非正则。
// MatchType is an enum for label matching types.
type MatchType int

// Possible MatchTypes.
const (
	MatchEqual MatchType = iota
	MatchNotEqual
	MatchRegexp
	MatchNotRegexp
)

var matchTypeToStr = [...]string{
	MatchEqual:     "=",
	MatchNotEqual:  "!=",
	MatchRegexp:    "=~",
	MatchNotRegexp: "!~",
}

func (m MatchType) String() string {
	if m < MatchEqual || m > MatchNotRegexp {
		panic("unknown match type")
	}
	return matchTypeToStr[m]
}

// Matcher 绑定 Type/Name/Value，正则类型时持有预编译 FastRegexMatcher。
// Matcher models the matching of a label.
type Matcher struct {
	Type  MatchType
	Name  string
	Value string

	re *FastRegexMatcher
}

// NewMatcher 构造 Matcher，正则类型时编译 FastRegexMatcher。
// NewMatcher returns a matcher object.
func NewMatcher(t MatchType, n, v string) (*Matcher, error) {
	m := &Matcher{
		Type:  t,
		Name:  n,
		Value: v,
	}
	if t == MatchRegexp || t == MatchNotRegexp {
		re, err := NewFastRegexMatcher(v)
		if err != nil {
			return nil, err
		}
		m.re = re
	}
	return m, nil
}

// MustNewMatcher 测试专用，出错时 panic。
// MustNewMatcher panics on error - only for use in tests!
func MustNewMatcher(mt MatchType, name, val string) *Matcher {
	m, err := NewMatcher(mt, name, val)
	if err != nil {
		panic(err)
	}
	return m
}

func (m *Matcher) String() string {
	// Start a buffer with a pre-allocated size on stack to cover most needs.
	var bytea [1024]byte
	b := bytes.NewBuffer(bytea[:0])

	if m.shouldQuoteName() {
		b.Write(strconv.AppendQuote(b.AvailableBuffer(), m.Name))
	} else {
		b.WriteString(m.Name)
	}
	b.WriteString(m.Type.String())
	b.Write(strconv.AppendQuote(b.AvailableBuffer(), m.Value))

	return b.String()
}

func (m *Matcher) shouldQuoteName() bool {
	for i, c := range m.Name {
		if c == '_' || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (i > 0 && c >= '0' && c <= '9') {
			continue
		}
		return true
	}
	return m.Name == ""
}

// Matches 按 Type 做相等或正则匹配。
// Matches returns whether the matcher matches the given string value.
func (m *Matcher) Matches(s string) bool {
	switch m.Type {
	case MatchEqual:
		return s == m.Value
	case MatchNotEqual:
		return s != m.Value
	case MatchRegexp:
		return m.re.MatchString(s)
	case MatchNotRegexp:
		return !m.re.MatchString(s)
	}
	panic("labels.Matcher.Matches: invalid match type")
}

// Inverse 返回逻辑取反的 Matcher（=↔!=，=~↔!~）。
// Inverse returns a matcher that matches the opposite.
func (m *Matcher) Inverse() (*Matcher, error) {
	switch m.Type {
	case MatchEqual:
		return NewMatcher(MatchNotEqual, m.Name, m.Value)
	case MatchNotEqual:
		return NewMatcher(MatchEqual, m.Name, m.Value)
	case MatchRegexp:
		return NewMatcher(MatchNotRegexp, m.Name, m.Value)
	case MatchNotRegexp:
		return NewMatcher(MatchRegexp, m.Name, m.Value)
	}
	panic("labels.Matcher.Matches: invalid match type")
}

// GetRegexString returns the regex string.
func (m *Matcher) GetRegexString() string {
	if m.re == nil {
		return ""
	}
	return m.re.GetRegexString()
}

// SetMatches 若正则可展开为有限字面量集合则返回备选值列表。
// SetMatches returns a set of equality matchers for the current regex matchers if possible.
// For examples the regexp `a(b|f)` will returns "ab" and "af".
// Returns nil if we can't replace the regexp by only equality matchers.
func (m *Matcher) SetMatches() []string {
	if m.re == nil {
		return nil
	}
	return m.re.SetMatches()
}

// Prefix 返回正则优化推导的必需前缀（纯等值匹配时为空）。
// Prefix returns the required prefix of the value to match, if possible.
// It will be empty if it's an equality matcher or if the prefix can't be determined.
func (m *Matcher) Prefix() string {
	if m.re == nil {
		return ""
	}
	return m.re.prefix
}

// IsRegexOptimized 指示 FastRegexMatcher 是否启用快速路径优化。
// IsRegexOptimized returns whether regex is optimized.
func (m *Matcher) IsRegexOptimized() bool {
	if m.re == nil {
		return false
	}
	return m.re.IsOptimized()
}
