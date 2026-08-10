/**
 * 查询并订阅浏览器 Permissions API 状态的 Hook。
 * 将 PermissionStatus 转为可序列化的 plain 对象，便于 React 状态更新与 UI 展示。
 */
import { useState, useEffect } from "react";

/** PermissionStatus 的纯对象快照（name + state）。 */
export type PlainPermissionStatus = {
  readonly name: string;
  readonly state: PermissionState;
};

/**
 * @param name 要查询的权限名（如 "notifications"）
 * @returns 当前权限状态；查询完成前为 null
 */
export default function useQueryPermission(
  name: PermissionName,
): PlainPermissionStatus | null {
  const [status, setStatus] = useState<PermissionStatus | null>(null);
  const [plainStatus, setPlainStatus] = useState<PlainPermissionStatus | null>(
    null,
  );

  function updatePlainStatus(newStatus: PermissionStatus) {
    setPlainStatus({
      name: newStatus.name,
      state: newStatus.state,
    });
  }

  // 权限名变化时重新 query，并清空旧状态
  useEffect(() => {
    setStatus(null);
    setPlainStatus(null);

    void navigator.permissions.query({ name }).then((newStatus) => {
      setStatus(newStatus);
      updatePlainStatus(newStatus);
    });
  }, [name]);

  // 监听 PermissionStatus 的 change 事件，同步 plain 快照
  useEffect(() => {
    if (!status) {
      return;
    }

    function onStatusChange() {
      if (!status) {
        return;
      }

      updatePlainStatus(status);
    }

    status.addEventListener("change", onStatusChange);
    return () => status.removeEventListener("change", onStatusChange);
  }, [status]);

  return plainStatus;
}
