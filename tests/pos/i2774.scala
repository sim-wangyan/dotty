object Test {
  trait T; trait Q
  val a: (given T) => (given Q) => Int = 1

  given Q = new Q {}
  val i1: Int = a(given new T{})
  given T = new T {}
  val i2: Int =  a
  val i3: Int =  a2

  def a2(given t: T)(given q: Q): Int = 1

}
