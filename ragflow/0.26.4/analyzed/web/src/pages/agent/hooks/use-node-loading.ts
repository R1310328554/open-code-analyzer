// use-node-loading.ts — 节点执行态追踪：从 SSE 事件流推导进行中/已完成节点 ID。

import {
  INodeData,
  INodeEvent,
  MessageEventType,
} from '@/hooks/use-send-message';
import { IMessage } from '@/interfaces/database/chat';
import { useCallback, useMemo, useState } from 'react';

/** 基于最后一条消息的 NodeStarted/NodeFinished 事件计算加载中的组件。 */
export const useNodeLoading = ({
  currentEventListWithoutMessageById,
}: {
  currentEventListWithoutMessageById: (messageId: string) => INodeEvent[];
}) => {
  const [derivedMessages, setDerivedMessages] = useState<IMessage[]>();

  // 取 derivedMessages 最后一条消息的 id 作为事件关联键
  const lastMessageId = useMemo(() => {
    return derivedMessages?.[derivedMessages?.length - 1]?.id;
  }, [derivedMessages]);

  const currentEventListWithoutMessage = useMemo(() => {
    if (!lastMessageId) {
      return [];
    }
    return currentEventListWithoutMessageById(lastMessageId);
  }, [currentEventListWithoutMessageById, lastMessageId]);

  const startedNodeList = useMemo(() => {
    const duplicateList = currentEventListWithoutMessage?.filter(
      (x) => x.event === MessageEventType.NodeStarted,
    ) as INodeEvent[];

    // 同一 component_id 仅保留首次 NodeStarted 事件
    // Remove duplicate nodes
    return duplicateList?.reduce<Array<INodeEvent>>((pre, cur) => {
      if (pre.every((x) => x.data.component_id !== cur.data.component_id)) {
        pre.push(cur);
      }
      return pre;
    }, []);
  }, [currentEventListWithoutMessage]);

  /** 收集当前消息下所有 NodeFinished 事件的 data 列表。 */
  const filterFinishedNodeList = useCallback(() => {
    const nodeEventList = currentEventListWithoutMessage
      .filter(
        (x) => x.event === MessageEventType.NodeFinished,
        // x.event === MessageEventType.NodeFinished &&
        // (x.data as INodeData)?.component_id === componentId,
      )
      .map((x) => x.data);

    return nodeEventList;
  }, [currentEventListWithoutMessage]);

  const lastNode = useMemo(() => {
    if (!startedNodeList) {
      return null;
    }
    return startedNodeList[startedNodeList.length - 1];
  }, [startedNodeList]);

  const startNodeIds = useMemo(() => {
    if (!startedNodeList) {
      return [];
    }
    return startedNodeList.map((x) => x.data.component_id);
  }, [startedNodeList]);

  const finishNodeIds = useMemo(() => {
    if (!lastNode) {
      return [];
    }
    const nodeDataList = filterFinishedNodeList();
    const finishNodeIdsTemp = nodeDataList.map(
      (x: INodeData) => x.component_id,
    );
    return Array.from(new Set(finishNodeIdsTemp));
  }, [lastNode, filterFinishedNodeList]);

  /** 已启动但尚未收到 NodeFinished 的 component_id 集合。 */
  const startButNotFinishedNodeIds = useMemo(() => {
    return startNodeIds.filter((x) => !finishNodeIds.includes(x));
  }, [finishNodeIds, startNodeIds]);

  return {
    lastNode,
    startButNotFinishedNodeIds,
    filterFinishedNodeList,
    setDerivedMessages,
  };
};
