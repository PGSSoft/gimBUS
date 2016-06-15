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

import com.pgssoft.gimbus.mocks.TestEvent1;
import com.pgssoft.gimbus.mocks.TestSubscriber3;

public class DelivererTest extends InstrumentationTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testExecution() throws Exception {
        //prepare
        EventBus bus = new EventBus();
        TestSubscriber3 testSubscriber1 = new TestSubscriber3();
        Method target1SubscriberMethod = TestSubscriber3.class.getMethod("onTestEvent1", TestEvent1.class);

        EventHandler test1EventHandler = new EventHandler(
                testSubscriber1,
                target1SubscriberMethod,
                Dispatcher.getDispatchingMethod(target1SubscriberMethod.getAnnotation(Subscribe.class)));
        TestEvent1 testEvent1 = new TestEvent1();

        //ger deliverer
        Deliverer deliverer1 = new Deliverer(bus, testEvent1, test1EventHandler);
        assertSame(bus, deliverer1.mEventBus);
        assertSame(test1EventHandler, deliverer1.mEventHandler);
        assertSame(testEvent1, deliverer1.mEvent);

        //execute
        deliverer1.run();
        assertSame(testEvent1, testSubscriber1.lastReceivedEvent1);
        testSubscriber1.lastReceivedEvent1 = null;
    }


}
