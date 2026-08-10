// Copyright 2019 Drone IO, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// orgs 包提供组织列表查询与成员关系缓存包装。
package orgs

import (
	"context"
	"fmt"
	"sync"
	"time"

	"github.com/drone/drone/core"

	lru "github.com/hashicorp/golang-lru"
)

// contentKey 缓存键格式：用户登录名与组织名。
const contentKey = "%s/%s"

// NewCache 用带 TTL 的 LRU 缓存包装 OrganizationService。
func NewCache(base core.OrganizationService, size int, ttl time.Duration) core.OrganizationService {
	// LRU 缓存避免短时间内重复查询同一用户的组织成员关系。
	cache, _ := lru.New(25)

	return &cacher{
		cache: cache,
		base:  base,
		size:  size,
		ttl:   ttl,
	}
}

// cacher 在底层 OrganizationService 上缓存 Membership 查询结果。
type cacher struct {
	mu sync.Mutex

	base core.OrganizationService
	size int
	ttl  time.Duration

	cache *lru.Cache
}

// item 缓存条目，含过期时间与成员/管理员标志。
type item struct {
	expiry time.Time
	member bool
	admin  bool
}

// List 委托底层服务列出用户所属组织。
func (c *cacher) List(ctx context.Context, user *core.User) ([]*core.Organization, error) {
	return c.base.List(ctx, user)
}

// Membership 查询用户在指定组织的成员与管理员身份，优先读缓存。
func (c *cacher) Membership(ctx context.Context, user *core.User, name string) (bool, bool, error) {
	key := fmt.Sprintf(contentKey, user.Login, name)
	now := time.Now()

	// 尝试从缓存读取成员关系。
	cached, ok := c.cache.Get(key)
	if ok {
		item := cached.(*item)
		// 条目过期则驱逐；未过期直接返回缓存结果。
		if now.After(item.expiry) {
			c.cache.Remove(cached)
		} else {
			return item.member, item.admin, nil
		}
	}

	// 缓存未命中或已过期时回源查询最新成员关系。
	member, admin, err := c.base.Membership(ctx, user, name)
	if err != nil {
		return false, false, err
	}

	c.cache.Add(key, &item{
		expiry: now.Add(c.ttl),
		member: member,
		admin:  admin,
	})

	return member, admin, nil
}
