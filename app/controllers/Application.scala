package controllers

import play.api._
import play.api.mvc._
import com.github.nscala_time.time.Imports._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

import reactivemongo.api._

import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object Application extends Controller with MongoController {

  def collection: JSONCollection = db.collection[JSONCollection]("recIndex")

  case class RecIndex(pid: String, start: DateTime)

  /*implicit val recIndexReads: Reads[RecIndex] = (
    (JsPath \ "pid").read[String] and
    (JsPath \ "start").read[DateTime]
   )(RecIndex.apply _)

  implicit val recIndexWrites: Writes[RecIndex] = (
    (JsPath \ "pid").write[String] and
    (JsPath \ "start").write[DateTime]
   )(unlift(RecIndex.unapply))
  */

  implicit val recIndexFormat: Format[RecIndex] = (
    (__ \ "pid").format[String] and
      (__ \ "start").format[DateTime]
    )(RecIndex.apply, unlift(RecIndex.unapply))

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def test2 = Action(BodyParsers.parse.json) {
    request =>
      request.body.validate[RecIndex] match {
        case s: JsSuccess[RecIndex] => Ok(Json.toJson(s.value))
        case e: JsError => BadRequest
      }
  }

  def test3 = Action {
    val json = Json.obj("name" -> "Henry", "birth" -> new DateTime())

    Ok(json)
  }

  def test4 = Action.async(parse.json) { request =>

    val transformer = (__ \ 'start).json.update(
      of[JsNumber].map {
        case JsNumber(n) => Json.obj("$date" -> n.toLong)
      })

    request.body.transform(transformer).map { result =>
      collection.insert(result).map { lastError =>
        Logger.debug(s"Successfully inserted with LastError: $lastError")
        Created
      }
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }
}