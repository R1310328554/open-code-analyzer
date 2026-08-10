// use-select-derived-session-list.ts — 探索页会话列表：服务端同步与临时新建会话。

import { useFetchSessionsByCanvasId } from '@/hooks/use-agent-request';
import { IAgentLogResponse } from '@/interfaces/database/agent';
import { useCallback, useEffect, useState } from 'react';
import { useExploreUrlParams } from './use-explore-url-params';

/** 维护可编辑会话列表：拉取 canvas 会话、插入/移除临时会话。 */
export const useSelectDerivedSessionList = () => {
  const [list, setList] = useState<
    Array<IAgentLogResponse & { is_new?: boolean }>
  >([]);

  const { data: sessions = [], loading } = useFetchSessionsByCanvasId();

  const { setSessionId } = useExploreUrlParams();

  /** 在列表头部插入空 id 的临时会话并导航至 isNew 状态。 */
  const addTemporarySession = useCallback(() => {
    const now = Date.now() / 1000;

    const tempSession: IAgentLogResponse & { is_new?: boolean } = {
      id: '',
      message: [],
      create_date: '',
      create_time: now,
      update_date: '',
      update_time: now,
      round: 0,
      thumb_up: 0,
      errors: '',
      source: '',
      user_id: '',
      dsl: '',
      reference: {},
      is_new: true,
    };

    setList([tempSession, ...sessions]);

    setSessionId('', true);
  }, [sessions, setSessionId]);

  /** 按 sessionId 从本地列表移除会话（含取消临时会话）。 */
  const removeTemporarySession = useCallback((sessionId: string) => {
    setList((prevList) => {
      return prevList.filter((session) => session.id !== sessionId);
    });
  }, []);

  // 服务端会话变更时同步到本地 list
  useEffect(() => {
    setList(sessions);
  }, [sessions]);

  return {
    sessions: list,
    loading,
    addTemporarySession,
    removeTemporarySession,
  };
};
