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

// goversion init：引用版本占位符，Go 版本低于 1.12 时编译失败。

package goversion

// 若工具链 Go 版本不满足 go1.12 构建标签，此处引用会导致编译错误。
// This will fail to compile if the Go runtime version isn't >= 1.12.
var _ = _SoftwareRequiresGOVERSION1_12
