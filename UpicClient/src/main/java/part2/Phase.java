package part2;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Phase {

  private Integer totalReq;
  private Integer numThread;
  private String IPAddress;
  private Integer resortID;
  private String dayID;
  private String seasonID;
  private Integer numSkier;
  private Integer startTime;
  private Integer endTime;
  private Integer numLifts;
  private CopyOnWriteArrayList<List<Record>> recordList;
  AtomicInteger successCallCount;
  AtomicInteger failCallCount;
  CountDownLatch nextLatch;
  CountDownLatch totalLatch;

  public Phase(Integer totalReq, Integer numThread, String IPAddress, Integer resortID, String dayID,
      String seasonID, Integer numSkier, Integer startTime, Integer endTime, Integer numLifts,
      AtomicInteger successCallCount, AtomicInteger failCallCount,
      CountDownLatch nextLatch, CopyOnWriteArrayList<List<Record>> recordList, CountDownLatch totalLatch) {
    this.totalReq = totalReq;
    this.numThread = numThread;
    this.IPAddress = IPAddress;
    this.resortID = resortID;
    this.dayID = dayID;
    this.seasonID = seasonID;
    this.numSkier = numSkier;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numLifts = numLifts;
    this.successCallCount = successCallCount;
    this.failCallCount = failCallCount;
    this.nextLatch = nextLatch;
    this.recordList = recordList;
    this.totalLatch = totalLatch;
  }

  public void processPhase() throws InterruptedException{
    int range = (int) Math.ceil(numSkier / numThread);
    for(int i = 0; i < numThread; i++) {
      int startSkier = i * range + 1;
      int endSkier = Math.min(numSkier, (i + 1) * range + 1);
      SingleThread t = new SingleThread(totalReq, IPAddress, resortID, dayID, seasonID, startSkier, endSkier,
          startTime, endTime, numLifts, successCallCount, failCallCount,
          nextLatch, recordList, totalLatch);
      Thread thread = new Thread(t);
      thread.start();
    }
  }
}
