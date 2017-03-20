package labeling;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import scala.Option;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Launcher {

  private static final String QUERY_SELECT_COUNT =
      "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> select (count(?uri) as ?count) "
          + "where {?uri rdfs:label ?label. "
          + "?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. } ";
  private static final String QUERY_SELECT_DATA =
      "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
      + "select ?type ?label "
      + "where {?uri rdfs:label ?label. "
      + "?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. } "
      + "ORDER BY ?uri LIMIT ${LIMIT} OFFSET ${OFFSET}";
  private static final int SHIFT = 20000;
  
  private final static MyStem mystemAnalyzer =
      new Factory("-ld --format json").newMyStem("3.0", Option.<File>empty()).get();
  
  private static final String KEY_COUNT = "count";
  private static final String KEY_TYPE = "type";
  private static final String KEY_LABEL = "label";

  private static final Option<String> nullOption = scala.Option.apply(null);
  
  public static void main(String[] args) {
    Map<String, List<String>> res = new HashMap<String, List<String>>();
    int count = getCount();
    for (int i = 0; i < count; i += SHIFT) {
      Map<String, List<String>> mapWindow = processingWindowBlazegraph(i);
      processintWindowLemma(res, mapWindow);
    }
    // сохранить res
  }
  
  static int getCount() {
    TupleQueryResult tqr = RDFStore.select(QUERY_SELECT_COUNT);
    int count = 0;
    try {
      while (tqr != null && tqr.hasNext()) {
        BindingSet bs = tqr.next();
        count = Integer.valueOf(bs.getValue(KEY_COUNT).stringValue());
      }
    } catch (QueryEvaluationException e) {
      e.printStackTrace();
    } finally {
      if (tqr != null) {
        try {
          tqr.close();
        } catch (QueryEvaluationException e) {
          e.printStackTrace();
        }
      }
    }
    return count;
  }
  
  static Map<String, List<String>> processingWindowBlazegraph(int offset) {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    TupleQueryResult tqr = RDFStore.select(QUERY_SELECT_DATA.replace("${LIMIT}", String.valueOf(SHIFT))
        .replace("${OFFSET}", String.valueOf(offset)));
    
    try {
      while (tqr != null && tqr.hasNext()) {
        BindingSet bs = tqr.next();
        String label = bs.getValue(KEY_LABEL).stringValue();
        String type = bs.getValue(KEY_TYPE).stringValue();

        if (map.containsKey(label)) {
          List<String> list = map.get(label);
          if(!list.contains(type)) {
            list.add(type);
          }
        } else {
          List<String> list = new ArrayList<String>();
          list.add(type);
          map.put(label, list);
        }
      }
    } catch (QueryEvaluationException e) {
      e.printStackTrace();
    } finally {
      if (tqr != null) {
        try {
          tqr.close();
        } catch (QueryEvaluationException e) {
          e.printStackTrace();
        }
      }
    }
    
    return map;
  }
  
  static void processintWindowLemma(Map<String, List<String>> res, Map<String, List<String>> mapWindow) {
    // for( ) {
    // ProcessingText.process();
    //}
  }

}
