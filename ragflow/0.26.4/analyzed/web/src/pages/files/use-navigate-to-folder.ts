// use-navigate-to-folder.ts — 文件页路由跳转与面包屑路径组装。

import { useNavigatePage } from '@/hooks/logic-hooks/navigate-hooks';
import { useFetchParentFolderList } from '@/hooks/use-file-request';
import { Routes } from '@/routes';
import { useCallback } from 'react';

/** 封装 navigateToFiles，按 folderId 切换当前浏览目录。 */
export const useNavigateToOtherFolder = () => {
  const { navigateToFiles } = useNavigatePage();

  const navigateToOtherFolder = useCallback(
    (folderId: string) => {
      navigateToFiles(folderId);
    },
    [navigateToFiles],
  );

  return navigateToOtherFolder;
};

/** 根据父级文件夹链生成面包屑项；根目录仅一层时不展示。 */
export const useSelectBreadcrumbItems = () => {
  const parentFolderList = useFetchParentFolderList();

  // 已在根目录时隐藏面包屑，避免冗余「root」项
  return parentFolderList.length === 1
    ? []
    : parentFolderList.map((x) => ({
        // 后端根目录名 '/' 映射为前端展示 root
        title: x.name === '/' ? 'root' : x.name,
        path: `${Routes.Files}?folderId=${x.id}`,
      }));
};
