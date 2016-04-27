package de.sjsolutions.pipay;

public interface FragmentListener {
    void addBalance(double amount);

    double getBalance();

    void showSnackbar(String text);
}
