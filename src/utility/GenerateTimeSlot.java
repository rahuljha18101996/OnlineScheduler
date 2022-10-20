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

    private static String getSlot(int hour) {
        return String.format("%d-%d",hour,hour + 1);
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

    public static List<String> getMergedSlot(int operator, String dateOfAppointment) throws Exception {
        List<String> result = new ArrayList<>();
        List<String> slots = new ArrayList<>();
        for (Slot s : getAvailableSlot(operator,dateOfAppointment)) slots.add(s.slot());
        List<String> list = new ArrayList<>();
        list.add(slots.get(0));

        for (int i = 1; i < slots.size(); i++) {
            String next = getNext(list.get(list.size() - 1));
            if (next.equals(slots.get(i))) {
                list.add(next);
            }else {
                result.add(merge(list.get(0),list.get(list.size() - 1)));
                list.clear();
                list.add(slots.get(i));
            }
        }
        if (list.size() > 0) result.add(merge(list.get(0),list.get(list.size() - 1)));
        return result;
    }

    private static String getNext(String slot) {
        return (Integer.parseInt(slot.substring(0,slot.indexOf("-"))) + 1) + "-" + (Integer.parseInt(slot.substring(slot.lastIndexOf("-") + 1)) + 1);
    }
    private static String merge(String first,String last) {
        return first.substring(0,first.indexOf("-")) + "-" + last.substring(last.lastIndexOf("-") + 1);
    }
}
