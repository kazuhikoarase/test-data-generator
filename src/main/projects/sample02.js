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
