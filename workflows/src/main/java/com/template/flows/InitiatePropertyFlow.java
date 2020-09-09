package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.PropertyContract;
import com.template.states.PropertyState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

// ******************
// * Initiator flow *
// ******************

@StartableByRPC
public class InitiatePropertyFlow extends FlowLogic<SignedTransaction> {

    private final int propertyId;
    private final String propertyAddress;
    private final int propertyPrice;
    private final int buyerId;
    private final int sellerId;

    public InitiatePropertyFlow(int propertyId, String propertyAddress, int propertyPrice, int buyerId, int sellerId) {
        this.propertyId = propertyId;
        this.propertyAddress = propertyAddress;
        this.propertyPrice = propertyPrice;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        // Stage 1 - We are adding a time window of 20 seconds for the Notary service to sign the transaction
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        System.out.println("Notary is detected");

        PropertyState state = new PropertyState(propertyId,propertyAddress,propertyPrice,buyerId,sellerId,false,false,getOurIdentity(),"New Property",getOurIdentity());
        System.out.println("State is created");

        final Command<PropertyContract.Commands.Create> txcommand = new Command<>(new PropertyContract.Commands.Create(), Arrays.asList(getOurIdentity().getOwningKey()));
        System.out.println("Command is created");

        TransactionBuilder tx = new TransactionBuilder(notary)
                .addOutputState(state, PropertyContract.ID)
                .addCommand(txcommand)
                .setTimeWindow(Instant.now(), Duration.ofSeconds(40));
        System.out.println("Transaction is detected");

        // Stage 2 - Verify that the transaction is valid.
        tx.verify(getServiceHub());
        System.out.println("Verify is done");
        // Stage 3
        // Sign the transaction.
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(tx);
        System.out.println("Transaction is signed");

        // Notarise and record the transaction in both parties' vaults.
        System.out.println("Transaction is Notarised");
        return subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));
    }
}
