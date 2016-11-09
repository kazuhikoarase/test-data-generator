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
