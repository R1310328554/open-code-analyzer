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

package core

import (
	"context"
	"errors"
)

var (
	// ErrValidatorSkip 表示流水线校验失败，但应静默跳过而非报错。
	ErrValidatorSkip = errors.New("validation failed: skip pipeline")

	// ErrValidatorBlock 表示流水线校验失败，但应阻塞等待人工审批而非报错。
	ErrValidatorBlock = errors.New("validation failed: block pipeline")
)

type (
	// ValidateArgs 表示一次流水线校验请求。
	ValidateArgs struct {
		User   *User       `json:"-"`
		Repo   *Repository `json:"repo,omitempty"`
		Build  *Build      `json:"build,omitempty"`
		Config *Config     `json:"config,omitempty"`
	}

	// ValidateService 校验 YAML 流水线配置，无效时返回错误。
	ValidateService interface {
		Validate(context.Context, *ValidateArgs) error
	}
)
