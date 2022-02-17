package part1;

public class Record {
  private Long startTime;
  private Long endTime;
  private Long latency;
  private String type;
  private Integer responseCode;

  public Record(Long startTime, Long endTime, Long latency, String type,
      Integer responseCode) {
    this.startTime = startTime;
    this.endTime = endTime;
    this.latency = latency;
    this.type = type;
    this.responseCode = responseCode;
  }

  public Long getStartTime() {
    return startTime;
  }

  public Long getEndTime() {
    return endTime;
  }

  public Long getLatency() {
    return latency;
  }

  public String getType() {
    return type;
  }

  public Integer getResponseCode() {
    return responseCode;
  }
}
