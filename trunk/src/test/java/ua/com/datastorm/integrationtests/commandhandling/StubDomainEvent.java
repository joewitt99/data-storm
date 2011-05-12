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

package ua.com.datastorm.integrationtests.commandhandling;

import org.axonframework.domain.AggregateIdentifier;
import org.axonframework.domain.DomainEvent;

import java.io.Serializable;

/**
 * @author Allard Buijze
 */
public class StubDomainEvent extends DomainEvent implements Serializable {

    private static final long serialVersionUID = 834667054977749990L;

    public StubDomainEvent(AggregateIdentifier aggregateIdentifier, long sequenceNumber) {
        super(sequenceNumber, aggregateIdentifier);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StubDomainEvent aggregate [");
        sb.append(getAggregateIdentifier());
        sb.append("] sequenceNo [");
        sb.append(getSequenceNumber());
        sb.append("]");
        return sb.toString();
    }
}
