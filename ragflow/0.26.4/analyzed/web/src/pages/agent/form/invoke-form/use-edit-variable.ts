// use-edit-variable.ts — Invoke variables 弹窗编辑：增删改与去重校验数据源。

import { useSetModalState } from '@/hooks/common-hooks';
import { useSetSelectedRecord } from '@/hooks/logic-hooks';
import { useCallback, useMemo, useState } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import { INextOperatorForm } from '../../interface';
import { FormSchemaType, VariableFormSchemaType } from './schema';

/** 管理 variables 列表的模态编辑：showModal、ok 保存、delete 删除。 */
export const useEditVariableRecord = ({
  form,
}: INextOperatorForm & { form: UseFormReturn<FormSchemaType> }) => {
  const { setRecord, currentRecord } =
    useSetSelectedRecord<VariableFormSchemaType>();

  const { visible, hideModal, showModal } = useSetModalState();
  const [index, setIndex] = useState(-1);
  const variables = useWatch({
    control: form.control,
    name: 'variables',
  });

  // 编辑时排除当前行，供 key 唯一性校验
  const otherThanCurrentQuery = useMemo(() => {
    return variables.filter((item, idx) => idx !== index);
  }, [index, variables]);

  /** 新增或 toSpliced 替换指定 index 的 variable 并关闭弹窗。 */
  const handleEditRecord = useCallback(
    (record: VariableFormSchemaType) => {
      const variables = form?.getValues('variables') || [];

      const nextVaribales =
        index > -1
          ? variables.toSpliced(index, 1, record)
          : [...variables, record];

      form.setValue('variables', nextVaribales);

      hideModal();
    },
    [form, hideModal, index],
  );

  const handleShowModal = useCallback(
    (idx?: number, record?: VariableFormSchemaType) => {
      setIndex(idx ?? -1);
      setRecord(record ?? ({} as VariableFormSchemaType));
      showModal();
    },
    [setRecord, showModal],
  );

  /** 按 index 从 variables 数组移除一条记录。 */
  const handleDeleteRecord = useCallback(
    (idx: number) => {
      const variables = form?.getValues('variables') || [];
      const nextVariables = variables.filter((item, index) => index !== idx);

      form.setValue('variables', nextVariables);
    },
    [form],
  );

  return {
    ok: handleEditRecord,
    currentRecord,
    setRecord,
    visible,
    hideModal,
    showModal: handleShowModal,
    otherThanCurrentQuery,
    handleDeleteRecord,
  };
};
