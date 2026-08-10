// Mantine UI 的 Prometheus /api/v1 客户端：React Query 封装 fetch、错误处理与 pathPrefix。

import { QueryKey, useQuery, useSuspenseQuery } from "@tanstack/react-query";
import { useSettings } from "../state/settingsSlice";

export const API_PATH = "api/v1";

export type SuccessAPIResponse<T> = {
  status: "success";
  data: T;
  warnings?: string[];
  infos?: string[];
};

export type ErrorAPIResponse = {
  status: "error";
  errorType: string;
  error: string;
};

export type APIResponse<T> = SuccessAPIResponse<T> | ErrorAPIResponse;

// createQueryFn 工厂返回 React Query 的 queryFn，处理 JSON/网络/业务错误。
const createQueryFn =
  <T>({
    pathPrefix,
    path,
    params,
    recordResponseTime,
  }: {
    pathPrefix: string;
    path: string;
    params?: Record<string, string>;
    recordResponseTime?: (time: number) => void;
  }) =>
  async ({ signal }: { signal: AbortSignal }) => {
    const queryString = params
      ? `?${new URLSearchParams(params).toString()}`
      : "";

    try {
      const startTime = Date.now();

      const res = await fetch(
        `${pathPrefix}/${API_PATH}${path}${queryString}`,
        {
          cache: "no-store",
          credentials: "same-origin",
          signal,
        }
      );

      if (
        !res.ok &&
        !res.headers.get("content-type")?.startsWith("application/json")
      ) {
        // For example, Prometheus may send a 503 Service Unavailable response
        // with a "text/plain" content type when it's starting up. But the API
        // may also respond with a JSON error message and the same error code.
        throw new Error(res.statusText);
      }

      const apiRes = (await res.json()) as APIResponse<T>;

      if (recordResponseTime) {
        recordResponseTime(Date.now() - startTime);
      }

      if (apiRes.status === "error") {
        throw new Error(
          apiRes.error !== undefined
            ? apiRes.error
            : 'missing "error" field in response JSON'
        );
      }

      return apiRes as SuccessAPIResponse<T>;
    } catch (error) {
      if (!(error instanceof Error)) {
        throw new Error("Unknown error");
      }

      switch (error.name) {
        case "TypeError":
          throw new Error("Network error or unable to reach the server");
        case "SyntaxError":
          throw new Error("Invalid JSON response");
        default:
          throw error;
      }
    }
  };

type QueryOptions = {
  key?: QueryKey;
  path: string;
  params?: Record<string, string>;
  enabled?: boolean;
  refetchInterval?: false | number;
  recordResponseTime?: (time: number) => void;
};

// useAPIQuery 非 suspense 查询 hook，支持 enabled 与 refetchInterval。
export const useAPIQuery = <T>({
  key,
  path,
  params,
  enabled,
  recordResponseTime,
  refetchInterval,
}: QueryOptions) => {
  const { pathPrefix } = useSettings();

  return useQuery<SuccessAPIResponse<T>>({
    queryKey: key !== undefined ? key : [path, params],
    retry: false,
    refetchOnWindowFocus: false,
    refetchInterval: refetchInterval,
    gcTime: 0,
    enabled,
    queryFn: createQueryFn({ pathPrefix, path, params, recordResponseTime }),
  });
};

// useSuspenseAPIQuery 配合 Suspense 边界，staleTime=Infinity 避免卸载时 GC 丢数据。
export const useSuspenseAPIQuery = <T>({ key, path, params }: QueryOptions) => {
  const { pathPrefix } = useSettings();

  return useSuspenseQuery<SuccessAPIResponse<T>>({
    queryKey: key !== undefined ? key : [path, params],
    retry: false,
    refetchOnWindowFocus: false,
    staleTime: Infinity, // Required for suspense queries since the component is briefly unmounted when loading the data, which together with a gcTime of 0 will cause the data to be garbage collected before it can be used.
    gcTime: 0,
    queryFn: createQueryFn({ pathPrefix, path, params }),
  });
};
