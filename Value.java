/*
  a "value" is either a
  double or a list of values,

  is a number iff list is null

  Value instances are immutable, so for
  example to insert a value x into
  existing value v, must do
  v = v.insert( x );

  because doing
  v.insert( x );
  produces a new Value that is discarded
  and v is unchanged!

*/

import java.util.LinkedList;

public class Value {

   public static Value ZERO = new Value( 0 );
   public static Value ONE = new Value( 1 );

   private double number;
   private LinkedList<Value> list;

   public Value( double num ) {
      number = num;
      list = null;  //
   }

   public Value( String s ) {
      number = Double.parseDouble( s );
      list = null;
   }

   // construct a value which is an empty list
   public Value() {
      list = new LinkedList<Value>();
   }

   // produce a deep copy of given value
   public Value( Value other ) {
      if ( other.list==null ) {// other is just a number
         this.number = other.number;
         this.list = null;
      }
      else {// other is a list
         list = new LinkedList<Value>();
         for (int k=0; k<other.list.size(); k++) {
            list.add( new Value( other.list.get(k) ) );
         }
      }
   }// copy constructor

   // represent this value as a string
   public String toString() {
      if (list == null) {// is simply a number
         if ( number == (int) number ) {
            return "" + ((int) number);
         }
         else {
            return "" + number;
         }
      }
      else {// is a list
         String s = "(";
         for (int k=0; k<list.size(); k++) {
            s += list.get(k).toString() + " ";
         }
         s += ")";
         return s;
      }

   }// toString

   // return a new Value that is the
   // first item in this
   public Value first() {
      if ( list==null ) {
         System.out.println("Oops, this Value is a number");
         System.exit(1);
         return null;
      }
      else if ( list.size() == 0 ) {
         System.out.println("Oops, this Value is an empty list");
         System.exit(1);
         return null;
      }
      else {// is a list and has a first element
         // call copy constructor
         return new Value( list.get(0) );
      }

   }// first

   // return a new Value that is the
   // rest of this list
   public Value rest() {
      if ( list==null ) {
         System.out.println("Oops, this Value is a number");
         System.exit(1);
         return null;
      }
      else if ( list.size() == 0 ) {
         System.out.println("Oops, this Value is an empty list");
         System.exit(1);
         return null;
      }
      else {// is a list and has a first element
         // call copy constructor
         Value temp = new Value( this );
         temp.list.remove( 0 );  // remove the first item
         return temp;
      }

   }// rest

   // insert given value in the
   // front of this value, which must
   // be a list
   public Value insert( Value a ) {
      if ( list==null ) {
         System.out.println("Oops, this Value is a number");
         System.exit(1);
         return null;
      }
      else {// is a list
         // call copy constructor
         Value temp = new Value( this );
         temp.list.add( 0, a );
         return temp;
      }

   }// insert

   public boolean isEmpty() {
      return list != null && list.size()==0;
   }

   public boolean isNumber() {
      return list == null;
   }

   public double getNumber() {
      if ( list == null ) {// is a number
         return number;
      }
      else {
         System.out.println("Oops, [" + this + "] is not a number");
         System.exit(1);
         return 0;
      }
   }

   public boolean numericEquals( Value other ) {
      if ( list == null && other.list==null ) {// both numbers
         return number == other.number;
      }
      else {
         System.out.println("Oops, [" + this + "] is not a number");
         System.exit(1);
         return false;
      }
   }

   public static void main(String[] args) {
      Value x = new Value( 37 );
      System.out.println("Should be number 37: " + x );

      Value a = new Value();

      x = new Value( "19" );
      Value y = new Value( "23" );
      Value z = new Value( "37" );

      a = a.insert( x );
      a = a.insert( y );
      a = a.insert( z );

      System.out.println("now have [" + a + "]");

      x = new Value();
      x = x.insert( new Value( 9 ) );
      a = a.insert( x );
      System.out.println("now have [" + a + "]");

      Value b = new Value( a );
      System.out.println("copy is " + b);
   }// main

}
