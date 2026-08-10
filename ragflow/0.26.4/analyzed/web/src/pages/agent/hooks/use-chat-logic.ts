// use-chat-logic.ts — 聊天「等待用户填表」态：解析 Begin inputs、构建表单列表与提交。

import { MessageType } from '@/constants/chat';
import { IMessage, Message } from '@/interfaces/database/chat';
import { get } from 'lodash';
import { useCallback, useMemo } from 'react';
import { BeginQuery } from '../interface';
import { buildBeginQueryWithObject } from '../utils';
/** useAwaitComponentData 入参：消息列表与表单提交回调。 */
type IAwaitCompentData = {
  derivedMessages: IMessage[];
  sendFormMessage: (params: { inputs: Record<string, BeginQuery> }) => void;
};
/** 检测末条 Assistant 消息是否带 inputs，并提供表单 OK 提交逻辑。 */
const useAwaitComponentData = (props: IAwaitCompentData) => {
  const { derivedMessages, sendFormMessage } = props;

  /** 从消息 data.inputs 读取 Begin 表单字段定义。 */
  const getInputs = useCallback((message: Message) => {
    return get(message, 'data.inputs', {}) as Record<string, BeginQuery>;
  }, []);

  /** 将 inputs 对象转为带 key 的数组供表单组件渲染。 */
  const buildInputList = useCallback(
    (message: Message) => {
      return Object.entries(getInputs(message)).map(([key, val]) => {
        return {
          ...val,
          key,
        };
      });
    },
    [getInputs],
  );

  /** 表单确认：合并用户填写值后 sendFormMessage 继续对话。 */
  const handleOk = useCallback(
    (message: Message) => (values: BeginQuery[]) => {
      const inputs = getInputs(message);
      const nextInputs = buildBeginQueryWithObject(inputs, values);
      sendFormMessage({
        inputs: nextInputs,
      });
    },
    [getInputs, sendFormMessage],
  );

  /** 最后一条 Assistant 消息含 inputs 时视为等待用户填表。 */
  const isWaiting = useMemo(() => {
    const temp = derivedMessages?.some((message, i) => {
      const hasInputs = Object.keys(getInputs(message)).length > 0;
      const flag =
        message.role === MessageType.Assistant &&
        derivedMessages.length - 1 === i &&
        hasInputs;
      return flag;
    });
    return temp;
  }, [derivedMessages, getInputs]);
  return { getInputs, buildInputList, handleOk, isWaiting };
};

export { useAwaitComponentData };
