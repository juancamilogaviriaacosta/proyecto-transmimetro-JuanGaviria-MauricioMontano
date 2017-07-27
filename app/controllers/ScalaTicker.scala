package controllers

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.stream.scaladsl.Source
import play.api.libs.json._

import scala.concurrent.duration._

trait ScalaTicker {

  def stringSource: Source[String, _] = {
    val df: DateTimeFormatter = DateTimeFormatter.ofPattern("dd HH mm ss")
    val tickSource = Source.tick(0 millis, 100 millis, "TICK")
    val s = tickSource.map((tick) => df.format(ZonedDateTime.now()) + " texto")
    s
  }

  def jsonSource: Source[JsValue, _] = {
    val tickSource = Source.tick(0 millis, 100 millis, "TICK")
    val s = tickSource.map((tick) => Json.toJson(ZonedDateTime.now))
    s
  }

}
