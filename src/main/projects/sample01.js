var project = {
  dataSource : 'TDGDB',
  patterns : [
    pattern('TDG_TEST1.KEY1')('01', '02')
  ],
  getUserRowData : function(tableName, rowNum, data, caseId) {
    return {};
  }
}
