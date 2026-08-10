// use-handle-name-change.ts — VariableAggregator 分组名编辑：仅允许字母数字下划线，失焦校验重名。

import message from '@/components/ui/message';
import { trim } from 'lodash';
import { ChangeEvent, useCallback, useEffect, useState } from 'react';
import { useFormContext } from 'react-hook-form';
import { VariableAggregatorFormSchemaType } from './schema';

/** 管理分组名本地 state，blur 时校验非空且不重名，非法则回滚 previousName。 */
export const useHandleNameChange = (previousName: string) => {
  const [name, setName] = useState<string>('');
  const form = useFormContext<VariableAggregatorFormSchemaType>();

  const handleNameBlur = useCallback(() => {
    const names = form.getValues('groups');
    const existsSameName = names.some((x) => x.group_name === name);
    if (trim(name) === '' || existsSameName) {
      if (existsSameName && previousName !== name) {
        message.error('The name cannot be repeated');
      }
      setName(previousName);
      return previousName;
    }
    return name;
  }, [form, name, previousName]);

  const handleNameChange = useCallback((e: ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    // 实时过滤非法字符，仅保留变量名合法字符
    const filteredValue = value.replace(/[^a-zA-Z0-9_]/g, '');
    setName(filteredValue);
  }, []);

  useEffect(() => {
    setName(previousName);
  }, [previousName]);

  return {
    name,
    handleNameBlur,
    handleNameChange,
  };
};
