import java.io.FileOutputStream

import com.maqdev.seabattle._
import org.scalatest.{FreeSpec, Matchers}

class DrawSpecification extends FreeSpec with Matchers {

   "SeaBattleGame should be created randomly" in {
     val creator = new SeaBattleGameCreator(9,9,List(1,2,3))
     val board = creator.createBoard

     board.shoot(Point(0,0))
     board.shoot(Point(1,1))
     board.shoot(Point(2,2))
     board.shoot(Point(3,3))

     val out = new FileOutputStream("/tmp/test-sea-battle-drawer.png")
     val drawer = new ImageBoardDrawer(300,300,"png",out,isMyBoard = true)
     drawer.drawBoard(board)
     out.close()

     val out2 = new FileOutputStream("/tmp/test-sea-battle-drawer-enemy.png")
     val drawer2 = new ImageBoardDrawer(300,300,"png",out2,isMyBoard = false)
     drawer2.drawBoard(board)
     out2.close()
   }
 }
