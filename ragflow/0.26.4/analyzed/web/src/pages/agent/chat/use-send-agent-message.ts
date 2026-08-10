// use-send-agent-message.ts — Agent 聊天 SSE 发送：消息拼装、会话/附件与流式回复落库。

import sonnerMessage from '@/components/ui/message';
import { MessageType } from '@/constants/chat';
import {
  useHandleMessageInputChange,
  useSelectDerivedMessages,
} from '@/hooks/logic-hooks';
import {
  IAttachment,
  IEventList,
  IMessageEndData,
  IMessageEndEvent,
  IMessageEvent,
  MessageEventType,
  useSendMessageBySSE,
} from '@/hooks/use-send-message';
import { Message } from '@/interfaces/database/chat';
import i18n from '@/locales/config';
import api from '@/utils/api';
import { get } from 'lodash';
import trim from 'lodash/trim';
import {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { useParams, useSearchParams } from 'react-router';
import { v4 as uuid } from 'uuid';
import { BeginId } from '../constant';
import { MessageWaitSuffix } from '../constant/chat';
import { AgentChatLogContext } from '../context';
import { transferInputsArrayToObject } from '../form/begin-form/use-watch-change';
import {
  useIsTaskMode,
  useSelectBeginNodeDataInputs,
} from '../hooks/use-get-begin-query';
import { useStopMessage } from '../hooks/use-stop-message';
import { BeginQuery } from '../interface';
import useGraphStore from '../store';
import { receiveMessageError } from '../utils';
import { shouldSplitMessage } from '../utils/chat';

/** 从 SSE 事件列表拼接 assistant 正文、思考标签、附件与 downloads。 */
export function findMessageFromList(eventList: IEventList) {
  const messageEventList = eventList.filter(
    (x) => x.event === MessageEventType.Message,
  ) as IMessageEvent[];

  let nextContent = '';

  let startIndex = -1;
  let endIndex = -1;
  let audioBinary = undefined;
  messageEventList.forEach((x, idx) => {
    const { data } = x;
    const { content, start_to_think, end_to_think, audio_binary } = data;
    if (audio_binary) {
      audioBinary = audio_binary;
    }
    if (start_to_think === true) {
      nextContent += '<think>' + content;
      startIndex = idx;
      return;
    }

    if (end_to_think === true) {
      endIndex = idx;
      nextContent += content + '</think>';
      return;
    }

    nextContent += content;
  });

  const currentIdx = messageEventList.length - 1;

  // 思考块未闭合时在末尾补 </think>
  if (startIndex >= 0 && startIndex <= currentIdx && endIndex === -1) {
    nextContent += '</think>';
  }

  const workflowFinished = eventList.find(
    (x) => x.event === MessageEventType.WorkflowFinished,
  ) as IMessageEvent;
  return {
    id: eventList[0]?.message_id,
    content: nextContent,
    audio_binary: audioBinary,
    attachment: workflowFinished?.data?.outputs?.attachment || {},
    downloads: workflowFinished?.data?.outputs?.downloads || [],
  };
}

/** 提取 UserInputs 事件中的表单输入 payload。 */
export function findInputFromList(eventList: IEventList) {
  const inputEvent = eventList.find(
    (x) => x.event === MessageEventType.UserInputs,
  );

  if (!inputEvent) {
    return {};
  }

  return {
    id: inputEvent?.message_id,
    data: inputEvent?.data,
  };
}

/** 从事件流末尾读取 _ERROR 或非零 code 的错误信息。 */
export function getLatestError(eventList: IEventList) {
  const latest = eventList.at(-1) as
    | { code?: number; message?: string }
    | undefined;
  return (
    get(latest, 'data.outputs._ERROR') ||
    (latest?.code && latest.code !== 0 ? latest?.message : undefined)
  );
}

/** 读取 Begin 节点 enablePrologue 时的开场白文案。 */
export const useGetBeginNodePrologue = () => {
  const getNode = useGraphStore((state) => state.getNode);
  const formData = get(getNode(BeginId), 'data.form', {});

  return useMemo(() => {
    if (formData?.enablePrologue) {
      return formData?.prologue;
    }
  }, [formData?.enablePrologue, formData?.prologue]);
};

/** 缓存 MessageEnd 事件，按 messageId 查询引用片段。 */
export function useFindMessageReference(answerList: IEventList) {
  const [messageEndEventList, setMessageEndEventList] = useState<
    IMessageEndEvent[]
  >([]);

  const findReferenceByMessageId = useCallback(
    (messageId: string) => {
      const event = messageEndEventList.find(
        (item) => item.message_id === messageId,
      );
      if (event) {
        return (event?.data as IMessageEndData)?.reference;
      }
    },
    [messageEndEventList],
  );

  useEffect(() => {
    const messageEndEvent = answerList.find(
      (x) => x.event === MessageEventType.MessageEnd,
    );
    if (messageEndEvent) {
      setMessageEndEventList((list) => {
        const nextList = [...list];
        if (
          nextList.every((x) => x.message_id !== messageEndEvent.message_id)
        ) {
          nextList.push(messageEndEvent as IMessageEndEvent);
        }
        return nextList;
      });
    }
  }, [answerList]);

  return { findReferenceByMessageId };
}

interface UploadResponseDataType {
  created_at: number;
  created_by: string;
  extension: string;
  id: string;
  mime_type: string;
  name: string;
  preview_url: null;
  size: number;
}

/** 维护聊天附件上传响应与本地 File 列表的增删清。 */
export function useSetUploadResponseData() {
  const [uploadResponseList, setUploadResponseList] = useState<
    UploadResponseDataType[]
  >([]);
  const [fileList, setFileList] = useState<File[]>([]);

  const append = useCallback((data: UploadResponseDataType, files: File[]) => {
    setUploadResponseList((prev) => [...prev, data]);
    setFileList((pre) => [...pre, ...files]);
  }, []);

  const clear = useCallback(() => {
    setUploadResponseList([]);
    setFileList([]);
  }, []);

  const removeFile = useCallback((file: File) => {
    setFileList((prev) => prev.filter((f) => f !== file));
    setUploadResponseList((prev) =>
      prev.filter((item) => item.name !== file.name),
    );
  }, []);

  return {
    uploadResponseList,
    fileList,
    setUploadResponseList,
    appendUploadResponseList: append,
    clearUploadResponseList: clear,
    removeFile,
  };
}

/** 构造用户消息体：uuid id、trim 后 content 与 User 角色。 */
export const buildRequestBody = (value: string = '') => {
  const id = uuid();
  const msgBody = {
    id,
    content: value.trim(),
    role: MessageType.User,
  };

  return msgBody;
};

/** Agent 聊天核心 Hook：SSE 发送、会话态、开场白、任务模式与消息列表联动。 */
export const useSendAgentMessage = ({
  url,
  addEventList,
  beginParams,
  isShared,
  refetch,
  isTaskMode: isTask,
  releaseMode,
}: {
  url?: string;
  addEventList?: (data: IEventList, messageId: string) => void;
  beginParams?: BeginQuery[];
  isShared?: boolean;
  refetch?: () => void;
  isTaskMode?: boolean;
  releaseMode?: string | null;
}) => {
  const { id: agentId } = useParams();
  const { handleInputChange, value, setValue } = useHandleMessageInputChange();
  const inputs = useSelectBeginNodeDataInputs();
  const [sessionId, setSessionId] = useState<string | null>(null);
  const { send, answerList, done, stopOutputMessage, resetAnswerList } =
    useSendMessageBySSE(url || api.agentChatCompletion);
  const firstAnswer = answerList[0];
  const messageId = useMemo(() => {
    return firstAnswer?.message_id;
  }, [firstAnswer]);

  const isTaskMode = useIsTaskMode(isTask);

  const { findReferenceByMessageId } = useFindMessageReference(answerList);
  const prologue = useGetBeginNodePrologue();
  const {
    derivedMessages,
    scrollRef,
    messageContainerRef,
    removeLatestMessage,
    removeMessageById,
    addNewestOneQuestion,
    addNewestOneAnswer,
    removeAllMessages,
    removeAllMessagesExceptFirst,
    scrollToBottom,
    addPrologue,
    setDerivedMessages,
  } = useSelectDerivedMessages();
  const { addEventList: addEventListFun } = useContext(AgentChatLogContext);
  const {
    appendUploadResponseList,
    clearUploadResponseList,
    uploadResponseList,
    fileList,
    removeFile,
  } = useSetUploadResponseData();

  const [searchParams] = useSearchParams();

  const userId = searchParams.get('userId');

  const { stopMessage } = useStopMessage();

  const stopConversation = useCallback(() => {
    const taskId = firstAnswer?.task_id;
    stopOutputMessage();
    if (!isShared) {
      stopMessage(taskId);
    }
  }, [firstAnswer, isShared, stopMessage, stopOutputMessage]);

  const sendMessage = useCallback(
    async ({
      message,
      beginInputs,
      exploreSessionId,
    }: {
      message: Message;
      messages?: Message[];
      beginInputs?: BeginQuery[];
      exploreSessionId?: string;
    }) => {
      const params: Record<string, unknown> = {
        agent_id: agentId,
        stream: true,
      };

      params.running_hint_text = i18n.t('flow.runningHintText', {
        defaultValue: 'is running...🕞',
      });
      params['openai-compatible'] = false;
      if (typeof message.content === 'string') {
        const query = inputs;

        params.query = message.content;
        // params.message_id = message.id;
        params.inputs = transferInputsArrayToObject(
          beginInputs || beginParams || query,
        ); // Begin 算子入参对象

        params.files = uploadResponseList;

        // 优先使用外层页面传入的 sessionId，避免 Hook 内缓存滞后
        // The hook keeps its own session cache for streamed replies, but that cache
        // can lag behind when the user switches sessions in Explore.
        params.session_id = exploreSessionId || sessionId;
        if (releaseMode) {
          params.release = releaseMode;
        }

        if (userId) {
          params.user_id = userId;
        }
      }

      try {
        const res = await send(params);

        clearUploadResponseList();

        if (receiveMessageError(res)) {
          sonnerMessage.error(res?.data?.message);

          // cancel loading
          setValue(message.content);
          removeLatestMessage();
        } else {
          refetch?.(); // pull the message list after sending the message successfully
        }
      } catch (error) {
        console.log('🚀 ~ useSendAgentMessage ~ error:', error);
      }
    },
    [
      agentId,
      inputs,
      beginParams,
      uploadResponseList,
      sessionId,
      releaseMode,
      userId,
      send,
      clearUploadResponseList,
      setValue,
      removeLatestMessage,
      refetch,
    ],
  );

  const sendFormMessage = useCallback(
    async (body: { inputs: Record<string, BeginQuery> }) => {
      addNewestOneQuestion({
        content: Object.entries(body.inputs)
          .map(([, val]) => `${val.name}: ${val.value}`)
          .join('<br/>'),
        role: MessageType.User,
      });
      await send({
        ...body,
        ...(isShared ? {} : { agent_id: agentId }),
        stream: true,
        session_id: sessionId,
        ...(releaseMode ? { release: releaseMode } : {}),
      });
      refetch?.();
    },
    [
      addNewestOneQuestion,
      agentId,
      isShared,
      refetch,
      releaseMode,
      send,
      sessionId,
    ],
  );

  // 重置会话：停止输出、清空 answerList 并按模式清理消息列表
  const resetSession = useCallback(() => {
    stopConversation();
    resetAnswerList();
    setSessionId(null);
    if (isTaskMode) {
      removeAllMessages();
    } else {
      removeAllMessagesExceptFirst();
    }
  }, [
    stopConversation,
    resetAnswerList,
    isTaskMode,
    removeAllMessages,
    removeAllMessagesExceptFirst,
  ]);

  const handlePressEnter = useCallback(
    ({ exploreSessionId }: { exploreSessionId?: string } = {}) => {
      if (trim(value) === '') return;
      const msgBody = buildRequestBody(value);
      if (done) {
        setValue('');
        sendMessage({
          message: msgBody,
          exploreSessionId,
        });
      }
      addNewestOneQuestion({ ...msgBody, files: fileList });
      setTimeout(() => {
        scrollToBottom();
      }, 100);
    },
    [
      value,
      done,
      addNewestOneQuestion,
      fileList,
      setValue,
      sendMessage,
      scrollToBottom,
    ],
  );

  const sendedTaskMessage = useRef(false);

  const sendMessageInTaskMode = useCallback(() => {
    if (isShared || !isTaskMode || sendedTaskMessage.current) {
      return;
    }
    const msgBody = buildRequestBody('');

    sendMessage({
      message: msgBody,
    });
    sendedTaskMessage.current = true;
  }, [isShared, isTaskMode, sendMessage]);

  useEffect(() => {
    sendMessageInTaskMode();
  }, [sendMessageInTaskMode]);

  useEffect(() => {
    const { content, id, attachment, audio_binary, downloads } =
      findMessageFromList(answerList);
    const inputAnswer = findInputFromList(answerList);
    const answer = content || getLatestError(answerList);

    if (answerList.length > 0) {
      const shouldSplit = shouldSplitMessage(answerList, content);

      // 需拆分：先写入 assistant 回复，再插入带 -wait 后缀的占位输入消息
      if (shouldSplit) {
        addNewestOneAnswer({
          answer: answer ?? '',
          audio_binary: audio_binary,
          attachment: attachment as IAttachment,
          downloads,
          id,
        });
        addNewestOneAnswer({
          answer: '',
          ...inputAnswer,
          id: `${id}${MessageWaitSuffix}`,
        });
      } else {
        addNewestOneAnswer({
          answer: answer ?? '',
          audio_binary: audio_binary,
          attachment: attachment as IAttachment,
          downloads,
          id,
          ...inputAnswer,
        });
      }
    }
  }, [answerList, addNewestOneAnswer]);

  useEffect(() => {
    if (isTaskMode) {
      return;
    }
    if (prologue) {
      addPrologue(prologue);
    }
  }, [
    addNewestOneAnswer,
    addPrologue,
    agentId,
    isTaskMode,
    prologue,
    send,
    sendFormMessage,
  ]);

  useEffect(() => {
    if (typeof addEventList === 'function') {
      addEventList(answerList, messageId);
    } else if (typeof addEventListFun === 'function') {
      addEventListFun(answerList, messageId);
    }
  }, [addEventList, answerList, addEventListFun, messageId]);

  useEffect(() => {
    if (firstAnswer?.session_id) {
      setSessionId(firstAnswer.session_id);
    }
  }, [firstAnswer]);

  return {
    value,
    sendLoading: !done,
    derivedMessages,
    scrollRef,
    messageContainerRef,
    handlePressEnter,
    handleInputChange,
    removeMessageById,
    stopOutputMessage: stopConversation,
    send,
    sendFormMessage,
    resetSession,
    findReferenceByMessageId,
    appendUploadResponseList,
    addNewestOneAnswer,
    sendMessage,
    removeFile,
    setDerivedMessages,
    addPrologue,
  };
};
