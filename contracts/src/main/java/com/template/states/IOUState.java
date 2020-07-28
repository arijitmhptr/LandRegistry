package com.template.states;

import com.template.schema.IOUSchemaV1;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(com.template.contracts.IOUContract.class)
public class IOUState implements LinearState, QueryableState {
    private final int amount;
    private final Party lender;
    private final Party borrower;
    private final UniqueIdentifier id;

    public IOUState(int amount, Party lender, Party borrower, UniqueIdentifier id) {
        this.amount = amount;
        this.lender = lender;
        this.borrower = borrower;
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public Party getLender() {
        return lender;
    }

    public Party getBorrower() {
        return borrower;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(lender,borrower);
    }

    @Override
    public UniqueIdentifier getLinearId() {
        return id;
    }

    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof IOUSchemaV1){
            return new IOUSchemaV1.PersistentIOU(
                    this.amount,
                    this.lender.getName().toString(),
                    this.borrower.getName().toString(),
                    this.id.getId()
            );
        }
        else {
            throw new IllegalArgumentException("Unrecognised Schema");
        }
    }

    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return Arrays.asList(new IOUSchemaV1());
    }

    @Override
    public String toString() {
        return "IOUState{" +
                "amount=" + amount +
                ", lender=" + lender +
                ", borrower=" + borrower +
                ", id=" + id +
                '}';
    }
}