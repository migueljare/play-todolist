package test

import org.specs2.mutable._

import play.api.libs.json._
import play.api.test._  
import play.api.test.Helpers._

class ApplicationSpec extends Specification {

  import models._

  "Application" should {
    "creacion tarea" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val tarea1 = route(FakeRequest.apply(POST, "/tasks").withJsonBody(Json.obj(
          "id" -> 1, "label" -> "Gel"))).get

        status(tarea1) must equalTo(CREATED)
      }
    }
    "error creacion tarea" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val tarea1 = route(FakeRequest.apply(POST, "/tasks")).get

        status(tarea1) must equalTo(BAD_REQUEST)
      }
    }
    "error creacion tarea con usuario" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val tarea1 = controllers.Application.addTask("anonimo")(FakeRequest())

        status(tarea1) must equalTo(BAD_REQUEST)

      }
    }
    "creacion de una tarea con usuario" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        val tarea1 = controllers.Application.addTask("anonimo")(
          FakeRequest().withFormUrlEncodedBody("id" -> "1", "label" -> "Jabon"))
        val result = controllers.Application.tasks(FakeRequest())

        status(tarea1) must equalTo(CREATED)
        status(result) must equalTo(OK)
        //redirectLocation(result) must equalTo(Some("/tasks"))

        contentAsString(result) must contain("Jabon")
      }
    }
    "consulta tarea no disponible" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val result = controllers.Application.consultTask(1)(FakeRequest())

        status(result) must equalTo(NOT_FOUND)
      }
    }
    "consulta de una tarea disponible" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        val tarea1 = controllers.Application.addTask("anonimo")(
          FakeRequest().withFormUrlEncodedBody("id" -> "1", "label" -> "Goma"))
        val result = controllers.Application.consultTask(1)(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain("""{"id":1,"label":"Goma","user_name":"anonimo","""
          +""""task_date":"NoData"}""")
      }
    }
    "listar tareas" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val tarea1 = controllers.Application.addTask("anonimo")(
          FakeRequest().withFormUrlEncodedBody("id" -> "1", "label" -> "Goma"))
        val tarea2 = controllers.Application.addTask("anonimo")(
          FakeRequest().withFormUrlEncodedBody("id" -> "2", "label" -> "Pan"))
        val tarea3 = controllers.Application.addTask("anonimo")(
          FakeRequest().withFormUrlEncodedBody("id" -> "3", "label" -> "Leche"))
        val result = controllers.Application.tasks(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain("""[{"id":1,"label":"Goma","user_name":"anonimo","""
          +""""task_date":"NoData"},{"id":2,"label":"Pan","user_name":"anonimo","""
          +""""task_date":"NoData"},{"id":3,"label":"Leche","user_name":"anonimo","""
          +""""task_date":"NoData"}]""")
      }      
    }
    "borrar tarea que no existe" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        val result = route(FakeRequest.apply(DELETE, "/tasks/1")).get 

        status(result) must equalTo(NOT_FOUND)
        contentAsString(result) must contain("Error al borrar")
      }
    }
    "borrar tarea que existe" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        val tarea1 = controllers.Application.addTask("anonimo")(
          FakeRequest().withFormUrlEncodedBody("id" -> "1", "label" -> "Pan"))
        val tarea2 = controllers.Application.addTask("anonimo")(
          FakeRequest().withFormUrlEncodedBody("id" -> "2", "label" -> "Pera"))
        val tarea3 = controllers.Application.addTask("anonimo")(
          FakeRequest().withFormUrlEncodedBody("id" -> "3", "label" -> "Pomelo"))
        val result = controllers.Application.deleteTask(2)(FakeRequest())
        val ver = controllers.Application.tasks(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain("Borrado")
        contentAsString(ver) must contain("""[{"id":1,"label":"Pan","user_name":"anonimo","""
          +""""task_date":"NoData"},{"id":3,"label":"Pomelo","user_name":"anonimo","""
          +""""task_date":"NoData"}]""")
      }
    }
    "error crear tarea a un usuario" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      
        val tarea1 = route(FakeRequest.apply(POST, "/miguel/tasks")).get

        status(tarea1) must equalTo(BAD_REQUEST)
      }
    }
    "crear tarea a un usuario" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        val usuario1 = controllers.Application.addTask("miguel")(
          FakeRequest().withFormUrlEncodedBody("id" -> "1", "label" -> "Kiwi"))

        status(usuario1) must equalTo(CREATED)
        contentAsString(usuario1) must contain("""Kiwi""")
      }
    }
    "obtener tareas de un usuario" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        val tarea1 = route(FakeRequest.apply(POST, "/pepe/tasks").withJsonBody(Json.obj(
          "id" -> 1, "label" -> "Gel", "user_name" -> "pepe"))).get
        val tarea2 = route(FakeRequest.apply(POST, "/pepe/tasks").withJsonBody(Json.obj(
          "id" -> 2, "label" -> "Champu", "user_name" -> "pepe"))).get

        val ver = route(FakeRequest.apply(GET, "/pepe/tasks")).get

        status(ver) must equalTo(OK)
        contentAsString(ver) must contain("""[{"id":1,"label":"Gel","user_name":"pepe","""
          +""""task_date":"NoData"},{"id":2,"label":"Champu","user_name":"pepe","""
          +""""task_date":"NoData"}]""")
      }
    }
    "error tarea a un usuario inexistente" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {

        val tarea1 = route(FakeRequest.apply(POST, "/pepe/tasks").withJsonBody(Json.obj(
          "id" -> 1, "label" -> "Gel", "user_name" -> "pepe"))).get
        val tarea2 = route(FakeRequest.apply(POST, "/pepe/tasks").withJsonBody(Json.obj(
          "id" -> 2, "label" -> "Champu", "user_name" -> "pepe"))).get

        val usuario1 = route(FakeRequest.apply(GET, "/pepito/tasks")).get

        status(usuario1) must equalTo(NOT_FOUND)
        contentAsString(usuario1) must contain("Usuario no encontrado")
      }
    }
    "error tarea porque usuario no existe" in{
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
        
        val usuario1 = controllers.Application.addTask("pepito")(
          FakeRequest().withFormUrlEncodedBody("id" -> "1", "label" -> "Kiwi", "user_name"->"pepe"))

        status(usuario1) must equalTo(NOT_FOUND)
        contentAsString(usuario1) must contain("No podemos insertar tarea debido que no existe el Usuario")
      }
    }
  }   
}







