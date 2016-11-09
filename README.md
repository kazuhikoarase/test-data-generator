test data generator
===

## 使い方

接続先のデータベースを config.js に設定します。

```javascript
var config = {

  dataSources : [
    {
      // sample database.
      name : 'TDGDB',
      url : 'jdbc:hsqldb:mem:tdgdb',
      driverClassName : 'org.hsqldb.jdbcDriver',
      info : { user : 'SA', password : '' },
      initSample : true
    }
    /*,{
      name : 'mydb',
      url : 'jdbc:foo:bar/DB',
      driverClassName : 'foo.bar.Driver',
      info : { user : 'foo', password : 'bar' }
    }*/
  ],

  /**
   *  colInfo = {
   *    tableName : string;
   *    columnName : string;
   *    typeName : string;
   *    columnSize : number;
   *    decimalDigits : number;
   *    isNullable : boolean;
   *  }
   */
  getDefaultValue : function(colInfo) {
    if (colInfo.isNullable) {
      return null;
    } else if (colInfo.typeName == 'CHAR' ||
        colInfo.typeName == 'CHARACTER' ||
        colInfo.typeName == 'VARCHAR' ||
        colInfo.typeName == 'VARCHAR2') {
      var s = '';
      for (var i = 0; i < colInfo.columnSize; i += 1) {
        s += 'X';
      }
      return s;
    } else if (colInfo.typeName == 'NUMBER' ||
        colInfo.typeName == 'DECIMAL') {
      return 0;
    } else if (colInfo.typeName == 'DATE') {
      return sqlName('sysdate');
    } else if (colInfo.typeName == 'TIMESTAMP') {
      return sqlName('sysdate');
    } else {
      throw 'unsupported type:' + colInfo.typeName;
    }
  }
};
```

common.js, config.js, tdg.jar 及び、 JDBCドライバーのライブラリを同一フォルダに配置します。

```
common.js  -- 共通関数ライブラリ
config.js  -- 設定ファイル
tdg.jar    -- ツール本体
hsqldb.jar -- JDBCドライバー(任意)
```

コマンドラインから、tdg を起動します。

```
java -cp tdg.jar;hsqldb.jar dataman.Main
```

ここで扱うデータ投入先のテーブルの DDL は下記のようなものとします。

```sql
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
  primary key (KEY1, KEY2)
);

create table TDG_TEST3 (
  KEY1 char(4) not null,
  FKEY1 char(2),
  FKEY2 char(4),
  VAL1 char(8),
  VAL2 integer,
  primary key (KEY1)
);
```

次に、生成用スクリプトを作成します。

```javascript
var project = {
  dataSource : 'TDGDB',
  patterns : [
    pattern('TDG_TEST1.KEY1')('01', '02')
  ],
  getUserRowData : function(tableName, rowNum, data, caseId) {
    return {};
  }
}
```

tdg にドロップするか、 File - Open から読み込ませると、下記のようなデータが生成されます。

TDG_TEST1

| KEY1 | VAL1   | VAL2   |
| ---- | ------ | ------ |
| 01   | (null) | (null) |
| 02   | (null) | (null) |

パターンで定義していない項目は、 getUserRowData 関数で個別に設定します。
引数の rowNum には行番号(0～)が渡ってきます。

```javascript
var project = {
  dataSource : 'TDGDB',
  patterns : [
    pattern('TDG_TEST1.KEY1')('01', '02')
  ],
  getUserRowData : function(tableName, rowNum, data, caseId) {
    var userData = {};
    userData.VAL1 = 'Test' + rowNum;
    return userData;
  }
}
```

TDG_TEST1

| KEY1 | VAL1  | VAL2   |
| ---- | ----- | ------ |
| 01   | Test0 | (null) |
| 02   | Test1 | (null) |

パターンで展開した値を参照したい場合は、引数の data を使います。

```javascript
var project = {
  dataSource : 'TDGDB',
  patterns : [
    pattern('TDG_TEST1.KEY1')('01', '02')
  ],
  getUserRowData : function(tableName, rowNum, data, caseId) {
    var userData = {};
    userData.VAL1 = 'Test' + rowNum;
    userData.VAL2 = 'Key' + data.KEY1;
    return userData;
  }
}
```

TDG_TEST1

| KEY1 | VAL1  | VAL2  |
| ---- | ----- | ----- |
| 01   | Test0 | Key01 |
| 02   | Test1 | Key02 |

