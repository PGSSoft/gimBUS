/*
 * Copyright (C) 2016 PGS Software SA
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.pgssoft.gimbus;

import android.support.annotation.NonNull;

/**
 * A Runnable that can be executed on Executor, does the job of invoking the event handler with
 * specified event.
 * <p/>
 * @author Lukasz Plominski
 */
/*package*/ final class Deliverer implements Runnable {

    final EventBus mEventBus;
    final Object mEvent;
    final EventHandler mEventHandler;

    Deliverer(@NonNull EventBus mEventBus, @NonNull Object mEvent, @NonNull EventHandler mEventHandler) {
        this.mEventBus = mEventBus;
        this.mEvent = mEvent;
        this.mEventHandler = mEventHandler;
    }

    @Override
    public void run() {
        mEventHandler.invoke(mEventBus, mEvent);
    }

}
