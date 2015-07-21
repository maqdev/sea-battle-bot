package com.maqdev.telegram

import akka.actor.ActorSystem
import akka.event.Logging
import spray.client.pipelining._
import spray.http._

import scala.concurrent.Future

class BotApi(token: String, implicit val system: ActorSystem) {
  val log = Logging.getLogger(system, this)

  def setWebhook(url: String) = send("setWebhook", Map("url" → url))

  def sendMessage(chatId: Int, text: String): Future[String] = send("sendMessage", Map("chat_id" → chatId.toString, "text" → text))

  def sendMessage(chat: Either[User, GroupChat], text: String): Future[String] = chat match {
    case Left(user) ⇒ sendMessage(user.id, text)
    case Right(chatGroup) ⇒ sendMessage(chatGroup.id, text)
  }

  def sendPhoto(chatId: Int, photo: FormFile): Future[String] = send("sendPhoto", Map("chat_id" → chatId.toString),
    Map("photo" → photo)
  )

  def sendPhoto(chat: Either[User, GroupChat], photo: FormFile): Future[String] = chat match {
    case Left(user) ⇒ sendPhoto(user.id, photo)
    case Right(chatGroup) ⇒ sendPhoto(chatGroup.id, photo)
  }

  def sendSticker(chatId: Int, sticker: String): Future[String] = send("sendSticker", Map("chat_id" → chatId.toString, "sticker" → sticker))

  def sendSticker(chat: Either[User, GroupChat], sticker: String): Future[String] = chat match {
    case Left(user) ⇒ sendSticker(user.id, sticker)
    case Right(chatGroup) ⇒ sendSticker(chatGroup.id, sticker)
  }

  def send(method: String, data: Map[String,String] = Map(), files: Map[String, FormFile] = Map()): Future[String] = {
    import system.dispatcher
    val uri = Uri(s"https://api.telegram.org/bot$token/$method")
    log.info("--> telegram: {} {} files: {}", uri, data, files.keys)
    val pipeline = sendReceive ~> unmarshal[String]
    pipeline {
      if (files.isEmpty) {
        Post(uri, FormData(data))
      }
      else {
        val mf = MultipartFormData(
          data.map(kv ⇒ BodyPart( HttpEntity(kv._2),  kv._1 ) ).toSeq ++
          files.map(kv ⇒ BodyPart(kv._2, kv._1)).toSeq
        )
        Post(uri, mf)
      }
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

case class User(id: Int, firstName: String, lastName: Option[String], username: Option[String])
case class GroupChat(id: Int, title: String)
case class Message(messageId: Int, from: User, date: Int, chat: Either[User, GroupChat], text: Option[String])
case class MessageUpdate(updateId: Int, message: Message)

object Stickers {
  def smileyValera = "BQADAgAD0gIAAvqspAXRCXxzjnmnagI"
  def ironicalMika = "BQADAgADxgIAAvqspAWPlbX9i9srDAI"
  def piratMika = "BQADAgADyAIAAvqspAWFc0o8oD2c7QI"
}