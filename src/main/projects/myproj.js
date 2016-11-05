
var username = 'testuser';

var itemIdList = [];
for (var i = 0; i < 100; i += 1) {
  itemIdList.push('ITEM' + numToStr(i + 1, 4) );
}

var project = {
  dataSource : 'TDGDB',
  /* optional */
  casePatterns : [
   'Case One', 'Case Two', 'Case Three'
  ],
  patterns : [
    pattern(
      'SHOP_MST.SHOP_ID',
      'STOCK_TBL.SHOP_ID'
      )('SHOP0001', 'SHOP0002', 'SHOP0003'),
    pattern(
      'ITEM_MST.ITEM_ID',
      'STOCK_TBL.ITEM_ID'
      )(itemIdList)
  ],
  /* optional (default: delete by primary key) */
  getDeleteKeys : function(tableName) {
    return ['SHOP_ID', 'ITEM_ID'];
  },
  getUserRowData : function(tableName, rowNum, data, caseId) {
    var userData = {};
    if (rowNum % 4 == 0) {
      userData['.comment'] = tableName + rowNum + '-' + caseId;
    }
    if (tableName == 'STOCK_TBL') {
      userData.QUANTITY = Math.floor(random() * 100);
      userData.EXPIRE_DATE = random() < 0.5? '20161231' : null;
      if (rowNum < 4) {
        userData.AMOUNT = Math.floor(random() * 5);
      } else if (rowNum < 8) {
        userData.AMOUNT = Math.floor(random() * 10);
      } else if (rowNum == 10) {
        userData.AMOUNT = 9999;
        userData['.deleted'] = true;
      } else {
        userData.AMOUNT = Math.floor(random() * 10);
      }
    } else if (tableName == 'ITEM_MST') {
      userData.ITEM_NAME = 'Name of ' + data.ITEM_ID;
    }
    userData.CRE_USER = username;
    userData.UPD_USER = username;
    userData.STD_AMOUNT = 0;
    return userData;
  }
};
