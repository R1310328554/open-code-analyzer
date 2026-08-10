// use-file-request.ts — 文件管理器：上传、移动、文件夹、下载与关联知识库 Hooks。

import message from '@/components/ui/message';
import { PaginationProps } from '@/interfaces/antd-compat';
import {
  IFetchFileListResult,
  IFolder,
} from '@/interfaces/database/file-manager';
import { IConnectRequestBody } from '@/interfaces/request/file-manager';
import fileManagerService from '@/services/file-manager-service';
import { downloadFileFromBlob } from '@/utils/file-util';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useDebounce } from 'ahooks';
import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useSearchParams } from 'react-router';
import {
  useGetPaginationWithRouter,
  useHandleSearchChange,
} from './logic-hooks';
import { useSetPaginationParams } from './route-hook';

/** 文件管理 React Query 缓存键枚举。 */
export const enum FileApiAction {
  UploadFile = 'uploadFile',
  FetchFileList = 'fetchFileList',
  MoveFile = 'moveFile',
  CreateFolder = 'createFolder',
  FetchParentFolderList = 'fetchParentFolderList',
  DeleteFile = 'deleteFile',
  DownloadFile = 'downloadFile',
  RenameFile = 'renameFile',
  ConnectFileToKnowledge = 'connectFileToKnowledge',
  FetchPureFileList = 'fetchPureFileList',
}

/** 从 URL searchParams 读取当前 folderId。 */
export const useGetFolderId = () => {
  const [searchParams] = useSearchParams();
  const id = searchParams.get('folderId') as string;

  return id ?? '';
};

/** 上传文件到指定 parentId，支持 webkitRelativePath 目录结构。 */
export const useUploadFile = () => {
  const { setPaginationParams } = useSetPaginationParams();
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [FileApiAction.UploadFile],
    mutationFn: async (params: { fileList: File[]; parentId: string }) => {
      const fileList = params.fileList;
      const pathList = params.fileList.map(
        (file) => (file as any).webkitRelativePath,
      );
      const formData = new FormData();
      formData.append('parent_id', params.parentId);
      fileList.forEach((file: any, index: number) => {
        formData.append('file', file);
        formData.append('path', pathList[index]);
      });
      try {
        const ret = await fileManagerService.uploadFile(formData);
        if (ret?.data.code === 0) {
          message.success(t('message.uploaded'));
          setPaginationParams(1);
          queryClient.invalidateQueries({
            queryKey: [FileApiAction.FetchFileList],
          });
        }
        return ret?.data?.code;
      } catch {
        return;
      }
    },
  });

  return { data, loading, uploadFile: mutateAsync };
};

/** 移动/重命名文件请求体。 */
export interface IMoveFileBody {
  src_file_ids: string[];
  dest_file_id?: string;
  new_name?: string;
}

/** 批量移动文件到目标文件夹。 */
export const useMoveFile = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [FileApiAction.MoveFile],
    mutationFn: async (params: IMoveFileBody) => {
      const { data } = await fileManagerService.moveFile(params);
      if (data.code === 0) {
        message.success(t('message.operated'));
        queryClient.invalidateQueries({
          queryKey: [FileApiAction.FetchFileList],
        });
      }
      return data.code;
    },
  });

  return { data, loading, moveFile: mutateAsync };
};

/** 在 parentId 下创建文件夹。 */
export const useCreateFolder = () => {
  const { setPaginationParams } = useSetPaginationParams();
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [FileApiAction.CreateFolder],
    mutationFn: async (params: { parentId: string; name: string }) => {
      const { data } = await fileManagerService.createFolder({
        name: params.name,
        parent_id: params.parentId,
        type: 'folder',
      });
      if (data.code === 0) {
        message.success(t('message.created'));
        setPaginationParams(1);
        queryClient.invalidateQueries({
          queryKey: [FileApiAction.FetchFileList],
        });
      }
      return data.code;
    },
  });

  return { data, loading, createFolder: mutateAsync };
};

/** 获取当前文件夹的祖先路径（面包屑）。 */
export const useFetchParentFolderList = () => {
  const id = useGetFolderId();
  const { data } = useQuery<IFolder[]>({
    queryKey: [FileApiAction.FetchParentFolderList, id],
    initialData: [],
    enabled: !!id,
    queryFn: async () => {
      const { data } = await fileManagerService.getAllParentFolder(
        {},
        `${id}/ancestors`,
      );

      return data?.data?.parent_folders?.toReversed() ?? [];
    },
  });

  return data;
};

