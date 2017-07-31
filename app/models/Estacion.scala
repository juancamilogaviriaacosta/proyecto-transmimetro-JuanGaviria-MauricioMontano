package models

class Estacion {

  private var nombre:String = null
  private var orden:Integer = null
  private var pasajerosActual:Integer = null
  private var numeroIngresos:Integer = null
  private var numeroSalidas:Integer = null

  def getNombre: String = synchronized { nombre }

  def setNombre(nombre: String): Unit = synchronized {
    this.nombre = nombre
  }

  def getOrden: Integer = synchronized { orden }

  def setOrden(orden: Integer): Unit = synchronized {
    this.orden = orden
  }

  def getPasajerosActual: Integer = synchronized { pasajerosActual }

  def setPasajerosActual(pasajerosActual: Integer): Unit = synchronized {
    this.pasajerosActual = pasajerosActual
  }

  def getNumeroIngresos: Integer = synchronized { numeroIngresos }

  def setNumeroIngresos(numeroIngresos: Integer): Unit = synchronized {
    this.numeroIngresos = numeroIngresos
  }

  def getNumeroSalidas: Integer = synchronized { numeroSalidas }

  def setNumeroSalidas(numeroSalidas: Integer): Unit = synchronized {
    this.numeroSalidas = numeroSalidas
  }

}
