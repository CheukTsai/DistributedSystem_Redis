package part2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThread implements Runnable {

  private static final Integer HTTP_OK = 200;
  private static final Integer HTTP_CREATED = 201;
  private static final Integer ALLOW_ATTEMPTS_NUM = 5;

  private Integer totalRideLiftCall;
  private String IPAddress;
  private Integer resortID;
  private String dayID;
  private String seasonID;
  private Integer startSkierID;
  private Integer endSkierID;
  private Integer startTime;
  private Integer endTime;
  private Integer numLifts;
  private CopyOnWriteArrayList<Record> recordList;
  AtomicInteger successCallCount;
  AtomicInteger failCallCount;
  CountDownLatch curLatch;
  CountDownLatch nextLatch;

  public SingleThread(Integer totalRideLiftCall, String IPAddress, Integer resortID,
      String dayID, String seasonID, Integer startSkierID, Integer endSkierID, Integer startTime,
      Integer endTime, Integer numLifts,
      AtomicInteger successCallCount, AtomicInteger failCallCount,
      CountDownLatch curLatch, CountDownLatch nextLatch, CopyOnWriteArrayList<Record> recordList) {
    this.totalRideLiftCall = totalRideLiftCall;
    this.IPAddress = IPAddress;
    this.resortID = resortID;
    this.dayID = dayID;
    this.seasonID = seasonID;
    this.startSkierID = startSkierID;
    this.endSkierID = endSkierID;
    this.startTime = startTime;
    this.endTime = endTime;
    this.numLifts = numLifts;
    this.successCallCount = successCallCount;
    this.failCallCount = failCallCount;
    this.curLatch = curLatch;
    this.nextLatch = nextLatch;
    this.recordList = recordList;
  }

  @Override
  public void run() {
    String url = "http://" + IPAddress + ":8080/UpicServer_war/skiers/";
    SkiersApi api = new SkiersApi();
    api.getApiClient().setBasePath(url);

    int i = 0;
    while (i < totalRideLiftCall) {
      Integer curLiftId = ThreadLocalRandom.current().nextInt(1, numLifts + 1);
      Integer curTime = ThreadLocalRandom.current().nextInt(startTime, endTime);
      Integer curSkierId = ThreadLocalRandom.current().nextInt(startSkierID, endSkierID);
      Integer curWaitTime = (int) (Math.random() * 10);
      long start = System.currentTimeMillis();

      try {

        LiftRide curLiftRide = generateLiftRideCall(curTime, curLiftId, curWaitTime);
        int curFailure = 0, curCode = 0;
        ApiResponse res = api.writeNewLiftRideWithHttpInfo(curLiftRide, resortID, seasonID,
            dayID, curSkierId);
        while (curFailure < ALLOW_ATTEMPTS_NUM) {
          curCode = res.getStatusCode();
          if (res.getStatusCode() == HTTP_OK || res.getStatusCode() == HTTP_CREATED) {
            successCallCount.getAndIncrement();
            break;
          }
          curFailure++;
        }
        if (curFailure == ALLOW_ATTEMPTS_NUM) {
          failCallCount.getAndIncrement();
        }
        long end = System.currentTimeMillis();
        recordList.add(new Record(start, end, end-start, "POST", curCode));
        System.out.println((end - start));
        i++;
      } catch (ApiException e) {
        failCallCount.getAndIncrement();
        e.printStackTrace();
      }

    }
    curLatch.countDown();
    nextLatch.countDown();
  }

  private LiftRide generateLiftRideCall(Integer time, Integer liftId, Integer waitTime) {
    LiftRide liftRide = new LiftRide();
    liftRide.setLiftID(liftId);
    liftRide.setTime(time);
    liftRide.setWaitTime(waitTime);
    return liftRide;
  }
}
