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

//go:build !remove_all_sd || enable_azure_sd

// Azure 虚拟机等服务发现插件注册桩。由 enable_azure_sd 或默认 SD 构建标签控制是否链接。

package plugins

import (
// blank import 链接 Azure SD 实现并在启动时注册 discovery 机制。
	_ "github.com/prometheus/prometheus/discovery/azure" // Register azure plugin.
)
