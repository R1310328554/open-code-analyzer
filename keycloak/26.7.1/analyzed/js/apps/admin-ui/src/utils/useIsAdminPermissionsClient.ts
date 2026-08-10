/**
 * 判断所选客户端是否为领域「管理权限」专用客户端的 Hook。
 * 用于在客户端详情页区分 admin permissions 客户端与普通 OAuth 客户端。
 */
import { useState, useEffect } from "react";
import { useRealm } from "../context/realm-context/RealmContext";

/**
 * @param selectedClientId 当前页面选中的客户端 ID
 * @returns 若该客户端即 realm.adminPermissionsClient 则为 true
 */
export function useIsAdminPermissionsClient(selectedClientId: string) {
  const { realmRepresentation } = useRealm();
  const [isAdminPermissionsClient, setIsAdminPermissionsClient] =
    useState<boolean>(false);

  useEffect(() => {
    if (realmRepresentation.adminPermissionsClient) {
      // 领域已配置 admin permissions 客户端时，比对 ID
      setIsAdminPermissionsClient(
        selectedClientId === realmRepresentation.adminPermissionsClient.id,
      );
    } else {
      setIsAdminPermissionsClient(false);
    }
  }, [selectedClientId, realmRepresentation]);

  return isAdminPermissionsClient;
}
