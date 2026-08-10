package validation

// validation 包 BlockedQuery 描述租户级查询拦截规则：pattern/regex/hash/types 与 query_tags 键值约束共同决定是否拒绝。

import "github.com/grafana/dskit/flagext"

type BlockedQuery struct {
	Pattern string                 `yaml:"pattern"`
	Regex   bool                   `yaml:"regex"`
	Hash    uint32                 `yaml:"hash"`
	Types   flagext.StringSliceCSV `yaml:"types"`
	// Tags defines a set of key=value constraints that must all match the
	// incoming request tags (from X-Query-Tags) for this rule to apply.
	// Keys are case-insensitive; values are matched case-insensitively.
	Tags map[string]string `yaml:"query_tags"`
}
// Tags 键名大小写不敏感，值比较亦忽略大小写，便于统一网关注入的标签格式。
