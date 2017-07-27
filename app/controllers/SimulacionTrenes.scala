package controllers

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

  def simular(): Unit = {
    val tm = cargarArchivos
    
    //Hilo para mover los trenes
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while ( {
            !llegaronTodosLosTrenes(tm.trenes)
          }) {
            for (t <- tm.trenes) {
              siguienteParada(tm.estaciones, t)
            }
            Thread.sleep(2000)
          }
          imprimir(tm)
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
          while ( {
            !llegaronTodosLosTrenes(tm.trenes)
          }) {
            for (e <- tm.estaciones) {
              val numeroPasajerosEnEstacion = new Random().nextInt(tm.pasajeros.size)
              if (numeroPasajerosEnEstacion > 0) {
                val pasajerosEstacion = tm.pasajeros.slice(0, numeroPasajerosEnEstacion)
                e.pasajeros ++= pasajerosEstacion
                e.numeroIngresos = e.numeroIngresos + numeroPasajerosEnEstacion
                tm.pasajeros = tm.pasajeros diff pasajerosEstacion
              }
            }
          }
          imprimir(tm)
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
          while ( {
            !llegaronTodosLosTrenes(tm.trenes)
          }) {
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
          }
          imprimir(tm)
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
          while ( {
            !llegaronTodosLosTrenes(tm.trenes)
          }) {
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
            //Thread.sleep(3000);
          }
          imprimir(tm)
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    }).start()

    //Hilo para imprimir el estado del sistema
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while ( {
            !llegaronTodosLosTrenes(tm.trenes)
          }) {
            imprimir(tm)
            Thread.sleep(500)
          }
          imprimir(tm)
        } catch {
          case e: Exception =>
            e.printStackTrace()
        }
      }
    }).start()
  }

  def cargarArchivos: Transmimetro = try {
    val url = this.getClass.getResource(this.getClass.getSimpleName + ".class")
    val f = new File(url.getPath).getParentFile.getParentFile.getParentFile.getParentFile
    val paquete = f.getAbsolutePath.replaceAll("%20", " ") + File.separator + "web" + File.separator + "public" + File.separator + "main"

    val tm = new Transmimetro
    tm.trenes = List[Tren]()
    tm.estaciones = List[Estacion]()
    tm.pasajeros = List[Pasajero]()

    val archivoEstaciones = new File(paquete + File.separator + "archivoEstaciones.csv")
    val fr1 = new FileReader(archivoEstaciones)
    val br1 = new BufferedReader(fr1)
    br1.readLine()
    var linea1:String = br1.readLine()
    var contador = 0
    while (linea1 != null) {
      val lineaSplit = linea1.split(";")
      val tmp = new Estacion
      tmp.numeroIngresos = 0
      tmp.numeroSalidas = 0
      tmp.nombre = (lineaSplit(0))
      tmp.orden = contador
      tmp.pasajeros = List[Pasajero]()
      tm.estaciones = tmp :: tm.estaciones
      contador = contador + 1
      linea1 = br1.readLine()
    }
    fr1.close()
    br1.close()

    val archivoTrenes = new File(paquete + File.separator + "archivoTrenes.csv")
    val fr2 = new FileReader(archivoTrenes)
    val br2 = new BufferedReader(fr2)
    br2.readLine()
    var linea2:String = br2.readLine()
    while (linea2 != null) {
      val lineaSplit:Array[String] = linea2.split(";")
      val tmp = new Tren
      tmp.id = (Integer.valueOf(lineaSplit(0)))
      tmp.estacionOrigen = buscarEstacion(tm.estaciones, lineaSplit(1))
      tmp.horaSalida = lineaSplit(2)
      tmp.estacionDestino = buscarEstacion(tm.estaciones, lineaSplit(3))
      tmp.capacidadPasajeros = Integer.valueOf(lineaSplit(4))
      tmp.pasajeros = List[Pasajero]()
      tm.trenes = tmp :: tm.trenes
      linea2 = br2.readLine()
    }
    fr2.close()
    br2.close()

    val archivoPasajeros = new File(paquete + File.separator + "archivoPasajeros.csv")
    val fr3 = new FileReader(archivoPasajeros)
    val br3 = new BufferedReader(fr3)
    br3.readLine()
    var linea3:String = br3.readLine()
    while (linea3 != null) {
      val lineaSplit = linea3.split(";")
      val tmp = new Pasajero
      tmp.id = (Integer.valueOf(lineaSplit(0)))
      tmp.nombre = (lineaSplit(1))
      tm.pasajeros = tmp :: tm.pasajeros
      linea3 = br3.readLine()
    }
    fr3.close()
    br3.close()
    tm
  } catch {
    case e: Exception =>
      e.printStackTrace()
      null
  }

  def buscarEstacion(estaciones: List[Estacion], nombre: String): Estacion = {
    for (tmp:Estacion <- estaciones) {
      if (tmp.nombre.equals(nombre)) return tmp
    }
    null
  }

  def buscarTren(trenes: List[Tren], id: Integer): Tren = {
    for (tmp:Tren <- trenes) {
      if (tmp.id.equals(id)) return tmp
    }
    null
  }

  def llegaronTodosLosTrenes(trenes: List[Tren]): Boolean = {
    for (t <- trenes) {
      if (t.estacionActual == null) return false
      else if (!t.estacionActual.equals(t.estacionDestino)) return false
    }
    true
  }

  def siguienteParada(estaciones: List[Estacion], tren: Tren): Unit = {
    if (tren.estacionActual == null) {
      tren.estacionActual = tren.estacionOrigen;
    } else if (!tren.estacionActual.equals(tren.estacionDestino)) {
      if (tren.estacionOrigen.orden > tren.estacionDestino.orden) {
        var i = estaciones.size - 1
        while(i >= 0) {
          val e:Estacion = estaciones(i);
          if (e.orden < tren.estacionActual.orden) {
            tren.estacionActual = e;
            i = 0
          }
          i = i - 1
        }
      } else {
        var i = 0
        while(i < estaciones.size) {
          val e:Estacion = estaciones(i);
          if (e.orden > tren.estacionActual.orden) {
            tren.estacionActual = e
            i = estaciones.size
          }
          i = i + 1
        }
      }
    }
  }

  def imprimir(tm: Transmimetro): Unit = synchronized {
    println("\n\n----------Informe " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "----------")
    for (tmp <- tm.estaciones) {
      println("Estacion " + tmp.nombre + ": pasajeros actual " + tmp.pasajeros.size + ", total ingresos: " + tmp.numeroIngresos + ", total salidas: " + tmp.numeroSalidas)
    }
    for (tmp <- tm.trenes) {
      println("Tren " + tmp.id + " esta en " + tmp.estacionActual.nombre + " con " + tmp.pasajeros.size + " pasajeros")
    }
  }

}
