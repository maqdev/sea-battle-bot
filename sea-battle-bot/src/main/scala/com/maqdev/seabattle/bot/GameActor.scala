package com.maqdev.seabattle.bot

import java.io.ByteArrayOutputStream

import akka.actor.{Actor, ActorLogging}
import com.maqdev.seabattle.{ImageBoardDrawer, Point, SeaBattleGameBoard, SeaBattleGameCreator}
import com.maqdev.telegram.{BotApi, GroupChat, MessageUpdate, User}
import spray.http._

import scala.concurrent.Future

class GameActor(botApi: BotApi) extends Actor with ActorLogging{
  val coordsRegex = """^([a-z]+)([0-9]+)$""".r

  override def receive: Receive = {
    case update: MessageUpdate ⇒ {
      val name = update.message.chat match {
        case Left(user) ⇒ user.firstName
        case Right(group) ⇒ "people"
      }
      botApi.sendMessage(update.message.chat, s"Yo $name! Lets play?")

      val height = 10
      val width = 10
      val creator = new SeaBattleGameCreator(width, height, List(1,2,2,3,3))
      val myBoard = creator.createBoard
      val enemyBoard = creator.createBoard

      sendBoards(update.message.chat, myBoard, enemyBoard)
      context.become(playing(myBoard, enemyBoard))
    }
  }

  def playing(myBoard: SeaBattleGameBoard, enemyBoard: SeaBattleGameBoard): Receive = {
    case update: MessageUpdate ⇒ {
      update.message.text match {
        case Some(coordsRegex(y, x)) ⇒ {
          val shootPoint = Point(x.toInt, y(0).toInt - 'a'.toInt)
          if (shootPoint.x < 0 || shootPoint.x >= enemyBoard.width ||
            shootPoint.y < 0 || shootPoint.y >= enemyBoard.height) {
            send(update.message.chat, "Coordinates are wrong!")
          } else {
            if (enemyBoard.shoot(shootPoint))
              send(update.message.chat, "You hit!")

            if (enemyBoard.complete) {
              sendBoards(update.message.chat, myBoard, enemyBoard)
              send(update.message.chat, "TADA! You Won!")
              context.unbecome()
            }
            else {
              val enemyShoot = myBoard.AIshoot
              send(update.message.chat, s"Enemy hit to $enemyShoot")
              if (myBoard.shoot(enemyShoot))
                send(update.message.chat, "You shot :-(")
              if (myBoard.complete) {
                sendBoards(update.message.chat, myBoard, enemyBoard)
                send(update.message.chat, "GAME OVER! Looser!")
                context.unbecome()
              } else {
                sendBoards(update.message.chat, myBoard, enemyBoard)
              }
            }
          }
        }
        case _ ⇒ sendHelp(update.message.chat)
      }
    }
  }

  def sendHelp(chat: Either[User,GroupChat]) = botApi.sendMessage(chat, "Please enter coordinates, for example e4")
  def send(chat: Either[User,GroupChat], msg: String) = botApi.sendMessage(chat, msg)

  def sendBoard(chat: Either[User, GroupChat], myBoard: SeaBattleGameBoard, isMyBoard: Boolean): Future[String] = {
    val out = new ByteArrayOutputStream()
    val drawer = new ImageBoardDrawer(500,500,"png",out,isMyBoard)
    drawer.drawBoard(myBoard)
    val httpData = HttpData(out.toByteArray)
    val httpEntity = HttpEntity(MediaTypes.`image/png`, httpData).asInstanceOf[HttpEntity.NonEmpty]
    val formFile = FormFile("board.png", httpEntity)
    botApi.sendPhoto(chat, formFile)
  }

  def sendBoards(chat: Either[User,GroupChat], myBoard: SeaBattleGameBoard, enemyBoard: SeaBattleGameBoard): Unit = {
    import context.dispatcher
    sendBoard(chat, myBoard, isMyBoard = true) map { r ⇒
      sendBoard(chat, enemyBoard, isMyBoard = false)
    } recover {
      case e: Throwable ⇒
        log.error(e, "Can't send board photo to " + chat.toString)
        send(chat, "Something wrong :( ")
    }
  }
}
