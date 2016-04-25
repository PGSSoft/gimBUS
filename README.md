## gimBUS
Event bus for Android.

### Simple usage
Create bus instance:
```java
EventBus eventBus = new EventBus();
```
Register subscriber:
```java
Subscriber subscriber = new Subscriber();
eventBus.register(subscriber);
...
public class Subscriber {
    @Subscribe
    public void receiveEvent(Event event) {
        ...
    }
}
```
Post some event to bus so your subscribers can receive it:
```java
eventBus.post(new Event());
```
When object should no longer receive any events, it should be unregistered:
```java
eventBus.unregister(subscriber);
```

### Delivery thread
By default, every event gets delivered in the same thread that the subscriber was registered in. However, when implementing your subscriber, you can decide to receive the event in another thread, e.g. to receive event in UI thread, your subscriver's method should be annotated as follows:
```java
public class Subscriber {
    @Subscribe(EventBus.DELIVER_IN_UI_THREAD)
    public void receiveEvent(Event event) {
        ...
    }
}
```
All available delivery modes:
- DELIVER_IN_DEFAULT_THREAD - Bus will deliver the even in the same thread as object was registered in. This is the default delivery mode, chosen for compatibility with Otto,
- DELIVER_IN_UI_THREAD - Bus will deliver the event in the UI thread,
- DELIVER_IN_BACKGROUND_THREAD - Bus will deliver the event in a background thread, using either internal or external Executor,
- DELIVER_IN_DISPATCHER_THREAD - Bus will deliver the event in the dispatcher thread, the thread that is used for event dispatching.