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

// PromQL 自动补全工厂：根据配置构造 HybridComplete 或注入自定义 CompleteStrategy。

import { HybridComplete } from './hybrid';
import { CachedPrometheusClient, HTTPPrometheusClient, PrometheusClient, PrometheusConfig } from '../client/prometheus';
import { CompletionContext, CompletionResult } from '@codemirror/autocomplete';

// CompleteStrategy 统一 promQL 补全入口，支持同步或异步返回 CompletionResult。
// Complete is the interface that defines the simple method that returns a CompletionResult.
// Every different completion mode must implement this interface.
export interface CompleteStrategy {
  promQL(context: CompletionContext): Promise<CompletionResult | null> | CompletionResult | null;
  destroy?(): void;
}

// CompleteConfiguration should be used to customize the autocompletion.
// CompleteConfiguration 可传 remote URL、缓存选项、元数据上限或完全自定义策略。
export interface CompleteConfiguration {
  remote?: PrometheusConfig | PrometheusClient;
  // maxMetricsMetadata is the maximum number of metrics in Prometheus for which metadata is fetched.
  // If the number of metrics exceeds this limit, no metric metadata is fetched at all.
  maxMetricsMetadata?: number;
  // When providing this custom CompleteStrategy, the settings above will not be used.
  completeStrategy?: CompleteStrategy;
}

// isPrometheusClient 判断 remote 字段是已构造客户端还是需包装 HTTPPrometheusClient 的配置。
export function isPrometheusClient(remoteConfig: PrometheusConfig | PrometheusClient): remoteConfig is PrometheusClient {
  const client = remoteConfig as PrometheusClient;
  return (
    typeof client.labelNames === 'function' &&
    typeof client.labelValues === 'function' &&
    typeof client.metricMetadata === 'function' &&
    typeof client.series === 'function' &&
    typeof client.metricNames === 'function'
  );
}

// newCompleteStrategy 默认 CachedPrometheusClient+HybridComplete，无 remote 时纯离线补全。
export function newCompleteStrategy(conf?: CompleteConfiguration): CompleteStrategy {
  if (conf?.completeStrategy) {
    return conf.completeStrategy;
  }
  if (conf?.remote) {
    if (isPrometheusClient(conf.remote)) {
      return new HybridComplete(conf.remote, conf.maxMetricsMetadata);
    }
    return new HybridComplete(new CachedPrometheusClient(new HTTPPrometheusClient(conf.remote), conf.remote.cache), conf.maxMetricsMetadata);
  }
  return new HybridComplete();
}
