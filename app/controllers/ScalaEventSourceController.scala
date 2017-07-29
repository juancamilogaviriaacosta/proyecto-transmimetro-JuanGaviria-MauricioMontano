package controllers

import java.io.File

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
      var basePath = "/app/public/upload/"

      var archivoTmp = new File(basePath)
      if(!archivoTmp.exists()) {
        basePath = Play.application().getFile("/public/upload").getAbsolutePath();
      }

      val filename = picture.filename
      val contentType = picture.contentType
      picture.ref.moveTo(Paths.get(basePath + filename), replace = true)
      reiniciarSimulacion();
      Ok(views.html.scalaeventsource())
    }.getOrElse {
      Redirect(routes.ScalaEventSourceController.index).flashing(
        "error" -> "Missing file")
    }
  }

}
