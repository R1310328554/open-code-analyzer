import type { Context } from "react";
import { useContext } from "react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { useRequiredContext } from "./useRequiredContext";

vi.mock("react");

const useContextMock = vi.mocked(useContext);

/** useRequiredContext Hook 单元测试：Provider 缺失时应明确报错 */
describe("useRequiredContext", () => {
  beforeEach(() => {
    useContextMock.mockReset();
  });

  /** Provider 存在时应正常返回 Context 值 */
  it("resolves the context", () => {
    const context = {} as Context<unknown>;
    const resolved = "FakeValue";

    useContextMock.mockReturnValue(resolved);

    expect(useRequiredContext(context)).toEqual(resolved);
  });

  /** 具名 Context 在值为 undefined/null 时应提示 displayName */
  it("throws if a named context cannot be resolved", () => {
    const displayName = "FakeDisplayName";
    const context = { displayName } as Context<unknown>;
    const expected = `No provider found for the '${displayName}' context, make sure it is included in your component hierarchy.`;

    useContextMock.mockReturnValue(undefined);
    expect(() => useRequiredContext(context)).toThrow(expected);

    useContextMock.mockReturnValue(null);
    expect(() => useRequiredContext(context)).toThrow(expected);
  });

  /** 无名 Context 在值为 undefined/null 时应给出通用错误信息 */
  it("throws if an unnamed context cannot be resolved", () => {
    const context = {} as Context<unknown>;
    const expected =
      "No provider found for an unknown context, make sure it is included in your component hierarchy.";

    useContextMock.mockReturnValue(undefined);
    expect(() => useRequiredContext(context)).toThrow(expected);

    useContextMock.mockReturnValue(null);
    expect(() => useRequiredContext(context)).toThrow(expected);
  });
});
