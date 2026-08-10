/**
 * next-request.ts — 基于 axios 的 HTTP 客户端：鉴权、snake_case、tenant 参数、LLM 缓存与统一错误处理。
 */

import message from '@/components/ui/message';
import { Authorization } from '@/constants/authorization';
import i18n from '@/locales/config';
import authorizationUtil, {
  getAuthorization,
  redirectToLogin,
} from '@/utils/authorization-util';
import notification from '@/utils/notification';
import axios from 'axios';
import { convertTheKeysOfTheObjectToSnake, isFormData } from './common-util';
import { setCachedLlmList } from './llm-cache';
import { addTenantParams } from './llm-util';

/** 浏览器网络异常时的 error.message 标识。 */
const FAILED_TO_FETCH = 'Failed to fetch';

/** HTTP 状态码 → i18n 错误文案映射。 */
export const RetcodeMessage = {
  200: i18n.t('message.200'),
  201: i18n.t('message.201'),
  202: i18n.t('message.202'),
  204: i18n.t('message.204'),
  400: i18n.t('message.400'),
  401: i18n.t('message.401'),
  403: i18n.t('message.403'),
  404: i18n.t('message.404'),
  406: i18n.t('message.406'),
  410: i18n.t('message.410'),
  413: i18n.t('message.413'),
  422: i18n.t('message.422'),
  500: i18n.t('message.500'),
  502: i18n.t('message.502'),
  503: i18n.t('message.503'),
  504: i18n.t('message.504'),
};
/** 受支持的 HTTP 结果码联合类型。 */
export type ResultCode =
  | 200
  | 201
  | 202
  | 204
  | 400
  | 401
  | 403
  | 404
  | 406
  | 410
  | 413
  | 422
  | 500
  | 502
  | 503
  | 504;

/** 统一 HTTP 错误提示：网络异常或按状态码展示 notification。 */
const errorHandler = (error: {
  response: Response;
  message: string;
}): Response => {
  const { response } = error;
  if (error.message === FAILED_TO_FETCH) {
    notification.error({
      description: i18n.t('message.networkAnomalyDescription'),
      message: i18n.t('message.networkAnomaly'),
    });
  } else {
    if (response && response.status) {
      const errorText =
        RetcodeMessage[response.status as ResultCode] || response.statusText;
      const { status, url } = response;
      notification.error({
        message: `${i18n.t('message.requestError')} ${status}: ${url}`,
        description: errorText,
      });
    }
  }
  return response ?? { data: { code: 1999 } };
};

/** 防止并发 401 重复跳转登录页。 */
let isRedirecting = false;

/** axios 实例：300s 超时，错误在 response 拦截器处理。 */
const request = axios.create({
  //   errorHandler,
  timeout: 300000,
  //   getResponse: true,
});

/** 请求拦截：snake_case 转换、tenant 参数注入、Authorization 头。 */
request.interceptors.request.use(
  (config) => {
    const data = convertTheKeysOfTheObjectToSnake(config.data);
    const params = convertTheKeysOfTheObjectToSnake(config.params);

    // Add tenant parameters to data
    const dataWithTenantParams = isFormData(data)
      ? data
      : addTenantParams(data, config.url);

    const newConfig = { ...config, data: dataWithTenantParams, params };

    // Skip token if explicitly requested
    if (!(newConfig as any).skipToken) {
      newConfig.headers.set(Authorization, getAuthorization());
    }

    return newConfig;
  },
  function (error) {
    return Promise.reject(error);
  },
);

/** 响应拦截：413/504 提示、LLM 列表缓存更新、业务 code 与 401 登出。 */
request.interceptors.response.use(
  async (response) => {
    if (response?.status === 413 || response?.status === 504) {
      message.error(RetcodeMessage[response?.status as ResultCode]);
    }

    if (response.config.responseType === 'blob') {
      return response;
    }
    const data = response?.data;

    // Update LLM list cache when fetching my_llm or llm_list
    if (data?.code === 0 && data?.data) {
      const url = response?.config?.url || '';
      if (url.includes('/v1/llm/my_llms') || url.includes('/v1/llm/list')) {
        setCachedLlmList(data.data);
      }
    }

    if (data?.code === 100) {
      message.error(data?.message);
    } else if (data?.code === 401) {
      if (!isRedirecting) {
        isRedirecting = true;
        notification.error({
          message: data?.message,
          description: data?.message,
          duration: 3,
        });
        authorizationUtil.removeAll();
        redirectToLogin();
      }
    } else if (data?.code !== 0) {
      notification.error({
        message: `${i18n.t('message.hint')} : ${data?.code}`,
        description: data?.message,
        duration: 3,
      });
    }
    return response;
  },
  function (error) {
    // Handle HTTP 401 (token expired / invalid)
    const status = error?.response?.status;
    if (status === 401) {
      if (!isRedirecting) {
        isRedirecting = true;
        const messageText =
          error?.response?.data?.message || RetcodeMessage[401];
        notification.error({
          message: messageText,
          description: messageText,
          duration: 3,
        });
        authorizationUtil.removeAll();
        redirectToLogin();
      }

      return Promise.reject(error);
    }

    errorHandler(error);
    return Promise.reject(error);
  },
);

/** 默认导出配置完成的 axios 实例。 */
export default request;

/** GET 快捷方法。 */
export const get = (url: string) => {
  return request.get(url);
};

/** POST 快捷方法（body 包在 data 字段）。 */
export const post = (url: string, body: any) => {
  return request.post(url, { data: body });
};

export const drop = () => {};

export const put = () => {};
