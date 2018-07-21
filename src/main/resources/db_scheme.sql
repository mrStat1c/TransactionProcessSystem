-- MySQL dump 10.13  Distrib 5.6.17, for Win64 (x86_64)
--
-- Host: localhost    Database: processing
-- ------------------------------------------------------
-- Server version	5.5.23

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `processing`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `processing` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `processing`;

--
-- Table structure for table `files`
--

DROP TABLE IF EXISTS `files`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `files` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `record_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `files`
--

LOCK TABLES `files` WRITE;
/*!40000 ALTER TABLE `files` DISABLE KEYS */;
/*!40000 ALTER TABLE `files` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `loyalty_txns`
--

DROP TABLE IF EXISTS `loyalty_txns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `loyalty_txns` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `record_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `card` varchar(16) NOT NULL,
  `card_type` varchar(45) NOT NULL,
  `order_number` int(11) NOT NULL,
  `settl_sum` decimal(10,2) NOT NULL,
  `bonus_sum` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `loyalty_txns`
--

LOCK TABLES `loyalty_txns` WRITE;
/*!40000 ALTER TABLE `loyalty_txns` DISABLE KEYS */;
/*!40000 ALTER TABLE `loyalty_txns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_indicators`
--

DROP TABLE IF EXISTS `order_indicators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_indicators` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_number` int(11) NOT NULL,
  `indicator` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_indicators`
--

LOCK TABLES `order_indicators` WRITE;
/*!40000 ALTER TABLE `order_indicators` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_indicators` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_positions`
--

DROP TABLE IF EXISTS `order_positions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_positions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_number` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `orig_price` decimal(10,2) NOT NULL,
  `settl_price` decimal(10,2) NOT NULL,
  `count` int(11) NOT NULL,
  `rejected` varchar(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_positions`
--

