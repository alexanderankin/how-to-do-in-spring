parser grammar MyParser;

options {
    tokenVocab = MyLexer;
}

sourceFile: commentsSpec packageSpec WHITESPACE+ commentsSpec importSpec WHITESPACE* commentsSpec;

// any sort of comment section
commentsSpec: (commentLine)*;

// a line comment
commentLine : COMMENT_START commentLineContent NEW_LINE_IN_LINE_COMMENT_MODE? ;

// the contents of the line comment, for convenience
commentLineContent : REST_OF_LINE? ;

packageSpec: PACKAGE WHITESPACE* IDENTIFIER WHITESPACE* SEMI_COLON? WHITESPACE*;

importSpec: importStatement+;

importStatement: IMPORT (NEW_LINE|WHITESPACE)+ (importSingle|importMultiple) WHITESPACE*;

importSingle: importIdentifier ((NEW_LINE* SEMI_COLON) | WHITESPACE+)?;

importMultiple: PAREN_OPEN
    (NEW_LINE|WHITESPACE)*
    (importIdentifier WHITESPACE* ((SEMI_COLON)|NEW_LINE)? WHITESPACE*)*
    PAREN_CLOSE (NEW_LINE|WHITESPACE)*;

//importIdentifier : (SINGLE_QUOTE|DOUBLE_QUOTE) IDENTIFIER (SINGLE_QUOTE|DOUBLE_QUOTE) ;
importIdentifier : (IDENTIFIER WHITESPACE*)? (SINGLE_STRING | DOUBLE_STRING);

