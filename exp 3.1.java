import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class WebAppServlet extends HttpServlet {
    Connection con;
    public void init() throws ServletException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/webappdb", "root", "password");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        String action = req.getParameter("action");
        if (action == null) {
            out.println("<html><body>");
            out.println("<h2>User Login</h2>");
            out.println("<form method='post' action='WebAppServlet?action=login'>");
            out.println("Username:<input type='text' name='username'><br>");
            out.println("Password:<input type='password' name='password'><br>");
            out.println("<input type='submit' value='Login'></form>");
            out.println("<hr><h2>Employee Records</h2>");
            out.println("<form method='get' action='WebAppServlet?action=employees'>");
            out.println("<input type='submit' value='View All Employees'></form>");
            out.println("<form method='get' action='WebAppServlet?action=searchEmp'>");
            out.println("Search by ID:<input type='text' name='empid'><input type='submit' value='Search'></form>");
            out.println("<hr><h2>Student Attendance</h2>");
            out.println("<form method='post' action='WebAppServlet?action=attendance'>");
            out.println("Student ID:<input type='text' name='sid'><br>");
            out.println("Date:<input type='date' name='date'><br>");
            out.println("Status:<select name='status'><option>Present</option><option>Absent</option></select><br>");
            out.println("<input type='submit' value='Submit Attendance'></form>");
            out.println("</body></html>");
        } else if (action.equals("employees")) {
            try {
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery("SELECT * FROM Employee");
                out.println("<html><body><h2>Employee Records</h2><table border='1'><tr><th>ID</th><th>Name</th><th>Salary</th></tr>");
                while (rs.next()) out.println("<tr><td>" + rs.getInt(1) + "</td><td>" + rs.getString(2) + "</td><td>" + rs.getDouble(3) + "</td></tr>");
                out.println("</table></body></html>");
            } catch (Exception e) { out.println("Error: " + e.getMessage()); }
        } else if (action.equals("searchEmp")) {
            String empid = req.getParameter("empid");
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM Employee WHERE EmpID=?");
                ps.setString(1, empid);
                ResultSet rs = ps.executeQuery();
                out.println("<html><body><h2>Search Result</h2>");
                if (rs.next()) out.println("EmpID:" + rs.getInt(1) + "<br>Name:" + rs.getString(2) + "<br>Salary:" + rs.getDouble(3));
                else out.println("No Employee Found");
                out.println("</body></html>");
            } catch (Exception e) { out.println("Error: " + e.getMessage()); }
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        String action = req.getParameter("action");
        if (action.equals("login")) {
            String user = req.getParameter("username");
            String pass = req.getParameter("password");
            if (user.equals("admin") && pass.equals("1234")) out.println("<html><body><h2>Welcome, " + user + "!</h2></body></html>");
            else out.println("<html><body><h2>Invalid Credentials</h2></body></html>");
        } else if (action.equals("attendance")) {
            String sid = req.getParameter("sid");
            String date = req.getParameter("date");
            String status = req.getParameter("status");
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO Attendance(StudentID, Date, Status) VALUES(?,?,?)");
                ps.setString(1, sid);
                ps.setString(2, date);
                ps.setString(3, status);
                int i = ps.executeUpdate();
                if (i > 0) out.println("<html><body><h2>Attendance Recorded Successfully</h2></body></html>");
                else out.println("<html><body><h2>Failed to Record Attendance</h2></body></html>");
            } catch (Exception e) { out.println("Error: " + e.getMessage()); }
        }
    }

    public void destroy() {
        try { con.close(); } catch (Exception e) {}
    }
}
