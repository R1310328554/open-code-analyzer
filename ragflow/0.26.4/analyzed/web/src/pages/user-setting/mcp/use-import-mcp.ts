/**
 * use-import-mcp.ts — 从 JSON 导入 MCP 配置（Zod 校验 mcpServers 结构）。
 */

import message from '@/components/ui/message';
import { FileMimeType } from '@/constants/common';
import { useSetModalState } from '@/hooks/common-hooks';
import { useImportMcpServer } from '@/hooks/use-mcp-request';
import { isEmpty } from 'lodash';
import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { z } from 'zod';

/** 单个 MCP 服务条目：type、url 必填，token 与 tool_configuration 可选。 */
const ServerEntrySchema = z.object({
  authorization_token: z.string().optional(),
  name: z.string().optional(),
  tool_configuration: z.object({}).passthrough().optional(),
  type: z.string(),
  url: z.string().url(),
});

/** 导入文件根结构：mcpServers 为 name -> ServerEntry 映射。 */
const McpConfigSchema = z.object({
  mcpServers: z.record(ServerEntrySchema),
});

/** 导入弹窗：仅接受 JSON，校验通过后调用 importMcpServer。 */
export const useImportMcp = () => {
  const {
    visible: importVisible,
    hideModal: hideImportModal,
    showModal: showImportModal,
  } = useSetModalState();
  const { t } = useTranslation();
  const { importMcpServer, loading } = useImportMcpServer();

  const onImportOk = useCallback(
    async ({ fileList }: { fileList: File[] }) => {
      if (fileList.length > 0) {
        const file = fileList[0];
        if (file.type !== FileMimeType.Json) {
          message.error(t('flow.jsonUploadTypeErrorMessage'));
          return;
        }

        const mcpStr = await file.text();
        const errorMessage = t('flow.jsonUploadContentErrorMessage');
        try {
          const mcp = JSON.parse(mcpStr);
          try {
            McpConfigSchema.parse(mcp);
          } catch (error) {
            console.error(error);
            message.error('Incorrect data format');
            return;
          }
          if (mcpStr && !isEmpty(mcp)) {
            const ret = await importMcpServer(mcp);
            if (ret.code === 0) {
              hideImportModal();
            }
          } else {
            message.error(errorMessage);
          }
        } catch (error) {
          console.error(error);
          message.error(errorMessage);
        }
      }
    },
    [hideImportModal, importMcpServer, t],
  );

  return {
    importVisible,
    showImportModal,
    hideImportModal,
    onImportOk,
    loading,
  };
};
