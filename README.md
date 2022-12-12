
# Neptune

An annotation-based slash command framework for JDA.

## How to Use

To start the framework, run:

```java
Neptune.start(jda, this);
```

Slash commands can take up to an hour to update globally. If you'd prefer to update a specific guild (which is instant), start Neptune like this instead:

```java
Neptune.start(jda, guild, this);
```
Or for multiple guilds:
```java
Neptune.start(jda, new Guild[]{guild1, guild2}, this);
```

Below is a working example of how to register a slash command. Use the @Command annotation and specify the command name, description and the required permissions.

Attach a method, with any name, with SlashCommandInteractionEvent as the first parameter. This holds all of the slash command event data and where you can respond to the event hook. The following parameters will define the command arguments and their order. Allowed parameter types are Integer, String, Boolean, Double, User, Role, Channel and Attachment. For an optional argument, annotate with @Optional, and ensure to null check the value when returned.

> This example will register "/ban &lt;user> [reason]".

```java
@Command(
        name = "ban",
        description = "Ban a member",
        permissions = {Permission.MANAGE_CHANNEL, Permission.ADMINISTRATOR}
)
public void onBan(SlashCommandInteractionEvent event, User user, @Optional String reason) {

}
```
Once a command is run, the method will return all values. As default slash command behaviour dictates, you will have 3 seconds to respond to the command through SlashCommandInteractionEvent. See the JDA wiki for [more info](https://github.com/DV8FromTheWorld/JDA/wiki/Interactions).

To unregister a command, simply remove any @Command reference to it and restart your Bot. It will automatically unregister globally/on the guild(s).

You can place your commands in any class within your package. It is important to **not instantiate classes which contain @Command** (except your main class, which we know exists), Neptune will do it. If you need data or outside variables in your command classes, we offer a similar system to Spring Boot. In your main class (the one you passed into Neptune#start) you can setup a variable to be accessable across your project using:

```java
@Instantiate
public TestClass testClass() { return new TestClass(); }
```
That object will then be accessible across your whole project. To use it elsewhere, create a variable with an identical name to the method and Neptune will beam the value through, as seen below:
```java
@Inject
private TestClass testClass;
```

To stop Neptune, you can run:
```java
Neptune.terminate();
```

If you struggle with anything, there is a working example in the test folder.

## Installation

Maven
```
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
repository>
```
```
<dependency>
    <groupId>com.github.sttephen</groupId>
    <artifactId>neptune</artifactId>
    <version>1.0</version>
<dependency>
```

Gradle
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
```
dependencies {
        implementation 'com.github.sttephen:neptune:1.0'
}
```

## Contributing

Contributions are always welcome. Please open a pull request and try to maintain a similar code quality and style.


## Authors

This framework was made for the team at [Virtual Ventures](https://virtualventures.io), but I decided to release it in case anyone found any use from it.



- Created by [Stephen (sttephen)](https://www.github.com/sttephen)


## License

Do what you want, I don't care. Good luck on your projects.
