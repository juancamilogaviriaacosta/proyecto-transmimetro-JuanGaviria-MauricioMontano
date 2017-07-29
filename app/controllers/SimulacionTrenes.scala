package controllers

import play.Play

class SimulacionTrenes {

  import models.Estacion
  import models.Pasajero
  import models.Transmimetro
  import models.Tren
  import java.io.BufferedReader
  import java.io.File
  import java.io.FileReader
  import java.text.SimpleDateFormat
  import java.util.Random
  import java.util.Date

  var tm:Transmimetro = null

  def simular(): Unit = {
    cargarArchivos

    //Hilo para mover los trenes
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while (!llegaronTodosLosTrenes()) {
            for (t <- tm.trenes) {
              siguienteParada(t)
            }
            Thread.sleep(5000)
          }
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    }).start()
    
    //Hilo para recibir pasajeros en las estaciones
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while (!llegaronTodosLosTrenes()) {
            for (e <- tm.estaciones) {
              val numeroPasajerosEnEstacion = new Random().nextInt(tm.pasajeros.size)
              if (numeroPasajerosEnEstacion > 0) {
                val pasajerosEstacion = tm.pasajeros.slice(0, numeroPasajerosEnEstacion)
                e.pasajeros ++= pasajerosEstacion
                e.numeroIngresos = e.numeroIngresos + numeroPasajerosEnEstacion
                tm.pasajeros = tm.pasajeros diff pasajerosEstacion
              }
            }
            Thread.sleep(2000)
          }
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    }).start()
    
    //Hilo para que los pasajeros suban a los trenes
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while (!llegaronTodosLosTrenes()) {
            for (e <- tm.estaciones) {
              for (t <- tm.trenes) {
                if (t.estacionActual != null && t.estacionActual.equals(e) && !e.pasajeros.isEmpty) {
                  val numeroPasajerosSuben = if (e.pasajeros.size <= (t.capacidadPasajeros - t.pasajeros.size)) {
                    e.pasajeros.size
                  }
                  else {
                    t.capacidadPasajeros - e.pasajeros.size
                  }
                  val pasajerosTmp = e.pasajeros.slice(0, numeroPasajerosSuben)
                  t.pasajeros ++= pasajerosTmp
                  e.pasajeros = e.pasajeros diff pasajerosTmp
                }
              }
            }
            Thread.sleep(3000)
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
          while (!llegaronTodosLosTrenes()) {
            for (t <- tm.trenes) {
              if (new Random().nextBoolean() && !t.pasajeros.isEmpty) {
                t.pasajeros = t.pasajeros diff List(t.pasajeros(0))
                t.estacionActual.numeroSalidas = t.estacionActual.numeroSalidas + 1
              }
              if (t.estacionActual != null && t.estacionActual.equals(t.estacionDestino)) {
                t.estacionActual.numeroSalidas = t.estacionActual.numeroSalidas + t.pasajeros.size
                t.pasajeros = t.pasajeros diff t.pasajeros
              }
            }
            Thread.sleep(2500)
          }
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    }).start()
  }

  def cargarArchivos: Transmimetro = try {

    tm = new Transmimetro
    tm.trenes = List[Tren]()
    tm.estaciones = List[Estacion]()
    tm.pasajeros = List[Pasajero]()

    var archivoEstaciones = new File("/app/public/archivoEstaciones.csv")
    if(!archivoEstaciones.exists()) {
      archivoEstaciones = Play.application().getFile("/public/archivoEstaciones.csv");
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
      tmp.pasajeros = List[Pasajero]()
      tm.estaciones = tmp :: tm.estaciones
      linea1 = br1.readLine()
    }
    tm.estaciones = tm.estaciones.reverse
    fr1.close()
    br1.close()

    var archivoTrenes = new File("/app/public/archivoTrenes.csv")
    if(!archivoTrenes.exists()) {
      archivoTrenes = Play.application().getFile("/public/archivoTrenes.csv");
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
      tmp.pasajeros = List[Pasajero]()
      tm.trenes = tmp :: tm.trenes
      linea2 = br2.readLine()
    }
    tm.trenes = tm.trenes.reverse
    fr2.close()
    br2.close()

    var archivoPasajeros = new File("/app/public/archivoPasajeros.csv")
    if(!archivoPasajeros.exists()) {
      archivoPasajeros = Play.application().getFile("/public/archivoPasajeros.csv");
    }
    val fr3 = new FileReader(archivoPasajeros)
    val br3 = new BufferedReader(fr3)
    br3.readLine()
    var linea3:String = br3.readLine()
    while (linea3 != null) {
      val lineaSplit = linea3.split(";")
      val tmp = new Pasajero
      tmp.id = Integer.valueOf(lineaSplit(0))
      tmp.nombre = lineaSplit(1)
      tm.pasajeros = tmp :: tm.pasajeros
      linea3 = br3.readLine()
    }
    tm.pasajeros = tm.pasajeros.reverse
    fr3.close()
    br3.close()
    tm
  } catch {
    case e: Exception =>
      e.printStackTrace()
      null
  }

  def buscarEstacion(nombre: String): Estacion = synchronized {
    for (tmp:Estacion <- tm.estaciones) {
      if (tmp.nombre.equals(nombre)) {
        return tmp
      }
    }
    return null
  }

  def buscarTren(id: Integer): Tren = synchronized {
    for (tmp:Tren <- tm.trenes) {
      if (tmp.id.equals(id)) {
        return tmp
      }
    }
    return null
  }

  def llegaronTodosLosTrenes(): Boolean = synchronized {
    for (t <- tm.trenes) {
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
      tren.estacionActual = tren.estacionOrigen;
    } else if (!tren.estacionActual.equals(tren.estacionDestino)) {
      if (tren.estacionOrigen.orden > tren.estacionDestino.orden) {
        var i = tm.estaciones.size - 1
        while(i >= 0) {
          val e:Estacion = tm.estaciones(i);
          if (e.orden < tren.estacionActual.orden) {
            tren.estacionActual = e;
            i = -1
          } else {
            i = i - 1
          }
        }
      } else {
        var i = 0
        while(i < tm.estaciones.size) {
          val e:Estacion = tm.estaciones(i);
          if (e.orden > tren.estacionActual.orden) {
            tren.estacionActual = e
            i = tm.estaciones.size
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
    for (tmp <- tm.estaciones) {
      info = info + "<tr> <td>"+ tmp.nombre +"</td> <td>"+ tmp.pasajeros.size +"</td> <td>"+ tmp.numeroIngresos +"</td> <td>"+ tmp.numeroSalidas +"</td> </tr>"
    }
    info = info + "</table> <br/> <br/>"
    info = info + "<table align=center> <tr> <th>Tren</th> <th>Ubicación actual</th> <th>Numero de pasajeros</th> <th>Estado</th> </tr>"
    for (tmp <- tm.trenes) {
      info = info + "<tr> <td>"+ tmp.id +"</td> <td>"+ tmp.estacionActual.nombre +"</td> <td>"+ tmp.pasajeros.size +"</td> <td>"+ (if (tmp.estacionActual == null) ("Preparado") else (if (tmp.estacionActual == tmp.estacionDestino) ("Finalzó recorrido") else ("En curso") )) +"</td> </tr>"
    }
    info = info + "</table>"
    info
  }

}
