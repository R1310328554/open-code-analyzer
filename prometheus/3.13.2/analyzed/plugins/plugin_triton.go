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

// Joyent Triton 服务发现插件注册桩：blank import 在 init 中注册 discovery/triton。

//go:build !remove_all_sd || enable_triton_sd

package plugins

import (
// 注册 Triton SD，通过 Triton Container Service API 发现容器与虚拟机实例。
	_ "github.com/prometheus/prometheus/discovery/triton" // Register triton plugin.
)
