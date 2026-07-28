package com.spearforge.sBank.modules;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReserveBackedEconomyPolicyTest {

    @Test
    void wealthAndDebtFlowsFundReserveBeforeNewLoansUseIt() throws IOException {
        String tax = Files.readString(Path.of("src/main/java/com/spearforge/sBank/modules/WealthTaxScheduler.java"));
        String debt = Files.readString(Path.of("src/main/java/com/spearforge/sBank/modules/DebtModule.java"));
        String loan = Files.readString(Path.of("src/main/java/com/spearforge/sBank/listener/LoanGuiListener.java"));

        assertTrue(tax.contains("EconomyReserve.credit(charge)"));
        assertTrue(debt.contains("EconomyReserve.credit(payment)"));
        assertTrue(loan.contains("EconomyReserve.allocateLoan(loan)"));
        assertTrue(loan.contains("EconomyReserve.restoreLoan(loan)"));
    }
}
