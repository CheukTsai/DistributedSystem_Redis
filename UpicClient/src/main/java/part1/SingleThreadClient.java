package part1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadClient {
    private static String IPAddress;
    private static final Integer resortID = 1;
    private static final String dayID = "abc";
    private static final String seasonID = "summer";
    private static Integer numSkiers;
    private static Integer numThreads;
    private static Integer numLifts;
    private static Integer numRuns;
    public static void main(String[] args) throws InterruptedException {

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

        long start = System.currentTimeMillis();
        int numReq1 = (int) 10000,
                start1 = 1,
                end1 = 90,
                endLatch = 1;
        CountDownLatch latch = new CountDownLatch(endLatch);
        Phase phase1 = new Phase(numReq1, 1,IPAddress, resortID, dayID, seasonID,
                numSkiers, start1, end1, numLifts, success, failure,
                latch);
        latch.await();
        long end = System.currentTimeMillis();
        long wallTime = end - start;
        System.out.println("*********************************************************");
        System.out.println("End......");
        System.out.println("Data for SingleThread");
        System.out.println("*********************************************************");
        System.out.println("Target server IP is: " + IPAddress);
        System.out.println("Number of created threads: 1");
        System.out.println("Number of requests: 10000");
        System.out.println("*********************************************************");
        System.out.println("Number of successful requests :" + success.get());
        System.out.println("Number of failed requests :" + failure.get());
        System.out.println("Total wall time: " + wallTime);
        System.out.println( "Throughput: " + (int)((success.get() + failure.get()) /
                (double)(wallTime / 1000) )+ " POST requests/second");
    }
}
