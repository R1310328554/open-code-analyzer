/** 按行增量解析 JSONL；不完整行保留在 buffer 中。 */
export async function* parseJsonlFromStream<T>(
  stream: ReadableStream<Uint8Array>,
): AsyncGenerator<T, void, unknown> {
  const reader = stream.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  try {
    while (true) {
      const { done, value } = await reader.read();

      if (done) {
        // 流结束时处理 buffer 中最后一行
        // Process any remaining data in buffer
        if (buffer.trim()) {
          try {
            yield JSON.parse(buffer.trim());
          } catch (error) {
            console.error(`Failed to parse final buffer: ${buffer}`, error);
          }
        }
        break;
      }

      // 解码当前 chunk 并追加到 buffer
      // Decode the chunk and add to buffer
      buffer += decoder.decode(value, { stream: true });

      // 按换行切分，完整行立即 parse
      // Process complete lines
      const lines = buffer.split("\n");
      buffer = lines.pop() || ""; // 末行可能不完整，留待下次
      // Keep incomplete line in buffer

      for (const line of lines) {
        const trimmed = line.trim();
        if (trimmed) {
          try {
            yield JSON.parse(trimmed);
          } catch (error) {
            console.error(`Failed to parse line: ${trimmed}`, error);
          }
        }
      }
    }
  } finally {
    reader.releaseLock();
  }
}

/**
 * 从 fetch Response.body 解析 JSONL 的便捷封装。
 * Helper function to parse JSONL from a Response object
 */
export async function* parseJsonlFromResponse<T>(
  response: Response,
): AsyncGenerator<T, void, unknown> {
  if (!response.body) {
    throw new Error("Response body is null");
  }
  yield* parseJsonlFromStream<T>(response.body);
}
