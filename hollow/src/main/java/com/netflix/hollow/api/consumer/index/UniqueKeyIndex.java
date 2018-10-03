package com.netflix.hollow.api.consumer.index;

public interface UniqueKeyIndex<API, T> {
    T findMatch(Object... keys);
}
