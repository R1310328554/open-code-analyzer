// hooks.ts — 文件管理页通用 hook：文件夹 ID、行选择、重命名、关联知识库与面包屑导航。

import { useSetModalState } from '@/hooks/common-hooks';
import { useConnectToKnowledge, useRenameFile } from '@/hooks/use-file-request';
import { TableRowSelection } from '@/interfaces/antd-compat';
import { IFile } from '@/interfaces/database/file-manager';
import { useCallback, useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

/** 从 URL searchParams 读取当前 folderId，缺省为空字符串。 */
export const useGetFolderId = () => {
  const [searchParams] = useSearchParams();
  const id = searchParams.get('folderId') as string;

  return id ?? '';
};

/** 表格多选状态；来源为 knowledgebase 的行禁用勾选。 */
export const useGetRowSelection = () => {
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

  const rowSelection: TableRowSelection<IFile> = {
    selectedRowKeys,
    getCheckboxProps: (record) => {
      return { disabled: record.source_type === 'knowledgebase' };
    },
    onChange: (newSelectedRowKeys: React.Key[]) => {
      setSelectedRowKeys(newSelectedRowKeys);
    },
  };

  return { rowSelection, setSelectedRowKeys };
};

/** 单文件重命名弹窗：维护 file record 并调用 renameFile。 */
export const useRenameCurrentFile = () => {
  const [file, setFile] = useState<IFile>({} as IFile);
  const {
    visible: fileRenameVisible,
    hideModal: hideFileRenameModal,
    showModal: showFileRenameModal,
  } = useSetModalState();
  const { renameFile, loading } = useRenameFile();

  const onFileRenameOk = useCallback(
    async (name: string) => {
      const ret = await renameFile({
        fileId: file.id,
        name,
      });

      if (ret === 0) {
        hideFileRenameModal();
      }
    },
    [renameFile, file, hideFileRenameModal],
  );

  const handleShowFileRenameModal = useCallback(
    async (record: IFile) => {
      setFile(record);
      showFileRenameModal();
    },
    [showFileRenameModal],
  );

  return {
    fileRenameLoading: loading,
    initialFileName: file.name,
    onFileRenameOk,
    fileRenameVisible,
    hideFileRenameModal,
    showFileRenameModal: handleShowFileRenameModal,
  };
};

/** useRenameCurrentFile 的完整返回类型别名。 */
export type UseRenameCurrentFileReturnType = ReturnType<
  typeof useRenameCurrentFile
>;

/** 将文件（单个或批量）关联至一个或多个知识库的弹窗逻辑。 */
export const useHandleConnectToKnowledge = () => {
  const {
    visible: connectToKnowledgeVisible,
    hideModal: hideConnectToKnowledgeModal,
    showModal: showConnectToKnowledgeModal,
  } = useSetModalState();
  const { connectFileToKnowledge: connectToKnowledge, loading } =
    useConnectToKnowledge();
  const [record, setRecord] = useState<IFile>({} as IFile);
  const [documentIds, setDocumentIds] = useState<string[]>([]);

  // 已关联知识库 ID 列表，供弹窗多选初始值
  const initialValue = useMemo(() => {
    return Array.isArray(record?.kbs_info)
      ? record?.kbs_info?.map((x) => x.kb_id)
      : [];
  }, [record?.kbs_info]);

  const onConnectToKnowledgeOk = useCallback(
    async (knowledgeIds: string[]) => {
      const ret = await connectToKnowledge({
        fileIds: documentIds,
        kbIds: knowledgeIds,
      });

      if (ret === 0) {
        hideConnectToKnowledgeModal();
      }
      return ret;
    },
    [connectToKnowledge, hideConnectToKnowledgeModal, documentIds],
  );

  /** 支持传入 IFile 或 fileId 数组，统一设置 documentIds 后打开弹窗。 */
  const handleShowConnectToKnowledgeModal = useCallback(
    (documents: IFile | string[]) => {
      if (Array.isArray(documents)) {
        setDocumentIds(documents);
        setRecord({} as IFile);
      } else {
        setRecord(documents);
        setDocumentIds([documents.id]);
      }

      showConnectToKnowledgeModal();
    },
    [showConnectToKnowledgeModal],
  );

  return {
    initialConnectedIds: initialValue,
    connectToKnowledgeLoading: loading,
    onConnectToKnowledgeOk,
    connectToKnowledgeVisible,
    hideConnectToKnowledgeModal,
    showConnectToKnowledgeModal: handleShowConnectToKnowledgeModal,
  };
};

/** useHandleConnectToKnowledge 的完整返回类型别名。 */
export type UseHandleConnectToKnowledgeReturnType = ReturnType<
  typeof useHandleConnectToKnowledge
>;

/** 面包屑 path 点击时调用 react-router navigate。 */
export const useHandleBreadcrumbClick = () => {
  const navigate = useNavigate();

  const handleBreadcrumbClick = useCallback(
    (path?: string) => {
      if (path) {
        navigate(path);
      }
    },
    [navigate],
  );

  return { handleBreadcrumbClick };
};
