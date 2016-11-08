
CREATE TABLE Accounts (id INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(40) NOT NULL UNIQUE,
    PASSWORD VARCHAR(100) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    pw_reset_token VARCHAR(50),
    failed_login_attempts INT NOT NULL,
    PRIMARY KEY (ID));
        
CREATE TABLE Players (id INT NOT NULL AUTO_INCREMENT,
    account_id INT NOT NULL,
    username VARCHAR(40) NOT NULL UNIQUE,
    rating INT,
    PRIMARY KEY (ID),
    FOREIGN KEY (account_id) REFERENCES Accounts(id));
    
CREATE TABLE Matches (id INT NOT NULL AUTO_INCREMENT,
    player1_id INT NOT NULL,
    player2_id INT NOT NULL,
    username1 VARCHAR(40) NOT NULL,
    username2 VARCHAR(40) NOT NULL,
    rating_before_player1 INT NOT NULL,
    rating_before_player2 INT NOT NULL,
    status INT NOT NULL,
    created_at VARCHAR(50) NOT NULL,
    winner_username VARCHAR(40),
    rating_change_player1 INT,
    rating_change_player2 INT,
    finished_at VARCHAR(50),
    PRIMARY KEY (ID));