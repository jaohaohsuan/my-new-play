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
import com.fasterxml.jackson.annotation.JsonValue

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
    (__ \ "pid").format[String] and (__ \ "start").format[DateTime]
    )(RecIndex.apply, unlift(RecIndex.unapply))

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def test2 = Action(BodyParsers.parse.json) { request =>
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
      of[JsNumber].map { o =>
        Json.obj("$date" -> o.value.toLong)
      })

    request.body.transform(transformer).map { result =>
      collection.insert(result).map { lastError =>
        Logger.debug(s"Successfully inserted with LastError: $lastError")
        Created
      }
    }.getOrElse(Future.successful(BadRequest("invalid json")))
  }

  def test5 = Action.async(parse.json) { request =>

    val dateRead = of[JsString].map { o =>
      Json.obj("$date" -> new DateTime(o.value).getMillis)
    }

    val weekOfYearRead = of[JsNumber].map { o => Json.obj("$int" -> o.value.toInt)}

    val transformer = (__ \ 'startTime).json.update(dateRead) andThen
                      (__ \ 'endTime).json.update(dateRead) andThen
                      (__ \ 'weekOfYear).json.update(weekOfYearRead)

    request.body.transform(transformer).map { result =>
      collection.insert(result).map { lastError =>
        Logger.debug(s"Successfully inserted with LastError: $lastError")
        Created
      }
    }.getOrElse(Future.successful(BadRequest))
  }

  def crash = Action.async { request =>

    val futurePIValue: Future[Double] = Future {
      throw new Exception("crash it！")
    }
    futurePIValue.map {
      case d:Double=> Ok(d.toString)
    } recover {
      case e:Throwable => BadRequest(e.getMessage)
    }
  }

  def demo1 = new Action[AnyContent]{
    def parser = BodyParsers.parse.anyContent

    def apply(request: Request[AnyContent]) = {
      Future[SimpleResult] {
        Ok("This is an blocking action and creating with complexity way")
      }
    }
  }
}