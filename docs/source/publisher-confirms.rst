Publisher Confirms
==================

https://www.rabbitmq.com/confirms.html
https://rabbitmq.docs.pivotal.io/35/rabbit-web-docs/confirms.html

The default AMQP publish is not transactional and doesn't guarantee that your message will actually reach the broker.

For high-performance guaranteed delivery it's recommended that you use 'Publisher Confirms'

To enable the publisher confirms specify `publisherConfirms=true` in the connection string or set them via options.

.. code-block:: java

    "host=localhost;publisherConfirms=true;timeout=10"


The `publish` method will wait for the confirm before return. A failure to confirm before the `timeout` will cause and
exception to be thrown.

When are messages considered received

* A transient message is confirmed the moment it is endued.
* A persistent message is confirmed as soon as it is persisted to disk, or when consumed on every queue.
* An unroutable transient message is confirmed directly it is published.
