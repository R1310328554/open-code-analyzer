/** 各显存单位到 GiB 的换算系数（GB/MB 为十进制，GiB/MiB 为二进制）。 */
const GIB_FACTOR: Record<string, number> = {
  gib: 1, // 1 GiB = 1 GiB
  gb: 1000 / 1024, // 1 GB（十进制）≈ 0.9765625 GiB
  mib: 1 / 1024, // 1 MiB = 1/1024 GiB
  mb: 1 / (1024 * 1024), // 1 MB（十进制）≈ 9.54e-7 GiB
};

/**
 * 将显存字符串（如 "8 GiB"、"16 GB"）解析为 GiB 数值；格式无效时返回 null。
 */
export function parseVRAM(vramString: string): number | null {
  if (!vramString) return null;

  const match = vramString.match(/^(\d+(?:\.\d+)?)\s*(GiB|GB|MiB|MB)$/i);
  if (!match) return null;

  const value = parseFloat(match[1]);
  const unit = match[2].toLowerCase();

  return value * GIB_FACTOR[unit];
}

/**
 * 汇总多块推理设备的显存（GiB）；无法解析的条目会被跳过。
 */
export function getTotalVRAM(inferenceComputes: { vram: string }[]): number {
  let totalVRAM = 0;
  for (const compute of inferenceComputes) {
    const parsed = parseVRAM(compute.vram);
    if (parsed !== null) {
      totalVRAM += parsed;
    }
  }
  return totalVRAM;
}
