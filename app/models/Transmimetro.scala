package models

class Transmimetro {

  private var estaciones:List[Estacion] = null
  private var trenes:List[Tren] = null
  private var capacidadSistema:Integer = 990000
  private var pasajerosActualesEnSistema:Integer = 0

  def getEstaciones: List[Estacion] = synchronized { estaciones }

  def setEstaciones(estaciones: List[Estacion]): Unit = synchronized {
    this.estaciones = estaciones
  }

  def getTrenes: List[Tren] = synchronized { trenes }

  def setTrenes(trenes: List[Tren]): Unit = synchronized {
    this.trenes = trenes
  }

  def getCapacidadSistema: Integer = synchronized { capacidadSistema }

  def setCapacidadSistema(capacidadSistema: Integer): Unit = synchronized {
    this.capacidadSistema = capacidadSistema
  }

  def getPasajerosActualesEnSistema: Integer = synchronized { pasajerosActualesEnSistema }

  def setPasajerosActualesEnSistema(pasajerosActualesEnSistema: Integer): Unit = synchronized {
    this.pasajerosActualesEnSistema = pasajerosActualesEnSistema
  }

}
