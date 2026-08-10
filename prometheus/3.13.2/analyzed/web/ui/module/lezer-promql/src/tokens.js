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

// Lezer PromQL 词法扩展：将标识符在特定语法上下文中特化为关键字 token。

import {
  And,
  Avg,
  Atan2,
  Bool,
  Bottomk,
  By,
  Count,
  CountValues,
  Group,
  GroupLeft,
  GroupRight,
  Ignoring,
  inf,
  Max,
  Min,
  nan,
  Offset,
  On,
  Or,
  Quantile,
  LimitK,
  LimitRatio,
  StartFn,
  EndFn,
  Stddev,
  Stdvar,
  Sum,
  Topk,
  Unless,
  Without,
  AtEnd,
  AtStart,
  Smoothed,
  Anchored,
  Fill,
  FillLeft,
  FillRight,
} from "./parser.terms.js";

// keywordTokens 映射始终可特化的 PromQL 关键字（inf/nan/bool/on/offset 等）。
const keywordTokens = {
  inf: inf,
  nan: nan,
  bool: Bool,
  ignoring: Ignoring,
  on: On,
  group_left: GroupLeft,
  group_right: GroupRight,
  offset: Offset,
};

export const specializeIdentifier = (value, stack) => {
  return keywordTokens[value.toLowerCase()] || -1;
};

// contextualKeywordTokens 列出仅在当前 LR 栈可 shift 时才生效的聚合/修饰/逻辑关键字。
const contextualKeywordTokens = {
  avg: Avg,
  atan2: Atan2,
  bottomk: Bottomk,
  count: Count,
  count_values: CountValues,
  group: Group,
  max: Max,
  min: Min,
  quantile: Quantile,
  limitk: LimitK,
  limit_ratio: LimitRatio,
  stddev: Stddev,
  stdvar: Stdvar,
  sum: Sum,
  topk: Topk,
  by: By,
  without: Without,
  and: And,
  or: Or,
  unless: Unless,
  smoothed: Smoothed,
  anchored: Anchored,
  fill: Fill,
  fill_left: FillLeft,
  fill_right: FillRight,
};

// extendIdentifier 处理 start/end/@ 修饰符及上下文关键字，依赖 stack.canShift 消歧。
export const extendIdentifier = (value, stack) => {
  if (value === "start" && stack.canShift(StartFn)) {
    return StartFn;
  }
  if (value === "end" && stack.canShift(EndFn)) {
    return EndFn;
  }
  if (value.toLowerCase() === "start" && stack.canShift(AtStart)) {
    return AtStart;
  }
  if (value.toLowerCase() === "end" && stack.canShift(AtEnd)) {
    return AtEnd;
  }

  const token = contextualKeywordTokens[value.toLowerCase()];
  if (token !== undefined && stack.canShift(token)) {
    return token;
  }

  return -1;
};
