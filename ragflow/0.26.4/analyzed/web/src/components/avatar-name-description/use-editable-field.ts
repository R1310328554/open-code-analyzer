// use-editable-field.ts — 头像/名称/描述等内联可编辑字段的状态与键盘交互 Hook。

'use client';

import { useCallback, useEffect, useRef, useState } from 'react';

/** 可编辑字段配置：是否必填等。 */
interface UseEditableFieldOptions {
  required?: boolean;
}

/** Hook 返回值：编辑态、ref 与进入/退出/键盘/失焦处理器。 */
interface UseEditableFieldReturn {
  isEditing: boolean;
  inputRef: React.RefObject<HTMLInputElement | null>;
  previousValueRef: React.RefObject<string>;
  handleEnterEdit: (currentValue: string) => void;
  handleExitEdit: () => void;
  handleKeyDown: (e: React.KeyboardEvent<HTMLInputElement>) => void;
  handleBlur: (currentValue: string, onChange: (value: string) => void) => void;
}

/** 管理单击进入编辑、Enter/Escape 提交/取消、失焦回滚空值。 */
export function useEditableField(
  options: UseEditableFieldOptions = {},
): UseEditableFieldReturn {
  const { required = true } = options;
  const [isEditing, setIsEditing] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const previousValueRef = useRef<string>('');

  // 进入编辑态后 requestAnimationFrame 聚焦输入框
  useEffect(() => {
    if (isEditing) {
      const frameId = requestAnimationFrame(() => {
        inputRef.current?.focus();
      });

      return () => cancelAnimationFrame(frameId);
    }
  }, [isEditing]);

  const handleEnterEdit = useCallback((currentValue: string) => {
    previousValueRef.current = currentValue;
    setIsEditing(true);
  }, []);

  const handleExitEdit = useCallback(() => {
    setIsEditing(false);
  }, []);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === 'Enter') {
        e.preventDefault();
        setIsEditing(false);
      }
      if (e.key === 'Escape') {
        setIsEditing(false);
      }
    },
    [],
  );

  const handleBlur = useCallback(
    (currentValue: string, onChange: (value: string) => void) => {
      // 必填且为空时恢复进入编辑前的值
      if (required && !currentValue?.trim()) {
        onChange(previousValueRef.current);
      }
      setIsEditing(false);
    },
    [required],
  );

  return {
    isEditing,
    inputRef,
    previousValueRef,
    handleEnterEdit,
    handleExitEdit,
    handleKeyDown,
    handleBlur,
  };
}
