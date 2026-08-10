/**
 * use-edit-mcp.ts — MCP 服务器新建/编辑弹窗状态与提交逻辑。
 */

import { useSetModalState } from '@/hooks/common-hooks';
import {
  useCreateMcpServer,
  useUpdateMcpServer,
} from '@/hooks/use-mcp-request';
import { useCallback, useState } from 'react';

/** 管理编辑弹窗；id 为空时 create，否则 updateMcpServer。 */
export const useEditMcp = () => {
  const {
    visible: editVisible,
    hideModal: hideEditModal,
    showModal: showEditModal,
  } = useSetModalState();
  const { createMcpServer, loading } = useCreateMcpServer();
  const [id, setId] = useState('');

  const { updateMcpServer, loading: updateLoading } = useUpdateMcpServer();

  const handleShowModal = useCallback(
    (id: string) => () => {
      setId(id);
      showEditModal();
    },
    [setId, showEditModal],
  );

  const handleOk = useCallback(
    async (values: any) => {
      let code;
      if (id) {
        code = await updateMcpServer({ ...values, mcp_id: id });
      } else {
        code = await createMcpServer(values);
      }
      if (code === 0) {
        hideEditModal();
      }
    },
    [createMcpServer, hideEditModal, id, updateMcpServer],
  );

  return {
    editVisible,
    hideEditModal,
    showEditModal: handleShowModal,
    loading: loading || updateLoading,
    createMcpServer,
    handleOk,
    id,
  };
};

/** useEditMcp 返回值类型，供设置页子组件引用。 */
export type UseEditMcpReturnType = ReturnType<typeof useEditMcp>;
