package labeling;

import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.List;
import java.util.Properties;

public class ProcessingText {

  private static StanfordCoreNLP pipeline;

  public static void init() {
    Properties props = new Properties();
    props.setProperty("annotators", "tokenize, ssplit");
    pipeline = new StanfordCoreNLP(props);
    // pipeline.addAnnotator(new Tree);// new TreeCoreAnnotations.TreeAnnotation.class);
  }

  public static List<CoreLabel> process(String text) {
    Annotation annotation = pipeline.process(text);// LemmaAnnotation
    // String val = annotation.get(LemmatizationAnnotation.class);
    return annotation.get(TokensAnnotation.class);
  }

}
