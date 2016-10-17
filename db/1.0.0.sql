CREATE TABLE category (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(4) NOT NULL,
  description varchar(45) NOT NULL,
  is_tax tinyint(1) NOT NULL default 0,
  PRIMARY KEY (id),
  UNIQUE KEY name_UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into category (name, is_tax, description) values ('SUP', 0, 'Supermercado');
insert into category (name, is_tax, description) values ('CT', 0, 'Comida trabajo');
insert into category (name, is_tax, description) values ('CF', 0, 'Comida fuera');
insert into category (name, is_tax, description) values ('SAL', 0, 'Salidas');
insert into category (name, is_tax, description) values ('TRAN', 0, 'Transporte');
insert into category (name, is_tax, description) values ('EDU', 0, 'Educación');
insert into category (name, is_tax, description) values ('ROP', 0, 'Ropa');
insert into category (name, is_tax, description) values ('EXT', 0, 'Extras');
insert into category (name, is_tax, description) values ('TAX', 1, 'Fijos');

CREATE TABLE month (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(7) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name_UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into month (name) values ('2016/09');
insert into month (name) values ('2016/10');
insert into month (name) values ('2016/11');
insert into month (name) values ('2016/12');

CREATE TABLE income (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  own tinyint(1) NOT NULL default 1,
  amount decimal(20,6),
  month_id bigint(20) NOT NULL,
  currency_id bigint(20) NOT NULL,
  description varchar(256) default '',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE currency (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  symbol varchar(3) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY symbol_UNIQUE (symbol)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into currency(symbol) values ('$');
insert into currency(symbol) values ('u$d');

CREATE TABLE exchange_rate (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  amount decimal(20,6),
  month_id bigint(20) NOT NULL,
  currency_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY month_curr_UNIQUE (month_id, currency_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE payment_method (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(4) NOT NULL,
  type varchar(4) NOT NULL,
  description varchar(45) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name_UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

insert into payment_method (name, description, type) values ('EF', 'Efectivo', 'cash');
insert into payment_method (name, description, type) values ('AR','Amex rio', 'tc');
insert into payment_method (name, description, type) values ('MC', 'Master citi', 'tc');
insert into payment_method (name, description, type) values ('VF', 'Visa francés', 'tc');
insert into payment_method (name, description, type) values ('VC', 'Visa citi', 'tc');
insert into payment_method (name, description, type) values ('VR', 'Visa rio', 'tc');

CREATE TABLE saving (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  amount decimal(20,6) NOT NULL,
  balance decimal(20,6) NOT NULL,
  month_id bigint(20) NOT NULL,
  currency_id bigint(20) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE investment (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  amount decimal(20,6) NOT NULL,
  balance decimal(20,6) NOT NULL,
  month_id bigint(20) NOT NULL,
  currency_id bigint(20) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TABLE expense (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  amount decimal(20,6) NOT NULL,
  day int NOT NULL,
  month_id bigint(20) NOT NULL,
  currency_id bigint(20) NOT NULL,
  payment_method_id bigint(20) NOT NULL,
  category_id bigint(20) NOT NULL,
  description varchar(256) default '',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


-- Test data (review FKs)
insert into income (own, amount, month_id, currency_id, description) values (0, 1200,1,1,'Deuda Germán');
insert into income (own, amount, month_id, currency_id, description) values (1,38922.13,1,1,'Sueldo');
insert into income (own, amount, month_id, currency_id, description) values (0, 1000,1,1,'Leo (regalo Marco)');

insert into exchange_rate (amount, month_id, currency_id) values (15.6, 1, 2);
insert into exchange_rate (amount, month_id, currency_id) values (15.88, 2, 2);
insert into exchange_rate (amount, month_id, currency_id) values (16.14, 3, 2);

insert into saving (amount, balance, month_id, currency_id) values (25100, 14322.05, 4, 1);
insert into saving (amount, balance, month_id, currency_id) values (1000, 53991.41, 4, 2);

insert into investment (amount, balance, month_id, currency_id) values (-10000, 15000, 4, 1);


insert into expense (amount, day, month_id, currency_id, payment_method_id, category_id, description) values (235,15,1,1,1,8,'');