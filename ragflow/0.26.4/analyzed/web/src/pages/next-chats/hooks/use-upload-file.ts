// use-upload-file.ts — 聊天附件上传：isNew 时先建会话，维护 files 与 fileMap。

import { FileUploadProps } from '@/components/file-upload';
import {
  useGetChatSearchParams,
  useUploadAndParseFile,
} from '@/hooks/use-chat-request';
import { useCallback, useState } from 'react';
import { useChatUrlParams } from './use-chat-url';
import { useSetConversation } from './use-set-conversation';

/** 管理聊天输入区文件上传、解析结果列表及移除逻辑。 */
export function useUploadFile() {
  const { uploadAndParseFile, loading, cancel } = useUploadAndParseFile();
  const [currentFiles, setCurrentFiles] = useState<Record<string, any>[]>([]);
  const [fileMap, setFileMap] = useState<Map<File, Record<string, any>>>(
    new Map(),
  );
  const { setConversation } = useSetConversation();
  const { conversationId, isNew } = useGetChatSearchParams();
  const { setConversationBoth } = useChatUrlParams();

  type FileUploadParameters = Parameters<
    NonNullable<FileUploadProps['onUpload']>
  >;

  /** 逐文件 uploadAndParseFile，成功则写入 currentFiles 与 fileMap。 */
  const handleUploadFile = useCallback(
    async (
      files: FileUploadParameters[0],
      options: FileUploadParameters[1],
      conversationId?: string,
    ) => {
      if (Array.isArray(files) && files.length) {
        for (const file of files) {
          const ret = await uploadAndParseFile({
            file,
            options,
            conversationId,
          });
          if (ret?.code === 0) {
            const data = ret.data;
            setCurrentFiles((list) => [...list, data]);
            setFileMap((map) => {
              map.set(file, data);
              return map;
            });
          }
        }
      }
    },
    [uploadAndParseFile],
  );

  /** 对外暴露的上传入口：无会话时先用文件名创建 conversation。 */
  const createConversationBeforeUploadFile: NonNullable<
    FileUploadProps['onUpload']
  > = useCallback(
    async (files, options) => {
      if (
        (conversationId === '' || isNew === 'true') &&
        Array.isArray(files) &&
        files.length
      ) {
        const data = await setConversation(files[0].name);
        if (data?.code === 0) {
          const backendConvId = data.data.id;
          setConversationBoth(backendConvId, '');
          handleUploadFile(files, options, backendConvId);
        }
      } else {
        handleUploadFile(files, options);
      }
    },
    [
      conversationId,
      handleUploadFile,
      isNew,
      setConversation,
      setConversationBoth,
    ],
  );

  /** 发送消息后清空已选附件。 */
  const clearFiles = useCallback(() => {
    setCurrentFiles([]);
    setFileMap(new Map());
  }, []);

  /** 移除单个附件；上传进行中则 cancel 当前请求。 */
  const removeFile = useCallback(
    (file: File) => {
      if (loading) {
        cancel();
        return;
      }
      const id = fileMap.get(file);
      if (id) {
        setCurrentFiles((list) => list.filter((item) => item !== id));
      }
    },
    [cancel, fileMap, loading],
  );

  return {
    handleUploadFile: createConversationBeforeUploadFile,
    files: currentFiles,
    isUploading: loading,
    removeFile,
    clearFiles: clearFiles,
  };
}
