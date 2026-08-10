"""RAGFlow Python SDK 交互示例：连接本地 Agent 并流式问答。"""

from .ragflow_sdk import RAGFlow

# 初始化 SDK 客户端（示例 API Key 与本地地址）
rag_object = RAGFlow(api_key="ragflow-FDfRECsXDRagsKPxb_EfZdDPcmngavSgYEzbU_Blgq4", base_url="http://localhost:9222")
# 按 ID 获取智能体并创建会话
assistant = rag_object.get_agent("b0bc46e43dfc11f1b4ff84ba59bc54d9")
session = assistant.create_session()

print("\n==================== Miss R =====================\n")
print("Hello. What can I do for you?")

# 交互循环：读取用户输入，流式打印助手回复
while True:
    question = input("\n==================== User =====================\n> ")
    print("\n==================== Miss R =====================\n")

    cont = ""
    # ask(stream=True) 逐 token 增量输出
    for ans in session.ask(question, stream=True):
        print(ans.content[len(cont) :], end="", flush=True)
        cont = ans.content
