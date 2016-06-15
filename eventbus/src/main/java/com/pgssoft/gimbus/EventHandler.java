/*
 * Copyright (C) 2016 PGS Software SA
 *
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.pgssoft.gimbus;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Represents one subscribing method, on one instance of an object.
 * Immutable, except that the mSubscriber reference can be cleared by GC - in this case object will be recycled soon.
 * <p/>
 * Two EventHandlers are equal when they refer to the same method on the same subscriber instance.
 * <p/>
 * Note: package access, the class is for internal use only.
 * <p/>
 * @author Lukasz Plominski
 */
/*package*/ final class EventHandler {

    /**
     * The subscriber, an instance to object to deliver the event to.
     */
    @NonNull
    final WeakReference<Object> mSubscriber;

    /**
     * The method in the subscriber that should be called for this certain event class.
     */
    @NonNull
    final Method mMethod;

    /**
     * The delivery thread, got from @Subscribe
     */
    @NonNull
    final Dispatcher.DispatchingMethod mDispatchingMethod;

    /**
     * Object hash code, cached because it will not change.
     */
    final int mHash;


    EventHandler(@NonNull Object subscriber, @NonNull Method method, @NonNull Dispatcher.DispatchingMethod dispatchingMethodd) {
        mSubscriber = new WeakReference<>(subscriber);
        mMethod = method;
        mDispatchingMethod = dispatchingMethodd;
        method.setAccessible(true);

        //Compute hash now, it will never change anyway, and it will be used frequently.
        //Plus it will not crash the app if GC clear the mSubscriber
        mHash = (31 + method.hashCode()) * 31 + System.identityHashCode(subscriber);
    }

    /**
     * Invokes the subscriber method to handle event.
     */
    void invoke(@NonNull EventBus bus, @NonNull Object event) {
        try {
            Object subscriber = mSubscriber.get();
            if (subscriber != null) {
                mMethod.invoke(subscriber, event);
            }
        } catch (InvocationTargetException e) {
            //we need to pass original exception, not the InvocationTargetException
            //todo add code to globally handle exceptions
//            bus.onSubscriberException(mSubscriber.get(), mMethod, e.getCause());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public int hashCode() {
        return mHash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final EventHandler other = (EventHandler) obj;
        // Use == for mSubscriber, so that different equal instances will still receive events.
        // We only guard against the case that the same object is registered multiple times
        return mHash == other.mHash
                && mSubscriber.get() == other.mSubscriber.get()
                && mMethod.equals(other.mMethod);
    }

}
