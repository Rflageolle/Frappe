import java.util.Scanner;

public class P11 {

   public static void main(String[] args) throws Exception {

      String name;

      if ( args.length == 1 ) {
         name = args[0];
      }
      else {
         System.out.print("Enter name of P11 program file: ");
         Scanner keys = new Scanner( System.in );
         name = keys.nextLine();
      }

      Lexer lex = new Lexer( name );
      NewParser parser = new NewParser( lex );

      // start with <statements>
      Node root = parser.parseDefs();

      // display parse tree for debugging/testing:
      TreeViewer viewer = new TreeViewer("Parse Tree", 0, 0, 1800, 1500, root );

      // execute the parse tree
      // root.execute();


   // while make parse tree thisnk about how to make mem table for where the entry into how to
   // evaluate starts, so when REPLparer sees a file definded function it can start from the
   // expression node to evaluate


   }// main

   // listens to the CL and parses calls
   // private void REPL() {
   //   Scanner in = new Scanner( System.in );
   //   String call = in.nextLine();
   //
   //   do {
   //     Lexer lex = new Lexer( call );
   //     Parser parse = new ReplParser( lex );
   //
   //   } while (call != null);
   // }
}
