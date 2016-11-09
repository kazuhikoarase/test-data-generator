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
