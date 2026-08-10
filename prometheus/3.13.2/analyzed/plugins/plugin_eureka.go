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

//go:build !remove_all_sd || enable_eureka_sd

// Netflix Eureka 服务发现插件注册桩。enable_eureka_sd 时纳入 Eureka 注册表目标拉取。

package plugins

import (
// blank import 注册 Eureka SD，解析 eureka_sd_configs 配置块。
	_ "github.com/prometheus/prometheus/discovery/eureka" // Register eureka plugin.
)
