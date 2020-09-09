package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.PropertyContract;
import com.template.states.PropertyState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.util.*;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ******************
// * Initiator flow *
// ******************
public class SurveyorApproveFlow {

@InitiatingFlow
@StartableByRPC
public static class SurveyorApproveInitiate extends FlowLogic<SignedTransaction> {

    private final UniqueIdentifier trackingid;
    private final Party owner;

    public SurveyorApproveInitiate(UniqueIdentifier trackingid, Party owner) {
        this.trackingid = trackingid;
        this.owner = owner;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        // 1. Retrieve the State from the vault using LinearStateQueryCriteria
        List<UUID> id = new ArrayList<>();
        id.add(trackingid.getId());

        QueryCriteria criteria = new QueryCriteria.LinearStateQueryCriteria(null, id);

        // 2. Get a reference to the inputState data that we are going to settle.
        Vault.Page results = getServiceHub().getVaultService().queryBy(PropertyState.class, criteria);
        StateAndRef inputState = (StateAndRef)results.getStates().get(0);
        PropertyState statetransfer = (PropertyState) inputState.getState().getData();

        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        final Command<PropertyContract.Commands.Transfer> txcommand = new Command<>(new PropertyContract.Commands.Transfer(), Arrays.asList(getOurIdentity().getOwningKey(), owner.getOwningKey()));
        TransactionBuilder tx = new TransactionBuilder(notary).addInputState(inputState)
                .addCommand(txcommand);

        if (statetransfer.getOwner().equals("") || statetransfer.getBuyerId()==0 || statetransfer.getSellerId()==0)
        {
            System.out.println("Inside If condition");
            tx.addOutputState(statetransfer.approvedBySurveyor(false, getOurIdentity()).transfer(owner, getOurIdentity()));
        }
        else{
            System.out.println("Inside Else condition");
            tx.addOutputState(statetransfer.approvedBySurveyor(true, getOurIdentity()).transfer(owner, getOurIdentity()));
        }
        // Stage 2 - Verify that the transaction is valid.
        tx.verify(getServiceHub());
        System.out.println("Verify is done");

        // Stage 3
        // Sign the transaction.
        final SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(tx);
        System.out.println("Transaction is partially signed");

        FlowSession session = initiateFlow(owner);
        System.out.println("Session is initiated with the CounterParty");

        final SignedTransaction fullsignTransaction = subFlow(new CollectSignaturesFlow(signedTransaction, Arrays.asList(session)));
        System.out.println("Transaction is fully signed");

        // Notarise and record the transaction in both parties' vaults.
        return subFlow(new FinalityFlow(fullsignTransaction, Arrays.asList(session)));
    }
    @InitiatedBy(SurveyorApproveInitiate.class)
    public static class SurveyorApproveRespond extends FlowLogic<SignedTransaction> {

        private final FlowSession countersession;

        public SurveyorApproveRespond(FlowSession countersession) {
            this.countersession = countersession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class signFlow extends SignTransactionFlow {

                private signFlow(FlowSession otherSideSession) {
                    super(otherSideSession);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    System.out.println("Inside CounterParty session");
                    requireThat(require ->{
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        require.using("This must be Property Transfer transaction", output instanceof PropertyState);
                        return null;
                    });
                }
            }
            final signFlow signtx = new signFlow(countersession);
            final SecureHash signedhash = subFlow(signtx).getId();

            return subFlow(new ReceiveFinalityFlow(countersession, signedhash));
        }
    }
}
}