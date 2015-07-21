package com.maqdev.seabattle.bot

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor}
import com.maqdev.telegram.{MessageUpdate, BotApi, User, GroupChat}

class GameActor(botApi: BotApi) extends Actor with ActorLogging{

  override def receive: Receive = {
    case update: MessageUpdate ⇒ botApi.sendMessage(update.message.chat, "Yo!" + update.message.text)
  }
}
