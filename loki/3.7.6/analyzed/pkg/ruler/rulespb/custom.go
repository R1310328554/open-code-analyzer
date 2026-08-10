package rulespb

// RuleGroupList 是 RuleGroupDesc 切片别名，Formatted 按 namespace 聚合为 rulefmt 映射。

import "github.com/prometheus/prometheus/model/rulefmt"

// RuleGroupList 表示某租户或列表 API 返回的全部规则组描述。
// RuleGroupList contains a set of rule groups
type RuleGroupList []*RuleGroupDesc

// Formatted 将每个 RuleGroupDesc 经 FromProto 归入 namespace 键下的切片。
// Formatted returns the rule group list as a set of formatted rule groups mapped
// by namespace
func (l RuleGroupList) Formatted() map[string][]rulefmt.RuleGroup {
	ruleMap := map[string][]rulefmt.RuleGroup{}
	for _, g := range l {
		if _, exists := ruleMap[g.Namespace]; !exists {
			ruleMap[g.Namespace] = []rulefmt.RuleGroup{FromProto(g)}
			continue
		}
		ruleMap[g.Namespace] = append(ruleMap[g.Namespace], FromProto(g))

	}
	return ruleMap
}
// 同一 namespace 下多组通过 append 合并，供 ruler sync 与 UI 展示。
