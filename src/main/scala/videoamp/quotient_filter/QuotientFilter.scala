package com.videoamp.quotient_filter

object Command {
  case class Query(data: String)
  case class Insert(data: String)
}

class QuotientFilter() {
  /**
   *
   */
  val quotientSize = 16
  val metadataSize = 6
  val multiplicitySize = 3
  val qf:Vector[collection.mutable.BitSet] = createSlots(math.pow(2, quotientSize).toInt)
  val multiplicityBits = {
    val mbits = collection.mutable.BitSet()
    for (a <- 1 to multiplicitySize)
    {
        mbits += (2 + a)
    }
    mbits
  }
  val clearBits = (collection.mutable.BitSet(1,2) &= multiplicityBits)
  def createSlots(p: Int): Vector[collection.mutable.BitSet] = {
    var slots = Vector[collection.mutable.BitSet]()
    for (a <- 1 to p)
    {
      slots = slots :+ collection.mutable.BitSet()
    }
    return slots
  }

  def isMember(value: String): Int ={
    val (quotient, remainder) = hash(value)
    val isOccupied = qf(quotient)(0)
    val isShifted = qf(quotient)(2)
    println(s"isOccupied $isOccupied isShifted $isShifted")
    (isOccupied, isShifted) match {
      case (false, _) =>
        return 0
      case (true, false) =>
        val (position, multiplicity, iterations) = findPositionInRun(quotient, remainder)
        return multiplicity
      case (true, true) =>
        val (clusterStart, totalRuns) = findClusterStart(quotient, 0)
        val runStart = findRunStart(clusterStart, totalRuns)
        val (position, multiplicity, iterations) = findPositionInRun(runStart, remainder)
        return multiplicity
    }
  }
  def insert(value: String): Int = {
    val (quotient, remainder) = hash(value)
    val isOccupied = qf(quotient)(0)
    val isShifted = qf(quotient)(2)

    var isExisting: Boolean = true
    var positionInRun: Int = 0
    var insertPosition: Int = 0
    var isNewRun: Boolean = !isOccupied
    println(s"isOccupied $isOccupied isShifted $isShifted")
    (isOccupied, isShifted) match {
      case (false, false) =>
        insertPosition = quotient
        positionInRun = 0
        isExisting = false
      case (true, false) =>
        val (position, multiplicity, iterations) = findPositionInRun(quotient, remainder)
        insertPosition = position
        isExisting = (multiplicity > 0)
        positionInRun = iterations
      case (true, true) =>
        val (clusterStart, totalRuns) = findClusterStart(quotient, 0)
        val runStart = findRunStart(clusterStart, totalRuns)
        val (position, multiplicity, iterations) = findPositionInRun(runStart, remainder)
        insertPosition = position
        isExisting = (multiplicity > 0)
        positionInRun = iterations
      case (false, true) =>
        val (clusterStart, totalRuns) = findClusterStart(quotient, 0)
        val runStart = findRunStart(clusterStart, totalRuns)
        insertPosition = runStart
        positionInRun = 0
        isExisting = false
    }
    println(s"insertPosition $insertPosition, isExisting $isExisting, positionInRun $positionInRun")
    isExisting match {
      case true =>
        return insertMultiple(insertPosition)
      case false =>
        return insertFirst(insertPosition, quotient, remainder)
    }
  }

  protected def clearSlotRemainder(position:Int): Int = {
    qf(position) &= clearBits
    return 1
  }

  protected def getSlotRemainder(position:Int): Int = {
    val remainder = (qf(position).toBitMask(0) >> this.metadataSize << this.metadataSize).toInt
    return remainder
  }

  protected def insertFirst(insertPosition: Int, quotient: Int, remainder: Int): Int = {
    // if new element shift everything and then add it
    shiftCluster(insertPosition)
    clearSlotRemainder(insertPosition)
    !qf(quotient)(0) match {
      case true =>
        // if next element has been shifted, put it as a continuation and is not start of new run
        if (qf(insertPosition + 1)(2)) {
          qf(insertPosition + 1)(1) = true
        }
      case false =>
    }
    // set isOccupied
    qf(quotient)(0) = true
    (quotient == insertPosition) match {
      case true =>
      case false =>
        qf(insertPosition)(2) = true
    }
    // set the multiple
    qf(insertPosition)(3) = true
    qf(insertPosition) |= collection.mutable.BitSet.fromBitMask(Array(remainder))
    return 1
  }

  protected def insertMultiple(slot: Int): Int = {
    val multiplicity = getMultiplicity(qf(slot))
    println(s"inserting multiplicty previous was $multiplicity")
    if (multiplicity >=  (math.pow(2,multiplicitySize) - 1) )
    {
      // max multiplicity is 7
      return multiplicity
    }
    return setMultiplicity(slot, multiplicity + 1)
  }

