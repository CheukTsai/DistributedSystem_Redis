package part1;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Phase {

  private static final Integer HTTP_OK = 200;
  private static final Integer HTTP_CREATED = 201;
  private static final Integer ALLOW_ATTEMPTS_NUM = 5;

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
  private CopyOnWriteArrayList<Record> recordList;
  AtomicInteger successCallCount;
  AtomicInteger failCallCount;
  CountDownLatch curLatch;
  CountDownLatch nextLatch;

  public Phase(Integer totalReq, Integer numThread, String IPAddress, Integer resortID, String dayID,
      String seasonID, Integer numSkier, Integer startTime, Integer endTime, Integer numLifts,
      AtomicInteger successCallCount, AtomicInteger failCallCount,
      CountDownLatch curLatch, CountDownLatch nextLatch, CopyOnWriteArrayList<Record> recordList) {
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
    this.curLatch = curLatch;
    this.nextLatch = nextLatch;
    this.recordList = recordList;
  }

  public void processPhase() throws InterruptedException{
    int range = (int) Math.ceil(numSkier / numThread);

    for(int i = 0; i < numThread; i++) {
      int start = i * range + 1;
      int end = Math.min(numSkier, (i + 1) * numSkier);
      SingleThread t = new SingleThread(totalReq, IPAddress, resortID, dayID, seasonID, start, end,
          startTime, endTime, numLifts, successCallCount, failCallCount,
          curLatch, nextLatch, recordList);
      Thread thread = new Thread(t);
      thread.start();

    }
  }
}
