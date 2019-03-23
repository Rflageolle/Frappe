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
      System.out.println("-----> parsing <program>:");
      Node first = parseFuncCall();
      Token token = lex.getNextToken();
      if ( token.isKind("eof") ) {
         return new Node( "program", first, null, null );
      }
      else {// have a funcDef
         lex.putBackToken( token );
         Node second = parseFuncDefs();
        return new Node("program", first, second, null );
      }
   }

   public Node parseFuncDefs() {
      System.out.println("-----> parsing <funcDefs>:");

      Node first = parseFuncDef();

      // look ahead to see if there are more funcDef's
      Token token = lex.getNextToken();

      if ( token.isKind("eof") ) {
         return new Node( "funcDefs", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseFuncDefs();
         return new Node( "funcDefs", first, second, null );
      }
   }

   public Node parseFuncDef() {
      System.out.println("-----> parsing <funcDef>:");

      Token token = lex.getNextToken();
      errorCheck( token, "def" );

      Token name = lex.getNextToken();  // the function name
      errorCheck( name, "var" );

      token = lex.getNextToken();
      errorCheck( token, "single", "(" );

      token = lex.getNextToken();

      if ( token.matches("single", ")" )) {// no params

         token = lex.getNextToken();
         if ( token.isKind("end") ) {// no statements
            return new Node("funcDef", name.getDetails(), null, null, null );
         }
         else {// have a statement
            lex.putBackToken( token );
            Node second = parseStatements();
            token = lex.getNextToken();
            errorCheck( token, "end" );
            return new Node("funcDef", name.getDetails(), null, second, null );
         }
      }// no params
      else {// have params
         lex.putBackToken( token );
         Node first = parseParams();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );

         token = lex.getNextToken();

         if ( token.isKind( "end" ) ) {// no statements
            return new Node( "funcDef", name.getDetails(), first, null, null );
         }
         else {// have statements
            lex.putBackToken( token );
            Node second = parseStatements();
            token = lex.getNextToken();
            errorCheck( token, "end" );
            return new Node("funcDef", name.getDetails(), first, second, null );
         }

      }// have params

   }// parseFuncDef


   private Node parseParams() {
      System.out.println("-----> parsing <params>:");

      Token token = lex.getNextToken();
      errorCheck( token, "var" );

      Node first = new Node( "var", token.getDetails(), null, null, null );

      token = lex.getNextToken();

      if ( token.matches( "single", ")" ) ) {// no more params
         lex.putBackToken( token );  // funcCall handles the )
         return new Node( "params", first, null, null );
      }
      else if ( token.matches( "single", "," ) ) {// have more params
         Node second = parseParams();
         return new Node( "params", first, second, null );
      }
      else {// error
         System.out.println("expected , or ) and saw " + token );
         System.exit(1);
         return null;
      }

   }// <params>

   private Node parseStatements() {
      System.out.println("-----> parsing <statements>:");

      Node first = parseStatement();

      // look ahead to see if there are more statement's
      Token token = lex.getNextToken();

      if ( token.isKind("eof") ) {
         return new Node( "stmts", first, null, null );
      }
      else if ( token.isKind("end") ||
                token.isKind("else")
              ) {
         lex.putBackToken( token );
         return new Node( "stmts", first, null, null );
      }
      else {
         lex.putBackToken( token );
         Node second = parseStatements();
         return new Node( "stmts", first, second, null );
      }
   }// <statements>

   private Node parseFuncCall() {
      System.out.println("-----> parsing <funcCall>:");

      Token name = lex.getNextToken(); // function name
      errorCheck( name, "var" );

      Token token = lex.getNextToken();
      errorCheck( token, "single", "(" );

      token = lex.getNextToken();

      if ( token.matches( "single", ")" ) ) {// no args
         return new Node( "funcCall", name.getDetails(), null, null, null );
      }
      else {// have args
         lex.putBackToken( token );
         Node first = parseArgs();
         return new Node( "funcCall", name.getDetails(), first, null, null );
      }

   }// <funcCall>

   private Node parseArgs() {
      System.out.println("-----> parsing <args>:");

      Node first = parseExpr();

      Token token = lex.getNextToken();

      if ( token.matches( "single", ")" ) ) {// no more args
         return new Node( "args", first, null, null );
      }
      else if ( token.matches( "single", "," ) ) {// have more args
         Node second = parseArgs();
         return new Node( "args", first, second, null );
      }
      else {// error
         System.out.println("expected , or ) and saw " + token );
         System.exit(1);
         return null;
      }

   }// <args>

   private Node parseStatement() {
      System.out.println("-----> parsing <statement>:");

      Token token = lex.getNextToken();

      // --------------->>>  <str>
      if ( token.isKind("string") ) {
         return new Node( "str", token.getDetails(),
                          null, null, null );
      }
      // --------------->>>   <var> = <expr> or funcCall
      else if ( token.isKind("var") ) {
         String varName = token.getDetails();
         token = lex.getNextToken();

         if ( token.matches("single","=") ) {// assignment
            Node first = parseExpr();
            return new Node( "sto", varName, first, null, null );
         }
         else if ( token.matches("single","(")) {// funcCall
            lex.putBackToken( token );
            lex.putBackToken( new Token("var",varName) );
            Node first = parseFuncCall();
            return first;
         }
         else {
            System.out.println("<var> must be followed by = or (, "
                  + " not " + token );
            System.exit(1);
            return null;
         }
      }
      // --------------->>>   if ...
      else if ( token.isKind("if") ) {
         Node first = parseExpr();

         token = lex.getNextToken();

         if ( token.isKind( "else" ) ) {// no statements for true case
            token = lex.getNextToken();
            if ( token.isKind( "end" ) ) {// no statements for false case
               return new Node( "if", first, null, null );
            }
            else {// have statements for false case
               lex.putBackToken( token );
               Node third = parseStatements();
               token = lex.getNextToken();
               errorCheck( token, "end" );
               return new Node( "if", first, null, third );
            }
         }
         else {// have statements for true case
            lex.putBackToken( token );
            Node second = parseStatements();

            token = lex.getNextToken();
            errorCheck( token, "else" );

            token = lex.getNextToken();

            if ( token.isKind( "end" ) ) {// no statements for false case
               return new Node( "if", first, second, null );
            }
            else {// have statements for false case
               lex.putBackToken( token );
               Node third = parseStatements();
               token = lex.getNextToken();
               errorCheck( token, "end" );
               return new Node( "if", first, second, third );
            }
         }

      }// if ...

      else if ( token.isKind( "return" ) ) {
         Node first = parseExpr();
         return new Node( "return", first, null, null );
      }// return

      else {
         System.out.println("Token " + token +
                             " can't begin a statement");
         System.exit(1);
         return null;
      }

   }// <statement>

   private Node parseExpr() {
      System.out.println("-----> parsing <expr>");

      Node first = parseTerm();

      // look ahead to see if there's an addop
      Token token = lex.getNextToken();

      if ( token.matches("single", "+") ||
           token.matches("single", "-")
         ) {
         Node second = parseExpr();
         return new Node( token.getDetails(), first, second, null );
      }
      else {// is just one term
         lex.putBackToken( token );
         return first;
      }

   }// <expr>

   private Node parseTerm() {
      System.out.println("-----> parsing <term>");

      Node first = parseFactor();

      // look ahead to see if there's a multop
      Token token = lex.getNextToken();

      if ( token.matches("single", "*") ||
           token.matches("single", "/")
         ) {
         Node second = parseTerm();
         return new Node( token.getDetails(), first, second, null );
      }
      else {// is just one factor
         lex.putBackToken( token );
         return first;
      }

   }// <term>

   private Node parseFactor() {
      System.out.println("-----> parsing <factor>");

      Token token = lex.getNextToken();

      if ( token.isKind("num") ) {
         return new Node("num", token.getDetails(), null, null, null );
      }
      else if ( token.isKind("var") ) {
         // could be simply a variable or could be a function call
         String name = token.getDetails();

         token = lex.getNextToken();

         if ( token.matches( "single", "(" ) ) {// is a funcCall
            lex.putBackToken( new Token( "single", "(") );  // put back the (
            lex.putBackToken( new Token( "var", name ) );  // put back name
            Node first = parseFuncCall();
            return first;
         }
         else {// is just a <var>
            lex.putBackToken( token );  // put back the non-( token
            return new Node("var", name, null, null, null );
         }
      }
      else if ( token.matches("single","(") ) {
         Node first = parseExpr();
         token = lex.getNextToken();
         errorCheck( token, "single", ")" );
         return first;
      }
      else if ( token.matches("single","-") ) {
         Node first = parseFactor();
         return new Node("opp", first, null, null );
      }
      else {
         System.out.println("Can't have a factor starting with " + token );
         System.exit(1);
         return null;
      }

   }// <factor>

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

