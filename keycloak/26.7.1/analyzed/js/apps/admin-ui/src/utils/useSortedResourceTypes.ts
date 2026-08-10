/**
 * 拉取客户端资源服务器授权 schema，并按 type 字段排序返回 resourceTypes 列表。
 * 用于 UMA/细粒度授权 UI 中的资源类型下拉与筛选。
 */
import ResourceServerRepresentation from "@keycloak/keycloak-admin-client/lib/defs/resourceServerRepresentation";
import { useMemo, useState } from "react";
import { useAdminClient } from "../admin-client";
import { useFetch } from "@keycloak/keycloak-ui-shared";
import { sortBy } from "lodash-es";

type UseSortedResourceTypesProps = {
  clientId: string;
};

export default function useSortedResourceTypes({
  clientId,
}: UseSortedResourceTypesProps) {
  const { adminClient } = useAdminClient();
  const [resourceServer, setResourceServer] =
    useState<ResourceServerRepresentation>();

  // clientId 变化时重新获取资源服务器配置
  useFetch(
    () =>
      adminClient.clients.getResourceServer({
        id: clientId,
      }),
    setResourceServer,
    [clientId],
  );

  const resourceTypes = useMemo(() => {
    const allResourceTypes = resourceServer?.authorizationSchema?.resourceTypes;
    return allResourceTypes
      ? sortBy(Object.values(allResourceTypes), "type")
      : [];
  }, [resourceServer]);

  return resourceTypes;
}