複数テーブル間のキーをリンクさせる場合は、パターンに定義を増やします。
```javascript
var project = {
  dataSource : 'TDGDB',
  patterns : [
    pattern(
        'TDG_TEST1.KEY1',
        'TDG_TEST2.KEY1')('01', '02')
  ],
  getUserRowData : function(tableName, rowNum, data, caseId) {
    var userData = {};
    userData.VAL1 = 'Test' + rowNum;
    userData.VAL2 = 'Key' + data.KEY1;
    return userData;
  }
}
```

TDG_TEST2

| KEY1 | KEY2 | VAL1  | VAL2  |
| ---- | ---- | ----- | ----- |
| 01   | XXXX | Test0 | Key01 |
| 02   | XXXX | Test1 | Key02 |

TDG_TEST2 も同時に生成されましたが、KEY2 が既定値の XXXX のまま、
また VAL1, VAL2 の値に TDG_TEST1 と同じ値が入っています。

KEY2 もパターンに加えて、展開します。

テーブルによって異なる値を設定する場合は getUserRowData 関数の tableName で条件分岐させます。

```javascript
var project = {
  dataSource : 'TDGDB',
  patterns : [
    pattern(
        'TDG_TEST1.KEY1',
        'TDG_TEST2.KEY1')('01', '02'),
    pattern(
        'TDG_TEST2.KEY2')('0001', '0002')
  ],
  getUserRowData : function(tableName, rowNum, data, caseId) {
    var userData = {};
    if (tableName == 'TDG_TEST1') {
      userData.VAL1 = 'Test' + rowNum;
      userData.VAL2 = 'Key' + data.KEY1;
    }
    return userData;
  }
}
```

TDG_TEST2

| KEY1 | KEY2 | VAL1   | VAL2   |
| ---- | ---- | ------ | ------ |
| 01   | 0001 | (null) | (null) |
| 01   | 0002 | (null) | (null) |
| 02   | 0001 | (null) | (null) |
| 02   | 0002 | (null) | (null) |

テーブルが増えた場合でも、基本的には要領は同じです。

```javascript
var project = {
  dataSource : 'TDGDB',
  patterns : [
    pattern(
        'TDG_TEST1.KEY1',
        'TDG_TEST2.KEY1',
        'TDG_TEST3.FKEY1')('01', '02'),
    pattern(
        'TDG_TEST2.KEY2',
        'TDG_TEST3.FKEY2')('0001', '0002')
  ],
  getUserRowData : function(tableName, rowNum, data, caseId) {
    var userData = {};
    if (tableName == 'TDG_TEST1') {
      userData.VAL1 = 'Test' + rowNum;
      userData.VAL2 = 'Key' + data.KEY1;
    } else if (tableName == 'TDG_TEST3') {
      userData.KEY1 = numToStr(rowNum + 1, 4);
    }
    return userData;
  }
}
```

TDG_TEST3

| KEY1 | FKEY1 | FKEY2 | VAL1   | VAL2   |
| ---- | ----- | ----- | ------ | ------ |
| 0001 | 01    | 0001  | (null) | (null) |
| 0002 | 01    | 0002  | (null) | (null) |
| 0003 | 02    | 0001  | (null) | (null) |
| 0004 | 02    | 0002  | (null) | (null) |

テストデータの削除条件は既定では Primary Key ですが、 getDeleteKeys 関数を定義することで任意の列を指定することも可能です。

```javascript
var project = {
  dataSource : 'TDGDB',
  patterns : [
    pattern('TDG_TEST2.KEY1')('01', '02'),
    pattern('TDG_TEST2.KEY2')('0001', '0002')
  ],
  getDeleteKeys : function() {
    return ['KEY1'];
  },
  getUserRowData : function(tableName, rowNum, data, caseId) {
    return {};
  }
}
```

```sql
delete from TDG_TEST2 where KEY1='01';
delete from TDG_TEST2 where KEY1='02';
```

getDeleteKeys を指定しない場合

```sql
delete from TDG_TEST2 where KEY1='01' and KEY2='0001';
delete from TDG_TEST2 where KEY1='01' and KEY2='0002';
delete from TDG_TEST2 where KEY1='02' and KEY2='0001';
delete from TDG_TEST2 where KEY1='02' and KEY2='0002';
```
