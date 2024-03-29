
# Neptune

An annotation-based slash command framework for JDA.

For questions, support or to chat with the team, come join the Discord:

[![Discord Banner](https://discordapp.com/api/guilds/835561528299880518/widget.png?style=banner2)](https://discord.gg/flyte)

## How to Use

You can create an instance of Neptune using the Builder, this allows you to control some of Neptune's functionality. Below is an example of how you can use the Builder:
```java
new Neptune.Builder(jda, this)
        .addGuilds(guild1, guild2)
        .clearCommands(true),
        .registerAllListeners(true)
        .create();
```

*Note: If you do not specify any guilds, commands will be registered/unregistered globally (this can take up to 2 hours due to a Discord limitation).*

## Commands
Below is a working example of how to register a slash command. Use the `@Command` annotation and specify the command name, description and the required permissions.

Annotate a method with `@Command` (the method can be called anything). The first parameter must be the SlashCommandInteractionEvent, from there you can list your arguments/options as parameters like shown below.

> This example will register "/ban &lt;user> [reason]".

```java
@Command(
        name = "ban",
        description = "Ban a member",
        permissions = {Permission.MANAGE_CHANNEL, Permission.ADMINISTRATOR}
)
public void onBan(SlashCommandInteractionEvent event, @Description("The user you wish to mute") User user, @Optional String reason) {

}
```
Parameters can be annotated with the following:
* `@Optional` - Makes the argument not required
* `@Description` - Allows you to specify a description for the argument/option (if not specified will be name of argument)

Once a command is run, the method will return all values. As default slash command behaviour dictates, you will have 3 seconds to respond to the command through SlashCommandInteractionEvent. See the JDA wiki for [more info](https://github.com/DV8FromTheWorld/JDA/wiki/Interactions).

If you want to unregister commands, add the `clearCommands` to the Builder as `true`, and remove the `@Command` annotation from your command.

## Listeners

If you choose it in the builder as shown above, then Neptune will register all of your listener classes (those which extend ListenerAdapter).


## Injection/Instantiation
Due to limitations beyond our control, you **cannot create your own instance of a class which Neptune has**. Neptune will manage instances of all classes which contain its @Command annotation, its @Inject annotation and all ListenerAdapter subclasses (if you have enabled this on startup, otherwise just those that use the aforementioned annotations).

That begs the question: how do you use variables from within a command/listener without being able to pass them through a constructor? Well, Neptune offers a similar system to Spring Boot.

In your main class (the one you started Neptune in) you can setup a variable to be accessible across your project by annotating either a method or field, as shown below:

```java
@Instantiate
public TestClass testClass() {
    return new TestClass();
}
// or
@Instantiate
private final TestClass testClass = new TestClass();
```
That object will then be accessible across your whole project. To use it elsewhere, create a variable with an identical name to the method and Neptune will beam the value through, as seen below:
```java
@Inject
private TestClass testClass;
```

## Terminating

Terminating Neptune will prevent all registered commands from running. To do this, you can run:
```java
Neptune#terminate();
```

If you struggle with anything, there is a working example in the test folder.

## Installation

**The latest version can be found in the releases tab on the right.**

Maven
```xml
<repository>
    <id>flyte-repository-releases</id>
    <name>Flyte Repository</name>
    <url>https://repo.flyte.gg/releases</url>
</repository>
```  
```xml
<dependency>
    <groupId>gg.flyte</groupId>
    <artifactId>neptune</artifactId>
    <version>VERSION</version>
</dependency>
```

Gradle (Kotlin DSL)
```kt
maven {
    name = "flyteRepositoryReleases"
    url = uri("https://repo.flyte.gg/releases")
}
        
implementation("gg.flyte:neptune:VERSION")
```  

## Contributing

Contributions are always welcome. Please open a pull request and try to maintain a similar code quality and style.


## Authors

This framework was made for the team at [Flyte](https://flyte.gg), but we decided to release it in case anyone found any use from it.



- Created by [Stephen (sttephen)](https://github.com/sttephen) & [Josh (joshbker)](https://github.com/joshbker)
