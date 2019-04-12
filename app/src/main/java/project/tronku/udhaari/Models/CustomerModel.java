package project.tronku.udhaari.Models;

public class CustomerModel {

    private String name, phone, date, description, time;
    private int amount;
    private long timeStamp;

    public CustomerModel(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public CustomerModel(String name, String phone, int amount, String date, String time, String description, long timeStamp) {
        this.name = name;
        this.phone = phone;
        this.date = date;
        this.description = description;
        this.amount = amount;
        this.time = time;
        this.timeStamp = timeStamp;
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

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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
}
