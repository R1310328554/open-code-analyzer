package util //nolint:revive

// util 包 PrepareLabelsAndMatchers 为 ingester/index gateway 序列量查询准备：解析 targetLabels 与 matchers，返回需聚合的标签集及是否全匹配选择器。

import "github.com/prometheus/prometheus/model/labels"

// PrepareLabelsAndMatchers 可选 tenantLabel 参数以从 matchers 中排除内部租户标签。
// PrepareLabelsAndMatchers is used by the ingester and index gateway to service volume requests.
// It returns a map of labels to aggregate into, a list of matchers to match streams against,
// as well a boolean to indicate if a match all selector was provided.
//
// The last argument, tenantLabel, is optional. If povided, a single string of the internal tenant label namne is expected.
func PrepareLabelsAndMatchers(targetLabels []string, matchers []*labels.Matcher, tenantLabel ...string) (map[string]struct{}, []*labels.Matcher, bool) {
	if len(targetLabels) > 0 {
		return prepareLabelsAndMatchersWithTargets(targetLabels, matchers, tenantLabel...)
	}

	var includeAll bool
	labelsToMatch := make(map[string]struct{})

	for _, m := range matchers {
		if m.Name == "" {
			includeAll = true
			continue
		}

		if len(tenantLabel) == 1 && m.Name == tenantLabel[0] {
			continue
		}

		labelsToMatch[m.Name] = struct{}{}
	}

	return labelsToMatch, matchers, includeAll
}

// prepareLabelsAndMatchersWithTargets 为缺失 target 自动追加 .+ 正则 matcher。
func prepareLabelsAndMatchersWithTargets(targetLabels []string, matchers []*labels.Matcher, tenantLabel ...string) (map[string]struct{}, []*labels.Matcher, bool) {
	matchAllIndex := -1
	labelsToMatch := make(map[string]struct{})
	targetsFound := make(map[string]bool, len(targetLabels))

	for _, target := range targetLabels {
		labelsToMatch[target] = struct{}{}
		targetsFound[target] = false
	}

	for i, m := range matchers {
		if m.Name == "" {
			matchAllIndex = i
			continue
		}

		if len(tenantLabel) == 1 && m.Name == tenantLabel[0] {
			continue
		}

		if _, ok := targetsFound[m.Name]; ok {
			targetsFound[m.Name] = true
		}
	}

	// Make sure all target labels are included in the matchers.
	for target, found := range targetsFound {
		if !found {
			matcher := labels.MustNewMatcher(labels.MatchRegexp, target, ".+")
			matchers = append(matchers, matcher)
		}
	}

	// If target labels has added a matcher, we can remove the all matcher
	if matchAllIndex > -1 && len(matchers) > 1 {
		matchers = append(matchers[:matchAllIndex], matchers[matchAllIndex+1:]...)
	}

	return labelsToMatch, matchers, false
}
// 空名 matcher 表示 match-all 选择器，includeAll 为 true 时需扫描全部 stream。
