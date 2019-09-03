package io.hoplin;

/**
 * Headers exchanges route based on arguments containing headers and optional values. Routing key
 * will be ignored as it is the headers that will be the basis for the match.
 */
public class HeaderExchange extends AbstractExchange {

  public HeaderExchange(final String name) {
    super(name);
  }

  @Override
  public ExchangeType getType() {
    return ExchangeType.HEADER;
  }
}
