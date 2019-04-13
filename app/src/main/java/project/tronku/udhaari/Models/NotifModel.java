package project.tronku.udhaari.Models;

public class NotifModel {

    private String name, phone, description, type;
    private int amount;
    private boolean read;
    private long timeStamp;

    public NotifModel(String name, String phone, String type, long timeStamp, boolean read) {
        this.name = name;
        this.phone = phone;
        this.type = type;
        this.timeStamp = timeStamp;
        this.read = read;
    }

    public NotifModel(String name, String phone, String description, String type, int amount, long timeStamp, boolean read) {
        this.name = name;
        this.phone = phone;
        this.description = description;
        this.type = type;
        this.amount = amount;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
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
}
