// memory-setting/hook.ts — 记忆库配置保存：omit id 后调用 updateMemory。

import { useUpdateMemory } from '@/pages/memories/hooks';
import { IMemory, IMemoryAppDetailProps } from '@/pages/memories/interface';
import { omit } from 'lodash';
import { useCallback, useState } from 'react';

/** 提交 IMemory 表单更新配置，维护 loading 状态供设置页按钮使用。 */
export const useUpdateMemoryConfig = () => {
  const { updateMemory } = useUpdateMemory();
  const [loading, setLoading] = useState(false);
  const onMemoryRenameOk = useCallback(
    async (data: IMemory) => {
      let res;
      setLoading(true);
      if (data?.id) {
        // console.log('memory-->', memory, data);
        try {
          // 更新接口 body 不含 id，其余字段原样提交
          const params = omit(data, [
            'id',
            // 'memory_type',
            // 'embd_id',
            // 'storage_type',
          ]);
          res = await updateMemory({
            // ...memoryDataTemp,
            // data: data,
            id: data.id,
            ...params,
          } as unknown as IMemoryAppDetailProps);
          // if (res && res.data.code === 0) {
          //   message.success(t('message.update_success'));
          // } else {
          //   message.error(t('message.update_fail'));
          // }
        } catch (e) {
          console.error('error', e);
        }
      }
      setLoading(false);
    },
    [updateMemory],
  );
  return { onMemoryRenameOk, loading };
};
