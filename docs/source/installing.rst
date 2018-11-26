Installation
============


Installing RabbitMQ in Docker
-----------------------------
We are going to setup RabittMQ in a Docker container.



Installing Docker on Ubuntu 18
++++++++++++++++++++++++++++++

So first step is to make sure we have docker up and running.

.. code-block:: bash

    sudo apt install apt-transport-https ca-certificates curl software-properties-common
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
    sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu bionic stable"
    sudo apt update
    sudo apt install docker-ce


Add current user to the docker group

.. code-block:: bash

    sudo usermod -a -G docker $USER

Logging out and logging back in is required because the group change will not have an effect unless your session is closed.
Otherwise you will receive  following error.

.. code-block:: bash

    docker: Got permission denied while trying to connect to the Docker daemon socket at unix:///var/run/docker.sock: Post http://%2Fvar%2Frun%2Fdocker.sock/v1.26/containers/create: dial unix /var/run/docker.sock: connect: permission denied.
    See 'docker run --help'.

The error message tells you that your current user can’t access the docker engine


Installing RabbitMQ
+++++++++++++++++++

Setup container, this will pull the latest RabbitMQ container.

.. code-block:: bash

    docker pull rabbitmq

Start RabittMQ with management console exposed on port 8787

.. code-block:: bash

    docker run -d --hostname rabbit-local --name rabbit-default -p 8787:15672 -p 5672:5672 rabbitmq:latest

Show logs from the started container

.. code-block:: bash

    docker logs rabbit-default


Login to the container

.. code-block:: bash

    docker logs rabbit-default


Config files and plugins https://www.rabbitmq.com/installing-plugins.html

.. code-block:: bash

    /etc/rabbitmq/rabbitmq.config
    /etc/rabbitmq/enabled_plugins

Enable a plugin for rabbitmq managements

.. code-block:: bash

    rabbitmq-plugins enable rabbitmq_management


Login to management console

.. code-block:: bash

    http://127.0.1.1:8787/#/
    username : guest
    password : guest


Start already defined container

.. code-block:: bash

    docker start rabbit-default

Adding dependency to your project
----------------------------------

.. code-block:: xml

    <groupId>io.hoplin</groupId>
    <artifactId>hoplin-client</artifactId>
    <version>1.0-SNAPSHOT</version>

