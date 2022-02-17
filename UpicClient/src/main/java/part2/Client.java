package part2;

import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
  private static String IPAddress;
  private static final Integer resortID = 1;
  private static final String dayID = "abc";
  private static final String seasonID = "summer";
  private static Integer numSkiers;
  private static Integer numThreads;
  private static Integer numLifts;
  private static Integer numRuns;
  private static CopyOnWriteArrayList<Record> recordList;

  public static void main(String[] args) throws InterruptedException{
    System.out.println("*********************************************************");
    System.out.println("Client starts...");
    System.out.println("*********************************************************");

    CmdParser cmdParser = new CmdParser();
    cmdParser.buildCmdParser(args);
    AtomicInteger success = new AtomicInteger(0);
    AtomicInteger failure = new AtomicInteger(0);
    IPAddress = cmdParser.ip;
    numThreads = cmdParser.numThreads;
    numLifts = cmdParser.numLifts;
    numRuns = cmdParser.numRuns;
    numSkiers = cmdParser.numSkiers;
    recordList = new CopyOnWriteArrayList<>();

    long start = System.currentTimeMillis();
    Integer phase1Threads = numThreads / 4,
        phase2Threads = numThreads,
        phase3Threads = numThreads / 4,
        phase1numSkiers = numSkiers / phase1Threads,
        numReq1 = (int) (numRuns * 0.2 * (double) (numSkiers / phase1Threads)),
        start1 = 1,
        end1 = 90,
        phase2startLatch = phase1Threads / 5;
    Integer
        phase2numSkiers = numSkiers / phase2Threads,
        numReq2 = (int) (numRuns * 0.6 * (numSkiers / phase2Threads)),
        start2 = 91,
        end2 = 360,
        phase3startLatch = phase2Threads / 5;

    Integer
        phase3numSkiers = numSkiers / phase3Threads,
        numReq3 = (int) (numRuns * 0.1),
        start3 = 361,
        end3 = 420;

    CountDownLatch latch3 = new CountDownLatch(phase3startLatch);
    CountDownLatch total = new CountDownLatch(phase1Threads + phase2Threads + phase3Threads);
    CountDownLatch latch2 = new CountDownLatch(phase2startLatch);
    CountDownLatch endLatch = new CountDownLatch(0);

    //Phase1
    Phase phase1 = new Phase(numReq1, phase1Threads,IPAddress, resortID, dayID, seasonID,
        phase1numSkiers, start1, end1, numLifts, success, failure,
        total, latch2, recordList);
    phase1.processPhase();
    latch2.await();

//    Phase2
//
    Phase phase2 = new Phase(numReq2, phase2Threads,IPAddress, resortID, dayID, seasonID,
        phase2numSkiers, start2, end2, numLifts, success, failure,
        total, latch3, recordList);
    phase2.processPhase();
    latch3.await();
    //Phase3
    Phase phase3 = new Phase(numReq3, phase3Threads,IPAddress, resortID, dayID, seasonID,
        phase3numSkiers, start3, end3, numLifts, success, failure,
        total, endLatch, recordList);
    phase3.processPhase();
    endLatch.await();

    total.await();

    long end = System.currentTimeMillis();
    long wallTime = end - start;
    long totalTime = 0;

    CSVWriter.write(recordList);

    Collections.sort(recordList, new Comparator<Record>() {
      @Override
      public int compare(Record o1, Record o2) {
        return Long.compare(o1.getLatency(), o2.getLatency());
      }
    });

    long p99Time = recordList.get((int)(recordList.size() * 0.99)).getLatency();

    for(int i = 0; i <recordList.size(); i++) {
      totalTime += recordList.get(i).getLatency();
    }
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
        recordList.get((recordList.size()- 1)/2 ).getLatency() + "ms");
    System.out.println("mean response time: " + (double) (totalTime / recordList.size()) + "ms");
    System.out.println("p99 response time: " +  p99Time + "ms");
    System.out.println( "Throughput: " + (int)((success.get() + failure.get()) /
        (double)(wallTime / 1000) )+ " POST requests/second");

  }

}
