// 独立文件存放：若从 localStorageMiddleware 引入会在 Redux 初始化前触发 listener。
// This has to live in its own file since including it from
// localStorageMiddleware.ts causes startup issues, as the
// middleware 中的 listener 会在 store 就绪前访问 action creator。
// listener setup there accesses an action creator before Redux
// 因此初始化逻辑单独导出，供 slice 初始 state 安全读取持久化值。
// has been initialized.
export const initializeFromLocalStorage = <T>(
  key: string,
  defaultValue: T
): T => {
  const value = localStorage.getItem(key);
  if (value === null) {
    return defaultValue;
  }
  return JSON.parse(value);
};
