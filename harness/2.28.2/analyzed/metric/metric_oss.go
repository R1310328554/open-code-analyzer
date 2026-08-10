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

// metric 包（OSS 构建）提供指标注册函数的空实现，编译时不链接 Prometheus。
package metric

import "github.com/drone/drone/core"

// BuildCount OSS 空实现，不注册构建总数指标。
func BuildCount(core.BuildStore) {}

// PendingBuildCount OSS 空实现，不注册待执行构建指标。
func PendingBuildCount(core.BuildStore) {}

// RunningBuildCount OSS 空实现，不注册运行中构建指标。
func RunningBuildCount(core.BuildStore) {}

// RunningJobCount OSS 空实现，不注册运行中作业指标。
func RunningJobCount(core.StageStore) {}

// PendingJobCount OSS 空实现，不注册待执行作业指标。
func PendingJobCount(core.StageStore) {}

// RepoCount OSS 空实现，不注册仓库总数指标。
func RepoCount(core.RepositoryStore) {}

// UserCount OSS 空实现，不注册用户总数指标。
func UserCount(core.UserStore) {}
