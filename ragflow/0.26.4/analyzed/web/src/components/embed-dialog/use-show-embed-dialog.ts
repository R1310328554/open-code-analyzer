// use-show-embed-dialog.ts — 嵌入对话弹窗：Token 校验与 Modal 显隐控制。

import { useSetModalState, useTranslate } from '@/hooks/common-hooks';
import { useFetchManualSystemTokenList } from '@/hooks/use-user-setting-request';
import { useCallback } from 'react';
import message from '../ui/message';

/** Token 缺失时弹出 chat.tokenError 提示。 */
export const useShowTokenEmptyError = () => {
  const { t } = useTranslate('chat');

  const showTokenEmptyError = useCallback(() => {
    message.error(t('tokenError'));
  }, [t]);
  return { showTokenEmptyError };
};

/** Beta 未配置时弹出 chat.betaError 提示。 */
export const useShowBetaEmptyError = () => {
  const { t } = useTranslate('chat');

  const showBetaEmptyError = useCallback(() => {
    message.error(t('betaError'));
  }, [t]);
  return { showBetaEmptyError };
};

/** 手动拉取系统 Token 列表并校验 beta/token 有效性。 */
export const useFetchTokenListBeforeOtherStep = () => {
  const { showTokenEmptyError } = useShowTokenEmptyError();
  const { showBetaEmptyError } = useShowBetaEmptyError();

  const { data: tokenList, fetchSystemTokenList } =
    useFetchManualSystemTokenList();

  let token = '',
    beta = '';

  if (Array.isArray(tokenList) && tokenList.length > 0) {
    token = tokenList[0].token;
    beta = tokenList[0].beta;
  }

  token =
    Array.isArray(tokenList) && tokenList.length > 0 ? tokenList[0].token : '';

  const handleOperate = useCallback(async () => {
    const ret = await fetchSystemTokenList();
    const list = ret;
    if (Array.isArray(list) && list.length > 0) {
      if (!list[0].beta) {
        showBetaEmptyError();
        return false;
      }
      return list[0]?.token;
    } else {
      showTokenEmptyError();
      return false;
    }
  }, [fetchSystemTokenList, showBetaEmptyError, showTokenEmptyError]);

  return {
    token,
    beta,
    handleOperate,
  };
};

/** 校验通过后打开嵌入弹窗，暴露 embedToken 与 beta 供 iframe 使用。 */
export const useShowEmbedModal = () => {
  const {
    visible: embedVisible,
    hideModal: hideEmbedModal,
    showModal: showEmbedModal,
  } = useSetModalState();

  const { handleOperate, token, beta } = useFetchTokenListBeforeOtherStep();

  const handleShowEmbedModal = useCallback(async () => {
    const succeed = await handleOperate();
    if (succeed) {
      showEmbedModal();
    }
  }, [handleOperate, showEmbedModal]);

  return {
    showEmbedModal: handleShowEmbedModal,
    hideEmbedModal,
    embedVisible,
    embedToken: token,
    beta,
  };
};
