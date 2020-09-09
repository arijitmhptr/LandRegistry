package com.template.states;

import com.template.contracts.PropertyContract;
import com.template.schema.PropertySchemaV1;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

// *********
// * State *
// *********
@BelongsToContract(com.template.contracts.PropertyContract.class)
public class PropertyState implements LinearState, QueryableState {

    private final int propertyId;
    private final String propertyAddress;
    private final int propertyPrice;
    private final int buyerId;
    private final int sellerId;
    private final boolean isMortgageApproved;
    private final boolean isSurveyorApproved;
    private final Party owner;
    private final String description;
    private final Party updatedBy;
    private final Date updatedTime;
    private final UniqueIdentifier linearid;

    public PropertyState(int propertyId, String propertyAddress, int propertyPrice, int buyerId, int sellerId, boolean isMortgageApproved, boolean isSurveyorApproved, Party owner, String description, Party updatedBy) {
        this.propertyId = propertyId;
        this.propertyAddress = propertyAddress;
        this.propertyPrice = propertyPrice;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.isMortgageApproved = isMortgageApproved;
        this.isSurveyorApproved = isSurveyorApproved;
        this.owner = owner;
        this.description = description;
        this.updatedBy = updatedBy;
        this.updatedTime = new Date();
        this.linearid = new UniqueIdentifier();
    }

    @ConstructorForDeserialization
    private PropertyState(int propertyId, String propertyAddress, int propertyPrice, int buyerId, int sellerId, boolean isMortgageApproved, boolean isSurveyorApproved, Party owner, String description, Party updatedBy, UniqueIdentifier linearId) {
        this.propertyId = propertyId;
        this.propertyAddress = propertyAddress;
        this.propertyPrice = propertyPrice;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.isMortgageApproved = isMortgageApproved;
        this.isSurveyorApproved = isSurveyorApproved;
        this.owner = owner;
        this.description = description;
        this.updatedBy = updatedBy;
        this.updatedTime = new Date();
        this.linearid = linearId;
    }
    public PropertyState transfer(Party newOwner, Party updatedBy){
        return new PropertyState(propertyId,propertyAddress,propertyPrice,buyerId,sellerId,
                isMortgageApproved,isSurveyorApproved,newOwner,description,updatedBy,linearid);
    }
    public PropertyState approveByBank(boolean isApproved, Party updatedBy){
        return new PropertyState(propertyId,propertyAddress,propertyPrice,buyerId,sellerId,
                isApproved,isSurveyorApproved,owner,description,updatedBy,linearid);
    }
    public PropertyState approvedBySurveyor(boolean isApproved, Party updatedBy){
        return new PropertyState(propertyId,propertyAddress,propertyPrice,buyerId,sellerId,
                isMortgageApproved,isApproved,owner,description,updatedBy,linearid);
    }
    public int getPropertyId() { return propertyId;}

    public String getPropertyAddress() { return propertyAddress;}

    public int getPropertyPrice() { return propertyPrice;}

    public int getBuyerId() { return buyerId; }

    public int getSellerId() { return sellerId; }

    public boolean isMortgageApproved() { return isMortgageApproved; }

    public boolean isSurveyorApproved() { return isSurveyorApproved; }

    public Party getOwner() { return owner; }

    public String getDescription() { return description; }

    public Party getUpdatedBy() { return updatedBy; }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() { return linearid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PropertyState)) return false;
        PropertyState that = (PropertyState) o;
        return getPropertyId() == that.getPropertyId() &&
                getPropertyPrice() == that.getPropertyPrice() &&
                getBuyerId() == that.getBuyerId() &&
                getSellerId() == that.getSellerId() &&
                isMortgageApproved() == that.isMortgageApproved() &&
                isSurveyorApproved() == that.isSurveyorApproved() &&
                Objects.equals(getPropertyAddress(), that.getPropertyAddress()) &&
                Objects.equals(getOwner(), that.getOwner()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getUpdatedBy(), that.getUpdatedBy()) &&
                Objects.equals(linearid, that.linearid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPropertyId(), getPropertyAddress(), getPropertyPrice(), getBuyerId(), getSellerId(), isMortgageApproved(), isSurveyorApproved(), getOwner(), getDescription(), getUpdatedBy(), linearid);
    }

    @Override
    public String toString() {
        return "PropertyState{" +
                "propertyId=" + propertyId +
                ", propertyAddress='" + propertyAddress + '\'' +
                ", propertyPrice=" + propertyPrice +
                ", buyerId=" + buyerId +
                ", sellerId=" + sellerId +
                ", isMortgageApproved=" + isMortgageApproved +
                ", isSurveyorApproved=" + isSurveyorApproved +
                ", owner=" + owner +
                ", description='" + description + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", updatedTime='" + updatedTime + '\'' +
                ", linearid=" + linearid +
                '}';
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if (schema instanceof PropertySchemaV1){
            return new PropertySchemaV1.PersistentProperty(
                    this.propertyId,
                    this.propertyAddress,
                    this.propertyPrice,
                    this.buyerId,
                    this.sellerId,
                    this.owner.getName().toString(),
                    this.linearid.getId());
        }
        else{
            throw new IllegalArgumentException("Unrecognized Schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return Arrays.asList(new PropertySchemaV1());
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner);
    }
}