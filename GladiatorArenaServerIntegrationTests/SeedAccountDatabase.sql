
USE GladiatorArenaServer;
DELETE FROM Matches;
DELETE FROM Players;
DELETE FROM Accounts;
INSERT INTO Accounts VALUES(DEFAULT, "tester", "$2a$08$FwfADf4UV2oaQ75xMRHKZO/0ETEB5asMk63YquyAtv1GjnxW1aKqC", "tester@test.com", NULL, 0);
INSERT INTO Accounts VALUES(DEFAULT, "tester2", "$2a$08$FwfADf4UV2oaQ75xMRHKZO/0ETEB5asMk63YquyAtv1GjnxW1aKqC", "tester2@test.com", NULL, 0);
