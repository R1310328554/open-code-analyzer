// Vitest/Jest 测试全局 setup：在 Node 22+ 无 jsdom Storage 时注入内存 localStorage 模拟。

import "@testing-library/jest-dom";

// Node 22+ 在未指定 --localstorage-file 时会预置 undefined 的 localStorage，
// Node.js v22+ pre-defines localStorage on globalThis as undefined (when
// --localstorage-file is omitted), which prevents jsdom from installing its
// Storage implementation. Provide an in-memory mock so module-level code that
// 使 Redux slice 初始化等模块级 localStorage 读取在测试中正常工作。
// reads localStorage (e.g. Redux slice initializers) works correctly in tests.
if (typeof localStorage === "undefined") {
  const store: Record<string, string> = {};
  Object.defineProperty(globalThis, "localStorage", {
    value: {
      getItem: (key: string) => store[key] ?? null,
      setItem: (key: string, value: string) => {
        store[key] = String(value);
      },
      removeItem: (key: string) => {
        delete store[key];
      },
      clear: () => {
        for (const k of Object.keys(store)) delete store[k];
      },
      key: (index: number) => Object.keys(store)[index] ?? null,
      get length() {
        return Object.keys(store).length;
      },
    },
    writable: true,
    configurable: true,
  });
}
