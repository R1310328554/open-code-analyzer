/**
 * next-search/hooks.ts — 新版搜索应用页数据层：SSE 问答、分块检索、思维导图与共享链接参数。
 */

import message from '@/components/ui/message';
import { SharedFrom } from '@/constants/chat';
import { useSetModalState } from '@/hooks/common-hooks';
import {
  useGetPaginationWithRouter,
  useSendMessageWithSse,
} from '@/hooks/logic-hooks';
import { useSetPaginationParams } from '@/hooks/route-hook';
import {
  useKnowledgeBaseId,
  useSelectTestingResult,
} from '@/hooks/use-knowledge-request';
import { ResponsePostType } from '@/interfaces/database/base';
import { IAnswer } from '@/interfaces/database/chat';
import { ITestingResult } from '@/interfaces/database/dataset';
import { IAskRequestBody } from '@/interfaces/request/chat';
import kbService from '@/services/knowledge-service';
import chatService from '@/services/next-chat-service';
import searchService from '@/services/search-service';
import api from '@/utils/api';
import { useMutation } from '@tanstack/react-query';
import { has, isEmpty, isEqual, trim } from 'lodash';
import {
  ChangeEventHandler,
  Dispatch,
  SetStateAction,
  useCallback,
  useEffect,
  useRef,
  useState,
} from 'react';
import { useSearchParams } from 'react-router';
import { ISearchAppDetailProps } from '../next-searches/hooks';
import { useClickDrawer } from './document-preview-modal/hooks';

/** 搜索页容器传入的 props：初始搜索词、应用详情与搜索状态 setter。 */
export interface ISearchingProps {
  searchText?: string;
  data: ISearchAppDetailProps;
  setIsSearching?: Dispatch<SetStateAction<boolean>>;
  setSearchText?: Dispatch<SetStateAction<string>>;
}

export type ISearchReturnProps = ReturnType<typeof useSearching>;

/** 从 URL 解析共享搜索参数（from、shared_id、tenantId 及 data_* 自定义字段）。 */
export const useGetSharedSearchParams = () => {
  const [searchParams] = useSearchParams();
  // 自定义参数统一以 data_ 前缀出现在 query string 中
  const data_prefix = 'data_';
  const data = Object.fromEntries(
    Array.from(searchParams.entries())
      .filter(([key]) => key.startsWith(data_prefix))
      .map(([key, value]) => [key.replace(data_prefix, ''), value]),
  );
  return {
    from: searchParams.get('from') as SharedFrom,
    sharedId: searchParams.get('shared_id'),
    locale: searchParams.get('locale'),
    tenantId: searchParams.get('tenantId'),
    data: data,
    visibleAvatar: searchParams.get('visible_avatar')
      ? searchParams.get('visible_avatar') !== '1'
      : true,
  };
};

/** 拉取问答思维导图；共享链接走 searchService，否则走 chatService。 */
export const useSearchFetchMindMap = () => {
  const [searchParams] = useSearchParams();
  const sharedId = searchParams.get('shared_id');
  const fetchMindMapFunc = sharedId
    ? searchService.mindmapShare
    : chatService.chatsMindmap;
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: ['fetchMindMap'],
    gcTime: 0,
    mutationFn: async (params: IAskRequestBody) => {
      try {
        const ret = await fetchMindMapFunc(params);
        return ret?.data?.data ?? {};
      } catch (error: any) {
        if (has(error, 'message')) {
          message.error(error.message);
        }

        return [];
      }
    },
  });

  return { data, loading, fetchMindMap: mutateAsync };
};

/** 打开思维导图抽屉；问题或 kb 变化时重新请求，避免重复拉取相同参数。 */
export const useShowMindMapDrawer = (
  kbIds: string[],
  question: string,
  searchId = '',
) => {
  const { visible, showModal, hideModal } = useSetModalState();
  const ref = useRef<any>();

  const {
    fetchMindMap,
    data: mindMap,
    loading: mindMapLoading,
  } = useSearchFetchMindMap();

  const handleShowModal = useCallback(() => {
    const searchParams = {
      question: trim(question),
      kb_ids: kbIds,
      search_id: searchId,
    };
    if (
      !isEmpty(searchParams.question) &&
      !isEqual(searchParams, ref.current)
    ) {
      ref.current = searchParams;
      fetchMindMap(searchParams);
    }
    showModal();
  }, [fetchMindMap, showModal, question, kbIds, searchId]);

  return {
    mindMap,
    mindMapVisible: visible,
    mindMapLoading,
    showMindMapModal: handleShowModal,
    hideMindMapModal: hideModal,
  };
};

