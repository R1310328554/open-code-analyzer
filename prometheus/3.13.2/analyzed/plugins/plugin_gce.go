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

//go:build !remove_all_sd || enable_gce_sd

// Google Compute Engine 服务发现插件注册桩。enable_gce_sd 时编译 GCE 实例列表发现支持。

package plugins

import (
// blank import 注册 GCE SD，供 gce_sd_configs 在运行时可用。
	_ "github.com/prometheus/prometheus/discovery/gce" // Register gce plugin.
)
