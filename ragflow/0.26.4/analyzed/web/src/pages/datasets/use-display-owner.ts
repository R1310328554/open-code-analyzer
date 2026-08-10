// use-display-owner.ts — 知识库列表所有者展示：当前租户自己的库不重复显示昵称。

import { useFetchTenantInfo } from '@/hooks/use-user-setting-request';
import { useCallback } from 'react';

/** 返回 getOwnerName：与当前 tenant_id 相同时返回 null 隐藏所有者列。 */
export function useDisplayOwnerName() {
  const { data } = useFetchTenantInfo();
  /** 非本租户的知识库才展示 nickname，本租户返回 null。 */
  const getOwnerName = useCallback(
    (tenantId: string, nickname: string) => {
      if (tenantId === data.tenant_id) {
        return null;
      }
      return nickname;
    },
    [data.tenant_id],
  );

  return getOwnerName;
}
