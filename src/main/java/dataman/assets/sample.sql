
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
