
DROP DATABASE GladiatorArenaServer;
CREATE DATABASE GladiatorArenaServer;
USE GladiatorArenaServer;

DROP USER GladArUsr;
FLUSH PRIVILEGES;
CREATE USER GladArUsr IDENTIFIED BY 'password'; 

GRANT USAGE ON *.* TO 'GladArUsr'@'%' IDENTIFIED BY 'password'; 
GRANT ALL PRIVILEGES ON GladiatorArenaServer.* TO 'GladArUsr'@'%'; 