/** 分页检索测试（当前页 chunks）；共享场景自动切换 retrievalTestShare。 */
export const useTestChunkRetrieval = (
  tenantId?: string,
): ResponsePostType<ITestingResult> & {
  testChunk: (...params: any[]) => void;
} => {
  const knowledgeBaseId = useKnowledgeBaseId();
  const { page, size: pageSize } = useSetPaginationParams();
  const [searchParams] = useSearchParams();
  const shared_id = searchParams.get('shared_id');
  const retrievalTestFunc = shared_id
    ? kbService.retrievalTestShare
    : kbService.retrievalTest;
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: ['testChunk'], // This method is invalid
    gcTime: 0,
    mutationFn: async (values: any) => {
      const { data } = await retrievalTestFunc({
        page,
        size: pageSize,
        ...values,
        kb_id: values.kb_id ?? knowledgeBaseId,
        tenant_id: tenantId,
      });
      if (data.code === 0) {
        const res = data.data;
        return {
          ...res,
          documents: res.doc_aggs,
        };
      }
      return (
        data?.data ?? {
          chunks: [],
          documents: [],
          total: 0,
        }
      );
    },
  });

  return {
    data: data ?? { chunks: [], documents: [], total: 0 },
    loading,
    testChunk: mutateAsync,
  };
};

/** 检索测试（全量 doc 聚合视图），与 useTestChunkRetrieval 共用接口。 */
export const useTestChunkAllRetrieval = (
  tenantId?: string,
): ResponsePostType<ITestingResult> & {
  testChunkAll: (...params: any[]) => void;
} => {
  const knowledgeBaseId = useKnowledgeBaseId();
  const { page, size: pageSize } = useSetPaginationParams();
  const [searchParams] = useSearchParams();
  const shared_id = searchParams.get('shared_id');
  const retrievalTestFunc = shared_id
    ? kbService.retrievalTestShare
    : kbService.retrievalTest;
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: ['testChunkAll'], // This method is invalid
    gcTime: 0,
    mutationFn: async (values: any) => {
      const { data } = await retrievalTestFunc({
        page,
        size: pageSize,
        ...values,
        kb_id: values.kb_id ?? knowledgeBaseId,
        tenant_id: tenantId,
      });
      if (data.code === 0) {
        const res = data.data;
        return {
          ...res,
          documents: res.doc_aggs,
        };
      }
      return (
        data?.data ?? {
          chunks: [],
          documents: [],
          total: 0,
        }
      );
    },
  });

  return {
    data: data ?? { chunks: [], documents: [], total: 0 },
    loading,
    testChunkAll: mutateAsync,
  };
};

/** 监听搜索词与分页，自动触发分块检索并维护已选文档 ID。 */
export const useTestRetrieval = (
  kbIds: string[],
  searchStr: string,
  sendingLoading: boolean,
) => {
  const { testChunk, loading } = useTestChunkRetrieval();
  const { pagination } = useGetPaginationWithRouter();

  const [selectedDocumentIds, setSelectedDocumentIds] = useState<string[]>([]);

  const handleTestChunk = useCallback(() => {
    const q = trim(searchStr);
    if (sendingLoading || isEmpty(q)) return;

    testChunk({
      kb_id: kbIds,
      highlight: true,
      question: q,
      doc_ids: Array.isArray(selectedDocumentIds) ? selectedDocumentIds : [],
      page: pagination.current,
      size: pagination.pageSize,
    });
  }, [
    sendingLoading,
    searchStr,
    kbIds,
    testChunk,
    selectedDocumentIds,
    pagination,
  ]);

  useEffect(() => {
    handleTestChunk();
  }, [handleTestChunk]);

  return {
    loading,
    selectedDocumentIds,
    setSelectedDocumentIds,
  };
};
/** 根据当前问题拉取相关搜索推荐词。 */
export const useFetchRelatedQuestions = (
  tenantId?: string,
  searchId?: string,
) => {
  const [searchParams] = useSearchParams();
  const shared_id = searchParams.get('shared_id');
  const retrievalTestFunc = shared_id
    ? searchService.getRelatedQuestionsShare
    : chatService.chatsRelatedQuestions;
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: ['fetchRelatedQuestions'],
    gcTime: 0,
    mutationFn: async (question: string): Promise<string[]> => {
      const { data } = await retrievalTestFunc({
        question,
        tenant_id: tenantId,
        search_id: searchId,
      });

      return data?.data ?? [];
    },
  });

  return { data, loading, fetchRelatedQuestions: mutateAsync };
};