// parse program
public Node parseProgram() {
  System.out.println("-----> parsing <program>");
  Node first = parseClasses();
  return new Node("program", first, null, null);
}

// parse classes
public Node parseClasses() {
  System.out.println("-----> parsing <classes>");
  Node first = parseClass();
  Token token = lex.getNextToken();
  if (token.isKind("class")) {
    return new Node("classes", first, null, null);
  } else {
    lex.putBackToken( token );
    Node second = parseClasses();
    return new Node("classes", first, second, null);
  }
}

// parse class
public Node parseClass() {
  System.out.println("-----> parsing <class>");

  Token keyClass = lex.getNextToken();
  errorCheck( keyClass, "class" );

  Token keyClassname = lex.getNextToken();  // the function name
  errorCheck( keyClassname, "CLASSNAME" );

  Token lBrace = lex.getNextToken();
  errorCheck( lbrace, "{");

  Token token = lex.getNextToken();

  if ( token.isKind("}")) {
    return new Node("class", null, null, null);
  } else {
    lex.putBackToken( token );
    Node first = parseMembers();
  }      //// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

}

// parse members
public Node parseMembers() {
  System.out.println("-----> parsing <members>");
  Node first = parseMember();
  Token token = lex.getNextToken();
  if ( token.isKind( "}") ) {
    return new Node("members", first, null, null);
  } else {
    lex.putBackToken(token);
    Node second = parseMembers()
    return new Node("memebers", first, second, null);
  }
}

