/*
 * Copyright (C) 2016 PGS Software SA
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

import com.pgssoft.gimbus.mocks.TestEvent1;
import com.pgssoft.gimbus.mocks.TestEvent2;
import com.pgssoft.gimbus.mocks.TestEvent3;
import com.pgssoft.gimbus.mocks.TestInterfaceEvent1;
import com.pgssoft.gimbus.mocks.TestSubscriber1;
import com.pgssoft.gimbus.mocks.TestSubscriber2;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for the Cache class.
 *
 * Created by Lukasz Plominski on 2016-03-07.
 */
public class CacheTest extends TestCase {


    protected void setUp() throws Exception {
        super.setUp();
        purgeCache();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        purgeCache();
    }

    void purgeCache() {
        synchronized (Cache.mEventClassHierarchyCache) {
            Cache.mEventClassHierarchyCache.clear();
        }
        synchronized (Cache.mEventHandlersCache) {
            Cache.mEventHandlersCache.clear();
        }
    }


    public void testEventClassHierarchyCache() throws Exception {

        //1. when asked first time, it should create data.
        assertEquals(0, Cache.mEventClassHierarchyCache.size());

        final List<Class<?>> classes1 = Cache.getEventClasses(new TestEvent1());

        assertEquals(2, classes1.size());
        assertTrue(classes1.contains(TestEvent1.class));
        assertTrue(classes1.contains(Object.class));

        //2. when asked second time, passing object of came class, it should return the same Set od data,
        final List<Class<?>> classes2 = Cache.getEventClasses(new TestEvent1());

        assertSame(classes1, classes2);
        assertEquals(2, classes2.size());


        //3. event implementing interfaces
        final List<Class<?>> classes3 = Cache.getEventClasses(new TestEvent2());

        assertEquals(4, classes3.size());
        assertTrue(classes3.contains(TestEvent2.class));
        assertTrue(classes3.contains(TestInterfaceEvent1.class));
        assertTrue(classes3.contains(Object.class));
        assertTrue(classes3.contains(Serializable.class));


        //4. test multi threaded access
        final int NUM_THREADS = 64;
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS * 40; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    //noinspection unused
                    int count = 0;
                    for (int i = 0; i < 64; i++) {
                        assertSame(classes3, Cache.getEventClasses(new TestEvent2()));
                    }
                }
            });
        }
        //wait for all runnables to finish
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);
    }

    public void testEventHandlersCache() throws Exception {

        //1. when asked first time, it should create data.
        assertEquals(0, Cache.mEventHandlersCache.size());

        final Map<Class<?>, List<EventHandler>> handlers1 = Cache.findAllEventHandlersForSubscriber(new TestSubscriber1());

        assertEquals(1, Cache.mEventHandlersCache.size());
        final Map<Class<?>, Cache.EventHandlersCacheItem> forObject = Cache.mEventHandlersCache.get(Object.class);
        //Object.class shouldn't be cached, can't have @Subscribe anyway
        assertNull(forObject);

        final Map<Class<?>, Cache.EventHandlersCacheItem> forTestSubscriber1 = Cache.mEventHandlersCache.get(TestSubscriber1.class);
        assertNotNull(forTestSubscriber1);
        assertEquals(1, forTestSubscriber1.size());

        assertEquals(1, handlers1.size());
        assertNotNull(handlers1.get(TestEvent1.class));
        assertEquals(4, handlers1.get(TestEvent1.class).size());

        //2. when asked next time for same subscriber class, it should retain same data.
        //noinspection unused
        final Map<Class<?>, List<EventHandler>> handlers2 = Cache.findAllEventHandlersForSubscriber(new TestSubscriber1());

        assertSame(forTestSubscriber1, Cache.mEventHandlersCache.get(TestSubscriber1.class));

        //3. wen asked for descendant of the already scanned TestSubscriber1, all old data should remain unchanged.
        //there should appear new data, an incremental difference between TestSubscriber1 and TestSubscriber2.
        final Map<Class<?>, List<EventHandler>> handlers3 = Cache.findAllEventHandlersForSubscriber(new TestSubscriber2());

        //old data
        assertSame(forTestSubscriber1, Cache.mEventHandlersCache.get(TestSubscriber1.class));
        //new data
        final Map<Class<?>, Cache.EventHandlersCacheItem> forTestSubscriber2 = Cache.mEventHandlersCache.get(TestSubscriber2.class);
        assertNotNull(forTestSubscriber2);
        assertEquals(1, forTestSubscriber2.size());

        assertEquals(2, handlers3.size());
        assertNotNull(handlers3.get(TestEvent1.class));
        assertEquals(4, handlers3.get(TestEvent1.class).size());
        assertNotNull(handlers3.get(TestEvent2.class));
        assertEquals(2, handlers3.get(TestEvent2.class).size());


        //4. test multi threaded access
        final int NUM_THREADS = 64;
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        for (int i = 0; i < NUM_THREADS * 40; i++) {
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    //noinspection unused
                    int count = 0;
                    for (int i = 0; i < 64; i++) {
                        final Map<Class<?>, Cache.EventHandlersCacheItem> forTestSubscriber3 = Cache.mEventHandlersCache.get(TestSubscriber2.class);
                        assertSame(forTestSubscriber2, forTestSubscriber3);
                        assertEquals(1, forTestSubscriber3.size());
                        for (Map.Entry<Class<?>, Cache.EventHandlersCacheItem> handlers : forTestSubscriber3.entrySet()) {
                            //dummy code, just to make sure loop is executed
                            count += handlers.getKey().getCanonicalName().length();
                        }
                    }
                }
            });
        }
        //wait for all runnables to finish
        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);
    }

    public void testMultipleStickyEventsOfSameClass() {
        assertTrue(Cache.stickyEvents.size() == 0);

        TestEvent1 testEvent1 = new TestEvent1();
        TestEvent2 testEvent2 = new TestEvent2();
        TestEvent3 testEvent3 = new TestEvent3();

        Cache.stickyEvents.put(testEvent1.getClass(), testEvent1);
        Cache.stickyEvents.put(testEvent2.getClass(), testEvent2);
        Cache.stickyEvents.put(testEvent3.getClass(), testEvent3);

        assertTrue(Cache.stickyEvents.size() == 3);
        assertSame(testEvent1, Cache.stickyEvents.get(TestEvent1.class));

        //Registering another sticky event of the same class should replace existing instance
        TestEvent1 testEvent11 = new TestEvent1();
        Cache.stickyEvents.put(testEvent11.getClass(), testEvent11);

        assertTrue(Cache.stickyEvents.size() == 3);
        assertSame(testEvent11, Cache.stickyEvents.get(TestEvent1.class));

        Cache.stickyEvents.remove(TestEvent1.class);
        assertTrue(Cache.stickyEvents.size() == 2);
        assertNull(Cache.stickyEvents.get(TestEvent1.class));
        assertSame(testEvent2, Cache.stickyEvents.get(TestEvent2.class));
        assertSame(testEvent3, Cache.stickyEvents.get(TestEvent3.class));

        //Cleanup
        Cache.stickyEvents.clear();
    }
}