/** 核心问答 hook：SSE 流式回答 + 分块高亮检索 + 可选相关搜索。 */
export const useSendQuestion = (
  kbIds: string[],
  tenantId?: string,
  searchId: string = '',
  related_search: boolean = false,
) => {
  const { sharedId } = useGetSharedSearchParams();
  const askUrl = sharedId
    ? api.askShare
    : searchId
      ? api.searchCompletion(searchId)
      : '';
  const { send, answer, done, stopOutputMessage } = useSendMessageWithSse();

  const { testChunk, loading } = useTestChunkRetrieval(tenantId);
  const { testChunkAll } = useTestChunkAllRetrieval(tenantId);
  const [sendingLoading, setSendingLoading] = useState(false);
  const [currentAnswer, setCurrentAnswer] = useState({} as IAnswer);
  const { fetchRelatedQuestions, data: relatedQuestions } =
    useFetchRelatedQuestions(tenantId, searchId);
  const [searchStr, setSearchStr] = useState<string>('');
  const [isFirstRender, setIsFirstRender] = useState(true);
  const [selectedDocumentIds, setSelectedDocumentIds] = useState<string[]>([]);
  const [pageSize, setPageSize] = useState(10);

  const sendQuestion = useCallback(
    (question: string, enableAI: boolean = true) => {
      const q = trim(question);
      if (isEmpty(q)) return;
      setIsFirstRender(false);
      setCurrentAnswer({} as IAnswer);
      if (enableAI) {
        if (!sharedId && !searchId) {
          message.error('Search ID is required.');
          return;
        }
        setSendingLoading(true);
        send(askUrl, {
          kb_ids: kbIds,
          question: q,
          tenantId,
          search_id: searchId,
        });
      }
      testChunk({
        kb_id: kbIds,
        highlight: true,
        question: q,
        page: 1,
        size: pageSize,
        search_id: searchId,
      });

      if (related_search) {
        fetchRelatedQuestions(q);
      }
    },
    [
      send,
      testChunk,
      askUrl,
      kbIds,
      fetchRelatedQuestions,
      pageSize,
      tenantId,
      searchId,
      sharedId,
      related_search,
    ],
  );

  const handleSearchStrChange: ChangeEventHandler<HTMLInputElement> =
    useCallback((e) => {
      setSearchStr(e.target.value);
    }, []);

  const handleClickRelatedQuestion = useCallback(
    (question: string, enableAI: boolean = true) =>
      () => {
        if (sendingLoading) return;

        setSearchStr(question);
        sendQuestion(question, enableAI);
      },
    [sendQuestion, sendingLoading],
  );

  const handleTestChunk = useCallback(
    (documentIds: string[], page: number = 1, size: number = 10) => {
      const q = trim(searchStr);
      if (sendingLoading || isEmpty(q)) return;

      testChunk({
        kb_id: kbIds,
        highlight: true,
        question: q,
        doc_ids: documentIds ?? selectedDocumentIds,
        page,
        size,
        search_id: searchId,
      });

      testChunkAll({
        kb_id: kbIds,
        highlight: true,
        question: q,
        doc_ids: [],
        page,
        size,
        search_id: searchId,
      });
    },
    [
      searchStr,
      sendingLoading,
      testChunk,
      kbIds,
      selectedDocumentIds,
      testChunkAll,
      searchId,
    ],
  );

  useEffect(() => {
    if (!isEmpty(answer)) {
      setCurrentAnswer(answer);
    }
  }, [answer]);

  useEffect(() => {
    if (done) {
      setSendingLoading(false);
    }
  }, [done]);

  return {
    sendQuestion,
    handleSearchStrChange,
    handleClickRelatedQuestion,
    handleTestChunk,
    setSelectedDocumentIds,
    loading,
    sendingLoading,
    answer: currentAnswer,
    relatedQuestions: relatedQuestions?.slice(0, 5) ?? [],
    searchStr,
    setSearchStr,
    isFirstRender,
    selectedDocumentIds,
    isSearchStrEmpty: isEmpty(trim(searchStr)),
    stopOutputMessage,
    pageSize,
    setPageSize,
  };
};

