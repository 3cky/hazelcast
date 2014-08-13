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

package com.hazelcast.concurrent.semaphore;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.test.HazelcastParallelClassRunner;
import com.hazelcast.test.HazelcastTestSupport;
import com.hazelcast.test.annotation.QuickTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(HazelcastParallelClassRunner.class)
@Category(QuickTest.class)
public class SemaphoreTest extends HazelcastTestSupport {

    private HazelcastInstance hz;

    @Before
    public void setUp() {
        hz = createHazelcastInstance();
    }

    @Test(timeout = 30000)
    public void testAcquire() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore(randomString());

        int numberOfPermits = 20;
        assertTrue(semaphore.init(numberOfPermits));
        for (int i = 0; i < numberOfPermits; i++) {
            assertEquals(numberOfPermits - i, semaphore.availablePermits());
            semaphore.acquire();
        }

        assertEquals(semaphore.availablePermits(), 0);
    }

    @Test(timeout = 30000)
    public void testAcquire_whenNoPermits() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore("testAcquire_whenNoPermits");
        final int numberOfPermits = 10;
        semaphore.init(numberOfPermits);
        semaphore.acquire(numberOfPermits);
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        thread.join(3000);
        assertTrue(thread.isAlive());
        assertEquals(0, semaphore.availablePermits());
    }

    @Test(timeout = 30000)
    public void testAcquire_whenNoPermits_andSemaphoreDestroyed() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore("testRelease_whenBlockedAcquireThread");
        int numberOfPermits = 10;
        semaphore.init(numberOfPermits);
        semaphore.acquire(numberOfPermits);
        new Thread() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        semaphore.destroy();
        assertEquals(0, semaphore.availablePermits());
    }

    @Test(expected = IllegalStateException.class, timeout = 30000)
    public void testAcquire_whenInstanceShutdown() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore(randomString());
        hz.shutdown();
        semaphore.acquire();
    }

    @Test(timeout = 30000)
    public void testRelease() {
        final ISemaphore semaphore = hz.getSemaphore(randomString());

        int numberOfPermits = 20;
        for (int i = 0; i < numberOfPermits; i++) {
            assertEquals(i, semaphore.availablePermits());
            semaphore.release();
        }

        assertEquals(semaphore.availablePermits(), numberOfPermits);
    }

    @Test(timeout = 30000)
    public void testRelease_whenArgumentNegative() {
        final ISemaphore semaphore = hz.getSemaphore("testRelease_whenArgumentNegative");
        try {
            semaphore.release(-5);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        assertEquals(0, semaphore.availablePermits());
    }

    @Test(timeout = 30000)
    public void testRelease_whenBlockedAcquireThread() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore("testRelease_whenBlockedAcquireThread");
        int numberOfPermits = 10;
        semaphore.init(numberOfPermits);
        semaphore.acquire(numberOfPermits);
        new Thread() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        semaphore.release();
        assertEquals(0, semaphore.availablePermits());
    }

    @Test(timeout = 30000)
    public void testMultipleAcquire() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore(randomString());
        int numberOfPermits = 20;

        assertTrue(semaphore.init(numberOfPermits));
        for (int i = 0; i < numberOfPermits; i += 5) {
            assertEquals(numberOfPermits - i, semaphore.availablePermits());
            semaphore.acquire(5);
        }
        assertEquals(semaphore.availablePermits(), 0);
    }

    @Test(timeout = 30000)
    public void testMultipleAcquire_whenNegative() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore("testRelease_whenArgumentNegative");
        int numberOfPermits = 10;
        semaphore.init(numberOfPermits);
        try {
            for (int i = 0; i < numberOfPermits; i += 5) {
                semaphore.acquire(-5);
                fail();
            }
        } catch (IllegalArgumentException expected) {
        }
        assertEquals(10, semaphore.availablePermits());

    }

    @Test(timeout = 30000)
    public void testMultipleAcquire_whenNotEnoughPermits() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore("testMultipleAcquire_whenNotEnoughPermits");
        int numberOfPermits = 10;
        semaphore.init(numberOfPermits);
        final Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    semaphore.acquire(5);
                    assertEquals(5, semaphore.availablePermits());
                    semaphore.acquire(6);
                    assertEquals(5, semaphore.availablePermits());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        thread.join(3000);
        assertTrue(thread.isAlive());
        assertEquals(5, semaphore.availablePermits());
    }

    @Test(timeout = 30000)
    public void testMultipleRelease() {
        final ISemaphore semaphore = hz.getSemaphore(randomString());
        int numberOfPermits = 20;

        for (int i = 0; i < numberOfPermits; i += 5) {
            assertEquals(i, semaphore.availablePermits());
            semaphore.release(5);
        }
        assertEquals(semaphore.availablePermits(), numberOfPermits);
    }

    @Test(timeout = 30000)
    public void testMultipleRelease_whenNegative() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore("testRelease_whenArgumentNegative");
        int numberOfPermits = 10;
        semaphore.init(numberOfPermits);
        semaphore.acquire(numberOfPermits);
        try {
            for (int i = 0; i < numberOfPermits; i += 5) {
                semaphore.release(-5);
                fail();
            }
        } catch (IllegalArgumentException expected) {
        }
        assertEquals(0, semaphore.availablePermits());
    }

    @Test(timeout = 30000)
    public void testMultipleRelease_whenBlockedAcquireThreads() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore("testRelease_whenBlockedAcquireThread");
        int numberOfPermits = 10;
        semaphore.init(numberOfPermits);
        semaphore.acquire(numberOfPermits);
        Thread[] threads = new Thread[4];
        for (int i = 0; i < 4; i++) {
            threads[i] = new BlockAcquireThread();
            threads[i].start();
        }
        semaphore.release();
        assertEquals(0, semaphore.availablePermits());
        semaphore.release();
        assertEquals(0, semaphore.availablePermits());
        semaphore.release();
        assertEquals(0, semaphore.availablePermits());
        semaphore.release();
        assertEquals(0, semaphore.availablePermits());
    }

    private class BlockAcquireThread extends Thread {
        ISemaphore semaphore;

        BlockAcquireThread() {
            semaphore = hz.getSemaphore("testRelease_whenBlockedAcquireThread");
        }

        @Override
        public void run() {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(timeout = 30000)
    public void testDrain() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore(randomString());
        int numberOfPermits = 20;

        assertTrue(semaphore.init(numberOfPermits));
        semaphore.acquire(5);
        int drainedPermits = semaphore.drainPermits();
        assertEquals(drainedPermits, numberOfPermits - 5);
        assertEquals(semaphore.availablePermits(), 0);
    }

    @Test(timeout = 30000)
    public void testDrain_whenNoPermits() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore("testDrain_whenNoPermits");
        int numberOfPermits = 10;
        semaphore.init(numberOfPermits);
        semaphore.acquire(numberOfPermits);
        assertEquals(0, semaphore.drainPermits());
    }

    @Test(timeout = 30000)
    public void testReduce() {
        final ISemaphore semaphore = hz.getSemaphore(randomString());
        int numberOfPermits = 20;

        assertTrue(semaphore.init(numberOfPermits));
        for (int i = 0; i < numberOfPermits; i += 5) {
            assertEquals(numberOfPermits - i, semaphore.availablePermits());
            semaphore.reducePermits(5);
        }

        assertEquals(semaphore.availablePermits(), 0);
    }

    @Test(timeout = 30000)
    public void testReduce_whenArgumentNegative() {
        final ISemaphore semaphore = hz.getSemaphore("testReduce_whenArgumentNegative");
        try {
            semaphore.reducePermits(-5);
            fail();
        } catch (IllegalArgumentException expected) {
        }
        assertEquals(0, semaphore.availablePermits());
    }


    @Test(timeout = 30000)
    public void testTryAcquire() {
        final ISemaphore semaphore = hz.getSemaphore(randomMapName());
        int numberOfPermits = 20;

        assertTrue(semaphore.init(numberOfPermits));
        for (int i = 0; i < numberOfPermits; i++) {
            assertEquals(numberOfPermits - i, semaphore.availablePermits());
            assertEquals(semaphore.tryAcquire(), true);
        }
        assertFalse(semaphore.tryAcquire());
        assertEquals(semaphore.availablePermits(), 0);
    }

    @Test(timeout = 30000)
    public void testTryAcquireMultiple() {
        final ISemaphore semaphore = hz.getSemaphore(randomString());
        int numberOfPermits = 20;

        assertTrue(semaphore.init(numberOfPermits));
        for (int i = 0; i < numberOfPermits; i += 5) {
            assertEquals(numberOfPermits - i, semaphore.availablePermits());
            assertEquals(semaphore.tryAcquire(5), true);
        }

        assertEquals(semaphore.availablePermits(), 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 30000)
    public void testTryAcquireMultiple_whenArgumentNegative() {
        final ISemaphore semaphore = hz.getSemaphore(randomString());
        int numberOfPermits = 10;
        int negativePermits = -5;

        semaphore.init(numberOfPermits);
        for (int i = 0; i < numberOfPermits; i += 5) {
            assertEquals(numberOfPermits - i, semaphore.availablePermits());
            assertEquals(semaphore.tryAcquire(negativePermits), true);
        }
    }

    @Test(timeout = 30000)
    public void testTryAcquire_whenNotEnoughPermits() throws InterruptedException {
        final ISemaphore semaphore = hz.getSemaphore(randomString());
        int numberOfPermits = 10;

        semaphore.init(numberOfPermits);
        semaphore.acquire(10);
        assertFalse(semaphore.tryAcquire(1));
    }


    @Test(timeout = 30000)
    public void testInit_whenNotIntialized() {
        ISemaphore semaphore = hz.getSemaphore(randomString());

        boolean result = semaphore.init(2);

        assertTrue(result);
        assertEquals(2, semaphore.availablePermits());
    }

    @Test(timeout = 30000)
    public void testInit_whenAlreadyIntialized() {
        ISemaphore semaphore = hz.getSemaphore(randomString());
        semaphore.init(2);

        boolean result = semaphore.init(4);

        assertFalse(result);
        assertEquals(2, semaphore.availablePermits());
    }
}
