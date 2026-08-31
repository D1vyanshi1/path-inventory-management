package com.path.inventory.entity;

import jakarta.persistence.*;
import java.time.LocalDate;


@Entity
@Table(name = "devices")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceName;

    private Double cost;

    @Column(unique = true)
    private String serialNumber;

    private String manufacturer;

    private LocalDate assetPutUseDate;

    private Integer warrantyPeriod;

    private LocalDate licenseRenewalDate;

    private boolean licenseReminderSent;

    private LocalDate warrantyExpiryDate;

    private String modelNumber;

    private String invoiceNumber;

    private LocalDate invoiceDate;

    private Integer assetAge;

    private Integer usefulLife;

    private String assetLocation;

    private String assetCustodian;

    private String imageName;

    @Column(unique = true)
    private String tagNumber;

    private boolean available;

    private LocalDate issueDate;

    private String category;

    private LocalDate physicalVerificationDate;

    private String physicalCondition;

    private String purchaseSource;


    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public Device() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }


    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDate getAssetPutUseDate() {
        return assetPutUseDate;
    }

    public void setAssetPutUseDate(LocalDate assetPutUseDate) {
        this.assetPutUseDate = assetPutUseDate;
    }

    public Integer getWarrantyPeriod() {
        return warrantyPeriod;
    }

    public void setWarrantyPeriod(Integer warrantyPeriod) {
        this.warrantyPeriod = warrantyPeriod;
    }

    public LocalDate getLicenseRenewalDate() {
        return licenseRenewalDate;
    }

    public void setLicenseRenewalDate(LocalDate licenseRenewalDate) {
        this.licenseRenewalDate = licenseRenewalDate;
    }

    public boolean isLicenseReminderSent() {
        return licenseReminderSent;
    }

    public void setLicenseReminderSent(boolean licenseReminderSent) {
        this.licenseReminderSent = licenseReminderSent;
    }

    public LocalDate getWarrantyExpiryDate() {
        return warrantyExpiryDate;
    }

    public void setWarrantyExpiryDate(LocalDate warrantyExpiryDate) {
        this.warrantyExpiryDate = warrantyExpiryDate;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Integer getAssetAge() {
        return assetAge;
    }

    public void setAssetAge(Integer assetAge) {
        this.assetAge = assetAge;
    }

    public Integer getUsefulLife() {
        return usefulLife;
    }

    public void setUsefulLife(Integer usefulLife) {
        this.usefulLife = usefulLife;
    }

    public String getAssetLocation() {
        return assetLocation;
    }

    public void setAssetLocation(String assetLocation) {
        this.assetLocation = assetLocation;
    }

    public String getAssetCustodian() {
        return assetCustodian;
    }

    public void setAssetCustodian(String assetCustodian) {
        this.assetCustodian = assetCustodian;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTagNumber() {
        return tagNumber;
    }

    public void setTagNumber(String tagNumber) {
        this.tagNumber = tagNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getPhysicalVerificationDate() {
        return physicalVerificationDate;
    }

    public void setPhysicalVerificationDate(LocalDate physicalVerificationDate) {
        this.physicalVerificationDate = physicalVerificationDate;
    }

    public String getPhysicalCondition() {
        return physicalCondition;
    }

    public void setPhysicalCondition(String physicalCondition) {
        this.physicalCondition = physicalCondition;
    }

    public String getPurchaseSource() {
        return purchaseSource;
    }

    public void setPurchaseSource(String purchaseSource) {
        this.purchaseSource = purchaseSource;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}

