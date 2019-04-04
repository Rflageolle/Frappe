/*
This class provides a recursive descent parser
for Corgi (the new version),
creating a parse tree which can be interpreted
to simulate execution of a Corgi program
*/

import java.util.*;
import java.io.*;

public class Parser {

  private Lexer lex;

  public Parser( Lexer lexer ) {
    lex = lexer;
  }

  //<defs> -> <def> | <def> <defs>
  public Node parseDefs(){
    System.out.println("-----> parsing <defs>");
    Node first = parseDef();
    Token check = lex.getNextToken();
    Node second = null;
    if (!check.matches("RESERVED", "EOF")){
      lex.putBackToken(check);
      second = parseDefs();
    }
      return new Node("defs", first, second, null);
    }

    //<def> -> LPAREN DEFINE LPAREN NAME RPAREN <expr> RPAREN | LPAREN DEFINE LPAREN NAME <params> RPAREN <expr> RPAREN
    public Node parseDef(){
      System.out.println("-----> parsing <def>");
      Token Lparen = lex.getNextToken();
      errorCheck ( Lparen, "LPAREN", "(" );
      Token Define = lex.getNextToken();
      errorCheck ( Define, "RESERVED", "define" );
      Lparen = lex.getNextToken();
      errorCheck ( Lparen, "LPAREN", "(" );
      Token Name = lex.getNextToken();
      errorCheck ( Name, "NAME" );
      Token Rparen = lex.getNextToken();
      if ( Rparen.matches( "RPAREN", ")" )){
        Node first = parseExpression();
        return new Node("def", Name.getKind(), first, null, null);
      }
      else {
        lex.putBackToken(Rparen);
        Node first = parseParams();
        Rparen = lex.getNextToken();
        errorCheck ( Rparen, "RPAREN", ")" );
        Node second = parseExpression();
        Rparen = lex.getNextToken();
        errorCheck ( Rparen, "RPAREN", ")" );
        return new Node("def", Name.getKind(), first, second, null);
      }

    }
    // THIS NEEDS WORK
    // <params> -> NAME | NAME <params>
    public Node parseParams(){
      System.out.println("-----> parsing <params>:");
      Token name = lex.getNextToken();
      if(name.isKind("NAME")) {
        Token check = lex.getNextToken();
        if (check.isKind("NAME")){
          lex.putBackToken(check);
          Node first = parseParams();
          return new Node("params", check.getDetails(), first, null, null);
        } else {
          lex.putBackToken(check);
          return new Node("params", name.getDetails(), null, null, null);
        }
      } else {
        lex.putBackToken(name);
        return null;
      }
    }

    // <expr> -> NUMBER | NAME |<list>
    public Node parseExpression() {
      System.out.println("-----> parsing <expr>:");
      Token check = lex.getNextToken();
      if ( check.isKind("NUMBER") || check.isKind( "NAME")) {
        return new Node("expression", check.getKind(), null, null, null);
      } else {
        errorCheck(check, "LPAREN", "(");
        Node first = parseList();
        return new Node("expression", first, null, null);
      }
    }

    // <list> -> LPAREN RPAREN | LPAREN <items> RPAREN
    public Node parseList() {
      System.out.println("-----> parsing <list>:");
      Token lparen = lex.getNextToken();
      errorCheck ( lparen, "LPAREN", "(" );

      Token check = lex.getNextToken();

      if ( check.matches( "RPAREN", " ) " ) ) {
        return new Node("list", null, null, null);
      }
      else {
        lex.putBackToken(check);
        Node first = parseItems();
        return new Node("list", first, null, null);
      }
    }

    // <items> -> <expr> | <expr> <items>
    public Node parseItems() {
      System.out.println("-----> parsing <items>:");
      Node first = parseExpression();
      Node second = null;
      Token check = lex.getNextToken();
      if ( check.isKind("NUMBER") || check.isKind("LPAREN")) {
        lex.putBackToken( check );
        second = parseExpression();
      } else {
        lex.putBackToken( check );
      }
      return new Node("items", first, second, null);
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
