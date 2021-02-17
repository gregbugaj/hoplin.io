# Setup container
 ```
 docker pull rabbitmq
 ```

Start rabittmq with management console on port 8787

 ```
 docker run -d --hostname rabbit-local --name rabbit-default -p 8787:15672 -p 5672:5672 rabbitmq:latest
 ```

# Show logs
 ```
 docker logs rabbit-default 
 ```

# login to the container

 ```
  docker exec -i -t rabbit-default bash
 ```


# Config files and plugins
https://www.rabbitmq.com/installing-plugins.html

```
/etc/rabbitmq/rabbitmq.config
/etc/rabbitmq/enabled_plugins
```

Enable a plugin
```
rabbitmq-plugins enable rabbitmq_management
```

Login to management console

```
http://127.0.1.1:8787/#/
username : guest
password : guest
```

# Start already defined container

```
docker start rabbit-default
```

# Management

**Display connection clientproperties**
The client property is visible in the management console if you go look at that specific connection;

```bash
  rabbitmqctl list_connections client_properties
```


