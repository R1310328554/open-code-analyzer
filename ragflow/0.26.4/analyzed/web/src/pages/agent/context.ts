// context.ts — Agent 画布与聊天相关 React Context：表单节点、实例、日志与 Handle 拖拽。

import { INodeEvent } from '@/hooks/use-send-message';
import { RAGFlowNodeType } from '@/interfaces/database/agent';
import { IMessage } from '@/interfaces/database/chat';
import { HandleType, Position } from '@xyflow/react';
import { Dispatch, SetStateAction, createContext } from 'react';
import { useAddNode } from './hooks/use-add-node';
import { useCacheChatLog } from './hooks/use-cache-chat-log';
import { useShowFormDrawer, useShowLogSheet } from './hooks/use-show-drawer';

/** 当前表单抽屉正在编辑的画布节点（供子表单读取 DSL）。 */
export const AgentFormContext = createContext<RAGFlowNodeType | undefined>(
  undefined,
);

type AgentInstanceContextType = Pick<
  ReturnType<typeof useAddNode>,
  'addCanvasNode'
> &
  Pick<ReturnType<typeof useShowFormDrawer>, 'showFormDrawer'> & {
    lastNode?: INodeEvent | null;
    currentSendLoading?: boolean;
    startButNotFinishedNodeIds?: string[];
  };

/** 画布实例级能力：加节点、表单抽屉、运行中节点与发送 loading 状态。 */
export const AgentInstanceContext = createContext<AgentInstanceContextType>(
  {} as AgentInstanceContextType,
);

type AgentChatContextType = Pick<
  ReturnType<typeof useShowLogSheet>,
  'showLogSheet'
> & {
  setLastSendLoadingFunc: (loading: boolean, messageId: string) => void;
  setDerivedMessages: Dispatch<SetStateAction<IMessage[] | undefined>>;
};

/** 聊天 UI 上下文：日志 Sheet 与 derivedMessages 写入回调。 */
export const AgentChatContext = createContext<AgentChatContextType>(
  {} as AgentChatContextType,
);

type AgentChatLogContextType = Pick<
  ReturnType<typeof useCacheChatLog>,
  'addEventList' | 'setCurrentMessageId'
>;

/** SSE 事件日志缓存：addEventList 与当前 messageId 切换。 */
export const AgentChatLogContext = createContext<AgentChatLogContextType>(
  {} as AgentChatLogContextType,
);

export type HandleContextType = {
  nodeId?: string;
  id?: string;
  type: HandleType;
  position: Position;
  isFromConnectionDrag: boolean;
};

/** 连接拖拽时当前 Handle 的 nodeId、类型与位置信息。 */
export const HandleContext = createContext<HandleContextType>(
  {} as HandleContextType,
);
