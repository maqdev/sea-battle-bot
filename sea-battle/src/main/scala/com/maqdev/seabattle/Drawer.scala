package com.maqdev.seabattle

import java.awt.{Font, Graphics2D, Color}
import java.awt.geom.{Ellipse2D, Line2D, Rectangle2D}
import java.awt.image.BufferedImage
import java.io.OutputStream
import scala.collection.mutable

trait Drawer {
  def drawBoard(board: SeaBattleGameBoard)
}

class ConsoleBoardDrawer(isMyBoard: Boolean) extends Drawer {

  def drawBoard(board: SeaBattleGameBoard) = {
    val lines = 1 to board.height map { y ⇒
      val line = new Array[Char](board.width)
      0 to (board.width-1) foreach { x ⇒
        line.update(x, ' ')
      }
      line
    }

    val missedShoots = board.shoots.clone()
    board.boats.foreach { boat ⇒
      boat.coords.foreach(missedShoots.remove)
      drawBoat(lines, boat, board.shoots)
    }

    missedShoots.foreach(drawMissedShoot(lines, _))

    lines.zipWithIndex.foreach { line ⇒
      line._1.foreach(print)
      println(" " + (line._2 + 'a'.toInt).toChar)
    }

    0 to (board.width-1) map print
    println
  }

  private def drawBoat(lines: Seq[Array[Char]], boat: Boat, shoots: mutable.Set[Point]) = {
    if (isMyBoard || boat.isDrowned(shoots)) {
      boat.coords.foreach { point ⇒
        if (shoots.contains(point))
          write(lines, point, 'X')
        else
          write(lines, point, '#')
      }
    } else {
      boat.coords.foreach { point ⇒
        if (shoots.contains(point))
          write(lines, point, 'Z')
      }
    }
  }

  private def drawMissedShoot(lines: Seq[Array[Char]], shoot: Point) = {
    write(lines, shoot, '*')
  }

  private def write(lines: Seq[Array[Char]], p: Point, c: Char): Unit = write(lines, p.x, p.y, c)
  private def write(lines: Seq[Array[Char]], x: Int, y: Int, c: Char): Unit = lines(y).update(x, c)
}

class ImageBoardDrawer(width: Int, height: Int, format: String = "png", outputStream: OutputStream, isMyBoard: Boolean) extends Drawer {
  private [ImageBoardDrawer] case class DrawOptions(pointWidth: Double, pointHeight: Double, borderWidth: Double,
                         boardWidth: Double, boardHeight: Double)