/** 搜索页主 hook：组合问答、文档预览抽屉、思维导图与分页 Top-K。 */
export const useSearching = ({
  searchText,
  data: searchData,
  setSearchText,
}: ISearchingProps) => {
  const { tenantId } = useGetSharedSearchParams();
  const {
    sendQuestion,
    handleClickRelatedQuestion,
    handleTestChunk,
    setSelectedDocumentIds,
    answer,
    sendingLoading,
    relatedQuestions,
    searchStr,
    loading,
    isFirstRender,
    selectedDocumentIds,
    isSearchStrEmpty,
    setSearchStr,
    stopOutputMessage,
    pageSize,
    setPageSize,
  } = useSendQuestion(
    searchData.search_config.kb_ids,
    tenantId as string,
    searchData.id,
    searchData.search_config.related_search,
  );

  const handleSearchStrChange = useCallback(
    (value: string) => {
      setSearchStr(value);
    },
    [setSearchStr],
  );

  const { visible, hideModal, documentId, selectedChunk, clickDocumentButton } =
    useClickDrawer();

  useEffect(() => {
    if (searchText) {
      setSearchStr(searchText);
      sendQuestion(searchText, searchData.search_config.summary);
      setSearchText?.('');
    }
  }, [
    searchText,
    sendQuestion,
    setSearchStr,
    setSearchText,
    searchData.search_config.summary,
  ]);

  const {
    mindMapVisible,
    hideMindMapModal,
    showMindMapModal,
    mindMapLoading,
    mindMap,
  } = useShowMindMapDrawer(
    searchData.search_config.kb_ids,
    searchStr,
    searchData.id,
  );
  const { chunks, total } = useSelectTestingResult();

  const handleSearch = useCallback(
    (value: string) => {
      sendQuestion(value, searchData.search_config.summary);
      setSearchStr?.(value);
      hideMindMapModal();
    },
    [
      setSearchStr,
      sendQuestion,
      hideMindMapModal,
      searchData.search_config.summary,
    ],
  );

  const handleTopChange = useCallback(
    (size: number) => {
      setPageSize(size);
      handleTestChunk(selectedDocumentIds, 1, size);
    },
    [handleTestChunk, selectedDocumentIds, setPageSize],
  );

  return {
    handleClickRelatedQuestion,
    handleSearchStrChange,
    handleTestChunk,
    setSelectedDocumentIds,
    answer,
    sendingLoading,
    relatedQuestions,
    searchStr,
    loading,
    isFirstRender,
    selectedDocumentIds,
    isSearchStrEmpty,
    setSearchStr,
    stopOutputMessage,

    visible,
    hideModal,
    documentId,
    selectedChunk,
    clickDocumentButton,
    mindMapVisible,
    hideMindMapModal,
    showMindMapModal,
    mindMapLoading,
    mindMap,
    chunks,
    total,
    handleSearch,
    pageSize,
    handleTopChange,
  };
};

/** 判断搜索应用是否需引导打开设置（缺 kb 或名称时 openSetting 为 true）。 */
export const useCheckSettings = (data: ISearchAppDetailProps) => {
  if (!data) {
    return {
      openSetting: false,
    };
  }
  const { search_config, name } = data;
  const { kb_ids } = search_config;
  return {
    openSetting: kb_ids && kb_ids.length > 0 && name ? false : true,
  };
};

/** 思维导图加载占位进度（0–100），约 43 秒内线性递增。 */
export const usePendingMindMap = () => {
  const [count, setCount] = useState<number>(0);
  const ref = useRef<NodeJS.Timeout>();

  const setCountInterval = useCallback(() => {
    ref.current = setInterval(() => {
      setCount((pre) => {
        if (pre > 40) {
          clearInterval(ref?.current);
        }
        return pre + 1;
      });
    }, 1000);
  }, []);

  useEffect(() => {
    setCountInterval();
    return () => {
      clearInterval(ref?.current);
    };
  }, [setCountInterval]);

  return Number(((count / 43) * 100).toFixed(0));
};
