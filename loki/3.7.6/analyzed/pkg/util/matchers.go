package util //nolint:revive

// util 包 matchers 工具将 PromQL matcher 拆分为索引可用 matchers 与后置 filters：空串匹配类选择器无法仅靠倒排索引满足，需在拉取 chunk 后再过滤。

import (
	"github.com/prometheus/prometheus/model/labels"
)

// SplitFiltersAndMatchers 分离 Matches("") 的 matcher；regexp .* 全匹配则直接跳过。
// SplitFiltersAndMatchers splits empty matchers off, which are treated as filters, see #220
func SplitFiltersAndMatchers(allMatchers []*labels.Matcher) (filters, matchers []*labels.Matcher) {
	for _, matcher := range allMatchers {
		// If a matcher matches "", we need to fetch possible chunks where
		// there is no value and will therefore not be in our label index.
		// e.g. {foo=""} and {foo!="bar"} both match "", so we need to return
		// chunks which do not have a foo label set. When looking entries in
		// the index, we should ignore this matcher to fetch all possible chunks
		// and then filter on the matcher after the chunks have been fetched.
		if matcher.Matches("") {
			// Always skip matches that match everything
			if matcher.Type == labels.MatchRegexp && matcher.Value == ".*" {
				continue
			}
			filters = append(filters, matcher)
		} else {
			matchers = append(matchers, matcher)
		}
	}
	return
}
// 典型 filter 如 {foo=""} 或 {foo!="bar"}，需在内存侧补全索引未覆盖的空标签 chunk。
