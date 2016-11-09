
create table SHOP_MST (
  SHOP_ID char(8) not null,
  SHOP_NAME varchar(20) not null,
  CRE_USER varchar(16) not null,
  CRE_DATE timestamp not null,
  UPD_USER varchar(16) not null,
  UPD_DATE timestamp not null,
  primary key (SHOP_ID)
);

create table ITEM_MST (
  ITEM_ID char(8) not null,
  ITEM_NAME varchar(20) not null,
  STD_AMOUNT decimal(10,2) not null,
  CRE_USER varchar(16) not null,
  CRE_DATE timestamp not null,
  UPD_USER varchar(16) not null,
  UPD_DATE timestamp not null,
  primary key (ITEM_ID)
);

create table STOCK_TBL (
  SHOP_ID char(8) not null,
  ITEM_ID char(8) not null,
  AMOUNT decimal(10,2) not null,
  QUANTITY decimal(10,2) not null,
  EXPIRE_DATE char(8),
  CRE_USER varchar(16) not null,
  CRE_DATE timestamp not null,
  UPD_USER varchar(16) not null,
  UPD_DATE timestamp not null,
  primary key (SHOP_ID,ITEM_ID)
);

create table TDG_TEST1 (
  KEY1 char(2) not null,
  VAL1 char(8),
  VAL2 char(8),
  primary key (KEY1)
);

create table TDG_TEST2 (
  KEY1 char(2) not null,
  KEY2 char(4) not null,
  VAL1 char(8),
  VAL2 integer,
  primary key (KEY1,KEY2)
);

create table TDG_TEST3 (
  KEY1 char(4) not null,
  FKEY1 char(2),
  FKEY2 char(4),
  VAL1 char(8),
  VAL2 integer,
  primary key (KEY1)
);
