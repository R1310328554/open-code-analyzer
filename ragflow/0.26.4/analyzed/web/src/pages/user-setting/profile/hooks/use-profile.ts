// use-profile.ts — 用户资料页：昵称、时区、密码与头像编辑状态管理。

// src/hooks/useProfile.ts
import { DEFAULT_TIMEZONE } from '@/constants/setting';
import {
  useFetchUserInfo,
  useSaveSetting,
} from '@/hooks/use-user-setting-request';
import { TimezoneList } from '@/pages/user-setting/constants';
import { rsaPsw } from '@/utils';
import { useCallback, useEffect, useState } from 'react';

/** 个人资料表单数据结构（含可选密码字段）。 */
interface ProfileData {
  userName: string;
  timeZone: string;
  currPasswd?: string;
  newPasswd?: string;
  avatar: string;
  email: string;
  confirmPasswd?: string;
}

/** 资料编辑弹窗类型枚举。 */
export const EditType = {
  editName: 'editName',
  editTimeZone: 'editTimeZone',
  editPassword: 'editPassword',
} as const;

export type IEditType = keyof typeof EditType;

export const modalTitle = {
  [EditType.editName]: 'Edit Name',
  [EditType.editTimeZone]: 'Edit Time Zone',
  [EditType.editPassword]: 'Edit Password',
} as const;

/** 将后端或前端的时区字符串统一映射为 TimezoneList 展示名。 */
const normalizeTimezone = (tz: string | undefined): string => {
  if (!tz) return '';
  // 兼容后端 "UTC+8\tAsia/Shanghai" 与前端 "GMT+08:00 Asia/Shanghai" 两种格式
  const parts = tz.split(/\t|\s+/);
  const ianaName = parts.length > 1 ? parts[parts.length - 1] : tz;
  return TimezoneList.find((item) => item.id === ianaName)?.name ?? '';
};

/**
 * 用户资料页核心 Hook：拉取用户信息、管理编辑态并提交保存。
 */
export const useProfile = () => {
  const { data: userInfo } = useFetchUserInfo();
  const [profile, setProfile] = useState<ProfileData>({
    userName: '',
    avatar: '',
    timeZone: '',
    email: '',
    currPasswd: '',
  });

  const [editType, setEditType] = useState<IEditType>(EditType.editName);
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState<Partial<ProfileData>>({});
  const {
    saveSetting,
    loading: submitLoading,
    data: saveSettingData,
  } = useSaveSetting();

  useEffect(() => {
    const profile = {
      userName: userInfo.nickname,
      timeZone: normalizeTimezone(userInfo.timezone) || DEFAULT_TIMEZONE?.name,
      avatar: userInfo.avatar || '',
      email: userInfo.email,
      currPasswd: userInfo.password,
    };
    setProfile(profile);
  }, [userInfo, setProfile]);

  useEffect(() => {
    if (saveSettingData === 0) {
      setIsEditing(false);
      setEditForm({});
    }
  }, [saveSettingData]);
  /** 按 editType 组装 payload 并调用 saveSetting（密码经 RSA 加密）。 */
  const onSubmit = (newProfile: ProfileData) => {
    const payload: Partial<{
      nickname: string;
      password: string;
      new_password: string;
      avatar: string;
      timezone: string;
    }> = {
      nickname: newProfile.userName,
      avatar: newProfile.avatar,
      timezone: newProfile.timeZone,
    };

    if (
      'currPasswd' in newProfile &&
      'newPasswd' in newProfile &&
      newProfile.currPasswd &&
      newProfile.newPasswd
    ) {
      payload.password = rsaPsw(newProfile.currPasswd!) as string;
      payload.new_password = rsaPsw(newProfile.newPasswd!) as string;
    }
    if (editType === EditType.editName && payload.nickname) {
      saveSetting({ nickname: payload.nickname });
      setProfile(newProfile);
    }
    if (editType === EditType.editTimeZone && payload.timezone) {
      saveSetting({ timezone: payload.timezone });
      setProfile(newProfile);
    }
    if (editType === EditType.editPassword && payload.password) {
      saveSetting({
        password: payload.password,
        new_password: payload.new_password,
      });
      setProfile(newProfile);
    }
  };

  const handleEditClick = useCallback(
    (type: IEditType) => {
      setEditForm(profile);
      setEditType(type);
      setIsEditing(true);
    },
    [profile],
  );

  const handleCancel = useCallback(() => {
    setIsEditing(false);
    setEditForm({});
  }, []);

  const handleSave = (data: ProfileData) => {
    const newProfile = { ...profile, ...data };

    onSubmit(newProfile);
  };

  /** 头像上传成功后同步本地 profile 并立即保存。 */
  const handleAvatarUpload = (avatar: string) => {
    setProfile((prev) => ({ ...prev, avatar }));
    saveSetting({ avatar });
  };

  return {
    profile,
    setProfile,
    submitLoading: submitLoading,
    isEditing,
    editType,
    editForm,
    handleEditClick,
    handleCancel,
    handleSave,
    handleAvatarUpload,
  };
};
