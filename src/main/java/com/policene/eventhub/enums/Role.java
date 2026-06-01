package com.policene.eventhub.enums;

public enum Role {
    ADMIN ("Admin"),
    ORGANIZER("Organizer"),
    CUSTOMER("Customer");

    private String description;

    Role (String description) {
        this.description = description;
    }

    public String getDescription () {
        return this.description;
    }
}
