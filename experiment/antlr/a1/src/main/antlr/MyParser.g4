parser grammar MyParser;

options {
    tokenVocab = MyLexer;
}

sourceFile: commentsSpec? packageSpec (NEW_LINE|WHITESPACE)+ commentsSpec? importSpec (NEW_LINE|WHITESPACE)* commentsSpec?;

// any sort of comment section
commentsSpec: (comment (NEW_LINE|WHITESPACE)*)+;

comment : (commentLine|commentBlock) ;

// a line comment
commentLine : COMMENT_LINE;

commentBlock: COMMENT_BLOCK;

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

