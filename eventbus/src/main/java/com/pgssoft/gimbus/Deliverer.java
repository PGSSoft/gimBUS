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
