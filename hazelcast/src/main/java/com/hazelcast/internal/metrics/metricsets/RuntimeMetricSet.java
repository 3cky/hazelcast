/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.internal.metrics.metricsets;

import com.hazelcast.internal.metrics.MetricsRegistry;
import com.hazelcast.internal.metrics.LongProbe;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import static com.hazelcast.util.Preconditions.checkNotNull;

/**
 * A Metric pack for exposing {@link java.lang.Runtime} metrics.
 */
public final class RuntimeMetricSet {

    private RuntimeMetricSet() {
    }

    /**
     * Registers all the metrics in this metrics pack.
     *
     * @param metricsRegistry the MetricsRegistry the metrics are registered on.
     */
    public static void register(MetricsRegistry metricsRegistry) {
        checkNotNull(metricsRegistry, "metricsRegistry");

        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();

        metricsRegistry.register(runtime, "runtime.freeMemory", new LongProbe<Runtime>() {
                    @Override
                    public long get(Runtime runtime) {
                        return runtime.freeMemory();
                    }
                }
        );

        metricsRegistry.register(runtime, "runtime.totalMemory", new LongProbe<Runtime>() {
                    @Override
                    public long get(Runtime runtime) {
                        return runtime.totalMemory();
                    }
                }
        );

        metricsRegistry.register(runtime, "runtime.maxMemory", new LongProbe<Runtime>() {
                    @Override
                    public long get(Runtime runtime) {
                        return runtime.maxMemory();
                    }
                }
        );

        metricsRegistry.register(runtime, "runtime.usedMemory", new LongProbe<Runtime>() {
                    @Override
                    public long get(Runtime runtime) {
                        return runtime.totalMemory() - runtime.freeMemory();
                    }
                }
        );

        metricsRegistry.register(runtime, "runtime.availableProcessors", new LongProbe<Runtime>() {
                    @Override
                    public long get(Runtime runtime) {
                        return runtime.availableProcessors();
                    }
                }
        );

        metricsRegistry.register(mxBean, "runtime.uptime", new LongProbe<RuntimeMXBean>() {
                    @Override
                    public long get(RuntimeMXBean runtimeMXBean) {
                        return runtimeMXBean.getUptime();
                    }
                }
        );
    }
}
