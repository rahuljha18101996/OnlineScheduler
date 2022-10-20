import constants.AppointmentStatus;
import dao.ClientDao;
import dao.ServiceOperatorDao;
import dao.SlotDao;
import entity.Appointment;
import entity.Client;
import entity.Slot;
import utility.GenerateTimeSlot;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static Client currentLoggedInUser;
    private static int serialNum = 0;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Hello Everyone,\nWelcome to the online scheduler simulation");
        pageLoginSignup(sc);
    }

    public static void pageLoginSignup(Scanner sc) {
        System.out.println("***************************************************");
        System.out.println("Please select from below");
        System.out.println("1. Login");
        System.out.println("2. SignUp");
        System.out.println("3. Exit");
        System.out.print("Your Choice: ");
        int choice = sc.nextInt();
        switch (choice) {
            case 1 -> login(sc);
            case 2 -> signup(sc);
            case 3 -> {
                System.out.println("Thanks for using Online Scheduler,\nGood Bye!!");
                System.exit(0);
            }
            default -> System.out.println("Enter a valid choice");
        }
        pageLoginSignup(sc);
        System.out.println("***************************************************");
    }
    public static void login(Scanner sc) {
        try {
            System.out.println("Enter Your Details");
            System.out.print("Enter Email: ");
            String email = sc.next();
            System.out.print("Enter Password: ");
            String password = sc.next();
            currentLoggedInUser =  ClientDao.getInstance().getClient(email,password);
            if (currentLoggedInUser != null) {
                System.out.println("Welcome " + currentLoggedInUser.ClientName());
            }else {
                System.out.println("Enter your correct credential");
                System.out.println("***************************************************");
                login(sc);
            }
        } catch (Exception e) {
            System.out.println("Something Went Wrong, Please try again!!");
            System.out.println("***************************************************");
            return;
        }
        System.out.println("***************************************************");
        homePage(sc);
    }
    public static void signup(Scanner sc) {
        try {
            System.out.println("Enter Your Details");
            System.out.print("Enter Name: ");
            String name = sc.next();
            System.out.print("Enter Email: ");
            String email = sc.next();
            System.out.print("Enter Password: ");
            String password = sc.next();
            currentLoggedInUser = ClientDao.getInstance().createClient(new Client(-1, name, email), password);
            if (currentLoggedInUser != null) {
                System.out.println("Hey " + currentLoggedInUser.ClientName() + ", Welcome to the online scheduler");
            } else {
                System.out.println("Something went wrong, Please try again!");
                System.out.println("***************************************************");
                signup(sc);
            }
        } catch (Exception e) {
            System.out.println("Something Went Wrong, Please try again!!");
            System.out.println("***************************************************");
            return;
        }
        System.out.println("***************************************************");
        homePage(sc);
    }
    public static void homePage(Scanner sc) {
            int choice = showMenu(sc);
        switch (choice) {
            case 1 -> bookAppointment(sc);
            case 2 -> cancelAppointment(sc);
            case 3 -> rescheduleAppointment(sc);
            case 4 -> showBookedAppointments(sc);
            case 5 -> showAvailableSlotsOfOperator(sc);
            case 6 -> {
                System.out.println("Thanks for using Online Scheduler,\nGood Bye!!");
                System.exit(0);
            }
            default -> System.out.println("Enter a valid choice");
        }
        System.out.println("***************************************************");
        homePage(sc);
    }
    private static void bookAppointment(Scanner sc) {
        System.out.println("***************************************************");
        System.out.println("Please Select Operator Id From Below");
        try {
            ServiceOperatorDao.getInstance().getServiceOperators().forEach(serviceOperator -> System.out.println(serviceOperator.operatorId() +"\t" + serviceOperator.operatorName()));
            System.out.println("Please Choose Operator Id: ");
            final int operatorId = sc.nextInt();
            System.out.println("Please select Date[dd/mm/yyyy]: ");
            String date = sc.next();
            System.out.println("Available Slots of the Operator are:  ");

            serialNum = 1;
            List<Slot> availableSlotsOfOperator = GenerateTimeSlot.getAvailableSlot(operatorId,date);
            availableSlotsOfOperator.forEach(
                    slot -> {
                        System.out.println(serialNum + " " + slot.slot());
                        serialNum++;
                    }
            );
            System.out.println("Please Select Slot From Above");
            int slotChosen = sc.nextInt() - 1;
            if(SlotDao.getInstance().createAppointment(new Appointment(currentLoggedInUser.ClientId()
                    ,operatorId, availableSlotsOfOperator.get(slotChosen).slot(), date, AppointmentStatus.BOOKED)))
                System.out.println("Appointment Scheduled Successfully!!");
            else {
                System.out.println("Something went wrong!!");
            }
        } catch (Exception e) {
            System.out.println("Something Went Wrong, Please try again!!");
        }
    }
    private static void cancelAppointment(Scanner sc) {
        System.out.println("***************************************************");
        System.out.println("Please enter Date of appointment[dd/mm/yyyy]: ");
        String date = sc.next();
        List<Appointment> appointments;
        try {
            appointments = SlotDao.getInstance().getBookedSlot(date).stream().filter(
                    appointment -> appointment.clientId() == currentLoggedInUser.ClientId()
            ).toList();
            System.out.println("Please Select Slots to cancel ");

            serialNum = 1;
            appointments.forEach(slot -> {
                System.out.println(serialNum + " " + slot.slot());
                serialNum++;
            });

            if (appointments.isEmpty()) {
                System.out.println("You don't have booked appointment!!");
                return;
            }

            System.out.println("Please enter serial number of above slot to cancel");
            int slotSequence = sc.nextInt() - 1;

            Appointment appointment = appointments.get(slotSequence);

            if(SlotDao.getInstance().cancelBookedSlot(appointment.clientId(),appointment.operatorId(),appointment.slot(),appointment.date())) System.out.println("Your Slot is cancelled Successfully!!!");
            else System.out.println("Something Went Wrong!!");
        } catch (Exception e) {
            System.out.println("Something Went Wrong, Please try again!!");
        }
    }
    private static void rescheduleAppointment(Scanner sc) {
        System.out.println("***************************************************");
        System.out.print("Please enter Date of appointment[dd/mm/yyyy]: ");
        String date = sc.next();
        List<Appointment> appointments;
        try {
            appointments = SlotDao.getInstance().getBookedSlot(date).stream().filter(
                    appointment -> appointment.clientId() == currentLoggedInUser.ClientId()
            ).toList();
            System.out.println("Please Select Slots to reschedule ");

            serialNum = 0;
            appointments.forEach(slot -> {
                System.out.println(serialNum + " " + slot.slot());
                serialNum++;
            });

            if (appointments.isEmpty()) {
                System.out.println("You don't have booked appointment!!");
                return;
            }

            System.out.println("Please enter serial number of above slot to reschedule");
            int slotSequence = sc.nextInt();
            Appointment appointment = appointments.get(slotSequence);

            System.out.println("Available Slots of the Operator are:  ");

            serialNum = 0;
            List<Slot> availableSlotsOfOperator = GenerateTimeSlot.getAvailableSlot(appointment.operatorId(),date);
            availableSlotsOfOperator.forEach(
                    slot -> {
                        System.out.println(serialNum + " " + slot.slot());
                        serialNum++;
                    }
            );

            if (availableSlotsOfOperator.isEmpty()) {
                System.out.println("No Slots are available, Try again later!!");
                return;
            }

            System.out.println("Please select slots from above to reschedule");
            int slotChosen = sc.nextInt();
            if(SlotDao.getInstance().resheduledBookedSlot(currentLoggedInUser.ClientId()
                    ,appointment.operatorId(), appointment.slot(), date, availableSlotsOfOperator.get(slotChosen).slot()))
                System.out.println("Appointment rescheduled Successfully!!!");
            else System.out.println("Something Went Wrong!!");
        } catch (Exception e) {
            System.out.println("Something Went Wrong, Please try again!!");
        }
    }
    private static void showBookedAppointments(Scanner sc) {
        System.out.println("***************************************************");
        System.out.println("Please select from below Operator Id");
        try {
            ServiceOperatorDao.getInstance().getServiceOperators().forEach(serviceOperator -> System.out.println(serviceOperator.operatorId() +"\t" + serviceOperator.operatorName()));
            System.out.print("Please Operator Id: ");
            final int operatorId = sc.nextInt();
            System.out.print("Please select Date[dd/mm/yyyy]: ");
            String date = sc.next();
            System.out.println("Booked Time Slots are : ");

            List<Appointment> bookedSlot = SlotDao.getInstance().getBookedSlot(date).stream().filter(
                            appointment -> appointment.operatorId() == operatorId
                    ).toList();

            if (bookedSlot.isEmpty()) {
                System.out.println("No slot are booked for the operator!!");
                return;
            }

            bookedSlot .forEach(appointment -> System.out.println(appointment.slot()));
        } catch (Exception e) {
            System.out.println("Something Went Wrong, Please try again!!");
        }
    }
    private static void showAvailableSlotsOfOperator(Scanner sc) {
        System.out.println("***************************************************");
        System.out.println("Please select from below Operator Id");
        try {
            ServiceOperatorDao.getInstance().getServiceOperators().forEach(serviceOperator -> System.out.println(serviceOperator.operatorId() +"\t" + serviceOperator.operatorName()));
            System.out.print("Please Operator Id: ");
            int choice = sc.nextInt();
            System.out.print("Please select Date[dd/mm/yyyy]: ");
            String date = sc.next();
            System.out.println("Available Time Slots are : ");
            GenerateTimeSlot.getAvailableSlot(choice,date).forEach(
                    slot -> System.out.println(slot.slot())
            );
        } catch (Exception e) {
            System.out.println("Something Went Wrong, Please try again!!");
        }
    }
    public static int showMenu(Scanner sc) {
            System.out.println("Please select from below");
            System.out.println("1. Book an Appointment");
            System.out.println("2. Cancel an existing appointment");
            System.out.println("3. Reschedule an existing Appointment");
            System.out.println("4. Show Booked Appointment of an Operator");
            System.out.println("5. Show Open Slot of Operator");
            System.out.println("6. Exit");
            System.out.print("Your Choice: ");
            return sc.nextInt();
        }

}