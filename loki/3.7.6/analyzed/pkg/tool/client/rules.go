package client

// rules 客户端方法对 Loki Ruler REST API 做 CRUD：按 namespace/group 路径编码创建、删除、获取与列举规则组。

import (
	"context"
	"fmt"
	"io"
	"net/url"

	"github.com/pkg/errors"
	log "github.com/sirupsen/logrus"
	"go.yaml.in/yaml/v3"

	"github.com/grafana/loki/v3/pkg/tool/rules/rwrulefmt"
)

// CreateRuleGroup 将 rwrulefmt.RuleGroup 序列化为 YAML 后 POST 到 namespace 路径。
// CreateRuleGroup creates a new rule group
func (r *LokiClient) CreateRuleGroup(ctx context.Context, namespace string, rg rwrulefmt.RuleGroup) error {
	payload, err := yaml.Marshal(&rg)
	if err != nil {
		return err
	}

	escapedNamespace := url.PathEscape(namespace)
	path := r.apiPath + "/" + escapedNamespace

	res, err := r.doRequest(ctx, path, "POST", payload)
	if err != nil {
		return err
	}

	res.Body.Close()

	return nil
}

// DeleteRuleGroup 对 /{namespace}/{group} 发起 DELETE 移除远端规则组。
// DeleteRuleGroup creates a new rule group
func (r *LokiClient) DeleteRuleGroup(ctx context.Context, namespace, groupName string) error {
	escapedNamespace := url.PathEscape(namespace)
	escapedGroupName := url.PathEscape(groupName)
	path := r.apiPath + "/" + escapedNamespace + "/" + escapedGroupName

	res, err := r.doRequest(ctx, path, "DELETE", nil)
	if err != nil {
		return err
	}

	res.Body.Close()

	return nil
}

// GetRuleGroup GET 单个规则组 YAML 并反序列化为 rwrulefmt.RuleGroup。
// GetRuleGroup retrieves a rule group
func (r *LokiClient) GetRuleGroup(ctx context.Context, namespace, groupName string) (*rwrulefmt.RuleGroup, error) {
	escapedNamespace := url.PathEscape(namespace)
	escapedGroupName := url.PathEscape(groupName)
	path := r.apiPath + "/" + escapedNamespace + "/" + escapedGroupName

	fmt.Println(path)
	res, err := r.doRequest(ctx, path, "GET", nil)
	if err != nil {
		return nil, err
	}

	defer res.Body.Close()
	body, err := io.ReadAll(res.Body)

	if err != nil {
		return nil, err
	}

	rg := rwrulefmt.RuleGroup{}
	err = yaml.Unmarshal(body, &rg)
	if err != nil {
		log.WithFields(log.Fields{
			"body": string(body),
		}).Debugln("failed to unmarshal rule group from response")

		return nil, errors.Wrap(err, "unable to unmarshal response")
	}

	return &rg, nil
}

// ListRules GET 全量或指定 namespace 的规则 map，键为 namespace、值为规则组切片。
// ListRules retrieves a rule group
func (r *LokiClient) ListRules(ctx context.Context, namespace string) (map[string][]rwrulefmt.RuleGroup, error) {
	path := r.apiPath
	if namespace != "" {
		path = path + "/" + namespace
	}

	res, err := r.doRequest(ctx, path, "GET", nil)
	if err != nil {
		return nil, err
	}

	defer res.Body.Close()
	body, err := io.ReadAll(res.Body)

	if err != nil {
		return nil, err
	}

	ruleSet := map[string][]rwrulefmt.RuleGroup{}
	err = yaml.Unmarshal(body, &ruleSet)
	if err != nil {
		return nil, err
	}

	return ruleSet, nil
}
// namespace 与 groupName 经 url.PathEscape 编码，避免特殊字符破坏 REST 路径。
