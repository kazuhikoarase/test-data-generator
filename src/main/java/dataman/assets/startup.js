
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
    var System = Java.type('java.lang.System');
    System.out.println(msg);
    main.getConsole().log(msg);
  }
};

var evalfile = function(src) {
  return main.evalfile(src);
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

  for (var i = 0; i < config.dataSources.length; i += 1) {
    var ds = config.dataSources[i];
    if (ds.initSample) {
      main.initSample(ds.name);
    }
  }
}();

if (typeof Java == 'undefined') {
  Java = {
      type : function(className) {
        var path = className.split(/\./g);
        var cls = Packages;
        for (var i = 0; i < path.length; i += 1) {
          cls = cls[path[i]];
        }
        return cls;
      }
  };
}

var assertEquals = function(expected, actual) {
  if (expected !== actual) {
    console.log('expected ' + expected + ' but ' + actual);
  }
};

var pattern = function() {

  var expand = function() {
    var expand_ = function(args) {
      for (var i = 0; i < args.length; i += 1) {
        var arg = args[i];
        if (typeof arg == 'object' &&
            typeof arg.splice == 'function') {
          expand_(arg);
        } else {
          list.push(arg);
        }
      }
    };
    var list = [];
    expand_(arguments);
    return list;
  };

  var toArray = function(args) {
    var list = [];
    for (var i = 0; i < args.length; i++) {
      list.push(args[i]);
    }
    return list;
  };

  assertEquals(2, expand(1, 2).length);
  assertEquals(3, expand(1, [2, 3]).length);
  assertEquals(2, expand([1, 2]).length);
  assertEquals(3, expand([1, [2, 3] ]).length);
  assertEquals(4, expand([1, [2, [3, 4] ] ]).length);
  assertEquals(6, expand([[0, 1], [2, [3, 4] ], 5]).length);
  !function() {
    assertEquals(3, expand(toArray(arguments) ).length);
  }([1, 2, 3]);

  return function() {
    var cols = arguments;
    return function() {
      var vals = arguments;
      return [cols, expand(toArray(vals) )];
    }
  };
}();

var sqlName = function(name) {
  var SqlName = Java.type('dataman.model.SqlName');
  return new SqlName(name);
};

// intf
var _intf = function() {

  var intf = {};

  intf.getDefaultValue = function(columnMeta) {
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

  intf.getUserRowData = function(tableName, rowNum, dataMap, caseId) {
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
    var HashMap = Java.type('java.util.HashMap');
    var map = new HashMap();
    if (data) {
      for (var k in data) {
        map.put('' + k, data[k]);
      }
    }
    return map;
  };

  intf.getDeleteKeys = function(tableName) {
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
        var HashSet = Java.type('java.util.HashSet');
        var keySet = new HashSet();
        for (var i = 0; i < deleteKeys.length; i += 1) {
          keySet.add(deleteKeys[i]);
        }
        return keySet;
      } else {
        var HashMap = Java.type('java.util.HashMap');
        var map = new HashMap();
        for (var k in deleteKeys) {
          map.put('' + k, deleteKeys[k]);
        }
        return map;
      }
    } else {
      throw 'illegal deleteKeys:' + deleteKeys;
    }
  };

  return intf;
}();

var _invokeFunction = function(name) {
  var names = name.split(/\./g);
  var f = global[names[0]];
  for (var i = 1; i < names.length; i += 1) {
    f = f[names[i]];
  }
  var args = [];
  for (var i = 1; i < arguments.length; i += 1) {
    args.push(arguments[i]);
  }
  return f.apply(null, args)
};
