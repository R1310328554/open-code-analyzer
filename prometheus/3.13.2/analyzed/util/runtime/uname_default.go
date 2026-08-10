// Copyright The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// 非 Linux 平台 Uname 桩：无法调用 unix.Uname 时仅返回 (GOOS) 占位字符串。

//go:build !linux

package runtime

import "runtime"

// Uname 在非 Linux 构建中返回括号包裹的 runtime.GOOS，供版本页展示。
// Uname for any platform other than linux.
func Uname() string {
	return "(" + runtime.GOOS + ")"
}
