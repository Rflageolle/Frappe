class Value {

  // will be treated as a Double
  private class Number {
    private Double value;

    public Number( int val) {
      value = Double.parseInt(val);
    }

    public Number( String val ) {
      value = String.parseDouble( val );
    }
  }
}
