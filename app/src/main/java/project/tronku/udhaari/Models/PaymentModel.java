package project.tronku.udhaari.Models;

public class PaymentModel {

    private String name, phone, date, description, time, status;
    private int amount;
    private long timeStamp;

    public PaymentModel(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public PaymentModel(String name, String phone, int amount) {
        this.name = name;
        this.phone = phone;
        this.amount = amount;
    }

    public PaymentModel(String name, String phone, int amount, String date, String time, String description, long timeStamp, String status) {
        this.name = name;
        this.phone = phone;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.time = time;
        this.timeStamp = timeStamp;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
