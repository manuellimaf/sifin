import com.despegar.cloud.masterchef.service.SimpleSQLAnalyzer

object SimpleSQLAnalyzerTest extends App {

  val approved = "select * from USER WHERE id = 2228;\n\nupdate      USER \nset ip     = '10.1.1.3'\nwhere id = 2228;\n\nselect * from USER WHERE id = 2228;"
  val audited = "set autocommit=1\nSET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ\nSHOW SESSION VARIABLES LIKE \\'lower_case_table_names\\'\nSELECT current_user()\nUSE `sima`\nSELECT version()\nSHOW SESSION STATUS LIKE \\'Ssl_cipher\\'\nset autocommit=1\nSET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ\nSHOW SESSION VARIABLES LIKE \\'lower_case_table_names\\'\nSELECT current_user()\nSET CHARACTER SET utf8\nSET NAMES utf8\nSHOW SESSION VARIABLES LIKE \\'sql_mode\\'\nSELECT CONNECTION_ID()\nUSE `sima`\nSHOW SESSION STATUS LIKE \\'Ssl_cipher\\'\nUSE `sima`\nset autocommit=1\nset autocommit=1\nSET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ\nSHOW SESSION VARIABLES LIKE \\'lower_case_table_names\\'\nSELECT current_user()\nSET CHARACTER SET utf8\nSET NAMES utf8\nSET SQL_SAFE_UPDATES=1\nSELECT CONNECTION_ID()\nUSE `sima`\nSHOW SESSION STATUS LIKE \\'Ssl_cipher\\'\nUSE `sima`\nset autocommit=1\nSHOW SESSION VARIABLES LIKE \\'sql_mode\\'\nSHOW SESSION VARIABLES LIKE \\'version_comment\\'\nSHOW SESSION VARIABLES LIKE \\'version\\'\nSELECT current_user()\nSHOW SESSION VARIABLES LIKE \\'lower_case_table_names\\'\nUSE `sima`\nUSE `sima`\nSHOW SESSION VARIABLES LIKE \\'version_compile_os\\'\nSHOW DATABASES\nSHOW FULL TABLES FROM `sima`\nSELECT nam\nSHOW PROCEDURE STATUS WHERE Db=\\'sima\\'\nSHOW FUNCTION STATUS WHERE Db=\\'sima\\'\nselect * from USER WHERE id = 2228 LIMIT \nSHOW INDEX FROM `sima`.`USER`\nupdate USER  set ip = \\'10.1.1.3\\' where id = 2228\nselect * from USER WHERE id = 2228 LIMIT \nSHOW INDEX FROM `sima`.`USER`"

  val result = SimpleSQLAnalyzer.analyze(approved, audited.split("\\n").toSeq.map( s => ("172.16.2.58", s)))

  println(result)



}
