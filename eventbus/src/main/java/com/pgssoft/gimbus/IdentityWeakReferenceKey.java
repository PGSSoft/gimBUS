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
