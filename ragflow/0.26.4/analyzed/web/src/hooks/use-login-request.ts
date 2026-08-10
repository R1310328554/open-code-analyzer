// use-login-request.ts — 登录、注册、登出与第三方 OAuth 渠道 Hooks。

import message from '@/components/ui/message';
import { Authorization } from '@/constants/authorization';
import userService, {
  getLoginChannels,
  loginWithChannel,
} from '@/services/user-service';
import {
  default as authorizationUtil,
  redirectToLogin,
  default as storage,
} from '@/utils/authorization-util';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { useSaveSetting } from './use-user-setting-request';

/** 邮箱密码登录请求体。 */
export interface ILoginRequestBody {
  email: string;
  password: string;
}

/** 注册请求体，在登录字段基础上增加 nickname。 */
export interface IRegisterRequestBody extends ILoginRequestBody {
  nickname: string;
}

/** 第三方登录渠道展示信息。 */
export interface ILoginChannel {
  channel: string;
  display_name: string;
  icon: string;
}

/** 拉取可用 OAuth/第三方登录渠道列表。 */
export const useLoginChannels = () => {
  const { data, isLoading } = useQuery({
    queryKey: ['loginChannels'],
    queryFn: async () => {
      const { data: res = {} } = await getLoginChannels();
      return res.data || [];
    },
  });

  return { channels: data as ILoginChannel[], loading: isLoading };
};

/** 跳转指定 channel 的 OAuth 登录流程。 */
export const useLoginWithChannel = () => {
  const { isPending: loading, mutateAsync } = useMutation({
    mutationKey: ['loginWithChannel'],
    mutationFn: async (channel: string) => {
      loginWithChannel(channel);
      return Promise.resolve();
    },
  });

  return { loading, login: mutateAsync };
};

/** 邮箱密码登录，成功后写入 Token 与 userInfo 到本地存储。 */
export const useLogin = () => {
  const { saveSetting } = useSaveSetting(true);
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: ['login'],
    mutationFn: async (params: { email: string; password: string }) => {
      const { data: res = {}, response } = await userService.login(params);
      if (res.code === 0) {
        // The language is based on the .lng stored in the client's local storage.
        // The language stored in the database is for agent template resources,
        // since the agent template resources are stored on the server.
        saveSetting({ language: storage.getLanguage() });
        const { data } = res;
        const authorization = response.headers.get(Authorization);
        const token = data.access_token;
        const userInfo = {
          avatar: data.avatar,
          name: data.nickname,
          email: data.email,
        };
        authorizationUtil.setItems({
          Authorization: authorization,
          userInfo: JSON.stringify(userInfo),
          Token: token,
        });
      }
      return res.code;
    },
  });

  return { data, loading, login: mutateAsync };
};

/** 用户注册，处理注册被禁用等错误提示。 */
export const useRegister = () => {
  const { t } = useTranslation();

  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: ['register'],
    mutationFn: async (params: {
      email: string;
      password: string;
      nickname: string;
    }) => {
      const { data = {} } = await userService.register(params);
      if (data.code === 0) {
        message.success(t('message.registered'));
      } else if (
        data.message &&
        data.message.includes('registration is disabled')
      ) {
        message.error(
          t('message.registerDisabled') || 'User registration is disabled',
        );
      }
      return data.code;
    },
  });

  return { data, loading, register: mutateAsync };
};

/** 登出：清除凭证并跳转登录页。 */
export const useLogout = () => {
  const { t } = useTranslation();
  const {
    data,
    isPending: loading,
    mutateAsync,
  } = useMutation({
    mutationKey: ['logout'],
    mutationFn: async () => {
      const { data = {} } = await userService.logout();
      if (data.code === 0) {
        message.success(t('message.logout'));
        authorizationUtil.removeAll();
        redirectToLogin();
      }
      return data.code;
    },
  });

  return { data, loading, logout: mutateAsync };
};
