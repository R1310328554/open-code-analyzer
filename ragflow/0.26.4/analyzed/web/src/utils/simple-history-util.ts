/**
 * simple-history-util.ts — 轻量 History 实现：popstate 监听、push/replace 与订阅回调（替代 react-router history）。
 */

/** 封装 window.history 与 popstate，向订阅者广播 location 与 action。 */
class GlobalHistory {
  private listeners: Array<(location: any, action: string) => void> = [];
  state: any;

  constructor() {
    window.addEventListener('popstate', this.handlePopState);
  }

  /** 浏览器后退/前进时通知所有 listener（action 为 POP）。 */
  private handlePopState = (event: PopStateEvent) => {
    const location = {
      pathname: window.location.pathname,
      search: window.location.search,
      hash: window.location.hash,
      state: event.state,
    };

    this.listeners.forEach((listener) => {
      listener(location, 'POP');
    });
  };

  /** pushState 并触发 PUSH 回调。 */
  push = (
    path:
      | string
      | { pathname?: string; search?: string; hash?: string; state?: any },
    state?: any,
  ) => {
    let finalPath = '';
    if (typeof path === 'string') {
      finalPath = path;
    } else {
      finalPath = path.pathname || '';
      if (path.search) finalPath += path.search;
      if (path.hash) finalPath += path.hash;
    }

    window.history.pushState(state, '', finalPath);

    const location = {
      pathname: window.location.pathname,
      search: window.location.search,
      hash: window.location.hash,
      state: state,
    };

    this.listeners.forEach((listener) => {
      listener(location, 'PUSH');
    });
  };

  /** replaceState 并触发 REPLACE 回调。 */
  replace = (
    path:
      | string
      | { pathname?: string; search?: string; hash?: string; state?: any },
    state?: any,
  ) => {
    let finalPath = '';
    if (typeof path === 'string') {
      finalPath = path;
    } else {
      finalPath = path.pathname || '';
      if (path.search) finalPath += path.search;
      if (path.hash) finalPath += path.hash;
    }

    window.history.replaceState(state, '', finalPath);

    const location = {
      pathname: window.location.pathname,
      search: window.location.search,
      hash: window.location.hash,
      state: state,
    };

    this.listeners.forEach((listener) => {
      listener(location, 'REPLACE');
    });
  };

  /** 调用 history.go(n)。 */
  go = (n: number) => {
    window.history.go(n);
  };

  goBack = () => {
    window.history.back();
  };

  goForward = () => {
    window.history.forward();
  };

  /** 注册路由变化监听，返回取消订阅函数。 */
  listen = (callback: (location: any, action: string) => void) => {
    this.listeners.push(callback);

    return () => {
      const index = this.listeners.indexOf(callback);
      if (index !== -1) {
        this.listeners.splice(index, 1);
      }
    };
  };

  get location() {
    return {
      pathname: window.location.pathname,
      search: window.location.search,
      hash: window.location.hash,
      state: history.state,
    };
  }

  get length() {
    return window.history.length;
  }

  get action() {
    return 'POP';
  }
}

/** 全局单例 history 对象。 */
export const history = new GlobalHistory();

/** 返回 history 单例，供自定义导航 Hook 使用。 */
export const useCustomNavigate = () => {
  return history;
};
