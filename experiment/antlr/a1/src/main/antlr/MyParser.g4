parser grammar MyParser;

options {
    tokenVocab = MyLexer;
}

sourceFile: packageSpec;

packageSpec: PACKAGE IDENTIFIER;

importSpec: importStatement+;

importStatement: IMPORT (SINGLE_STRING|DOUBLE_STRING|PAREN_OPEN(DOUBLE_STRING*)PAREN_CLOSE);
