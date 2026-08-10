// use-edit-query.ts — Begin 节点 inputs 查询项：弹窗增删改与重名校验上下文。

import { useSetModalState } from '@/hooks/common-hooks';
import { useSetSelectedRecord } from '@/hooks/logic-hooks';
import { useCallback, useMemo, useState } from 'react';
import { UseFormReturn, useWatch } from 'react-hook-form';
import { BeginQuery, INextOperatorForm } from '../../interface';

/** 管理 Begin inputs 列表编辑：弹窗显隐、当前记录、保存/删除与排除当前项的列表。 */
export const useEditQueryRecord = ({
  form,
}: INextOperatorForm & { form: UseFormReturn }) => {
  const { setRecord, currentRecord } = useSetSelectedRecord<BeginQuery>();
  const { visible, hideModal, showModal } = useSetModalState();
  const [index, setIndex] = useState(-1);
  const inputs: BeginQuery[] = useWatch({
    control: form.control,
    name: 'inputs',
  });

  // 编辑时排除当前索引，供重名校验使用
  const otherThanCurrentQuery = useMemo(() => {
    return inputs.filter((item, idx) => idx !== index);
  }, [index, inputs]);

  /** 保存查询项：有 index 则替换，否则追加到 inputs。 */
  const handleEditRecord = useCallback(
    (record: BeginQuery) => {
      const inputs: BeginQuery[] = form?.getValues('inputs') || [];

      const nextQuery: BeginQuery[] =
        index > -1 ? inputs.toSpliced(index, 1, record) : [...inputs, record];

      form.setValue('inputs', nextQuery);

      hideModal();
    },
    [form, hideModal, index],
  );

  /** 打开编辑弹窗并设置当前 index 与 record。 */
  const handleShowModal = useCallback(
    (idx?: number, record?: BeginQuery) => {
      setIndex(idx ?? -1);
      setRecord(record ?? ({} as BeginQuery));
      showModal();
    },
    [setRecord, showModal],
  );

  /** 按索引从 inputs 中移除一条查询项。 */
  const handleDeleteRecord = useCallback(
    (idx: number) => {
      const inputs = form?.getValues('inputs') || [];
      const nextInputs = inputs.filter(
        (item: BeginQuery, index: number) => index !== idx,
      );

      form.setValue('inputs', nextInputs);
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
