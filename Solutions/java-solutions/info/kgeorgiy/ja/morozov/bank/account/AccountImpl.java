package info.kgeorgiy.ja.morozov.bank.account;

import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of {@link Account}.
 * It can be local or remote.
 */
public class AccountImpl implements Account, Serializable {
    private final String id;
    private final AtomicReference<BigDecimal> amount;

    /**
     * A constructor for creating remote account.
     * @param id given id.
     * @param amount given amount of money.
     */
    public AccountImpl(final String id, BigDecimal amount) {
        this.id = id;
        this.amount = new AtomicReference<>(amount);
    }

    /**
     * A constructor for creating local account using a remote account.
     * @param account given account.
     * @throws RemoteException if there are problems with access to a remote account.
     */
    public AccountImpl(Account account) throws RemoteException {
        this.id = account.getId();
        this.amount = new AtomicReference<>(account.getAmount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getAmount() {
        System.out.println("Getting amount of money for account " + id);
        return amount.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAmount(final BigDecimal amount) {
        System.out.println("Setting amount of money for account " + id);
        this.amount.set(amount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAmount(BigDecimal difference) {
        this.amount.updateAndGet((last) -> {
            if (last.add(difference).compareTo(BigDecimal.ZERO) < 0) {
                System.out.println("Updating amount of for account " + id + " has been failed");
                return last;
            }
            System.out.println("Updating amount of for account " + id + " has been successful");
            return last.add(difference);
        });
    }
}
