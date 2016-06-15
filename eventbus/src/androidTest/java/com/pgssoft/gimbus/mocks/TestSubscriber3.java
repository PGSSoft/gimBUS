/*
 * Copyright (C) 2016 PGS Software SA
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
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