parser grammar MyParser;

options {
    tokenVocab = MyLexer;
}

sourceFile: packageSpec WHITESPACE+ importSpec WHITESPACE*;
//sourceFile: commentsSpec* packageSpec WHITESPACE+ importSpec WHITESPACE*;
//commentsSpec: COMMENT_START COMMENT_LINE;

packageSpec: PACKAGE IDENTIFIER SEMI_COLON?;

importSpec: importStatement+;

importStatement: IMPORT (NEW_LINE|WHITESPACE)+ (importSingle|importMultiple);

importSingle: importIdentifier ((NEW_LINE* SEMI_COLON) | WHITESPACE+)?;

importMultiple: PAREN_OPEN
    (NEW_LINE|WHITESPACE)*
    (importIdentifier WHITESPACE* ((SEMI_COLON)|NEW_LINE)? WHITESPACE*)*
    PAREN_CLOSE (NEW_LINE|WHITESPACE)*;

//importIdentifier : (SINGLE_QUOTE|DOUBLE_QUOTE) IDENTIFIER (SINGLE_QUOTE|DOUBLE_QUOTE) ;
importIdentifier : SINGLE_STRING | DOUBLE_STRING ;

