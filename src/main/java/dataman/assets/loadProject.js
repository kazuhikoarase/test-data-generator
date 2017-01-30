
!function() {
  main.setDataSource(project.dataSource);
  main.setSchema(typeof project.schema == 'string'? project.schema : null);
  if (project.casePatterns) {
    for (var c = 0; c < project.casePatterns.length; c += 1) {
      main.addCasePattern(project.casePatterns[c]);
    }
  }
  for (var p = 0; p < project.patterns.length; p += 1) {
    var pattern = project.patterns[p];
    var columns = pattern[0];
    var values = pattern[1];
    for (var c = 0; c < columns.length; c += 1) {
      for (var v = 0; v < values.length; v += 1) {
        main.addPattern(p, columns[c], values[v]);
      }
    }
  }
}();
