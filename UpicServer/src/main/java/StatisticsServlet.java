import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;

@WebServlet(name = "StatisticsServlet")
public class StatisticsServlet extends HttpServlet {
    final Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        res.setStatus(HttpServletResponse.SC_OK);

        Status status = new Status();

        PrintWriter out = res.getWriter();
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        out.print(this.gson.toJson(status));
        out.flush();
    }

    public static class Status {
        private EndpointStat[] endpointStats;
        public static class EndpointStat {
            private final String URL;
            private final String operation;
            private final Integer mean;
            private final Integer max;
            public EndpointStat() {
                URL = "/resorts";
                operation = "GET";
                mean = 11;
                max = 198;
            }
        }
        public Status() {
            endpointStats = new EndpointStat[1];
            endpointStats[0] = new EndpointStat();
        }
    }
}
