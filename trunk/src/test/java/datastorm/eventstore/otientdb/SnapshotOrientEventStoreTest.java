package datastorm.eventstore.otientdb;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OProperty;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.axonframework.domain.DomainEventStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static datastorm.eventstore.otientdb.OrientEventStoreTestUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 */
public class SnapshotOrientEventStoreTest {
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
    public void testEventSchema() {
        orientEventStore.appendSnapshotEvent("Simple", new SimpleDomainEvent(1, agId("1"), "val"));

        ORecordIteratorClass<ODocument> iteratorClass = database.browseClass(SnapshotEventEntry.SNAPSHOT_EVENT_CLASS,
                false);
        assertTrue(iteratorClass.hasNext());
        final ODocument eventDocument = iteratorClass.next();
        final OClass eventClass = eventDocument.getSchemaClass();

        assertSnapshotEventSchema(eventClass);

        assertEquals(1, eventClass.getClusterIds().length);
    }

    @Test
    public void testStoringWithSnapshot() {
        final List<SimpleDomainEvent> firstDomainEvents = createSimpleDomainEvents(new int[] {1, 2},
                        new String[]  {"1", "1"});
        orientEventStore.appendEvents("Aggregatte", stream(firstDomainEvents));

        orientEventStore.appendSnapshotEvent("Aggregatte", new SimpleDomainEvent(3, agId("1"), "val"));

        final List<SimpleDomainEvent> secondDomainEvents = createSimpleDomainEvents(new int[] {4, 5},
                         new String[]  {"1", "1"});
        orientEventStore.appendEvents("Aggregatte", stream(secondDomainEvents));

        final SimpleDomainEvent snapshotEvent = new SimpleDomainEvent(6, agId("1"), "val");

        orientEventStore.appendSnapshotEvent("Aggregatte", snapshotEvent);

        final List<SimpleDomainEvent> thirdDomainEvents = createSimpleDomainEvents(new int[] {7, 8},
                         new String[]  {"1", "1"});
        orientEventStore.appendEvents("Aggregatte", stream(thirdDomainEvents));

        final DomainEventStream readStream = orientEventStore.readEvents("Aggregatte", agId("1"));

        final List<SimpleDomainEvent> resultEvents = new ArrayList<SimpleDomainEvent>();
        resultEvents.add(snapshotEvent);
        resultEvents.addAll(thirdDomainEvents);

        assertDomainEventsEquality(resultEvents, readStream);
    }

    @Test
    public void testSortingWithSnapshot() {
        final List<SimpleDomainEvent> firstDomainEvents = createSimpleDomainEvents(new int[] {1, 8},
                        new String[]  {"1", "1"});
        orientEventStore.appendEvents("Aggregatte", stream(firstDomainEvents));

        orientEventStore.appendSnapshotEvent("Aggregatte", new SimpleDomainEvent(3, agId("1"), "val"));

        final List<SimpleDomainEvent> secondDomainEvents = createSimpleDomainEvents(new int[] {4, 7},
                         new String[]  {"1", "1"});
        orientEventStore.appendEvents("Aggregatte", stream(secondDomainEvents));

        final SimpleDomainEvent snapshotEvent = new SimpleDomainEvent(6, agId("1"), "val");

        orientEventStore.appendSnapshotEvent("Aggregatte", snapshotEvent);

        final List<SimpleDomainEvent> thirdDomainEvents = createSimpleDomainEvents(new int[] {5, 2},
                         new String[]  {"1", "1"});
        orientEventStore.appendEvents("Aggregatte", stream(thirdDomainEvents));

        final DomainEventStream readStream = orientEventStore.readEvents("Aggregatte", agId("1"));

        final List<SimpleDomainEvent> resultEvents = new ArrayList<SimpleDomainEvent>();
        resultEvents.add(snapshotEvent);
        resultEvents.add(secondDomainEvents.get(1));
        resultEvents.add(firstDomainEvents.get(1));

        assertDomainEventsEquality(resultEvents, readStream);
    }

}
