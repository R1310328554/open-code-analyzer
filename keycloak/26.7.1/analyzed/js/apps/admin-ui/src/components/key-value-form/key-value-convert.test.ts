import { describe, expect, it, vi } from "vitest";
import {
  arrayToKeyValue,
  keyValueToArray,
  KeyValueType,
} from "./key-value-convert";

vi.mock("react");

/** 键值对表单与多值属性对象互转函数的单元测试。 */
describe("Tests the convert functions for attribute input", () => {
  it("converts empty array into form value", () => {
    const given: KeyValueType[] = [];

    // 空数组应转为空对象
    const result = keyValueToArray(given);

    expect(result).toEqual({});
  });

  it("converts array into form value", () => {
    const given = [{ key: "theKey", value: "theValue" }];

    // 单行键值对应单元素字符串数组
    const result = keyValueToArray(given);

    expect(result).toEqual({ theKey: ["theValue"] });
  });

  it("convert only values", () => {
    const given = [
      { key: "theKey", value: "theValue" },
      { key: "", value: "" },
    ];

    // 空 key 行应被过滤，不参与结果
    const result = keyValueToArray(given);

    expect(result).toEqual({ theKey: ["theValue"] });
  });

  it("convert object to attributes", () => {
    const given = { one: ["1"], two: ["2"] };

    // 多值对象展开为键值对行列表
    const result = arrayToKeyValue(given);

    expect(result).toEqual([
      { key: "one", value: "1" },
      { key: "two", value: "2" },
    ]);
  });

  it("convert duplicates into array values", () => {
    const given = [
      { key: "theKey", value: "one" },
      { key: "theKey", value: "two" },
    ];

    // 重复 key 合并为同一属性下的多值数组
    const result = keyValueToArray(given);

    expect(result).toEqual({ theKey: ["one", "two"] });
  });
});
