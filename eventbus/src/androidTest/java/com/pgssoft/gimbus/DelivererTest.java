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
