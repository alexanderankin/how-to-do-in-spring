parser grammar MyParser;

options {
    tokenVocab = MyLexer;
}

sourceFile: PACKAGE SINGLE_QUOTE IDENTIFIER SINGLE_QUOTE;