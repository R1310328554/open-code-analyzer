/** 判断用户 ID 是否属于轻量级（临时/占位）用户。 */
export const isLightweightUser = (userId?: string) =>
  userId?.startsWith("lightweight-");
