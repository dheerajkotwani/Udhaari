package project.tronku.udhaari.Models;

public class VendorModel {

    private String name, serviceName, phone, date, description, time;
    private int amount;
    private long timeStamp;

    public VendorModel(String name, String serviceName, String phone) {
        this.name = name;
        this.serviceName = serviceName;
        this.phone = phone;
    }

    public VendorModel(String serviceName, String phone, int amount, String date, String time, String description, long timeStamp) {
        this.serviceName = serviceName;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