  def drawBoard(board: SeaBattleGameBoard) = {
    val borderWidth = 2.0
    val pointWidth = (width-borderWidth*2)/(board.width+1)
    val pointHeight = (width-borderWidth*2)/(board.width+1)
    val drawOptions = DrawOptions(
      pointWidth,
      pointHeight,
      borderWidth,
      board.width * pointWidth,
      board.height * pointHeight
    )

    val canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val g = canvas.createGraphics()
    try {
      if (isMyBoard) {
        g.setColor(Color.WHITE)
      }
      else {
        g.setColor(new Color(0xE0, 0xEC, 0xFF))
      }
      g.fillRect(0, 0, canvas.getWidth, canvas.getHeight)
      g.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
        java.awt.RenderingHints.VALUE_ANTIALIAS_ON)
      g.setColor(Color.BLACK)
      g.draw(new Rectangle2D.Double(0, 0, drawOptions.boardWidth, drawOptions.boardHeight))

      for (x ← 1 to board.width - 1) {
        g.draw(new Line2D.Double(
          x * drawOptions.pointWidth, 0,
          x * drawOptions.pointWidth, drawOptions.boardHeight
        ))
      }

      for (y ← 1 to board.height - 1) {
        g.draw(new Line2D.Double(
          0, y * drawOptions.pointHeight,
          drawOptions.boardWidth, y * drawOptions.pointHeight
        ))
      }

      g.setFont(new Font("Courier", Font.PLAIN, ((pointHeight+pointWidth)/3).toInt))

      for (x ← 1 to board.width) {
        val c = x.toString
        g.drawString(c.toString, ((x-1) * drawOptions.pointWidth + drawOptions.pointWidth/3).toFloat, (height - pointHeight/2).toFloat)
      }
      for (y ← 1 to board.height) {
        val c = ('a' + y - 1).toChar
        g.drawString(c.toString, (width - pointWidth).toFloat, (y * drawOptions.pointHeight - drawOptions.pointHeight/3).toFloat)
      }

      val missedShoots = board.shoots.clone()
      board.boats.foreach { boat ⇒
        boat.coords.foreach(missedShoots.remove)
        drawBoat(g, drawOptions, boat, board)
      }

      missedShoots.foreach(miss ⇒ drawMissedShoot(g, drawOptions, miss, board))
    } finally {
      g.dispose()
    }
    javax.imageio.ImageIO.write(canvas, format, outputStream)
  }

  private def drawBoat(g: Graphics2D, drawOptions: DrawOptions, boat: Boat, board: SeaBattleGameBoard) = {
    if (isMyBoard || boat.isDrowned(board.shoots)) {
      boat.coords.foreach { point ⇒
        if (board.shoots.contains(point))
          drawShotBoatPart(g, drawOptions, point, board)
        else
          drawBoatPart(g, drawOptions, point, board)
      }
    } else {
      boat.coords.foreach { point ⇒
        if (board.shoots.contains(point))
          drawEnemyShotBoatPart(g, drawOptions, point, board)
      }
    }
  }

  private def drawBoatPart(g: Graphics2D, drawOptions: DrawOptions, point: Point, board: SeaBattleGameBoard) = {
    if (isMyBoard)
      g.setColor(Color.BLUE)
    else
      g.setColor(Color.GREEN)
    //g.setStroke()
    g.draw(new Rectangle2D.Double(
      point.x * drawOptions.pointWidth + drawOptions.borderWidth,
      point.y * drawOptions.pointHeight + drawOptions.borderWidth,
      drawOptions.pointWidth - drawOptions.borderWidth*2,
      drawOptions.pointHeight - drawOptions.borderWidth*2
    ))
    g.draw(new Line2D.Double (
      point.x * drawOptions.pointWidth + drawOptions.borderWidth,
      point.y * drawOptions.pointHeight + drawOptions.borderWidth,
      (point.x+1) * drawOptions.pointWidth - drawOptions.borderWidth,
      (point.y+1) * drawOptions.pointHeight - drawOptions.borderWidth
    ))
    g.draw(new Line2D.Double (
      point.x * drawOptions.pointWidth + drawOptions.borderWidth,
      (point.y+1) * drawOptions.pointHeight - drawOptions.borderWidth,
      (point.x+1) * drawOptions.pointWidth - drawOptions.borderWidth,
      point.y * drawOptions.pointHeight + drawOptions.borderWidth
    ))
  }

  private def drawShotBoatPart(g: Graphics2D, drawOptions: DrawOptions, point: Point, board: SeaBattleGameBoard) = {
    drawBoatPart(g, drawOptions, point, board)
    if (board.isLast(point)) {
      g.setColor(Color.RED)
    }
    else {
      g.setColor(Color.ORANGE)
    }
    g.fill(new Ellipse2D.Double (
      point.x * drawOptions.pointWidth + drawOptions.borderWidth*4,
      point.y * drawOptions.pointHeight + drawOptions.borderWidth*4,
      drawOptions.pointWidth - drawOptions.borderWidth*8,
      drawOptions.pointHeight - drawOptions.borderWidth*8
    ))
  }

  private def drawMissedShoot(g: Graphics2D, drawOptions: DrawOptions, point: Point, board: SeaBattleGameBoard) = {
    if (board.isLast(point)) {
      g.setColor(Color.BLACK)
    }
    else {
      g.setColor(Color.GRAY)
    }
    g.fill(new Ellipse2D.Double (
      point.x * drawOptions.pointWidth + drawOptions.borderWidth*4,
      point.y * drawOptions.pointHeight + drawOptions.borderWidth*4,
      drawOptions.pointWidth - drawOptions.borderWidth*8,
      drawOptions.pointHeight - drawOptions.borderWidth*8
    ))
  }

  private def drawEnemyShotBoatPart(g: Graphics2D, drawOptions: DrawOptions, point: Point, board: SeaBattleGameBoard) = {
    if (board.isLast(point)) {
      g.setColor(Color.RED)
    }
    else {
      g.setColor(Color.ORANGE)
    }
    g.fill(new Ellipse2D.Double (
      point.x * drawOptions.pointWidth + drawOptions.borderWidth*4,
      point.y * drawOptions.pointHeight + drawOptions.borderWidth*4,
      drawOptions.pointWidth - drawOptions.borderWidth*8,
      drawOptions.pointHeight - drawOptions.borderWidth*8
    ))
  }
}

