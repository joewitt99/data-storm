package datastorm.eventstore.otientdb;

import com.google.common.primitives.Ints;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import org.axonframework.domain.*;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Utility class that is used to make OrientDb EventStore tests shorter and much more readable by leveraging
 * static imports composite assertions and factory methods.
 */
abstract class OrientEventStoreTestUtils {

    /**
     * Generates list of {@link SimpleDomainEvent}s by passed in sequence numbers and aggregate IDs.
     * Each sequence number and Aggregate identifier are mapped one by one. {@link StringAggregateIdentifier} will
     * be created from passed in Aggregate IDs. {@link datastorm.eventstore.otientdb.SimpleDomainEvent#getValue()}
     * property will be auto generated.
     *
     *
     * @param sequenceNumbers Array of sequence numbers.
     * @param ids             Array of Aggregate IDs.
     * @return                List of {@link SimpleDomainEvent}s created from passed in data.
     */
    public static List<SimpleDomainEvent> createSimpleDomainEvents(int sequenceNumbers[], String[] ids) {
        if (sequenceNumbers.length != ids.length) {
            throw new IllegalArgumentException("Amount of sequence numbers should be equal to" +
                    " amount of aggregate IDs");
        }
        final List<SimpleDomainEvent> domainEvents = new ArrayList<SimpleDomainEvent>(ids.length);
        for (int i = 0; i < ids.length; i++) {
            domainEvents.add(new SimpleDomainEvent(sequenceNumbers[i],
                    new StringAggregateIdentifier(ids[i]),
                    "val" + i + "-" + ids[i]));
        }
        return domainEvents;
    }

    /**
     * Converts list of {@link DomainEvent}s into {@link DomainEventStream}.
     * @param domainEvents List of {@link DomainEvent}s.
     * @return             {@link DomainEventStream} that contains passed in DomainEvents.
     */
    public static DomainEventStream stream(List<? extends DomainEvent> domainEvents) {
        return new SimpleDomainEventStream(domainEvents);
    }

    /**
     * Checks that {@link DomainEventStream} contains only DomainEvents form the passed in list.
     * equals method will be used to check equality.
     *
     * @param appendedEvents List of DomainEvents that should be contained into the stream.
     * @param readEvents     {@link DomainEventStream} to be checked.
     */
    public static void assertDomainEventsEquality(List<? extends DomainEvent> appendedEvents,
                                                  DomainEventStream readEvents) {
        for (DomainEvent appendedEvent : appendedEvents) {
            assertTrue(readEvents.hasNext());
            final DomainEvent readEvent = readEvents.next();
            assertEquals(appendedEvent, readEvent);
        }
        assertFalse(readEvents.hasNext());
    }

    /**
     * Sort passed in list of {@link DomainEvent}s by sequence number.
     * Original list will be untouched.
     *
     * @param domainEvents List of {@link DomainEvent}s to be sorted.
     * @return             Sorted copy of {@link DomainEvent}s list.
     */
    public static List<? extends DomainEvent> sortBySequenceNumber(List<? extends DomainEvent> domainEvents) {
        List<? extends DomainEvent> copiedEvents = new ArrayList<DomainEvent>(domainEvents);
        Collections.sort(copiedEvents, new Comparator<DomainEvent>() {
            @Override
            public int compare(DomainEvent eventOne, DomainEvent eventTwo) {
                return eventOne.getSequenceNumber().compareTo(eventTwo.getSequenceNumber());
            }
        });
        return copiedEvents;
    }

    /**
     * Creates {@link AggregateIdentifier} form its String presentation.
     *
     * @param   id  String presentation of {@link AggregateIdentifier}.
     * @return  Instance of {@link AggregateIdentifier}.
     */
    public static AggregateIdentifier agId(String id) {
        return new StringAggregateIdentifier(id);
    }

    /**
     * Checks that cluster names that are passed in as array of strings were added.
     * @param beforeClusters  List of clusters before operation.
     * @param afterClusters   List of clusters after operation.
     * @param clusterNames    Names of clusters that should be added.
     */
    public static void assertClusterNames(Collection<String> beforeClusters,
                                          Collection<String> afterClusters, String[] clusterNames) {
        assertEquals(beforeClusters.size() + clusterNames.length, afterClusters.size());
        for (String clusterName : clusterNames) {
            assertFalse(beforeClusters.contains(clusterName));
            assertTrue(afterClusters.contains(clusterName));
        }
    }

    /**
     * Check that OrientDB class associated only with clusters which names are passed as array od strings.
     *
     * @param expectedClusterNames Cluster names to which class should be associated.
     * @param className            OrientDB class name.
     * @param database             Database where class should be defined.
     */
    public static void assertClassHasClusterIds(String[] expectedClusterNames, String className,
                                         ODatabaseDocument database) {
        final OClass oClass = database.getMetadata().getSchema().getClass(className);
        assertEquals(expectedClusterNames.length, oClass.getClusterIds().length);
        final List<Integer> clusterIds = Ints.asList(oClass.getClusterIds());
        for(final String expectedClusterName : expectedClusterNames) {
            int expectedClusterId = database.getClusterIdByName(expectedClusterName);
            assertTrue(clusterIds.contains(expectedClusterId));
        }
    }
}
