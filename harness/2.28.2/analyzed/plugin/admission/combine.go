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

package admission

import (
	"context"

	"github.com/drone/drone/core"
)

// Combine 将多个 AdmissionService 组合为链式校验：任一拒绝则整体失败。
func Combine(service ...core.AdmissionService) core.AdmissionService {
	return &combined{services: service}
}

// combined 按顺序调用各子准入服务。
type combined struct {
	services []core.AdmissionService
}

// Admit 依次调用各子服务的 Admit，首个错误即返回。
func (s *combined) Admit(ctx context.Context, user *core.User) error {
	for _, service := range s.services {
		if err := service.Admit(ctx, user); err != nil {
			return err
		}
	}
	return nil
}
