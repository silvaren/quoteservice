package io.github.silvaren.quoteservice

import akka.actor.Actor
import io.github.silvaren.quotepersistence.QuotePersistence.QuoteDb
import io.github.silvaren.quotepersistence.{QuotePersistence, Serialization}
import org.joda.time.{DateTime, DateTimeZone}
import spray.http.MediaTypes._
import spray.routing._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

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

  override def preStart(): Unit = {
    super.preStart()
    println("Connecting to mongo quotedb...")
    val quoteDbF = QuotePersistence.connectToQuoteDb(Boot.parameters.dbConfig)
    quoteDbF.foreach(q => quoteDb = Some(q))
    Await.result(quoteDbF, Duration.Inf)
  }

  override def postStop(): Unit = {
    super.postStop()
    println("Disonnecting from mongo quotedb...")
    quoteDb.foreach(QuotePersistence.disconnectFromQuoteDb(_))
  }
}


// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {

  var quoteDb: Option[QuoteDb] = None

  def retrieveQuotes(db: QuoteDb) = {
    val initialDate = new DateTime().withZone(DateTimeZone.forID("America/Sao_Paulo"))
      .withYear(2015)
      .withMonthOfYear(11)
      .withDayOfMonth(1)
      .withHourOfDay(0)
      .withMinuteOfHour(0)
      .withSecondOfMinute(0)
      .withMillisOfSecond(0)
    val quotesPromise = QuotePersistence.retrieveQuotes("PETR4", initialDate, db)
    onComplete(quotesPromise){
      case Success(quotes) => complete(Serialization.gson.toJson(quotes.toArray))
      case Failure(t) => complete(t.getMessage)
    }
  }

  val myRoute =
    path("") {
      dynamic {
        get {
          respondWithMediaType(`text/html`) {
            // XML is marshalled to `text/xml` by default, so we simply override here

            quoteDb match {
              case Some(db) => retrieveQuotes(db)
              case None => complete("Not connected to quotedb!")
            }

          }
        }
      }
    }
}