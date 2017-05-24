SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `sj_auth`
--

-- --------------------------------------------------------

--
-- Table structure for table `oauth_client_details`
--

DROP TABLE IF EXISTS `oauth_client_details`;
CREATE TABLE IF NOT EXISTS `oauth_client_details` (
  `client_id` varchar(64) NOT NULL,
  `resource_ids` varchar(256) DEFAULT NULL,
  `client_secret` varchar(256) DEFAULT NULL,
  `scope` varchar(256) DEFAULT NULL,
  `authorized_grant_types` varchar(256) DEFAULT NULL,
  `web_server_redirect_uri` varchar(256) DEFAULT NULL,
  `authorities` varchar(256) DEFAULT NULL,
  `access_token_validity` int(11) DEFAULT NULL,
  `refresh_token_validity` int(11) DEFAULT NULL,
  `additional_information` varchar(4096) DEFAULT NULL,
  `autoapprove` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `oauth_client_details`
--

INSERT INTO `oauth_client_details` (`client_id`, `resource_ids`, `client_secret`, `scope`, `authorized_grant_types`, `web_server_redirect_uri`, `authorities`, `access_token_validity`, `refresh_token_validity`, `additional_information`, `autoapprove`) VALUES
  ('server_client', NULL, 'v-%kXyBTm%wf2T+JsPv_SjHP2d8*Fq#XYwE+m@pJM5EeZf?-^fXX&_$m34#e89H@8Ss=LQG^&^!!sXw^j#*MJSmuuP%_ZupfxYd@gxCxF%rTLy%wN%7ENtCa%BMD!ZXP', 'read', 'client_credentials', NULL, 'ROLE_APPLICATION', NULL, NULL, NULL, 'true'),
  ('user_cred', NULL, 'supersecret', 'read,write', 'password,refresh_token', NULL, 'ROLE_CLIENT', 6000000, 9000000, NULL, 'true'),
  ('vending_admin', NULL, 'supersecret', 'read,write', 'authorization_code,refresh_token', 'https://sjcoins.testing.softjourn.if.ua/vending/sso', 'ROLE_CLIENT', 6000000, 9000000, NULL, 'true'),
  ('vending_server', NULL, '6fM33QCmv9e8CnTLBZ85A2mF', 'rollback', 'client_credentials', NULL, 'ROLE_CLIENT', 6000000, 9000000, NULL, 'true');

-- --------------------------------------------------------

--
-- Table structure for table `refresh_tokens`
--

DROP TABLE IF EXISTS `refresh_tokens`;
CREATE TABLE IF NOT EXISTS `refresh_tokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `expiration` datetime NOT NULL,
  `value` varchar(1024) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17580 DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
CREATE TABLE IF NOT EXISTS `role` (
  `id` varchar(255) NOT NULL,
  `super_role` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `role`
--

INSERT INTO `role` (`id`, `super_role`) VALUES
  ('ROLE_BILLING', b'0'),
  ('ROLE_INVENTORY', b'0'),
  ('ROLE_SUPER_ADMIN', b'1'),
  ('ROLE_USER_MANAGER', b'0');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `full_name` varchar(255) DEFAULT NULL,
  `email` varchar(256) DEFAULT NULL,
  `ldap_id` varchar(255) NOT NULL,
  PRIMARY KEY (`ldap_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `users_role`
--

DROP TABLE IF EXISTS `users_role`;
CREATE TABLE IF NOT EXISTS `users_role` (
  `user_ldap_id` varchar(255) NOT NULL,
  `role_id` varchar(255) NOT NULL,
  PRIMARY KEY (`user_ldap_id`,`role_id`),
  KEY `FK3qjq7qsiigxa82jgk0i0wuq3g` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `users_role`
--
ALTER TABLE `users_role`
  ADD CONSTRAINT `FK3qjq7qsiigxa82jgk0i0wuq3g` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  ADD CONSTRAINT `FKc64xim204iatckmrt58bn9i0j` FOREIGN KEY (`user_ldap_id`) REFERENCES `users` (`ldap_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
