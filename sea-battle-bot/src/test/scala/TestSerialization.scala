import com.maqdev.telegram.MessageUpdate
import eu.inn.binders.dynamic.DefaultValueSerializerFactory
import eu.inn.binders.naming.CamelCaseToSnakeCaseConverter
import org.scalatest.{Matchers, FreeSpec}
import org.scalatest.concurrent.ScalaFutures

class TestSerialization extends FreeSpec with ScalaFutures with Matchers {
  "BotApi Serialization " - {
    "Update Message " in {
      val str ="""{"update_id":590079449,
"message":{"message_id":13,"from":{"id":2627258,"first_name":"Maga","last_name":"Abdurakhmanov","username":"maqdev"},"chat":{"id":2627258,"first_name":"Maga","last_name":"Abdurakhmanov","username":"maqdev"},"date":1437444922,"text":"kokok"}}"""
      import eu.inn.binders.json._
      implicit val factory = new DefaultSerializerFactory[CamelCaseToSnakeCaseConverter]
      implicit val defaultSerializerFactory = new DefaultValueSerializerFactory[CamelCaseToSnakeCaseConverter]
      val update = str.parseJson[MessageUpdate]
      //println(update)
    }
  }
}