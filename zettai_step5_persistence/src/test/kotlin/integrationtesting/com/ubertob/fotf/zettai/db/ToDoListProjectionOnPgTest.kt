package integrationtesting.com.ubertob.fotf.zettai.db

import com.ubertob.fotf.zettai.db.createToDoListEventStreamerOnPg
import com.ubertob.fotf.zettai.db.jdbc.TransactionIsolationLevel
import com.ubertob.fotf.zettai.db.jdbc.TransactionProvider
import com.ubertob.fotf.zettai.domain.pgDataSourceForTest
import com.ubertob.fotf.zettai.domain.tooling.expectSuccess
import com.ubertob.fotf.zettai.events.ToDoListEvent
import com.ubertob.fotf.zettai.queries.ToDoListProjection
import com.ubertob.fotf.zettai.queries.ToDoListProjectionAbstractTest
import com.ubertob.fotf.zettai.queries.ToDoListProjectionOnPg

internal class ToDoListProjectionOnPgTest : ToDoListProjectionAbstractTest() {

    val dataSource = pgDataSourceForTest()
    val txProvider = TransactionProvider(dataSource, TransactionIsolationLevel.ReadCommitted)
    val streamer = createToDoListEventStreamerOnPg()
    val projection = ToDoListProjectionOnPg(txProvider) { txProvider.tryRun(streamer.fetchAfter(it)) }

    override fun buildListProjection(events: List<ToDoListEvent>): ToDoListProjection =
        projection.apply {
            txProvider.tryRun(streamer.store(events)).expectSuccess()
            update()
        }
}