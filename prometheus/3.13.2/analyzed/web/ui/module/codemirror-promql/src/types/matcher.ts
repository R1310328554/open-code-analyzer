// 标签匹配器模型：封装 Lezer 匹配符类型、标签名与值，并判断是否为空匹配。

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

import { EqlSingle, Neq } from '@prometheus-io/lezer-promql';

// Matcher 对应 PromQL 标签选择器中单条 label matcher 的语义表示。
export class Matcher {
  type: number;
  name: string;
  value: string;

  constructor(type: number, name: string, value: string) {
    this.type = type;
    this.name = name;
    this.value = value;
  }

// matchesEmpty 识别 EqlSingle 空串或 Neq 非空等会选中全部序列的退化匹配。
  matchesEmpty(): boolean {
    switch (this.type) {
      case EqlSingle:
        return this.value === '';
      case Neq:
        return this.value !== '';
      default:
        return false;
    }
  }
}
// 匹配器类型用于向量选择器 lint 与标签解析逻辑。
