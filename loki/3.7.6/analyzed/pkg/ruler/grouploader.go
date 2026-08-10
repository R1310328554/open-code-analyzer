package ruler

// GroupLoader 实现 Prometheus rules.GroupLoader，用 Loki logql/syntax 解析规则表达式，从 YAML 文件加载 rulefmt.RuleGroups。

import (
	"bytes"
	"os"
	"sync"

	"github.com/pkg/errors"
	"github.com/prometheus/common/model"
	"github.com/prometheus/prometheus/model/rulefmt"
	"github.com/prometheus/prometheus/promql/parser"
	"github.com/prometheus/prometheus/rules"
	"go.yaml.in/yaml/v3"

	"github.com/grafana/loki/v3/pkg/logql/syntax"
)

// GroupLoader 零值可用，Parse 走 syntax.ParseExpr 并适配 parser.Expr。
type GroupLoader struct{}

func (GroupLoader) Parse(query string) (parser.Expr, error) {
	expr, err := syntax.ParseExpr(query)
	if err != nil {
		return nil, err
	}

	return exprAdapter{expr}, nil
}

// Load 读文件后 parseRules，KnownFields(true) 拒绝未知 YAML 字段。
func (g GroupLoader) Load(identifier string, _ bool, _ model.ValidationScheme) (*rulefmt.RuleGroups, []error) {
	b, err := os.ReadFile(identifier)
	if err != nil {
		return nil, []error{errors.Wrap(err, identifier)}
	}
	rgs, errs := g.parseRules(b)
	for i := range errs {
		errs[i] = errors.Wrap(errs[i], identifier)
	}
	return rgs, errs
}

func (GroupLoader) parseRules(content []byte) (*rulefmt.RuleGroups, []error) {
	var (
		groups rulefmt.RuleGroups
		errs   []error
	)

	decoder := yaml.NewDecoder(bytes.NewReader(content))
	decoder.KnownFields(true)

	if err := decoder.Decode(&groups); err != nil {
		errs = append(errs, err)
	}

	if len(errs) > 0 {
		return nil, errs
	}

	return &groups, ValidateGroups(groups.Groups...)
}

// CachingGroupLoader 缓存已加载文件内容，Prune 移除不再引用的 identifier。
type CachingGroupLoader struct {
	loader rules.GroupLoader
	cache  map[string]*rulefmt.RuleGroups
	mtx    sync.RWMutex
}

func NewCachingGroupLoader(l rules.GroupLoader) *CachingGroupLoader {
	return &CachingGroupLoader{
		loader: l,
		cache:  make(map[string]*rulefmt.RuleGroups),
	}
}

func (l *CachingGroupLoader) Load(identifier string, ignoreUnknownFields bool, validationScheme model.ValidationScheme) (*rulefmt.RuleGroups, []error) {
	groups, errs := l.loader.Load(identifier, ignoreUnknownFields, validationScheme)
	if errs != nil {
		return nil, errs
	}

	l.mtx.Lock()
	defer l.mtx.Unlock()

	l.cache[identifier] = groups

	return groups, nil
}

func (l *CachingGroupLoader) Prune(toKeep []string) {
	keep := make(map[string]struct{}, len(toKeep))
	for _, f := range toKeep {
		keep[f] = struct{}{}
	}

	l.mtx.Lock()
	defer l.mtx.Unlock()

	for key := range l.cache {
		if _, ok := keep[key]; !ok {
			delete(l.cache, key)
		}
	}
}

// AlertingRules 展平 cache 中全部 alerting 规则供 MemStore 等组件使用。
func (l *CachingGroupLoader) AlertingRules() []rulefmt.Rule {
	l.mtx.RLock()
	defer l.mtx.RUnlock()

	var rules []rulefmt.Rule
	for _, group := range l.cache {
		for _, g := range group.Groups {
			for _, rule := range g.Rules {
				rules = append(rules, rulefmt.Rule{
					Record:      rule.Record,
					Alert:       rule.Alert,
					Expr:        rule.Expr,
					For:         rule.For,
					Labels:      rule.Labels,
					Annotations: rule.Annotations,
				})
			}
		}
	}

	return rules
}

func (l *CachingGroupLoader) Parse(query string) (parser.Expr, error) {
	return l.loader.Parse(query)
}
// ValidateGroups 在 parseRules 成功后校验规则组格式与 LogQL 语法。
