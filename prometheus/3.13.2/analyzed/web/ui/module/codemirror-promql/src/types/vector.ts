// 二元向量匹配类型定义：基数枚举、ON/GROUP 标签列表与 FILL 填充值结构。

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

// VectorMatchCardinality 描述 one-to-one、many-to-one 等 PromQL 向量对齐基数。
export enum VectorMatchCardinality {
  CardOneToOne = 'one-to-one',
  CardManyToOne = 'many-to-one',
  CardOneToMany = 'one-to-many',
  CardManyToMany = 'many-to-many',
}

export interface FillValues {
  lhs: number | null;
  rhs: number | null;
}

// VectorMatching 汇总 card、matchingLabels、on、include 与 fill 完整匹配配置。
export interface VectorMatching {
// card 字段标识参与二元运算的两个 instant vector 之间的对齐方式。
  // The cardinality of the two Vectors.
  card: VectorMatchCardinality;
// matchingLabels 为 ON/IGNORING 子句中用于对齐样本的标签名列表。
  // MatchingLabels contains the labels which define equality of a pair of
  // elements from the Vectors.
  matchingLabels: string[];
// on 为 true 表示 ON 子句（包含列出的标签），false 表示 IGNORING（排除）。
  // On includes the given label names from matching,
  // rather than excluding them.
  on: boolean;
// include 存放 group_left/group_right 额外带入结果侧的低基数标签。
  // Include contains additional labels that should be included in
  // the result from the side with the lower cardinality.
  include: string[];
// fill 可选，对应 fill / fill_left / fill_right 修饰符解析出的填充值。
  // Fill contains optional fill values for missing elements.
  fill: FillValues;
}
