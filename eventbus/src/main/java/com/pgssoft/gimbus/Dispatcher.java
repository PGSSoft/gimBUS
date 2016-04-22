/*
 * Copyright (C) 2016 Arivald
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

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

/**
 * Dispatcher is an class used to process posted event.
 * It is responsible for finding all subscribers, and delivering event to subscribers
 * according to their needs.
 * <p/>
 * Dispatcher is Runnable, it will be executed by posting to some Handler, usually to dispatcher thread Handler
 * <p/>
 * @author Arivald
 */
/*package*/ final class Dispatcher implements Runnable {

    interface DispatchingMethod {
        void dispatch(@NonNull Dispatcher dispatcher, @NonNull EventHandler eventHandler);
    }

    final static DispatchingMethod IN_DISPATCHER_THREAD = new DispatchingMethod() {
        @Override
        public void dispatch(@NonNull Dispatcher dispatcher, @NonNull EventHandler eventHandler) {
            eventHandler.invoke(dispatcher.mEventBus, dispatcher.mEvent);
        }
    };

    final static DispatchingMethod IN_UI_THREAD = new DispatchingMethod() {
        @Override
        public void dispatch(@NonNull Dispatcher dispatcher, @NonNull EventHandler eventHandler) {
            EventBus.mUiThreadHandler.post(new Deliverer(dispatcher.mEventBus, dispatcher.mEvent, eventHandler));
        }
    };

    final static DispatchingMethod IN_BACKGROUND_THREAD = new DispatchingMethod() {
        @Override
        public void dispatch(@NonNull Dispatcher dispatcher, @NonNull EventHandler eventHandler) {
            dispatcher.mEventBus.mBackgroundExecutor.execute(new Deliverer(dispatcher.mEventBus, dispatcher.mEvent, eventHandler));
        }
    };

    final static DispatchingMethod IN_DEFAULT_THREAD = new DispatchingMethod() {
        @Override
        public void dispatch(@NonNull Dispatcher dispatcher, @NonNull EventHandler eventHandler) {
            Object subscriber = eventHandler.mSubscriber.get();
            if (subscriber != null) {
                Handler handler = dispatcher.mEventBus.getDefaultThreadForSubscriber(subscriber);
                if (handler == null) {
                    //fallback
                    IN_BACKGROUND_THREAD.dispatch(dispatcher, eventHandler);
                    return;
                }
                handler.post(new Deliverer(dispatcher.mEventBus, dispatcher.mEvent, eventHandler));
            }
        }
    };


    final EventBus mEventBus;
    final Object mEvent;
    final Object mSingleSubscriber;

    Dispatcher(@NonNull EventBus mEventBus, @NonNull Object mEvent, @Nullable Object mSingleSubscriber) {
        this.mEventBus = mEventBus;
        this.mEvent = mEvent;
        this.mSingleSubscriber = mSingleSubscriber;
    }

    static DispatchingMethod getDispatchingMethod(@NonNull final Subscribe subscribeAnnotation) {
        switch (subscribeAnnotation.value()) {
            case EventBus.DELIVER_IN_DEFAULT_THREAD:
                return IN_DEFAULT_THREAD;
            case EventBus.DELIVER_IN_UI_THREAD:
                return IN_UI_THREAD;
            case EventBus.DELIVER_IN_BACKGROUND_THREAD:
                return IN_BACKGROUND_THREAD;
            case EventBus.DELIVER_IN_DISPATCHER_THREAD:
                return IN_DISPATCHER_THREAD;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void run() {
        boolean dispatched = false;

        for (Class<?> eventType : Cache.getEventClasses(mEvent)) {
            Set<EventHandler> registeredEventHandlersForEventType = mEventBus.mRegisteredEventHandlersByEventType.get(eventType);
            if (registeredEventHandlersForEventType != null) {
                for (EventHandler eventHandler : registeredEventHandlersForEventType) {
                    //skip GCed subscribers, skip other subscribers if in single subscriber mode
                    if (mSingleSubscriber != null
                            ? eventHandler.mSubscriber.get() == mSingleSubscriber
                            : eventHandler.mSubscriber.get() != null) {
                        dispatched = true;
                        eventHandler.mDispatchingMethod.dispatch(this, eventHandler);
                    }
                }
            }
        }
        //if not dispatched, send DeadEvent
        if (!dispatched && !(mEvent instanceof DeadEvent)) {
            new Dispatcher(mEventBus, new DeadEvent(mEventBus, mEvent), mSingleSubscriber).run();
        }
    }

}
