/*
 * Copyright (C) 2016 PGS Software SA
 * Copyright (C) 2007 The Guava Authors
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.pgssoft.gimbus;

import android.support.annotation.NonNull;

/**
 * Wraps an event that was posted, but which had no subscribers and thus could not be delivered.
 * <p/>
 * <p>Registering a DeadEvent subscriber is useful for debugging or logging, as it can detect
 * misconfigurations in a system's event distribution.
 *
 * @author Lukasz Plominski (Android EventBus code)
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
