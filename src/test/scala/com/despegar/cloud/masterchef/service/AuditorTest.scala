package com.despegar.cloud.masterchef.service

import com.despegar.cloud.masterchef.connector._
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.mock.MockitoSugar

class AuditorTest extends FlatSpec with Matchers with MockitoSugar {

  val jira = Jira("testJira", SessionInProgress, Some("some-database"), Some("some-database-user"), Some(CloudiaUser("user","someMail",Seq(CloudiaGroup("name","mail")))), Seq("attachment"))
  val vaultConnectorMock = mock[VaultConnector]
  when(vaultConnectorMock.getDBMetadata("some-database")).thenReturn(DBMetadata("host",33033,"someAuditFile","someGroup"))

  "Auditor" should "validate a correct session" in {
    val jiraConnectorMock: JiraConnector = mock[JiraConnector]
    when(jiraConnectorMock.getApprovedSQL(jira)).thenReturn(approvedSql)

    val auditor = new MySQLAuditor {
      override val jiraConnector = jiraConnectorMock
      override val vaultConnector = vaultConnectorMock
      override def vaultConfig = null
      override def checkNoActiveSessions(user: String, db: String, metadata: DBMetadata): Unit = {}
      override def ssh(host: String, command: String, encoding: String = "utf-8"): Seq[String] = auditoryOk.split("\n").toSeq
      override def sendMail(jira: Jira, subject: String, body: String, group: Option[CloudiaGroup]): Unit = {}
    }

    val result = auditor.analyzeUserActivity(jira)

    result should equal(AnalysisOk)
  }

  "Auditor" should "validate a correct session even with comments from invasive workbenches" in {
    val jiraConnectorMock: JiraConnector = mock[JiraConnector]
    when(jiraConnectorMock.getApprovedSQL(jira)).thenReturn(approvedSql)

    val auditor = new MySQLAuditor {
      override val jiraConnector = jiraConnectorMock
      override val vaultConnector = vaultConnectorMock
      override def vaultConfig = null
      override def checkNoActiveSessions(user: String, db: String, metadata: DBMetadata): Unit = {}
      override def ssh(host: String, command: String, encoding: String = "utf-8"): Seq[String] = auditoryWithComments.split("\n").toSeq
      override def sendMail(jira: Jira, subject: String, body: String, group: Option[CloudiaGroup]): Unit = {}
    }

    val result = auditor.analyzeUserActivity(jira)

    result should equal(AnalysisOk)
  }

  "Auditor" should "not validate an anomalous session" in {
    val jiraConnectorMock: JiraConnector = mock[JiraConnector]
    when(jiraConnectorMock.getApprovedSQL(jira)).thenReturn(approvedSql2)

    val auditor = new MySQLAuditor {
      override val jiraConnector = jiraConnectorMock
      override val vaultConnector = vaultConnectorMock
      override def vaultConfig = null
      override def checkNoActiveSessions(user: String, db: String, metadata: DBMetadata): Unit = {}
      override def ssh(host: String, command: String, encoding: String = "utf-8"): Seq[String] = auditoryOk.split("\n").toSeq
      override def sendMail(jira: Jira, subject: String, body: String, group: Option[CloudiaGroup]): Unit = {}
    }

    val result = auditor.analyzeUserActivity(jira)

    result should not equal AnalysisOk
  }

  val approvedSql = "update INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID set ir.DOCUMENT_ISSUING_COUNTRY = ii.DOCUMENT_ISSUING_COUNTRY WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in ('1927460301','1927144501','1933623901','1927202801','1936211801','1935819701','1935639201','1935335301','1934486101','1933982001','1933316101','1933241201','1933156301','1933143901','1932981801','1932910601','1932564001','1932531101','1932378601','1932375501','1932026301','1931947001','1931873801','1931681801','1931685601','1931513301');\n"
  val approvedSql2 = "update INVOICE_REQUEST_OTHER ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID set ir.DOCUMENT_ISSUING_COUNTRY = ii.DOCUMENT_ISSUING_COUNTRY WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in ('1927460301','1927144501','1933623901','1927202801','1936211801','1935819701','1935639201','1935335301','1934486101','1933982001','1933316101','1933241201','1933156301','1933143901','1932981801','1932910601','1932564001','1932531101','1932378601','1932375501','1932026301','1931947001','1931873801','1931681801','1931685601','1931513301');DROP TABLE TEST;\n"

