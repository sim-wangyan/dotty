package scala.runtime

import scala.Tuple.{ Concat, Size, Head, Tail, Elem, Zip, Map, Take, Drop, Split }

object DynamicTuple {
  inline val MaxSpecialized = 22
  inline private val XXL = MaxSpecialized + 1

  def to$Array(xs: Tuple, n: Int) = {
    val arr = new Array[Object](n)
    var i = 0
    var it = xs.asInstanceOf[Product].productIterator
    while (i < n) {
      arr(i) = it.next().asInstanceOf[Object]
      i += 1
    }
    arr
  }

  def cons$Array[H](x: H, elems: Array[Object]): Array[Object] = {
    val elems1 = new Array[Object](elems.length + 1)
    elems1(0) = x.asInstanceOf[Object]
    System.arraycopy(elems, 0, elems1, 1, elems.length)
    elems1
  }

  def dynamicToArray(self: Tuple): Array[Object] = (self: Any) match {
    case self: TupleXXL => self.toArray
    case self: Product => productToArray(self)
    case self: Unit => Array.emptyObjectArray
  }

  def dynamicToIArray(self: Tuple): IArray[Object] = (self: Any) match {
    case self: TupleXXL => self.elems
    case self: Product => productToArray(self).asInstanceOf[IArray[Object]]
    case self: Unit => Array.emptyObjectArray.asInstanceOf[IArray[Object]]
  }

  def productToArray(self: Product): Array[Object] = {
    val arr = new Array[Object](self.productArity)
    var i = 0
    while (i < arr.length) {
      arr(i) = self.productElement(i).asInstanceOf[Object]
      i += 1
    }
    arr
  }

