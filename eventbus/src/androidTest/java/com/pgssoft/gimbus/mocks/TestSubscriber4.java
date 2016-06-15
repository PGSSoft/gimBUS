/*
 * Copyright (C) 2016 PGS Software SA
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.pgssoft.gimbus.mocks;


import java.util.ArrayList;
import java.util.List;

import com.pgssoft.gimbus.EventBus;
import com.pgssoft.gimbus.Subscribe;


public class TestSubscriber4 {
    public List<Object> eventsDispatcher = new ArrayList<>();
    public List<Object> eventsUI = new ArrayList<>();

    @Subscribe(EventBus.DELIVER_IN_DISPATCHER_THREAD)
    public void onTestEvent1Dis(TestEvent1 event) {
        eventsDispatcher.add(event);
    }

    @Subscribe(EventBus.DELIVER_IN_UI_THREAD)
    public void onTestEvent1UI(TestEvent1 event) {
        eventsUI.add(event);
    }

    @Subscribe(EventBus.DELIVER_IN_BACKGROUND_THREAD)
    public void onTestEvent2Exception(TestEvent2 event) {
        throw new RuntimeException("onTestEvent2Exception");
    }

    @Subscribe(EventBus.DELIVER_IN_DISPATCHER_THREAD)
    public void onTestEvent3Exception(TestEvent3 event) {
        throw new RuntimeException("onTestEvent3Exception");
    }

}