package rulestore

// rulestore 包定义 RuleStore 接口与 ErrGroupNotFound 等错误，List 可返回仅含元数据的组，完整规则需 LoadRuleGroups。

import (
	"context"
	"errors"

	"github.com/grafana/loki/v3/pkg/ruler/rulespb"
)

var (
	// ErrGroupNotFound is returned if a rule group does not exist
	ErrGroupNotFound = errors.New("group does not exist")
	// ErrGroupNamespaceNotFound is returned if a namespace does not exist
	ErrGroupNamespaceNotFound = errors.New("group namespace does not exist")
	// ErrUserNotFound is returned if the user does not currently exist
	ErrUserNotFound = errors.New("no rule groups found for user")
)

// RuleStore 抽象 ruler 规则 CRUD：按 user/namespace 列表、加载、读写与删除。
// RuleStore is used to store and retrieve rules.
// Methods starting with "List" prefix may return partially loaded groups: with only group Name, Namespace and User fields set.
// To make sure that rules within each group are loaded, client must use LoadRuleGroups method.
type RuleStore interface {
// ListAllUsers 返回配置过规则的全部租户 ID。
	// ListAllUsers returns all users with rule groups configured.
	ListAllUsers(ctx context.Context) ([]string, error)

	// ListAllRuleGroups returns all rule groups for all users.
	ListAllRuleGroups(ctx context.Context) (map[string]rulespb.RuleGroupList, error)

	// ListRuleGroupsForUserAndNamespace returns all the active rule groups for a user from given namespace.
	// If namespace is empty, groups from all namespaces are returned.
	ListRuleGroupsForUserAndNamespace(ctx context.Context, userID string, namespace string) (rulespb.RuleGroupList, error)

// LoadRuleGroups 填充 List 返回的 RuleGroupDesc.Rules 字段。
	// LoadRuleGroups loads rules for each rule group in the map.
	// Parameter with groups to load *MUST* be coming from one of the List methods.
	// Reason is that some implementations don't do anything, since their List method already loads the rules.
	LoadRuleGroups(ctx context.Context, groupsToLoad map[string]rulespb.RuleGroupList) error

	GetRuleGroup(ctx context.Context, userID, namespace, group string) (*rulespb.RuleGroupDesc, error)
	SetRuleGroup(ctx context.Context, userID, namespace string, group *rulespb.RuleGroupDesc) error

	// DeleteRuleGroup deletes single rule group.
	DeleteRuleGroup(ctx context.Context, userID, namespace string, group string) error

	// DeleteNamespace lists rule groups for given user and namespace, and deletes all rule groups.
	// If namespace is empty, deletes all rule groups for user.
	DeleteNamespace(ctx context.Context, userID, namespace string) error
}
// ErrGroupNamespaceNotFound 与 ErrUserNotFound 供 API 层区分 404 场景。
