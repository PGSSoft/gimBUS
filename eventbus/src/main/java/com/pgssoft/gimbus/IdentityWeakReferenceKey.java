/*
 * Copyright (C) 2016 PGS Software SA
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.pgssoft.gimbus;

import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;

/**
 * A key for the ConcurrentHashMap, where key object have to be distinguished by identity, not equality,
 * have to be weakly referenced.
 * <p/>
 * Object is immutable, with exception for the referenced object, that may be GCed.
 * <p/>
 * @author Lukasz Plominski
 */
public class IdentityWeakReferenceKey<T> extends WeakReference<T> {

    public IdentityWeakReferenceKey(@NonNull T reference) {
        super(reference);
        mHash = System.identityHashCode(reference);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // implementation

    /**
     * Object hash code, cached because it will not change.
     */
    final int mHash;

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

        final IdentityWeakReferenceKey other = (IdentityWeakReferenceKey) obj;
        return mHash == other.mHash && get() == other.get();
    }

}
