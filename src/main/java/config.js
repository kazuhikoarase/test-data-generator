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
    /*
    {
      name : 'mydb',
      url : 'jdbc:buzzzzzzzzzzzzzzzzzzz',
      driverClassName : 'buzzzzzzzzzzzzzzzzzzz',
      info : { user : 'my', password : 'name' }
    },*/
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
