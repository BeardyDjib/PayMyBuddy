-- 1. Activer l’extension pgcrypto pour pouvoir utiliser crypt() et gen_salt()
CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- 2. Table des utilisateurs
CREATE TABLE app_user (
                          id       SERIAL PRIMARY KEY,           -- identifiant auto-incrémenté
                          username VARCHAR(100) NOT NULL,        -- jusqu’à 100 caractères
                          email    VARCHAR(100) NOT NULL UNIQUE, -- e-mail unique
                          password TEXT NOT NULL                 -- on stocke ici le hash du mot de passe
);


-- 3. Table des transactions
CREATE TABLE transaction (
                             id           SERIAL PRIMARY KEY,            -- identifiant auto-incrémenté
                             sender_id    INT     NOT NULL,              -- lien vers app_user(id)
                             receiver_id  INT     NOT NULL,              -- lien vers app_user(id)
                             description  VARCHAR(255),                  -- texte libre (facultatif)
                             amount       NUMERIC(10,2)  NOT NULL CHECK (amount > 0),  -- montant positif
                             fee_percent  NUMERIC(5,2)   NOT NULL DEFAULT 0.5,         -- 0,5 % par défaut

                             FOREIGN KEY (sender_id)   REFERENCES app_user(id),
                             FOREIGN KEY (receiver_id) REFERENCES app_user(id)
);

-- 4. Table de liaison pour les connexions user → user
CREATE TABLE user_connection (
                                 user_id       INT NOT NULL,                -- ID de l’utilisateur
                                 connection_id INT NOT NULL,                -- ID de son contact
                                 PRIMARY KEY (user_id, connection_id),

                                 FOREIGN KEY (user_id)       REFERENCES app_user(id),
                                 FOREIGN KEY (connection_id) REFERENCES app_user(id)
);


-- 5. Indexation pour accélérer les requêtes sur les expéditeurs et destinataires
CREATE INDEX idx_transaction_sender
    ON transaction(sender_id);

CREATE INDEX idx_transaction_receiver
    ON transaction(receiver_id);
