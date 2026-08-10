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

// +build nolimit
// +build !oss

// license 包（nolimit 构建）提供无配额限制的许可证桩实现。
package license

import (
	"github.com/drone/drone/core"
)

// DefaultLicense 无限制的空许可证，Kind 为 LicenseFree。
var DefaultLicense = &core.License{Kind: core.LicenseFree}

// Trial 直接返回 DefaultLicense，忽略 SCM 平台参数。
func Trial(string) *core.License         { return DefaultLicense }
// Load 直接返回 DefaultLicense，不读取文件。
func Load(string) (*core.License, error) { return DefaultLicense, nil }
