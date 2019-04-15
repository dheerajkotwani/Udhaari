package project.tronku.udhaari.Models;

public class NotifModel {

    private String name, phone, type;
    private int totalAmount, amountPaying;
    private boolean read;
    private long timeStamp;

    public NotifModel(String name, String phone, String type, long timeStamp, boolean read) {
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.timeStamp = timeStamp;
        this.read = read;
    }

    public NotifModel(String name, String phone, String type, int totalAmount, int amountPaying, long timeStamp, boolean read) {
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.totalAmount = totalAmount;
        this.amountPaying = amountPaying;
        this.timeStamp = timeStamp;
        this.read = read;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean getRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getAmountPaying() {
        return amountPaying;
    }

    public void setAmountPaying(int amountPaying) {
        this.amountPaying = amountPaying;
    }
}