LOCK TABLES `order_positions` WRITE;
/*!40000 ALTER TABLE `order_positions` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_positions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `number` int(11) NOT NULL,
  `sale_point_id` int(11) NOT NULL,
  `order_date` datetime NOT NULL,
  `record_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `card_id` int(11) DEFAULT NULL,
  `file_id` int(11) NOT NULL,
  `ccy_id` int(11) NOT NULL,
  `sum` decimal(10,2) NOT NULL,
  `rejected` varchar(1) NOT NULL,
  `sale_point_order_num` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `file_fk` (`file_id`),
  CONSTRAINT `file_fk` FOREIGN KEY (`file_id`) REFERENCES `files` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER delete_order_dependent_tables AFTER DELETE ON orders
FOR EACH ROW
BEGIN
DELETE FROM order_positions WHERE order_number = OLD.number;
DELETE FROM order_indicators WHERE order_number = OLD.number;
DELETE FROM rejects WHERE order_number = OLD.number;
DELETE FROM loyalty_txns WHERE order_number = OLD.number;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rejects`
--

DROP TABLE IF EXISTS `rejects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rejects` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `file_name` varchar(45) NOT NULL,
  `order_number` int(11) DEFAULT NULL,
  `order_position_number` int(11) DEFAULT NULL,
  `type` varchar(45) NOT NULL,
  `code` int(11) NOT NULL,
  `incorrect_field_value` varchar(45) DEFAULT NULL,
  `record_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rejects`
--

LOCK TABLES `rejects` WRITE;
/*!40000 ALTER TABLE `rejects` DISABLE KEYS */;
/*!40000 ALTER TABLE `rejects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'processing'
--

--
-- Current Database: `dictionaries`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `dictionaries` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `dictionaries`;

--
-- Table structure for table `card_status_history`
--

DROP TABLE IF EXISTS `card_status_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `card_status_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `card_id` int(11) NOT NULL,
  `begin_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `card_status_fk` (`status_id`),
  KEY `card_fk` (`card_id`),
  CONSTRAINT `card_fk` FOREIGN KEY (`card_id`) REFERENCES `cards` (`id`) ON DELETE CASCADE,
  CONSTRAINT `card_status_fk` FOREIGN KEY (`status_id`) REFERENCES `card_statuses` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `card_status_history`
--

LOCK TABLES `card_status_history` WRITE;
/*!40000 ALTER TABLE `card_status_history` DISABLE KEYS */;
INSERT INTO `card_status_history` VALUES (4,4563,'2017-01-08 07:14:12',1),(5,4563,'2018-01-04 07:14:12',2),(6,7583,'2018-01-08 07:14:12',1),(7,4563,'2018-01-08 07:14:12',1);
/*!40000 ALTER TABLE `card_status_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `card_statuses`
--

DROP TABLE IF EXISTS `card_statuses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `card_statuses` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `status` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `status_UNIQUE` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `card_statuses`
--

LOCK TABLES `card_statuses` WRITE;
/*!40000 ALTER TABLE `card_statuses` DISABLE KEYS */;
INSERT INTO `card_statuses` VALUES (1,'Active'),(2,'Blocked'),(3,'Issued');
/*!40000 ALTER TABLE `card_statuses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cards`
--

DROP TABLE IF EXISTS `cards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cards` (
  `id` int(11) NOT NULL,
  `number` varchar(16) NOT NULL,
  `type` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cards`
--

LOCK TABLES `cards` WRITE;
/*!40000 ALTER TABLE `cards` DISABLE KEYS */;
INSERT INTO `cards` VALUES (0,'0','0'),(4563,'1234567890123456','Silver'),(7583,'9483478347855543','Green');
/*!40000 ALTER TABLE `cards` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `currencies`
--

DROP TABLE IF EXISTS `currencies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `currencies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(3) NOT NULL,
  `description` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code_UNIQUE` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `currencies`
--

LOCK TABLES `currencies` WRITE;
/*!40000 ALTER TABLE `currencies` DISABLE KEYS */;
INSERT INTO `currencies` VALUES (1,'RUB','Russian Rubles'),(2,'EUR','Euro'),(3,'---','Unknown currency');
/*!40000 ALTER TABLE `currencies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `currency_courses`
--

DROP TABLE IF EXISTS `currency_courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `currency_courses` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ccy_id` int(11) NOT NULL,
  `date` date NOT NULL,
  `course` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `currencies_fk` (`ccy_id`),
  CONSTRAINT `currencies_fk` FOREIGN KEY (`ccy_id`) REFERENCES `currencies` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `currency_courses`
--

LOCK TABLES `currency_courses` WRITE;
/*!40000 ALTER TABLE `currency_courses` DISABLE KEYS */;
INSERT INTO `currency_courses` VALUES (3,1,'2018-07-21',1.00),(4,2,'2018-07-21',69.69),(5,1,'2017-12-28',1.00),(6,1,'2018-05-12',1.00),(7,2,'2018-05-12',69.69);
/*!40000 ALTER TABLE `currency_courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_lines`
--

DROP TABLE IF EXISTS `product_lines`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_lines` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_lines`
--

LOCK TABLES `product_lines` WRITE;
/*!40000 ALTER TABLE `product_lines` DISABLE KEYS */;
INSERT INTO `product_lines` VALUES (0,'UNKNOWN'),(1,'ALCOHOL'),(2,'MEET'),(3,'UNDEFINIED');
/*!40000 ALTER TABLE `product_lines` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `products` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  `product_line_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `product_line_fk` (`product_line_id`),
  CONSTRAINT `product_line_fk` FOREIGN KEY (`product_line_id`) REFERENCES `product_lines` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (0,'undefined',0),(1,'FDHG54',2),(2,'FKS63',1),(3,'DSG456',2),(4,'GFDG345',2);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sale_point_agreements`
--

DROP TABLE IF EXISTS `sale_point_agreements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sale_point_agreements` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sale_point_id` int(11) DEFAULT NULL,
  `type` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `sale_points_fk` (`sale_point_id`),
  CONSTRAINT `sale_points_fk` FOREIGN KEY (`sale_point_id`) REFERENCES `sale_points` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sale_point_agreements`
--

LOCK TABLES `sale_point_agreements` WRITE;
/*!40000 ALTER TABLE `sale_point_agreements` DISABLE KEYS */;
INSERT INTO `sale_point_agreements` VALUES (1,435211,'LateDispatch'),(2,435211,'ForeignCurrency');
/*!40000 ALTER TABLE `sale_point_agreements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sale_points`
--

DROP TABLE IF EXISTS `sale_points`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sale_points` (
  `id` int(11) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sale_points`
--

LOCK TABLES `sale_points` WRITE;
/*!40000 ALTER TABLE `sale_points` DISABLE KEYS */;
INSERT INTO `sale_points` VALUES (0,'UNKNOWN'),(163456,'GOI234'),(435211,'FjKjf34');
/*!40000 ALTER TABLE `sale_points` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'dictionaries'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-07-21 18:37:07
