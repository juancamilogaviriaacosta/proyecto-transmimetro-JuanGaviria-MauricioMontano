package controllers

import play.Play
import java.nio.file.Paths
import javax.inject.{Inject, Singleton}
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.mvc._

@Singleton
class ScalaEventSourceController @Inject()(cc: ControllerComponents) extends AbstractController(cc) with ScalaTicker {

  def index() = Action {
    Ok(views.html.scalaeventsource())
  }

  def streamClock() = Action {
    Ok.chunked(stringSource via EventSource.flow).as(ContentTypes.EVENT_STREAM)
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      val basePath = Play.application.path.getPath + "/tmp/"
      val filename = picture.filename
      val contentType = picture.contentType
      picture.ref.moveTo(Paths.get(basePath + filename), replace = true)
      Ok(views.html.scalaeventsource())
    }.getOrElse {
      Redirect(routes.ScalaEventSourceController.index).flashing(
        "error" -> "Missing file")
    }
  }

}
