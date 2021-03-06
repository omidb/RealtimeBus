package com.example.hello.impl

import com.example.hello.api.{GreetingMessage, HelloService}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.Future


/**
  * Created by knoldus on 16/2/17.
  */
/**
  * Implementation of the HelloService.
  */
class HelloServiceImpl(persistentEntityRegistry: PersistentEntityRegistry) extends HelloService {

  override def hello(id: String) = ServiceCall { _ =>
    // Look up the Hello entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HelloEntity](id)

    // Ask the entity the Hello command.
    ref.ask(Hello(id, None))
  }

  override def useGreeting(id: String) = ServiceCall { request =>

    println(s"I got a greeting message with id $id and message ${request.message}")
    // Look up the Hello entity for the given ID.
    val ref = persistentEntityRegistry.refFor[HelloEntity](id)

    // Tell the entity to use the greeting message specified.
    ref.ask(UseGreetingMessage(request.message, request.sendTo))
  }

  override def greetingsTopic(): Topic[GreetingMessage] = {
//    TopicProducer.singleStreamWithOffset{
//      offset => o
//    }
//

    TopicProducer.singleStreamWithOffset {
      offset =>
        persistentEntityRegistry.eventStream(HelloEventTag.instance, offset)
          .map(ev => (convertEvent(ev), offset))
    }
  }

  private def convertEvent(helloEvent: EventStreamElement[HelloEvent]): GreetingMessage = {
    println(s"converting event ${helloEvent.event}")
    helloEvent.event match {
      case GreetingMessageChanged(msg, users) => GreetingMessage(msg, users)
    }
  }

}