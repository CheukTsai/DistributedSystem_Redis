
import com.google.gson.Gson;
import java.io.PrintWriter;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

@WebServlet(name = "SkiersServlet", value = "/SkiersServlet")
public class SkiersServlet extends HttpServlet {
    private Gson gson = new Gson();
    private Message outputMsg = new Message("fine");

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

    private HttpRequestStatus checkStatus(String urlPath, String type) {
        if(urlPath == null || urlPath.isEmpty()) return HttpRequestStatus.GET_NO_PARAM;
        String skierID = "";
        String seasons = "";
        String dayID = "";
        String[] urlParts = urlPath.split("/");
        if(urlParts.length == 8) {
            if(!urlParts[2].equals("seasons") || !urlParts[4].equals("day") || !urlParts[6].equals("skiers")) {
                outputMsg = new Message("Page2 Not Found");
                return HttpRequestStatus.NOT_VALID;
            }
            skierID = urlParts[1];
            seasons = urlParts[3];
            dayID = urlParts[5];
            skierID = urlParts[5];
            if(!isValidNumber(skierID) || !isValidNumber(seasons) || !isValidNumber(dayID)
                || !isValidNumber(skierID)) {
                outputMsg = new Message("Invalid Input Information");
                return HttpRequestStatus.NOT_VALID;
            }
            if(type.equals("GET"))
            return HttpRequestStatus.GET_SKIERS_WITH_RESORT_SEASON_DAY_ID;
            else return HttpRequestStatus.POST_SKIERS_WITH_RESORT_SEASON_DAY_ID;

        } else if(urlParts.length == 3) {
            if(!urlParts[2].equals("vertical")) {
                outputMsg = new Message("Page3 Not Found");
                return HttpRequestStatus.NOT_VALID;
            }
            skierID = urlParts[1];
            if(!isValidNumber(skierID)) {
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
