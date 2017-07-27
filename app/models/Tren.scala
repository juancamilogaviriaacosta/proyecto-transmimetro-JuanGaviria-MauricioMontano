package models

class Tren {

  var id:Integer = null
  var horaSalida:String = null
  var estacionOrigen:Estacion = null
  var estacionDestino:Estacion = null
  var estacionActual:Estacion = null
  var pasajeros:List[Pasajero] = null
  var capacidadPasajeros:Integer = null

}
