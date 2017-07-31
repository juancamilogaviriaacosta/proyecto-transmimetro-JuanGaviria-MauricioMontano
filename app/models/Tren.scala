package models

import java.util.Date

class Tren {

  private var id:Integer = null
  private var horaSalida:Date = null
  private var estacionOrigen:Estacion = null
  private var estacionDestino:Estacion = null
  private var estacionActual:Estacion = null
  private var pasajerosActual:Integer = null
  private var capacidadDelTren:Integer = null

  def getId: Integer = synchronized { id }

  def setId(id: Integer): Unit = synchronized {
    this.id = id
  }

  def getHoraSalida: Date = synchronized { horaSalida }

  def setHoraSalida(horaSalida: Date): Unit = synchronized {
    this.horaSalida = horaSalida
  }

  def getEstacionOrigen: Estacion = synchronized { estacionOrigen }

  def setEstacionOrigen(estacionOrigen: Estacion): Unit = synchronized {
    this.estacionOrigen = estacionOrigen
  }

  def getEstacionDestino: Estacion = synchronized { estacionDestino }

  def setEstacionDestino(estacionDestino: Estacion): Unit = synchronized {
    this.estacionDestino = estacionDestino
  }

  def getEstacionActual: Estacion = synchronized { estacionActual }

  def setEstacionActual(estacionActual: Estacion): Unit = synchronized {
    this.estacionActual = estacionActual
  }

  def getPasajerosActual: Integer = synchronized { pasajerosActual }

  def setPasajerosActual(pasajerosActual: Integer): Unit = synchronized {
    this.pasajerosActual = pasajerosActual
  }

  def getCapacidadDelTren: Integer = synchronized { capacidadDelTren }

  def setCapacidadDelTren(capacidadDelTren: Integer): Unit = synchronized {
    this.capacidadDelTren = capacidadDelTren
  }

}
