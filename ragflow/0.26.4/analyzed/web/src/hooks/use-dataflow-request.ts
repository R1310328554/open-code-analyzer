// use-dataflow-request.ts — Dataflow 画布 CRUD 与详情查询 Hooks（React Query）。

import message from '@/components/ui/message';
import { IFlow } from '@/interfaces/database/agent';
import dataflowService from '@/services/dataflow-service';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useParams } from 'react-router';

/** Dataflow 相关 React Query mutation/query 键枚举。 */
export const enum DataflowApiAction {
  ListDataflow = 'listDataflow',
  RemoveDataflow = 'removeDataflow',
  FetchDataflow = 'fetchDataflow',
  RunDataflow = 'runDataflow',
  SetDataflow = 'setDataflow',
}

/** 批量删除 Dataflow 画布，成功后刷新列表并 Toast。 */
export const useRemoveDataflow = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [DataflowApiAction.RemoveDataflow],
    mutationFn: async (ids: string[]) => {
      const { data } = await dataflowService.removeDataflow({
        canvas_ids: ids,
      });
      if (data.code === 0) {
        queryClient.invalidateQueries({
          queryKey: [DataflowApiAction.ListDataflow],
        });

        message.success(t('message.deleted'));
      }
      return data.code;
    },
  });

  return { data, loading, removeDataflow: mutateAsync };
};

/** 创建或更新 Dataflow（有 id 为修改，无 id 为新建）。 */
export const useSetDataflow = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: [DataflowApiAction.SetDataflow],
    mutationFn: async (params: Partial<IFlow>) => {
      const { data } = await dataflowService.setDataflow(params);
      if (data.code === 0) {
        queryClient.invalidateQueries({
          queryKey: [DataflowApiAction.FetchDataflow],
        });

        message.success(t(`message.${params.id ? 'modified' : 'created'}`));
      }
      return data?.code;
    },
  });

  return { data, loading, setDataflow: mutateAsync };
};

/** 按路由 id 拉取单个 Dataflow 详情。 */
export const useFetchDataflow = () => {
  const { id } = useParams();

  const {
    data,
    isFetching: loading,
    refetch,
  } = useQuery<IFlow>({
    queryKey: [DataflowApiAction.FetchDataflow, id],
    gcTime: 0,
    initialData: {} as IFlow,
    enabled: !!id,
    refetchOnWindowFocus: false,
    queryFn: async () => {
      const { data } = await dataflowService.fetchDataflow(id);

      return data?.data ?? ({} as IFlow);
    },
  });

  return { data, loading, refetch };
};
