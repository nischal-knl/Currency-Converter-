import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class CurrencyConverter {

    private static String API_URL =
            "https://v6.exchangerate-api.com/v6/d983432e30fd653515345c69/latest/";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CurrencyConverter().createGUI());
    }

    // ================= GUI ===================
    public void createGUI() {

        JFrame frame = new JFrame("Currency Converter");
        frame.setSize(520, 420);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout());

        // ---------- HEADER ----------
        JPanel header = new JPanel();
        header.setBackground(new Color(25, 118, 210));
        JLabel title = new JLabel("Currency Converter");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        header.add(title);
        frame.add(header, BorderLayout.NORTH);

        // ---------- MAIN PANEL ----------
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Currency List
        String[] currencyNames = {"USD", "NPR", "EUR", "INR", "GBP", "JPY"};
        String[] flagPaths = {
                "flags/us.png",
                "flags/np.png",
                "flags/eu.png",
                "flags/in.png",
                "flags/uk.png",
                "flags/jp.png"
        };

        JComboBox<String> fromBox = new JComboBox<>(currencyNames);
        JComboBox<String> toBox = new JComboBox<>(currencyNames);

        fromBox.setRenderer(new IconRenderer(flagPaths));
        toBox.setRenderer(new IconRenderer(flagPaths));

        JTextField amountField = new JTextField();

        JLabel resultLabel = new JLabel("Converted Amount: ", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // ---------- CONVERT BUTTON ----------
        JButton convertBtn = new JButton("Convert");
        convertBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        convertBtn.setBackground(new Color(25, 118, 210));
        convertBtn.setForeground(Color.WHITE);
        convertBtn.setFocusPainted(false);
        convertBtn.setBorder(null);

        // ---------- SWAP BUTTON (icon only) ----------
        JButton swapBtn = new JButton();
        swapBtn.setPreferredSize(new Dimension(50, 40));
        swapBtn.setBackground(new Color(230, 230, 230));
        swapBtn.setFocusPainted(false);
        swapBtn.setBorder(null);

        ImageIcon swapIcon = new ImageIcon("flags/swap.png");
        Image scaledSwap = swapIcon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        swapBtn.setIcon(new ImageIcon(scaledSwap));

        // ---------- ADD COMPONENTS ----------
        amountField.setPreferredSize(new Dimension(250, 35));
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        convertBtn.setPreferredSize(new Dimension(140, 45));
        convertBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));

        swapBtn.setPreferredSize(new Dimension(55, 40));

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("From Currency:"), gbc);

        gbc.gridx = 1;
        panel.add(fromBox, gbc);

        gbc.gridx = 2;
        panel.add(swapBtn, gbc);

        gbc.gridx = 3;
        panel.add(toBox, gbc);

// Amount row
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Amount:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        panel.add(amountField, gbc);
        gbc.gridwidth = 1;

// Convert button row
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(convertBtn, gbc);

// Result label
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 4;
        panel.add(resultLabel, gbc);

        frame.add(panel, BorderLayout.CENTER);

        // ---------- SWAP ACTION ----------
        swapBtn.addActionListener(e -> {
            int f = fromBox.getSelectedIndex();
            int t = toBox.getSelectedIndex();

            fromBox.setSelectedIndex(t);
            toBox.setSelectedIndex(f);
        });

        // ---------- CONVERT ACTION ----------
        convertBtn.addActionListener(e -> {
            try {
                String from = fromBox.getSelectedItem().toString();
                String to = toBox.getSelectedItem().toString();
                double amount = Double.parseDouble(amountField.getText());

                double rate = fetchRate(from, to);

                double converted = amount * rate;
                resultLabel.setText("Converted Amount: " + converted + " " + to);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid amount.");
            }
        });

        frame.setVisible(true);
    }

    // ================= FETCH RATE ===================
    private double fetchRate(String base, String target) {
        try {
            URL url = new URL(API_URL + base);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) response.append(line);
            br.close();

            JSONObject json = new JSONObject(response.toString());
            JSONObject rates = json.getJSONObject("conversion_rates");

            return rates.getDouble(target);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // ================= FLAG RENDERER ===================
    class IconRenderer extends DefaultListCellRenderer {
        String[] icons;

        public IconRenderer(String[] icons) {
            this.icons = icons;
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);

            int i = list.getSelectedIndex();
            if (index >= 0) i = index;

            if (i >= 0 && i < icons.length) {
                ImageIcon icon = new ImageIcon(icons[i]);
                Image scaled = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
            }

            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            return label;
        }
    }
}
