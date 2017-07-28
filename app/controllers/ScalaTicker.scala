package controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.stream.scaladsl.Source
import play.api.libs.json._

import scala.concurrent.duration._

trait ScalaTicker {

  object instancia {
    val st:SimulacionTrenes = new SimulacionTrenes
    st.simular()
  }

  def stringSource: Source[String, _] = {
    val tickSource = Source.tick(0 millis, 1000 millis, "TICK")
    val s = tickSource.map((tick) => instancia.st.imprimir())
    s
  }

  def jsonSource: Source[JsValue, _] = {
    val tickSource = Source.tick(0 millis, 100 millis, "TICK")
    val s = tickSource.map((tick) => Json.toJson(ZonedDateTime.now))
    s
  }

}
