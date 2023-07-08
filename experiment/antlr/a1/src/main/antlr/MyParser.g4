parser grammar MyParser;

options {
    tokenVocab = MyLexer;
}

sourceFile: packageSpec WHITESPACE+ importSpec WHITESPACE*;

packageSpec: PACKAGE IDENTIFIER SEMI_COLON?;

importSpec: importStatement+;

importStatement: IMPORT WHITESPACE+ (importSingle|importMultiple);

importSingle: importIdentifier SEMI_COLON? WHITESPACE*;

importMultiple: PAREN_OPEN WHITESPACE* (importIdentifier SEMI_COLON? WHITESPACE*)* PAREN_CLOSE WHITESPACE*;

//importIdentifier : (SINGLE_QUOTE|DOUBLE_QUOTE) IDENTIFIER (SINGLE_QUOTE|DOUBLE_QUOTE) ;
importIdentifier : SINGLE_STRING | DOUBLE_STRING ;

