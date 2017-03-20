package labeling;

import com.bigdata.rdf.sail.webapp.client.IPreparedTupleQuery;
import com.bigdata.rdf.sail.webapp.client.RemoteRepository;
import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;

public class RDFStore {
  private static final long maxQueryMs = 10000;

  private static final RemoteRepositoryManager repo =
      new RemoteRepositoryManager("http://winghouse.semiot.ru:3030/blazegraph");
  private static final RemoteRepository rr = repo.getRepositoryForDefaultNamespace();
  
  public static TupleQueryResult select(String query) {
    TupleQueryResult result = null;
    try {
      IPreparedTupleQuery iprepQuery = rr.prepareTupleQuery(query);
      iprepQuery.setMaxQueryMillis(maxQueryMs);
      result = iprepQuery.evaluate();
    } catch (Exception e) {
      e.printStackTrace();
      if (result != null) {
        try {
          result.close();
        } catch (QueryEvaluationException ex) {
          ex.printStackTrace();
        }
      }
    }
    return result;
  }
}
