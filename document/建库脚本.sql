/*
SQLyog 企业版 - MySQL GUI v8.14 
MySQL - 5.1.73 : Database - obd_fulr
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*Table structure for table `t_device_push_msg` */

DROP TABLE IF EXISTS `t_device_push_msg`;

CREATE TABLE `t_device_push_msg` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `device_id` bigint(20) unsigned NOT NULL COMMENT '设备ID',
  `msg_type` int(10) unsigned NOT NULL COMMENT '消息类型，50:查询_CAN数据定时回传参数,81:设置_CAN数据定时回传参数',
  `msg_data` blob COMMENT '消息参数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `temp_flag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC COMMENT='盒子命令推送表';

/*Table structure for table `t_device_push_msg_history` */

DROP TABLE IF EXISTS `t_device_push_msg_history`;

CREATE TABLE `t_device_push_msg_history` (
  `id` bigint(20) unsigned NOT NULL,
  `device_id` bigint(20) unsigned NOT NULL COMMENT '设备ID',
  `msg_type` int(10) unsigned NOT NULL COMMENT '消息类型，',
  `msg_data` blob COMMENT '消息参数',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `temp_flag` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC COMMENT='盒子命令历史推送表';

/*Table structure for table `t_device_reserve` */

DROP TABLE IF EXISTS `t_device_reserve`;

CREATE TABLE `t_device_reserve` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sn` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '设备标识',
  `type` int(11) DEFAULT NULL COMMENT '0:未知,1:元征OBD盒子,2:迪娜OBD盒子',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci CHECKSUM=1 DELAY_KEY_WRITE=1 ROW_FORMAT=DYNAMIC;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
