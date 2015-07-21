package com.maqdev.telegram

import akka.actor.ActorSystem
import akka.event.Logging
import spray.client.pipelining._
import spray.http.{FormData, Uri}

import scala.concurrent.Future

class BotApi(token: String, implicit val system: ActorSystem) {
  val log = Logging.getLogger(system, this)

  def setWebhook(url: String) = send("setWebhook", Map("url" → url))

  def sendMessage(chatId: Int, text: String): Future[String] = send("sendMessage", Map("chat_id" → chatId.toString, "text" → text))

  def sendMessage(chat: Either[User, GroupChat], text: String): Future[String] = chat match {
    case Left(user) ⇒ sendMessage(user.id, text)
    case Right(chatGroup) ⇒ sendMessage(chatGroup.id, text)
  }

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
    } recover {
      case e: Throwable ⇒
        log.error("<-- telegram: {} ", e)
        throw e
    }
  }
}

case class User(id: Int, firstName: String, lastName: String, userName: String)
case class GroupChat(id: Int, title: String)
case class Message(messageId: Int, from: User, date: Int, chat: Either[User, GroupChat], text: Option[String])
case class MessageUpdate(updateId: Int, message: Message)