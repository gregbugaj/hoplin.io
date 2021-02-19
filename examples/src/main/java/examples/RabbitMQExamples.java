package examples;


import io.hoplin.RabbitMQClient;
import io.hoplin.RabbitMQOptions;

public class RabbitMQExamples {

  public void createClientWithManualParams() {

    RabbitMQOptions config = new RabbitMQOptions();

    // Each parameter is optional

    // The default parameter with be used if the parameter is not set

    config.setUser("user1");

    config.setPassword("password1");

    config.setHost("localhost");

    config.setPort(5672);

    config.setVirtualHost("vhost1");

    config.setConnectionTimeout(6000); // in milliseconds

    config.setRequestedHeartbeat(60); // in seconds

    config.setHandshakeTimeout(6000); // in milliseconds

    config.setRequestedChannelMax(5);

    config.setNetworkRecoveryInterval(500); // in milliseconds

    config.setAutomaticRecoveryEnabled(true);

    RabbitMQClient client = RabbitMQClient.create(config);
    System.out.println(client);
  }
}
