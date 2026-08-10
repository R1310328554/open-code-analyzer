// use-mcp-request.ts — MCP 服务器 CRUD、导入导出与连通性测试 Hooks。

import message from '@/components/ui/message';
import { ResponseType } from '@/interfaces/database/base';
import {
  IExportedMcpServers,
  IMcpServer,
  IMcpServerListResponse,
  IMCPTool,
} from '@/interfaces/database/mcp';
import {
  IImportMcpServersRequestBody,
  ITestMcpRequestBody,
} from '@/interfaces/request/mcp';
import i18n from '@/locales/config';
import mcpServerService, {
  listMcpServers,
} from '@/services/mcp-server-service';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useDebounce } from 'ahooks';
import {
  useGetPaginationWithRouter,
  useHandleSearchChange,
} from './logic-hooks';

/** MCP 服务器 React Query 缓存键枚举。 */
export const enum McpApiAction {
  ListMcpServer = 'listMcpServer',
  GetMcpServer = 'getMcpServer',
  CreateMcpServer = 'createMcpServer',
  UpdateMcpServer = 'updateMcpServer',
  DeleteMcpServer = 'deleteMcpServer',
  ImportMcpServer = 'importMcpServer',
  ExportMcpServer = 'exportMcpServer',
  ListMcpServerTools = 'listMcpServerTools',
  TestMcpServerTool = 'testMcpServerTool',
  TestMcpServer = 'testMcpServer',
}

/** 分页搜索 MCP 服务器列表。 */
export const useListMcpServer = () => {
  const { searchString, handleInputChange } = useHandleSearchChange();
  const { pagination, setPagination } = useGetPaginationWithRouter();
  const debouncedSearchString = useDebounce(searchString, { wait: 500 });

  const { data, isFetching: loading } = useQuery<IMcpServerListResponse>({
    queryKey: [
      McpApiAction.ListMcpServer,
      {
        debouncedSearchString,
        ...pagination,
      },
    ],
    initialData: { total: 0, mcp_servers: [] },
    gcTime: 0,
    queryFn: async () => {
      const { data } = await listMcpServers({
        keywords: debouncedSearchString,
        page_size: pagination.pageSize,
        page: pagination.current,
      });
      return data?.data;
    },
  });

  return {
    data,
    loading,
    handleInputChange,
    setPagination,
    searchString,
    pagination: { ...pagination, total: data?.total },
  };
};

/** 按 mcp_id 拉取单个 MCP 服务器详情。 */
export const useGetMcpServer = (id: string) => {
  const { data, isFetching: loading } = useQuery<IMcpServer>({
    queryKey: [McpApiAction.GetMcpServer, id],
    initialData: {} as IMcpServer,
    gcTime: 0,
    enabled: !!id,
    queryFn: async () => {
      const { data } = await mcpServerService.get({ mcp_id: id });
      return data?.data ?? {};
    },
  });

  return { data, loading, id };
};

/** 创建 MCP 服务器。 */
export const useCreateMcpServer = () => {
  const queryClient = useQueryClient();
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [McpApiAction.CreateMcpServer],
    mutationFn: async (params: Record<string, any>) => {
      const { data = {} } = await mcpServerService.create(params);
      if (data.code === 0) {
        message.success(i18n.t(`message.created`));

        queryClient.invalidateQueries({
          queryKey: [McpApiAction.ListMcpServer],
        });
      }
      return data.code;
    },
  });

  return { data, loading, createMcpServer: mutateAsync };
};

/** 更新 MCP 服务器配置。 */
export const useUpdateMcpServer = () => {
  const queryClient = useQueryClient();
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [McpApiAction.UpdateMcpServer],
    mutationFn: async (params: Record<string, any>) => {
      const { data = {} } = await mcpServerService.update(params);
      if (data.code === 0) {
        message.success(i18n.t(`message.updated`));

        queryClient.invalidateQueries({
          queryKey: [McpApiAction.ListMcpServer],
        });
      }
      return data.code;
    },
  });

  return { data, loading, updateMcpServer: mutateAsync };
};

/** 批量删除 MCP 服务器。 */
export const useDeleteMcpServer = () => {
  const queryClient = useQueryClient();
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [McpApiAction.DeleteMcpServer],
    mutationFn: async (ids: string[]) => {
      const results = await Promise.all(
        ids.map((id) => mcpServerService.delete({ mcp_id: id })),
      );
      const failed = results.find(({ data = {} }) => data.code !== 0);
      const data = failed?.data ?? { code: 0, data: true };
      if (!failed) {
        message.success(i18n.t(`message.deleted`));

        queryClient.invalidateQueries({
          queryKey: [McpApiAction.ListMcpServer],
        });
      }
      return data;
    },
  });

  return { data, loading, deleteMcpServer: mutateAsync };
};

/** 从 JSON 批量导入 MCP 服务器。 */
export const useImportMcpServer = () => {
  const queryClient = useQueryClient();
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [McpApiAction.ImportMcpServer],
    mutationFn: async (params: IImportMcpServersRequestBody) => {
      const { data = {} } = await mcpServerService.import(params);
      if (data.code === 0) {
        message.success(i18n.t(`message.operated`));

        queryClient.invalidateQueries({
          queryKey: [McpApiAction.ListMcpServer],
        });
      }
      return data;
    },
  });

  return { data, loading, importMcpServer: mutateAsync };
};

/** 批量导出 MCP 服务器配置为 JSON。 */
export const useExportMcpServer = () => {
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation<ResponseType<IExportedMcpServers>, Error, string[]>({
    mutationKey: [McpApiAction.ExportMcpServer],
    mutationFn: async (ids) => {
      const results = await Promise.all(
        ids.map((id) => mcpServerService.export({ mcp_id: id })),
      );
      const failed = results.find(({ data = {} }) => data.code !== 0);
      const data = (failed?.data ?? {
        code: 0,
        data: results.reduce<IExportedMcpServers>(
          (acc, result) => ({
            mcpServers: {
              ...acc.mcpServers,
              ...(result.data?.data?.mcpServers ?? {}),
            },
          }),
          { mcpServers: {} },
        ),
      }) as ResponseType<IExportedMcpServers>;
      if (!failed) {
        message.success(i18n.t(`message.operated`));
      }
      return data;
    },
  });

  return { data, loading, exportMcpServer: mutateAsync };
};

/** 测试 MCP 服务器连通性并返回可用工具列表。 */
export const useTestMcpServer = () => {
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation<ResponseType<IMCPTool[]>, Error, ITestMcpRequestBody>({
    mutationKey: [McpApiAction.TestMcpServer],
    mutationFn: async (params) => {
      const { data } = await mcpServerService.test(params);

      return data;
    },
  });

  return { data, loading, testMcpServer: mutateAsync };
};
