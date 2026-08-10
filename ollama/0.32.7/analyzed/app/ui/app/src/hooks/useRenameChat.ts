/**
 * 重命名聊天会话标题的 React Query mutation 钩子。
 */
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { renameChat } from "@/api";

/** 返回重命名 chat 的 useMutation；成功后刷新列表与单聊缓存。 */
/** 返回重命名 chat 的 useMutation；成功后刷新列表与单聊缓存。 */
export function useRenameChat() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ chatId, title }: { chatId: string; title: string }) =>
      renameChat(chatId, title),
    onSuccess: (_, { chatId }) => {
      // 使聊天列表缓存失效并重新拉取
      // 使聊天列表缓存失效并重新拉取
      // Invalidate and refetch chats list
      queryClient.invalidateQueries({ queryKey: ["chats"] });
      // 使该聊天详情缓存失效以更新标题
      // 使该聊天详情缓存失效以更新标题
      // Invalidate the specific chat to update its title
      queryClient.invalidateQueries({ queryKey: ["chat", chatId] });
    },
    onError: (error) => {
      console.error("Failed to rename chat:", error);
    },
  });
}
