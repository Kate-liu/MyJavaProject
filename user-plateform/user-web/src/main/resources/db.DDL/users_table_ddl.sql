DROP TABLE users;


CREATE TABLE users
(
    id          INT         NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    name        VARCHAR(16) NOT NULL,
    password    VARCHAR(64) NOT NULL,
    email       VARCHAR(64) NOT NULL,
    phoneNumber VARCHAR(32) NOT NULL

)