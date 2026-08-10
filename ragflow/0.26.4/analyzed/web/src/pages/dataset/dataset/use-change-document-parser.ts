// use-change-document-parser.ts — 单文档切换解析器/流水线弹窗与 setDocumentParser 提交。

import { useSetModalState } from '@/hooks/common-hooks';
import { useSetDocumentParser } from '@/hooks/use-document-request';
import { IDocumentInfo } from '@/interfaces/database/document';
import { IChangeParserRequestBody } from '@/interfaces/request/document';
import { useCallback, useState } from 'react';

/** 维护待修改文档 record，打开弹窗并在确认后调用 setDocumentParser。 */
export const useChangeDocumentParser = () => {
  const { setDocumentParser, loading } = useSetDocumentParser();
  const [record, setRecord] = useState<IDocumentInfo>({} as IDocumentInfo);

  const {
    visible: changeParserVisible,
    hideModal: hideChangeParserModal,
    showModal: showChangeParserModal,
  } = useSetModalState();

  /** 携带 parser_id、pipeline_id 与 parser_config 更新单文档解析配置。 */
  const onChangeParserOk = useCallback(
    async (parserConfigInfo: IChangeParserRequestBody) => {
      if (record?.id && record?.dataset_id) {
        const ret = await setDocumentParser({
          parserId: parserConfigInfo.parser_id,
          pipelineId: parserConfigInfo.pipeline_id || '',
          documentId: record?.id,
          datasetId: record?.dataset_id,
          parserConfig: parserConfigInfo.parser_config,
        });
        if (ret === 0) {
          hideChangeParserModal();
        }
      }
    },
    [record?.id, record?.dataset_id, setDocumentParser, hideChangeParserModal],
  );

  const handleShowChangeParserModal = useCallback(
    (row: IDocumentInfo) => {
      setRecord(row);
      showChangeParserModal();
    },
    [showChangeParserModal],
  );

  return {
    changeParserLoading: loading,
    onChangeParserOk,
    changeParserVisible,
    hideChangeParserModal,
    showChangeParserModal: handleShowChangeParserModal,
    changeParserRecord: record,
  };
};

/** 仅需 showChangeParserModal 的组件可引用此类型。 */
export type UseChangeDocumentParserShowType = Pick<
  ReturnType<typeof useChangeDocumentParser>,
  'showChangeParserModal'
>;
