// use-rename-agent.ts — Agent 重命名弹窗：读取当前记录并调用 updateAgentSetting。

import { useSetModalState } from '@/hooks/common-hooks';
import { useUpdateAgentSetting } from '@/hooks/use-agent-request';
import { IFlow } from '@/interfaces/database/agent';
import { pick } from 'lodash';
import { useCallback, useState } from 'react';

/** 维护待重命名 Agent 状态与弹窗显隐，成功后关闭弹窗。 */
export const useRenameAgent = () => {
  const [agent, setAgent] = useState<IFlow>({} as IFlow);
  const {
    visible: agentRenameVisible,
    hideModal: hideAgentRenameModal,
    showModal: showAgentRenameModal,
  } = useSetModalState();
  const { updateAgentSetting, loading } = useUpdateAgentSetting();

  /** 仅更新 title，其余 id/avatar/description/permission 原样提交。 */
  const onAgentRenameOk = useCallback(
    async (name: string) => {
      const ret = await updateAgentSetting({
        ...pick(agent, ['id', 'avatar', 'description', 'permission']),
        title: name,
      });

      if (ret === 0) {
        hideAgentRenameModal();
      }
    },
    [updateAgentSetting, agent, hideAgentRenameModal],
  );

  /** 打开弹窗前将列表行 record 写入本地 state 作为初始名称。 */
  const handleShowAgentRenameModal = useCallback(
    async (record: IFlow) => {
      setAgent(record);
      showAgentRenameModal();
    },
    [showAgentRenameModal],
  );

  return {
    agentRenameLoading: loading,
    initialAgentName: agent?.title,
    onAgentRenameOk,
    agentRenameVisible,
    hideAgentRenameModal,
    showAgentRenameModal: handleShowAgentRenameModal,
  };
};
