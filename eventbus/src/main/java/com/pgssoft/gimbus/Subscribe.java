/*
 * Copyright (C) 2016 PGS Software SA
 * Copyright (C) 2007 The Guava Authors
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.pgssoft.gimbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event subscriber.
 * <p/>
 * <p>The type of event will be indicated by the method's first (and only) parameter. If this
 * annotation is applied to methods with zero parameters, or more than one parameter, the object
 * containing the method will not be able to register for event delivery from the {@link EventBus}.
 * <p/>
 * @author Lukasz Plominski (Android EventBus code)
 * @author Cliff Biffle (Guava inherited code)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {

    /**
     * The event delivery thread, see EventBus for details.
     */
    @EventBus.DeliveryThread int value() default EventBus.DELIVER_IN_DEFAULT_THREAD;

}
