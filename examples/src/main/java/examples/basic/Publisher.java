package examples.basic;

import examples.BaseExample;
import java.nio.charset.Charset;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publisher extends BaseExample {

  private static final Logger log = LoggerFactory.getLogger(Publisher.class);

  public static void main(final String... args) {
    System.out.print("Enter a message. 'q' to quit.");
    final Scanner scanner = new Scanner(System.in, "UTF-8");

    String in;
    while (true) {
      in = scanner.nextLine();
      if ("quit".equalsIgnoreCase(in)) {
        break;
      }

      final TextMessage msg = new TextMessage();
      msg.setText(in);

      log.info("Publishing : {}", msg);
    }
  }
}
