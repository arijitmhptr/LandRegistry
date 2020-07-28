package com.template.contracts;

import com.template.states.IOUState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.identity.AbstractParty;
import net.corda.core.transactions.LedgerTransaction;

import java.util.List;
import java.util.stream.Collectors;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class IOUContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.IOUContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
        if (tx.getCommands().size() != 1)
            throw new IllegalArgumentException("One command is required");

        CommandData command = tx.getCommand(0).getValue();
        if (command instanceof Commands.Issue){
            requireThat(req -> {
                req.using("There should not be any input state",
                        tx.getInputs().isEmpty());
                req.using("There should be one Output State",
                        tx.getOutputs().size() == 1);
                IOUState output = tx.outputsOfType(IOUState.class).get(0);
                req.using("Lender and Borrower shouldn't be same",
                        !output.getLender().equals(output.getBorrower()));
                req.using("All the participant must sign this transaction",
                        tx.getCommand(0).getSigners().containsAll(output.getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList())));
                req.using("The IOU amount ahould be positive",
                        output.getAmount() > 0);
                return null;
                    }

            );

        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Issue implements Commands {}
    }
}