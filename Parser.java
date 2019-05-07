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

  public Node parseProgram() {
    Node first = parseClasses();
    return new Node("program", first, null, null);
  }

  public Node parseClasses() {
    Node first = parseClass();
    Node second = null;

    Token check = lex.getNextToken();
    if ( check.isKind("class") ) {
      lex.putBackToken(check);
      second = paresClasses();
    }

    return new Node("classes", first, second);
  }

  // <class> -> CLASS CLASSNAME LBRACE <members> RBRACE
  public Node parseClass() {
    Token class = lex.getNextToken();
    errorCheck( class, "CLASS");

    Token class_name = lex.getNextToken();
    errorCheck( class_name, "CLASSNAME");

    Token lbrace = lex.getNextToken();
    errorCheck( lbrace, "LBRACE");

    Node first = parseMembers();

    Token rbrace = lex.getNextToken();
    errorCheck( rbrace, "RBRACE" );

    return new Node( "class", class_name.getDetails(), first, null, null);

  }

  //<members> -> <member> | <member> <members>
  public Node parseMembers() {
    Node first = parseMember();
    Node second = null;

    Token check = lex.getNextToken();
    if ( check.isKind("STATIC") || check.isKind("CLASSNAME") || check.isKind("NAME") ) {
      lex.putBackToken(check);
      second = parseMembers();
    }

    return new Node("members", first, second, null);
  }

  // <member> -> <staticField> |  <staticMethod> | <constructor> | <instanceField> | <instanceMethod>
  public Node parseMember() {
    Node first = null;
    Token check = lex.getNextToken();

    if ( check.isKind( "STATIC" ) ) {
      Token lookahead = lex.getNextToken();
      lex.putBackToken( check );
      if ( lookahead.isKind( "LPAREN" ) ) {
        lex.putBackToken( lookahead );
        first = parseStaticMethod();
        return new Node( "memeber", first, null, null );
      } else {
        lex.putBackToken( lookahead );
        first = parseStaticField();
        return new Node ( "member", first, null, null );
      }
    } else if ( check.isKind("CLASSNAME") ) {
      lex.putBackToken( check );
      first = parseConstructor();
      return new Node( "member", first, null, null );
    } else {
      Token lookahead = lex.getNextToken();
      lex.putBackToken( check );
      if ( lookahead.isKind( "LPAREN" )  {
        lex.putBackToken( lookahead );
        first = parseInstanceMethod();
        return new Node( "member", first, null, null );
      } else {
        lex.putBackToken( lookahead );
        first = parseInstanceField();
        return new Node( "member", first, null, null );
      }
    }
  }

  // <staticField> -> STATIC NAME | STATIC NAME EQUALS <expression>
  public Node parseStaticField() {
    Node first = null;

    Token stat = lex.getNextToken();
    errorCheck( stat, "STATIC" );

    Token name = lex.getNextToken();
    errorCheck( name, "NAME" );

    Token check = lex.getNextToken();

    if ( check.isKind( "EQUALS" ) ) {
      first = parseExpression();
    } else {
      lex.putBackToken( check );
    }

    return new Node( "static-field", name.getDetails(), first, null, null);

  }

  // <staticMethod> -> STATIC NAME <restOfMethod>
  public Node parseStaticMethod() {
    Token stat = lex.getNextToken();
    errorCheck( stat, "STATIC" );

    Token name = lex.getNextToken();
    errorCheck( stat, "NAME" );

    Node first = parseRestOfMethod();

    return new Node ( "static-method", name.getDetails(), first, null, null );
  }

  // <instanceField> -> NAME
  public Node parseInstanceField() {
    Token name = getNextToken(name);
    errorCheck( name, "NAME" );

    return new Node( "instance-field", name.getDetails(), null, null, null);
  }

  // <constructor> -> CLASSNAME <restOfMethod>
  public Node parseConstructor() {
    Token name = getNextToken();
    errorCheck( name, "CLASSNAME");

    Node first = parseRestOfMethod();

    return new Node( "constructor", name.getDetails(), first, null, null );
  }

  // <instanceMethod> -> NAME <restOfMethod>
  public Node parseInstanceMethod() {
    Token name = lex.getNextToken();
    errorCheck( name, "NAME" );

    Node first = parseRestOfMethod();

    return new Node( "instance-method", name.getDetails(), null, null );
  }

  //<restOfmethod> -> LPAREN RPAREN <methodBody> | LPAREN <params> RPAREN <methodBody>
  public Node parseRestOfMethod() {
    Node first = null;
    Node second = null;

    Token lparen = lex.getNextToken();
    errorCheck( lparen, "LPAREN" );

    Token rparen = lex.getNextToken();

    if ( rparen.isKind( "RPAREN")) {
      first = parseMethodBody();

      return new Node( "rest-of-method", first, second, null );
    } else {
      lex.putBackToken( rparen );
      first = parseParams();

      rparen = lex.getNextToken();
      errorCheck( rparen, "RPAREN" );

      second = parseMethodBody();

      return new Node( "rest-of-method", first, second, null );
    }

  }

  // <params> -> NAME | NAME COMMA <params>
  public Node parseParams() {
    Node first = null;
    Token name = lex.getNextToken();
    errorCheck( name, "NAME" );

    Token comma = lex.getNextToken();

    if ( comma.isKind( "COMMA" ) ) {
      first = parseParams();
    }

    return new Node( "params", name.getDetails(), first, null, null );
  }

  // <methodBody> -> LBRACE RBRACE | LBRACE <statements> RBRACE
  public Node parseMethodBody() {
    Node first = null;

    Token lbrace = lex.getNextToken();
    errorCheck( lbrace, "LBRACE" );

    Token rbrace = lex.getNextToken();

    if ( !rbrace.isKind( "RBRACE" ) ) {
      first = parseStatements();
    }

    return new Node( "method-body", first, null, null );
  }

  // <statements> -> <statement> | <statement> <statements>
  public Node parseStatements() {
    Node first = parseStatement();
    Node second = null;

    Token check = lex.getNextToken();

    if ( check.isKind( "NAME") | check.isKind( "CLASSNAME" ) | check.isKind( "WHILE" ) | check.isKind( "IF" ) | check.isKind( "RETURN" ) ) {
      second = parseStatements();
    }

    return new Node( "statements", first, second, null );
  }

  // <statement> -> NAME EQUALS <rhs> | // assignment statement
               //
               // <ref>       | // a method call that
               //                // doesn't use returned value, if any
               //
               // <whileStatement> |
               //
               // <ifStatement> |
               //
               // RETURN <expression>
  public Node parseStatement() {
    Node first = null;
    Token name = lex.getNextToken();

    if ( name.isKind( "CLASSNAME" ) ) {
      first = parseRef();
    } else if ( name.isKind( "NAME" ) ) {
      Token
    } else if ( name.isKind( "WHILE" ) ) {

    } else if ( name.isKind( "IF" ) ) {

    } else {

    }
  }

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
