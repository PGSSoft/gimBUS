/*
 * Copyright (C) 2016 Arivald
 * Copyright (C) 2012 Square, Inc.
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.pgssoft.gimbus;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dispatches events to subscribers handlers, and provides ways for subscribers to register themselves.
 * <p/>
 * <p>The EventBus allows publish-subscribe-style communication between components without requiring
 * the components to explicitly register with one another (and thus be aware of each other).
 * It is <em>not</em> intended for interprocess communication.
 * <p/>
 * <h2>Receiving Events</h2>
 * <p/>
 * <p>To receive events, an object should:
 * <ol>
 * <li>Expose a method, known as the <i>event handler</i>, which accepts a single argument
 * of the type of event desired;
 * <li>Mark it with a {@link Subscribe} annotation;
 * <li>Pass itself to an EventBus instance's {@link #register(Object)} method.
 * </ol>
 * <p/>
 * <h2>Posting Events</h2>
 * <p/>
 * <p>To post an event, simply provide the event object to the {@link #post(Object)} method, or
 * the {@link #send(Object)} method, or any of its variants. The EventBus instance will determine the
 * type of event and route it to all registered subscribers.
 * <p/>
 * <p>Events are routed based on their type &mdash; an event will be delivered to any subscriber for
 * any type to which the event is <em>assignable</em>. This includes implemented interfaces, all
 * superclasses, and all interfaces implemented by superclasses.
 * <p/>
 * <h2>Event Handlers</h2>
 * <p/>
 * <p>Event handler methods must accept only one argument: the event.
 * <p/>
 * <p>Subscribers should not, in general, throw. If they do, the EventBus will TODO catch and log the
 * exception. This is rarely the right solution for error handling and should not be relied upon; it
 * is intended solely to help find problems during development.
 * <p/>
 * <h2>Dead Events</h2>
 * <p>If an event is posted, but no registered subscribers can accept it, it is considered "dead."
 * To give the system a second chance to handle dead events, they are wrapped in an instance of
 * {@link DeadEvent} and reposted.
 * <p/>
 * <p>This class is safe for concurrent use.
 *
 * @author Arivald (Android EventBus code)
 * @author Cliff Biffle (Guava inherited code)
 */
@SuppressWarnings("unused")
public class EventBus {

//todo add sticky events
//sticky event is and event that when posted stays in bus until some conditions are meet, ex timeout, or it is removed explicitly.
//there should be only one sticky event for event class, new one replaces old one.
//sticky events should be delivered to subscriber just after he registered in bus

    /**
     * Bus will deliver the even in the same thread as object was registered in.
     * Object can always have assigned different default thread, see assignThreadForSubscriber().
     * <p/>
     * This is the default delivery mode, chosen for compatibility with Otto.
     * <p/>
     * In many cases this is close equivalent to the DELIVER_IN_UI_THREAD, id You registered subscriber
     * in the UI thread;
     * <p/>
     * The thread have to have a Looper. If there is no Looper, effectively it will work
     * like DELIVER_IN_BACKGROUND_THREAD.
     */
    public static final int DELIVER_IN_DEFAULT_THREAD = 0;

    /**
     * Bus will deliver the event in the UI thread
     * .
     * Use this for events that have to update UI.
     * Avoid for events that may execute longer, have to do some processing.
     */
    public static final int DELIVER_IN_UI_THREAD = 1;

    /**
     * Bus will deliver the event in a background thread, using either internal or external Executor.
     * <p/>
     * Use this for events that have to do processing, and does not access UI.
     */
    public static final int DELIVER_IN_BACKGROUND_THREAD = 2;

    /**
     * Bus will deliver the event in the dispatcher thread, the thread that is used for event dispatching.
     * <p/>
     * This is the most efficient delivery mode, but have drawback of blocking the delivery thread.
     * In case of EventBus.sendXXX() methods, where the dispatch is executed in sender thread, the
     * event handler will be executed in this thread, before sendXXX() returns.
     * <p/>
     * Use it only for very short handlers, for example to update few variables, or to generate other
     * events, or to start some async job, or to start the network call (that have its own thread).
     */
    public static final int DELIVER_IN_DISPATCHER_THREAD = 3;

