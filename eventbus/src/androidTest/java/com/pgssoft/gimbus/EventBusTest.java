/*
 * Copyright (C) 2016 PGS Software SA
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.pgssoft.gimbus;

import android.os.Looper;
import android.test.InstrumentationTestCase;

import com.pgssoft.gimbus.mocks.Reference;
import com.pgssoft.gimbus.mocks.TestEvent1;
import com.pgssoft.gimbus.mocks.TestEvent2;
import com.pgssoft.gimbus.mocks.TestEvent3;
import com.pgssoft.gimbus.mocks.TestInterfaceEvent1;
import com.pgssoft.gimbus.mocks.TestSubscriber3;
import com.pgssoft.gimbus.mocks.TestSubscriber4;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * General EventBus tests.
 *
 * Created by Lukasz Plominski on 2016-03-31.
 */
public class EventBusTest extends InstrumentationTestCase {

    public void testRegisterUnregisterAndGetSubscribersForEventType() throws Exception {

        EventBus eventBus = new EventBus();
        TestSubscriber3 testSubscriber1 = new TestSubscriber3();
        TestSubscriber3 testSubscriber2 = new TestSubscriber3();
        Set<EventHandler> found;

        eventBus.register(testSubscriber1);


        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent1.class);
        assertEquals(1, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent2.class);
        assertEquals(1, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent3.class);
        assertEquals(1, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestInterfaceEvent1.class);
        assertEquals(1, found.size());


        eventBus.unregister(testSubscriber1);


        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent1.class);
        assertEquals(0, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent2.class);
        assertEquals(0, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent3.class);
        assertEquals(0, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestInterfaceEvent1.class);
        assertEquals(0, found.size());


        eventBus.register(testSubscriber1);
        eventBus.register(testSubscriber2);


        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent1.class);
        assertEquals(2, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent2.class);
        assertEquals(2, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestEvent3.class);
        assertEquals(2, found.size());

        found = eventBus.mRegisteredEventHandlersByEventType.get(TestInterfaceEvent1.class);
        assertEquals(2, found.size());
    }

    public void testDelivery() throws Exception {
        EventBus eventBus = new EventBus();
        TestSubscriber3 testSubscriber3 = new TestSubscriber3();
        TestSubscriber3 testTarget2 = new TestSubscriber3();

        TestEvent1 testEvent1 = new TestEvent1();
        TestEvent2 testEvent2 = new TestEvent2();
        TestEvent3 testEvent3 = new TestEvent3();

        eventBus.register(testSubscriber3);
        eventBus.register(testTarget2);

        //case 1 TestEvent1
        //note: use send(), because it will dispatch in this thread, while all subscribers in TestTarget1
        //are Bus.DISPATCHER_THREAD, so all processing will be in this thread;
        eventBus.send(testEvent1);

        assertSame(testEvent1, testSubscriber3.lastReceivedEvent1);
        assertSame(testEvent1, testTarget2.lastReceivedEvent1);

        assertNull(testSubscriber3.lastReceivedEvent2);
        assertNull(testSubscriber3.lastReceivedEvent3);
        assertNull(testSubscriber3.lastReceivedInterfaceEvent1);

        //cleanup
        testSubscriber3.lastReceivedEvent1 = null;
        testTarget2.lastReceivedEvent1 = null;


        //case 2 TestEvent2
        eventBus.send(testEvent2);

        assertSame(testEvent2, testSubscriber3.lastReceivedEvent2);
        assertSame(testEvent2, testSubscriber3.lastReceivedInterfaceEvent1);
        assertSame(testEvent2, testTarget2.lastReceivedEvent2);
        assertSame(testEvent2, testTarget2.lastReceivedInterfaceEvent1);

        assertNull(testSubscriber3.lastReceivedEvent1);
        assertNull(testSubscriber3.lastReceivedEvent3);

        //cleanup
        testSubscriber3.lastReceivedEvent2 = null;
        testSubscriber3.lastReceivedInterfaceEvent1 = null;
        testTarget2.lastReceivedEvent2 = null;
        testTarget2.lastReceivedInterfaceEvent1 = null;

        //case 3 TestEvent3
        eventBus.send(testEvent3);

        assertSame(testEvent3, testSubscriber3.lastReceivedEvent1);
        assertSame(testEvent3, testSubscriber3.lastReceivedEvent3);
        assertSame(testEvent3, testTarget2.lastReceivedEvent1);
        assertSame(testEvent3, testTarget2.lastReceivedEvent3);

        assertNull(testSubscriber3.lastReceivedEvent2);
        assertNull(testSubscriber3.lastReceivedInterfaceEvent1);

        //cleanup
        testSubscriber3.lastReceivedEvent1 = null;
        testSubscriber3.lastReceivedEvent3 = null;
        testTarget2.lastReceivedEvent1 = null;
        testTarget2.lastReceivedEvent3 = null;
    }

    //TODO
