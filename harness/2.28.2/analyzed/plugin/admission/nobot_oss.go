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

package admission

import (
	"time"

	"github.com/drone/drone/core"
)

// Nobot 在 OSS 构建中返回空操作准入控制器，不执行反机器人校验。
func Nobot(core.UserService, time.Duration) core.AdmissionService {
	return new(noop)
}
