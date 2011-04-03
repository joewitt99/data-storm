package datastorm.eventstore.otientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.axonframework.domain.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static datastorm.eventstore.otientdb.OrientEventStoreTestUtils.*;
import static org.junit.Assert.*;

/**
 * Integration test case for {@link OrientEventStore}.
 * This test case tests only  storing and reading events for default cluster.
 *
 * @author EniSh
 */
public class OrientEventStoreTest {
    protected ODatabaseDocumentTx database;
    protected OrientEventStore orientEventStore;

    @Before
    public void setUp() throws Exception {
        database = new ODatabaseDocumentTx("memory:default");
        database.create();
        orientEventStore = new OrientEventStore();
        orientEventStore.setDatabase(database);
    }

    @After
    public void tearDown() throws Exception {
        database.delete();
    }

    @Test
    public void testEventsAppending() {
        final List<SimpleDomainEvent> domainEvents = new ArrayList<SimpleDomainEvent>();
        domainEvents.add(new SimpleDomainEvent(1, agId("1"), "val"));

        orientEventStore.appendEvents("Doc", stream(domainEvents));

        ORecordIteratorClass<ODocument> iteratorClass = database.browseClass("Doc", false);
        assertTrue(iteratorClass.hasNext());
        final ODocument eventDocument = iteratorClass.next();

        final Set<String> fieldNames = eventDocument.fieldNames();
        assertEquals(4, fieldNames.size());

        assertTrue(fieldNames.contains("aggregateIdentifier"));
        assertTrue(fieldNames.contains("sequenceNumber"));
        assertTrue(fieldNames.contains("timestamp"));
        assertTrue(fieldNames.contains("body"));

        assertEquals("1", eventDocument.<String>field("aggregateIdentifier"));
        assertEquals((Long)1L , eventDocument.<Long>field("sequenceNumber"));
        assertEquals(domainEvents.get(0).getTimestamp().toString() ,
                eventDocument.<String>field("timestamp"));

        assertFalse(iteratorClass.hasNext());
    }

    @Test
    public void testEventSchema() {
        final List<SimpleDomainEvent> domainEvents = new ArrayList<SimpleDomainEvent>();
        domainEvents.add(new SimpleDomainEvent(1, agId("1"), "val"));

        orientEventStore.appendEvents("Doc", stream(domainEvents));

        ORecordIteratorClass<ODocument> iteratorClass = database.browseClass("Doc", false);
        assertTrue(iteratorClass.hasNext());
        final ODocument eventDocument = iteratorClass.next();
        final OClass eventClass = eventDocument.getSchemaClass();

        assertNotNull(eventClass);
        assertEquals("Doc", eventClass.getName());

        final OProperty aggregateIdentifierProperty = eventClass.getProperty("aggregateIdentifier");
        assertNotNull(aggregateIdentifierProperty);
        assertTrue(aggregateIdentifierProperty.isMandatory());
        assertTrue(aggregateIdentifierProperty.isNotNull());
        assertEquals(OType.STRING, aggregateIdentifierProperty.getType());

        final OProperty sequenceNumberProperty = eventClass.getProperty("sequenceNumber");
        assertNotNull(sequenceNumberProperty);
        assertTrue(sequenceNumberProperty.isMandatory());
        assertTrue(sequenceNumberProperty.isNotNull());
        assertEquals(OType.LONG, sequenceNumberProperty.getType());

        final OProperty timestampProperty = eventClass.getProperty("timestamp");
        assertNotNull(timestampProperty);
        assertTrue(timestampProperty.isMandatory());
        assertTrue(timestampProperty.isNotNull());
        assertEquals("29", timestampProperty.getMin());
        assertEquals("29", timestampProperty.getMax());
        assertEquals(OType.STRING, timestampProperty.getType());

        final OProperty bodyProperty = eventClass.getProperty("body");
        assertNotNull(bodyProperty);
        assertTrue(bodyProperty.isMandatory());
        assertTrue(bodyProperty.isNotNull());
        assertEquals(OType.BINARY, bodyProperty.getType());

        assertEquals(1, eventClass.getClusterIds().length);
    }

