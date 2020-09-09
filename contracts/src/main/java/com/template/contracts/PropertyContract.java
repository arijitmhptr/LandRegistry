package com.template.contracts;

import com.template.states.PropertyState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class PropertyContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.PropertyContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        if (tx.getCommands().size() != 1)
            throw new IllegalArgumentException("One command is required");

        CommandData command = tx.getCommand(0).getValue();

        if (command instanceof Commands.Create){
            requireThat(req -> {
                req.using("A property create transaction shouldn't consume any input state",
                        tx.getInputs().isEmpty());
                req.using("A property create transaction should produce one output state",
                        tx.getOutputs().size() == 1);
                PropertyState output = tx.outputsOfType(PropertyState.class).get(0);
                req.using("Buyer and Seller id shouldn't be same",
                        !(output.getBuyerId() == output.getSellerId()));
//                req.using("All the participant must sign this transaction",
//                        tx.getCommand(0).getSigners().containsAll(output.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList())));
                req.using("The Property price should be positive",
                        output.getPropertyPrice() > 0);
                return null; }
            );
        }
        else if (command instanceof Commands.Transfer){
            requireThat(req -> {
                req.using("A property transafer transaction should consume only one input state",
                        tx.getInputs().size() == 1);
                req.using("A property transafer transaction should create only one output state",
                        tx.getOutputs().size() == 1);
                final PropertyState input = tx.inputsOfType(PropertyState.class).get(0);
                final PropertyState output = tx.outputsOfType(PropertyState.class).get(0);
                req.using("The Owner must change in Property Transfer transaction",
                        input.getOwner() != output.getOwner());
                return null; }
            );
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Create implements Commands {}
        class Transfer implements Commands {}
    }
}