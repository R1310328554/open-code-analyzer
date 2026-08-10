// hook.ts — 解析结果内联编辑器状态：内容与当前编辑项索引。

import { useEffect, useRef, useState } from 'react';
import { IJsonContainerProps, IObjContainerProps } from './interface';

/** 初始化 content 并随 initialValue 变化同步，管理 activeEditIndex 与 editDivRef。 */
export const useParserInit = ({
  initialValue,
}: {
  initialValue:
    | IJsonContainerProps['initialValue']
    | IObjContainerProps['initialValue'];
}) => {
  const [content, setContent] = useState(initialValue);

  // 外部 initialValue 变更时重置本地编辑内容
  useEffect(() => {
    setContent(initialValue);
    console.log('initialValue json parse', initialValue);
  }, [initialValue]);

  const [activeEditIndex, setActiveEditIndex] = useState<number | undefined>(
    undefined,
  );
  const editDivRef = useRef<HTMLDivElement>(null);

  return {
    content,
    setContent,
    activeEditIndex,
    setActiveEditIndex,
    editDivRef,
  };
};
