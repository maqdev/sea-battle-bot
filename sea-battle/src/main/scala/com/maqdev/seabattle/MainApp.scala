package com.maqdev.seabattle

import scala.io.StdIn
import scala.util.control.Breaks._

object MainApp extends App {

  println("Hello my friend! Lets play?")

  val height = 10
  val width = 10
  val creator = new SeaBattleGameCreator(width, height, List(3,3,3,3))
  val myBoard = creator.createBoard
  val enemyBoard = creator.createBoard
  val myBoardDrawer = new ConsoleBoardDrawer(true)
  val enemyBoardDrawer = new ConsoleBoardDrawer(false)
  val coordsRegex = """^([a-z]+)([0-9]+)$""".r

  breakable {
    printBoards()
    printHelp()
    for (cmd ← inputIterator()) {
      cmd match {
        case "quit" ⇒ break()
        case coordsRegex(y,x) ⇒ {
          val shootPoint = Point(x.toInt, y(0).toInt - 'a'.toInt)
          if (shootPoint.x < 0 || shootPoint.x >= enemyBoard.width ||
            shootPoint.y < 0 || shootPoint.y >= enemyBoard.height ) {
            println("Coordinates are wrong!")
          } else {
            if (enemyBoard.shoot(shootPoint))
              println("You hit!")

            if (enemyBoard.complete) {
              printBoards()
              println("TADA! You Won!")
              break()
            }
            else {
              val enemyShoot = myBoard.AIshoot
              println(s"Enemy hit to $enemyShoot")
              if (myBoard.shoot(enemyShoot))
                println("You shot :-(")
              if (myBoard.complete) {
                printBoards()
                println("GAME OVER!")
                break()
              } else {
                printBoards()
              }
            }
          }
        }
        case _ ⇒ printHelp()
      }
    }
  }

  def printBoards(): Unit = {
    1 to width foreach { _ ⇒ print('-') }
    println()
    println("Your boats:")
    myBoardDrawer.drawBoard(myBoard)
    1 to width foreach { _ ⇒ print('-') }
    println()
    println("Enemy boats:")
    enemyBoardDrawer.drawBoard(enemyBoard)
    1 to width foreach { _ ⇒ print('-') }
    println()
  }
  def printHelp(): Unit = {
    println("Print coords eg 'a1' or 'quit'")
  }
  def inputIterator(): Iterator[String] = new Iterator[String] {
    var eof = false
    override def hasNext: Boolean = !eof
    override def next(): String = {
      val s = StdIn.readLine()
      if (s == null) {
        eof = true
        null
      } else {
        s.trim
      }
    }
  }

  def write(o: Any) = print(o)
  def writeln(o: Any) = println(o)
  def writeln() = println()
}
