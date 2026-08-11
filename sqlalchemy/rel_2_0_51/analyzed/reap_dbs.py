"""Drop Oracle Database, SQL Server databases that are left over from a

# 多进程测试后清理残留 Oracle/SQL Server 数据库（驱动连接未及时释放时）
"""Drop Oracle Database, SQL Server databases that are left over from a
multiprocessing test run.

Currently the cx_Oracle driver seems to sometimes not release a
TCP connection even if close() is called, which prevents the provisioning
system from dropping a database in-process.

For SQL Server, databases still remain in use after tests run and
running a kill of all detected sessions does not seem to release the
database in process.

"""

import logging
import sys

from sqlalchemy.testing import provision

logging.basicConfig()
logging.getLogger(provision.__name__).setLevel(logging.INFO)

# 读取 db_idents 文件并调用 testing.provision 回收数据库
provision.reap_dbs(sys.argv[1])
