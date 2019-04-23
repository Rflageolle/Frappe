
import java.util.*;
import java.io.*;


public class NewParser {

  private Lexer lex;
  private int parens;

  public NewParser( Lexer lexer ) {
    lex = lexer;
    parens = 0;
  }

  //<defs> -> <def> | <def> <defs>
  public Node parseDefs(){
    System.out.println("-----> parsing <defs>");
    Node first = parseDef();
    Token check = lex.getNextToken();
    if (!check.isKind("EOF")){
      lex.putBackToken(check);
      Node second = parseDefs();
      return new Node("defs", first, second, null);
    }
    else {
      return new Node("defs", first, null, null);
    }

    }

    //<def> -> LPAREN DEFINE LPAREN NAME <params> RPAREN <args> RPAREN
    public Node parseDef(){
      System.out.println("-----> parsing <def>");
      Token lparen = lex.getNextToken();
      errorCheck ( lparen, "LPAREN", "(" );
      Token define = lex.getNextToken();
      errorCheck ( define, "RESERVED", "define" );
      lparen = lex.getNextToken();
      errorCheck ( lparen, "LPAREN", "(" );
      Token name = lex.getNextToken();
      errorCheck ( name, "NAME" );

      Node first = parseParams();
      Token rparen = lex.getNextToken();
      errorCheck( rparen, "RPAREN", ")");

      Node second = parseExpr();
      rparen = lex.getNextToken();
      errorCheck( rparen, "RPAREN", ")");

      return new Node("def", name.getDetails(), first, second, null);
    }

    //<params> -> <param> | <param> <params>
    public Node parseParams() {
      System.out.println("-----> parsing <params>");
      Node first = parseParam();

      Token check = lex.getNextToken();
      if ( !check.isKind("RPAREN") ) {
        lex.putBackToken( check );
        Node second = parseParams();

        return new Node( "params", first, second, null );

      } else {
        lex.putBackToken(check);

        return new Node( "params", first, null, null );
      }
    }

    public Node parseParam() {
      System.out.println("-----> parsing <param>");
      Token name = lex.getNextToken();

      return new Node("param", name.getDetails(), null, null, null);
    }

    //<args> -> <arg> | <arg> <args>
    public Node parseArgs(){
      System.out.println("-----> parsing <args>");
      Node first = parseArg();

      Token check = lex.getNextToken();
      if ( !check.isKind( "RPAREN" ) ) {
        lex.putBackToken(check);
        Node second = parseArgs();

        return new Node("args", first, second, null);
      } else {
        lex.putBackToken(check);

        return new Node("args", first, null, null);
      }
    }

    //<arg> -> <expr> | <list> | NUMBER | NAME
    public Node parseArg() {
      System.out.println("-----> parsing <arg>");
      Token check = lex.getNextToken();
      if ( check.isKind("NUMBER") || check.isKind("NAME")) {
        return new Node("arg", check.getDetails(), null, null, null);
      } else {
          Token name = lex.getNextToken();
          if ( name.matches("REPL_FUNCTION", "quote")) {
            Node first = parseList();
            Token rparen = lex.getNextToken();
            errorCheck(rparen, "RPAREN");
            return new Node("arg", "list", first, null, null);
          } else {
            lex.putBackToken(name);
            lex.putBackToken(check);
            Node first = parseExpr();
            
            return new Node("arg", name.getDetails(), first, null, null);
          }
      }
    }

    //<expr> -> LPAREN NAME <args> RPAREN
    public Node parseExpr(){
      System.out.println("-----> parsing <expr>");
      Token lparen = lex.getNextToken();
      errorCheck( lparen, "LPAREN", "(");
      Token name = lex.getNextToken();
      if ( name.isKind("RESERVED") || name.isKind("REPL_FUNCTION")) {
        if (name.matches("RESERVED", "if")) {
          Node first = parseArg();
          Node second = parseArg();
          Node third = parseArg();
          Token rparen = lex.getNextToken();
          errorCheck( rparen, "RPAREN");

          return new Node("expr", name.getDetails(), first, second, third);
        } else if ( name.matches("REPL_FUNCTION", "quote") ) {
          Node first = parseList();
          Token rparen = lex.getNextToken();
          errorCheck(rparen, "RPAREN", ")");

          return new Node("expr", name.getDetails(), first, null, null);

        } else {
          return new Node("noooooooo FUCK", null, null, null);
        }

      } else if ( name.isKind("NUMBER_FUNCTION") || name.isKind("LIST_FUNCTION") || name.isKind("BOOL_FUNCTION") ) {
        Node first = parseArgs();
        Token rparen = lex.getNextToken();
        errorCheck( rparen, "RPAREN");

        return new Node( "expr", name.getDetails(), first, null, null);

      } else {
        Node first = parseArgs();
        Token rparen = lex.getNextToken();
        errorCheck( rparen, "RPAREN");
        return new Node( "expr", name.getDetails(), first, null, null);
      }
    }

    //<list> -> LPAREN RPAREN | LPAREN <items> RPAREN
    public Node parseList() {
      System.out.println("-----> parsing <list>");
        Token lparen = lex.getNextToken();
        errorCheck( lparen, "LPAREN");

        Token check = lex.getNextToken();
        if ( !check.isKind( "RPAREN" ) ) {
          lex.putBackToken( check );
          Node first = parseItems();
          Token rparen = lex.getNextToken();
          errorCheck( rparen, "RPAREN" );

          return new Node( "list", first, null, null);
        } else {
          errorCheck( check, "RPAREN" );

          return new Node( "list", "null", null, null, null);
        }

    }

    //<items> -> <item> | <item> <items>
    public Node parseItems() {
      System.out.println("-----> parsing <items>");
      Node first = parseItem();
      Token check = lex.getNextToken();

      if ( !check.isKind("RPAREN") ) {
        lex.putBackToken( check );
        Node second = parseItems();

        return new Node( "items", first, second, null );
      } else {
        lex.putBackToken( check );

        return new Node( "items", first, null, null );
      }
    }

    //<item> -> NAME | NUMBER | <list>
    public Node parseItem() {
      System.out.println("-----> parsing <item>");
      Token check = lex.getNextToken();
      if ( !check.isKind("LPAREN") ) {
        return new Node( "item", check.getDetails(), null, null, null );
      } else {
        lex.putBackToken( check );
        Node first = parseList();

        return new Node( "item", "list", first, null, null);
      }
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
}
