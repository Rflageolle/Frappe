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
	   if (!check.isKind("EOF")){
		lex.putBackToken(check);
		second = parseDefs();
	   }
	   return new Node("defs", first, second, null);
   }
   
   //<def> -> LPAREN DEFINE LPAREN NAME RPAREN <expr> RPAREN | LPAREN DEFINE LPAREN NAME <params> RPAREN <expr> RPAREN
   public Node parseDef(){
   	System.out.println("-----> parsing <def>");
	Token Lparen = lex.getNextToken();
	errorCheck ( Lparen, "symbol", "(" );
	Token Define = lex.getNextToken():
	errorCheck ( Define, "DEFINE" );
	Lparen = lex.getNextToken();
	errorCheck ( Lparen, "symbol", "(" );
	Token Name = lex.getNextToken();
	errorCheck ( Name, "NAME" );
	Token Rparen = lex.getNextToken();
	   if ( Rparen.isSymbol( ")" ){
		Node first = parseExpression();
		return new Node("def", Name, first, null, null);
	   }
	   else {
		lex.putBackToken(Rparen);
		Node first = parseParams();
		Rparen = lex.getNextToken();
		errorCheck ( Rparen, "symbol", "): );
  		Node second = parseExpression();
		Rparen = lex.getNextToken();
		errorCheck ( Rparen, "symbol", ")" );
		return new Node("def", Name, first, second, null);
	   }
	  
   }
   
   public Node parseParams(){
	   System.out.println("-----> parsing <params>:);
	   Token Name = lex.getNextToken();
	   errorCheck ( Name, "NAME" );
	   Token check = lex.getNextToken();
	   if (check.isKind("NAME")){
            	Node first = parseParams();
		return new Node("params", Name, first, null, null);
	   }		      
	   return new Node("params", Name, null, null, null);
   }