    @Test
    public void testBasicEventsStoring() throws Exception {

        final List<SimpleDomainEvent> domainEvents = createSimpleDomainEvents(new int[] {1, 2},
                new String[]  {"1", "1"});

        orientEventStore.appendEvents("Simple", stream(domainEvents));

        DomainEventStream readEventStream = orientEventStore.readEvents("Simple", agId("1"));

        assertDomainEventsEquality(domainEvents, readEventStream);
    }

    @Test
    public void testEventsSorting() throws Exception {

        final List<SimpleDomainEvent> domainEvents = createSimpleDomainEvents(
                new int[] { 3, 1, 5, 9, 2, 4, 6, 8, 7 },
                new String[] {"1", "1", "1", "1", "1", "1", "1", "1", "1"}
        );

        orientEventStore.appendEvents("Simple", stream(domainEvents));

        DomainEventStream readEventStream = orientEventStore.readEvents("Simple", agId("1"));

        assertDomainEventsEquality(sortBySequenceNumber(domainEvents), readEventStream);
    }

    @Test
    public void testEventsFromDifferentTypesWithSameId() {
        final List<SimpleDomainEvent> domainEventsDocOne = createSimpleDomainEvents(new int[] {1, 2},
                new String[] {"1", "1"});

        final List<SimpleDomainEvent> domainEventsDocTwo = createSimpleDomainEvents(new int[] {1, 2},
                new String[] {"1", "1"});

        orientEventStore.appendEvents("DocOne", stream(domainEventsDocOne));
        orientEventStore.appendEvents("DocTwo", stream(domainEventsDocTwo));

        DomainEventStream readEventStreamDocTwo = orientEventStore.readEvents("DocTwo", agId("1"));

        DomainEventStream readEventStreamDocOne = orientEventStore.readEvents("DocOne", agId("1"));


        assertDomainEventsEquality(domainEventsDocOne, readEventStreamDocOne);
        assertDomainEventsEquality(domainEventsDocTwo, readEventStreamDocTwo);
    }

    @Test
    public void testEventsFromDifferentTypesWithDiffId() {
        final List<SimpleDomainEvent> domainEventsDocOne = createSimpleDomainEvents(new int[] {1, 2},
                  new String[] {"1", "1"});
        final List<SimpleDomainEvent> domainEventsDocTwo = createSimpleDomainEvents(new int[] {1, 2},
                  new String[] {"2", "2"});

        orientEventStore.appendEvents("DocOne", stream(domainEventsDocOne));
        orientEventStore.appendEvents("DocTwo", stream(domainEventsDocTwo));

        DomainEventStream readEventStreamDocTwo = orientEventStore.readEvents("DocTwo", agId("2"));

        DomainEventStream readEventStreamDocOne = orientEventStore.readEvents("DocOne", agId("1"));


        assertDomainEventsEquality(domainEventsDocOne, readEventStreamDocOne);
        assertDomainEventsEquality(domainEventsDocTwo, readEventStreamDocTwo);
    }

    @Test
    public void testEventsWithDiffId() {
        final List<SimpleDomainEvent> domainEventsDocOne = createSimpleDomainEvents(new int[] {1, 2},
                  new String[] {"1", "1"});
        final List<SimpleDomainEvent> domainEventsDocTwo = createSimpleDomainEvents(new int[] {1, 2},
                  new String[] {"2", "2"});

        orientEventStore.appendEvents("Doc", stream(domainEventsDocOne));
        orientEventStore.appendEvents("Doc", stream(domainEventsDocTwo));

        DomainEventStream readEventStreamDocTwo = orientEventStore.readEvents("Doc", agId("2"));

        DomainEventStream readEventStreamDocOne = orientEventStore.readEvents("Doc", agId("1"));

        assertDomainEventsEquality(domainEventsDocOne, readEventStreamDocOne);
        assertDomainEventsEquality(domainEventsDocTwo, readEventStreamDocTwo);
    }

}