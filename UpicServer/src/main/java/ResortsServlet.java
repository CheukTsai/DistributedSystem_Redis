import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;

@WebServlet(name = "ResortsServlet")
public class ResortsServlet extends HttpServlet {
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
        GET_SKIERS_WITH_RESORT_SEASON_DAY,
        GET_SEASONS_WITH_RESORT,
        POST_SEASONS_WITH_RESORT,
        NOT_VALID
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();
        HttpRequestStatus curStatus = checkStatus(urlPath);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }


    private HttpRequestStatus checkStatus(String urlPath) {
        if(urlPath == null || urlPath.isEmpty()) return HttpRequestStatus.GET_NO_PARAM;
        String resortID = "";
        String seasons = "";
        String dayID = "";
        String[] urlParts = urlPath.split("/");
        if(urlParts.length == 7) {
            if(!urlParts[2].equals("seasons") || !urlParts[4].equals("day") || !urlParts[6].equals("skiers")) {
                outputMsg = new Message("Page2 Not Found");
                return HttpRequestStatus.NOT_VALID;
            }
            resortID = urlParts[1];
            seasons = urlParts[3];
            dayID = urlParts[5];
            if(!isValidNumber(resortID) || !isValidNumber(seasons) || !isValidNumber(dayID)) {
                outputMsg = new Message("Invalid Input Information");
                return HttpRequestStatus.NOT_VALID;
            }
            return HttpRequestStatus.GET_SKIERS_WITH_RESORT_SEASON_DAY;
        } else if(urlParts.length == 3) {
            if(!urlParts[2].equals("seasons")) {
                outputMsg = new Message("Page3 Not Found");
                return HttpRequestStatus.NOT_VALID;
            }
            resortID = urlParts[1];
            if(!isValidNumber(resortID)) {
                outputMsg = new Message("Invalid resortNumber");
                return HttpRequestStatus.NOT_VALID;
            }
            return HttpRequestStatus.GET_SEASONS_WITH_RESORT;
        } else {
            outputMsg = new Message(String.valueOf(urlParts.length));
            return HttpRequestStatus.NOT_VALID;
        }
    }



    private void handleNoParam(HttpServletResponse res) throws IOException{
        res.setContentType("text/plain");
        res.setStatus(HttpServletResponse.SC_OK);
        Resort[] resorts = new Resort[1];
        resorts[0] = new Resort();
        try{
            PrintWriter out = res.getWriter();
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            out.print(this.gson.toJson(resorts));
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

    public static class Resort {
        private String resortName;
        private Integer resortID;
        public Resort(){
            resortName = "string";
            resortID = 0;
        }
    }
}
