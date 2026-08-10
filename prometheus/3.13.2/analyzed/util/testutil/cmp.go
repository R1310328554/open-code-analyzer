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

// 测试断言辅助：基于 go-cmp 的 RequireEqual，对 labels 等 Prometheus 结构使用定制比较器。

package testutil

import (
	"fmt"
	"testing"

	"github.com/google/go-cmp/cmp"
	"github.com/stretchr/testify/require"

	"github.com/prometheus/prometheus/model/labels"
)

// RequireEqual 以 labels.Equal 等选项替代 testify 的 DeepEqual 做结构比较。
// RequireEqual is a replacement for require.Equal using go-cmp adapted for
// Prometheus data structures, instead of DeepEqual.
func RequireEqual(t testing.TB, expected, actual any, msgAndArgs ...any) {
	t.Helper()
	RequireEqualWithOptions(t, expected, actual, nil, msgAndArgs...)
}

// RequireEqualWithOptions 允许传入额外 cmp.Option 并输出 diff。
// RequireEqualWithOptions works like RequireEqual but allows extra cmp.Options.
func RequireEqualWithOptions(t testing.TB, expected, actual any, extra []cmp.Option, msgAndArgs ...any) {
	t.Helper()
	options := append([]cmp.Option{cmp.Comparer(labels.Equal)}, extra...)
	if cmp.Equal(expected, actual, options...) {
		return
	}
	diff := cmp.Diff(expected, actual, options...)
	require.Fail(t, fmt.Sprintf("Not equal: \n"+
		"expected: %s\n"+
		"actual  : %s%s", expected, actual, diff), msgAndArgs...)
}
