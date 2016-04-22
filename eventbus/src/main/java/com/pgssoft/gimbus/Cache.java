/*
 * Copyright (C) 2016 Arivald
 * Copyright (C) 2012 Square, Inc.
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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache of the class and annotation related data, shared for all instances of the event bus.
 * <p/>
 * Note: package access, the class is for internal use only.
 * <p/>
 * @author Arivald
 */
/*package*/ class Cache {

    /**
     * Cache for all classes/interfaces of given event class.
     * Key: event class
     * Value: list of classes/interfaces
     */
    static final Map<Class<?>, List<Class<?>>> mEventClassHierarchyCache = new ConcurrentHashMap<>();

    /**
     * Get list of classes implemented by event object.
     * This includes all super classes, all implemented interfaces with its superclasses, and all interfaces of superclasses.
     */
    @NonNull
    static List<Class<?>> getEventClasses(@NonNull final Object event) {
        //Note: lists are created once and never modified, created in one thread, and used by many thread afterwards.
        //No need to use thread safe collections.

        Class<?> eventClass = event.getClass();
        List<Class<?>> classes = mEventClassHierarchyCache.get(eventClass);
        //if event was not cached yet, cache it
        if (classes == null) {
            synchronized (mEventClassHierarchyCache) {
                //make sure another thread didn't cached it when we waited for synchronization.
                classes = mEventClassHierarchyCache.get(eventClass);
                if (classes == null) {
                    //Optimization:
                    //In sane production code an event should have two classes (Object.class and event class).
                    //In rare cases and event hierarchy is created. Or event implementing an interface.
                    //But Android's ArrayList minimum grow is 12 elements, leaving empty space.
                    //To minimize memory footprint, but not impact performance, I choose 4 as initial size, it is enough for common cases.
                    classes = new ArrayList<>(4);
                    List<Class<?>> parents = new LinkedList<>();
                    parents.add(eventClass);

                    while (!parents.isEmpty()) {
                        Class<?> clazz = parents.remove(0);
                        classes.add(clazz);

                        Class<?> parent = clazz.getSuperclass();
                        if (parent != null) {
                            parents.add(parent);
                        }
                        Collections.addAll(classes, clazz.getInterfaces());
                    }
                    mEventClassHierarchyCache.put(eventClass, classes);
                }
            }
        }

        return classes;
    }


    /**
     * Cache of found event handlers for given subscriber & exact event class. Every super class of the
     * subscriber or event will have another entry.
     * This speed-up registering further objects of same class, or derived / similar classes.
     * <p/>
     * First map key: a subscriber class
     * Second map key: an event class
     * Value: a linked-list of event handlers for the combination of the subscriber class and event class
     */
    static final Map<Class<?>, Map<Class<?>, EventHandlersCacheItem>> mEventHandlersCache = new ConcurrentHashMap<>();

    /**
     * An item in the mEventHandlersCache.
     * Note: the old-style linked list (the "nextItem") will be used only if given subscriber do
     * subscribe twice to the same event class, which shouldn't happen in production code. So it is
     * faster and cheaper to use this field and for-loop, instead of LinkedList instance.
     */
    static class EventHandlersCacheItem {
        @NonNull
        final Method eventHandlerMethod;
        @NonNull
        final Dispatcher.DispatchingMethod dispatchingMethod;
        @Nullable
        final EventHandlersCacheItem nextItem;

        EventHandlersCacheItem(@NonNull final Method eventHandlerMethod,
                               @NonNull final Dispatcher.DispatchingMethod dispatchingMethod,
                               @Nullable final EventHandlersCacheItem nextItem) {
            this.eventHandlerMethod = eventHandlerMethod;
            this.dispatchingMethod = dispatchingMethod;
            this.nextItem = nextItem;
        }
    }

    /**
     * Finds all methods on subscriber that can be used as event handlers, creating and EventHandler
     * for every suitable method.
     *
     * @return A map, where the key is the event class, and the value is list of subscriber's
     * event handlers that can handle event of this class.
     */
    @NonNull
    static Map<Class<?>, List<EventHandler>> findAllEventHandlersForSubscriber(@NonNull final Object subscriber) {
        //Note: both Map and Lists returned from this method are intermediate objects only,
        //used in this one thread only, no thread safety needed.

        Map<Class<?>, List<EventHandler>> result = new HashMap<>();

        Class<?> currentSubscriberClass = subscriber.getClass();
        while (currentSubscriberClass != Object.class) {
            //get cached list of available event handlers for subscriber type, create one if no cached yet
            Map<Class<?>, EventHandlersCacheItem> cachedEventHandlers = mEventHandlersCache.get(currentSubscriberClass);
            if (cachedEventHandlers == null) {
                synchronized (mEventHandlersCache) {
                    //check again, some other thread could finish scanning when we waited for synchronisation
                    cachedEventHandlers = mEventHandlersCache.get(currentSubscriberClass);
                    if (cachedEventHandlers == null) {
                        cachedEventHandlers = scanForEventHandlers(currentSubscriberClass);
                        mEventHandlersCache.put(currentSubscriberClass, cachedEventHandlers);
                    }
                }
            }

            //create EventHandler instances for subscriber.
            //Key: event class
            //Value: linked list of @Subscribe methods that can handle this event class.
            for (Map.Entry<Class<?>, EventHandlersCacheItem> entry : cachedEventHandlers.entrySet()) {
                //key: class of the event
                List<EventHandler> eventHandlersByEventClass = result.get(entry.getKey());
                if (eventHandlersByEventClass == null) {
                    //Optimization: in production code there should be just one event handler for given class and subscriber instance.
                    eventHandlersByEventClass = new ArrayList<>(1);
                    result.put(entry.getKey(), eventHandlersByEventClass);
                }
                for (EventHandlersCacheItem cacheItem = entry.getValue(); cacheItem != null; cacheItem = cacheItem.nextItem) {
                    eventHandlersByEventClass.add(new EventHandler(subscriber, cacheItem.eventHandlerMethod, cacheItem.dispatchingMethod));
                }
            }
            currentSubscriberClass = currentSubscriberClass.getSuperclass();
        }
        return result;
    }

    /**
     * Scans single level of the subscriber class, finds any method annotated with the @Subscribe annotation,
     * builds a Map for cache.
     */
    @NonNull
    static Map<Class<?>, EventHandlersCacheItem> scanForEventHandlers(@NonNull final Class<?> subscriberClass) {
        //NOTE this Map is created once and never modified afterwards. It will be read in many threads later, no synchronisation.
        //I think it is safe to use the HashMap, the ConcurrentHashMap is too heavy.
        Map<Class<?>, EventHandlersCacheItem> eventHandlers = new HashMap<>();

        for (Method method : subscriberClass.getDeclaredMethods()) {
            if (method.isBridge()) {
                continue;
            }
            Subscribe annotation = method.getAnnotation(Subscribe.class);
            if (annotation != null) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new IllegalStateException("Method " + method + " has @Subscribe annotation but requires "
                            + parameterTypes.length + " arguments. Method must require a single argument.");
                }
                Class<?> eventType = parameterTypes[0];
                eventHandlers.put(eventType, new EventHandlersCacheItem(method, Dispatcher.getDispatchingMethod(annotation), eventHandlers.get(eventType)));
            }
        }

        return eventHandlers;
    }


}
