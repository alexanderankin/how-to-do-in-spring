lexer grammar MyLexer;

COMMENT_START: '//' -> mode(LINE_COMMENT_MODE);
SEMI_COLON: ';';
NEW_LINE: [\r\n];
WHITESPACE: [\p{WHITE_SPACE}];
SINGLE_STRING: '\'' ~'\''* '\'';
DOUBLE_STRING: '"' ~'"'* '"'; // todo implement ESCAPED_VALUE
TICK_STRING: '`' ~'`'* '`';
SINGLE_QUOTE: '\'';
DOUBLE_QUOTE: '"';

PACKAGE: 'package';

IMPORT: 'import';
PAREN_OPEN: '(';
PAREN_CLOSE: ')';

IDENTIFIER: [a-zA-Z]+;
//COMMENT_LINE: '//'~[\r\n]*[\r\n]?;

mode LINE_COMMENT_MODE;

//REST_OF_LINE: ~[\r\n]*[\r\n] -> mode(DEFAULT_MODE);
NEW_LINE_IN_LINE_COMMENT_MODE: [\r\n] -> mode(DEFAULT_MODE);
REST_OF_LINE: .+?;
