import com.maqdev.seabattle._
import org.scalatest.{Matchers, FreeSpec}
import scala.collection.mutable

class SeaBattleGameSpecification extends FreeSpec with Matchers {
  "SeaBattleGame should be created randomly" in {
    val creator = new SeaBattleGameCreator(10,10,List(1,2,3))
    val game = creator.createBoard
    game.boats.size should equal(3)
  }

  "SeaBattleGame should be serialized" in {
    val game = new SeaBattleGameBoard(10,10, Seq(
      Boat(Seq(Point(1,2), Point(1,3)))
    ), mutable.Set(Point(1,2)))

    import eu.inn.binders.json._
    val gameJson = game.toJson

    val s: String = """{"width":10,"height":10,"boats":[{"coords":[{"x":1,"y":2},{"x":1,"y":3}]}],"shoots":[{"x":1,"y":2}]}"""
    gameJson should equal(s)
  }

  "SeaBattleGame should be deserialized" in {
    val o = new SeaBattleGameBoard(10,10, Seq(
      Boat(Seq(Point(1,2), Point(1,3)))
    ), mutable.Set(Point(1,2)))

    import eu.inn.binders.json._
    val s: String = """
    {"width":10,"height":10,"boats":[{"coords":[{"x":1,"y":2},{"x":1,"y":3}]}],"shoots":[{"x":1,"y":2}]}
    """
    val n = s.parseJson[SeaBattleGameBoard]
    SeaBattleGameBoard.unapply(o) should equal(SeaBattleGameBoard.unapply(n))
  }
}
