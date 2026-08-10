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

import "context"

// Canceler 负责取消正在执行或排队中的构建。
type Canceler interface {
	// Cancel 取消指定的构建。
	Cancel(context.Context, *Repository, *Build) error

	// CancelPending 取消与给定构建同类型的所有待处理构建。
	CancelPending(context.Context, *Repository, *Build) error
}
