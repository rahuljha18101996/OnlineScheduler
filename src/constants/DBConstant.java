package constants;

public class DBConstant {
    public static final String DATABASE = "onlinescheduler";
    public static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    public static final String DB_URL = String.format("jdbc:mysql://localhost:3306/%s",DATABASE);
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "SQLpass1996@";
    public static final String CLIENT_TABLE = "Client";
    public static final String OPERATOR_TABLE = "ServiceOperator";
    public static final String APPOINTMENTS_TABLE = "Appointment";
}
