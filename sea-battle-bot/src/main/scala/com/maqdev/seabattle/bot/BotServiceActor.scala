package com.maqdev.seabattle.bot

import akka.actor.Actor
import akka.event.Logging
import spray.http.MediaTypes._
import spray.http._
import spray.routing._

class BotServiceActor extends Actor with HttpService {
  import com.typesafe.config._
  val conf = ConfigFactory.load()
  val token = conf.getString("token")
  val log = Logging(context.system, this)

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
      path("/sea-battle/webhook" / RestPath ) { token ⇒
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
                  HttpResponse(StatusCodes.OK, HttpEntity(ContentType(`text/html`), "YYYY"))
                }
              }
          }
        }
      }

  implicit class Regex(sc: StringContext) {
    def r = new util.matching.Regex(sc.parts.mkString, sc.parts.tail.map(_ => "x"): _*)
  }
}
