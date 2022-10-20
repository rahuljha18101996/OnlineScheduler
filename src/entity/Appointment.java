package entity;

public record Appointment(int clientId,int operatorId,String slot,String date,String status) {
}
