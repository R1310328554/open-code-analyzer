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

//go:build !remove_all_sd || enable_dns_sd

// DNS SRV/A/AAAA 记录服务发现插件注册桩。enable_dns_sd 时链接基于 DNS 查询的目标发现。

package plugins

import (
// blank import 注册 dns_sd_configs 所需的 DNS 发现器。
	_ "github.com/prometheus/prometheus/discovery/dns" // Register dns plugin.
)
