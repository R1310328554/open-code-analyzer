// use-change-search.ts — 轻量搜索字符串 state，不含分页联动。

import { useCallback, useState } from 'react';

/** 维护 searchString 与 handleInputChange，仅更新本地搜索词。 */
export const useHandleSearchStrChange = () => {
  const [searchString, setSearchString] = useState('');
  const handleInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
      const value = e.target.value;
      setSearchString(value);
    },
    [],
  );

  return { handleInputChange, searchString };
};
