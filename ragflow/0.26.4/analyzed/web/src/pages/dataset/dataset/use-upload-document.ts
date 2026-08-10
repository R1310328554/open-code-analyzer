// use-upload-document.ts — 文档上传弹窗：支持表格列角色配置、部分成功与创建后自动解析。

import { UploadFormSchemaType } from '@/components/file-upload-dialog';
import { useSetModalState } from '@/hooks/common-hooks';
import {
  useRunDocument,
  useUploadDocument,
} from '@/hooks/use-document-request';
import { getUnSupportedFilesCount } from '@/utils/document-util';
import { useCallback } from 'react';

/** 封装 uploadDocument 与可选的 parseOnCreation 自动 runDocumentByIds。 */
export const useHandleUploadDocument = () => {
  const {
    visible: documentUploadVisible,
    hideModal: hideDocumentUploadModal,
    showModal: showDocumentUploadModal,
  } = useSetModalState();
  const { uploadDocument, loading } = useUploadDocument();
  const { runDocumentByIds } = useRunDocument();

  const onDocumentUploadOk = useCallback(
    async ({
      fileList,
      parseOnCreation,
      tableColumnMode,
      tableColumnRoles,
    }: UploadFormSchemaType) => {
      if (fileList.length > 0) {
        // 手动表格列模式时组装 parser_config 传给上传接口
        // Build parser_config if column roles are configured
        let parserConfig: Record<string, any> | undefined;
        if (
          tableColumnMode === 'manual' &&
          tableColumnRoles &&
          Object.keys(tableColumnRoles).length > 0
        ) {
          parserConfig = {
            table_column_mode: 'manual',
            table_column_roles: tableColumnRoles,
          };
        }

        const ret = await uploadDocument(fileList as File[], parserConfig);

        // code 0 全成功；code 500 且带 message 时可能部分文件已入库
        // Check for success (code === 0) or partial success (code === 500 with some files)
        const isSuccess = ret?.code === 0;
        const isPartialSuccess = ret?.code === 500 && ret?.message;

        if (!isSuccess && !isPartialSuccess) {
          return;
        }

        // 勾选「创建后解析」时，对已入库文档 ID 批量启动解析
        // Trigger parsing for both full and partial success when parseOnCreation is enabled
        if (
          (isSuccess || isPartialSuccess) &&
          parseOnCreation &&
          ret.data?.length > 0
        ) {
          runDocumentByIds({
            documentIds: ret.data.map((x: any) => x.id),
            run: 1,
          });
        }

        if (isSuccess) {
          hideDocumentUploadModal();
          return 0;
        }

        // 部分失败时若至少有一个文件成功则仍关闭弹窗
        // For partial success (code 500), check if any files were uploaded
        const count = getUnSupportedFilesCount(ret?.message);
        if (count !== fileList.length) {
          hideDocumentUploadModal();
          return 0;
        }

        return ret?.code;
      }
    },
    [uploadDocument, runDocumentByIds, hideDocumentUploadModal],
  );

  return {
    documentUploadLoading: loading,
    onDocumentUploadOk,
    documentUploadVisible,
    hideDocumentUploadModal,
    showDocumentUploadModal,
  };
};
