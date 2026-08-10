/**
 * Ollama 账户登录态、连接 URL 与登出的 React Query 钩子。
 */
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchUser, fetchConnectUrl, disconnectUser } from "@/api";

/** 暴露 user 数据、认证状态及 refresh/disconnect 等操作。 */
export function useUser() {
  const queryClient = useQueryClient();

  const userQuery = useQuery({
    queryKey: ["user"],
    queryFn: async () => {
      const result = await fetchUser();
      return result;
    },
    staleTime: 5 * 60 * 1000, // 5 分钟后视为 stale
    // Consider data stale after 5 minutes
    gcTime: 10 * 60 * 1000, // 缓存保留 10 分钟
    // Keep in cache for 10 minutes
    retry: 10,
    retryDelay: (attemptIndex) => Math.min(500 * attemptIndex, 2000),
    refetchOnMount: true, // 组件挂载时总是尝试拉取
    // Always fetch when component mounts
  });

  // 手动刷新用户信息的 mutation
  // Mutation to refresh user data
  const refreshUser = useMutation({
    mutationFn: () => fetchUser(),
    onSuccess: (data) => {
      queryClient.setQueryData(["user"], data);
    },
  });

  // 连接账户用的 signin URL（默认不自动请求）
  // Query for connect URL (only fetched when needed)
  const connectUrlQuery = useQuery({
    queryKey: ["connectUrl"],
    queryFn: fetchConnectUrl,
    enabled: false, // 需调用 refetch 时才拉取
    // Don't fetch automatically
    staleTime: Infinity, // 连接 URL 视为不变
    // Connect URL doesn't change
  });

  const disconnectMutation = useMutation({
    mutationFn: disconnectUser,
    onSuccess: () => {
      queryClient.setQueryData(["user"], null);
    },
  });

  const isLoading = userQuery.isLoading || userQuery.isFetching;
  const isAuthenticated = Boolean(userQuery.data?.name);

  return {
    user: userQuery.data,
    isLoading,
    isError: userQuery.isError,
    error: userQuery.error,
    isAuthenticated,
    refreshUser: refreshUser.mutate,
    isRefreshing: refreshUser.isPending,
    refetchUser: userQuery.refetch,
    fetchConnectUrl: connectUrlQuery.refetch,
    connectUrl: connectUrlQuery.data,
    disconnectUser: disconnectMutation.mutate,
  };
}
