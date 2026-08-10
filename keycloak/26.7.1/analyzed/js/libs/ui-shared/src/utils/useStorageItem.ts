import { Dispatch, useCallback, useEffect, useState } from "react";

/**
 * 读取并同步 [Web Storage API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API) 中指定键的字符串值。
 * 当其他文档（如另一个标签页）修改同一存储项时，通过 [`storage`](https://developer.mozilla.org/en-US/docs/Web/API/Window/storage_event) 事件自动更新本地状态。
 *
 * @param storageArea 目标存储区域，须实现 [`Storage`](https://developer.mozilla.org/en-US/docs/Web/API/Storage) 接口（如 [`localStorage`](https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage)、[`sessionStorage`](https://developer.mozilla.org/en-US/docs/Web/API/Window/sessionStorage)）
 * @param keyName 要读取的键名，与 [`Storage.getItem()`](https://developer.mozilla.org/en-US/docs/Web/API/Storage/getItem) 参数一致
 * @param defaultValue 未读到存储值时的默认字符串
 */
export function useStorageItem(
  storageArea: Storage,
  keyName: string,
  defaultValue: string,
): [string, Dispatch<string>] {
  const [value, setInnerValue] = useState(
    () => storageArea.getItem(keyName) ?? defaultValue,
  );

  const setValue = useCallback((newValue: string) => {
    setInnerValue(newValue);
    storageArea.setItem(keyName, newValue);
  }, []);

  useEffect(() => {
    // 键名或 storageArea 变化时重新从存储读取；React 仅在值变化时重渲染
    setInnerValue(storageArea.getItem(keyName) ?? defaultValue);

    // 监听跨文档 storage 事件，以便其他标签页修改时同步本地 state
    window.addEventListener("storage", handleStorage);

    function handleStorage(event: StorageEvent) {
      // 事件来自其他 storageArea（如 local vs session）时可忽略
      if (event.storageArea !== storageArea) {
        return;
      }

      // key 为 null 表示整区被清空；否则只关心目标键或其清空
      if (event.key === null || event.key === keyName) {
        setInnerValue(event.newValue ?? defaultValue);
      }
    }

    return () => window.removeEventListener("storage", handleStorage);
  }, [storageArea, keyName]);

  return [value, setValue];
}
