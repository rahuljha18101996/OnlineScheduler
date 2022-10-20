package utility;

import dao.ServiceOperatorDao;
import dao.SlotDao;
import entity.Appointment;
import entity.ServiceOperator;
import entity.Slot;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class GenerateTimeSlot {
    private static List<String> getTimeSlot(String dateOfAppointment) throws Exception {
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime appointmentDateTime;
        List<String> slots = new ArrayList<>();
        try {
            String[] dateParams = dateOfAppointment.split("/");
            int d = Integer.parseInt(dateParams[0]);
            int m = Integer.parseInt(dateParams[1]);
            int y = Integer.parseInt(dateParams[2]);

            appointmentDateTime = LocalDateTime.of(y,m,d,0,0,0);
        }catch (NumberFormatException | DateTimeException e) {
            throw  new Exception("Cannot Process!");
        }

        if (appointmentDateTime.toLocalDate().isBefore(current.toLocalDate())) {
            throw  new Exception("Cannot Process Appointment, Slot is expired!");
        }else if (appointmentDateTime.toLocalDate().isEqual(current.toLocalDate())) {
            LocalDateTime nextDay = LocalDateTime.now().plus(1, ChronoUnit.DAYS);
            current = current.plus(1,ChronoUnit.HOURS);
            while (current.plus(1,ChronoUnit.HOURS).toLocalDate().isBefore(nextDay.toLocalDate())) {
                slots.add(getSlot(current.getHour()));
                current = current.plus(1,ChronoUnit.HOURS);
            }
            if (current.getHour() == 23) slots.add(getSlot(current.getHour()));
        }else if (appointmentDateTime.toLocalDate().isAfter(current.toLocalDate())) {
            LocalDateTime nextDay = appointmentDateTime.plus(1, ChronoUnit.DAYS);
            current = appointmentDateTime;
            while (current.plus(1,ChronoUnit.HOURS).toLocalDate().isBefore(nextDay.toLocalDate())) {
                slots.add(getSlot(current.getHour()));
                current = current.plus(1,ChronoUnit.HOURS);
            }
            if (current.getHour() == 23) slots.add(getSlot(current.getHour()));
        }else {
            throw  new Exception("Something went wrong!");
        }

        return slots;
    }
    private static String getSlot(int currentHour) {
        StringBuilder stringBuilder = new StringBuilder();
        if (currentHour == 0) {
            stringBuilder.append("12:00 AM");
        }else if (currentHour < 12) {
            stringBuilder.append(currentHour < 10 ? "0" : "").append(currentHour).append(":00").append(" AM");
        }else {
            int n = currentHour % 12;
            n = n == 0 ? 12 : n;
            stringBuilder.append(n < 10 ? "0" : "").append(n).append(":00").append(" PM");
        }

        stringBuilder.append(" - ");

        currentHour++;

        if (currentHour < 12) {
            stringBuilder.append(currentHour < 10 ? "0" : "").append(currentHour).append(":00").append(" AM");
        }else if (currentHour == 24) {
            stringBuilder.append("12:00 AM");
        }else {
            int n = currentHour % 12;
            n = n == 0 ? 12 : n;
            stringBuilder.append(n < 10 ? "0" : "").append(n).append(":00").append(" PM");
        }

        return stringBuilder.toString();
    }

    public static List<Slot> getAvailableSlot(int operator, String dateOfAppointment) throws Exception {
        List<String> allSlots = getTimeSlot(dateOfAppointment);
        List<ServiceOperator> serviceOperators = ServiceOperatorDao.getInstance().getServiceOperators();
        List<Appointment> bookedSlot = SlotDao.getInstance().getBookedSlot(dateOfAppointment);

        List<Slot> result = new ArrayList<>();
        for (ServiceOperator op : serviceOperators) {
            for (String slot : allSlots) {
                result.add(new Slot(op.operatorId(),slot));
            }
        }

        for (Appointment gs : bookedSlot) {
            result.remove(new Slot(gs.operatorId(), gs.slot()));
        }

        if (operator != 0) {
            result = result.stream().filter(slot -> slot.operatorId() == operator).toList();
        }

        return result;
    }
}
