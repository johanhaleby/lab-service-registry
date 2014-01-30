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

Messages
--------
All messages requires the following message properties:

<table>
    <th>Property</th>
    <th>Description</th>
    <tr>
        <td>appId</td>
        <td>The application id, for example "service-registry"</td>
    </tr>
    <tr>
        <td>streamId</td>
        <td>The id of the stream (aggregate id). May be equal to appId in some cases or a UUID.</td>
    </tr>
    <tr>
        <td>timestamp</td>
        <td>The timestamp indicating when the message was created</td>
    </tr>
    <tr>
        <td>messageId</td>
        <td>The id to uniquely define a particular message (UUID). Used for idempotency.</td>
    </tr>
    <tr>
        <td>type</td>
        <td>The type of the event, for example "ServiceOnlineEvent"</td>
    </tr>
</table>

It's ok to add additional message properties.

Different messages have different body properties that are required. It's ok to add additional properties to the body as well if needed.

Messages consumed by Service Registry
-------------------------------------

### ServiceOnlineEvent
Note that appId and streamId will be the same this event

Example:
```javascript
{
  "createdBy":"Johan",
  "description":"This is the description of service1",
  "sourceUrl":"http://source1.com",
  "serviceUrl":"http://someurl1.com"
}
```

### ServiceOfflineEvent

Example:
```javascript
{
   "createdAt":1390290351389,
   "streamId":"service1",
   "meta":{ },
   "messageId":"7f44fbf2-c7a5-43f7-b364-af62335fef39",
   "type":"ServiceOfflineEvent"
}
```

Other Events
------------
Other events declared in Lab but not consumed by Service Registry.

### LogEvent

Example:
```javascript
{
  "message":"My error message",
  "level":"ERROR",
}
```

### GameCreatedEvent

Example:
```javascript
{
  "gameUrl":"http://rps.com/games/ultimate-rps-1",
  "gameType":"rock-paper-scissors",
  "createdBy":"player1",
  "players":[
     "player1",
     "player2"
  ]
}
```

### GameEndedEvent

Example:
```javascript
{
  "gameType":"rock-paper-scissors",
  "scores":{
     "player1":10,
     "player2":20
  }
}
```