// parse member
public Node parseMember() {
  System.out.println("-----> parsing <member>");
  Token keyword = lex.getNextToken();
  if ( keyword.matches("keyword", "STATIC")) {
    Token name = lex.getNextToken();
    errorCheck(name, "NAME");
    Token token = lex.getNextToken();
    if ( token.isSymbol("(")) {
      //static method
      lex.putBackToken(keyword);
      lex.putBackToken(name);
      lex.putBackToken(token);
      Node first = parseStaticMethod();
      return new Node("member", first, null, null);
    } else {
      //staticfield with expression
      lex.putBackToken(keyword);
      lex.putBackToken(name);
      lex.putBackToken(token);
      Node first = parseStaticField();
      return new Node("member", first, null, null);
    }
  } else if (keyword.isKind("NAME")) {
    Token check = lex.getNextToken();
    lex.putback( keyword ); // put back instanceField or instanceMethod NAME
    if ( check.isSymbol("(")) {
      //instanceMethod
      lex.putBackToken( check );
      Node first = parseInstanceMethod();
      return new Node("member", first, null, null);
    } else {
      //instanceField
      lex.putBackToken( check );
      Node first = parseInstanceField();
      return new Node("member", first, null, null);
    }
  } else {
    // constructor
    lex.putBackToken( keyword );
    lex.putBackToken( check );
    Node first = parseConstructor();
    return new Node("member", first, null, null);
  } //end parse member

// parse staticField
public Node parseStaticField() {
	System.out.println("-----> parsing <staticField>");
  // static should be kind STATIC
  Token keyword = lex.getNextToken();
  if ( keyword.matches("keyword", "STATIC") ) {
    // name should be kind NAME
    Token name = lex.getNextToken();
    errorCheck(name, "NAME");
    Token equ = lex.getNextToken();
    if ( equ.isSymbol("=") ) {
      // staticField with expression
      Node first = parseExpr();
      return new Node("staticField", name.getDetails(), first, null, null);
    } else {
      // staticField w/o
    	equ.putBackToken( equ );
      return new Node("staticField", name.getDetails(), null, null, null);
    }
  }
}

// parse staticmethod
public Node parseStaticMethod() {
	System.out.println("-----> parsing <staticMethod>");
  // STATIC NAME <restOfMethod>
  Token keyword = lex.getNextToken();
  if ( keyword.matches("keyword", "STATIC")) {
    //consume static
    Token name = lex.getNextToken();
    errorCheck(name, "NAME");
    // consume name

    Node first = parseRestOfMethod();
    return new Node("staticMethod", name.getDetails(), first, null, null);
  }
}

// parse instatnceField
public Node parseInstanceField() {
	System.out.println("-----> parsing <instanceField>");
  // NAME
  Token name = lex.getNextToken();
  errorCheck(name, "NAME");

  if ( static.isKind("NAME") ) {
    return new Node("instanceField", name.getDetails(), null, null, null);
  }
}

// parse constructor
public Node parseConstructor() {
	System.out.println("-----> parsing <constructor>");
  // CLASSNAME <restOfMethod>
  Token classname = lex.getNextToken();
  errorCheck(classname, "CLASSNAME");

  Node first = parseRestOfMethod();
  return new Node("constructor", classname.getDetails(), first, null, null);
}

// parse instanceMethod
public Node parseInstanceMethod() {
	System.out.println("-----> parsing <instanceMethod>");
  // NAME <restOfMethod>
  Token name = lex.getNextToken();
  errorCheck(name, "NAME");

  Node first = parseRestOfMethod();
  return new Node("instanceMethod", name.getDetails(), first, null, null);
}

// parse restOfMethod
public Node parseRestOfMethod() {
	System.out.println("-----> parsing <restOfMethod>");
  // LPAREN RPAREN <methodBody> |
  // LPAREN <params> RPAREN <methodBody>

  // consume lparen
  Token lparen = lex.getNextToken();
  errorCheck(lparen, "symbol", "(");

  Token check = lex.getNextToken();
  if ( check.isSymbol( ")" ) ) {
    Node first = parseMethodBody();
    return new Node("restOfMethod", first, null, null);
  } else {
    // consume <params>
    lex.putBackToken( check );
    Node first = parseParams();
    // consume rparn
    Token rparen = lex.getNextToken();
    if ( rparen.isSymbol( ")" ) ) {
      Node second = parseMethodBody();
      return new Node("restOfMethod", first, second, null);
    }
  }
}

// parse params
public Node parseParams() {
	System.out.println("-----> parsing <params>");
  // NAME ||
  // NAME COMMA <params>
  Token name = lex.getNextToken();
  errorCheck( name, "NAME" );

  // check for COMMA
  Token check = lex.getNextToken();
  if( check.isSymbol( "," ) ) {
  	Node first = parseParams();
  	return new Node("params", name.getDetails(), first, null, null);
  }
  // down to one param
  lex.putBackToken( check );
  return new Node("params", name.getDetails(), null, null, null);
}
// parse methodBody
public Node parseMethodBody() {
	// consume lparen
	Token lbrace = lex.getNextToken();
	errorCheck( lbrace, "symbol", "{" );

	Token check = lex.getNextToken();
	// check if an empty method body
	if( check.isSymbol("}") ) {
		return new Node("methodBody", null, null, null);
	} else {
		lex.putBackToken( check );
		Node first = parseStatements();
		return new Node("methodBody", first, null, null);
	}
}

// parse statements
public Node parseStatements() {
	System.out.println("-----> parsing <statements>");
	// at least one statment
	Node first = parseStatement();
	// get next token to see if end of statements
	Token check = lex.getNextToken();
	if( check.isSymbol("}") ) {
		return new Node("statements", first, null, null);
	} else {
		lex.putBackToken( check );
		Node second = parseStatements();
		return new Node("statements", first, second, null);
	}
}

// parse statement
public Node parseStatement() {
	System.out.println("-----> parsing <statement>");

	Token token = lex.getNextToken();
	if( token.isKind("NAME") || token.isKind("CLASSNAME") ) { // either NAME EQUALS <rhs> | <refChain>
		Token check = lex.getNextToken();
		if( check.isSymbol("=") ) { // NAME EQUALS <rhs>
			Node first = parseRHS();
			return new Node("statement", first, null, null);

		} else { // <refChain>
      lex.putBackToken( token );
      lex.putBackToken( check );
      Node first = parseRefChain();
      return new Node("statement", first, null, null);

		}
	} else if( token.matches("keyword", "WHILE") ) { // <whileStatement>
    Node first = parseWhileStatement();
    return new Node("statement", first, null, null);

	} else if( token.matches("keyword", "IF") ) { // <ifStatement>
    Node first = parseIfStatement();
    return new Node("statement", first, null, null);

	} else { // RETURN <expression>
    // no need to put back RETURN token
    Node first = parseExpr();
    return new Node("statement", first, null, null);
	}
}

// parse whileStatement
public Node parseWhileStatement() {
  System.out.println("-----> parsing <whileStatement>");

  Token lparen = lex.getNextToken();
  errorCheck( lparen, "SYMBOL", "(");

  // parse while expression
  Node first = parseExpr();
  Token rparen = lex.getNextToken();
  errorCheck( rparen, "SYMBOL", ")" );

  // make sure we start the loop body with '{'
  Token lbrace = lex.getNextToken();
  errorCheck( lbrace, "SYMBOL", "{");

  // put back brace and call parse <loopBody>
  lex.putBackToken( lbrace );
  Node second = parseLoopBody();
  return new Node("statement", first, second, null);
}
      
// parse ifStatement
public Node parseIfStatement() {
  System.out.println("-----> parsing <ifStatement>");

  //consume left paren
  Token lparen = lex.getNextToken();
  errorCheck( lparen, "SYMBOL", "(" );

  Node first = parseExpr();
  // consume right paren
  Token rparen = lex.getNextToken();
  errorCheck( rparen, "SYMBOL", ")" );

  // check for statements in if body
  Token lbrace = lex.getNextToken();
  errorCheck( lbrace, "SYMBOL", "{" );
  Token check = lex.getNextToken();
  if( !check.isSymbol("}") ) {
    lex.putBackToken( check );
    Node second = parseStatements();
    // consume right brace of if body
    Token rbrace = lex.getNextToken();
    errorCheck( rbrace, "SYMBOL", "}" );

    // check for ELSE statement
    Token check = lex.getNextToken();
    if( check.matches("keyword", "ELSE") ) {
      // consume left brace of else
      Token lbrace = lex.getNextToken();
      errorCheck( lbrace, "SYMBOL", "{" );

      Token braceCheck = lex.getNextToken();
      if( !braceCheck.isSymbol("}") ) {
        lex.putBackToken( braceCheck );
        Node third = parseStatements();
        // if and else had statements so three nodes
        return new Node("ifStatement", first, second, third);
      } else {
        // empty else body so only two nodes
        return new Node("ifStatement", first, second, null);
      }
    } else {
      lex.putBackToken( check );
      // if block with statements, no else
      return new Node("ifStatement", first, second, null);
    }
  } else {
    // empty if body, check for else
    Token elseCheck = lex.getNextToken();
    if( elseCheck.matches("keyword", "ELSE") ) {
      Token lbrace = lex.getNextToken();
      errorCheck( lbrace, "SYMBOL", "{" );

      // check for statements in else body
      Token braceCheck = lex.getNextToken();
      if( !braceCheck.isSymbol("}") ) {
        lex.putBackToken( braceCheck );
        Node second = parseStatements();
        // empty if body but else has statements: 2 nodes
        return new Node("ifStatement", first, second, null);
      }
    }
  }
  // empty if and else bodies: 1 node
  return new Node("ifStatement", first, null, null);
}

// parse rhs
public Node parseRHS() {
  System.out.println("-----> parsing <rhs>");

  Token check = lex.getNextToken();
  if( check.matches("keyword", "NEW") ) { // NEW CLASSNAME <argPart>
    Token className = lex.getNextToken();
    Node first = parseArgsPart();
    return new Node("rhs", first, null, null);

  } else { // <expression>
    lex.putBackToken( check );
    Node first = parseExpr();
    return new Node("rhs", first, null, null);

  }
}

// parse loopBody
public Node parseLoopBody() {
  System.out.println("-----> parsing <loopBody>");

  // consume left brace of loop body
  Token lbrace = lex.getNextToken();
  errorCheck( lbrace, "SYMBOL", "{" );

  // check for <statements>
  Token check = lex.getNextToken();
  if( !check.isSymbol("}") ) {
    lex.putBackToken( check );
    Node first = parseStatements();
    return new Node("loopBody", first, null, null);
  
  } 
  // empty loop body
  return new Node("loopBody", null, null, null);
}

// parse expression
public Node parseExpr() {
  System.out.println("-----> parsing <expression>");

  // check if <refChain> or not
  Token check = lex.getNextToken();
  if( check.matches("keyword", "THIS") || check.matches("keyword", "NULL") ||
      check.matches("keyword", "TRUE") || check.matches("keyword", "FALSE") ) {

    // not <refChain> and "keyword"
    return new Node("expression", check.getDetails(), null, null, null);

  } else if( check.isKind("STR") || check.isKind("NUM") ) {
    // not <refChain> and NUM or STR
    return new Node("expression", check.getKind(), null, null, null);
  }
  // put Token back and call parse <refChain>
  lex.putBackToken( check );
  Node first = parseRefChain();
  return new Node("expression", first, null, null);
}

// parse refChain
public Node parseRefChain() {
  System.out.println("-----> parsing <refChain>");

  // both cases start with <caller>
  Node first = parseCaller();

  // check for DOT <refChain>
  Token check = lex.getNextToken();
  if( check.isSymbol(".") ) {
    // there is a <refChain>
    Node second = parseRefChain();
    return new Node("refChain", first, second, null);
  }
  // no <refChain>, put back Token and return first
  lex.putBackToken( check );
  return new Node("refChain", first, null, null);
}

// parse caller
public Node parseCaller() {
  System.out.println("-----> parsing <caller>");

  Token token = lex.getNextToken();
  // check if token is CLASSNAME
  if( token.isKind("CLASSNAME") ) {
    // no parsing calls, return null node with classname
    return new Node("caller", token.getDetails(), null, null, null);

  } else if( token.isKind("NAME") ) {
    // check if just NAME or NAME <argsPart>
    Token check = lex.getNextToken();
    if( check.isSymbol("(") ) {
      // has <argsPart>
      lex.putBackToken( check );
      Node first = parseArgsPart();
      return new Node("caller", token.getDetails(), first, null, null);
    }
    // just NAME, no <argsPart>
    lex.putBackToken( check );
    return new Node("caller", token.getDetails(), null, null, null);
  }
}

// parse argsPart
// parse args
