# Introduction

The purpose of this lab is to get experience developing microservices communicating using messaging. The domain is a website for multiplayer games. Examples of services:

* Game logic
* Leaderboards & statistics
* Tournaments
* AI players
* Lobby
* Achievements

To make it interesting all services should publish messages on the same message bus where we have defined a number of topics. We have also defined schemas for the basic messages that must be supported. Your service may extend the schema and add new messages. However, you are NOT allowed to create new topics.

### Service?

A *service* is defined as a selfcontained application that is versioned, released and deployed separately from everything else. It also owns its data, that is it can use whatever database and schema it wants. No other service will be using another service database directly. There are two ways to communicate with our services:

* Using messaging (publish/subscribe)
* Using HTTP

### Micro?

We interpret the word micro in *microservices* to mean that the services should be as small as possible and do only one thing. Contrast this against the traditional monolithic approach where an application contains many functions. A monolothic game would typically include both a lobby for creating and joining games, the game logic, leaderboards, AI players etc. In this lab we will break these things into separate services. A really really small service is of course simpler to understand and therefore hopefully less work to develop and maintain. However, there will also be some overhead since services must communicate with eachother using an API. Designing and implementing these API will require more work than simple method calls. We believe that the benefits outway this overhead.

## Topics

The following topics have been defined:

### service

Messages containing the status of a specific service such as the service going online or simple logging.

### game

Messages relating to a specific game.

# Messages

All messages requires the following message properties (not defined in the body but rather as meta-data attached to each message):

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

Messages also have a body which are different for each message. It's ok to add additional properties to the body as well if needed.

## Service messages

### ServiceOnlineEvent
Note that appId and streamId will be the same for this event.

Use `streamId` to specify the unique name of the service, must be human readable (no UUID).

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

Body is empty. Use `streamId` to specify which service to unregister.

### LogEvent

`streamId` is a unique identifier for the node that is sending the log message.

Example:
```javascript
{
  "message":"My error message",
  "level":"ERROR",
}
```

## Game messages

`streamId` is the game identifier which must be a UUID! Use `appId` to determine what kind of game it is, for example `rock-paper-scissors`.

### GameCreatedEvent

Example:
```javascript
{
  "gameUrl":"http://rps.com/games/ultimate-rps-1",
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
  "scores":{
     "player1":10,
     "player2":20
  }
}
```