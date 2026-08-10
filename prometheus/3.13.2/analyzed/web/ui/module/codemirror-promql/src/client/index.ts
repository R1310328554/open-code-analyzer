// Copyright 2021 The Prometheus Authors
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

// Prometheus HTTP 客户端模块入口：导出客户端类型、配置与可注入的 fetch 函数。

export { PrometheusClient, PrometheusConfig, CacheConfig } from './prometheus';

export type FetchFn = (input: RequestInfo, init?: RequestInit) => Promise<Response>;
