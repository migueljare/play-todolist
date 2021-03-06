package test

import org.specs2.mutable._  
import play.api.test._  
import play.api.test.Helpers._

class ModelSpec extends Specification {

    import models._

    def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str  
    def strToDate(str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)
    def dateToStr(date: java.util.Date) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date)

    "Models" should {
        "creacion tarea modelo" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.create("Arroz")
                val Some(ver) = Task.findTarea(1)

                ver must equalTo("Arroz")
            }
        }
        "consulta tarea modelo" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.create("Uva")
                Task.create("Naranja")
                Task.create("Limon")

                val ver1 = Task.consult(1)
                val ver2 = Task.consult(2)
                val ver3 = Task.consult(3)

                ver1.label must equalTo("Uva")
                ver2.label must equalTo("Naranja")
                ver3.label must equalTo("Limon")
            }
        }
        "listado tareas modelo" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.create("Cereza")
                Task.create("Melon")
                Task.create("Sandia")

                val todos = Task.all()
                val task1 = todos.head
                val task2 = todos.tail.head
                val task3 = todos.tail.tail.head

                task1.label must equalTo("Cereza")
                task2.label must equalTo("Melon")
                task3.label must equalTo("Sandia")
            }
        }
        "borrado tarea modelo" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.create("Harina")
                Task.create("Queso")

                val delete = Task.delete(2)

                delete must equalTo(1) //si >0 es que lo ha borrado
            }
        }
        "crear tarea usuario modelo" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.createInUser("pepe","agua")
                Task.createInUser("pepe","vino")
                val todos = Task.all("pepe")
                val task1 = todos.head
                val task2 = todos.tail.head

                task1.label must equalTo("agua")
                task2.label must equalTo("vino")
            }
        }
        "crear fecha tarea" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                val fecha = strToDate("1992-05-24")
                Task.create("Vodka")
                val crear = Task.createFecha(1, fecha)

                crear must equalTo(1)
            }
        }
        "obtener fecha tarea" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                val fecha = strToDate("1992-05-25")
                Task.create("Vodka")
                val crear = Task.createFecha(1, fecha)
                val Some(obtener) = Task.obtFecha(1)
                val fechaInv = dateToStr(obtener)
                val igual = dateIs(fecha, fechaInv)

                igual mustEqual true
                fechaInv must equalTo("1992-05-25")
            }
        }
        "crear usuario y buscarlo" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.createUser("federico")
                val Some(usuario) = Task.findUser("federico")

                usuario must equalTo("federico")
            }
        }
        "buscar usuario creado" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                val Some(usuario) = Task.findUser("miguel")

                usuario must equalTo("miguel")
            }
        }
        "buscar tarea" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.create("Cotizar")
                val Some(tarea) = Task.findTarea(1)

                tarea must equalTo("Cotizar")
            }
        }
        "registrar categoria de una tarea sin usuario modelo" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.create("Naranja")
                Task.create("Limon")
                Task.create("Zanahoria")
                val creado1 = Task.createCategory(1,"fruta")
                val creado2 = Task.createCategory(2,"fruta")
                val ver = Task.all()
                val pieza1 = ver.head
                val pieza2 = ver.tail.head
                val pieza3 = ver.tail.tail.head
                val Some(categoria) = pieza1.category
                val Some(categoria2) = pieza2.category
                val Some(categoria3) = pieza3.category

                creado1 must equalTo(1)
                creado2 must equalTo(1)
                categoria must equalTo("fruta")
                categoria2 must equalTo("fruta")
                categoria3 must equalTo("descatalogado")
            }
        }
        "registrar categoria de una tarea con usuario modelo" in {
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.create("Naranja")
                Task.create("Limon")
                Task.create("Zanahoria")
                val creado1 = Task.createUserCategory(1,"anonimo","fruta")
                val creado2 = Task.createUserCategory(2,"anonimo","fruta")
                val ver = Task.all()
                val pieza1 = ver.head
                val pieza2 = ver.tail.head
                val pieza3 = ver.tail.tail.head
                val Some(categoria) = pieza1.category
                val Some(categoria2) = pieza2.category
                val Some(categoria3) = pieza3.category

                creado1 must equalTo(1)
                creado2 must equalTo(1)
                categoria must equalTo("fruta")
                categoria2 must equalTo("fruta")
                categoria3 must equalTo("descatalogado")
                pieza1.user_name must equalTo("anonimo")
                pieza2.user_name must equalTo("anonimo")
                pieza3.user_name must equalTo("anonimo")
            }
        }
        "listar tareas de un usuario en categoria modelo" in{
            running(FakeApplication(additionalConfiguration = inMemoryDatabase())){

                Task.create("Naranja")
                Task.create("Limon")
                Task.create("Zanahoria")
                Task.createUserCategory(1,"anonimo","fruta")
                Task.createUserCategory(2,"anonimo","fruta")
                Task.createCategory(3,"hortaliza")
                val creado1 = Task.getUserCategory("anonimo","fruta")
                val creado2 = Task.getCategory("hortaliza")
                val pieza1 = creado1.head
                val pieza2 = creado1.tail.head
                val pieza3 = creado2.head
                val Some(categoria) = pieza1.category
                val Some(categoria2) = pieza2.category
                val Some(categoria3) = pieza3.category

                categoria must equalTo("fruta")
                categoria2 must equalTo("fruta")
                categoria3 must equalTo("hortaliza")
                pieza1.user_name must equalTo("anonimo")
                pieza2.user_name must equalTo("anonimo")
                pieza3.user_name must equalTo("anonimo")
            }
        }
    }  
}