package examples.basic;

import examples.BaseExample;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Subscriber extends BaseExample {

  public static void main(final String... args) {
    System.out.print("Enter a message. 'Quit' to quit.");
    final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());

    String in;
    while (true) {
      in = scanner.nextLine();
      if ("quit".equalsIgnoreCase(in)) {
        break;
      }

      System.out.println(in);
    }
  }
}
