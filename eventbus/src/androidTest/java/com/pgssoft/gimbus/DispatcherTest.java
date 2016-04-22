/*
 * Copyright (C) 2016 Arivald
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
