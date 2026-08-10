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

//go:build !remove_all_sd || enable_aws_sd

// AWS EC2 等服务发现插件注册桩。构建标签 enable_aws_sd 或未 strip 全部 SD 时编译进二进制。

package plugins

import (
// blank import 触发 discovery/aws 包 init 向 SD 注册表登记 AWS 发现器。
	_ "github.com/prometheus/prometheus/discovery/aws" // Register aws plugin.
)
