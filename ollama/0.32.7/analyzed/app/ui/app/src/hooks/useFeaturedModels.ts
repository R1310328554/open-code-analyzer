/**
 * 获取实验性推荐/精选模型列表的 React Query 钩子。
 */
import { useQuery } from "@tanstack/react-query";
import { getModelRecommendations } from "@/api";
import type { ModelRecommendation } from "@/api";

/** 缓存 5 分钟、窗口聚焦时不自动 refetch。 */
export function useFeaturedModels() {
  return useQuery<ModelRecommendation[], Error>({
    queryKey: ["modelRecommendations"],
    queryFn: getModelRecommendations,
    staleTime: 5 * 60 * 1000,
    gcTime: 30 * 60 * 1000,
    refetchOnWindowFocus: false,
  });
}
