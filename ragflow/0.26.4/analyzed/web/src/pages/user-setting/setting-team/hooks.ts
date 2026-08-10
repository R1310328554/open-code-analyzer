// setting-team/hooks.ts — 团队设置：邀请成员、同意/拒绝、删除与退出租户。

import { useSetModalState, useShowDeleteConfirm } from '@/hooks/common-hooks';
import {
  useAddTenantUser,
  useAgreeTenant,
  useDeleteTenantUser,
  useFetchUserInfo,
} from '@/hooks/use-user-setting-request';
import { useCallback } from 'react';
import { useTranslation } from 'react-i18next';

/** 打开/关闭邀请成员弹窗并在确认后调用 addTenantUser。 */
export const useAddUser = () => {
  const { addTenantUser } = useAddTenantUser();
  const {
    visible: addingTenantModalVisible,
    hideModal: hideAddingTenantModal,
    showModal: showAddingTenantModal,
  } = useSetModalState();

  const handleAddUserOk = useCallback(
    async (email: string) => {
      const code = await addTenantUser(email);
      if (code === 0) {
        hideAddingTenantModal();
      }
    },
    [addTenantUser, hideAddingTenantModal],
  );

  return {
    addingTenantModalVisible,
    hideAddingTenantModal,
    showAddingTenantModal,
    handleAddUserOk,
  };
};

/** 删除团队成员：二次确认后调用 deleteTenantUser。 */
export const useHandleDeleteUser = () => {
  const { deleteTenantUser, loading } = useDeleteTenantUser();
  const showDeleteConfirm = useShowDeleteConfirm();
  const { t } = useTranslation();

  const handleDeleteTenantUser = (userId: string) => () => {
    showDeleteConfirm({
      title: t('setting.sureDelete'),
      onOk: async () => {
        const code = await deleteTenantUser({ userId });
        if (code === 0) {
        }
        return;
      },
    });
  };

  return { handleDeleteTenantUser, deleteTenantUser, loading };
};

/** 处理租户邀请：同意则 agreeTenant，拒绝则 deleteTenantUser。 */
export const useHandleAgreeTenant = () => {
  const { agreeTenant } = useAgreeTenant();
  const { deleteTenantUser } = useDeleteTenantUser();
  const { data: user } = useFetchUserInfo();

  const handleAgree = (tenantId: string, isAgree: boolean) => () => {
    if (isAgree) {
      agreeTenant(tenantId);
    } else {
      deleteTenantUser({ tenantId, userId: user.id });
    }
  };

  return { handleAgree };
};

/** 当前用户退出团队：确认后 deleteTenantUser(userId, tenantId)。 */
export const useHandleQuitUser = () => {
  const { deleteTenantUser, loading } = useDeleteTenantUser();
  const showDeleteConfirm = useShowDeleteConfirm();
  const { t } = useTranslation();

  const handleQuitTenantUser = (userId: string, tenantId: string) => () => {
    showDeleteConfirm({
      title: t('setting.sureQuit'),
      onOk: async () => {
        deleteTenantUser({ userId, tenantId });
      },
    });
  };

  return { handleQuitTenantUser, loading };
};
