package info.kgeorgiy.ja.morozov.bank;

import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;

/**
 * Simple tests for a remote bank.
 */
public class BankTests {
    /**
     * Entry point.
     */
    public static void main(String[] args) {
        Launcher launcher = LauncherFactory.create();

        LauncherDiscoveryRequest tests = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(BankJUnitTests.class))
                .build();

        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        launcher.execute(tests, listener);

        TestExecutionSummary summary = listener.getSummary();

        summary.printTo(new PrintWriter(System.out));
        if (summary.getTotalFailureCount() > 0) {
            System.exit(1);
        }
        System.exit(0);
    }
}
