# EventBus
A simple and effective subscription based event bus written in Java

#### What can it do?
EventBus support synchronous and asynchronous event dispatching. Events may be
cancellable. Events are dispatched to annotated subscriber methods.

#### How does it work?
EventBus used lambda meta factories to access subscriber methods without reflection, using
the [lambda-factory](https://github.com/Hervian/lambda-factory/) library. Any object can
be dispatched as an event, and a method accepting a single parameter will be able to act on
the event.

#### How to use it? 

Create an event bus:
```java
final EventBus bus = new SimpleEventBus();
```

Create a listener:
```java
public static final class TestListenerClass {
    @Listener public void onString(final String string) {
        new RuntimeException("Found a string: " + string).printStackTrace();
        indicateSuccess = true;
    }
}
```

Register the listener:
```java
bus.registerListeners(new TestListenerClass());
```

Dispatch an event:
```java
final String event = "Hello World!";
bus.throwSync(event);
```

and that is it. Events can also be dispatched asynchronously, by using `throwAsync`.

JavaDoc can be found in the [docs](https://github.com/Sauilitired/EventBus/tree/master/docs/) directory.

#### Maven
EventBus is using the JitPack maven repository.

![JitPack Badge](https://jitpack.io/v/Sauilitired/EventBus.svg)

To use it, add the following repository:
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
</repository>
```
and the following dependency:
```xml
<dependency>
    <groupId>com.github.Sauilitired</groupId>
    <artifactId>EventBus</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Contributions &amp; Contact
Contributions are very welcome! The project uses the 
[Google Java](https://google.github.io/styleguide/javaguide.html) code style. The project is licensed 
under the MIT license.

If you have any further questions or feedback, then feel free to join our [Discord](https://discord.gg/ngZCzbU).\
If the project helped you, then you are free to give me some coffee money via [PayPal](https://www.paypal.me/Sauilitired)
:coffee:
