
create database line_learning;

use line_learning;

CREATE TABLE user (
  id       CHAR(40) NOT NULL PRIMARY KEY,
  email    VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL
);

CREATE UNIQUE INDEX email_unique_index ON user (email);

CREATE TABLE scene (
  id      CHAR(40) NOT NULL PRIMARY KEY,
  name    VARCHAR(255) NOT NULL,
  user_id CHAR(40) NOT NULL
);

CREATE UNIQUE INDEX name_user_index ON scene (name, user_id);

CREATE TABLE cue_line (
  id       CHAR(40) NOT NULL PRIMARY KEY,
  cue      VARCHAR(32727) NOT NULL,
  line     VARCHAR(32727) NOT NULL,
  ord      INTEGER NOT NULL,
  scene_id CHAR(40) NOT NULL
);

CREATE UNIQUE INDEX scene_id_order_index ON cue_line (scene_id, ord);

ALTER TABLE scene ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES user (id)
  ON UPDATE NO ACTION
  ON DELETE NO ACTION;

ALTER TABLE cue_line ADD CONSTRAINT scene_fk FOREIGN KEY (scene_id) REFERENCES scene (id)
  ON UPDATE NO ACTION
  ON DELETE NO ACTION;