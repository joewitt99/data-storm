/*
 * Copyright (c) 2010. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package datastorm.integrationtests.eventstore.benchmark;

import datastorm.integrationtests.commandhandling.StubDomainEvent;
import org.axonframework.domain.AggregateIdentifier;
import org.axonframework.domain.DomainEvent;
import org.axonframework.domain.SimpleDomainEventStream;
import org.axonframework.eventstore.EventStore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for benchmarks of eventstore implementations.
 *
 * @author Jettro Coenradie
 */
public abstract class AbstractEventStoreBenchmark {

    private static final int THREAD_COUNT = 100;   //100
    private static final int TRANSACTION_COUNT = 100;    //500
    private static final int TRANSACTION_SIZE = 10;       //2

    protected abstract void prepareEventStore();

    protected static AbstractEventStoreBenchmark prepareBenchMark(String... appContexts) {
        Assert.notEmpty(appContexts);
        ApplicationContext context = new ClassPathXmlApplicationContext(appContexts);
        return context.getBean(AbstractEventStoreBenchmark.class);
    }

    public void startBenchMark() throws InterruptedException {
        prepareEventStore();

        long start = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<Thread>();
        for (int t = 0; t < getThreadCount(); t++) {
            Thread thread = new Thread(getRunnableInstance());
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            thread.join();
        }
        long end = System.currentTimeMillis();

        System.out.println(String.format(
                "Result: %s threads concurrently wrote %s * %s events each in %s milliseconds. That is an average of %.0f events per second",
                getThreadCount(),
                getTransactionCount(),
                getTransactionSize(),
                (end - start),
                (((float) getThreadCount() * getTransactionCount() * getTransactionSize()) / ((float) (end - start)
                        / 1000))));

    }

    protected abstract Runnable getRunnableInstance();

    protected int saveAndLoadLargeNumberOfEvents(AggregateIdentifier aggregateId, EventStore eventStore,
                                                 int eventSequence) {
        List<DomainEvent> events = new ArrayList<DomainEvent>();
        for (int t = 0; t < getTransactionSize(); t++) {
            events.add(new StubDomainEvent(aggregateId, eventSequence++));
        }
        eventStore.appendEvents("benchmark", new SimpleDomainEventStream(events));
        return eventSequence;
    }

    protected int getThreadCount() {
        return THREAD_COUNT;
    }

    protected int getTransactionCount() {
        return TRANSACTION_COUNT;
    }

    protected int getTransactionSize() {
        return TRANSACTION_SIZE;
    }
}
