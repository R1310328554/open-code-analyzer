/** 历史遗留 JSDoc 占位（原 parseQuery 相关，现仅保留注释）。 */

// import numeral from 'numeral';

import { Base64 } from 'js-base64';
import JSEncrypt from 'jsencrypt';

/** 返回当前 window.innerWidth 的对象包装。 */
export const getWidth = () => {
  return { width: window.innerWidth };
};
/** 使用内置公钥 RSA 加密 Base64 编码后的密码（登录传输）。 */
export const rsaPsw = (password: string) => {
  const pub =
    '-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArq9XTUSeYr2+N1h3Afl/z8Dse/2yD0ZGrKwx+EEEcdsBLca9Ynmx3nIB5obmLlSfmskLpBo0UACBmB5rEjBp2Q2f3AG3Hjd4B+gNCG6BDaawuDlgANIhGnaTLrIqWrrcm4EMzJOnAOI1fgzJRsOOUEfaS318Eq9OVO3apEyCCt0lOQK6PuksduOjVxtltDav+guVAA068NrPYmRNabVKRNLJpL8w4D44sfth5RvZ3q9t+6RTArpEtc5sh5ChzvqPOzKGMXW83C95TxmXqpbK6olN4RevSfVjEAgCydH6HN6OhtOQEcnrU97r9H0iZOWwbw3pVrZiUkuRD1R56Wzs2wIDAQAB-----END PUBLIC KEY-----';
  const encryptor = new JSEncrypt();

  encryptor.setPublicKey(pub);

  return encryptor.encrypt(Base64.encode(password));
};

/** 默认导出 getWidth 与 rsaPsw。 */
export default {
  getWidth,
  rsaPsw,
};

/** 从文件名提取小写扩展名。 */
export const getFileExtension = (filename: string) =>
  filename.slice(filename.lastIndexOf('.') + 1).toLowerCase();
