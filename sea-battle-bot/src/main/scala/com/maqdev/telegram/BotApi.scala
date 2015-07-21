package com.maqdev.telegram

import akka.actor.ActorSystem
import akka.event.Logging
import spray.client.pipelining._
import spray.http.{FormData, Uri}

import scala.concurrent.Future

class BotApi(token: String, implicit val system: ActorSystem) {
  val log = Logging.getLogger(system, this)

  def setWebhook(url: String) = send("setWebhook", Map("url" → url))

  def send(method: String, data: Map[String,String] = Map()): Future[String] = {
    import system.dispatcher
    val uri = Uri(s"https://api.telegram.org/bot$token/$method")
    log.info("--> telegram: {} {}", uri, data)
    val pipeline = sendReceive ~> unmarshal[String]
    pipeline {
      Post(uri, FormData(data))
    } map { s ⇒
      log.info("<-- telegram: {} ", s)
      s
    }
  }
}
