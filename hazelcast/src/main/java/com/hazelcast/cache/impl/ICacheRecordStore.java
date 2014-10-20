/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.cache.impl;

import com.hazelcast.cache.impl.record.CacheRecord;
import com.hazelcast.config.CacheConfig;
import com.hazelcast.map.impl.MapEntrySet;
import com.hazelcast.nio.serialization.Data;

import javax.cache.expiry.ExpiryPolicy;
import javax.cache.processor.EntryProcessor;
import java.util.Map;
import java.util.Set;

/**
 * {@link ICacheRecordStore} is the core contract providing internal functionality to {@link com.hazelcast.cache.ICache}
 * implementations on partition scope. All of the ICache methods actually maps to a method on this interface through Hazelcast's
 * rpc mechanism. Hazelcast {@link com.hazelcast.spi.Operation}'s send to the relevant partition to be executed and the final
 * results returned to the callers.
 *
 * For each partition there is only one {@link ICacheRecordStore} in the cluster.
 * <p>Implementations of this interface may provide different internal data persistence like on-heap storage</p>
 * Each expirible cache entry is actually a {@link Data}, {@link CacheRecord} pair.
 * <p>Key type: always serialized form of {@link Data}</p>
 * <p>Value types: depend on the configuration</p>
 *
 * @see com.hazelcast.cache.impl.CacheRecordStore
 */
public interface ICacheRecordStore {

    /**
     * Gets the value to which the specified key is mapped,
     * or {@code null} if this cache contains no mapping for the key.
     * <p>
     * If the cache is configured to use read-through, and get would return null
     * because the entry is missing from the cache, the Cache's {@link javax.cache.integration.CacheLoader}
     * is called in an attempt to load the entry.
     * </p>
     * @param key key
     * @param expiryPolicy custom expiry policy or null to use configured default value.
     * @return value in previously configured type
     */
    Object get(Data key, ExpiryPolicy expiryPolicy);

    /**
     * Associates the specified value with the specified key in this cache,
     * returning an existing value if one existed.
     * <p>
     * If the cache previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A cache
     * <tt>c</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #contains(Data) c.contains(k)} would return
     * <tt>true</tt>.)
     * <p>
     * The previous value is returned, or null if there was no value associated
     * with the key previously.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param expiryPolicy custom expiry policy or null to use configured default value.
     * @param caller  uuid of the calling node or client
     * @return the value associated with the key at the start of the operation or
     *         null if none was associated
     */
    void put(Data key, Object value, ExpiryPolicy expiryPolicy, String caller);

    /**
     * Associates the specified value with the specified key in this cache,
     * returning an existing value if one existed.
     * <p>
     * If the cache previously contained a mapping for
     * the key, the old value is replaced by the specified value.  (A cache
     * <tt>c</tt> is said to contain a mapping for a key <tt>k</tt> if and only
     * if {@link #contains(Data) c.contains(k)} would return
     * <tt>true</tt>.)
     * <p>
     * The previous value is returned, or null if there was no value associated
     * with the key previously.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @param expiryPolicy custom expiry policy or null to use configured default value.
     * @param caller  uuid of the calling node or client
     * @return the value associated with the key at the start of the operation or
     *         null if none was associated
     */
    Object getAndPut(Data key, Object value, ExpiryPolicy expiryPolicy, String caller);

    /**
     /**
     * Removes the mapping for a key from this cache if it is present.
     * <p>
     * More formally, if this cache contains a mapping from key <tt>k</tt> to
     * value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping is removed.
     * (The cache can contain at most one such mapping.)
     *
     * <p>Returns <tt>true</tt> if this cache previously associated the key,
     * or <tt>false</tt> if the cache contained no mapping for the key.
     * <p>
     * The cache will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the cache
     * @param expiryPolicy custom expiry policy or null to use configured default value.
     * @param caller  uuid of the calling node or client
     * @return returns false if there was no matching key
     */
    boolean putIfAbsent(Data key, Object value, ExpiryPolicy expiryPolicy, String caller);

    /**
     * Atomically removes the entry for a key only if currently mapped to some
     * value.
     * <p>
     * This is equivalent to:
     * <pre><code>
     * if (cache.containsKey(key)) {
     *   V oldValue = cache.get(key);
     *   cache.remove(key);
     *   return oldValue;
     * } else {
     *   return null;
     * }
     * </code></pre>
     * except that the action is performed atomically.
     *
     * @param key key with which the specified value is associated
     * @param caller  uuid of the calling node or client
     * @return the value if one existed or null if no mapping existed for this key
     */
    Object getAndRemove(Data key, String caller);

