package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.IOUContract;
import com.template.states.IOUState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singletonList;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class IOUInitiator extends FlowLogic<SignedTransaction> {
    private final int amount;
    private final Party otherparty;

    private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new IOU.");
    private final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
    private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
    private final Step GATHERING_SIGS = new Step("Gathering the counterparty's signature.") {
        
        @Override
        public ProgressTracker childProgressTracker() {
            return CollectSignaturesFlow.Companion.tracker();
        }
    };
    private final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
            return FinalityFlow.Companion.tracker();
        }
    };

    // The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
    // checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call()
    // function.
    private final ProgressTracker progressTracker = new ProgressTracker(
            GENERATING_TRANSACTION,
            VERIFYING_TRANSACTION,
            SIGNING_TRANSACTION,
            GATHERING_SIGS,
            FINALISING_TRANSACTION
    );

    public IOUInitiator(int amount, Party otherparty) {
        this.amount = amount;
        this.otherparty = otherparty;
    }

    @Override
    public ProgressTracker getProgressTracker() { return progressTracker; }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        // Initiator flow logic goes here.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        
        // Stage 1.
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);

        Party me = getOurIdentity();
        IOUState state = new IOUState(amount,me,otherparty,new UniqueIdentifier());
        final Command<IOUContract.Commands.Issue> txcommand = new Command<>(new IOUContract.Commands.Issue(),
                Arrays.asList(state.getLender().getOwningKey(), state.getBorrower().getOwningKey()));
        TransactionBuilder tx = new TransactionBuilder(notary)
                .addOutputState(state, IOUContract.ID)
                .addCommand(txcommand);
        
                // Stage 2.
        progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
        // Verify that the transaction is valid.
        tx.verify(getServiceHub());
         
        // Stage 3.
         progressTracker.setCurrentStep(SIGNING_TRANSACTION);
         // Sign the transaction.
        final SignedTransaction partSign = getServiceHub().signInitialTransaction(tx);
        
        // Stage 4.
         progressTracker.setCurrentStep(GATHERING_SIGS);
         // Send the state to the counterparty, and receive it back with their signature.
        FlowSession session = initiateFlow(otherparty);
        final SignedTransaction fullSign = subFlow(new CollectSignaturesFlow(partSign, Arrays.asList(session), CollectSignaturesFlow.Companion.tracker()));
        
        // Stage 5.
         progressTracker.setCurrentStep(FINALISING_TRANSACTION);
         // Notarise and record the transaction in both parties' vaults.
        return subFlow(new FinalityFlow(fullSign, Arrays.asList(session)));
    }
}
