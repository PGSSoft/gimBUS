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

public class TestSubscriber1 {

    @Subscribe
    public void onTestEvent1_default(TestEvent1 event) {

    }

    @Subscribe(EventBus.DELIVER_IN_DISPATCHER_THREAD)
    public void onTestEvent1_dispatcher(TestEvent1 event) {

    }

    @Subscribe(EventBus.DELIVER_IN_BACKGROUND_THREAD)
    public void onTestEvent1_background(TestEvent1 event) {

    }

    @Subscribe(EventBus.DELIVER_IN_UI_THREAD)
    public void onTestEvent1_ui(TestEvent1 event) {

    }

}