/** 文件列表 Hook 返回的搜索与分页字段类型。 */
export interface IListResult {
  searchString: string;
  handleInputChange: React.ChangeEventHandler<HTMLInputElement>;
  pagination: PaginationProps;
  setPagination: (pagination: { page: number; pageSize: number }) => void;
  loading: boolean;
}

/** 分页拉取当前文件夹下的文件/子文件夹列表。 */
export const useFetchFileList = () => {
  const { searchString, handleInputChange } = useHandleSearchChange();
  const { pagination, setPagination } = useGetPaginationWithRouter();
  const id = useGetFolderId();
  const debouncedSearchString = useDebounce(searchString, { wait: 500 });

  const { data, isFetching: loading } = useQuery<IFetchFileListResult>({
    queryKey: [
      FileApiAction.FetchFileList,
      {
        id,
        debouncedSearchString,
        ...pagination,
      },
    ],
    initialData: { files: [], parent_folder: {} as IFolder, total: 0 },
    gcTime: 0,
    queryFn: async () => {
      const { data } = await fileManagerService.listFile({
        parent_id: id,
        keywords: debouncedSearchString,
        page_size: pagination.pageSize,
        page: pagination.current,
      });

      return data?.data;
    },
  });

  const onInputChange: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (e) => {
      setPagination({ page: 1 });
      handleInputChange(e);
    },
    [handleInputChange, setPagination],
  );

  return {
    ...data,
    searchString,
    handleInputChange: onInputChange,
    pagination: { ...pagination, total: data?.total },
    setPagination,
    loading,
  };
};

/** 批量删除文件。 */
export const useDeleteFile = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [FileApiAction.DeleteFile],
    mutationFn: async (params: { fileIds: string[]; parentId: string }) => {
      const { data } = await fileManagerService.removeFile({
        ids: params.fileIds,
      });
      if (data.code === 0) {
        message.success(t('message.deleted'));
      }
      queryClient.invalidateQueries({
        queryKey: [FileApiAction.FetchFileList],
      });
      return data.code;
    },
  });

  return { data, loading, deleteFile: mutateAsync };
};

/** 下载单个文件为 Blob 并触发浏览器保存。 */
export const useDownloadFile = () => {
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [FileApiAction.DownloadFile],
    mutationFn: async (params: { id: string; filename?: string }) => {
      const response = await fileManagerService.getFile({}, params.id);
      const blob = new Blob([response.data], { type: response.data.type });
      downloadFileFromBlob(blob, params.filename);
    },
  });
  return { data, loading, downloadFile: mutateAsync };
};

/** 通过 moveFile 接口重命名文件。 */
export const useRenameFile = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [FileApiAction.RenameFile],
    mutationFn: async (params: { fileId: string; name: string }) => {
      const { data } = await fileManagerService.moveFile({
        src_file_ids: [params.fileId],
        new_name: params.name,
      });
      if (data.code === 0) {
        message.success(t('message.renamed'));
        queryClient.invalidateQueries({
          queryKey: [FileApiAction.FetchFileList],
        });
      }
      return data.code;
    },
  });

  return { data, loading, renameFile: mutateAsync };
};

/** 将文件关联到指定知识库。 */
export const useConnectToKnowledge = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [FileApiAction.ConnectFileToKnowledge],
    mutationFn: async (params: IConnectRequestBody) => {
      const { data } = await fileManagerService.connectFileToKnowledge(params);
      if (data.code === 0) {
        message.success(t('message.operated'));
        queryClient.invalidateQueries({
          queryKey: [FileApiAction.FetchFileList],
        });
      }
      return data.code;
    },
  });

  return { data, loading, connectFileToKnowledge: mutateAsync };
};

/** 手动触发拉取某文件夹下纯文件列表（最多 100 条）。 */
export const useFetchPureFileList = () => {
  const { mutateAsync, isPending: loading } = useMutation({
    mutationKey: [FileApiAction.FetchPureFileList],
    gcTime: 0,

    mutationFn: async (parentId: string) => {
      const { data } = await fileManagerService.listFile({
        parent_id: parentId,
        page_size: 100,
        page: 1,
      });

      return data;
    },
  });

  return { loading, fetchList: mutateAsync };
};
