// use-change-node-name.ts — 画布节点与 Agent 工具重命名：失焦校验、重名拦截与写回 store。

import message from '@/components/ui/message';
import { trim } from 'lodash';
import {
  ChangeEvent,
  Dispatch,
  SetStateAction,
  useCallback,
  useEffect,
  useState,
} from 'react';
import { Operator } from '../constant';
import useGraphStore from '../store';
import { getAgentNodeTools } from '../utils';

/** Agent 子工具节点改名：校验非空、同 Agent 内不重名后 updateAgentToolById。 */
export function useHandleToolNodeNameChange({
  id,
  name,
  setName,
}: {
  id?: string;
  name?: string;
  setName: Dispatch<SetStateAction<string>>;
}) {
  const {
    clickedToolId,
    findUpstreamNodeById,
    getAgentToolById,
    updateAgentToolById,
  } = useGraphStore((state) => state);
  const agentNode = findUpstreamNodeById(id)!;
  const tools = getAgentNodeTools(agentNode);
  const previousName = getAgentToolById(clickedToolId, agentNode)?.name;

  const handleToolNameBlur = useCallback(() => {
    const trimmedName = trim(name);
    const existsSameName = tools.some((x) => x.name === trimmedName);

    // 空名则恢复 previousName
    // Not changed
    if (trimmedName === '') {
      setName(previousName || '');
      return true;
    }

    if (existsSameName && previousName !== name) {
      message.error('The name cannot be repeated');
      return false;
    }

    if (agentNode?.id) {
      updateAgentToolById(agentNode, clickedToolId, { name });
    }

    return true;
  }, [
    agentNode,
    clickedToolId,
    name,
    previousName,
    setName,
    tools,
    updateAgentToolById,
  ]);

  return { handleToolNameBlur, previousToolName: previousName };
}

/** 通用节点改名 Hook：Tool 节点走工具逻辑，其余走 updateNodeName。 */
export const useHandleNodeNameChange = ({
  id,
  data,
}: {
  id?: string;
  data: any;
}) => {
  const [name, setName] = useState<string>('');
  const { updateNodeName, nodes, getOperatorTypeFromId } = useGraphStore(
    (state) => state,
  );
  const previousName = data?.name;
  const isToolNode = getOperatorTypeFromId(id) === Operator.Tool;

  const { handleToolNameBlur, previousToolName } = useHandleToolNodeNameChange({
    id,
    name,
    setName,
  });

  const handleNameBlur = useCallback(() => {
    const trimmedName = trim(name);
    const existsSameName = nodes.some((x) => x.data.name === name);

    // 空名则恢复 previousName
    // Not changed
    if (!trimmedName) {
      setName(previousName || '');
      return true;
    }

    if (existsSameName && previousName !== name) {
      message.error('The name cannot be repeated');
      return false;
    }

    if (id) {
      updateNodeName(id, name);
    }

    return true;
  }, [name, id, updateNodeName, previousName, nodes]);

  /** 受控输入：同步本地 name 状态。 */
  const handleNameChange = useCallback((e: ChangeEvent<HTMLInputElement>) => {
    setName(e.target.value);
  }, []);

  /** 外部 name 变更时，Tool 节点同步 previousToolName，否则同步 previousName。 */
  useEffect(() => {
    setName(isToolNode ? previousToolName : previousName);
  }, [isToolNode, previousName, previousToolName]);

  return {
    name,
    handleNameBlur: isToolNode ? handleToolNameBlur : handleNameBlur,
    handleNameChange,
  };
};
