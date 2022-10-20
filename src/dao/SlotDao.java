package dao;

import config.DBConnect;
import constants.AppointmentStatus;
import constants.DBConstant;
import entity.Appointment;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SlotDao {
    private static SlotDao instance = new SlotDao();
    public static SlotDao getInstance() {
        return instance;
    }
    private DBConnect dbConnect;
    private SlotDao() {
        dbConnect = DBConnect.getInstance();
        dbConnect.connect();
    }

    public boolean createAppointment(Appointment appointment) {
        try(PreparedStatement statement = dbConnect.getConnection().prepareStatement(String.format(
                "INSERT INTO %s (OperatorId,ClientId,Slot,Date,Status) VALUES (?, ? , ? , ? , ?)",
                DBConstant.APPOINTMENTS_TABLE
        ))) {
            statement.setInt(1,appointment.operatorId());
            statement.setInt(2,appointment.clientId());
            statement.setString(3, appointment.slot());
            statement.setString(4, appointment.date());
            statement.setString(5, appointment.status());
            statement.execute();

            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Appointment> getBookedSlot(String date) throws Exception {
        List<Appointment> appointments = new ArrayList<>();
        try(Statement statement = dbConnect.getConnection().createStatement()) {
            String query = String.format(
                    "SELECT * FROM %s WHERE Date = '%s' AND Status = 'Booked';",
                    DBConstant.APPOINTMENTS_TABLE,
                    date
            );
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int clientId = resultSet.getInt("ClientId");
                int operatorId = resultSet.getInt("OperatorId");
                String slot = resultSet.getString("Slot");
                String _date = resultSet.getString("Date");
                String status = resultSet.getString("Status");
                appointments.add(new Appointment(clientId,operatorId,slot,_date,status));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return appointments;
    }
    public boolean resheduledBookedSlot(int clientId,int operatorId,String slot,String date,String newSlot) throws Exception {
        try(PreparedStatement statement = dbConnect.getConnection().prepareStatement(
                String.format(
                        "UPDATE %s SET Slot = '%s' WHERE ClientId = ? AND OperatorId = ? AND Slot = ? AND Date = ? ;",
                        DBConstant.APPOINTMENTS_TABLE,
                        newSlot
                )
        )) {

            statement.setInt(1,clientId);
            statement.setInt(2,operatorId);
            statement.setString(3,slot);
            statement.setString(4,date);

            int n = statement.executeUpdate();
            return n == 1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean cancelBookedSlot(int clientId,int operatorId,String slot,String date) throws Exception {
        try(PreparedStatement statement = dbConnect.getConnection().prepareStatement(
                String.format(
                        "UPDATE %s SET Status = '%s' WHERE ClientId = ? AND OperatorId = ? AND Slot = ? AND Date = ? ;",
                        DBConstant.APPOINTMENTS_TABLE,
                        AppointmentStatus.CANCELLED
                )
        )) {

            statement.setInt(1,clientId);
            statement.setInt(2,operatorId);
            statement.setString(3,slot);
            statement.setString(4,date);

            int n = statement.executeUpdate();
            return n == 1;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
