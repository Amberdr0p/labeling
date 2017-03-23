package labeling;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class WriteFile {
  
  public static void WriteToEndFile(String path, Map<String, List<String>> map) {
    Writer writer = null;
    Writer writerWithMoreType = null;
    try {
      writer =
          new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, false), "utf-8"));
      writerWithMoreType =
          new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+"MoreTypes", false), "utf-8"));
      
      for (Map.Entry<String, List<String>> entry : map.entrySet()) {
        List<String> list = entry.getValue();
        if(list.size()==1 || (list.size()==2 && list.contains(DBpediaEnum.THING.getTypeChar()))) {
          String type = null;
          for(String tp : list) {
            if(!tp.equals(DBpediaEnum.THING.getTypeChar())) {
              type = tp;
              break;
            }
          }
          
          StringBuilder sb = new StringBuilder();
          sb.append(entry.getKey()).append(',').append(type).append('\n');
          
          write(writer, sb.toString());
          // writer.write(line + "\r\n");
        } else {
          
          StringBuilder sb = new StringBuilder();
          sb.append(entry.getKey());
          for(String tpp : list) {
            sb.append(',').append(tpp);
          }
          sb.append('\n');
          
          write(writerWithMoreType, sb.toString());
        }
      }
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
  
  private static void write(Writer writer, String line) throws IOException {
    writer.write(line);
  }
  
}
