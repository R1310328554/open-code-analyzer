// use-system-request.ts — 系统级配置（如是否开放注册）查询 Hook。

import userService from '@/services/user-service';
import { useQuery } from '@tanstack/react-query';

/** 拉取系统配置（含 registerEnabled），默认允许注册。 */
/** 返回 config 与 loading，用于登录/注册页判断是否展示注册入口。 */
export const useSystemConfig = () => {
  const { data, isLoading } = useQuery({
    queryKey: ['systemConfig'],
    queryFn: async () => {
      const { data = {} } = await userService.getSystemConfig();
      return data.data || { registerEnabled: 1 }; // Default to enabling registration
    },
  });

  return { config: data, loading: isLoading };
};
