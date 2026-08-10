/**
 * use-export-mcp.ts — 将选中的 MCP 服务器配置导出为 JSON 文件。
 */

import message from '@/components/ui/message';
import { useExportMcpServer } from '@/hooks/use-mcp-request';
import { IMcpServer } from '@/interfaces/database/mcp';
import { downloadJsonFile } from '@/utils/file-util';
import i18n from '@/locales/config';
import { useCallback } from 'react';

/** 批量导出 MCP；文件名优先使用 mcp.name。 */
export function useExportMcp(mcp?: IMcpServer) {
  const { exportMcpServer } = useExportMcpServer();

  const handleExportMcpJson = useCallback(
    (ids: string[]) => async () => {
      if (ids.length === 0) {
        message.warning(i18n.t('mcp.noServerSelected'));
        return;
      }
      const data = await exportMcpServer(ids);
      if (data.code === 0) {
        downloadJsonFile(data.data, `${mcp?.name || 'mcp'}.json`);
      }
    },
    [exportMcpServer, mcp],
  );

  return {
    handleExportMcpJson,
  };
}
