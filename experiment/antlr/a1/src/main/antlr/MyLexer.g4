lexer grammar MyLexer;

WHITESPACE: [\p{WHITE_SPACE}];
SEMI_COLON: ';';
SINGLE_STRING: '\'' ~'\''* '\'';
DOUBLE_STRING: '"' ~'"'* '"'; // todo implement ESCAPED_VALUE
TICK_STRING: '`' ~'`'* '`';

PACKAGE: 'package';

IMPORT: 'import';
PAREN_OPEN: '(';
PAREN_CLOSE: ')';

IDENTIFIER: [a-zA-Z]+;
