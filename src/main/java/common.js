
var assertEquals = function(expected, actual) {
  if (expected !== actual) {
    console.log('expected ' + expected + ' but ' + actual);
  }
};

var numToStr = function(n, digits) {
  digits = digits || 0;
  var s = '' + ~~(+n);
  while (s.length < digits) {
    s = '0' + s;
  }
  return s;
};

var strToDate = function(sdate) {
  sdate = '' + sdate;
  if (sdate.length != 8) {
    return new Date();
  }
  return new Date(+sdate.substring(0, 4),
      +sdate.substring(4, 6) - 1,
      +sdate.substring(6, 8) );
};

var dateToStr = function(date, delm) {
  delm = delm || '';
  return numToStr(date.getFullYear(), 4) +
    delm + numToStr(date.getMonth() + 1, 2) +
    delm + numToStr(date.getDate(), 2);
};

var rollDate = function(sdate, offset) {
  var date = strToDate(sdate);
  date.setDate(date.getDate() + offset);
  return dateToStr(date);
};

var rollMonth = function(sdate, offset) {
  var date = strToDate(sdate);
  date.setMonth(date.getMonth() + offset);
  date.setDate(1);
  return dateToStr(date);
};

var getFirstDateOfMonth = function(sdate) {
  var date = strToDate(sdate);
  date.setDate(1);
  return dateToStr(date);
};

var getLastDateOfMonth = function(sdate) {
  var date = strToDate(sdate);
  date.setMonth(date.getMonth() + 1);
  date.setDate(0);
  return dateToStr(date);
};

var random = function() {
  var rand = java.util.Random(0);
  return function() {
    return +rand.nextDouble();
  };
}();

var dumpData = function(data) {
  var s = '';
  s += '{';
  var i = 0;
  for (var k in data) {
    if (i > 0) {
      s += ',';
    }
    s += k + '=' + data[k];
    i += 1;
  }
  s += '}';
  console.log(s);
}

// test my self.

assertEquals('01', numToStr(1, 2) );
assertEquals('0', numToStr(0, 1) );
assertEquals('000', numToStr(0, 3) );
assertEquals('20160229', dateToStr(strToDate('20160229') ) );
assertEquals('20160301', rollDate('20160229', 1) );
assertEquals('20160229', rollDate('20160301', -1) );
assertEquals('20151231', rollDate('20160101', -1) );
assertEquals('20160201', rollMonth('20160105', 1) );
assertEquals('20151201', rollMonth('20160105', -1) );
assertEquals('20160101', getFirstDateOfMonth('20160105') );
assertEquals('20160131', getLastDateOfMonth('20160105') );
assertEquals(7, ~~(random() * 10) );
assertEquals(2, ~~(random() * 10) );
