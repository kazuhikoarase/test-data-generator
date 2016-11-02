
var global = this;

var console = {
  log : function() {
    var msg = '';
    for (var i = 0; i < arguments.length; i += 1) {
      if (i > 0) {
        msg += ' ';
      }
      msg += arguments[i];
    }
    java.lang.System.out.println(msg);
    main.getConsole().log(msg);
  }
};

!function() {

  for (var i = 0; i < config.dataSources.length; i += 1) {
    var ds = config.dataSources[i];
    var info = new java.util.Properties();
    for (var k in ds.info) {
      info.setProperty(k, ds.info[k]);
    }
    main.addDataSource(ds.name, ds.driverClassName, ds.url, info);
  }
  if (config.sample) {
    main.initSample(config.sample.dataSource);
  }
}();

var pattern = function() {
  var cols = arguments;
  return function() {
    var vals = arguments;
    return [cols, vals];
  }
};

var sqlName = function(name) {
  return Packages.dataman.model.SqlName(name);
};

// intf

var getDefaultValue = function(columnMeta) {
  var tableName = '' + columnMeta.getTableName();
  var columnName = '' + columnMeta.getColumnName();
  var typeName = '' + columnMeta.getTypeName();
  var columnSize = +columnMeta.getColumnSize();
  var decimalDigits = +columnMeta.getDecimalDigits();
  var isNullable = !!columnMeta.isNullable();
  var colInfo = {
    tableName : tableName,
    columnName : columnName,
    typeName : typeName,
    columnSize : columnSize,
    decimalDigits : decimalDigits,
    isNullable : isNullable
  };
  return config.getDefaultValue.call(null, colInfo);
};

var getUserRowData = function(tableName, rowNum, dataMap, caseId) {
  var entries = dataMap.entrySet().toArray();
  var data = {};
  for (var i = 0; i < entries.length; i += 1) {
    if (entries[i].getValue().getType() != 1) {
      // not generated.
      continue;
    }
    var value = entries[i].getValue().getValue();
    if (value == null) {
      
    } else if (value instanceof java.lang.String) {
      value = '' + value;
    } else if (value instanceof java.lang.Number) {
      value = +value;
    }
    data['' + entries[i].getKey()] = value;
  }
  data = project.getUserRowData(tableName, rowNum, data,
      caseId != null? '' + caseId : null);
  var map = java.util.HashMap();
  if (data) {
    for (var k in data) {
      map.put('' + k, data[k]);
    }
  }
  return map;
};

var getDeleteKeys = function(tableName) {
  if (!project.getDeleteKeys) {
    return null;
  }
  var deleteKeys = project.getDeleteKeys(tableName);
  if (!deleteKeys) {
    return null;
  } else if (typeof deleteKeys == 'string') {
    return deleteKeys;
  } else if (typeof deleteKeys == 'object') {
    if (typeof deleteKeys.splice == 'function') {
      var keySet = java.util.HashSet();
      for (var i = 0; i < deleteKeys.length; i += 1) {
        keySet.add(deleteKeys[i]);
      }
      return keySet;
    } else {
      var map = java.util.HashMap();
      for (var k in deleteKeys) {
        map.put('' + k, deleteKeys[k]);
      }
      return map;
    }
  } else {
    throw 'illegal deleteKeys:' + deleteKeys;
  }
};

var invokeFunction = function(name) {
  var names = name.split(/\./g);
  var f = global[names[0]];
  for (var i = 1; i < names.length; i += 1) {
    f = f[names[i]];
  }
  var args = [];
  for (var i = 1; i < arguments.length; i += 1) {
    args.push(arguments[i]);
  }
  f.apply(null, args)
};
