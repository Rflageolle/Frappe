class ReplParser {

  Lexer lex;

  public ReplParser (Lexer lex) {
    this.lex = lex;
  }


  // GRAMMAR FOR REPL
  // each line of the repl is a <call>
  // <call> -> LPAREN PREDEFINEDFUNCNAME <params> RPAREN | LPAREN NAME <params> RPAREN
    private void parseCall(){

      Token lparen = lex.getNextToken();
      isLPAREN(lparen);

      Token funcName = lex.getNextToken();

      if (funcName.isKind("NUMBER_FUNCTION")) {
        switch ( funcName.getDetails() ) {
          // idea is we know how many params so parse get them and call function
          case "plus":
            Token param1 = lex.getNextToken();
            Token param2 = lex.getNextToken();

            Double Value.plus(param1.getDetails(), param2.getDetails());
          case "minus":
            Token param1 = lex.getNextToken();
            Token param2 = lex.getNextToken();

            Double Value.minus(param1.getDetails(), param2.getDetails());
          case "times":
            Token param1 = lex.getNextToken();
            Token param2 = lex.getNextToken();

            Double Value.times(param1.getDetails(), param2.getDetails());
          case "div":
            Token param1 = lex.getNextToken();
            Token param2 = lex.getNextToken();

            Double Value.div(param1.getDetails(), param2.getDetails());
          case "lt":
            Token param1 = lex.getNextToken();
            Token param2 = lex.getNextToken();

            Double Value.lt(param1.getDetails(), param2.getDetails());
          case "le":
            Token param1 = lex.getNextToken();
            Token param2 = lex.getNextToken();

            Double Value.le(param1.getDetails(), param2.getDetails());
          case "eq":
            Token param1 = lex.getNextToken();
            Token param2 = lex.getNextToken();

            Double Value.eq(param1.getDetails(), param2.getDetails());
          case "ne":
            Token param1 = lex.getNextToken();
            Token param2 = lex.getNextToken();

            Double Value.ne(param1.getDetails(), param2.getDetails());
        }
      } else if (funcName.isKind("LIST_FUNCTION")) {
        switch ( funcName.getDetails() ) {
          case "ins":
            Token param1 = lex.getNextToken();
            ArrayList[Token] param2 = parseQuote(); // parseQuote will return an ArrayList of Tokens "lists values"

            ArrayList[Token] fin = VList.ins( param1.getDetails, param2);
          case "first":
            ArrayList[Token] param = parseQuote(); // parseQuote will need to handle null single and multiple elements

            ArrayList[Token] fin = VList.first( param );

          case "rest":
            ArrayList[Token] param = parseQuote();

            ArrayList[Token] fin = VList.rest();
        }
      } else if ( funcName.isKind("BOOL_FUNCTION")) {
        switch ( funcName.getDetails() ) {
          case "null":
            ArrayList[Token] param = parseQuote();

            VBool.null(param);

          case "num":
            Token param = lex.getNextToken();
            if (param.isKind("NUMBER"){
              return 1;
            } else { return 0; }

          case "list":
            // need to test if is a list
        }
      } else {

      }

    }

  // <params> -> <param> | <param> <params> // we will know if it is a param because (parens % 2) != 0
  private Node parseParameters() {
    Node first = parseParameter();

    Token check = lex.getNextToken();
    if (check.isKind("RPAREN")) {
      return new Node("repl_parameters", first, null, null);
    } else {
      lex.putBackToken(check);
      Node second = parseParameters();
      return new Node("repl_parameters", first, null, null);
    }
  }

  // <param> -> value | <call>
  public Node parseParameter() {
    Token check = lex.getNextToken();

    if (check.isKind("NUMBER")) {
      return new Node ("repl_parameter", check.getDetails(), null, null);
    } else {
      errorCheck( check, "LPAREN");
      lex.putBackToken(check);
      Node first = parseCall();
    }

  }

  public parseRepl() {
    parseCall();
  }



  // supplementary methods
  // check whether token is correct kind
  private void errorCheck( Token token, String kind ) {
    if( ! token.isKind( kind ) ) {
      System.out.println("Error:  expected " + token +
         " to be of kind " + kind );
      System.exit(1);
    }
  }

  // check whether token is correct kind and details
  private void errorCheck( Token token, String kind, String details ) {
    if( ! token.isKind( kind ) ||
        ! token.getDetails().equals( details ) ) {
          System.out.println("Error:  expected " + token +
          " to be kind= " + kind +
          " and details= " + details );
          System.exit(1);
    }
  }

  private void isLPAREN( Token token ){
    if (token.isKind( "LPAREN")) {
      parens = parens + 1;
      return true;
    } else {
      ! token.getDetails().equals( details ) ) {
        System.out.println("Error:  expected " + token +
        " to be kind= " + kind +
        " and details= " + details );
        System.exit(1);
    }
  }

  private void isRPAREN( Token token ){
    if (token.isKind( "LPAREN")) {
      parens = parens - 1;
      return true;
    } else {
      ! token.getDetails().equals( details ) ) {
        System.out.println("Error:  expected " + token +
        " to be kind= " + kind +
        " and details= " + details );
        System.exit(1);
    }
  }

}
