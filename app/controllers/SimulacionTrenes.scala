package controllers

import models.Estacion
import models.Transmimetro
import models.Tren
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.Random
import java.util.Calendar
import play.Play

class SimulacionTrenes {

  private var tm:Transmimetro = null

  def simular(): Unit = {
    cargarArchivos

    //Se crea un hilo por cada tren para representar su comportamiento independiente
    //Los hilos generan el movimiento de cada tren
    for (t <- tm.getTrenes) {
      new Thread(new Runnable() {
        override def run(): Unit = {
          try {
            while (!finDeOperacionDiaria()) {
              siguienteParada(t)

              //si el tren llega a su destino entonces se bajan todos los pasajeros
              if (t.getEstacionActual != null && t.getEstacionActual.equals(t.getEstacionDestino)) {
                t.getEstacionActual.setNumeroSalidas(t.getEstacionActual.getNumeroSalidas + t.getPasajerosActual)
                tm.setPasajerosActualesEnSistema(tm.getPasajerosActualesEnSistema - t.getPasajerosActual)
                t.setPasajerosActual(0)
              }
              Thread.sleep(2000) //frecuencia de los buses es de 2 segundos
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
    for (e <- tm.getEstaciones) {
      new Thread(new Runnable() {
        override def run(): Unit = {
          try {
            while (!finDeOperacionDiaria()) {
              val numeroPasajerosEnEstacion = new Random().nextInt(100)
              if (numeroPasajerosEnEstacion > 0) {
                e.setPasajerosActual(e.getPasajerosActual + numeroPasajerosEnEstacion)
                e.setNumeroIngresos(e.getNumeroIngresos + numeroPasajerosEnEstacion)
                tm.setPasajerosActualesEnSistema(numeroPasajerosEnEstacion)
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

    //Hilo para que los pasajeros suban a los trenes
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while (!finDeOperacionDiaria()) {
            for (e <- tm.getEstaciones) {
              for (t <- tm.getTrenes) {
                if (t.getEstacionActual != null && !t.getEstacionActual.equals(t.getEstacionDestino) && t.getEstacionActual.equals(e) && e.getPasajerosActual > 0) {
                  val numeroPasajerosCabenEnTren:Integer = t.getCapacidadDelTren - t.getPasajerosActual
                  t.setPasajerosActual(t.getPasajerosActual + numeroPasajerosCabenEnTren)
                  e.setPasajerosActual(if (e.getPasajerosActual - numeroPasajerosCabenEnTren < 0) 0 else e.getPasajerosActual - numeroPasajerosCabenEnTren)
                }
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

    //Hilo para que los pasajeros bajen de los trenes
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while (!finDeOperacionDiaria()) {
            for (t <- tm.getTrenes) {
              if (t.getPasajerosActual > 0) {
                val numeroPasajerosQueBajan = new Random().nextInt(t.getPasajerosActual)
                t.setPasajerosActual(t.getPasajerosActual - numeroPasajerosQueBajan)
                t.getEstacionActual.setNumeroSalidas(t.getEstacionActual.getNumeroSalidas + numeroPasajerosQueBajan)
                tm.setPasajerosActualesEnSistema(tm.getPasajerosActualesEnSistema - numeroPasajerosQueBajan)
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

    //Hilo para emular el paso del tiempo
    new Thread(new Runnable() {
      override def run(): Unit = {
        try {
          while (!finDeOperacionDiaria()) {
            val cal = Calendar.getInstance();
            cal.setTime(tm.getHoraActual)
            cal.add(Calendar.MINUTE, 1)
            tm.setHoraActual(cal.getTime)
            Thread.sleep(2500)
          }

          //Se deben salir del sistema las personas que queden en las estaciones al final del dia
          if(finDeOperacionDiaria) {
            for (e <- tm.getEstaciones) {
              e.setNumeroSalidas(e.getNumeroSalidas + e.getPasajerosActual);
              e.setPasajerosActual(0)
            }
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
    tm.setTrenes(List[Tren]())
    tm.setEstaciones(List[Estacion]())

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
      tmp.setNumeroIngresos(0)
      tmp.setNumeroSalidas(0)
      tmp.setNombre(lineaSplit(0))
      tmp.setOrden(Integer.valueOf(lineaSplit(1)))
      tmp.setPasajerosActual(0)
      tm.setEstaciones(tmp :: tm.getEstaciones)
      linea1 = br1.readLine()
    }
    tm.setEstaciones(tm.getEstaciones.reverse)
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
      tmp.setId(Integer.valueOf(lineaSplit(0)))
      tmp.setEstacionOrigen(buscarEstacion(lineaSplit(1)))
      val horaSplit:Array[String] = lineaSplit(2).split(":");

      val cal = Calendar.getInstance()
      cal.set(Calendar.HOUR_OF_DAY, Integer.valueOf(horaSplit(0)));
      cal.set(Calendar.MINUTE, Integer.valueOf(horaSplit(1)));
      cal.set(Calendar.SECOND, 0);

      tmp.setHoraSalida(cal.getTime)
      tmp.setEstacionDestino(buscarEstacion(lineaSplit(3)))
      tmp.setCapacidadDelTren(Integer.valueOf(lineaSplit(4)))
      tmp.setPasajerosActual(0)
      tm.setTrenes(tmp :: tm.getTrenes)
      linea2 = br2.readLine()
    }
    tm.setTrenes(tm.getTrenes.reverse)
    fr2.close()
    br2.close()

    //comienza la operacion a las 4:00
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 4);
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    tm.setHoraActual(cal.getTime)

    //finaliza la operacion a las 23:00
    val cal2 = Calendar.getInstance()
    cal2.set(Calendar.HOUR_OF_DAY, 23);
    cal2.set(Calendar.MINUTE, 0);
    cal2.set(Calendar.SECOND, 0);
    tm.setHoraFinDeOperacion(cal2.getTime)

    return tm
  } catch {
    case e: Exception =>
      e.printStackTrace()
      return null
  }

  def buscarEstacion(nombre: String): Estacion = synchronized {
    for (tmp:Estacion <- tm.getEstaciones) {
      if (tmp.getNombre.equals(nombre)) {
        return tmp
      }
    }
    return null
  }

  def buscarTren(id: Integer): Tren = synchronized {
    for (tmp:Tren <- tm.getTrenes) {
      if (tmp.getId.equals(id)) {
        return tmp
      }
    }
    return null
  }

  def finDeOperacionDiaria(): Boolean = synchronized {
    val finHora = tm.getHoraActual.after(tm.getHoraFinDeOperacion)
    var finTrenes = true;
    for (t <- tm.getTrenes) {
      if (t.getEstacionActual == null) {
        finTrenes = false
      } else if (!t.getEstacionActual.equals(t.getEstacionDestino)) {
        finTrenes = false
      }
    }
    return finHora || finTrenes
  }

  def siguienteParada(tren: Tren): Unit = synchronized {
    if (tren.getEstacionActual == null) {
      if(tm.getHoraActual.after(tren.getHoraSalida)) {
        tren.setEstacionActual(tren.getEstacionOrigen)
      }
    } else if (!tren.getEstacionActual.equals(tren.getEstacionDestino)) {
      if (tren.getEstacionOrigen.getOrden > tren.getEstacionDestino.getOrden) {
        var i = tm.getEstaciones.size - 1
        while(i >= 0) {
          val e:Estacion = tm.getEstaciones(i)
          if (e.getOrden < tren.getEstacionActual.getOrden) {
            tren.setEstacionActual(e)
            i = -1
          } else {
            i = i - 1
          }
        }
      } else {
        var i = 0
        while(i < tm.getEstaciones.size) {
          val e:Estacion = tm.getEstaciones(i)
          if (e.getOrden > tren.getEstacionActual.getOrden) {
            tren.setEstacionActual(e)
            i = tm.getEstaciones.size
          } else {
            i = i + 1
          }
        }
      }
    }
  }

  def imprimir(): String = synchronized {
    var info:String = ""
    info = "Informe " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tm.getHoraActual) + "<br/><br/>"
    info = info + "<table> <tr> <th>Estación</th> <th>Pasajeros actual</th> <th>Total ingresos</th> <th>Total salidas</th> </tr>"
    for (tmp <- tm.getEstaciones) {
      info = info + "<tr> <td>"+ tmp.getNombre +"</td> <td>"+ tmp.getPasajerosActual +"</td> <td>"+ tmp.getNumeroIngresos +"</td> <td>"+ tmp.getNumeroSalidas +"</td> </tr>"
    }
    info = info + "</table> <br/> <br/>"
    info = info + "<table align=center> <tr> <th>Tren</th> <th>Ubicación actual</th> <th>Numero de pasajeros</th> <th>Estado</th> </tr>"
    for (tmp <- tm.getTrenes) {
      info = info + "<tr> <td>"+ tmp.getId +"</td> <td>" + (if (tmp.getEstacionActual == null) tmp.getEstacionOrigen.getNombre else tmp.getEstacionActual.getNombre) +"</td> <td>"+ tmp.getPasajerosActual +"</td> <td>"+ (if (tmp.getEstacionActual == null) ("Preparado") else (if (tmp.getEstacionActual == tmp.getEstacionDestino) ("Finalzó recorrido") else ("En curso") )) +"</td> </tr>"
    }
    info = info + "</table>"
    info
  }

}
