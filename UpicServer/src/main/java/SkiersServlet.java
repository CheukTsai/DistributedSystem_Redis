
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import models.ChannelPool;
import models.LiftRide;

@WebServlet(name = "SkiersServlet", value = "/SkiersServlet")
public class SkiersServlet extends HttpServlet {
    private Gson gson = new Gson();
    private Message outputMsg = new Message("hello");
    private String skierID;
    private String resortID;
    private String dayID;
    private String seasonID;
    private ChannelPool channelPool;
    private final static String QUEUE_NAME = "hello";

    public static class Message{
        String message;
        public Message(String msg) {
            message = msg;
        }
    }
    private enum HttpRequestStatus{
        GET_NO_PARAM,
        GET_SKIERS_WITH_RESORT_SEASON_DAY_ID,
        POST_SKIERS_WITH_RESORT_SEASON_DAY_ID,
        GET_VERTICAL_WITH_ID,
        POST_SEASONS_WITH_RESORT,
        NOT_VALID
    }

    @Override
    public void init() throws ServletException {
        try {
            System.out.println("start");
            super.init();
            channelPool = new ChannelPool();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        HttpRequestStatus curStatus = checkStatus(urlPath, "POST");
        PrintWriter out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        if(!curStatus.equals(HttpRequestStatus.NOT_VALID)) {
            res.setStatus(HttpServletResponse.SC_OK);
            if(curStatus.equals(HttpRequestStatus.GET_NO_PARAM)) handleNoParam(res);
            else{
                out.write(gson.toJson(outputMsg));
                out.flush();
            }
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(outputMsg));
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        HttpRequestStatus curStatus = checkStatus(urlPath, req.getMethod());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();
        if(!curStatus.equals(HttpRequestStatus.NOT_VALID)) {
            res.setStatus(HttpServletResponse.SC_OK);
            if(curStatus.equals(HttpRequestStatus.GET_NO_PARAM)) handleNoParam(res);
            else{
                LiftRide liftRide = getReqBody(req);
                liftRide.setDayID(dayID);
                liftRide.setSkierID(skierID);
                liftRide.setSeasonID(seasonID);
                liftRide.setResortID(resortID);
//                String msg = "SkierID: " + skierID + "has successfully uploaded" +
//                        "liftRide information #" + liftRide.getLiftID() + "@" + seasonID + "_" +
//                        dayID + "_" + resortID;
                String message = gson.toJson(liftRide);
                if(sendMessageToQueue(message)) {
                    res.getWriter().write(gson.toJson(new Message("a")));
                } else {
                    res.getWriter().write("not success");
                }
                res.getWriter().flush();
            }
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.write(gson.toJson(outputMsg));
            out.flush();
        }
    }

    private boolean sendMessageToQueue(String msg) {
        try {
            Channel channel = channelPool.getChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish("", QUEUE_NAME,
                    null,msg.getBytes(StandardCharsets.UTF_8));
            channelPool.add(channel);
            return true;
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private HttpRequestStatus checkStatus(String urlPath, String type) {
        if(urlPath == null || urlPath.isEmpty()) return HttpRequestStatus.GET_NO_PARAM;
        String resortID = "";
        String seasons = "";
        String dayID = "";
        String skierID = "";
        String[] urlParts = urlPath.split("/");
        if(urlParts.length == 8) {
            if(!urlParts[2].equals("seasons") || !urlParts[4].equals("days") || !urlParts[6].equals("skiers")) {
                outputMsg = new Message("Page2 Not Found");
                return HttpRequestStatus.NOT_VALID;
            }
            resortID = urlParts[1];
            seasons = urlParts[3];
            dayID = urlParts[5];
            skierID = urlParts[7];
            if(!isValidNumber(resortID) || !isValidNumber(dayID)
                || !isValidNumber(skierID)) {
                outputMsg = new Message("Invalid Input Information");
                return HttpRequestStatus.NOT_VALID;
            }
            this.resortID = resortID;
            this.seasonID = seasons;
            this.dayID = dayID;
            this.skierID = skierID;
            if(type.equals("GET"))
            return HttpRequestStatus.GET_SKIERS_WITH_RESORT_SEASON_DAY_ID;
            else return HttpRequestStatus.POST_SKIERS_WITH_RESORT_SEASON_DAY_ID;

        } else if(urlParts.length == 3) {
            if(!urlParts[2].equals("vertical")) {
                outputMsg = new Message("Page3 Not Found");
                return HttpRequestStatus.NOT_VALID;
            }
            resortID = urlParts[1];
            if(!isValidNumber(resortID)) {
                outputMsg = new Message("Invalid resortNumber");
                return HttpRequestStatus.NOT_VALID;
            }
            return HttpRequestStatus.GET_VERTICAL_WITH_ID;
        } else {
            outputMsg = new Message(String.valueOf(urlParts.length));
            return HttpRequestStatus.NOT_VALID;
        }
    }



    private void handleNoParam(HttpServletResponse res) throws IOException{
        res.setContentType("text/plain");
        res.setStatus(HttpServletResponse.SC_OK);
        try{
            PrintWriter out = res.getWriter();
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private LiftRide getReqBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = req.getReader();
        try{
            while((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LiftRide liftRide = gson.fromJson(sb.toString(), LiftRide.class);
        return liftRide;
    }

    private boolean isValidNumber(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            int digits = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

}
