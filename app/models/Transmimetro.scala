package models

class Transmimetro {

  var estaciones:List[Estacion] = null
  var trenes:List[Tren] = null
  val capacidadSistema:Integer = 990000
  var pasajerosActualesEnSistema:Integer = 0
}