    /**
     * Removes the mapping for a key from this cache if it is present.
     * <p>
     * More formally, if this cache contains a mapping from key <tt>k</tt> to
     * value <tt>v</tt> such that
     * <code>(key==null ?  k==null : key.equals(k))</code>, that mapping is removed.
     * (The cache can contain at most one such mapping.)
     *
     * <p>Returns <tt>true</tt> if this cache previously associated the key,
     * or <tt>false</tt> if the cache contained no mapping for the key.
     * <p>
     * The cache will not contain a mapping for the specified key once the
     * call returns.
     *
     * @param key key whose mapping is to be removed from the cache
     * @param caller  uuid of the calling node or client
     * @return returns false if there was no matching key
     * @return
     */
    boolean remove(Data key, String caller);

    /**
     * Atomically removes the mapping for a key only if currently mapped to the
     * given value.
     * <p>
     * This is equivalent to:
     * <pre><code>
     * if (cache.containsKey(key) &amp;&amp; equals(cache.get(key), oldValue) {
     *   cache.remove(key);
     *   return true;
     * } else {
     *   return false;
     * }
     * </code></pre>
     * except that the action is performed atomically.
     *
     * @param key key whose mapping is to be removed from the cache
     * @param value value expected to be associated with the specified key
     * @param caller  uuid of the calling node or client
     * @return returns false if there was no matching key
     */
    boolean remove(Data key, Object value, String caller);

    /**
     * Atomically replaces the entry for a key only if currently mapped to some
     * value.
     * <p>
     * This is equivalent to
     * <pre><code>
     * if (cache.containsKey(key)) {
     *   cache.put(key, value);
     *   return true;
     * } else {
     *   return false;
     * }</code></pre>
     * except that the action is performed atomically.
     *
     * @param key  the key with which the specified value is associated
     * @param value the value to be associated with the specified key
     * @param expiryPolicy custom expiry policy or null to use configured default value.
     * @param caller  uuid of the calling node or client
     * @return <tt>true</tt> if the value was replaced
     */
    boolean replace(Data key, Object value, ExpiryPolicy expiryPolicy, String caller);

    /**
     * Atomically replaces the entry for a key only if currently mapped to a
     * given value.
     * <p>
     * This is equivalent to:
     * <pre><code>
     * if (cache.containsKey(key) &amp;&amp; equals(cache.get(key), oldValue)) {
     *  cache.put(key, newValue);
     * return true;
     * } else {
     *  return false;
     * }
     * </code></pre>
     * except that the action is performed atomically.
     *
     * @param key      key with which the specified value is associated
     * @param oldValue value expected to be associated with the specified key
     * @param newValue value to be associated with the specified key
     * @param expiryPolicy custom expiry policy or null to use configured default value.
     * @param caller  uuid of the calling node or client
     * @return <tt>true</tt> if the value was replaced
     */
    boolean replace(Data key, Object oldValue, Object newValue, ExpiryPolicy expiryPolicy, String caller);

    /**
     * Atomically replaces the value for a given key if and only if there is a
     * value currently mapped by the key.
     * <p>
     * This is equivalent to
     * <pre><code>
     * if (cache.containsKey(key)) {
     *   V oldValue = cache.get(key);
     *   cache.put(key, value);
     *   return oldValue;
     * } else {
     *   return null;
     * }
     * </code></pre>
     * except that the action is performed atomically.
     *
     * @param key   key with which the specified value is associated
     * @param value value to be associated with the specified key
     * @param expiryPolicy custom expiry policy or null to use configured default value.
     * @param caller  uuid of the calling node or client
     * @return the previous value associated with the specified key, or
     *         <tt>null</tt> if there was no mapping for the key.
     */
    Object getAndReplace(Data key, Object value, ExpiryPolicy expiryPolicy, String caller);

    /**
     * Determines if this store contains an entry for the specified key.
     * <p>
     * More formally, returns <tt>true</tt> if and only if this store contains a
     * mapping for a key <tt>k</tt> such that <tt>key.equals(k)</tt>.
     * (There can be at most one such mapping.)
     *
     * @param key key whose presence in this store is to be tested.
     * @return <tt>true</tt> if this map contains a mapping for the specified key
     */
    boolean contains(Data key);

    /**
     * Gets a collection of entries from the store, returning them as
     * {@link Map} of the values associated with the set of keys requested.
     * <p>
     * If the cache is configured read-through, and a get for a key would
     * return null because an entry is missing from the cache, the Cache's
     * {@link javax.cache.integration.CacheLoader} is called in an attempt to load the entry. If an
     * entry cannot be loaded for a given key, the key will not be present in
     * the returned Map.
     *
     * @param keySet The keys whose associated values are to be returned.
     * @param expiryPolicy custom expiry policy or null to use configured default value.
     * @return A map of entries that were found for the given keys. Keys not found
     *         in the cache are not in the returned map.
     */
    MapEntrySet getAll(Set<Data> keySet, ExpiryPolicy expiryPolicy);

