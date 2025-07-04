MPD

CREATE TABLE User (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
);

CREATE TABLE Transaction (
  id INT PRIMARY KEY AUTO_INCREMENT,
  sender_id INT NOT NULL,
  receiver_id INT NOT NULL,
  description VARCHAR(255),
  amount DOUBLE NOT NULL,
  FOREIGN KEY (sender_id) REFERENCES User(id),
  FOREIGN KEY (receiver_id) REFERENCES User(id)
);

CREATE TABLE User_Connection (
  user_id INT NOT NULL,
  connection_id INT NOT NULL,
  PRIMARY KEY (user_id, connection_id),
  FOREIGN KEY (user_id) REFERENCES User(id),
  FOREIGN KEY (connection_id) REFERENCES User(id)
);