//    volatile Throwable theException;
//    public void testExceptionHandling() throws Exception {
//
//        EventBus eventBus = new EventBus() {
//            @Override
//            protected void onSubscriberException(@NonNull Object target, @NonNull Method method, @NonNull Throwable exception) {
//                theException = exception;
//            }
//        };
//        TestTarget2 testTarget2 = new TestTarget2();
//
//        TestEvent2 testEvent2 = new TestEvent2();
//        TestEvent3 testEvent3 = new TestEvent3();
//
//        eventBus.register(testTarget2);
//
//        //case 1 : one of delivery methods that involve ExecutorRunnable - shared code path
//        theException = null;
//        //post TestEvent2, should trigger testTarget2.onTestEvent2Exception()
//        eventBus.send(testEvent2);
//
//        //wait a bit
//        long timeLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
//        while (theException == null
//                && timeLimit > System.currentTimeMillis()) {
//            Thread.sleep(100);
//        }
//
//        assertNotNull(theException);
//        assertSame(theException.getClass(), RuntimeException.class);
//        assertEquals(theException.getMessage(), "onTestEvent2Exception");
//
//        //case 2 : in DISPATCHER thread
//        theException = null;
//        //post TestEvent2, should trigger testTarget2.onTestEvent3Exception()
//        eventBus.send(testEvent3);
//
//        //no waiting, we used send() and onTestEvent3Exception is DISPATCHER, so should be executed
//        //before send() returned
//
//        assertNotNull(theException);
//        assertSame(theException.getClass(), RuntimeException.class);
//        assertEquals(theException.getMessage(), "onTestEvent3Exception");
//
//    }

    public void testWeakReferenceToTargets() throws Exception {
        EventBus eventBus = new EventBus();
        TestSubscriber3 testSubscriber3 = new TestSubscriber3();
        WeakReference<TestSubscriber3> weakTarget = new WeakReference<>(testSubscriber3);
        TestEvent1 testEvent1 = new TestEvent1();
        eventBus.register(testSubscriber3);

        //first make sure it delivers correctly
        //note: use send(), because it will dispatch in this thread, while all handlers in TestSubscriber1
        //are DELIVER_IN_DISPATCHER_THREAD, so all processing will be in this thread;
        eventBus.send(testEvent1);

        assertSame(testEvent1, testSubscriber3.lastReceivedEvent1);

        assertNull(testSubscriber3.lastReceivedEvent2);
        assertNull(testSubscriber3.lastReceivedEvent3);
        assertNull(testSubscriber3.lastReceivedInterfaceEvent1);

        //now force GC
        //noinspection UnusedAssignment
        testSubscriber3 = null;
        long timeLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        while (weakTarget.get() != null && timeLimit > System.currentTimeMillis()) {
            System.runFinalization();
            Runtime.getRuntime().gc();
            System.gc();
            Thread.sleep(100);
        }
        assertNull(weakTarget.get());

        //check delivery again, it should not raise exceptions
        eventBus.send(testEvent1);
    }

    public void testUIThreadDelivery() throws Exception {
        //given
        EventBus eventBus = new EventBus();
        final Reference<Boolean> isUIthread = new Reference<>();

        Object aSubscriber = new Object() {

            @Subscribe(EventBus.DELIVER_IN_UI_THREAD)
            void onTestEvent1(TestEvent1 event) {
                isUIthread.ref = Looper.myLooper() == Looper.getMainLooper();
            }
        };
        eventBus.register(aSubscriber);

        //when
        eventBus.send(new TestEvent1());

        //then
        long timeLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        while (isUIthread.ref == null && timeLimit > System.currentTimeMillis()) {
            System.gc();
            Thread.sleep(100);
        }
        assertNotNull(isUIthread.ref); //NULL here mean that event handler was not called at all
        assertTrue(isUIthread.ref);
    }

    public void testBackgroundThreadDelivery() throws Exception {
        //given
        EventBus eventBus = new EventBus();
        final Reference<Boolean> isBkgThread = new Reference<>();

        Object aSubscriber = new Object() {

            @Subscribe(EventBus.DELIVER_IN_BACKGROUND_THREAD)
            void onTestEvent1(TestEvent1 event) {
                isBkgThread.ref = Looper.myLooper() != Looper.getMainLooper()
                        && Thread.currentThread().getName().contains(EventBus.BACKGROUND_THREAD_NAME);
            }
        };
        eventBus.register(aSubscriber);

        //when
        eventBus.send(new TestEvent1());

        //then
        long timeLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        while (isBkgThread.ref == null && timeLimit > System.currentTimeMillis()) {
            System.gc();
            Thread.sleep(100);
        }
        assertNotNull(isBkgThread.ref); //NULL here mean that event handler was not called at all
        assertTrue(isBkgThread.ref);
    }

    public void testDeliveryInSequence() throws Exception {
        //bus have to deliver events in same order as posted, with exception for completely asynchronous background delivery
        final int COUNT = 100 * 100;
        EventBus eventBus = new EventBus();
        TestSubscriber4 testTarget = new TestSubscriber4();

        List<Object> sequence = new ArrayList<>();
        for (int i = 0; i < COUNT; i++) {
            sequence.add(new TestEvent1());
        }

        eventBus.register(testTarget);

        //post sequence
        for (int i = 0; i < COUNT; i++)
            eventBus.post(sequence.get(i));

        //wait a bit for delivery
        long timeLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        while (testTarget.eventsDispatcher.size() < COUNT
                && testTarget.eventsUI.size() < COUNT
                && timeLimit > System.currentTimeMillis()) {
            Thread.sleep(100);
        }

        //check
        assertEquals(COUNT, testTarget.eventsDispatcher.size());
        for (int i = 0; i < COUNT; i++)
            assertSame(sequence.get(i), testTarget.eventsDispatcher.get(i));

        assertEquals(COUNT, testTarget.eventsUI.size());
        for (int i = 0; i < COUNT; i++)
            assertSame(sequence.get(i), testTarget.eventsUI.get(i));
    }

    public void testStickyEvent() {
        EventBus eventBus = new EventBus();
        TestSubscriber3 testSubscriber3 = new TestSubscriber3();

        TestEvent1 testEvent1 = new TestEvent1();
        eventBus.sendSticky(testEvent1);

        //Sticky event should be delivered right after new subscriber registered
        eventBus.register(testSubscriber3);
        assertSame(testEvent1, testSubscriber3.lastReceivedEvent1);

        testSubscriber3.lastReceivedEvent1 = null;
        eventBus.unregister(testSubscriber3);

        //After removing sticky event, it should no longer be sent when new subscriber registers
        eventBus.removeStickyEvent(TestEvent1.class);
        eventBus.register(testSubscriber3);
        assertNull(testSubscriber3.lastReceivedEvent1);
    }
}
