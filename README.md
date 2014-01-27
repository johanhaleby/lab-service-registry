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

ServiceOnlineEvent
------------------

[Schema:](http://json-schema.org/)
```javascript
{
	"type":"object",
	"$schema": "http://json-schema.org/draft-03/schema",
	"id": "http://jsonschema.net",
	"required":true,
	"properties":{
		"body": {
			"type":"object",
			"id": "http://jsonschema.net/body",
			"required":true,
			"properties":{
				"createdBy": {
					"type":"string",
					"id": "http://jsonschema.net/body/createdBy",
					"required":true
				},
				"description": {
					"type":"string",
					"id": "http://jsonschema.net/body/description",
					"required":true
				},
				"serviceUrl": {
					"type":"string",
					"id": "http://jsonschema.net/body/serviceUrl",
					"required":true
				},
				"sourceUrl": {
					"type":"string",
					"id": "http://jsonschema.net/body/sourceUrl",
					"required":true
				}
			}
		},
		"createdAt": {
			"type":"number",
			"id": "http://jsonschema.net/createdAt",
			"required":true
		},
		"messageId": {
			"type":"string",
			"id": "http://jsonschema.net/messageId",
			"required":true
		},
		"meta": {
			"type":"object",
			"id": "http://jsonschema.net/meta",
			"required":false
		},
		"streamId": {
			"type":"string",
			"id": "http://jsonschema.net/streamId",
			"required":true
		},
		"type": {
			"type":"string",
			"id": "http://jsonschema.net/type",
			"required":true
		}
	}
}
```

Example:
```javascript
{
   "body":{
      "createdBy":"Johan",
      "description":"This is the description of service1",
      "sourceUrl":"http://source1.com",
      "serviceUrl":"http://someurl1.com"
   },
   "streamId":"service1",
   "createdAt":1390482313083,
   "messageId":"7f44fbf2-c7a5-43f7-b364-af6b3c5fef39",
   "type":"ServiceOnlineEvent",
   "meta":{ }
}
```

ServiceOfflineEvent
------------------

[Schema:](http://json-schema.org/)
```javascript
{
	"type":"object",
	"$schema": "http://json-schema.org/draft-03/schema",
	"id": "http://jsonschema.net",
	"required":true,
	"properties":{
		"createdAt": {
			"type":"number",
			"id": "http://jsonschema.net/createdAt",
			"required":true
		},
		"meta": {
			"type":"object",
			"id": "http://jsonschema.net/meta",
			"required":false
		},
		"streamId": {
			"type":"string",
			"id": "http://jsonschema.net/streamId",
			"required":true
		},
		"messageId": {
            "type":"string",
            "id": "http://jsonschema.net/messageId",
            "required":true
        },
		"type": {
			"type":"string",
			"id": "http://jsonschema.net/type",
			"required":true
		}
	}
}
```

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

```javascript
{
   "body":{
      "message":"My error message",
      "level":"ERROR",
      "context":"service-registry"
   },
   "createdAt":1390819324935,
   "messageId":"3561f4a6-40a6-49e7-b3ef-676d0e819644",
   "type":"LogEvent",
   "meta":{

   }
}
```

### GameCreatedEvent

```javascript
{
   "body":{
      "gameUrl":"http://rps.com/games/ultimate-rps-1",
      "gameType":"rock-paper-scissors",
      "createdBy":"player1",
      "players":[
         "player1",
         "player2"
      ]
   },
   "streamId":"ultimate-rps-1",
   "createdAt":1390820223272,
   "messageId":"b1a4e0a7-a8d9-48a1-a196-636e560fe6bb",
   "type":"GameCreatedEvent",
   "meta":{

   }
}
```

### GameEndedEvent

```javascript
{
   "body":{
      "result":"true",
      "gameType":"rock-paper-scissors",
      "scores":{
         "player1":10,
         "player2":20
      }
   },
   "streamId":"ultimate-rps-1",
   "createdAt":1390820506016,
   "messageId":"a494eef8-066b-4c1d-a645-6ef666c10e22",
   "type":"GameEndedEvent",
   "meta":{

   }
}
```