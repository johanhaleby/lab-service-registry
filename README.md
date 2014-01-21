Service Registry Example
========================

An example of a Service Registry that is easily deployed to Heroku. It using AMQP (such as RabbitMQ) to listens to service online and service offline events and registers services to MongoDB.

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

Schema:
```javascript
{
	"type":"object",
	"$schema": "http://json-schema.org/draft-03/schema",
	"id": "http://jsonschema.net",
	"required":false,
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
				"entryPoint": {
					"type":"string",
					"id": "http://jsonschema.net/body/entryPoint",
					"required":true
				},
				"name": {
					"type":"string",
					"id": "http://jsonschema.net/body/name",
					"required":true
				}
			}
		},
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
      "entryPoint":"http://someurl1.com",
      "name":"service1"
   },
   "streamId":"bfa15a72-13c0-4908-ac90-d1ad8ebc281b",
   "createdAt":1389971146921,
   "type":"ServiceOnlineEvent",
   "meta":{

   }
}
```

ServiceOnlineEvent
------------------

Schema:
```javascript
{
	"type":"object",
	"$schema": "http://json-schema.org/draft-03/schema",
	"id": "http://jsonschema.net",
	"required":false,
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
   "streamId":"bfa15a72-13c0-4908-ac90-d1ad8ebc281b",
   "meta":{

   },
   "type":"ServiceOfflineEvent"
}
```




