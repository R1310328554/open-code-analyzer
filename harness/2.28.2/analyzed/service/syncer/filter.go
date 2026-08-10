// Copyright 2019 Drone.IO Inc. All rights reserved.
// Use of this source code is governed by the Drone Non-Commercial License
// that can be found in the LICENSE file.

// +build !oss

package syncer

import (
	"strings"

	"github.com/drone/drone/core"
)

// FilterFunc 决定哪些远程仓库应同步到本地数据存储。
type FilterFunc func(*core.Repository) bool

// NamespaceFilter 返回仅允许指定命名空间（大小写不敏感）通过的过滤器。
func NamespaceFilter(namespaces []string) FilterFunc {
	// 命名空间列表为空时退化为全量通过。
	if len(namespaces) == 0 {
		return noopFilter
	}
	return func(r *core.Repository) bool {
		for _, namespace := range namespaces {
			if strings.EqualFold(namespace, r.Namespace) {
				return true
			}
		}
		return false
	}
}

// noopFilter 始终返回 true 的空操作过滤器。
func noopFilter(*core.Repository) bool {
	return true
}
