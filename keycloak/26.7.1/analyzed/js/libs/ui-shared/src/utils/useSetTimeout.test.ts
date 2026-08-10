/**
 * @vitest-environment jsdom
 */
import { renderHook } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { useSetTimeout } from "./useSetTimeout";

vi.useFakeTimers();

/** useSetTimeout Hook 单元测试：调度、取消与卸载清理行为 */
describe("useSetTimeout", () => {
  it("schedules timeouts and triggers the callbacks", () => {
    const { result } = renderHook(() => useSetTimeout());
    const setTimeoutSpy = vi.spyOn(global, "setTimeout");

    // 调度两个不同延迟的回调
    const callback1 = vi.fn();
    const callback2 = vi.fn();
    result.current(callback1, 1000);
    result.current(callback2, 500);

    // 确认 setTimeout 以正确参数被调用
    expect(setTimeoutSpy).toHaveBeenCalledTimes(2);
    expect(setTimeoutSpy).toBeCalledWith(expect.any(Function), 1000);
    expect(setTimeoutSpy).toBeCalledWith(expect.any(Function), 500);

    // 推进虚拟时钟，验证回调按延迟顺序触发
    expect(callback2).not.toBeCalled();
    vi.advanceTimersByTime(500);
    expect(callback1).not.toBeCalled();
    expect(callback2).toBeCalled();
    vi.advanceTimersByTime(500);
    expect(callback1).toBeCalled();

    setTimeoutSpy.mockRestore();
  });

  it("throws if a timeout is scheduled after the component has unmounted", () => {
    const { result, unmount } = renderHook(() => useSetTimeout());

    unmount();

    // 卸载后再次调度应抛出明确错误
    expect(() => result.current(vi.fn(), 1000)).toThrowError(
      "Can't schedule a timeout on an unmounted component.",
    );
  });

  it("clears a timeout if the component unmounts", () => {
    const { result, unmount } = renderHook(() => useSetTimeout());
    const setTimeoutSpy = vi.spyOn(global, "setTimeout");
    const clearTimeoutSpy = vi.spyOn(global, "clearTimeout");
    const callback = vi.fn();

    result.current(callback, 1000);

    // 卸载时应清除尚未触发的定时器
    unmount();
    expect(clearTimeoutSpy).toBeCalled();

    // 定时器被清除后回调不应再执行
    vi.runOnlyPendingTimers();
    expect(callback).not.toBeCalled();

    setTimeoutSpy.mockRestore();
    clearTimeoutSpy.mockRestore();
  });

  it("clears a timeout when cancelled", () => {
    const { result } = renderHook(() => useSetTimeout());
    const setTimeoutSpy = vi.spyOn(global, "setTimeout");
    const clearTimeoutSpy = vi.spyOn(global, "clearTimeout");
    const callback = vi.fn();
    const cancel = result.current(callback, 1000);

    // 主动调用 cancel 应清除对应定时器
    cancel();
    expect(clearTimeoutSpy).toBeCalled();

    // 取消后回调不应再执行
    vi.runOnlyPendingTimers();
    expect(callback).not.toBeCalled();

    setTimeoutSpy.mockRestore();
    clearTimeoutSpy.mockRestore();
  });
});
