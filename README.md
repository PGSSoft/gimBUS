![gimBUS](gimbus_logo.png "gimBUS")


## gimBUS [![Build Status](https://travis-ci.org/PGSSoft/gimBUS.svg?branch=master)](https://travis-ci.org/PGSSoft/gimBUS)
Event bus for Android with fine-grained thread control.

### Why not use Otto by Square?

gimBUS has better delivery thread control, scroll down for details. gimBUS is based on Guava too, so transition from Otto is easy. Also - Otto is now deprecated in favor of Rx approach.

### How to use gimBUS?

* import library from Gradle
```
compile 'com.pgs-soft:gimbus:1.1.0'
```

* Create bus instance (prefferably as singleton, i.e. within Application.java sub-class):
```java
EventBus eventBus = new EventBus();
```
* Register any object as subscriber, mark receiver methods with `@Subscribe` annotation:
```java
someMethod(){
    ...
    eventBus.register(this);
    ...
}

@Subscribe
public void receiveEvent(Event event) {
        // any posted Event instances will be delivered here
        ...
    }
}
```

* Post some event to bus so your subscribers can receive it:
```java
eventBus.post(new Event());
```

* When object should no longer receive any events, it should be unregistered:
```java
eventBus.unregister(this);
```

### Delivery thread
By default, every event gets delivered in the same thread that the subscriber was registered in. However, when implementing your subscriber, you can decide to receive the event in another thread, e.g. to receive event in UI thread, your subscriber's method should be annotated as follows:
```java
public class Subscriber {
    @Subscribe(EventBus.DELIVER_IN_UI_THREAD)
    public void receiveEvent(Event event) {
        ...
    }
}
```
Available delivery modes:
- `DELIVER_IN_DEFAULT_THREAD` - Bus will deliver the even in the same thread as object was registered in. This is the default delivery mode, chosen for compatibility with Otto,
- `DELIVER_IN_UI_THREAD` - Bus will deliver the event in the UI thread,
- `DELIVER_IN_BACKGROUND_THREAD` - Bus will deliver the event in a background thread, using either internal or external Executor,
- `DELIVER_IN_DISPATCHER_THREAD` - Bus will deliver the event in the dispatcher thread, the thread that is used for event dispatching. 

### What is this _gimbus_?
In Poland - it is a colloquial name of [school bus](https://en.wikipedia.org/wiki/School_bus#Poland).

### Authors ###
* Łukasz Płomiński (architecture and development)
* Bartosz Stokrocki (maven, travis, repository)

### License ###
The MIT License (MIT)

Copyright (c) 2016 PGS Software

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