  val auditoryOk =
    """20160707 09:32:33,some-database,some-database-user,172.16.0.55,7421551,140793,QUERY,,'set autocommit=1',0
      |20160707 09:32:33,some-database,some-database-user,172.16.0.55,7421551,140794,QUERY,,'SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ',0 
      |20160707 09:32:33,some-database,some-database-user,172.16.0.55,7421551,140795,QUERY,,'SHOW SESSION VARIABLES LIKE \'lower_case_table_names\'',0 
      |20160707 09:32:34,some-database,some-database-user,172.16.0.55,7421551,140796,QUERY,,'SELECT current_user()',0 
      |20160707 09:32:34,some-database,some-database-user,172.16.0.55,7421551,140797,QUERY,,'SELECT version()',0 
      |20160707 09:32:34,some-database,some-database-user,172.16.0.55,7421551,140798,QUERY,,'SHOW SESSION STATUS LIKE \'Ssl_cipher\'',0 
      |20160707 09:32:43,some-database,some-database-user,172.16.0.55,7421557,140801,QUERY,,'set autocommit=1',0 
      |20160707 09:32:43,some-database,some-database-user,172.16.0.55,7421557,140802,QUERY,,'SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ',0 
      |20160707 09:32:43,some-database,some-database-user,172.16.0.55,7421557,140803,QUERY,,'SHOW SESSION VARIABLES LIKE \'lower_case_table_names\'',0 
      |20160707 09:32:43,some-database,some-database-user,172.16.0.55,7421557,140804,QUERY,,'SELECT current_user()',0 
      |20160707 09:32:43,some-database,some-database-user,172.16.0.55,7421557,140805,QUERY,,'SET CHARACTER SET utf8',0 
      |20160707 09:32:43,some-database,some-database-user,172.16.0.55,7421557,140806,QUERY,,'SET NAMES utf8',0 
      |20160707 09:32:43,some-database,some-database-user,172.16.0.55,7421557,140807,QUERY,,'SHOW SESSION VARIABLES LIKE \'sql_mode\'',0 
      |20160707 09:32:43,some-database,some-database-user,172.16.0.55,7421557,140808,QUERY,,'SELECT CONNECTION_ID()',0 
      |20160707 09:32:44,some-database,some-database-user,172.16.0.55,7421557,140809,QUERY,,'SHOW SESSION STATUS LIKE \'Ssl_cipher\'',0 
      |20160707 09:32:44,some-database,some-database-user,172.16.0.55,7421557,140810,QUERY,`somedb`,'USE `somedb`',0 
      |20160707 09:32:44,some-database,some-database-user,172.16.0.55,7421557,140811,QUERY,`somedb`,'set autocommit=1',0 
      |20160707 09:32:44,some-database,some-database-user,172.16.0.55,7421560,140812,QUERY,,'set autocommit=1',0 
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140813,QUERY,,'SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ',0 
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140814,QUERY,,'SHOW SESSION VARIABLES LIKE \'lower_case_table_names\'',0 
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140815,QUERY,,'SELECT current_user()',0 
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140816,QUERY,,'SET CHARACTER SET utf8',0 
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140817,QUERY,,'SET NAMES utf8',0 
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140818,QUERY,,'SELECT CONNECTION_ID()',0 
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140819,QUERY,,'SHOW SESSION STATUS LIKE \'Ssl_cipher\'',0 
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140820,QUERY,`somedb`,'USE `somedb`',0 
      |20160707 09:32:46,some-database,some-database-user,172.16.0.55,7421560,140821,QUERY,`somedb`,'set autocommit=1',0 
      |20160707 09:32:46,some-database,some-database-user,172.16.0.55,7421560,140822,QUERY,`somedb`,'SHOW SESSION VARIABLES LIKE \'sql_mode\'',0 
      |20160707 09:32:46,some-database,some-database-user,172.16.0.55,7421560,140823,QUERY,`somedb`,'SHOW SESSION VARIABLES LIKE \'version_comment\'',0 
      |20160707 09:32:46,some-database,some-database-user,172.16.0.55,7421560,140824,QUERY,`somedb`,'SHOW SESSION VARIABLES LIKE \'version\'',0 
      |20160707 09:32:46,some-database,some-database-user,172.16.0.55,7421560,140825,QUERY,`somedb`,'SELECT current_user()',0 
      |20160707 09:32:46,some-database,some-database-user,172.16.0.55,7421560,140826,QUERY,`somedb`,'SHOW SESSION VARIABLES LIKE \'lower_case_table_names\'',0 
      |20160707 09:32:46,some-database,some-database-user,172.16.0.55,7421557,140827,QUERY,`somedb`,'USE `somedb`',0 
      |20160707 09:32:47,some-database,some-database-user,172.16.0.55,7421560,140828,QUERY,`somedb`,'USE `somedb`',0 
      |20160707 09:32:47,some-database,some-database-user,172.16.0.55,7421557,140829,QUERY,`somedb`,'SHOW DATABASES',0 
      |20160707 09:32:47,some-database,some-database-user,172.16.0.55,7421560,140830,QUERY,`somedb`,'SHOW SESSION VARIABLES LIKE \'version_compile_os\'',0 
      |20160707 09:32:47,some-database,some-database-user,172.16.0.55,7421557,140831,QUERY,`somedb`,'SHOW DATABASES',0 
      |20160707 09:32:48,some-database,some-database-user,172.16.0.55,7421557,140832,QUERY,`somedb`,'SHOW FULL TABLES FROM `somedb`',0 
      |20160707 09:32:49,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SELECT name, type FROM mysql.proc WHERE Db=\'sima\'',1142 
      |20160707 09:32:49,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW PROCEDURE STATUS WHERE Db=\'sima\'',0 
      |20160707 09:32:49,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW FUNCTION STATUS WHERE Db=\'sima\'',0 
      |20160707 09:32:49,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_GE_BYTEARRAY`',0 
      |20160707 09:32:50,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_GE_PROPERTY`',0 
      |20160707 09:32:50,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_HI_ACTINST`',0 
      |20160707 09:32:50,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_HI_ATTACHMENT`',0 
      |20160707 09:32:51,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_HI_COMMENT`',0 
      |20160707 09:32:51,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_HI_DETAIL`',0 
      |20160707 09:32:52,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_HI_PROCINST`',0 
      |20160707 09:32:52,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_HI_TASKINST`',0 
      |20160707 09:32:52,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_RE_DEPLOYMENT`',0 
      |20160707 09:32:53,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_RE_PROCDEF`',0 
      |20160707 09:32:53,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_RU_EVENT_SUBSCR`',0 
      |20160707 09:32:53,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_RU_EXECUTION`',0 
      |20160707 09:32:54,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_RU_IDENTITYLINK`',0 
      |20160707 09:32:54,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_RU_JOB`',0 
      |20160707 09:32:54,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_RU_TASK`',0 
      |20160707 09:32:55,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ACT_RU_VARIABLE`',0 
      |20160707 09:32:55,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`AGENCY_NOTIFICATION`',0 
      |20160707 09:32:56,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`AGENT`',0 
      |20160707 09:32:56,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ASYNC_COMMAND`',0 
      |20160707 09:32:56,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ATTACHMENT_FILE`',0 
      |20160707 09:32:57,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BACKOFFICE_QUERY`',0 
      |20160707 09:32:57,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BANK_DEPOSIT`',0 
      |20160707 09:32:58,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BANK_DEPOSIT_REFUND`',0 
      |20160707 09:32:58,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BANK_TICKET`',0 
      |20160707 09:32:58,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BILLING_ADDRESS`',0 
      |20160707 09:32:59,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BILLING_INFORMATION`',0 
      |20160707 09:32:59,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BI_RESUMEN_HISTORY`',0 
      |20160707 09:32:59,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BI_RESUMEN_HISTORY_TMP`',0 
      |20160707 09:33:00,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BOOKING_CLASS`',0 
      |20160707 09:33:00,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BREAKDOWN_ITEM`',0 
      |20160707 09:33:00,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BREAKDOWN_ITEM_DETAIL`',0 
      |20160707 09:33:01,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`BUG_TOTEM_PROMO`',0 
      |20160707 09:33:01,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CADIVI_QUOTA_REQUEST`',0 
      |20160707 09:33:02,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CANCEL_FLIGHT_REQUEST`',0 
      |20160707 09:33:02,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CANCEL_FLIGHT_REQUEST_PAX`',0 
      |20160707 09:33:02,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CANCEL_PAYMENT`',0 
      |20160707 09:33:03,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CANCEL_PAYMENT_REQUEST`',0 
      |20160707 09:33:03,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CARD`',0 
      |20160707 09:33:03,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CARRIER_INFO`',0 
      |20160707 09:33:04,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CHANGE_FLIGHT_DATA_PAX`',0 
      |20160707 09:33:04,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CHANGE_FLIGHT_DATA_REQUEST`',0 
      |20160707 09:33:05,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CHANGE_FLIGHT_REQUEST`',0 
      |20160707 09:33:05,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CHANGE_FLIGHT_TICKET`',0 
      |20160707 09:33:06,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CHARGE_INCONSISTENCY_DETAIL`',0 
      |20160707 09:33:06,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CHARGE_INCONSISTENCY_REQUEST`',0 
      |20160707 09:33:06,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CHECK_REFUND`',0 
      |20160707 09:33:07,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CHECK_REFUND_METHOD`',0 
      |20160707 09:33:07,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CONVERSION_RATIO_INFO_TMP`',0 
      |20160707 09:33:07,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CORPORATE_TRANSACTION`',0 
      |20160707 09:33:08,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`COUPON`',0 
      |20160707 09:33:08,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`COUPON_REFUND`',0 
      |20160707 09:33:09,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`COUPON_REFUND_METHOD`',0 
      |20160707 09:33:09,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CREDIT_CARD_BIN`',0 
      |20160707 09:33:09,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CREDIT_CARD_REFUND`',0 
      |20160707 09:33:10,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CUSTOMER`',0 
      |20160707 09:33:10,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CUSTOMER_PAYMENT`',0 
      |20160707 09:33:10,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`CUSTOMER_REFUND`',0 
      |20160707 09:33:11,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`DEPOSIT_OPTION`',0 
      |20160707 09:33:11,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`DEPOSIT_PAYMENT_NOTIFICATION`',0 
      |20160707 09:33:12,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`DISPLAY_PRICE`',0 
      |20160707 09:33:12,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`DUPLICATED_TRANSACTION`',0 
      |20160707 09:33:12,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`DVAULT_CARD_DATA`',0 
      |20160707 09:33:13,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`DYNAPROV_INFORMATION`',0 
      |20160707 09:33:13,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`DYNAPROV_INFORMATION_ITEM`',0 
      |20160707 09:33:13,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`EMAIL`',0 
      |20160707 09:33:14,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`EVENT_ACTION_CONFIG`',0 
      |20160707 09:33:14,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`EVENT_ASYNC_COMMAND`',0 
      |20160707 09:33:14,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`EVENT_CLIENT_NOTIFICATION`',0 
      |20160707 09:33:15,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`EVENT_NOTIFICATION`',0
      |20160707 09:33:15,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`EVENT_NOTIFICATION_ERROR`',0
      |20160707 09:33:16,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`EVENT_SUSCRIBER_CLIENT`',0
      |20160707 09:33:16,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FARE_BASIS_CODE`',0
      |20160707 09:33:16,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FAT_ACCOUNTING_ERROR`',0
      |20160707 09:33:17,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT`',0
      |20160707 09:33:17,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_CHARGE`',0
      |20160707 09:33:17,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_CHARGE_HISTORY`',0
      |20160707 09:33:18,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_DISCOUNT`',0
      |20160707 09:33:18,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_DISCOUNT_AUDIT`',0
      |20160707 09:33:19,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_HISTORY`',0
      |20160707 09:33:19,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_INFO`',0
      |20160707 09:33:19,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_ITINERARY`',0
      |20160707 09:33:20,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_PROMO`',0
      |20160707 09:33:20,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_ROUTE`',0
      |20160707 09:33:21,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_SEAT_INFO`',0
      |20160707 09:33:21,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_SEGMENT`',0
      |20160707 09:33:21,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_SEGMENT_MEAL`',0
      |20160707 09:33:22,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_TICKET`',0
      |20160707 09:33:22,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLIGHT_TICKET_CHARGE`',0
      |20160707 09:33:22,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLOW_ASYNC_NON_TX_JOB`',0
      |20160707 09:33:23,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLOW_PROCESS_REQ`',0
      |20160707 09:33:23,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLOW_PROCESS_REQ_PARAM`',0
      |20160707 09:33:23,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLOW_SUSPENDED_PROCESS`',0
      |20160707 09:33:24,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLOW_SUSPENSION_STATUS`',0
      |20160707 09:33:24,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FLOW_VERSION_MAP`',0
      |20160707 09:33:25,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`FREQUENT_FLYER`',0
      |20160707 09:33:25,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`GDS_INFO`',0
      |20160707 09:33:25,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`GROSS_INCOME_HISTORY`',0
      |20160707 09:33:26,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`HISTORY_ATTACHMENT_TOKEN`',0
      |20160707 09:33:26,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`HUMAN_TASK`',0
      |20160707 09:33:26,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`INVOICE_DATA`',0
      |20160707 09:33:27,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`INVOICE_DOCUMENT`',0
      |20160707 09:33:27,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`INVOICE_INFORMATION`',0
      |20160707 09:33:28,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`INVOICE_NUMBER_ITEM`',0
      |20160707 09:33:28,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`INVOICE_REQUEST`',0
      |20160707 09:33:28,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ISSUE_CANCEL_CLERK`',0
      |20160707 09:33:29,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LEGAL_PIVOT`',0
      |20160707 09:33:29,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LEGAL_PIVOT_2K`',0
      |20160707 09:33:29,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LEGAL_REPORT`',0
      |20160707 09:33:30,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LEGAL_REPORT_MAILS`',0
      |20160707 09:33:30,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ACCOUNTING`',0
      |20160707 09:33:31,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ACCOUNTING_FAILURE`',0
      |20160707 09:33:31,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ACCOUNTING_REQUEST`',0
      |20160707 09:33:31,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ACCOUNTING_REQUEST_FAILURE`',0
      |20160707 09:33:32,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ACCOUNTING_RESULT`',0
      |20160707 09:33:32,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ALERT`',0
      |20160707 09:33:32,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ASYNC_JOB_ERROR`',0
      |20160707 09:33:33,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_BETA_ERROR_CHAS`',0
      |20160707 09:33:33,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_CHANGE_FLIGHT_CHARGES`',0
      |20160707 09:33:33,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_CREDIT_CARD_PAYMENT`',0
      |20160707 09:33:34,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_FLIGHT_STATE`',0
      |20160707 09:33:34,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_IMPORT_PNR_ERROR`',0
      |20160707 09:33:35,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_INPROCESS_MILES`',0
      |20160707 09:33:35,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_INVALIDATED_TOKEN`',0
      |20160707 09:33:35,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ISSUING_PCC`',0
      |20160707 09:33:35,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT id.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\',',1054
      |20160707 09:33:36,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_JOB_DATA`',0
      |20160707 09:33:36,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_NOTIFICATION_SERVICES`',0
      |20160707 09:33:37,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_OAS_ACCOUNT_RECEIVABLE`',0
      |20160707 09:33:37,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_OAS_ORDER_MANAGEMENT`',0
      |20160707 09:33:37,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_OAS_PAYMENT_ORDER`',0
      |20160707 09:33:38,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_OAS_SEND_REFUND`',0
      |20160707 09:33:38,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_ORIGINAL_TICKET_AND_PAX`',0
      |20160707 09:33:38,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_OSS_ADD_DEPOSIT`',0
      |20160707 09:33:39,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_PRODUCT_WORKLIST_FLOW`',0
      |20160707 09:33:40,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_QUEUE_HISTORY`',0
      |20160707 09:33:40,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_REJECTED_CREDIT_CARD_RECOVERED`',0
      |20160707 09:33:40,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_REQUEST_PROGRESS`',0
      |20160707 09:33:41,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\',',0
      |20160707 09:33:41,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_RISK_ANALYSIS`',0
      |20160707 09:33:41,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_SCHEDULE_CHANGE`',0
      |20160707 09:33:42,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_STRUCTURED_DATA_ERROR`',0
      |20160707 09:33:42,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_TICKETING_RESULT`',0
      |20160707 09:33:42,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_TRANSACTION_FLOW`',0
      |20160707 09:33:43,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_TRANSACTION_FLOW_DETAIL`',0
      |20160707 09:33:43,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`MILES_ISSUANCE_IN_PROCESS`',0
      |20160707 09:33:43,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`MILES_ISSUE_CONTRACT`',0
      |20160707 09:33:44,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`NOTIFICATION_CLIENT`',0
      |20160707 09:33:44,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`OFFLINE_FLIGHT_INFO`',0
      |20160707 09:33:44,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`OPEN_TICKET`',0
      |20160707 09:33:45,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`OPEN_TICKET_REQUEST`',0
      |20160707 09:33:45,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ORACLE_ACCOUNTING_STATUS`',0
      |20160707 09:33:46,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`ORDER_MANAGEMENT_INFO`',0
      |20160707 09:33:46,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAX`',0
      |20160707 09:33:46,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAX_PAYMENT`',0
      |20160707 09:33:47,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAYMENT_METHOD`',0
      |20160707 09:33:47,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAYMENT_METHOD_BANK_DEPOSIT`',0
      |20160707 09:33:47,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAYMENT_METHOD_BANK_TICKET`',0
      |20160707 09:33:48,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAYMENT_METHOD_COUPON`',0
      |20160707 09:33:48,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAYMENT_METHOD_CREDIT_CARD`',0
      |20160707 09:33:48,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAYMENT_METHOD_DEBIT_CARD`',0
      |20160707 09:33:49,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAYMENT_METHOD_LOCAL_CREDIT`',0
      |20160707 09:33:49,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PAYMENT_REQUEST`',0
      |20160707 09:33:50,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PCC_HISTORY`',0
      |20160707 09:33:50,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\') LIMIT 0, 1000',0
      |20160707 09:33:50,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PHONE`',0
      |20160707 09:33:51,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`POLCOM`',0
      |20160707 09:33:51,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PRICE_JUMP`',0
      |20160707 09:33:51,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PRODUCT`',0
      |20160707 09:33:52,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PRODUCT_AUTORIZATION`',0
      |20160707 09:33:52,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PRODUCT_CONFIG`',0
      |20160707 09:33:52,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PRODUCT_SPECIAL_REQUEST_TYPE`',0
      |20160707 09:33:53,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PROFIT_EXPLAIN`',0
      |20160707 09:33:53,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`PURCHASE_HISTORY`',0
      |20160707 09:33:54,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`QUALITY_ERROR_RESOLUTION`',0
      |20160707 09:33:54,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_ADMINISTRATIVE_DIVISION`',0
      |20160707 09:33:54,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_AIRPORT`',0
      |20160707 09:33:55,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_AREA`',0
      |20160707 09:33:55,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_ASSOCIATE_COLLECTION_PNR`',0
      |20160707 09:33:55,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_BACKOFFICE_QUERY_TYPE`',0
      |20160707 09:33:56,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_BANK_ACCOUNT`',0
      |20160707 09:33:56,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_BRAND`',0
      |20160707 09:33:56,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_CARD_TYPE`',0
      |20160707 09:33:57,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_CARRIER`',0
      |20160707 09:33:57,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_CHANNEL`',0
      |20160707 09:33:58,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_CITY`',0
      |20160707 09:33:58,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_COBRAND`',0
      |20160707 09:33:58,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_COMMENT_KEY`',0
      |20160707 09:33:59,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_COUNTRY`',0
      |20160707 09:33:59,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_COUPON_SECTOR`',0
      |20160707 09:33:59,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_COUPON_TYPE`',0
      |20160707 09:34:00,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_CREDIT_CARD_BIN`',0
      |20160707 09:34:00,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_CURRENCY`',0
      |20160707 09:34:01,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_DIAGNOSTIC`',0
      |20160707 09:34:01,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_DISCOUNT_REASON`',0
      |20160707 09:34:02,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_EVENT_TYPE`',0
      |20160707 09:34:02,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_FLIGHT_DISCOUNT_CONCEPT`',0
      |20160707 09:34:02,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_GDS_REMARK_ERROR`',0
      |20160707 09:34:03,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_HUMAN_TASK_TYPE`',0
      |20160707 09:34:03,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_IDENTIFICATION_TYPE`',0
      |20160707 09:34:04,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_MEAL`',0
      |20160707 09:34:04,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_MERCHANT`',0
      |20160707 09:34:04,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_MRW_AGENCY`',0
      |20160707 09:34:05,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_PAYMENT_CHANNEL`',0
      |20160707 09:34:05,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_PCC`',0
      |20160707 09:34:05,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_PROMO`',0
      |20160707 09:34:06,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_REASON_MESSAGE`',0
      |20160707 09:34:06,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_REFUND_MEDIA`',0
      |20160707 09:34:06,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_REQUEST_TYPE`',0
      |20160707 09:34:07,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_RESOLUTION`',0
      |20160707 09:34:07,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_RISK_ANALYSIS_RESOLUTION`',0
      |20160707 09:34:08,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_SPECIAL_REQUEST_SUB_TYPE`',0
      |20160707 09:34:08,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_SPECIAL_REQUEST_TYPE`',0
      |20160707 09:34:08,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_TIMER_TYPE`',0
      |20160707 09:34:09,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_VENDOR`',0
      |20160707 09:34:09,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RD_VENEZUELA_STATE`',0
      |20160707 09:34:10,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REFUND_APPROVAL_DATA`',0
      |20160707 09:34:10,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REFUND_ITEM`',0
      |20160707 09:34:10,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REFUND_REQUEST`',0
      |20160707 09:34:11,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REMARK`',0
      |20160707 09:34:11,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REMARK_INCONSISTENCY`',0
      |20160707 09:34:12,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REMARK_MFP`',0
      |20160707 09:34:12,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REQUEST`',0
      |20160707 09:34:12,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REQUEST_CHARGE`',0
      |20160707 09:34:13,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REQUEST_FLIGHT_TICKET`',0
      |20160707 09:34:13,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`REQUEST_PAX`',0
      |20160707 09:34:13,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RESCHEDULING_REQUEST`',0
      |20160707 09:34:14,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_BANK_ACCOUNT_SCOPE`',0
      |20160707 09:34:14,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_BRAND_COUNTRY_REASON_MESSAGE`',0
      |20160707 09:34:14,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_CANCEL_FLIGHT_REQUEST_ROUTE`',0
      |20160707 09:34:15,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_CHANGE_FLIGHT_DATA_REASON`',0
      |20160707 09:34:15,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_CONTACT_DETAIL`',0
      |20160707 09:34:16,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_CONTACT_DETAIL_BY_PRODUCT`',0
      |20160707 09:34:16,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_COUNTRY_REASON_MESSAGE`',0
      |20160707 09:34:16,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_COUPON_SECTOR_TYPE`',0
      |20160707 09:34:17,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_DIAGNOSTIC_SCOPE`',0
      |20160707 09:34:17,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_EVENT_BRAND_SUSCRIBER`',0
      |20160707 09:34:17,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_EVENT_CHANNEL_SUSCRIBER`',0
      |20160707 09:34:18,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_EVENT_COUNTRY_SUSCRIBER`',0
      |20160707 09:34:18,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_EVENT_SOURCE_SUSCRIBER`',0
      |20160707 09:34:18,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_EVENT_TYPE_SUSCRIBER`',0
      |20160707 09:34:19,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_FLIGHT_DISCOUNT_CONCEPT_REASON`',0
      |20160707 09:34:19,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_IDENTIFICATION_TYPE_SCOPE`',0
      |20160707 09:34:20,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_LAST_DISCOUNT_ACCOUNTED`',0
      |20160707 09:34:20,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_REASON_MESSAGE_PRODUCT_TASK`',0
      |20160707 09:34:21,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_REASON_MESSAGE_REQUEST_TASK`',0
      |20160707 09:34:21,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_REASON_MESSAGE_SCOPE`',0
      |20160707 09:34:21,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_REASON_MESSAGE_TASK`',0
      |20160707 09:34:22,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_REQUEST_TAX`',0
      |20160707 09:34:22,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_SPEC_REQ_CANCEL_REASON_SCOPE`',0
      |20160707 09:34:22,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_SUGGESTION_REQUEST_ATTACHMENT`',0
      |20160707 09:34:23,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_TICKET_SEGMENT`',0
      |20160707 09:34:23,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_TRIBUTARY_TYPE_SCOPE`',0
      |20160707 09:34:23,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_VENDOR_DIAGNOSTIC`',0
      |20160707 09:34:24,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`RL_VENDOR_SCOPE`',0
      |20160707 09:34:24,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`SCHEDULE_CHANGE_NOTIFICATION`',0
      |20160707 09:34:25,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`SEARCH_INSURANCE`',0
      |20160707 09:34:25,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`SECURE_AUTH_3D`',0
      |20160707 09:34:25,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`SPECIAL_REQUEST`',0
      |20160707 09:34:26,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`SPECIAL_REQUEST_TAG`',0
      |20160707 09:34:26,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`STOP_OVER`',0
      |20160707 09:34:26,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`SUGGESTION_REQUEST`',0
      |20160707 09:34:27,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TAG`',0
      |20160707 09:34:27,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TAX_DETAIL`',0
      |20160707 09:34:27,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TAX_INFO`',0
      |20160707 09:34:28,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TEMP_CARD`',0
      |20160707 09:34:28,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TEST_CONFIG`',0
      |20160707 09:34:29,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TEST_CRO_RESERVAS`',0
      |20160707 09:34:29,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TEST_CUSTOMER`',0
      |20160707 09:34:29,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TMP_BUG_AX`',0
      |20160707 09:34:30,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TMP_HISTORY_SEARCH`',0
      |20160707 09:34:30,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TOKEN_OPERATION`',0
      |20160707 09:34:30,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TRANSACTION`',0
      |20160707 09:34:31,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TRANSACTION_ID`',0
      |20160707 09:34:31,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TRANSACTION_ID_GENERATION_MODE`',0
      |20160707 09:34:32,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TRANSACTION_INFO`',0
      |20160707 09:34:32,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`TRANSACTION_VERSION_HISTORY`',0
      |20160707 09:34:32,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`USER`',0
      |20160707 09:34:33,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`USER_ACCESS_PERMISSION`',0
      |20160707 09:34:33,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`VALIDATE_PAYMENT_REQUEST`',0
      |20160707 09:34:33,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`VALIDATE_REMARKS_REQUEST`',0
      |20160707 09:34:34,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`V_EVENT`',0
      |20160707 09:34:34,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`V_TIMER`',0
      |20160707 09:34:35,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`WESTERN_UNION_REFUND`',0
      |20160707 09:34:35,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`WORKLIST_METADATA`',0
      |20160707 09:34:35,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`pp`',0
      |20160707 09:34:36,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`temp_cards`',0
      |20160707 09:34:36,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`temp_users`',0
      |20160707 09:36:06,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\') LIMIT 0, 1000',0 
      |20160707 09:40:23,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\',',0 
      |20160707 09:40:42,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'update INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID set ir.DOCUMENT_ISSUING_COUNTRY = ii.DOCUMENT_ISSUING_COUNTRY WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\')',0
      |20160707 09:40:45,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\',',0 
      |20160707 09:41:16,some-database,some-database-user,172.16.0.55,7421557,140900,QUERY,`somedb`,'SHOW FULL TABLES FROM `ii`',1044 
      |20160707 09:41:24,some-database,some-database-user,172.16.0.55,7421557,140901,QUERY,`somedb`,'SHOW FULL TABLES FROM `ir`',1044 
      |20160707 09:41:24,some-database,some-database-user,172.16.0.55,7421557,140902,QUERY,`somedb`,'SHOW FULL TABLES FROM `ir`',1044 
      |20160707 09:41:26,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND ii.DOCUMENT_ISSUING_COUNTRY <> ir.DOCUMENT_ISSUING_COUNTRY AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001',0""".stripMargin

