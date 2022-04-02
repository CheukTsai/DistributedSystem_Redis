package part2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
  private static String IPAddress;
  private static final Integer resortID = 1;
  private static final String dayID = "123";
  private static final String seasonID = "1";
  private static Integer numSkiers;
  private static Integer numThreads;
  private static Integer numLifts;
  private static Integer numRuns;
  private static CopyOnWriteArrayList<List<Record>> recordList;


  public static void main(String[] args) throws InterruptedException{
    System.out.println("*********************************************************");
    System.out.println("Client2 starts...");
    System.out.println("*********************************************************");


//    CmdParser cmdParser = new CmdParser();
//    cmdParser.buildCmdParser(args);
    AtomicInteger success = new AtomicInteger(0);
    AtomicInteger failure = new AtomicInteger(0);
//    IPAddress = cmdParser.ip;
//    numThreads = cmdParser.numThreads;
//    numLifts = cmdParser.numLifts;
//    numRuns = cmdParser.numRuns;
//    numSkiers = cmdParser.numSkiers;
    IPAddress = "localhost:8080/UpicServer_war_exploded/";
//      IPAddress = "6650-lb-843d040d79513d08.elb.us-west-2.amazonaws.com:8080/Assignment2_Server_war/";
//    IPAddress = "lb1-d5f956b076f1e934.elb.us-west-2.amazonaws.com/UpicServer_war";
//    IPAddress = "ec2-34-219-103-126.us-west-2.compute.amazonaws.com:8080//UpicServer_war";
    numThreads = 512;
    numLifts = 40;
    numRuns = 10;
    numSkiers =  20000;
    recordList = new CopyOnWriteArrayList<>();

    long start = System.currentTimeMillis();
    int phase1Threads = numThreads / 4,
        phase2Threads = numThreads,
        phase3Threads = numThreads / 10,
        numReq1 = (int) (numRuns * 0.2 * (double) (numSkiers / phase1Threads)),
        start1 = 1,
        end1 = 90,
        phase2startLatch = phase1Threads / 5,
        totalLatchCount = phase1Threads + phase2Threads + phase3Threads;
    CountDownLatch latch = new CountDownLatch(phase2startLatch);
    CountDownLatch totalLatch = new CountDownLatch(totalLatchCount);
    Phase phase1 = new Phase(numReq1, phase1Threads,IPAddress, resortID, dayID, seasonID,
            numSkiers, start1, end1, numLifts, success, failure,
            latch, recordList, totalLatch);
    phase1.processPhase();
    latch.await();

    int numReq2 = (int) (numRuns * 0.6 * (numSkiers / phase2Threads)),
        start2 = 91,
        end2 = 360,
        phase3startLatch = phase2Threads / 5;
    latch = new CountDownLatch(phase3startLatch);
    Phase phase2 = new Phase(numReq2, phase2Threads,IPAddress, resortID, dayID, seasonID,
            numSkiers, start2, end2, numLifts, success, failure,
            latch, recordList, totalLatch);
    phase2.processPhase();
    latch.await();

    int numReq3 = (int) (numRuns * 0.1),
        start3 = 361,
        end3 = 420;
    latch = new CountDownLatch(phase3Threads);
    Phase phase3 = new Phase(numReq3, phase3Threads,IPAddress, resortID, dayID, seasonID,
            numSkiers, start3, end3, numLifts, success, failure, latch, recordList, totalLatch);
    phase3.processPhase();
    latch.await();
    totalLatch.await();

    long end = System.currentTimeMillis();
    long wallTime = end - start;
    long totalTime = 0;

    List<Record> outputList = new ArrayList<>();
    for(List<Record> list : recordList) {
      outputList.addAll(list);
    }

    CSVWriter.write(outputList, String.valueOf(numThreads));

    outputList.sort(new Comparator<Record>() {
      @Override
      public int compare(Record o1, Record o2) {
        return Long.compare(o1.getLatency(), o2.getLatency());
      }
    });

    long p99Time = outputList.get((int)(outputList.size() * 0.99)).getLatency();

    for(int i = 0; i <outputList.size(); i++) {
      totalTime += outputList.get(i).getLatency();
    }

    Collections.sort(outputList, new Comparator<Record>() {
      @Override
      public int compare(Record o1, Record o2) {
        return Long.compare(o1.getEndTime(), o2.getEndTime());
      }
    });
    List<int[]> meanLatList = new ArrayList<>();
    int n = outputList.size();
    long s = outputList.get(0).getEndTime(), cur = 0;
    int count = 1, i = 0, sec = 0;
    while(i < n) {
      if(outputList.get(i).getEndTime() - s <= 1000) {
        cur += outputList.get(i).getLatency();
        count++;
      } else {
        sec++;
        meanLatList.add(new int[]{sec, (int) (cur / count)});
        count = 1;
        s = outputList.get(i).getEndTime();
        cur = outputList.get(i).getLatency();
      }
      i++;
    }

    CSVWriterForMeanLatency.write(meanLatList, String.valueOf(numThreads));

    System.out.println("*********************************************************");
    System.out.println("End......");
    System.out.println("Data for Client2");
    System.out.println("*********************************************************");
    System.out.println("Target server IP is: " + IPAddress);
    System.out.println("Number of created threads: " + numThreads);
    System.out.println("Number of skiers: " + numSkiers);
    System.out.println("Number of lifts: " + numLifts);
    System.out.println("Number of runs per skier per day: " + numRuns);
    System.out.println("*********************************************************");
    System.out.println("Number of successful requests :" + success.get());
    System.out.println("Number of failed requests :" + failure.get());
    System.out.println("Total wall time: " + wallTime);
    System.out.println("median response time: " +
        outputList.get((outputList.size()- 1)/2 ).getLatency() + "ms");
    System.out.println("mean response time: " + (double) (totalTime / outputList.size()) + "ms");
    System.out.println("p99 response time: " +  p99Time + "ms");
    System.out.println( "Throughput: " + (int)((success.get() + failure.get()) /
        (double)(wallTime / 1000) )+ " POST requests/second");

  }

}
