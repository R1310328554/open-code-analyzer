/**
 * 删除聊天会话的 mutation 钩子；成功时刷新列表并在必要时导航离开。
 */
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { deleteChat } from "@/api";
import { useNavigate } from "@tanstack/react-router";

/** 返回删除 chat 的 useMutation 实例。 */
export function useDeleteChat() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (chatId: string) => deleteChat(chatId),
    onSuccess: (_, chatId) => {
      // If we're currently viewing the deleted chat, navigate away
      const currentPath = window.location.pathname;
      if (currentPath === `/c/${chatId}`) {
        navigate({ to: "/c/$chatId", params: { chatId: "new" } });
      }

      queryClient.invalidateQueries({ queryKey: ["chats"] });
    },
    onError: (error, chatId) => {
      console.error("Failed to delete chat:", chatId, error);
    },
  });
}
