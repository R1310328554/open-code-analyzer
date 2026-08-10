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

// plugins 最小构建：blank import 注册 file 与 http 服务发现插件，供精简版 Prometheus 二进制链接。

package plugins

import (
// side-effect import 在 init 中注册基于文件的 SD 机制。
	_ "github.com/prometheus/prometheus/discovery/file" // Register file plugin.
// side-effect import 在 init 中注册 HTTP 端点 SD 机制。
	_ "github.com/prometheus/prometheus/discovery/http" // Register http plugin.
)
