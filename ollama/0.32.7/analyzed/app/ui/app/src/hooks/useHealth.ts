/**
 * 后端健康探测钩子：不健康时每 10ms 轮询，恢复后停止。
 */
import { useQuery } from "@tanstack/react-query";
import { fetchHealth } from "@/api";

/** 返回 isHealthy 与 isChecking 状态。 */
export function useHealth() {
  const healthQuery = useQuery({
    queryKey: ["health"],
    queryFn: fetchHealth,
    refetchInterval: (query) => {
      // If the server is not healthy, poll every 10ms
      // Once healthy, stop polling
      return query.state.data === false ? 10 : false;
    },
    refetchIntervalInBackground: true,
    retry: false, // Don't retry, just return false
    staleTime: 0, // Always consider stale so we keep polling
  });

  return {
    isHealthy: healthQuery.data ?? false,
    isChecking: healthQuery.isLoading,
  };
}
