package part2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVWriter {


    public static void write(List<Record> list, String numThreads) {
      File csvFile = new File("records" + "_" + numThreads + ".csv");
      if(csvFile.exists()) csvFile.delete();
      try{
        csvFile.createNewFile();
        FileWriter writer = new FileWriter(csvFile);
        writer.append("StartTime, EndTime, Latency, Type, Code\n");
        writer.flush();
        for(Record record : list) {
          writer.append(String.valueOf(record.getStartTime()));
          writer.append(",");
          writer.append(String.valueOf(record.getEndTime()));
          writer.append(",");
          writer.append(String.valueOf(record.getLatency()));
          writer.append(",");
          writer.append(record.getType());
          writer.append(",");
          writer.append(String.valueOf(record.getResponseCode()));
          writer.append("\n");
          writer.flush();
        }
        writer.close();
      } catch(IOException e) {
        e.printStackTrace();
      }

    }
}
