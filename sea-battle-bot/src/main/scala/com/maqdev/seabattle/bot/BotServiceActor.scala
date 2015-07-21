package com.maqdev.seabattle.bot

import akka.actor.{Props, ActorRef, Actor}
import akka.event.Logging
import com.maqdev.telegram.{User, GroupChat, MessageUpdate, BotApi}
import spray.http.MediaTypes._
import spray.http._
import spray.routing._
import scala.collection.concurrent.TrieMap
import scala.collection.mutable

class BotServiceActor extends Actor with HttpService {
  import com.typesafe.config._
  val conf = ConfigFactory.load()
  val token = conf.getString("token")
  val log = Logging(context.system, this)
  val telegramToken = conf.getString("telegram-token")
  val myUrl = conf.getString("my-url")
  val botApi = new BotApi(telegramToken, context.system)
  botApi.setWebhook(myUrl + token)

  val gameActors = TrieMap[Either[User, GroupChat], ActorRef]()

  def actorRefFactory = context
  def receive = runRoute(myRoute)

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          complete {
            <html>Yo!</html>
          }
        }
      }
    } ~
      path("sea-battle" / "webhook" / RestPath ) { token ⇒
        post {
          entity(as[String]) {
            data ⇒
              log.info(s"=> $data")
              if (token.toString() != this.token) {
                complete {
                  HttpResponse(StatusCodes.Unauthorized, HttpEntity(ContentType(`text/html`), "Unauthorized"))
                }
              }
              else {
                complete {
                  import eu.inn.binders.json._
                  try {
                    val update = data.parseJson[MessageUpdate]
                    val actorRef = gameActors.getOrElse(update.message.chat,
                      synchronized {
                        val newGame = context.system.actorOf(Props(new GameActor(botApi)), update.message.chat.toString)
                        gameActors.put(update.message.chat, newGame)
                        newGame
                      }
                    )
                    actorRef ! update
                    HttpResponse(StatusCodes.OK, HttpEntity(ContentType(`text/html`), "ok"))
                  }
                  catch {
                    case e: Throwable ⇒
                      log.error("Failed: ", e)
                      HttpResponse(StatusCodes.InternalServerError, HttpEntity(ContentType(`text/html`), "failed"))
                  }
                }
              }
          }
        }
      }

  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }
}
