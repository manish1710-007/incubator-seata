/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.seata.common.thread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.util.concurrent.FastThreadLocalThread;
import io.seata.common.util.CollectionUtils;

/**
 * The type Named thread factory.
 *
 */
public class NamedThreadFactory implements ThreadFactory {
    private final static Map<String, AtomicInteger> PREFIX_COUNTER = new ConcurrentHashMap<>();
    private final ThreadGroup group;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String prefix;
    private final int totalSize;
    private final boolean makeDaemons;

    /**
     * Instantiates a new Named thread factory.
     *
     * @param prefix      the prefix
     * @param totalSize   the total size
     * @param makeDaemons the make daemons
     */
    public NamedThreadFactory(String prefix, int totalSize, boolean makeDaemons) {
        int prefixCounter = CollectionUtils.computeIfAbsent(PREFIX_COUNTER, prefix, key -> new AtomicInteger(0))
                .incrementAndGet();
        SecurityManager securityManager = System.getSecurityManager();
        group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.prefix = prefix + "_" + prefixCounter;
        this.makeDaemons = makeDaemons;
        this.totalSize = totalSize;
    }

    /**
     * Instantiates a new Named thread factory.
     *
     * @param prefix      the prefix
     * @param makeDaemons the make daemons
     */
    public NamedThreadFactory(String prefix, boolean makeDaemons) {
        this(prefix, 0, makeDaemons);
    }

    /**
     * Instantiates a new Named thread factory.
     *
     * @param prefix    the prefix
     * @param totalSize the total size
     */
    public NamedThreadFactory(String prefix, int totalSize) {
        this(prefix, totalSize, true);
    }

    @Override
    public Thread newThread(Runnable r) {
        String name = prefix + "_" + counter.incrementAndGet();
        if (totalSize > 1) {
            name += "_" + totalSize;
        }
        Thread thread = new FastThreadLocalThread(group, r, name);

        thread.setDaemon(makeDaemons);
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            thread.setPriority(Thread.NORM_PRIORITY);
        }
        return thread;
    }
}
