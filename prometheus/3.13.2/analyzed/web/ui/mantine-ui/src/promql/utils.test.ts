// promql/utils 单元测试：节点类型展开、占位符检测、值类型推断与标签名引号。

import { describe, expect, it } from "vitest";
import {
  getNonParenNodeType,
  containsPlaceholders,
  nodeValueType,
  maybeQuoteLabelName,
} from "./utils";
import { nodeType, valueType, binaryOperatorType } from "./ast";

// getNonParenNodeType 应穿透任意层 parenExpr 返回内层真实 nodeType。
describe("getNonParenNodeType", () => {
  it("works for non-paren type", () => {
    expect(
      getNonParenNodeType({ type: nodeType.numberLiteral, val: "1" })
    ).toBe(nodeType.numberLiteral);
  });

  it("works for single parentheses wrapper", () => {
    expect(
      getNonParenNodeType({
        type: nodeType.parenExpr,
        expr: {
          type: nodeType.numberLiteral,
          val: "1",
        },
      })
    ).toBe(nodeType.numberLiteral);
  });

  it("works for multiple parentheses wrappers", () => {
    expect(
      getNonParenNodeType({
        type: nodeType.parenExpr,
        expr: {
          type: nodeType.parenExpr,
          expr: {
            type: nodeType.parenExpr,
            expr: {
              type: nodeType.numberLiteral,
              val: "1",
            },
          },
        },
      })
    ).toBe(nodeType.numberLiteral);
  });
});

// containsPlaceholders 检测 AST 子树是否含 placeholder 节点。
describe("containsPlaceholders", () => {
  it("does not find placeholders in complete expressions", () => {
    expect(
      containsPlaceholders({
        type: nodeType.parenExpr,
        expr: {
          type: nodeType.numberLiteral,
          val: "1",
        },
      })
    ).toBe(false);
  });

  it("finds placeholders at the root", () => {
    expect(
      containsPlaceholders({
        type: nodeType.placeholder,
        children: [],
      })
    ).toBe(true);
  });

  it("finds placeholders in nested expressions with placeholders", () => {
    expect(
      containsPlaceholders({
        type: nodeType.parenExpr,
        expr: {
          type: nodeType.placeholder,
          children: [],
        },
      })
    ).toBe(true);
  });
});

// nodeValueType 推断字面量、选择器、调用与二元运算结果的 PromQL 值类型。
describe("nodeValueType", () => {
  it("works for binary expressions with placeholders", () => {
    expect(
      nodeValueType({
        type: nodeType.binaryExpr,
        op: binaryOperatorType.add,
        lhs: { type: nodeType.placeholder, children: [] },
        rhs: { type: nodeType.placeholder, children: [] },
        matching: null,
        bool: false,
      })
    ).toBeNull();
  });

  it("works for scalar-scalar binops", () => {
    expect(
      nodeValueType({
        type: nodeType.binaryExpr,
        op: binaryOperatorType.add,
        lhs: { type: nodeType.numberLiteral, val: "1" },
        rhs: { type: nodeType.numberLiteral, val: "1" },
        matching: null,
        bool: false,
      })
    ).toBe(valueType.scalar);
  });

  it("works for scalar-vector binops", () => {
    expect(
      nodeValueType({
        type: nodeType.binaryExpr,
        op: binaryOperatorType.add,
        lhs: {
          type: nodeType.vectorSelector,
          name: "metric_name",
          matchers: [],
          offset: 0,
          timestamp: null,
          startOrEnd: null,
          anchored: false,
          smoothed: false,
        },
        rhs: { type: nodeType.numberLiteral, val: "1" },
        matching: null,
        bool: false,
      })
    ).toBe(valueType.vector);
  });

  it("works for vector-vector binops", () => {
    expect(
      nodeValueType({
        type: nodeType.binaryExpr,
        op: binaryOperatorType.add,
        lhs: {
          type: nodeType.vectorSelector,
          name: "metric_name",
          matchers: [],
          offset: 0,
          timestamp: null,
          startOrEnd: null,
          anchored: false,
          smoothed: false,
        },
        rhs: {
          type: nodeType.vectorSelector,
          name: "metric_name",
          matchers: [],
          offset: 0,
          timestamp: null,
          startOrEnd: null,
          anchored: false,
          smoothed: false,
        },
        matching: null,
        bool: false,
      })
    ).toBe(valueType.vector);
  });
});

// maybeQuoteLabelName 对非标识符标签名加双引号，合法标识符保持原样。
describe("maybeQuoteLabelName", () => {
  it("does not quote valid PromQL label names", () => {
    expect(maybeQuoteLabelName("job")).toBe("job");
    expect(maybeQuoteLabelName("status_code")).toBe("status_code");
  });

  it("quotes and escapes label names with extended characters", () => {
    expect(maybeQuoteLabelName("service.version")).toBe('"service.version"');
    expect(maybeQuoteLabelName("team/name")).toBe('"team/name"');
    expect(maybeQuoteLabelName('team"name')).toBe('"team\\"name"');
  });
});
