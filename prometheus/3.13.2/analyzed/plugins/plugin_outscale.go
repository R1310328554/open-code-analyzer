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

// Outscale 云厂商服务发现插件注册桩：blank import 注册 discovery/outscale。

//go:build !remove_all_sd || enable_outscale_sd

package plugins

import (
// 注册 Outscale SD，调用 Outscale API 列举虚拟机实例作为 scrape 目标。
	_ "github.com/prometheus/prometheus/discovery/outscale" // Register outscale plugin.
)
