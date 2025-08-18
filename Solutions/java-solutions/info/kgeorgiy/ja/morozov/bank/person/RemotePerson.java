package info.kgeorgiy.ja.morozov.bank.person;

import info.kgeorgiy.ja.morozov.bank.account.Account;

import java.util.List;

/**
 * This class implements {@link Person}. It's a remote object.
 */
public class RemotePerson extends AbstractPerson {

    /**
     * This constructor creates a new person by given data. It also creates an account linked with this person.
     */
    public RemotePerson(String name, String surname, String passportData, List<Account> accounts) {
        super(name, surname, passportData, accounts);
    }
}
