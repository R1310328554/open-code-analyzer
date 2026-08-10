// use-explore-url-params.ts — Agent 探索页 URL 参数：canvasId、sessionId 与导航。

import { useCallback, useMemo } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router';

/** 解析/更新探索页路由：canvasId、sessionId、isNew 及 setCanvasId/setSessionId。 */
export const useExploreUrlParams = () => {
  const { id: canvasId } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  // 从 query 读取当前会话 id
  const sessionId = useMemo(
    () => searchParams.get('sessionId') || undefined,
    [searchParams],
  );

  // 标记是否为新建会话（?isNew=true）
  const isNew = useMemo(
    () => searchParams.get('isNew') || undefined,
    [searchParams],
  );

  /** 切换画布并跳转至 /agent/:id/explore。 */
  const setCanvasId = useCallback(
    (id: string) => {
      navigate(`/agent/${id}/explore`);
    },
    [navigate],
  );

  /** 在当前 canvas 下更新 sessionId，可选附带 isNew 标记。 */
  const setSessionId = useCallback(
    (id: string, isNewParam?: boolean) => {
      const params = new URLSearchParams();
      if (id) params.set('sessionId', id);
      if (isNewParam) params.set('isNew', 'true');
      navigate(
        `/agent/${canvasId}/explore${params.toString() ? `?${params}` : ''}`,
      );
    },
    [canvasId, navigate],
  );

  return {
    canvasId,
    sessionId,
    isNew,
    setCanvasId,
    setSessionId,
  };
};
