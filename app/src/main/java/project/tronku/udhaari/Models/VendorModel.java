package project.tronku.udhaari.Models;

public class VendorModel {

    private String name, serviceName, phone;

    public VendorModel(String name, String serviceName, String phone) {
        this.name = name;
        this.serviceName = serviceName;
        this.phone = phone;
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
}
