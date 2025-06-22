import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class StyledBankingGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankingSystemGUI().createAndShowGUI());
    }
}

enum AccountType {
    SAVINGS, CURRENT;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}

class Account {
    final int accountNumber;
    final String name;
    final AccountType accountType;
    double balance = 0.0;
    final List<String> transactions = new ArrayList<>();

    public Account(int accNum, String name, AccountType type) {
        this.accountNumber = accNum;
        this.name = name;
        this.accountType = type;
        addTransaction("Account opened with balance ₹0.00");
    }

    public void deposit(double amount) throws Exception {
        if (amount <= 0) throw new Exception("Amount must be positive");
        balance += amount;
        addTransaction("Deposited ₹" + String.format("%.2f", amount) + 
            " | New Balance ₹" + String.format("%.2f", balance));
    }

    public void withdraw(double amount) throws Exception {
        if (amount <= 0) throw new Exception("Amount must be positive");
        if (amount > balance) throw new Exception("Insufficient balance");
        balance -= amount;
        addTransaction("Withdrew ₹" + String.format("%.2f", amount) + 
            " | New Balance ₹" + String.format("%.2f", balance));
    }

    private void addTransaction(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        transactions.add("[" + timestamp + "] " + message);
    }
}

class BankingSystemGUI {
    private JFrame frame;
    private JTable table;
    private JTextArea transactionArea;
    private JTextField nameField, accNumField, amountField, searchField;
    private JComboBox<AccountType> typeCombo;
    private DefaultTableModel tableModel;

    private final Map<Integer, Account> accounts = new HashMap<>();

    private final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel Blue
    private final Color LIGHT_GREY = new Color(240, 240, 240);
    private final Font TITLE_FONT = new Font("Arial", Font.BOLD, 14);

    public void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        frame = new JFrame("Banking System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout(10, 10));

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
                "Account Operations", TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, Color.BLACK));

        accNumField = createLabeledTextField(inputPanel, "Account Number:", "Enter unique account number (numbers only)");
        nameField = createLabeledTextField(inputPanel, "Customer Name:", "Enter customer name");
        JLabel typeLabel = new JLabel("Account Type:");
        typeCombo = new JComboBox<>(AccountType.values());
        typeCombo.setToolTipText("Choose account type");
        inputPanel.add(wrapInPanel(typeLabel));
        inputPanel.add(wrapInPanel(typeCombo));
        amountField = createLabeledTextField(inputPanel, "Amount:", "Enter the amount for deposit/withdrawal");

        JButton createBtn = createButton("Create Account", e -> createAccount());
        JButton depositBtn = createButton("Deposit", e -> deposit());
        JButton withdrawBtn = createButton("Withdraw", e -> withdraw());
        JButton clearBtn = createButton("Clear", e -> clearFields());

        searchField = new JTextField(15);
        searchField.setToolTipText("Search by account number or name");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                filterAccounts(searchField.getText());
            }
        });

        JLabel searchLabel = new JLabel("Search:");
        inputPanel.add(wrapInPanel(searchLabel));
        inputPanel.add(wrapInPanel(searchField));
        inputPanel.add(createBtn);
        inputPanel.add(depositBtn);
        inputPanel.add(withdrawBtn);
        inputPanel.add(clearBtn);

        frame.add(inputPanel, BorderLayout.NORTH);
    }

    private void createAccountTable() {
        String[] columns = {"Account #", "Name", "Type", "Balance"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> showTransactions());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Accounts", TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, Color.BLACK));

        frame.add(scrollPane, BorderLayout.CENTER);
    }

    private void createTransactionPanel() {
        transactionArea = new JTextArea(10, 50);
        transactionArea.setEditable(false);
        transactionArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JScrollPane scrollPane = new JScrollPane(transactionArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                "Transactions", TitledBorder.LEFT, TitledBorder.TOP, TITLE_FONT, Color.BLACK));

        frame.add(scrollPane, BorderLayout.SOUTH);
    }

    private JTextField createLabeledTextField(JPanel panel, String labelText, String tooltip) {
        JLabel label = new JLabel(labelText);
        JTextField textField = new JTextField(15);
        textField.setToolTipText(tooltip);
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
        btn.setFocusPainted(false);
        btn.addActionListener(listener);
        btn.setToolTipText(text);
        return btn;
    }

    private void createAccount() {
        try {
            int accNum = parseIntField(accNumField, "Account Number");
            String name = nameField.getText().trim();
            if (name.isEmpty()) throw new Exception("Name is required");
            if (accounts.containsKey(accNum)) throw new Exception("Account already exists");

            AccountType type = (AccountType) typeCombo.getSelectedItem();
            Account account = new Account(accNum, name, type);
            accounts.put(accNum, account);
            updateTable();
            JOptionPane.showMessageDialog(frame, "Account Created Successfully");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void deposit() {
        try {
            int accNum = parseIntField(accNumField, "Account Number");
            double amt = parseDoubleField(amountField, "Amount");
            Account acc = findAccount(accNum);
            acc.deposit(amt);
            updateTable();
            showTransactionsForAccount(acc);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void withdraw() {
        try {
            int accNum = parseIntField(accNumField, "Account Number");
            double amt = parseDoubleField(amountField, "Amount");
            Account acc = findAccount(accNum);
            acc.withdraw(amt);
            updateTable();
            showTransactionsForAccount(acc);
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private int parseIntField(JTextField field, String name) throws Exception {
        String text = field.getText().trim();
        if (text.isEmpty()) throw new Exception(name + " is required");
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new Exception(name + " must be a number");
        }
    }

    private double parseDoubleField(JTextField field, String name) throws Exception {
        String text = field.getText().trim();
        if (text.isEmpty()) throw new Exception(name + " is required");
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new Exception(name + " must be a number");
        }
    }

    private Account findAccount(int accNum) throws Exception {
        Account acc = accounts.get(accNum);
        if (acc == null) throw new Exception("Account not found");
        return acc;
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

    private void showTransactions() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            int accNum = (int) tableModel.getValueAt(row, 0);
            Account acc = accounts.get(accNum);
            if (acc != null) showTransactionsForAccount(acc);
        }
    }

    private void showTransactionsForAccount(Account acc) {
        StringBuilder sb = new StringBuilder();
        for (String t : acc.transactions)
            sb.append(t).append("\n");
        transactionArea.setText(sb.toString());
    }

    private void updateTable() {
        String keyword = searchField.getText().trim();
        tableModel.setRowCount(0);
        for (Account acc : accounts.values()) {
            if (keyword.isEmpty() || matchesKeyword(acc, keyword)) {
                tableModel.addRow(new Object[]{
                        acc.accountNumber,
                        acc.name,
                        acc.accountType,
                        String.format("₹%.2f", acc.balance)
                });
            }
        }
    }

    private boolean matchesKeyword(Account acc, String keyword) {
        return String.valueOf(acc.accountNumber).contains(keyword) ||
               acc.name.toLowerCase().contains(keyword.toLowerCase());
    }

    private void filterAccounts(String keyword) {
        updateTable();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
