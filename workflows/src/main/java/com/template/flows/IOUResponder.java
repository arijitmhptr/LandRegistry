package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.IOUState;
import net.corda.core.contracts.ContractState;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Responder flow *
// ******************
@InitiatedBy(IOUInitiator.class)
public class IOUResponder extends FlowLogic<SignedTransaction> {
    private final FlowSession counterpartySession;
    public IOUResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Responder flow logic goes here.
    class signflow extends SignTransactionFlow {
        private signflow(FlowSession otherSideSession,ProgressTracker progressTracker) {
            super(otherSideSession, progressTracker);
        }

        @Override
        protected void checkTransaction(SignedTransaction stx) {
            requireThat(req -> {
                ContractState data = stx.getTx().getOutputs().get(0).getData();
                req.using("This must be an IOU instance",data instanceof IOUState);
                return null;
                    });
        }
    }
        final signflow signtx = new signflow(counterpartySession, SignTransactionFlow.Companion.tracker());
        final SecureHash id = subFlow(signtx).getId();

        return subFlow(new ReceiveFinalityFlow(counterpartySession, id));
    }

}


