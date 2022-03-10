package part2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;

import java.util.ArrayList;
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
    private CopyOnWriteArrayList<List<Record>> recordList;
    AtomicInteger successCallCount;
    AtomicInteger failCallCount;
    CountDownLatch nextLatch;
    CountDownLatch totalLatch;

    public SingleThread(Integer totalRideLiftCall, String IPAddress, Integer resortID,
                        String dayID, String seasonID, Integer startSkierID, Integer endSkierID, Integer startTime,
                        Integer endTime, Integer numLifts,
                        AtomicInteger successCallCount, AtomicInteger failCallCount,
                        CountDownLatch nextLatch, CopyOnWriteArrayList<List<Record>> recordList, CountDownLatch totalLatch) {
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
        this.nextLatch = nextLatch;
        this.recordList = recordList;
        this.totalLatch = totalLatch;
    }

    @Override
    public void run() {
        String url = "http://" + IPAddress +"/UpicServer_war";
        SkiersApi api = new SkiersApi();
        List<Record> records = new ArrayList<>();
        api.getApiClient().setBasePath(url).setReadTimeout(10000);
        int curSuccess = 0;
        int curFail = 0;
        int i = 0;
        while (i < totalRideLiftCall) {
            Integer curLiftId = ThreadLocalRandom.current().nextInt(1, numLifts + 1);
            Integer curTime = ThreadLocalRandom.current().nextInt(startTime, endTime);
            Integer curSkierId = ThreadLocalRandom.current().nextInt(startSkierID, endSkierID);
            Integer curWaitTime = (int) (Math.random() * 10) + 1;
            int retry = 0;

            while (retry < ALLOW_ATTEMPTS_NUM) {
                try {
                    long start = System.currentTimeMillis();
                    LiftRide curLiftRide = generateLiftRideCall(curTime, curLiftId, curWaitTime);
                    ApiResponse<Void> res = api.writeNewLiftRideWithHttpInfo(curLiftRide, resortID, seasonID,
                            dayID, curSkierId);
                    if (res.getStatusCode() == HTTP_OK || res.getStatusCode() == HTTP_CREATED) {
                        curSuccess++;
                        long end = System.currentTimeMillis();
                        System.out.println(end-start);
                        records.add(new Record(start, end, end - start, "POST", res.getStatusCode()));
                        break;
                    }
                } catch (ApiException e) {
                    retry++;
                    e.printStackTrace();
                }
            }
            if (retry == ALLOW_ATTEMPTS_NUM) {
                curFail++;
            }
            i++;
        }
        System.out.println("thread #: " + totalLatch.getCount());
        nextLatch.countDown();
        totalLatch.countDown();
        successCallCount.getAndAdd(curSuccess);
        failCallCount.getAndAdd(curFail);
        recordList.add(new ArrayList<>(records));

    }

    private LiftRide generateLiftRideCall(Integer time, Integer liftId, Integer waitTime) {
        LiftRide liftRide = new LiftRide();
        liftRide.setLiftID(liftId);
        liftRide.setTime(time);
        liftRide.setWaitTime(waitTime);
        return liftRide;
    }
}
