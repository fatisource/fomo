// Required imports
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class StyledBankingGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankingSystemGUI().createAndShowGUI());
    }
}

class BankingSystemGUI {
    private JFrame frame;
    private JTable table;
    private JTextArea transactionArea;
    private JTextField nameField, accNumField, amountField, searchField;
    private JComboBox<String> typeCombo;
    private DefaultTableModel tableModel;

    private LinkedList<Account> accounts = new LinkedList<>(); // Changed to LinkedList

    private final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private final Color LIGHT_GREY = new Color(240, 240, 240);
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);

    public void createAndShowGUI() {
        frame = new JFrame("Banking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 650);
        frame.setLayout(new BorderLayout(10, 10));

        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 14));

        createInputPanel();
        createAccountTable();
        createTransactionPanel();

        frame.setVisible(true);
    }

    private void createInputPanel() {
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        inputPanel.setBackground(LIGHT_GREY);
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Account Operations", 0, 0, TITLE_FONT, Color.BLACK));

        accNumField = createLabeledTextField(inputPanel, "Account Number:");
        nameField = createLabeledTextField(inputPanel, "Customer Name:");

        JLabel typeLabel = new JLabel("Account Type:");
        typeLabel.setForeground(Color.BLACK);
        typeCombo = new JComboBox<>(new String[]{"Savings", "Current"});
        typeCombo.setForeground(Color.BLACK);
        inputPanel.add(wrapInPanel(typeLabel));
        inputPanel.add(wrapInPanel(typeCombo));

        amountField = createLabeledTextField(inputPanel, "Amount:");

        JButton createBtn = createButton("Create Account", e -> createAccount());
        JButton depositBtn = createButton("Deposit", e -> deposit());
        JButton withdrawBtn = createButton("Withdraw", e -> withdraw());
        JButton clearBtn = createButton("Clear", e -> clearFields());

        searchField = new JTextField(15);
        searchField.setForeground(Color.BLACK);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterAccounts(searchField.getText());
            }
        });

        inputPanel.add(wrapInPanel(new JLabel("Search:"))).setForeground(Color.BLACK);
        inputPanel.add(wrapInPanel(searchField));

        inputPanel.add(createBtn);
        inputPanel.add(depositBtn);
        inputPanel.add(withdrawBtn);
        inputPanel.add(clearBtn);

        frame.add(inputPanel, BorderLayout.NORTH);
    }

    private void createAccountTable() {
        String[] columns = {"Account #", "Name", "Type", "Balance"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setForeground(Color.BLACK);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> showTransactions());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Accounts", 0, 0, TITLE_FONT, Color.BLACK));

        frame.add(scrollPane, BorderLayout.CENTER);
    }

    private void createTransactionPanel() {
        transactionArea = new JTextArea(8, 30);
        transactionArea.setForeground(Color.BLACK);
        transactionArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(transactionArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Transactions", 0, 0, TITLE_FONT, Color.BLACK));

        frame.add(scrollPane, BorderLayout.SOUTH);
    }

    private JTextField createLabeledTextField(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Color.BLACK);
        JTextField textField = new JTextField(15);
        textField.setForeground(Color.BLACK);
        panel.add(wrapInPanel(label));
        panel.add(wrapInPanel(textField));
        return textField;
    }

    private JPanel wrapInPanel(JComponent comp) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(LIGHT_GREY);
        panel.add(comp);
        return panel;
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY_COLOR);
        btn.setForeground(Color.WHITE);
        btn.addActionListener(listener);
        return btn;
    }

    private void createAccount() {
        try {
            int accNum = Integer.parseInt(accNumField.getText());
            String name = nameField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();

            if (name.isEmpty()) throw new Exception("Name is required");

            for (Account a : accounts) if (a.accountNumber == accNum) throw new Exception("Account exists");

            Account account = new Account(accNum, name, type);
            accounts.add(account);
            updateTable();
            JOptionPane.showMessageDialog(frame, "Account Created Successfully");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deposit() {
        try {
            int accNum = Integer.parseInt(accNumField.getText());
            double amt = Double.parseDouble(amountField.getText());
            Account acc = find(accNum);
            acc.deposit(amt);
            updateTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void withdraw() {
        try {
            int accNum = Integer.parseInt(accNumField.getText());
            double amt = Double.parseDouble(amountField.getText());
            Account acc = find(accNum);
            acc.withdraw(amt);
            updateTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        accNumField.setText("");
        nameField.setText("");
        amountField.setText("");
        searchField.setText("");
        table.clearSelection();
        transactionArea.setText("");
        updateTable();
    }

    private Account find(int accNum) throws Exception {
        for (Account a : accounts) if (a.accountNumber == accNum) return a;
        throw new Exception("Account not found");
    }

    private void showTransactions() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int accNum = (int) tableModel.getValueAt(row, 0);
            try {
                Account acc = find(accNum);
                transactionArea.setText(String.join("\n", acc.transactions));
            } catch (Exception ignored) {}
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (Account acc : accounts) {
            tableModel.addRow(new Object[]{acc.accountNumber, acc.name, acc.accountType, String.format("₹%.2f", acc.balance)});
        }
    }

    private void filterAccounts(String keyword) {
        tableModel.setRowCount(0);
        for (Account acc : accounts) {
            if (String.valueOf(acc.accountNumber).contains(keyword) || acc.name.toLowerCase().contains(keyword.toLowerCase())) {
                tableModel.addRow(new Object[]{acc.accountNumber, acc.name, acc.accountType, String.format("₹%.2f", acc.balance)});
            }
        }
    }
}

class Account {
    int accountNumber;
    String name;
    String accountType;
    double balance = 0.0;
    LinkedList<String> transactions = new LinkedList<>(); // Changed to LinkedList

    public Account(int accNum, String name, String type) {
        this.accountNumber = accNum;
        this.name = name;
        this.accountType = type;
        transactions.add("Account opened with balance ₹0.00");
    }

    public void deposit(double amount) throws Exception {
        if (amount <= 0) throw new Exception("Amount must be positive");
        balance += amount;
        transactions.add("Deposited ₹" + amount + " | New Balance ₹" + balance);
    }

    public void withdraw(double amount) throws Exception {
        if (amount > balance) throw new Exception("Insufficient balance");
        balance -= amount;
        transactions.add("Withdrew ₹" + amount + " | New Balance ₹" + balance);
    }
}