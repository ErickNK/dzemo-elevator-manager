CREATE TABLE `elevators` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elevator_tag` bigint NOT NULL,
  `floor` int NOT NULL DEFAULT '0',
  `state` varchar(45) DEFAULT NULL,
  `direction` varchar(45) DEFAULT NULL,
  `door_state` varchar(45) DEFAULT NULL,
  `created_date` timestamp NULL DEFAULT NULL,
  `updated_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `elevator_audit_trail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `elevator_id` bigint NOT NULL,
  `floor` int NOT NULL DEFAULT '0',
  `state` varchar(45) DEFAULT NULL,
  `direction` varchar(45) DEFAULT NULL,
  `door_state` varchar(45) DEFAULT NULL,
  `created_date` timestamp NULL DEFAULT NULL,
  `updated_date` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci