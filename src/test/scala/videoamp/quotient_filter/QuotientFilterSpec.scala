package com.videoamp.quotient_filter
import org.scalatest._

class QuotientFilterSpec extends FlatSpec with Matchers{
  "QuotientFilter" should "take values and return multiplicity values when queried to a max of 7" in {
    val quotientFilter = new QuotientFilter()
    val insertValues = Vector(
      "8b1e238a243411e5bd2c0ae748cfd56b",
      "a1d48cba-24dc-11e5-a5b6-0ae748cfd56b",
      "a10cbea6-24dc-11e5-a5b6-0ae748cfd56b"
    )
    for (value <- insertValues) {
      for (i <- 1 until 7) {
        quotientFilter.insert(value) should be (i)
        quotientFilter.isMember(value) should be (i)
      }
      for (i <-1 until 10) {
        quotientFilter.insert(value) should be (7)
        quotientFilter.isMember(value) should be (7)
      }
    }
  }
}
