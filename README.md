Service Registry Example
========================

An example of a Service Registry that is easily deployed to Heroku. It uses AMQP (such as RabbitMQ) to listen to service online and service offline events and registers services to MongoDB.
The topic exchange to send messages to is called `lab` and routing key is `service`.


Prerequisites
-------------
If you want to run the program locally you need:

1. Java 7
2. Maven 3
3. MongoDB (`brew install mongodb`)
4. RabbitMQ (`brew install rabbitmq`)

Deployment
----------
The project contains Heroku configurations in the files `system.properties` and `Procfile`. However you need to [add two properties](https://toolbelt.heroku.com/) pointing out the AMQP and MongoDB URL's.

```bash
heroku config:set AMQP_URL="<amqp_url>"
heroku config:set MONGOHQ_URL="<mongodb_url>"
```

Both URLs must contain username and password, port etc.
