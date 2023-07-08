lexer grammar MyLexer;

//COMMENT_START: '//';
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
//COMMENT_LINE: ~[\r\n]+;
