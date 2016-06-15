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

public class TestSubscriber2 extends TestSubscriber1 {


    @Subscribe(EventBus.DELIVER_IN_BACKGROUND_THREAD)
    public void onTestEvent2_background(TestEvent2 event) {

    }

    @Subscribe(EventBus.DELIVER_IN_UI_THREAD)
    public void onTestEvent2_ui(TestEvent2 event) {

    }

}
