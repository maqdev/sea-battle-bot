package com.maqdev.seabattle.bot

import com.maqdev.telegram.BotApi

import scala.concurrent.duration._

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import spray.can.Http

object Boot extends App {


  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("spray-seabattle")

  // create and start our service actor
  val service = system.actorOf(Props[BotServiceActor], "sea-battle-bot")

  import com.typesafe.config._
  val conf = ConfigFactory.load()
  val serverPort = conf.getInt("port")
  val serverInterface = conf.getString("interface")

  val telegramToken = conf.getString("telegram-token")
  val token = conf.getString("token")
  val myUrl = conf.getString("my-url")
  val botApi = new BotApi(telegramToken, system)
  botApi.setWebhook(myUrl + token)

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = serverInterface, port = serverPort)
}
