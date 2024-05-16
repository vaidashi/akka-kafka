import org.apache.kafka.common.serialization.{
  StringDeserializer,
  StringSerializer
}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.consumer.{ ConsumerConfig }

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import akka.kafka.{
  ConsumerSettings,
  ProducerSettings,
  Subscriptions
}
import akka.kafka.ConsumerMessage.TransactionalMessage
import akka.kafka.ProducerMessage
import akka.kafka.scaladsl.{ Consumer, Transactional }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.io.StdIn

// Define an object called ProducerConsumer.
object ProducerConsumer {
  // Create an ActorSystem, which is the core of any Akka application. It's used to create and manage actors.
  implicit val system = ActorSystem(Behaviors.empty, "producerOne")
  
  // Create an ExecutionContext, which is used to execute tasks asynchronously.
  implicit val ec = ExecutionContext.Implicits.global

  // Define entry point of the program.
  def main(args: Array[String]): Unit = {
    // Check if the user provided exactly one command-line argument. If not, throw an exception.
    require(args.length == 1, "one param is required to set transaction id")

    // Get the transactional ID from the command-line argument.
    val transactionalId = args(0)

    // Set up the Kafka bootstrap server. This is the initial connection point to a Kafka cluster.
    val bootstrapServers = "127.0.0.1:9092"

    // Load the consumer configuration from the application's configuration file.
    val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")

    // Create consumer settings based on the consumer configuration.
    val consumerSettings: ConsumerSettings[String, String] = ConsumerSettings(
      consumerConfig,
      new StringDeserializer(), // Deserialize keys as strings
      new StringDeserializer()  // Deserialize values as strings
    )
      .withBootstrapServers(bootstrapServers) // Set the bootstrap servers
      .withGroupId("group01") // Set the group ID for this consumer
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest") // Reset the offset to the earliest available message

    // Load the producer configuration from the application's configuration file.
    val producerConfig = system.settings.config.getConfig("akka.kafka.producer")

    // Create producer settings based on the producer configuration.
    val producerSettings = ProducerSettings(
      producerConfig,
      new StringSerializer(), // Serialize keys as strings
      new StringSerializer()  // Serialize values as strings
    )
      .withBootstrapServers(bootstrapServers) // Set the bootstrap servers

    // Create a transactional source that consumes messages from the "test1" topic.
    val drainingControl: Consumer.DrainingControl[_] = Transactional.source(
      consumerSettings,
      Subscriptions.topics("test1") // Subscribe to the "test1" topic
    )
      // Map each consumed message to a new ProducerMessage.
      .map { msg: TransactionalMessage[String, String] =>
        ProducerMessage.single(new ProducerRecord[String, String](
          "test2", // Send the message to the "test2" topic
          msg.record.key,
          msg.record.value
        ),
        msg.partitionOffset)
      }
      // Materialize the stream to a transactional sink with the producer settings and transactional ID.
      .toMat(Transactional.sink(producerSettings, transactionalId)) (
        Consumer.DrainingControl.apply
      )
      // Run the stream.
      .run()

    // Wait for the user to press Enter before shutting down the stream.
    StdIn.readLine("Consumer started \n Press Enter to stop")

    // Drain and shut down the stream.
    val future = drainingControl.drainAndShutdown
    // When the shutdown is complete, terminate the ActorSystem.
    future.onComplete(_ => system.terminate)
  }
}
