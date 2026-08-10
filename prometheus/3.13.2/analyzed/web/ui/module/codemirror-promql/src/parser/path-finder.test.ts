// path-finder 单元测试：验证 walkBackward、containsChild 与 containsAtLeastOneChild 行为。

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

import {
  Add,
  AggregateExpr,
  BinaryExpr,
  Div,
  Eql,
  FunctionCallBody,
  Gte,
  Gtr,
  Lss,
  Lte,
  Mod,
  Mul,
  Neq,
  Sub,
  VectorSelector,
} from '@prometheus-io/lezer-promql';
import { createEditorState } from '../test/utils-test';
import { containsAtLeastOneChild, containsChild, walkBackward } from './path-finder';
import { SyntaxNode } from '@lezer/common';
import { syntaxTree } from '@codemirror/language';

// containsAtLeastOneChild 测试比较/算术运算符子节点存在性检测。
describe('containsAtLeastOneChild test', () => {
  const testCases = [
    {
      title: 'should not find a node if none is defined',
      expr: '1 > 2',
      pos: 3,
      expectedResult: false,
      child: [],
    },
    {
      title: 'should find a node in the given list',
      expr: '1 > 2',
      pos: 0,
      take: BinaryExpr,
      child: [Eql, Neq, Lte, Lss, Gte, Gtr],
      expectedResult: true,
    },
    {
      title: 'should not find a node in the given list',
      expr: '1 > 2',
      pos: 0,
      take: BinaryExpr,
      child: [Mul, Div, Mod, Add, Sub],
      expectedResult: false,
    },
  ];
  testCases.forEach((value) => {
    it(value.title, () => {
      const state = createEditorState(value.expr);
      const subTree = syntaxTree(state).resolve(value.pos, -1);
      const node = value.take == null ? subTree : subTree.getChild(value.take);
      expect(node).toBeTruthy();
      if (node) {
        expect(value.expectedResult).toEqual(containsAtLeastOneChild(node, ...value.child));
      }
    });
  });
});

// containsChild 测试按序匹配 FunctionCallBody 内多个 Expr 子节点。
describe('containsChild test', () => {
  const testCases = [
    {
      title: 'Should find all expr in a subtree',
      expr: 'metric_name / ignor',
      pos: 0,
      expectedResult: true,
      walkThrough: [BinaryExpr],
      child: ['Expr', 'Expr'],
    },
    {
      title: 'Should not find all child required',
      expr: 'sum(ra)',
      pos: 0,
      expectedResult: false,
      walkThrough: [AggregateExpr, FunctionCallBody],
      child: ['Expr', 'Expr'],
    },
  ];
  testCases.forEach((value) => {
    it(value.title, () => {
      const state = createEditorState(value.expr);
      let node: SyntaxNode | null | undefined = syntaxTree(state).resolve(value.pos, -1);
      for (const enter of value.walkThrough) {
        node = node?.getChild(enter);
      }
      expect(node).toBeTruthy();
      if (node) {
        expect(value.expectedResult).toEqual(containsChild(node, ...value.child));
      }
    });
  });
});

// walkBackward 测试从光标位置向上找到 VectorSelector 祖先节点。
describe('walkbackward test', () => {
  const testCases = [
    {
      title: 'should find the parent',
      expr: 'metric_name{}',
      pos: 12,
      exit: VectorSelector,
      expectedResult: VectorSelector,
    },
  ];
  testCases.forEach((value) => {
    it(value.title, () => {
      const state = createEditorState(value.expr);
      const tree = syntaxTree(state).resolve(value.pos, -1);
      expect(value.expectedResult).toEqual(walkBackward(tree, value.exit)?.type.id);
    });
  });
});
// 路径查找测试确保语法树遍历工具与 Lezer 节点一致。
