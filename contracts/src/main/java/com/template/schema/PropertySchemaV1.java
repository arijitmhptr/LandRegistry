package com.template.schema;

import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.UUID;

public class PropertySchemaV1 extends MappedSchema {
    public PropertySchemaV1() {
        super(PropertySchema.class, 1, Arrays.asList(PersistentProperty.class));
    }

    @Entity
    @Table(name = "PROPERTY_TBL")
    public static class PersistentProperty extends PersistentState{
        @Column(name = "PropertyId") private final int propertyId;
        @Column(name = "Address") private final String propertyAddress;
        @Column(name = "Price") private final int propertyPrice;
        @Column(name = "BuyerID") private final int buyerId;
        @Column(name = "SellerID") private final int sellerId;
        @Column(name = "owner") private final String owner;
        @Column(name = "LinearID") private final UUID id;

        public PersistentProperty(int propertyId, String propertyAddress, int propertyPrice, int buyerId, int sellerId, String owner, UUID id) {
            this.propertyId = propertyId;
            this.propertyAddress = propertyAddress;
            this.propertyPrice = propertyPrice;
            this.buyerId = buyerId;
            this.sellerId = sellerId;
            this.owner = owner;
            this.id = id;
        }

        public int getPropertyId() { return propertyId; }

        public String getPropertyAddress() { return propertyAddress; }

        public int getPropertyPrice() { return propertyPrice; }

        public int getBuyerId() { return buyerId; }

        public int getSellerId() { return sellerId; }

        public UUID getId() {
            return id;
        }
        public String getowner() {
            return owner;
        }

        // Default constructor required by hibernate.
        public PersistentProperty() {
            this.propertyId = 0;
            this.propertyAddress = null;
            this.propertyPrice = 0;
            this.buyerId = 0;
            this.sellerId = 0;
            this.owner = null;
            this.id = null;
        }

    }
}
