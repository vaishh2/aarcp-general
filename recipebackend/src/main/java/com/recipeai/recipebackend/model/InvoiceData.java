package com.recipeai.recipebackend.model;

public class InvoiceData {

    private String invoiceDate;
    private String dueDate;
    private String amount;
    private String taxAmount;

    // ✅ No-args constructor (needed by frameworks like Spring/Jackson)
    public InvoiceData() {}

    // ✅ All-args constructor (optional, handy for creating objects quickly)
    public InvoiceData(String invoiceDate, String dueDate, String amount, String taxAmount) {
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.amount = amount;
        this.taxAmount = taxAmount;
    }

    // ✅ Getters and setters
    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(String taxAmount) {
        this.taxAmount = taxAmount;
    }

    // ✅ toString() for debugging/logging
    @Override
    public String toString() {
        return "InvoiceData{" +
                "invoiceDate='" + invoiceDate + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", amount='" + amount + '\'' +
                ", taxAmount='" + taxAmount + '\'' +
                '}';
    }
}
