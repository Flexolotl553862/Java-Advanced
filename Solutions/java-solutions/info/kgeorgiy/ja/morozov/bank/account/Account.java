package info.kgeorgiy.ja.morozov.bank.account;

import java.math.BigDecimal;
import java.rmi.*;

/**
 * An account for bank app. It can be remote or local.
 */
public interface Account extends Remote {
    /**
     * Returns account identifier.
     */
    String getId() throws RemoteException;

    /**
     * Returns amount of money in the account.
     */
    BigDecimal getAmount() throws RemoteException;

    /**
     * Sets amount of money in the account.
     */
    void setAmount(BigDecimal amount) throws RemoteException;

    /**
     * Changes the amount of money by a given difference
     */
    void addAmount(BigDecimal difference) throws RemoteException;
}
