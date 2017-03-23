package labeling;

import edu.stanford.nlp.ling.CoreLabel;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import ru.stachek66.nlp.mystem.model.Info;
import scala.Option;
import scala.collection.JavaConversions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Launcher {

  private static final Pattern p = Pattern
      .compile("^[А-яЁё\\d\\s\\.\\,\\-\\\\\\/\\№\\'\\’\\!\\+\\&\\—\\«\\»\\\"\\`\\:\\;]*[А-яЁё]+"
          + "[А-яЁё\\d\\s\\.\\,\\-\\\\\\/\\№\\'\\’\\!\\+\\&\\—\\«\\»\\\"\\`\\:\\;]*$");


  private static final String REGEX_ROMAN_NUMERALS =
      "(M{0,3})(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])";
  private static final String REGEX_BRACKETS = "\\(.*\\)";
  private static final Pattern patternRomanNumerals = Pattern.compile(REGEX_ROMAN_NUMERALS);
  private static final Pattern patternNumerals = Pattern.compile(".*[0-9]+.*");


  private static final String QUERY_SELECT_COUNT =
      "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> select (count(?uri) as ?count) "
          + "where {?uri rdfs:label ?label. "
          + "?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?type. } ";
  private static final String QUERY_SELECT_DATA =
      "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> " + "select ?type ?label "
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
    ProcessingText.init();
    Map<String, List<String>> res = new HashMap<String, List<String>>();
    int count = getCount();
    for (int i = 0; i < count; i += SHIFT) {
      Map<String, List<String>> mapWindow = processingWindowBlazegraph(i);
      processintWindowLemma(res, mapWindow);
    }
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
    TupleQueryResult tqr = RDFStore.select(QUERY_SELECT_DATA
        .replace("${LIMIT}", String.valueOf(SHIFT)).replace("${OFFSET}", String.valueOf(offset)));

    System.out.println("Select query for window complete.");
    try {
      while (tqr != null && tqr.hasNext()) {
        BindingSet bs = tqr.next();
        String label = bs.getValue(KEY_LABEL).stringValue();
        String type = getType(bs.getValue(KEY_TYPE).stringValue());
        System.out.println(label + " " + type);
        if (map.containsKey(label)) {
          List<String> list = map.get(label);
          if (!list.contains(type)) {
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

  static String getType(String type) {
    return DBpediaEnum.getTypeCharForType(type);
  }

  static void processintWindowLemma(Map<String, List<String>> res,
      Map<String, List<String>> mapWindow) {

    //

    // ДОБАВИТЬ ПРОВЕРКУ с русскими символами (т.е. p)

    //
    for (Map.Entry<String, List<String>> entry : mapWindow.entrySet()) {
      String label = entry.getKey();
      String cutLabel = label.replaceAll(REGEX_BRACKETS, "");
      // System.out.println(entry.getKey() + "/" + entry.getValue());
      List<CoreLabel> listCL = ProcessingText.process(cutLabel);

      StringBuffer resLabel = new StringBuffer();
      boolean isFirst = true;
      boolean haveRuSymbols = false;
      for (CoreLabel cl : listCL) {
        
        String ot = cl.originalText();
        if (patternNumerals.matcher(ot).matches() || patternRomanNumerals.matcher(ot).matches()) {
          if (!isFirst) {
            resLabel.append(" ");
          }
          resLabel.append(ot);
          isFirst = false;
        } else if (p.matcher(ot).matches()) {
          if (!isFirst) {
            resLabel.append(" ");
          }
          resLabel.append(getLemmaForWord(ot));
          isFirst = false;
          haveRuSymbols = true;
        }
        
      }
      String resStr = resLabel.toString();
      if (haveRuSymbols && !resStr.isEmpty()) {
        if (res.containsKey(resStr)) {
          List<String> listRes = res.get(resStr);
          for (String str : entry.getValue()) {
            if (!listRes.contains(str)) {
              listRes.add(str);
            }
          }
        } else {
          res.put(resStr, entry.getValue());
        }
      }
    }
  }

  static String getLemmaForWord(String cutLabel) {
    Iterable<Info> result;
    try {
      result = JavaConversions
          .asJavaIterable(mystemAnalyzer.analyze(Request.apply(cutLabel)).info().toIterable());
      for (final Info info : result) {
        Option<String> lex = info.lex();
        if (lex != null && lex != nullOption) {
          return lex.get();
        }
      }

    } catch (MyStemApplicationException e) {
      e.printStackTrace();
    }
    return "";
  }

}
