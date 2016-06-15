/*
 * Copyright (C) 2016 PGS Software SA
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.pgssoft.gimbus.mocks;

/**
 * Object that can be final (ex accessible in inner ad-hoc classes), and can hold reference to other object
 */
@SuppressWarnings("unused")
public class Reference<T> {
    public T ref;

    public Reference() {
        this.ref = null;
    }

    public Reference(T ref) {
        this.ref = ref;
    }
}
