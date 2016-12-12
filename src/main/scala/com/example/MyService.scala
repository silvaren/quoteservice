package com.example

import akka.actor.Actor
import spray.routing._
import spray.http.MediaTypes._
import io.github.silvaren.quotepersistence.{QuotePersistence, Serialization}
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  val myRoute =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
        val initialDate = new DateTime().withZone(DateTimeZone.forID("America/Sao_Paulo"))
          .withYear(2015)
          .withMonthOfYear(11)
          .withDayOfMonth(1)
          .withHourOfDay(0)
          .withMinuteOfHour(0)
          .withSecondOfMinute(0)
          .withMillisOfSecond(0)

          val quoteDb = QuotePersistence.connectToQuoteDb(Boot.parameters.dbConfig)
          val quotesPromise = QuotePersistence.retrieveQuotes("PETR4", initialDate, quoteDb)
          onComplete(quotesPromise.future){
            case Success(quotes) => {println(Serialization.gson.toJson(quotes));complete(Serialization.gson.toJson(quotes))}
            case Failure(t) => complete(t.getMessage)
          }
        }
      }
    }
}