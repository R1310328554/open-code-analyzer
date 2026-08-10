// 语法树路径工具：自叶向根回溯，以及检测子节点是否包含指定 Lezer 节点类型。

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

import { SyntaxNode } from '@lezer/common';

// walkBackward 沿 parent 链向上查找指定 type.id，未找到则返回 null。
// walkBackward will iterate other the tree from the leaf to the root until it founds the given `exit` node.
// It returns null if the exit is not found.
export function walkBackward(node: SyntaxNode | null, exit: number): SyntaxNode | null {
  for (;;) {
    if (!node || node.type.id === exit) {
      return node;
    }
    node = node.parent;
  }
  return null;
}

// containsAtLeastOneChild 判断直接兄弟子节点中是否存在任一匹配类型或名称。
export function containsAtLeastOneChild(node: SyntaxNode, ...child: (number | string)[]): boolean {
  const cursor = node.cursor();
  if (!cursor.next()) {
    // let's try to move directly to the children level and
    // return false immediately if the current node doesn't have any child
    return false;
  }
  let result = false;
  do {
    result = child.some((n) => cursor.type.id === n || cursor.type.name === n);
  } while (!result && cursor.nextSibling());
  return result;
}

// containsChild 按顺序检查兄弟子节点是否依次匹配 child 列表（用于 Expr 序列）。
export function containsChild(node: SyntaxNode, ...child: (number | string)[]): boolean {
  const cursor = node.cursor();
  if (!cursor.next()) {
    // let's try to move directly to the children level and
    // return false immediately if the current node doesn't have any child
    return false;
  }
  let i = 0;

  do {
    if (cursor.type.is(child[i])) {
      i++;
    }
  } while (i < child.length && cursor.nextSibling());

  return i >= child.length;
}
// 路径查找辅助函数供二元表达式与修饰符解析复用。
