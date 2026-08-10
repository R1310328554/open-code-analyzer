import { describe, expect, it } from "vitest";
import { isDefined } from "./isDefined";

/** isDefined 工具函数单元测试：区分「已定义」与 null/undefined */
describe("isDefined", () => {
  /** 0、false、空字符串等 falsy 但已定义的值应返回 true */
  it("detects defined values", () => {
    expect(isDefined(0)).toBe(true);
    expect(isDefined(false)).toBe(true);
    expect(isDefined("")).toBe(true);
  });

  /** null 与 undefined 应视为未定义 */
  it("detects undefined values", () => {
    expect(isDefined(undefined)).toBe(false);
    expect(isDefined(null)).toBe(false);
  });
});
