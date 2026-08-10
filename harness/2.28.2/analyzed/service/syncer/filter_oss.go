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

// +build oss

package syncer

import "github.com/drone/drone/core"

// FilterFunc 决定哪些远程仓库应同步到本地数据存储。
type FilterFunc func(*core.Repository) bool

// NamespaceFilter 在 OSS 构建中为无操作实现，始终同步全部仓库。
func NamespaceFilter(namespaces []string) FilterFunc {
	return noopFilter
}

// noopFilter 始终返回 true 的空操作过滤器。
func noopFilter(*core.Repository) bool {
	return true
}
