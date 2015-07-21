package com.maqdev.seabattle

import scala.collection.mutable
import scala.util.Random

class SeaBattleGameException extends RuntimeException
//class CantShootSelfException extends SeaBattleGameException
class CantPlaceBoats extends RuntimeException

class SeaBattleGameBoard(val width: Int,
                    val height: Int,
                    val boats: Seq[Boat],
                    val shoots: mutable.Set[Point]) {
  val random = new Random(System.currentTimeMillis)
  private [this] var _lastShoot: Option[Point] = None

  def lastShoot: Option[Point] = _lastShoot
  def isLast(point: Point): Boolean = lastShoot.map{ _ == point} getOrElse { false }

  def shoot(point: Point): Boolean = {
    if (!shoots.contains(point)) {
      _lastShoot = Some(point)
      shoots += point
      boats.exists(_.coords.exists(_ == point))
    }
    else
      false
  }

  def AIshoot: Point = {
    boats.find(_.isShot(shoots)).flatMap { boat: Boat ⇒
      val boatShootCoords = boat.coords.collect{
        case c if shoots.contains(c) ⇒ c
      }
      //println("bbb" + boatShootCoords)
      if (boatShootCoords.size == 1) {
        boatShootCoords.head.pointsVertHorz.filter(_.insideRange(width,height)).find(!shoots.contains(_))
      } else {
        val head = boatShootCoords.head
        val (xdir, ydir) = ( boatShootCoords.reverse.head.x - head.x, boatShootCoords.reverse.head.y - head.y)
        val point1 = head.copy(advance(head.x, xdir, 1), advance(head.y, ydir, 1))
        val point2 = head.copy(advance(head.x, xdir, -1), advance(head.y, ydir, -1))
        //println(s"xdir = $xdir, ydir = $ydir, point1 = $point1, point2 = $point2")
        if (!point1.insideRange(width,height) || shoots.contains(point1)) {
          if (!point2.insideRange(width,height) || shoots.contains(point2)) {
            None
          }
          else {
            Some(point2)
          }
        }
        else {
          Some(point1)
        }
      }
    } getOrElse {
      (0 to 3) map { _ ⇒
        Point(random.nextInt(width), random.nextInt(height))
      } collectFirst {
        case point if !shoots.contains(point) ⇒ point
      } getOrElse {
        val p: Point =
          (0 to (width-1) flatMap { x: Int ⇒
            0 to (height-1) flatMap { y: Int ⇒
              val point = Point(x,y)
              if (!shoots.contains(point))
                Some(point)
              else
                None
            }
          }).head
        p
      }
    }
  }

  def complete: Boolean = {
    boats.forall{ boat ⇒
      boat.coords.forall(shoots.contains)
    }
  }

  private def advance(a: Int, dir: Int, advanceFor: Int) = if (dir != 0) {
    if ((dir > 0 && advanceFor > 0) || (dir < 0 && advanceFor < 0))
      a + dir + advanceFor
    else
      a + advanceFor
  } else {
    a
  }
}

object SeaBattleGameBoard {
  def apply(width: Int, height: Int, boats: Seq[Boat], shoots: Seq[Point]) =
    new SeaBattleGameBoard(width,height, boats, mutable.Set[Point](shoots: _*))

  def unapply(a: SeaBattleGameBoard) = Some((a.width, a.height, a.boats, a.shoots.toSeq: Seq[Point]))
}


case class Point(x: Int, y: Int) {
  def pointsAround: Seq[Point] = -1 to +1 flatMap { dx ⇒
    -1 to +1 map { dy ⇒
      this.copy(x = this.x + dx, y = this.y + dy)
    }
  }

  def pointsVertHorz: Seq[Point] = Seq(Point(x-1,y),Point(x+1,y),Point(x,y-1),Point(x,y+1))
  def insideRange(width: Int, height:Int) = x >= 0 && x < width && y >= 0 && y < height
  override def toString = (y + 'a'.toInt).toChar + x.toString
}
case class Boat(coords: Seq[Point]) {
  def isShot(shoots: mutable.Set[Point]) = coords.exists(shoots.contains) && ! isDrowned(shoots)
  def isDrowned(shoots: mutable.Set[Point]) = coords.forall(shoots.contains)
}

case class SeaBattleGameCreator(width: Int, height: Int, boatSizes: Seq[Int]) {
  val max_tries = 30
  val random = new Random(System.currentTimeMillis)
  def createBoard: SeaBattleGameBoard = {

    val existingBoatPoints = mutable.Set[Point]()

    val boats = boatSizes map { boatSize ⇒
      val boat = 0 to max_tries map { _ ⇒
        randomBoad(boatSize)
      } collectFirst {
        case randomBoat if randomBoat.coords.forall(c ⇒ canPlaceBoatPart(c, existingBoatPoints)) ⇒ randomBoat
      } getOrElse {
        throw new CantPlaceBoats
      }
      existingBoatPoints ++= boat.coords
      boat
    }

    new SeaBattleGameBoard(width, height, boats, mutable.Set[Point]())
  }

  private def headBoatPoint(existingBoatPoints: mutable.Set[Point]) = new PartialFunction[Any,Point] {
    val point = Point(random.nextInt(width), random.nextInt(height))
    override def isDefinedAt(x: Any): Boolean = canPlaceBoatPart(point, existingBoatPoints)
    override def apply(v1: Any): Point = point
  }

  private def canPlaceBoatPart(point: Point, existingBoatPoints: mutable.Set[Point]): Boolean = {
    !point.pointsAround.exists(existingBoatPoints.contains)
  }

  private def randomBoad(boatSize: Int): Boat = {
    val headPoint: Point = Point(random.nextInt(width), random.nextInt(height))

    val (xdir, ydir) = {
      if (random.nextBoolean()) {
        (if (headPoint.x < width / 2) 1 else -1, 0)
      }
      else {
        (0, if (headPoint.y < height / 2) 1 else -1)
      }
    }

    val otherPoints: Seq[Point] = 1 to boatSize-1 map {
      delta ⇒ Point(headPoint.x + xdir * delta, headPoint.y + ydir * delta)
    }

    Boat(Seq(headPoint) ++ otherPoints)
  }
}
