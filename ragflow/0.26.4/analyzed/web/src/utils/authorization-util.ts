/**
 * authorization-util.ts — 本地鉴权存储与登录跳转：Token、UserInfo、Authorization。
 */

import { Authorization, Token, UserInfo } from '@/constants/authorization';
import { getSearchValue } from './common-util';
/** 登出时需清除的 localStorage 键集合。 */
const KeySet = [Authorization, Token, UserInfo];

/** localStorage 读写封装：鉴权头、令牌与用户信息。 */
const storage = {
  /** 读取 Authorization 请求头字符串。 */
  getAuthorization: () => {
    return localStorage.getItem(Authorization);
  },
  /** 读取 access token。 */
  getToken: () => {
    return localStorage.getItem(Token);
  },
  /** 读取用户信息 JSON 字符串。 */
  getUserInfo: () => {
    return localStorage.getItem(UserInfo);
  },
  /** 解析 UserInfo 为对象，缺失时返回 null。 */
  getUserInfoObject: () => {
    const userInfoStr = localStorage.getItem(UserInfo);
    return userInfoStr ? JSON.parse(userInfoStr) : null;
  },
  /** 写入 Authorization。 */
  setAuthorization: (value: string) => {
    localStorage.setItem(Authorization, value);
  },
  /** 写入 Token。 */
  setToken: (value: string) => {
    localStorage.setItem(Token, value);
  },
  /** 写入用户信息（对象自动 JSON 序列化）。 */
  setUserInfo: (value: string | Record<string, unknown>) => {
    const valueStr = typeof value !== 'string' ? JSON.stringify(value) : value;
    localStorage.setItem(UserInfo, valueStr);
  },
  /** 批量写入键值对。 */
  setItems: (pairs: Record<string, string>) => {
    Object.entries(pairs).forEach(([key, value]) => {
      localStorage.setItem(key, value);
    });
  },
  /** 仅移除 Authorization。 */
  removeAuthorization: () => {
    localStorage.removeItem(Authorization);
  },
  /** 清除全部鉴权相关 localStorage 项。 */
  removeAll: () => {
    KeySet.forEach((x) => {
      localStorage.removeItem(x);
    });
  },
  /** 持久化界面语言偏好。 */
  setLanguage: (lng: string) => {
    localStorage.setItem('lng', lng);
  },
  /** 读取已保存的语言代码。 */
  getLanguage: (): string => {
    return localStorage.getItem('lng') as string;
  },
};

/**
 * 获取当前 Authorization：优先 URL ?auth=，否则 localStorage。
 */
export const getAuthorization = () => {
  const auth = getSearchValue('auth');
  const authorization = auth
    ? 'Bearer ' + auth
    : storage.getAuthorization() || '';

  return authorization;
};

export default storage;

// 直接跳转登录页（非 SPA 路由）
/** 重定向至站点 /login 页。 */
export function redirectToLogin() {
  // const env = import.meta.env;
  window.location.href = location.origin + `/login`;
}
