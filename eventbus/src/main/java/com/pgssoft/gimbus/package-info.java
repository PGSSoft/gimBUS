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

/**
 * A event bus.
 * An fast, multi-threaded event bus for Android applications.
 * <p/>
 * Idea for this library came to me during one commercial project, where single-threaded Otto turned
 * out to be not enough. One of the most problematic things in Otto is that it is single threaded.
 * And fact that the event sender have to wait until the event is processed. Andy unexpected delay
 * have impact on the UI thread, creating hiccups.
 * <p/>
 * This code is an second attempt to create even bus library up to my needs, the first one belongs
 * to the mentioned above commercial project. I have to redo it from scratch, but on the
 * bright side I can avoid some bad choices I did in the first version.
 * <p/>
 * This library uses the Guava API and code as the base, in conjunction with some ideas of the
 * Otto library.
 * <p/>
 * Vocabulary
 * <p/>
 * Eventy - an object posted to te bus. Events are distinguished by class, with all super classes,
 * and all implemented interfaces.
 * Event handler - a method that handle given type of events. Handler can have any assess specifier.
 * Subscriber - an object that registered itself in bus, its methods with @Subscribe will become event handlers.
 * Dispatcher - class that do all work related to finding all event handlers for given event object.
 * Deliverer - class responsible for calling the event handler, in proper thread.
 */

package com.pgssoft.gimbus;