/*
 * Copyright (C) 2016 Arivald
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pgssoft.gimbus;

import android.support.annotation.NonNull;

/**
 * Wraps an event that was posted, but which had no subscribers and thus could not be delivered.
 * <p/>
 * <p>Registering a DeadEvent subscriber is useful for debugging or logging, as it can detect
 * misconfigurations in a system's event distribution.
 *
 * @author Arivald (Android EventBus code)
 * @author Cliff Biffle (Guava inherited code)
 */
public class DeadEvent {

    @NonNull
    public final EventBus eventBus;

    @NonNull
    public final Object event;

    /**
     * Creates a new DeadEvent.
     *
     * @param eventBus the event bus broadcasting the original event.
     * @param event    the event that could not be delivered.
     */
    /*package*/ DeadEvent(@NonNull EventBus eventBus, @NonNull Object event) {
        this.eventBus = eventBus;
        this.event = event;
    }

}
