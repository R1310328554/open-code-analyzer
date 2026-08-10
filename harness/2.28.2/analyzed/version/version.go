// Copyright 2019 Drone IO, Inc.
// Copyright 2016 The Linux Foundation
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

// version 包定义 Drone 服务器编译版本号与 Git 构建元数据。
package version

import "github.com/coreos/go-semver/semver"

var (
	// GitRepository 编译时对应的 Git 仓库地址。
	GitRepository string
	// GitCommit 编译时对应的 Git 提交哈希。
	GitCommit string
	// VersionMajor 主版本号，不兼容的 API 变更时递增。
	VersionMajor int64 = 2
	// VersionMinor 次版本号，向后兼容的功能新增时递增。
	VersionMinor int64 = 28
	// VersionPatch 修订号，向后兼容的问题修复时递增。
	VersionPatch int64 = 1
	// VersionPre 预发布标识（如 alpha、beta）。
	VersionPre = ""
	// VersionDev 开发分支元数据；正式发行版为空字符串。
	VersionDev string
)

// Version 为上述各字段组装的 semver 版本对象。
var Version = semver.Version{
	Major:      VersionMajor,
	Minor:      VersionMinor,
	Patch:      VersionPatch,
	PreRelease: semver.PreRelease(VersionPre),
	Metadata:   VersionDev,
}
