CREATE TABLE category (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(45) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name_UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into category (name) values ('Supermercado');
insert into category (name) values ('Comida trabajo');
insert into category (name) values ('Comida fuera');
insert into category (name) values ('Salidas');
insert into category (name) values ('Transporte');
insert into category (name) values ('Educación');
insert into category (name) values ('Ropa');
insert into category (name) values ('Extras');

CREATE TABLE month (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(7) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name_UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into month (name) values ('2016/10');
insert into month (name) values ('2016/11');
insert into month (name) values ('2016/12');
