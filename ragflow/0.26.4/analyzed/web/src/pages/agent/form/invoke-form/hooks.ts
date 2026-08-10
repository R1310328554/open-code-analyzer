// hooks.ts — Invoke 节点变量表 CRUD：component_id、value 与增删行。

import get from 'lodash/get';
import {
  ChangeEventHandler,
  MouseEventHandler,
  useCallback,
  useMemo,
} from 'react';
import { v4 as uuid } from 'uuid';
import { IGenerateParameter, IInvokeVariable } from '../../interface';
import useGraphStore from '../../store';

/** 管理 Invoke 节点 variables 列表：编辑、删除、新增与保存单行。 */
export const useHandleOperateParameters = (nodeId: string) => {
  const { getNode, updateNodeForm } = useGraphStore((state) => state);
  const node = getNode(nodeId);
  const dataSource: IGenerateParameter[] = useMemo(
    () => get(node, 'data.form.variables', []) as IGenerateParameter[],
    [node],
  );

  /** 按 row.id 更新 variables 中某一字段并写回画布。 */
  const changeValue = useCallback(
    (row: IInvokeVariable, field: string, value: string) => {
      const newData = [...dataSource];
      const index = newData.findIndex((item) => row.id === item.id);
      const item = newData[index];
      newData.splice(index, 1, {
        ...item,
        [field]: value,
      });

      updateNodeForm(nodeId, { variables: newData });
    },
    [dataSource, nodeId, updateNodeForm],
  );

  const handleComponentIdChange = useCallback(
    (row: IInvokeVariable) => (value: string) => {
      changeValue(row, 'component_id', value);
    },
    [changeValue],
  );

  const handleValueChange = useCallback(
    (row: IInvokeVariable): ChangeEventHandler<HTMLInputElement> =>
      (e) => {
        changeValue(row, 'value', e.target.value);
      },
    [changeValue],
  );

  const handleRemove = useCallback(
    (id?: string) => () => {
      const newData = dataSource.filter((item) => item.id !== id);
      updateNodeForm(nodeId, { variables: newData });
    },
    [updateNodeForm, nodeId, dataSource],
  );

  /** 追加一条空 variable 行（uuid id）。 */
  const handleAdd: MouseEventHandler = useCallback(
    (e) => {
      e.preventDefault();
      e.stopPropagation();
      updateNodeForm(nodeId, {
        variables: [
          ...dataSource,
          {
            id: uuid(),
            key: '',
            component_id: undefined,
            value: '',
          },
        ],
      });
    },
    [dataSource, nodeId, updateNodeForm],
  );

  /** 合并整行编辑结果到 variables 并同步节点 form。 */
  const handleSave = (row: IGenerateParameter) => {
    const newData = [...dataSource];
    const index = newData.findIndex((item) => row.id === item.id);
    const item = newData[index];
    newData.splice(index, 1, {
      ...item,
      ...row,
    });

    updateNodeForm(nodeId, { variables: newData });
  };

  return {
    handleAdd,
    handleRemove,
    handleComponentIdChange,
    handleValueChange,
    handleSave,
    dataSource,
  };
};
