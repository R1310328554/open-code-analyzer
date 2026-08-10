/**
 * React Query 钩子：轮询并暴露 Ollama 云端功能开关状态。
 */
import { useQuery } from "@tanstack/react-query";
import { getCloudStatus, type CloudStatusResponse } from "@/api";

/** 返回云端禁用标志、加载/错误状态及原始响应。 */
export function useCloudStatus() {
  const cloudQuery = useQuery<CloudStatusResponse | null>({
    queryKey: ["cloudStatus"],
    queryFn: getCloudStatus,
    retry: false,
    staleTime: 60 * 1000,
  });

  return {
    cloudStatus: cloudQuery.data,
    cloudDisabled: cloudQuery.data?.disabled ?? false,
    isKnown: cloudQuery.data !== null && cloudQuery.data !== undefined,
    isLoading: cloudQuery.isLoading,
    isError: cloudQuery.isError,
    error: cloudQuery.error,
  };
}
