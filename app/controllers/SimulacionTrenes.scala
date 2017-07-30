package controllers

import models.Estacion
import models.Transmimetro
import models.Tren
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Random
import java.util.Date
import play.Play

class SimulacionTrenes {

  private var tm:Transmimetro = null

  def getTm: Transmimetro = synchronized { tm }

  def setTm(tm: Transmimetro): Unit = synchronized  {
    this.tm = tm
  }

  def simular(): Unit = {
    cargarArchivos

    //Se crea un hilo por cada tren para representar su comportamiento independiente
    //Los hilos generan el movimiento de cada tren
    for (t <- getTm.trenes) {
      new Thread(new Runnable() {
        override def run(): Unit = {
          try {
            while (!finDeOperacionDiaria()) {
              siguienteParada(t)
              Thread.sleep(1000)
            }
          } catch {
            case e: Exception =>
              e.printStackTrace()
          }
        }
      }).start()
    }
    
    //Se crea un hilo por cada estacion para representar su comportamiento independiente
    //Los hilos reciben pasajeros en las estaciones de manera aleatoria
    for (e <- getTm.estaciones) {
      new Thread(new Runnable() {
        override def run(): Unit = {
          try {
            while (!finDeOperacionDiaria()) {
              val numeroPasajerosEnEstacion = if ((getTm.capacidadSistema - getTm.pasajerosActualesEnSistema) < 0) 0 else new Random().nextInt(getTm.capacidadSistema - getTm.pasajerosActualesEnSistema)
              if (numeroPasajerosEnEstacion > 0) {
                e.pasajerosActual = e.pasajerosActual + numeroPasajerosEnEstacion
                e.numeroIngresos = e.numeroIngresos + numeroPasajerosEnEstacion
                getTm.pasajerosActualesEnSistema = numeroPasajerosEnEstacion
              }
              Thread.sleep(1500)
            }
          } catch {
            case e: Exception =>
              e.printStackTrace()
          }
        }
      }).start()
    }

    //Hilo para que los pasajeros suban a los trenes
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while (!finDeOperacionDiaria()) {
            for (e <- getTm.estaciones) {
              for (t <- getTm.trenes) {
                if (t.estacionActual != null && t.estacionActual.equals(e) && e.pasajerosActual > 0) {
                  val numeroPasajerosCabenEnTren:Integer = t.capacidadPasajeros - t.pasajerosActual
                  t.pasajerosActual = t.pasajerosActual + numeroPasajerosCabenEnTren
                  e.pasajerosActual = e.pasajerosActual - numeroPasajerosCabenEnTren
                }
              }
            }
            Thread.sleep(1500)
          }
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    }).start()

    //Hilo para que los pasajeros bajen de los trenes
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while (!finDeOperacionDiaria()) {
            for (t <- getTm.trenes) {
              if (new Random().nextBoolean() && t.pasajerosActual > 0) {
                val numeroPasajerosQueBajan = new Random().nextInt(t.pasajerosActual)
                t.pasajerosActual = t.pasajerosActual - numeroPasajerosQueBajan
                t.estacionActual.numeroSalidas = t.estacionActual.numeroSalidas + numeroPasajerosQueBajan
                getTm.pasajerosActualesEnSistema = getTm.pasajerosActualesEnSistema - numeroPasajerosQueBajan
              }
              if (t.estacionActual != null && t.estacionActual.equals(t.estacionDestino)) {
                t.estacionActual.numeroSalidas = t.estacionActual.numeroSalidas + t.pasajerosActual
                getTm.pasajerosActualesEnSistema = getTm.pasajerosActualesEnSistema - t.pasajerosActual
                t.pasajerosActual = 0
              }
            }
            Thread.sleep(1500)
          }
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    }).start()
  }

  def cargarArchivos: Transmimetro = try {

    setTm(new Transmimetro)
    getTm.trenes = List[Tren]()
    getTm.estaciones = List[Estacion]()

    var archivoEstaciones = new File("/app/public/archivoEstaciones.csv")
    if(!archivoEstaciones.exists()) {
      archivoEstaciones = Play.application().getFile("/public/archivoEstaciones.csv")
    }
    val fr1 = new FileReader(archivoEstaciones)
    val br1 = new BufferedReader(fr1)
    br1.readLine()
    var linea1:String = br1.readLine()
    while (linea1 != null) {
      val lineaSplit = linea1.split(";")
      val tmp = new Estacion
      tmp.numeroIngresos = 0
      tmp.numeroSalidas = 0
      tmp.nombre = lineaSplit(0)
      tmp.orden = Integer.valueOf(lineaSplit(1))
      tmp.pasajerosActual = 0
      getTm.estaciones = tmp :: getTm.estaciones
      linea1 = br1.readLine()
    }
    getTm.estaciones = getTm.estaciones.reverse
    fr1.close()
    br1.close()

    var archivoTrenes = new File("/app/public/archivoTrenes.csv")
    if(!archivoTrenes.exists()) {
      archivoTrenes = Play.application().getFile("/public/archivoTrenes.csv")
    }
    val fr2 = new FileReader(archivoTrenes)
    val br2 = new BufferedReader(fr2)
    br2.readLine()
    var linea2:String = br2.readLine()
    while (linea2 != null) {
      val lineaSplit:Array[String] = linea2.split(";")
      val tmp = new Tren
      tmp.id = (Integer.valueOf(lineaSplit(0)))
      tmp.estacionOrigen = buscarEstacion(lineaSplit(1))
      tmp.horaSalida = lineaSplit(2)
      tmp.estacionDestino = buscarEstacion(lineaSplit(3))
      tmp.capacidadPasajeros = Integer.valueOf(lineaSplit(4))
      tmp.pasajerosActual = 0
      getTm.trenes = tmp :: getTm.trenes
      linea2 = br2.readLine()
    }
    getTm.trenes = getTm.trenes.reverse
    fr2.close()
    br2.close()
    return getTm
  } catch {
    case e: Exception =>
      e.printStackTrace()
      return null
  }

  def buscarEstacion(nombre: String): Estacion = synchronized {
    for (tmp:Estacion <- getTm.estaciones) {
      if (tmp.nombre.equals(nombre)) {
        return tmp
      }
    }
    return null
  }

  def buscarTren(id: Integer): Tren = synchronized {
    for (tmp:Tren <- getTm.trenes) {
      if (tmp.id.equals(id)) {
        return tmp
      }
    }
    return null
  }

  def finDeOperacionDiaria(): Boolean = synchronized {
    for (t <- getTm.trenes) {
      if (t.estacionActual == null) {
        return false
      } else if (!t.estacionActual.equals(t.estacionDestino)) {
        return false
      }
    }
    return true
  }

  def siguienteParada(tren: Tren): Unit = synchronized {
    if (tren.estacionActual == null) {
      tren.estacionActual = tren.estacionOrigen
    } else if (!tren.estacionActual.equals(tren.estacionDestino)) {
      if (tren.estacionOrigen.orden > tren.estacionDestino.orden) {
        var i = getTm.estaciones.size - 1
        while(i >= 0) {
          val e:Estacion = getTm.estaciones(i)
          if (e.orden < tren.estacionActual.orden) {
            tren.estacionActual = e
            i = -1
          } else {
            i = i - 1
          }
        }
      } else {
        var i = 0
        while(i < getTm.estaciones.size) {
          val e:Estacion = getTm.estaciones(i)
          if (e.orden > tren.estacionActual.orden) {
            tren.estacionActual = e
            i = getTm.estaciones.size
          } else {
            i = i + 1
          }
        }
      }
    }
  }

  def imprimir(): String = synchronized {
    var info:String = ""
    info = "Informe " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "<br/><br/>"
    info = info + "<table> <tr> <th>Estación</th> <th>Pasajeros actual</th> <th>Total ingresos</th> <th>Total salidas</th> </tr>"
    for (tmp <- getTm.estaciones) {
      info = info + "<tr> <td>"+ tmp.nombre +"</td> <td>"+ tmp.pasajerosActual +"</td> <td>"+ tmp.numeroIngresos +"</td> <td>"+ tmp.numeroSalidas +"</td> </tr>"
    }
    info = info + "</table> <br/> <br/>"
    info = info + "<table align=center> <tr> <th>Tren</th> <th>Ubicación actual</th> <th>Numero de pasajeros</th> <th>Estado</th> </tr>"
    for (tmp <- getTm.trenes) {
      info = info + "<tr> <td>"+ tmp.id +"</td> <td>"+ tmp.estacionActual.nombre +"</td> <td>"+ tmp.pasajerosActual +"</td> <td>"+ (if (tmp.estacionActual == null) ("Preparado") else (if (tmp.estacionActual == tmp.estacionDestino) ("Finalzó recorrido") else ("En curso") )) +"</td> </tr>"
    }
    info = info + "</table>"
    info
  }

}
