/*
 * Copyright (C) 2016 PGS Software SA
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.pgssoft.gimbus;

import android.test.InstrumentationTestCase;

import com.pgssoft.gimbus.mocks.TestEvent1;
import com.pgssoft.gimbus.mocks.TestSubscriber3;

public class DispatcherTest extends InstrumentationTestCase {

    public void testExecution() throws Exception {
        //prepare
        TestSubscriber3 subscriber1 = new TestSubscriber3();
        TestEvent1 testEvent1 = new TestEvent1();
        EventBus eventBus1 = new EventBus();
        eventBus1.register(subscriber1);

        //get dispatcher
        Dispatcher dispatcher = new Dispatcher(eventBus1, testEvent1, subscriber1);
        assertSame(eventBus1, dispatcher.mEventBus);
        assertSame(testEvent1, dispatcher.mEvent);
        assertSame(subscriber1, dispatcher.mSingleSubscriber);

        //try to execute dispatcher
        //it should deliver event
        dispatcher.run();
        assertSame(testEvent1, subscriber1.lastReceivedEvent1);
        subscriber1.lastReceivedEvent1 = null;

        //cleanup
        eventBus1.unregister(subscriber1);
    }

}
