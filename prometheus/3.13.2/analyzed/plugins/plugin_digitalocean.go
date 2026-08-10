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

//go:build !remove_all_sd || enable_digitalocean_sd

// DigitalOcean Droplet 服务发现插件注册桩。enable_digitalocean_sd 时编译 DO API 目标发现支持。

package plugins

import (
// blank import 注册 DigitalOcean SD 驱动。
	_ "github.com/prometheus/prometheus/discovery/digitalocean" // Register digitalocean plugin.
)
