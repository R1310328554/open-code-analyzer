/** 获取当前登录管理员的 UserRepresentation 与所在 realm。 */
import UserRepresentation from "@keycloak/keycloak-admin-client/lib/defs/userRepresentation";
import { useFetch } from "@keycloak/keycloak-ui-shared";
import { useState } from "react";
import { useAdminClient } from "../admin-client";
import { useWhoAmI } from "../context/whoami/WhoAmI";

/** 根据 WhoAmI 中的 userId 拉取完整用户详情，并附带 realm 名。 */
export function useCurrentUser() {
  const { adminClient } = useAdminClient();
  const { whoAmI } = useWhoAmI();
  const [currentUser, setCurrentUser] = useState<UserRepresentation>();

  useFetch(
    () => adminClient.users.findOne({ id: whoAmI.userId }),
    setCurrentUser,
    [whoAmI.userId],
  );

  return { ...currentUser, realm: whoAmI.realm };
}
