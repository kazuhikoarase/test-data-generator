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
