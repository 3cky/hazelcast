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

package com.hazelcast.mapreduce.aggregation.impl;

import com.hazelcast.mapreduce.Collator;
import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;
import com.hazelcast.mapreduce.Mapper;
import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Supplier;

import java.math.BigInteger;
import java.util.Map;

public class BigIntegerAvgAggregation<Key, Value>
        implements AggType<Key, Value, Key, BigInteger, AvgTuple<Long, BigInteger>, AvgTuple<Long, BigInteger>, BigInteger> {

    @Override
    public Collator<Map.Entry<Key, AvgTuple<Long, BigInteger>>, BigInteger> getCollator() {
        return new Collator<Map.Entry<Key, AvgTuple<Long, BigInteger>>, BigInteger>() {
            @Override
            public BigInteger collate(Iterable<Map.Entry<Key, AvgTuple<Long, BigInteger>>> values) {
                long count = 0;
                BigInteger amount = BigInteger.ZERO;
                for (Map.Entry<Key, AvgTuple<Long, BigInteger>> entry : values) {
                    AvgTuple<Long, BigInteger> tuple = entry.getValue();
                    count += tuple.getFirst();
                    amount = amount.add(tuple.getSecond());
                }
                return amount.divide(BigInteger.valueOf(count));
            }
        };
    }

    @Override
    public Mapper<Key, Value, Key, BigInteger> getMapper(Supplier<Key, Value, BigInteger> supplier) {
        return new SupplierConsumingMapper<Key, Value, BigInteger>(supplier);
    }

    @Override
    public CombinerFactory<Key, BigInteger, AvgTuple<Long, BigInteger>> getCombinerFactory() {
        return new BigIntegerAvgCombinerFactory<Key>();
    }

    @Override
    public ReducerFactory<Key, AvgTuple<Long, BigInteger>, AvgTuple<Long, BigInteger>> getReducerFactory() {
        return new BigIntegerAvgReducerFactory<Key>();
    }

    static final class BigIntegerAvgCombinerFactory<Key>
            implements CombinerFactory<Key, BigInteger, AvgTuple<Long, BigInteger>> {

        @Override
        public Combiner<Key, BigInteger, AvgTuple<Long, BigInteger>> newCombiner(Key key) {
            return new BigIntegerAvgCombiner<Key>();
        }
    }

    static final class BigIntegerAvgReducerFactory<Key>
            implements ReducerFactory<Key, AvgTuple<Long, BigInteger>, AvgTuple<Long, BigInteger>> {

        @Override
        public Reducer<Key, AvgTuple<Long, BigInteger>, AvgTuple<Long, BigInteger>> newReducer(Key key) {
            return new BigIntegerAvgReducer<Key>();
        }
    }

    private static final class BigIntegerAvgCombiner<Key>
            extends Combiner<Key, BigInteger, AvgTuple<Long, BigInteger>> {

        private long count;
        private BigInteger amount = BigInteger.ZERO;

        @Override
        public void combine(Key key, BigInteger value) {
            count++;
            amount = amount.add(value);
        }

        @Override
        public AvgTuple<Long, BigInteger> finalizeChunk() {
            long count = this.count;
            BigInteger amount = this.amount;
            this.count = 0;
            this.amount = BigInteger.ZERO;
            return new AvgTuple<Long, BigInteger>(count, amount);
        }
    }

    private static final class BigIntegerAvgReducer<Key>
            extends Reducer<Key, AvgTuple<Long, BigInteger>, AvgTuple<Long, BigInteger>> {

        private volatile long count;
        private volatile BigInteger amount = BigInteger.ZERO;

        @Override
        public void reduce(AvgTuple<Long, BigInteger> value) {
            count += value.getFirst();
            amount = amount.add(value.getSecond());
        }

        @Override
        public AvgTuple<Long, BigInteger> finalizeReduce() {
            return new AvgTuple<Long, BigInteger>(count, amount);
        }
    }
}