    /**
     *  Calculates the entry size of this store which reflects the partition size of the cache
      * @return partition size of the cache
     */
    int size();

    /**
     * Provides two functionality depending on the isRemoveAll parameter.
     * <p>
     * if isRemoveAll is <tt>true</tt>, keys will be ignored and internal data will be removed without publishing any events.
     * <p>
     * if isRemoveAll is <tt>false</tt>, record of keys will be deleted one by one and publish a REMOVE event for each key.
     * If the provided keys set is empty, then it will function as delete all keys to provide an easy shortcut.
     * </p>
     * @param keys Set of keys to be cleaned
     * @param isRemoveAll <tt>true</tt> value forces a clean all without events
     */
    void clear(Set<Data> keys, boolean isRemoveAll);

    /**
     * Destroy is equivalent of these in order.
     * <ul>
     *     <li>Clear All</li>
     *     <li>close resources</li>
     *     <li>unregister all listeners</li>
     * </ul>
     */
    void destroy();

    /**
     * Gets the configuration of the cache that this store belongs to.
     * @return
     */
    CacheConfig getConfig();

    /**
     * Gets the name of the distributed object name of the cache
     * @return name
     */
    String getName();

    /**
     * Returns a readonly map of the internal key value store
     * @return readonly map of the internal key value store
     */
    Map<Data, CacheRecord> getReadOnlyRecords();

    /**
     * Get internal record of the store by key
     * @param key the key to the entry
     * @return {@link CacheRecord} instance mapped
     */
    CacheRecord getRecord(Data key);

    /**
     * Associates the specified record with the specified key.
     * This is simply a put operation on the internal map data
     * without any CacheLoad
     *
     * @param key
     * @param record
     */
    void setRecord(Data key, CacheRecord record);

    /**
     * Removes the record for a key
     * @param key the key to the entry
     * @return the removed record if one exists
     */
    CacheRecord removeRecord(Data key);

    /**
     * Starting from the provided table index, a set of keys returned with maximum size is <code>size</code>
     * @param tableIndex initial table index
     * @param size maximum key set size
     * @return {@link CacheKeyIteratorResult} which wraps keys and last tableIndex
     */
    CacheKeyIteratorResult iterator(int tableIndex, int size);

    /**
     * Invokes an {@link EntryProcessor} against the {@link javax.cache.Cache.Entry} specified by
     * the provided key. If an {@link javax.cache.Cache.Entry} does not exist for the specified key,
     * an attempt is made to load it (if a loader is configured) or a surrogate
     * {@link javax.cache.Cache.Entry}, consisting of the key with a null value is used instead.
     * <p>
     *
     * @param key            the key to the entry
     * @param entryProcessor the {@link EntryProcessor} to invoke
     * @param arguments      additional arguments to pass to the
     *                       {@link EntryProcessor}
     * @return the result of the processing, if any, defined by the
     *         {@link EntryProcessor} implementation
     */
    Object invoke(Data key, EntryProcessor entryProcessor, Object[] arguments);

    /**
     * Synchronously loads the specified entries into the cache using the
     * configured {@link javax.cache.integration.CacheLoader} for the given keys.
     * <p>
     * If an entry for a key already exists in the Cache, a value will be loaded
     * if and only if <code>replaceExistingValues</code> is true.   If no loader
     * is configured for the cache, no objects will be loaded.
     *
     * @param keys                  the keys to load
     * @param replaceExistingValues when true existing values in the Cache will
     *                              be replaced by those loaded from a CacheLoader
     * @return Set of keys which are successfully loaded.
     */
    Set<Data> loadAll(Set<Data> keys, boolean replaceExistingValues);

    /**
     * Gets the Cache statistics associated with this {@link com.hazelcast.cache.impl.CacheService}
     * @return {@link CacheStatisticsImpl} cache statistics
     */
    CacheStatisticsImpl getCacheStats();

    /**
     * Publish a Completion Event.
     * <p>Synchronous Event Listeners requires Completion Event to understand all events executed and it is ready to proceed
     * the method call as this Completion events always received at the end.</p>
     * @param cacheName  cache name
     * @param completionId completion Id of the caller method
     * @param dataKey  the key
     * @param orderKey order key, all events of a method call will share same order key
     */
    void publishCompletedEvent(String cacheName, int completionId, Data dataKey, int orderKey);

}
