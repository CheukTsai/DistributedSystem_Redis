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
  private static final String dayID = "abc";
  private static final String seasonID = "summer";
  private static Integer numSkiers;
  private static Integer numThreads;
  private static Integer numLifts;
  private static Integer numRuns;
  private static CopyOnWriteArrayList<Record> recordList;

  public static void main(String[] args) throws InterruptedException{
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
        phase2startLatch = phase1Threads / 10;
    Integer
        phase2numSkiers = numSkiers / phase2Threads,
        numReq2 = (int) (numRuns * 0.6 * (numSkiers / phase2Threads)),
        start2 = 91,
        end2 = 360,
        phase3startLatch = phase2Threads / 10;

    Integer
        phase3numSkiers = numSkiers / phase3Threads,
        numReq3 = (int) (numRuns * 0.1),
        start3 = 361,
        end3 = 420;

    CountDownLatch latch3 = new CountDownLatch(phase3startLatch);
    CountDownLatch total = new CountDownLatch(phase1Threads + phase2Threads + phase3Threads);
    CountDownLatch latch2 = new CountDownLatch(phase2startLatch);

    //Phase1
    Phase phase1 = new Phase(numReq1, phase1Threads,IPAddress, resortID, dayID, seasonID,
        phase1numSkiers, start1, end1, numLifts, success, failure,
        total, latch2, recordList);
    phase1.processPhase();

//    Phase2
//
    Phase phase2 = new Phase(numReq2, phase2Threads,IPAddress, resortID, dayID, seasonID,
        phase2numSkiers, start2, end2, numLifts, success, failure,
        total, latch3, recordList);
    phase2.processPhase();

    //Phase3
    Phase phase3 = new Phase(numReq3, phase3Threads,IPAddress, resortID, dayID, seasonID,
        phase3numSkiers, start3, end3, numLifts, success, failure,
        total, null, recordList);
    phase3.processPhase();

    total.await();

    long end = System.currentTimeMillis();
    long wallTime = end - start;
    long totalTime = 0;
    long p1Time = 0;

    Collections.sort(recordList, new Comparator<Record>() {
      @Override
      public int compare(Record o1, Record o2) {
        return Long.compare(o1.getLatency(), o2.getLatency());
      }
    });

    for(int i = 0; i <recordList.size(); i++) {
      if(i < (recordList.size() / 100)) p1Time += recordList.get(i).getLatency();
      totalTime += recordList.get(i).getLatency();
    }

    System.out.println("Number of threads: " + numThreads);
    System.out.println("Number of skiers: " + numSkiers);
    System.out.println("Number of lifts: " + numLifts);
    System.out.println("Number of runs per skier per day: " + numRuns);
    System.out.println("IP: " + IPAddress);
    System.out.println("=========================================================");
    System.out.println("# of successful requests :" + success.get());
    System.out.println("# of failed requests :" + failure.get());
    System.out.println("wall time: " + wallTime);
    System.out.println( "throughput per second: " + (int)((success.get() + failure.get()) /
        (double)(wallTime / 1000) )+ " post requests per second");
    System.out.println("median response time: " +
        recordList.get((recordList.size()- 1)/2 ).getLatency() + "ms");
    System.out.println("mean response time: " + (double) (totalTime / recordList.size()) + "ms");
    System.out.println("p99 response time: " + (double) (p1Time / (recordList.size() /100)) + "ms");
  }

}
