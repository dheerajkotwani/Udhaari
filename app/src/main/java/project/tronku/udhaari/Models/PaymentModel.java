package project.tronku.udhaari.Models;

public class PaymentModel {

    private String name, phone, date;
    private int amount;

    public PaymentModel(String name, String phone, int amount) {
        this.name = name;
        this.phone = phone;
        this.amount = amount;
    }

    public PaymentModel(String name, String phone, int amount, String date) {
        this.name = name;
        this.phone = phone;
        this.amount = amount;
        this.date = date;
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

}
