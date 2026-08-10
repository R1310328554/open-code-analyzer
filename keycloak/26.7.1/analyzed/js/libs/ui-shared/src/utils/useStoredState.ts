import { Dispatch, useCallback, useMemo } from "react";
import { useStorageItem } from "./useStorageItem";

/**
 * 类似 React `useState()`，但将状态持久化到 [Web Storage API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API)。
 * 其他文档修改同一键时，通过 [`storage`](https://developer.mozilla.org/en-US/docs/Web/API/Window/storage_event) 事件自动同步。
 *
 * 值以 [JSON](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/JSON) 序列化存储，因此 `defaultValue` 与后续写入的值须可 JSON 序列化。
 * 反序列化后的对象与原始引用不会 referentially equal。
 *
 * @param storageArea 目标存储区域，须实现 [`Storage`](https://developer.mozilla.org/en-US/docs/Web/API/Storage) 接口
 * @param keyName 存储键名，与 [`Storage.getItem()`](https://developer.mozilla.org/en-US/docs/Web/API/Storage/getItem) 一致
 * @param defaultValue 无存储值时的默认值（须可 JSON 序列化）
 */
export function useStoredState<S>(
  storageArea: Storage,
  keyName: string,
  defaultValue: S,
): [S, Dispatch<S>] {
  const defaultValueSerialized = useMemo(
    () => JSON.stringify(defaultValue),
    [defaultValue],
  );

  const [storedValue, setStoredValue] = useStorageItem(
    storageArea,
    keyName,
    defaultValueSerialized,
  );

  const value = useMemo<S>(() => JSON.parse(storedValue), [storedValue]);
  const setValue = useCallback(
    (value: S) => setStoredValue(JSON.stringify(value)),
    [],
  );

  return [value, setValue];
}