  val auditoryWithComments =
    """20160707 09:32:33,some-database,some-database-user,172.16.0.55,7421551,140793,QUERY,,'set autocommit=1',0
      |20160707 09:32:45,some-database,some-database-user,172.16.0.55,7421560,140820,QUERY,`somedb`,'USE `somedb`',0
      |20160707 09:32:46,some-database,some-database-user,172.16.0.55,7421560,140821,QUERY,`somedb`,'set autocommit=1',0
      |20160707 09:33:35,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT id.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\',',1054
      |20160707 09:33:36,some-database,some-database-user,172.16.0.55,7421557,140833,QUERY,`somedb`,'SHOW COLUMNS FROM `somedb`.`LOG_JOB_DATA`',0
      |20160707 09:36:06,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\') LIMIT 0, 1000',0
      |20160707 09:40:23,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\',',0
      |20160707 09:40:42,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'/* ApplicationName=DBeaver Enterprise 3.7.1 - Main */ update INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID set ir.DOCUMENT_ISSUING_COUNTRY = ii.DOCUMENT_ISSUING_COUNTRY WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\')',0
      |20160707 09:40:45,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001\',\'1931873801\',\'1931681801\',\'1931685601\',\'1931513301\',',0
      |20160707 09:41:16,some-database,some-database-user,172.16.0.55,7421557,140900,QUERY,`somedb`,'SHOW FULL TABLES FROM `ii`',1044
      |20160707 09:41:24,some-database,some-database-user,172.16.0.55,7421557,140901,QUERY,`somedb`,'SHOW FULL TABLES FROM `ir`',1044
      |20160707 09:41:24,some-database,some-database-user,172.16.0.55,7421557,140902,QUERY,`somedb`,'SHOW FULL TABLES FROM `ir`',1044
      |20160707 09:41:26,some-database,some-database-user,172.16.0.55,7421560,140835,QUERY,`somedb`,'SELECT ir.id, ir.DOCUMENT_ISSUING_COUNTRY, ii.DOCUMENT_ISSUING_COUNTRY FROM INVOICE_REQUEST ir INNER JOIN REQUEST r ON r.ID = ir.REQUEST_ID INNER JOIN PRODUCT p ON r.PRODUCT_ID = p.ID INNER JOIN sima.TRANSACTION t ON p.TRANSACTION_ID = t.ID INNER JOIN INVOICE_INFORMATION ii ON t.ID = ii.TRANSACTION_ID WHERE ii.DOCUMENT_ISSUING_COUNTRY is not null AND ii.DOCUMENT_ISSUING_COUNTRY <> ir.DOCUMENT_ISSUING_COUNTRY AND t.TRANSACTION_CODE in (\'1927460301\',\'1927144501\',\'1933623901\',\'1927202801\',\'1936211801\',\'1935819701\',\'1935639201\',\'1935335301\',\'1934486101\',\'1933982001\',\'1933316101\',\'1933241201\',\'1933156301\',\'1933143901\',\'1932981801\',\'1932910601\',\'1932564001\',\'1932531101\',\'1932378601\',\'1932375501\',\'1932026301\',\'1931947001',0""".stripMargin

}
