// navigate-hooks.ts — 页面导航 Hook：数据集、聊天、Agent、搜索等路由跳转封装。

import { AgentCategory, AgentQuery } from '@/constants/agent';
import { NavigateToDataflowResultProps } from '@/pages/dataflow-result/interface';
import { Routes } from '@/routes';
import { useCallback } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router';

/** URL 查询参数键名枚举（knowledgeId、id）。 */
export enum QueryStringMap {
  KnowledgeId = 'knowledgeId',
  id = 'id',
}

/** 聚合各业务模块 navigateTo* 方法，统一基于 react-router navigate。 */
export const useNavigatePage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { id } = useParams();

  /** 跳转数据集列表，可选 isCreate 打开创建弹窗。 */
  const navigateToDatasetList = useCallback(
    ({ isCreate = false }: { isCreate?: boolean }) => {
      if (isCreate) {
        navigate(Routes.Datasets + '?isCreate=true');
      } else {
        navigate(Routes.Datasets);
      }
    },
    [navigate],
  );

  const navigateToMemoryList = useCallback(
    ({ isCreate = false }: { isCreate?: boolean }) => {
      if (isCreate) {
        navigate(Routes.Memories + '?isCreate=true');
      } else {
        navigate(Routes.Memories);
      }
    },
    [navigate],
  );

  const navigateToDataset = useCallback(
    (id: string) => () => {
      // navigate(`${Routes.DatasetBase}${Routes.DataSetOverview}/${id}`);
      navigate(`${Routes.Dataset}/${id}`);
    },
    [navigate],
  );
  const navigateToDatasetOverview = useCallback(
    (id: string) => () => {
      navigate(`${Routes.DatasetBase}${Routes.DataSetOverview}/${id}`);
    },
    [navigate],
  );

  const navigateToDataFile = useCallback(
    (id: string) => () => {
      navigate(`${Routes.DatasetBase}${Routes.Files}/${id}`);
    },
    [navigate],
  );

  const navigateToHome = useCallback(() => {
    navigate(Routes.Root);
  }, [navigate]);

  const navigateToProfile = useCallback(() => {
    navigate(Routes.ProfileSetting);
  }, [navigate]);

  const navigateToOldProfile = useCallback(() => {
    navigate(Routes.UserSetting);
  }, [navigate]);

  const navigateToChatList = useCallback(() => {
    navigate(Routes.Chats);
  }, [navigate]);

  const navigateToChat = useCallback(
    (id: string) => () => {
      navigate(`${Routes.Chat}/${id}`);
    },
    [navigate],
  );

  const navigateToAgents = useCallback(() => {
    navigate(Routes.Agents);
  }, [navigate]);

  const navigateToAgentList = useCallback(() => {
    navigate(Routes.AgentList);
  }, [navigate]);

  /** 跳转 Agent 画布页，可选附带 category 查询参数。 */
  const navigateToAgent = useCallback(
    (id: string, category?: AgentCategory) => () => {
      navigate(`${Routes.Agent}/${id}?${AgentQuery.Category}=${category}`);
    },
    [navigate],
  );

  const navigateToAgentExplore = useCallback(
    (id: string) => () => {
      navigate(`${Routes.Agent}/${id}/explore`);
    },
    [navigate],
  );

  const navigateToAgentLogs = useCallback(
    (id: string) => () => {
      navigate(`${Routes.AgentLogPage}/${id}`);
    },
    [navigate],
  );

  const navigateToAgentTemplates = useCallback(() => {
    navigate(Routes.AgentTemplates);
  }, [navigate]);

  const navigateToSearchList = useCallback(() => {
    navigate(Routes.Searches);
  }, [navigate]);

  const navigateToSearch = useCallback(
    (id: string) => () => {
      navigate(`${Routes.Search}/${id}`);
    },
    [navigate],
  );
  const navigateToMemory = useCallback(
    (id: string) => () => {
      navigate(`${Routes.Memory}${Routes.MemoryMessage}/${id}`);
    },
    [navigate],
  );

  const navigateToChunkParsedResult = useCallback(
    (id: string, knowledgeId?: string) => () => {
      navigate(
        `${Routes.ParsedResult}/chunks?id=${knowledgeId}&doc_id=${id}`,
        // `${Routes.DataflowResult}?id=${knowledgeId}&doc_id=${id}&type=chunk`,
      );
    },
    [navigate],
  );

  /** 读取当前 URL 中 knowledgeId/id 等查询参数。 */
  const getQueryString = useCallback(
    (queryStringKey?: QueryStringMap) => {
      const allQueryString = {
        [QueryStringMap.KnowledgeId]: searchParams.get(
          QueryStringMap.KnowledgeId,
        ),
        [QueryStringMap.id]: searchParams.get(QueryStringMap.id),
      };
      if (queryStringKey) {
        return allQueryString[queryStringKey];
      }
      return allQueryString;
    },
    [searchParams],
  );

  const navigateToChunk = useCallback(
    (route: Routes) => {
      navigate(
        `${route}/${id}?${QueryStringMap.KnowledgeId}=${getQueryString(QueryStringMap.KnowledgeId)}`,
      );
    },
    [getQueryString, id, navigate],
  );

  const navigateToFiles = useCallback(
    (folderId?: string) => {
      navigate(`${Routes.Files}?folderId=${folderId}`);
    },
    [navigate],
  );

  const navigateToDataSourceDetail = useCallback(
    (id?: string) => {
      navigate(
        `${Routes.UserSetting}${Routes.DataSource}${Routes.DataSourceDetailPage}?id=${id}`,
      );
    },
    [navigate],
  );

  /** 跳转数据流解析结果页，props 序列化为 query string。 */
  const navigateToDataflowResult = useCallback(
    (props: NavigateToDataflowResultProps) => () => {
      const params: string[] = [];
      Object.keys(props).forEach((key) => {
        if (props[key as keyof typeof props]) {
          params.push(`${key}=${props[key as keyof typeof props]}`);
        }
      });
      navigate(
        // `${Routes.ParsedResult}/${id}?${QueryStringMap.KnowledgeId}=${knowledgeId}`,
        `${Routes.DataflowResult}?${params.join('&')}`,
      );
    },
    [navigate],
  );

  const navigateToModelSetting = useCallback(() => {
    navigate(`${Routes.UserSetting}${Routes.Model}`);
  }, [navigate]);

  return {
    navigateToDatasetList,
    navigateToDataset,
    navigateToDatasetOverview,
    navigateToHome,
    navigateToProfile,
    navigateToChatList,
    navigateToChat,
    navigateToChunkParsedResult,
    getQueryString,
    navigateToChunk,
    navigateToAgents,
    navigateToAgent,
    navigateToAgentExplore,
    navigateToAgentLogs,
    navigateToAgentTemplates,
    navigateToSearchList,
    navigateToSearch,
    navigateToFiles,
    navigateToAgentList,
    navigateToOldProfile,
    navigateToDataflowResult,
    navigateToDataFile,
    navigateToDataSourceDetail,
    navigateToMemory,
    navigateToMemoryList,
    navigateToModelSetting,
  };
};