  protected def shiftCluster(slot: Int): Boolean = {
  // Shift all slots to the right of the current slot until an empty slot is found
  // This method is poor. Switch to having a map with an intermediary holder.
    println("shifting cluster")
    if (!qf(slot)(0) & !qf(slot)(1) & !qf(slot)(2))
    {
      println("Empty slot")
      return true
    }
    val nextSlotValue = qf(slot+1)
    (nextSlotValue(0), nextSlotValue(1), nextSlotValue(2)) match {
      case (false, false, false) =>
      case _ =>
        println(nextSlotValue(0))
        println(nextSlotValue(1))
        println(nextSlotValue(2))
        shiftCluster(slot+1)
    }
    qf(slot+1) |= qf(slot)
    qf(slot+1) += 2
    clearSlotRemainder(slot)

    return true
  }

  protected def findClusterStart(slot: Int, totalRuns: Int = 0):(Int,Int) = {
    /**
     * Count to the left of the canonical slot until you find the
     * cluster start.
     *
     * Return the cluster start and total number of runs before reaching
     * the run of the current value
     */
    println("find cluster start")
    val current = qf(slot)
    val macros = (current(0), current(1), current(2))
    macros match {
      case (true, false, false) =>
        return (slot, totalRuns)
      case (true, _, true) =>
        findClusterStart(slot-1, totalRuns + 1)
      case (_, _, true) =>
        findClusterStart(slot-1, totalRuns)
      case (_, _, _) =>
        return (0, 0)
        // error
    }
  }

  protected def findRunStart(slot: Int, totalRuns: Int): (Int) = {
    /*
     * Keep moving right until you've passed enough run starts
     * to the start of the run we're looking for
     */
    println("find run start")
    val isContinuation  = qf(slot)(1)
    totalRuns match {
      case 0 =>
        return slot
      case _ =>
    }
    isContinuation match {
      case false =>
        findRunStart(slot+1, totalRuns-1)
      case true =>
        findRunStart(slot+1, totalRuns)
    }
  }

  protected def findPositionInRun(slot: Int, remainder:Int, iterations:Int = 0): (Int, Int, Int) = {
    /**
     * Assuming the correct run has been found, look for the correct position
     * that the remainder should be in assuming it exists.
     *
     * Return a tuple consisting of location where it should be
     * the current multiplicity, the total number of iterations it's
     * gone through.
     */
    println("find position in run")
    // remove metadata bits to compare remainders
    val difference = remainder - getSlotRemainder(slot)
    (qf(slot)(2), iterations) match {
      // if reach start of new cluster, return this location
      case (false, x:Int) if (x > 0) =>
        println("found start of new cluster")
        return(slot, 0, iterations)
      case (false, x:Int) if (x == 0) =>
      case (true, _) =>
    }
    println(s"difference $difference")
    difference match {
      case x:Int if (x < 0) =>
        // these 3 bits represent the multiplicity value
        val multiplicity = getMultiplicity(qf(slot))
        return (slot, 0, iterations)
      case x:Int if (x == 0) =>
        // these 3 bits represent the multiplicity value
        val multiplicity = getMultiplicity(qf(slot))
        return (slot, multiplicity, iterations)
      case x:Int if (x > 0) =>
        return findPositionInRun(slot+1, remainder, iterations + 1)
    }
  }

  protected def getMultiplicity(value: collection.mutable.BitSet): Int = {
    /*
     * Return the multiplicity value from the metabits in the slot
     */
      val multiplicity = (((value & multiplicityBits).toBitMask(0) / 8)).toInt
      println(s"getmultiplicity $multiplicity")
      return multiplicity
  }

  protected def clearMultiplicity(slot: Int): Int = {
      // clear the multiplicity bits
      qf(slot) &~= multiplicityBits
      return 0
  }


  protected def setMultiplicity(slot: Int, multiplicity: Int): Int = {
    /*
     * Set the multiplicity metabits in the given slot
     */
      val multiplicityBitSet= collection.mutable.BitSet.fromBitMask(Array(multiplicity << multiplicitySize))
      clearMultiplicity(slot)
      qf(slot) |= multiplicityBitSet
      val currentMultiplicity = getMultiplicity(qf(slot))
      println(s"attempted $multiplicity")
      println(s"setmultiplicity to $currentMultiplicity")
      return multiplicity
  }

  def hash(value: String):(Int, Int) = {
    /*
     *  Assumes input is UUID1.
     */
    // this hash function works only for uuid1s
    val hash = java.lang.Long.parseLong(value.substring(0,8), 16)
    // get first 16 bits as quotient
    val quotient = (hash >> quotientSize).toInt
    // get last 16 bits as remainder. Shift left 6 bits to make room
    // for metadata bits
    val remainder = (((hash >> quotientSize << quotientSize) ^ hash) << metadataSize).toInt
    // use last 16 bits of first section of uuid1
    // last 6 digits as remainder
    println(s"Quotient $quotient Remainder $remainder")
    return (quotient, remainder)
  }
}
