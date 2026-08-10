// CSS 媒体查询 Hook：订阅 matchMedia 变化并在 React 状态中反映是否匹配。

import { useEffect, useState } from 'react';

// useMedia 监听 change 事件，适用于响应式布局与窄屏 UI 切换。
// A hook to determine whether a CSS media query finds any matches.
const useMedia = (query: string): boolean => {
  const mediaQuery = window.matchMedia(query);
  const [matches, setMatches] = useState(mediaQuery.matches);

  useEffect(() => {
    const handler = () => setMatches(mediaQuery.matches);
    mediaQuery.addEventListener('change', handler);
    return () => mediaQuery.removeEventListener('change', handler);
  }, [mediaQuery]);

  return matches;
};

export default useMedia;
