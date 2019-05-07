class StackFrameList {

  StackFrameCell head;
  int argnum;

  public StackFrameList() {
    head = null();
  }

  public void setHead(String str) {

    head = New StackFrameCell(str);

  }

  private class StackFrameCell {

    ArrayList[StackFrameCell] args = new ArrayList[StackFrameCell]()
    String name;
    String alias;
    Value value;
    int argnum;
    Boolean userDefined;

    public StackFrameCell(String n, Value v) {
      name = n;
      value = v;
      userDefined = false;
    }

    public StackFrameCell(String n) {
      name = n;
      value = v;
      userDefined = false;
    }

    public getName() {
      return this.name;
    }

    public getValue() {
      return this.value;
    }

    // when we are going through args want to remember where we are
    public StackFrameCell getArg( n ) {
      return args[n];
    }

    public void setValue(Value v) {
      this.value = v;
    }

    public void setAlias(String paramName) {
      this.alias = paramName;
    }

    public StackFrameCell nextLevel() {
      if ( head != null ) {
        if (args[argnum] != null) {
          StackFrameCell next = args[argnum];
          argnum++;
        } else {
          System.out.println("there are no args for the call");
          Sytem.exit();
        }
      } else {
        System.out.println("there are no args for the call");
        System.exit();
      }
    }

    public Value returnNextArg() {
      StackFrameCell next = head.nextLevel();
      if ( next.getValue() != null ) {
        return next.getValue();
      } else {
        // calculate value
        return next.calculate();
      }
    }

    public void enterNextArg() {
      this = next;
    }

    public Value calculate() {
      if ( !userDefined ) {
        switch ( name ) {
          case "plus":
            Value x = returnNextArg();
            Value y = returnNextArg();
            return new Value(x.getNumber() + y.getNumber());
          case "minus":
            Value x = returnNextArg();
            Value y = returnNextArg();
            return new Value(x.getNumber() - y.getNumber());
          case "times":
            Value x = returnNextArg();
            Value y = returnNextArg();
            return new Value(x.getNumber() * y.getNumber());
          case "div":
            Value x = returnNextArg();
            Value y = returnNextArg();
            return new Value(x.getNumber() / y.getNumber()); // need to catch div by zero exception
          case "lt":
            Value x = returnNextArg();
            Value y = returnNextArg();
            if ( x.getNumber() < y.getNumber() ) {
              return Value.ONE;
            } else { return Value.ZERO }
          case "le":
            Value x = returnNextArg();
            Value y = returnNextArg();
            if ( x.getNumber() <= y.getNumber() ) {
              return Value.ONE;
            } else { return Value.ZERO }
          case "eq":
            Value x = returnNextArg();
            Value y = returnNextArg();
            if ( x.getNumber() == y.getNumber() ) {
              return Value.ONE;
            } else { return Value.ZERO }
          case "ne":
            Value x = returnNextArg();
            Value y = returnNextArg();
            if ( x.getNumber() != y.getNumber() ) {
              return Value.ONE;
            } else { return Value.ZERO }
          case "and":
            Value x = returnNextArg();
            Value y = returnNextArg();
            if ( x.getNumber() < 0.0 && y.getNumber() < 0.0) {
              return Value.ONE;
            } else { return Value.ZERO }
          case "or":
            Value x = returnNextArg();
            Value y = returnNextArg();
            if ( x.getNumber() < 0.0 || y.getNumber() < 0.0 ) {
              return Value.ONE;
            } else { return Value.ZERO }
          case "not":
            Value x = returnNextArg();
            if ( x.getNumber == 0.0 ) {
              return Value.ONE;
            } else { return Value.ZERO }
          case "ins":
            Value x = returnNextArg();
            Value y = returnNextArg();
            return y.insert( x );
          case "first":
            Value y = returnNextArg();
            return y.first();
          case "rest":
            Value y = returnNextArg();
            return y.rest();
          case "null":
            Value x = returnNextArg();
            return x.null();
          case "list":
            Value x = returnNextArg();
            if ( x.isNumber() ) {
              return Value.ZERO;
            } else {
              return Value.ONE;
            }
        }
      }
    }

  }
}
