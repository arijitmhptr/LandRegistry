package com.template.schema;

import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Arrays;
import java.util.UUID;

public class IOUSchemaV1 extends MappedSchema {
    public IOUSchemaV1() {
        super(IOUSchema.class, 1, Arrays.asList(PersistentIOU.class));
    }

    @Entity
    @Table(name = "IOU_STATES")
    public static class PersistentIOU extends PersistentState{
        @Column(name = "amount") private final int amount;
        @Column(name = "lender") private final String lender;
        @Column(name = "borrower")private final String borrower;
        @Column(name = "id")private final UUID id;

        public PersistentIOU(int amount, String lender, String borrower, UUID id) {
            this.amount = amount;
            this.lender = lender;
            this.borrower = borrower;
            this.id = id;
        }
        public int getAmount() {
            return amount;
        }
        public String getLender() {
            return lender;
        }
        public String getBorrower() {
            return borrower;
        }
        public UUID getId() {
            return id;
        }
        // Default constructor required by hibernate.
        public PersistentIOU() {
            this.amount = 0;
            this.lender = null;
            this.borrower = null;
            this.id = null;
        }
    }
}
