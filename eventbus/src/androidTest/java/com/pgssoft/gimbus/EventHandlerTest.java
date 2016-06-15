/*
 * Copyright (C) 2016 PGS Software SA
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.pgssoft.gimbus;

import android.test.InstrumentationTestCase;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import com.pgssoft.gimbus.mocks.TestEvent1;
import com.pgssoft.gimbus.mocks.TestEvent2;
import com.pgssoft.gimbus.mocks.TestSubscriber3;

public class EventHandlerTest extends InstrumentationTestCase {


    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructorAndHashCode() throws Exception {

        //given

        EventBus bus = new EventBus();
        TestSubscriber3 testSubscriber1 = new TestSubscriber3();
        Method method = TestSubscriber3.class.getDeclaredMethod("onTestEvent1", TestEvent1.class);

        //when

        //constructor does not allow null parameters, should throw
        try {
            //noinspection ConstantConditions
            new EventHandler(null, method, Dispatcher.getDispatchingMethod(method.getAnnotation(Subscribe.class)));
            fail("Should have throw");
        } catch (Throwable ignored) {
        }
        try {
            //noinspection ConstantConditions
            new EventHandler(testSubscriber1, null, null);
            fail("Should have throw");
        } catch (Throwable ignored) {
        }

        //but will proper parameters it should not throw
        EventHandler subject = new EventHandler(testSubscriber1, method, Dispatcher.getDispatchingMethod(method.getAnnotation(Subscribe.class)));

        //test for weak reference
        //set testTarget1 to null, then call GC few times
        int oldHashCode = subject.hashCode();

        //then

        //noinspection UnusedAssignment
        testSubscriber1 = null;
        long timeLimit = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(10);
        while (subject.mSubscriber.get() != null && timeLimit > System.currentTimeMillis()) {
            System.runFinalization();
            Runtime.getRuntime().gc();
            System.gc();
            Thread.sleep(100);
        }
        //reference should have been freed
        assertNull(subject.mSubscriber.get());
        //hash code must remain unchanged
        assertEquals(oldHashCode, subject.hashCode());
        //and deliverEvent should not try to deliver, nor throw.
        subject.invoke(bus, new Object());
    }

    public void testEquality() throws Exception {
        //two subscribers created for same method and same target should be equal, and have same hashcode.

        TestSubscriber3 testSubscriber1 = new TestSubscriber3();
        TestSubscriber3 testSubscriber2 = new TestSubscriber3();

        Method method1 = TestSubscriber3.class.getDeclaredMethod("onTestEvent1", TestEvent1.class);
        Method method2 = TestSubscriber3.class.getDeclaredMethod("onTestEvent2", TestEvent2.class);

        EventHandler subject_1_1 = new EventHandler(testSubscriber1, method1, Dispatcher.getDispatchingMethod(method1.getAnnotation(Subscribe.class)));
        EventHandler subject_1_2 = new EventHandler(testSubscriber1, method2, Dispatcher.getDispatchingMethod(method2.getAnnotation(Subscribe.class)));
        EventHandler subject_2_1 = new EventHandler(testSubscriber2, method1, Dispatcher.getDispatchingMethod(method1.getAnnotation(Subscribe.class)));
        EventHandler subject_2_2 = new EventHandler(testSubscriber2, method2, Dispatcher.getDispatchingMethod(method2.getAnnotation(Subscribe.class)));

        EventHandler secondSubject_1_1 = new EventHandler(testSubscriber1, method1, Dispatcher.getDispatchingMethod(method1.getAnnotation(Subscribe.class)));

        //then
        assertTrue(secondSubject_1_1.equals(subject_1_1));
        assertTrue(subject_1_1.equals(secondSubject_1_1));
        assertTrue(subject_1_1.hashCode() == secondSubject_1_1.hashCode());

        assertFalse(subject_1_1.equals(subject_1_2));
        assertFalse(subject_1_1.equals(subject_2_1));
        assertFalse(subject_1_1.equals(subject_2_2));
        assertFalse(subject_1_2.equals(subject_2_1));
        assertFalse(subject_1_1.equals(subject_2_2));
        assertFalse(subject_2_1.equals(subject_2_2));
    }

    public void testEventDelivery() throws Exception {
        //given
        EventBus bus = new EventBus();
        TestSubscriber3 testSubscriber1 = new TestSubscriber3();
        Method method = TestSubscriber3.class.getDeclaredMethod("onTestEvent1", TestEvent1.class);
        TestEvent1 testEvent1 = new TestEvent1();
        EventHandler subject = new EventHandler(testSubscriber1, method, Dispatcher.getDispatchingMethod(method.getAnnotation(Subscribe.class)));

        //when
        subject.invoke(bus, testEvent1);

        //then
        assertSame(testEvent1, testSubscriber1.lastReceivedEvent1);
    }


}
