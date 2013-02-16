CREATE DATABASE `supergomoku`;
CREATE  TABLE `supergomoku`.`users` (
   `idusers` INT NOT NULL AUTO_INCREMENT ,
   `username` VARCHAR(45) NOT NULL ,
   `password` VARCHAR(45) NOT NULL ,
   PRIMARY KEY (`idusers`) );