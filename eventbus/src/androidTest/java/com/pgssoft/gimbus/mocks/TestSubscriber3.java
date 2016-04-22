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
package com.pgssoft.gimbus.mocks;


import com.pgssoft.gimbus.EventBus;
import com.pgssoft.gimbus.Subscribe;

public class TestSubscriber3 {
    public TestEvent1 lastReceivedEvent1 = null;
    public TestEvent2 lastReceivedEvent2 = null;
    public TestEvent3 lastReceivedEvent3 = null;
    public TestInterfaceEvent1 lastReceivedInterfaceEvent1 = null;

    @Subscribe(EventBus.DELIVER_IN_DISPATCHER_THREAD)
    public void onTestEvent1(TestEvent1 event) {
        lastReceivedEvent1 = event;
    }

    @Subscribe(EventBus.DELIVER_IN_DISPATCHER_THREAD)
    /* package */ void onTestEvent2(TestEvent2 event) {
        lastReceivedEvent2 = event;
    }

    @Subscribe(EventBus.DELIVER_IN_DISPATCHER_THREAD)
    protected void onTestEvent3(TestEvent3 event) {
        lastReceivedEvent3 = event;
    }

    @Subscribe(EventBus.DELIVER_IN_DISPATCHER_THREAD)
    private void onTestInterfaceEvent1(TestInterfaceEvent1 event) {
        lastReceivedInterfaceEvent1 = event;
    }
}