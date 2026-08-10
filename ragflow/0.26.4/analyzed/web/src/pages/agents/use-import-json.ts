// use-import-json.ts — 从 JSON 文件导入 Agent/Dataflow DSL 并创建新画布。

import { useToast } from '@/components/hooks/use-toast';
import message from '@/components/ui/message';
import { AgentCategory } from '@/constants/agent';
import { FileMimeType } from '@/constants/common';
import { useSetModalState } from '@/hooks/common-hooks';
import { useSetAgent } from '@/hooks/use-agent-request';
import * as bridge from '@/pages/agent/utils/dsl-bridge';
import { inferIsAgentFromImport } from '@/pages/agent/utils/dsl-bridge';
import isEmpty from 'lodash/isEmpty';
import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { FormSchemaType } from './upload-agent-dialog/upload-agent-form';

/** 管理上传弹窗、文件校验、DSL 解析与 setAgent 创建流程。 */
export const useHandleImportJsonFile = () => {
  const {
    visible: fileUploadVisible,
    hideModal: hideFileUploadModal,
    showModal: showFileUploadModal,
  } = useSetModalState();
  const { t } = useTranslation();
  const { toast } = useToast();
  const { loading, setAgent } = useSetAgent();

  /** 校验 MIME 为 JSON，解析后经 dsl-bridge 转为内部 DSL 并提交。 */
  const onFileUploadOk = useCallback(
    async ({ fileList, name }: FormSchemaType) => {
      if (fileList.length > 0) {
        const file = fileList[0];
        // 非 JSON 类型直接 toast 提示
        if (file.type !== FileMimeType.Json) {
          toast({ title: t('flow.jsonUploadTypeErrorMessage') });
          return;
        }

        const graphOrDslStr = await file.text();
        const errorMessage = t('flow.jsonUploadContentErrorMessage');
        try {
          const rawParsed = JSON.parse(graphOrDslStr);
          if (graphOrDslStr && !isEmpty(rawParsed)) {
            // 根据导入结构推断 Agent 画布或 Dataflow 画布
            const isAgent = inferIsAgentFromImport(rawParsed);
            const dsl = bridge.importDsl(rawParsed, isAgent);
            setAgent({
              title: name,
              dsl,
              canvas_category: isAgent
                ? AgentCategory.AgentCanvas
                : AgentCategory.DataflowCanvas,
            });
            hideFileUploadModal();
          } else {
            message.error(errorMessage);
          }
        } catch {
          message.error(errorMessage);
        }
      }
    },
    [hideFileUploadModal, setAgent, t, toast],
  );

  return {
    fileUploadVisible,
    handleImportJson: showFileUploadModal,
    hideFileUploadModal,
    onFileUploadOk,
    loading,
  };
};
