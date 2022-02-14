package part1;

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

    long start = System.currentTimeMillis();
    int phase1Threads = numThreads / 4,
        phase1numSkiers = numSkiers / phase1Threads,
        numReq1 = (int) (numRuns * 0.2 * (numSkiers / phase1Threads)),
        start1 = 1,
        end1 = 90,
        phase2startLatch = phase1Threads / 10;
    CountDownLatch latch2 = new CountDownLatch(phase2startLatch);
    //Phase1
    Phase phase1 = new Phase(numReq1, phase1Threads,IPAddress, resortID, dayID, seasonID,
        phase1numSkiers, start1, end1, numLifts, success, failure,
        new CountDownLatch(0), latch2);
    phase1.processPhase();

    int phase2Threads = numThreads,
        phase2numSkiers = numSkiers / phase2Threads,
        numReq2 = (int) (numRuns * 0.6 * (numSkiers / phase2Threads)),
        start2 = 91,
        end2 = 360,
        phase3startLatch = phase2Threads / 10;
    CountDownLatch latch3 = new CountDownLatch(phase3startLatch);

    //Phase2
    Phase phase2 = new Phase(numReq2, phase2Threads,IPAddress, resortID, dayID, seasonID,
        phase2numSkiers, start2, end2, numLifts, success, failure,
        new CountDownLatch(0), latch3);
    latch2.await();
    phase2.processPhase();

    int phase3Threads = numThreads / 4,
        phase3numSkiers = numSkiers / phase3Threads,
        numReq3 = (int) (numRuns * 0.1),
        start3 = 361,
        end3 = 420,
        endLatch = phase3Threads;
    CountDownLatch lastLatch = new CountDownLatch(endLatch);

    //Phase3
    Phase phase3 = new Phase(numReq3, phase3Threads,IPAddress, resortID, dayID, seasonID,
        phase3numSkiers, start3, end3, numLifts, success, failure,
        new CountDownLatch(0), lastLatch);
    latch3.await();
    phase3.processPhase();

    long end = System.currentTimeMillis();
    long wallTime = end - start;

    System.out.println("Number of threads: " + numThreads);
    System.out.println("Number of skiers: " + numSkiers);
    System.out.println("Number of lifts: " + numLifts);
    System.out.println("Number of runs per skier per day: " + numRuns);
    System.out.println("IP: " + IPAddress);
    System.out.println("=========================================================");
    System.out.println("# of successful requests :" + success);
    System.out.println("# of failed requests :" + failure);
    System.out.println("wall time: " + wallTime);
    System.out.println( "throughput per second: " + (success.get() + failure.get()) /
        (double) (wallTime / 1000) + " post requests per second");
  }

}