    @IntDef({DELIVER_IN_DEFAULT_THREAD, DELIVER_IN_UI_THREAD, DELIVER_IN_BACKGROUND_THREAD, DELIVER_IN_DISPATCHER_THREAD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DeliveryThread {
    }


    /**
     * Creates a new EventBus named "default".
     * Will use default background threads executor, will share threads and pools with other instances
     */
    public EventBus() {
        this("default", null);
    }

    /**
     * Creates a new EventBus with the given {@code identifier} and Executor.
     *
     * @param identifier a brief identifier for this bus, for debugging purposes.
     * @param executor   executor to manage background threads. Pass null to use internal one.
     */
    public EventBus(@NonNull String identifier, @Nullable Executor executor) {
        mIdentifier = identifier;
        mDispatcherThread = createDispatcherThread();
        mBackgroundExecutor = executor != null ? executor : getSharedExecutor();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // API

    /**
     * Call to register all event handlers for subscriber.
     */
    public void register(@NonNull Object subscriber) {
        assignThreadForSubscriber(subscriber);

        //Key: the event class to handle
        //Value: set of event handlers that can handle this event class.
        for (Map.Entry<Class<?>, List<EventHandler>> entry : Cache.findAllEventHandlersForSubscriber(subscriber).entrySet()) {
            Class<?> eventType = entry.getKey();
            CopyOnWriteArraySet<EventHandler> registeredEventHandlersForEventType = mRegisteredEventHandlersByEventType.get(eventType);
            //If there is no Set of EventHandlers for this type of event, we have to create one. But it must be safe, no race condition, thus synchronized().
            if (registeredEventHandlersForEventType == null) {
                synchronized (mRegisteredEventHandlersByEventType) {
                    //check again, maybe other thread managed to add the Set already, while this one waied for the synchronization
                    registeredEventHandlersForEventType = mRegisteredEventHandlersByEventType.get(eventType);
                    if (registeredEventHandlersForEventType == null) {
                        registeredEventHandlersForEventType = new CopyOnWriteArraySet<>();
                        mRegisteredEventHandlersByEventType.put(eventType, registeredEventHandlersForEventType);
                    }
                }
            }
            //finally, add new event handlers to the registered handlers
            registeredEventHandlersForEventType.addAll(entry.getValue());

            Object stickyEvent = Cache.stickyEvents.get(entry.getKey());
            if (stickyEvent != null) {
                sendTo(stickyEvent, subscriber);
            }
        }
    }

    /**
     * Unregister all event handler methods for a subscriber.
     * By the way this method also removes all subscribers that was already garbage collected.
     *
     * @param subscriber a @Nullable object whose event handlers methods should be unregistered.
     *                   Pass null to remove old, already garbage collected objects.
     */
    public void unregister(@Nullable Object subscriber) {
        List<EventHandler> eventHandlersToRemove = new ArrayList<>();
        //NOTE: mRegisteredEventHandlersByEventType is a concurrent map, reads are permitted without synchronisation.
        //this method does not modify the mRegisteredEventHandlersByEventType, it modify only its values, sets of eventHandlers.
        for (CopyOnWriteArraySet<EventHandler> eventHandlers : mRegisteredEventHandlersByEventType.values()) {
            for (EventHandler eventHandler : eventHandlers) {
                Object eventHandlerSubscriber = eventHandler.mSubscriber.get();
                //Note: if the eventHandlerSubscriber is null, it means that object was GCed,
                //so it should be unregistered too.
                if (eventHandlerSubscriber == null || eventHandlerSubscriber == subscriber) {
                    //Note: the eventHandlers is a CopyOnWriteArraySet, it is much better performance-wise
                    //to remove all handlers in one steep, at the end of loop.
                    eventHandlersToRemove.add(eventHandler);
                }
            }
            eventHandlers.removeAll(eventHandlersToRemove);
            eventHandlersToRemove.clear();
        }


        //remove related default thread handler
        for (IdentityWeakReferenceKey<Object> key : mSubscribersDefaultThreads.keySet()) {
            Object ref = key.get();
            //Note: if the ref is null, it means that object was GCed,
            //so it should be removed too.
            if (ref == null || ref == subscriber) {
                mSubscribersDefaultThreads.remove(key);
            }
        }
    }

    /**
     * Assign current thread to the subscriber object.
     * To make it work, the thread have to have a Looper (Looper.myLooper() != null).
     * If current thread have no Looper, all event handlers marked with DELIVER_IN_DEFAULT_THREAD,
     * it will be executed as DELIVER_IN_BACKGROUND_THREAD.
     * <p/>
     * This method can be used to re-assign the default thread for already registered object.
     *
     * @param subscriber @NonNull a subscriber object to assign thread for.
     */
    public void assignThreadForSubscriber(@NonNull Object subscriber) {
        Looper looper = Looper.myLooper();

        if (looper != null) {
            //Assumption: this is called always to change thread, so no check for old value.
            mSubscribersDefaultThreads.put(
                    new IdentityWeakReferenceKey<>(subscriber),
                    looper != Looper.getMainLooper() ? new Handler(looper) : mUiThreadHandler
            );
        } else {
            mSubscribersDefaultThreads.remove(new IdentityWeakReferenceKey<>(subscriber));
        }
    }


    /**
     * Posts an event to all registered subscribers.
     * The dispatch code will be executed in the separated dispatch thread, method will return immediately.
     * <p/>
     * Generally PostXxx() methods are less performance-friendly than sendXxx() methods, but have
     * advantage od constant, fast eecution time.
     * Use this method in cases when Your thread is already busy, or You can not afford ags, like in the UI thread.
     * <p/>
     * If no subscribers have been subscribed for {@code event}'s class, and {@code event} is not already a
     * {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
     *
     * @param event @NonNull event to post.
     * @throws NullPointerException if the event is null.
     */
    public void post(@NonNull final Object event) {
        mDispatcherThread.post(new Dispatcher(this, event, null));
    }

    /**
     * Posts an event to one specific subscriber object.
     * The dispatch code will be executed in the separated dispatch thread, method will return immediately.
     * <p/>
     * Generally PostXxx() methods are less performance-friendly than sendXxx() methods, but have
     * advantage od constant, fast eecution time.
     * Use this method in cases when Your thread is already busy, or You can not afford ags, like in the UI thread.
     * <p/>
     * If no subscribers have been subscribed for {@code event}'s class, and {@code event} is not already a
     * {@link DeadEvent}, it will be wrapped in a DeadEvent and re-posted.
     *
     * @param event      @NonNull event to post.
     * @param subscriber @NonNull subscriber to deliver event to. Subscriber must be registered in the event bus already.
     * @throws NullPointerException if the event is null.
     */
    public void postTo(@NonNull final Object event, @NonNull Object subscriber) {
        mDispatcherThread.post(new Dispatcher(this, event, subscriber));
    }

    /**
     * Posts an event to all registered subscribers, with delay. Subscriber must be registered
     * after requested time passes.
     * The dispatch code will be executed in the separated dispatch thread, method will return immediately.
     * <p/>
     * If no subscribers have been subscribed for {@code event}'s class, and {@code event} is not already a
     * {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
     *
     * @param event        @NonNull event to post.
     * @param milliseconds delay in milliseconds
     * @throws NullPointerException if the event is null.
     */
    public void postDelayed(@NonNull final Object event, long milliseconds) {
        mDispatcherThread.postDelayed(new Dispatcher(this, event, null), milliseconds);
    }

    /**
     * Posts an event to one specific subscriber object, with delay. Subscriber must be registered
     * after requested time passes.
     * The dispatch code will be executed in the separated dispatch thread, method will return immediately.
     * <p/>
     * If no subscribers have been subscribed for {@code event}'s class, and {@code event} is not already a
     * {@link DeadEvent}, it will be wrapped in a DeadEvent and re-posted.
     *
     * @param event        @NonNull event to post.
     * @param subscriber   @NonNull subscriber to deliver event to. Subscriber must be registered in the event bus already.
     * @param milliseconds delay in milliseconds
     * @throws NullPointerException if the event is null.
     */
    public void postToDelayed(@NonNull final Object event, @NonNull Object subscriber, long milliseconds) {
        mDispatcherThread.postDelayed(new Dispatcher(this, event, subscriber), milliseconds);
    }

    /**
     * Send an event to all registered subscribers, dispatching it in current thread, before method returns.
     * Note: All subscribers with DELIVER_IN_DISPATCHER_THREAD will also be processed in same thread,
     * before method returns.
     * <p/>
     * Generally sendXxx() methods are more performance-friendly than postXxx() methods.
     * Use this method in cases when you are not concerned about how long the dispatch will take.
     * Avoid using sendXxx() and prefer postXxx() in UI thread, as there is no guarantee how long it may take.
     * <p/>
     * If no subscribers have been subscribed for {@code event}'s class, and {@code event} is not already a
     * {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
     *
     * @param event @NonNull event to post.
     * @throws NullPointerException if the event is null.
     */
    public void send(@NonNull final Object event) {
        new Dispatcher(this, event, null).run();
    }

    /**
     * Same as {@link #send(Object)}, but additionally {@code event} will be cached and delivered to
     * every new subscriber immediately after it registers itself in the event bus. Sticky events remain
     * active unless they get removed using {@link #removeStickyEvent(Class)} method.
     * There can only exist one sticky event of given time at a time. If another sticky event of given type is sent,
     * old instance gets replaced by a new one.
     *
     * @param event @NonNull sticky event to send
     */
    public void sendSticky(@NonNull final Object event) {
        Cache.stickyEvents.put(event.getClass(), event);
        send(event);
    }

    /**
     * Removes sticky event, which was previously sent using {@link #sendSticky(Object)} method.
     * Once event is removed from cache, it will no longer be sent to new subscribers on registration.
     *
     * @param eventClass @NonNull class of sticky event to be removed
     */
    public void removeStickyEvent(@NonNull Class<?> eventClass) {
        Cache.stickyEvents.remove(eventClass);
    }

    /**
     * Send an event to all registered subscribers, dispatching it in current thread, before method
     * returns, to one specific subscriber.
     * This method will process all dispatch code in caller thread. It means that Thread.DISPATCHER subscribers
     * will have to finish before control will be returned to caller.
     * <p/>
     * Generally sendXxx() methods are more performance-friendly than postXxx() methods.
     * Use this method in cases when you are not concerned about how long the dispatch will take.
     * Avoid using sendXxx() and prefer postXxx() in UI thread, as there is no guarantee how long it may take.
     * <p/>
     * If no subscribers have been subscribed for {@code event}'s class, and {@code event} is not already a
     * {@link DeadEvent}, it will be wrapped in a DeadEvent and re-posted.
     *
     * @param event      @NonNull event to post.
     * @param subscriber @NonNull subscriber to deliver event to. Subscriber must be registered in the event bus already.
     * @throws NullPointerException if the event is null.
     */
    public void sendTo(@NonNull final Object event, @NonNull Object subscriber) {
        new Dispatcher(this, event, subscriber).run();
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation

    static final String BACKGROUND_THREAD_NAME = "EventBus.Executor #";
    static final String DISPATHER_THREAD_NAME = "EventBus.Dispatcher";


    static ThreadPoolExecutor mSharedExecutor = null;
    static final Handler mUiThreadHandler = new Handler(Looper.getMainLooper());

    @NonNull
    final String mIdentifier;
    @NonNull
    final Handler mDispatcherThread;
    @NonNull
    final Executor mBackgroundExecutor;


    /**
     * All registered subscribers, indexed by event type.
     * Inner Set is a CopyOnWriteArraySet.
     */
    final Map<Class<?>, CopyOnWriteArraySet<EventHandler>> mRegisteredEventHandlersByEventType = new ConcurrentHashMap<>();

    /**
     * A map of android Handler objects that are default thread handlers for the subscribers.
     */
    final Map<IdentityWeakReferenceKey<Object>, Handler> mSubscribersDefaultThreads = new ConcurrentHashMap<>();


    synchronized Executor getSharedExecutor() {
        if (mSharedExecutor == null) {
            int numberOfThreads = Math.max(4, Math.min(16, Runtime.getRuntime().availableProcessors() * 2));
            mSharedExecutor = new ThreadPoolExecutor(
                    numberOfThreads, numberOfThreads,
                    10, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new ThreadFactory() {
                        private final AtomicInteger mCount = new AtomicInteger(1);

                        @Override
                        public Thread newThread(@NonNull Runnable r) {
                            Thread thread = new Thread(r, BACKGROUND_THREAD_NAME + mCount.getAndIncrement());
                            thread.setPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                            return thread;
                        }
                    });
            mSharedExecutor.allowCoreThreadTimeOut(true);
        }
        return mSharedExecutor;
    }

    Handler createDispatcherThread() {
        HandlerThread thread = new HandlerThread(DISPATHER_THREAD_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND - 4);
        thread.start();
        return new Handler(thread.getLooper());
    }

    Handler getDefaultThreadForSubscriber(@NonNull Object subscriber) {
        return mSubscribersDefaultThreads.get(new IdentityWeakReferenceKey<>(subscriber));
    }


}