  def dynamicFromArray[T <: Tuple](xs: Array[Object]): T = xs.length match {
    case 0  => ().asInstanceOf[T]
    case 1  => Tuple1(xs(0)).asInstanceOf[T]
    case 2  => Tuple2(xs(0), xs(1)).asInstanceOf[T]
    case 3  => Tuple3(xs(0), xs(1), xs(2)).asInstanceOf[T]
    case 4  => Tuple4(xs(0), xs(1), xs(2), xs(3)).asInstanceOf[T]
    case 5  => Tuple5(xs(0), xs(1), xs(2), xs(3), xs(4)).asInstanceOf[T]
    case 6  => Tuple6(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5)).asInstanceOf[T]
    case 7  => Tuple7(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6)).asInstanceOf[T]
    case 8  => Tuple8(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7)).asInstanceOf[T]
    case 9  => Tuple9(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8)).asInstanceOf[T]
    case 10 => Tuple10(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9)).asInstanceOf[T]
    case 11 => Tuple11(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10)).asInstanceOf[T]
    case 12 => Tuple12(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11)).asInstanceOf[T]
    case 13 => Tuple13(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12)).asInstanceOf[T]
    case 14 => Tuple14(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13)).asInstanceOf[T]
    case 15 => Tuple15(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13), xs(14)).asInstanceOf[T]
    case 16 => Tuple16(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13), xs(14), xs(15)).asInstanceOf[T]
    case 17 => Tuple17(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13), xs(14), xs(15), xs(16)).asInstanceOf[T]
    case 18 => Tuple18(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13), xs(14), xs(15), xs(16), xs(17)).asInstanceOf[T]
    case 19 => Tuple19(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13), xs(14), xs(15), xs(16), xs(17), xs(18)).asInstanceOf[T]
    case 20 => Tuple20(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13), xs(14), xs(15), xs(16), xs(17), xs(18), xs(19)).asInstanceOf[T]
    case 21 => Tuple21(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13), xs(14), xs(15), xs(16), xs(17), xs(18), xs(19), xs(20)).asInstanceOf[T]
    case 22 => Tuple22(xs(0), xs(1), xs(2), xs(3), xs(4), xs(5), xs(6), xs(7), xs(8), xs(9), xs(10), xs(11), xs(12), xs(13), xs(14), xs(15), xs(16), xs(17), xs(18), xs(19), xs(20), xs(21)).asInstanceOf[T]
    case _ => TupleXXL.fromIArray(xs.clone().asInstanceOf[IArray[Object]]).asInstanceOf[T]
  }

  def dynamicFromIArray[T <: Tuple](xs: IArray[Object]): T =
    if (xs.length <= 22) dynamicFromArray(xs.asInstanceOf[Array[Object]])
    else TupleXXL.fromIArray(xs).asInstanceOf[T]

  def dynamicFromProduct[T <: Tuple](xs: Product): T = (xs.productArity match {
    case 1 =>
      xs match {
        case xs: Tuple1[_] => xs
        case xs => Tuple1(xs.productElement(0))
      }
    case 2 =>
      xs match {
        case xs: Tuple2[_, _] => xs
        case xs => Tuple2(xs.productElement(0), xs.productElement(1))
      }
    case 3 =>
      xs match {
        case xs: Tuple3[_, _, _] => xs
        case xs => Tuple3(xs.productElement(0), xs.productElement(1), xs.productElement(2))
      }
    case 4 =>
      xs match {
        case xs: Tuple4[_, _, _, _] => xs
        case xs => Tuple4(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3))
      }
    case 5 =>
      xs match {
        case xs: Tuple5[_, _, _, _, _] => xs
        case xs => Tuple5(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4))
      }
    case 6 =>
      xs match {
        case xs: Tuple6[_, _, _, _, _, _] => xs
        case xs => Tuple6(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5))
      }
    case 7 =>
      xs match {
        case xs: Tuple7[_, _, _, _, _, _, _] => xs
        case xs => Tuple7(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6))
      }
    case 8 =>
      xs match {
        case xs: Tuple8[_, _, _, _, _, _, _, _] => xs
        case xs => Tuple8(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7))
      }
    case 9 =>
      xs match {
        case xs: Tuple9[_, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple9(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8))
      }
    case 10 =>
      xs match {
        case xs: Tuple10[_, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple10(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9))
      }
    case 11 =>
      xs match {
        case xs: Tuple11[_, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple11(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10))
      }
    case 12 =>
      xs match {
        case xs: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple12(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11))
      }
    case 13 =>
      xs match {
        case xs: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple13(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12))
      }
    case 14 =>
      xs match {
        case xs: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple14(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13))
      }
    case 15 =>
      xs match {
        case xs: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple15(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13), xs.productElement(14))
      }
    case 16 =>
      xs match {
        case xs: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple16(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13), xs.productElement(14), xs.productElement(15))
      }
    case 17 =>
      xs match {
        case xs: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple17(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13), xs.productElement(14), xs.productElement(15), xs.productElement(16))
      }
    case 18 =>
      xs match {
        case xs: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple18(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13), xs.productElement(14), xs.productElement(15), xs.productElement(16), xs.productElement(17))
      }
    case 19 =>
      xs match {
        case xs: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple19(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13), xs.productElement(14), xs.productElement(15), xs.productElement(16), xs.productElement(17), xs.productElement(18))
      }
    case 20 =>
      xs match {
        case xs: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple20(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13), xs.productElement(14), xs.productElement(15), xs.productElement(16), xs.productElement(17), xs.productElement(18), xs.productElement(19))
      }
    case 21 =>
      xs match {
        case xs: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple21(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13), xs.productElement(14), xs.productElement(15), xs.productElement(16), xs.productElement(17), xs.productElement(18), xs.productElement(19), xs.productElement(20))
      }
    case 22 =>
      xs match {
        case xs: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] => xs
        case xs => Tuple22(xs.productElement(0), xs.productElement(1), xs.productElement(2), xs.productElement(3), xs.productElement(4), xs.productElement(5), xs.productElement(6), xs.productElement(7), xs.productElement(8), xs.productElement(9), xs.productElement(10), xs.productElement(11), xs.productElement(12), xs.productElement(13), xs.productElement(14), xs.productElement(15), xs.productElement(16), xs.productElement(17), xs.productElement(18), xs.productElement(19), xs.productElement(20), xs.productElement(21))
      }
    case _ =>
      xs match {
        case xs: TupleXXL => xs
        case xs => TupleXXL.fromIArray(xs.productIterator.map(_.asInstanceOf[Object]).toArray.asInstanceOf[IArray[Object]]) // TODO use Iterator.toIArray
      }
  }).asInstanceOf[T]

  // Cons for Tuple1 to Tuple22
  def specialCaseCons[H, This <: Tuple](x: H, self: This): H *: This = {
    val res = (self: Any) match {
      case self: Unit =>
        Tuple1(x)
      case self: Tuple1[_] =>
        Tuple2(x, self._1)
      case self: Tuple2[_, _] =>
        Tuple3(x, self._1, self._2)
      case self: Tuple3[_, _, _] =>
        Tuple4(x, self._1, self._2, self._3)
      case self: Tuple4[_, _, _, _] =>
        Tuple5(x, self._1, self._2, self._3, self._4)
      case self: Tuple5[_, _, _, _, _] =>
        Tuple6(x, self._1, self._2, self._3, self._4, self._5)
      case self: Tuple6[_, _, _, _, _, _] =>
        Tuple7(x, self._1, self._2, self._3, self._4, self._5, self._6)
      case self: Tuple7[_, _, _, _, _, _, _] =>
        Tuple8(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7)
      case self: Tuple8[_, _, _, _, _, _, _, _] =>
        Tuple9(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8)
      case self: Tuple9[_, _, _, _, _, _, _, _, _] =>
        Tuple10(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9)
      case self: Tuple10[_, _, _, _, _, _, _, _, _, _] =>
        Tuple11(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10)
      case self: Tuple11[_, _, _, _, _, _, _, _, _, _, _] =>
        Tuple12(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11)
      case self: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple13(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12)
      case self: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple14(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13)
      case self: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple15(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14)
      case self: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple16(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15)
      case self: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple17(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16)
      case self: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple18(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17)
      case self: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple19(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18)
      case self: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple20(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18, self._19)
      case self: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple21(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18, self._19, self._20)
      case self: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple22(x, self._1, self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18, self._19, self._20, self._21)
      case self: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        val arr: Array[Object] = Array(
          x.asInstanceOf[Object], self._1.asInstanceOf[Object], self._2.asInstanceOf[Object],
          self._3.asInstanceOf[Object], self._4.asInstanceOf[Object], self._5.asInstanceOf[Object],
          self._6.asInstanceOf[Object], self._7.asInstanceOf[Object], self._8.asInstanceOf[Object],
          self._9.asInstanceOf[Object], self._10.asInstanceOf[Object], self._11.asInstanceOf[Object],
          self._12.asInstanceOf[Object], self._13.asInstanceOf[Object], self._14.asInstanceOf[Object],
          self._15.asInstanceOf[Object], self._16.asInstanceOf[Object], self._17.asInstanceOf[Object],
          self._18.asInstanceOf[Object], self._19.asInstanceOf[Object], self._20.asInstanceOf[Object],
          self._21.asInstanceOf[Object], self._22.asInstanceOf[Object],
        )
        TupleXXL.fromIArray(arr.asInstanceOf[IArray[Object]])
    }
    res.asInstanceOf[H *: This]
  }

  // Cons for TupleXXL
  def xxlCons[H, This <: Tuple](x: H, xxl: TupleXXL): H *: This = {
    val arr = new Array[Object](xxl.productArity + 1)
    System.arraycopy(xxl.elems, 0, arr, 1, xxl.productArity)
    arr(0) = x.asInstanceOf[Object]
    TupleXXL.fromIArray(arr.asInstanceOf[IArray[Object]]).asInstanceOf[H *: This]
  }

  def dynamicCons[H, This <: Tuple](x: H, self: This): H *: This = (self: Any) match {
    case xxl: TupleXXL => xxlCons(x, xxl)
    case _ => specialCaseCons(x, self)
  }

  def dynamicConcat[This <: Tuple, That <: Tuple](self: This, that: That): Concat[This, That] = {
    type Result = Concat[This, That]

    // If one of the tuples is empty, we can leave early
    (self: Any) match {
      case self: Unit => return that.asInstanceOf[Result]
      case _ =>
    }

    (that: Any) match {
      case that: Unit => return self.asInstanceOf[Result]
      case _ =>
    }

    val arr = new Array[Object](self.size + that.size)

    // Copies the tuple to an array, at the given offset
    inline def copyToArray[T <: Tuple](tuple: T, array: Array[Object], offset: Int): Unit = (tuple: Any) match {
      case xxl: TupleXXL =>
        System.arraycopy(xxl.elems, 0, array, offset, tuple.size)
      case _ =>
        tuple.asInstanceOf[Product].productIterator.asInstanceOf[Iterator[Object]]
          .copyToArray(array, offset, tuple.size)
    }

    // In the general case, we copy the two tuples to an array, and convert it back to a tuple
    copyToArray(self, arr, 0)
    copyToArray(that, arr, self.size)
    dynamicFromIArray[Result](arr.asInstanceOf[IArray[Object]])
  }

  def dynamicSize[This <: Tuple](self: This): Size[This] = (self: Any) match {
    case self: Unit => 0.asInstanceOf[Size[This]]
    case self: Product => self.productArity.asInstanceOf[Size[This]]
  }

  // Tail for Tuple1 to Tuple22
  def specialCaseTail[This <: NonEmptyTuple] (self: This): Tail[This] = {
    val res = (self: Any) match {
      case self: Tuple1[_] =>
        ()
      case self: Tuple2[_, _] =>
        Tuple1(self._2)
      case self: Tuple3[_, _, _] =>
        Tuple2(self._2, self._3)
      case self: Tuple4[_, _, _, _] =>
        Tuple3(self._2, self._3, self._4)
      case self: Tuple5[_, _, _, _, _] =>
        Tuple4(self._2, self._3, self._4, self._5)
      case self: Tuple6[_, _, _, _, _, _] =>
        Tuple5(self._2, self._3, self._4, self._5, self._6)
      case self: Tuple7[_, _, _, _, _, _, _] =>
        Tuple6(self._2, self._3, self._4, self._5, self._6, self._7)
      case self: Tuple8[_, _, _, _, _, _, _, _] =>
        Tuple7(self._2, self._3, self._4, self._5, self._6, self._7, self._8)
      case self: Tuple9[_, _, _, _, _, _, _, _, _] =>
        Tuple8(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9)
      case self: Tuple10[_, _, _, _, _, _, _, _, _, _] =>
        Tuple9(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10)
      case self: Tuple11[_, _, _, _, _, _, _, _, _, _, _] =>
        Tuple10(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11)
      case self: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple11(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12)
      case self: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple12(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13)
      case self: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple13(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14)
      case self: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple14(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15)
      case self: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple15(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16)
      case self: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple16(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17)
      case self: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple17(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18)
      case self: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple18(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18, self._19)
      case self: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple19(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18, self._19, self._20)
      case self: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple20(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18, self._19, self._20, self._21)
      case self: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple21(self._2, self._3, self._4, self._5, self._6, self._7, self._8, self._9, self._10, self._11, self._12, self._13, self._14, self._15, self._16, self._17, self._18, self._19, self._20, self._21, self._22)
    }
    res.asInstanceOf[Tail[This]]
  }

  // Tail for TupleXXL
  def xxlTail[This <: NonEmptyTuple](xxl: TupleXXL): Tail[This] = {
    if (xxl.productArity == 23) {
      val elems = xxl.elems
      Tuple22(
        elems(1), elems(2), elems(3), elems(4), elems(5), elems(6), elems(7),
        elems(8), elems(9), elems(10), elems(11), elems(12), elems(13), elems(14),
        elems(15), elems(16), elems(17), elems(18), elems(19), elems(20),
        elems(21), elems(22)
      ).asInstanceOf[Tail[This]]
    } else {
      val arr = new Array[Object](xxl.elems.length - 1)
      System.arraycopy(xxl.elems, 1, arr, 0, xxl.elems.length - 1)
      TupleXXL.fromIArray(arr.asInstanceOf[IArray[Object]]).asInstanceOf[Tail[This]]
    }
  }

  def dynamicTail[This <: NonEmptyTuple](self: This): Tail[This] = (self: Any) match {
    case xxl: TupleXXL => xxlTail(xxl)
    case _ => specialCaseTail(self)
  }

  def dynamicApply[This <: NonEmptyTuple, N <: Int] (self: This, n: N): Elem[This, N] = {
    type Result = Elem[This, N]
    val res = (self: Any) match {
      case self: Unit => throw new IndexOutOfBoundsException(n.toString)
      case self: Product => self.productElement(n)
    }
    res.asInstanceOf[Result]
  }

  // Benchmarks showed that this is faster than doing (it1 zip it2).copyToArray(...)
  def zipIterators(it1: Iterator[Any], it2: Iterator[Any], size: Int): IArray[Object] = {
    val arr = new Array[Object](size)
    var i = 0
    while (i < size) {
      arr(i) = (it1.next(), it2.next())
      i += 1
    }
    arr.asInstanceOf[IArray[Object]]
  }

  def dynamicZip[This <: Tuple, T2 <: Tuple](t1: This, t2: T2): Zip[This, T2] = {
    if (t1.size == 0 || t2.size == 0) return ().asInstanceOf[Zip[This, T2]]
    val size = Math.min(t1.size, t2.size)
    Tuple.fromIArray(
      zipIterators(
        t1.asInstanceOf[Product].productIterator,
        t2.asInstanceOf[Product].productIterator,
        size
      )
    ).asInstanceOf[Zip[This, T2]]
  }

  // Map for Tuple1 to Tuple22
  def specialCaseMap[This <: Tuple, F[_]](self: This, f: [t] => t => F[t]): Map[This, F] = {
    type Result = Map[This, F]
    val res = (self: Any) match {
      case sekf: Unit =>
        ()
      case self: Tuple1[_] =>
        Tuple1(f[Object](self._1.asInstanceOf[Object]))
      case self: Tuple2[_, _] =>
        Tuple2(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]))
      case self: Tuple3[_, _, _] =>
        Tuple3(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]))
      case self: Tuple4[_, _, _, _] =>
        Tuple4(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]))
      case self: Tuple5[_, _, _, _, _] =>
        Tuple5(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]))
      case self: Tuple6[_, _, _, _, _, _] =>
        Tuple6(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]))
      case self: Tuple7[_, _, _, _, _, _, _] =>
        Tuple7(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]))
      case self: Tuple8[_, _, _, _, _, _, _, _] =>
        Tuple8(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]))
      case self: Tuple9[_, _, _, _, _, _, _, _, _] =>
        Tuple9(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]))
      case self: Tuple10[_, _, _, _, _, _, _, _, _, _] =>
        Tuple10(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]))
      case self: Tuple11[_, _, _, _, _, _, _, _, _, _, _] =>
        Tuple11(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]))
      case self: Tuple12[_, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple12(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]))
      case self: Tuple13[_, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple13(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]))
      case self: Tuple14[_, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple14(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]))
      case self: Tuple15[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple15(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]), f[Object](self._15.asInstanceOf[Object]))
      case self: Tuple16[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple16(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]), f[Object](self._15.asInstanceOf[Object]), f[Object](self._16.asInstanceOf[Object]))
      case self: Tuple17[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple17(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]), f[Object](self._15.asInstanceOf[Object]), f[Object](self._16.asInstanceOf[Object]), f[Object](self._17.asInstanceOf[Object]))
      case self: Tuple18[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple18(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]), f[Object](self._15.asInstanceOf[Object]), f[Object](self._16.asInstanceOf[Object]), f[Object](self._17.asInstanceOf[Object]), f[Object](self._18.asInstanceOf[Object]))
      case self: Tuple19[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple19(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]), f[Object](self._15.asInstanceOf[Object]), f[Object](self._16.asInstanceOf[Object]), f[Object](self._17.asInstanceOf[Object]), f[Object](self._18.asInstanceOf[Object]), f[Object](self._19.asInstanceOf[Object]))
      case self: Tuple20[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple20(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]), f[Object](self._15.asInstanceOf[Object]), f[Object](self._16.asInstanceOf[Object]), f[Object](self._17.asInstanceOf[Object]), f[Object](self._18.asInstanceOf[Object]), f[Object](self._19.asInstanceOf[Object]), f[Object](self._20.asInstanceOf[Object]))
      case self: Tuple21[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple21(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]), f[Object](self._15.asInstanceOf[Object]), f[Object](self._16.asInstanceOf[Object]), f[Object](self._17.asInstanceOf[Object]), f[Object](self._18.asInstanceOf[Object]), f[Object](self._19.asInstanceOf[Object]), f[Object](self._20.asInstanceOf[Object]), f[Object](self._21.asInstanceOf[Object]))
      case self: Tuple22[_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _] =>
        Tuple22(f[Object](self._1.asInstanceOf[Object]), f[Object](self._2.asInstanceOf[Object]), f[Object](self._3.asInstanceOf[Object]), f[Object](self._4.asInstanceOf[Object]), f[Object](self._5.asInstanceOf[Object]), f[Object](self._6.asInstanceOf[Object]), f[Object](self._7.asInstanceOf[Object]), f[Object](self._8.asInstanceOf[Object]), f[Object](self._9.asInstanceOf[Object]), f[Object](self._10.asInstanceOf[Object]), f[Object](self._11.asInstanceOf[Object]), f[Object](self._12.asInstanceOf[Object]), f[Object](self._13.asInstanceOf[Object]), f[Object](self._14.asInstanceOf[Object]), f[Object](self._15.asInstanceOf[Object]), f[Object](self._16.asInstanceOf[Object]), f[Object](self._17.asInstanceOf[Object]), f[Object](self._18.asInstanceOf[Object]), f[Object](self._19.asInstanceOf[Object]), f[Object](self._20.asInstanceOf[Object]), f[Object](self._21.asInstanceOf[Object]), f[Object](self._22.asInstanceOf[Object]))
    }
    res.asInstanceOf[Result]
  }

  def dynamicMap[This <: Tuple, F[_]](self: This, f: [t] => t => F[t]): Map[This, F] = (self: Any) match {
    case self: Unit => ().asInstanceOf[Map[This, F]]
    case _ =>
      Tuple.fromArray(self.asInstanceOf[Product].productIterator.map(f(_)).toArray) // TODO use toIArray of Object to avoid double/triple array copy
        .asInstanceOf[Map[This, F]]
  }

  def dynamicTake[This <: Tuple, N <: Int](self: This, n: N): Take[This, N] = {
    if (n < 0) throw new IndexOutOfBoundsException(n.toString)
    val actualN = Math.min(n, self.size)

    type Result = Take[This, N]

    if (actualN == 0) ().asInstanceOf[Result]
    else {
      val arr = (self: Any) match {
        case xxl: TupleXXL =>
          xxl.elems.asInstanceOf[Array[Object]].take(actualN)
        case _ =>
          val arr = new Array[Object](actualN)
          self.asInstanceOf[Product].productIterator.asInstanceOf[Iterator[Object]]
            .copyToArray(arr, 0, actualN)
          arr
      }

      dynamicFromIArray(arr.asInstanceOf[IArray[Object]]).asInstanceOf[Result]
    }
  }

  def dynamicDrop[This <: Tuple, N <: Int](self: This, n: N): Drop[This, N] = {
    if (n < 0) throw new IndexOutOfBoundsException(n.toString)
    val size = self.size
    val actualN = Math.min(n, size)
    val rem = size - actualN

    type Result = Drop[This, N]

    if (rem == 0) ().asInstanceOf[Result]
    else {
      val arr = (self: Any) match {
        case xxl: TupleXXL =>
          xxl.elems.asInstanceOf[Array[Object]].drop(actualN)
        case _ =>
          val arr = new Array[Object](rem)
          self.asInstanceOf[Product].productIterator.asInstanceOf[Iterator[Object]]
            .drop(actualN).copyToArray(arr, 0, rem)
          arr
      }

      dynamicFromIArray(arr.asInstanceOf[IArray[Object]]).asInstanceOf[Result]
    }
  }

  def dynamicSplitAt[This <: Tuple, N <: Int](self: This, n: N): Split[This, N] = {
    if (n < 0) throw new IndexOutOfBoundsException(n.toString)
    val size = self.size
    val actualN = Math.min(n, size)

    type Result = Split[This, N]

    val (arr1, arr2) = (self: Any) match {
      case () => (Array.empty[Object], Array.empty[Object])
      case xxl: TupleXXL =>
        xxl.elems.asInstanceOf[Array[Object]].splitAt(actualN)
      case _ =>
        val arr1 = new Array[Object](actualN)
        val arr2 = new Array[Object](size - actualN)
        val it = self.asInstanceOf[Product].productIterator.asInstanceOf[Iterator[Object]]
        it.copyToArray(arr1, 0, actualN)
        it.copyToArray(arr2, 0, size - actualN)
        (arr1, arr2)
    }

    (
      dynamicFromIArray(arr1.asInstanceOf[IArray[Object]]),
      dynamicFromIArray(arr2.asInstanceOf[IArray[Object]])
    ).asInstanceOf[Result]
  }

  def consIterator(head: Any, tail: Tuple): Iterator[Any] =
    Iterator.single(head) ++ tail.asInstanceOf[Product].productIterator

  def concatIterator(tup1: Tuple, tup2: Tuple): Iterator[Any] =
    tup1.asInstanceOf[Product].productIterator ++ tup2.asInstanceOf[Product].productIterator
}
