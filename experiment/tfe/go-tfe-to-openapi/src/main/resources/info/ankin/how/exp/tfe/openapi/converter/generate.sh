# curl --output ~/Downloads/antlr-4.13.0-complete.jar 'https://www.antlr.org/download/antlr-4.13.0-complete.jar'
alias antlr='java -jar ~/Downloads/antlr-4.13.0-complete.jar'
antlr -Xexact-output-dir -o ../../../../../../../../java/info/ankin/how/exp/tfe/openapi/converter/generated/ -package info.ankin.how.exp.tfe.openapi.converter.generated GoLexer.g4
antlr -Xexact-output-dir -o ../../../../../../../../java/info/ankin/how/exp/tfe/openapi/converter/generated/ -package info.ankin.how.exp.tfe.openapi.converter.generated GoParser.g4 -visitor
