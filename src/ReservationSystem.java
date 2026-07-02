import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TASK 1 — Online Train Reservation System
 * Stack  : Java Swing + SQLite (via JDBC)
 * Setup  : Add sqlite-jdbc-*.jar to classpath (see README)
 */
public class ReservationSystem {

    // ── Colour palette ───────────────────────────────────────────────────────
    private static final Color BG      = new Color(30, 30, 46);
    private static final Color SURFACE = new Color(49, 50, 68);
    private static final Color ACCENT  = new Color(137, 180, 250);
    private static final Color GREEN   = new Color(166, 227, 161);
    private static final Color RED     = new Color(243, 139, 168);
    private static final Color TEXT    = new Color(205, 214, 244);
    private static final Color MUTED   = new Color(127, 132, 156);

    // ── Train data (number → name) ───────────────────────────────────────────
    private static final Map<String, String> TRAINS = new HashMap<>() {{
        put("12345", "Rajdhani Express");
        put("22691", "Rajdhani Express (Bangalore)");
        put("12951", "Mumbai Rajdhani");
        put("12001", "Shatabdi Express");
        put("12002", "Bhopal Shatabdi");
        put("12301", "Howrah Rajdhani");
    }};

    // ── Database ─────────────────────────────────────────────────────────────
    private Connection conn;

    // ── UI screens ───────────────────────────────────────────────────────────
    private JFrame frame;
    private CardLayout cards;
    private JPanel cardPanel;

    // Login
    private JTextField tfLoginUser;
    private JPasswordField pfLoginPass;

    // Reservation
    private JTextField tfPassenger, tfTrainNo, tfTrainName, tfDate, tfSrc, tfDest;
    private JComboBox<String> cbClass;

    // Cancellation
    private JTextField tfCancelPNR;
    private JLabel lblCancelDetails;
    private JButton btnConfirmCancel;
    private String currentCancelPNR;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReservationSystem().launch());
    }

    private void launch() {
        initDB();
        buildFrame();
        frame.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DATABASE
    // ═══════════════════════════════════════════════════════════════════════
    private void initDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:reservations.db");
            Statement st = conn.createStatement();
            st.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    pnr         TEXT PRIMARY KEY,
                    passenger   TEXT NOT NULL,
                    train_no    TEXT NOT NULL,
                    train_name  TEXT NOT NULL,
                    class_type  TEXT NOT NULL,
                    journey_date TEXT NOT NULL,
                    source      TEXT NOT NULL,
                    destination TEXT NOT NULL
                )""");
            // Demo users table
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL
                )""");
            st.execute("INSERT OR IGNORE INTO users VALUES ('admin','admin123')");
            st.execute("INSERT OR IGNORE INTO users VALUES ('user1','pass1')");
            st.close();
        } catch (Exception e) {
            showErr("DB Error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // FRAME / CARD LAYOUT
    // ═══════════════════════════════════════════════════════════════════════
    private void buildFrame() {
        frame = new JFrame("🚂 Train Reservation System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(580, 520);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(BG);

        cards     = new CardLayout();
        cardPanel = new JPanel(cards);
        cardPanel.setBackground(BG);

        cardPanel.add(buildLoginPanel(),       "LOGIN");
        cardPanel.add(buildReservationPanel(), "RESERVE");
        cardPanel.add(buildCancelPanel(),      "CANCEL");

        frame.add(cardPanel);
        cards.show(cardPanel, "LOGIN");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SCREEN 1 — LOGIN
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildLoginPanel() {
        JPanel p = darkPanel(new GridBagLayout());
        GridBagConstraints g = gbc();

        g.gridy = 0; p.add(label("🚂 Train Reservation System", 20, Font.BOLD, ACCENT), g);
        g.gridy = 1; p.add(label("Please log in to continue", 13, Font.ITALIC, MUTED), g);
        g.gridy = 2; p.add(Box.createVerticalStrut(20), g);

        tfLoginUser = field(16);
        pfLoginPass = new JPasswordField(16);
        styleField(pfLoginPass);

        g.gridy = 3; p.add(row("Username:", tfLoginUser), g);
        g.gridy = 4; p.add(row("Password:", pfLoginPass), g);
        g.gridy = 5; p.add(Box.createVerticalStrut(10), g);

        JButton btn = btn("Login", ACCENT);
        g.gridy = 6; p.add(btn, g);

        btn.addActionListener(e -> doLogin());
        pfLoginPass.addActionListener(e -> doLogin());

        return p;
    }

    private void doLogin() {
        String u = tfLoginUser.getText().trim();
        String pw = new String(pfLoginPass.getPassword());
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM users WHERE username=? AND password=?");
            ps.setString(1, u); ps.setString(2, pw);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                pfLoginPass.setText("");
                cards.show(cardPanel, "RESERVE");
            } else {
                showErr("❌ Invalid username or password.");
            }
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SCREEN 2 — RESERVATION
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildReservationPanel() {
        JPanel outer = darkPanel(new BorderLayout(10, 10));
        outer.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Header
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hdr.setOpaque(false);
        hdr.add(label("📋 Book a Ticket", 18, Font.BOLD, ACCENT));
        JButton switchBtn = btn("→ Cancellation", MUTED);
        switchBtn.addActionListener(e -> cards.show(cardPanel, "CANCEL"));
        hdr.add(switchBtn);
        outer.add(hdr, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(7, 2, 8, 10));
        form.setOpaque(false);

        tfPassenger = field(20); tfTrainNo = field(10);
        tfTrainName = field(20); tfTrainName.setEditable(false);
        tfTrainName.setBackground(new Color(39, 40, 55));
        tfDate = field(12); tfSrc = field(15); tfDest = field(15);
        cbClass = new JComboBox<>(new String[]{"Sleeper", "3AC", "2AC", "1AC", "General"});
        cbClass.setBackground(SURFACE); cbClass.setForeground(TEXT);

        tfTrainNo.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                String name = TRAINS.getOrDefault(tfTrainNo.getText().trim(), "");
                tfTrainName.setText(name);
            }
        });

        form.add(label("Passenger Name:", 13, Font.PLAIN, TEXT));  form.add(tfPassenger);
        form.add(label("Train Number:",   13, Font.PLAIN, TEXT));  form.add(tfTrainNo);
        form.add(label("Train Name:",     13, Font.PLAIN, TEXT));  form.add(tfTrainName);
        form.add(label("Class Type:",     13, Font.PLAIN, TEXT));  form.add(cbClass);
        form.add(label("Date (YYYY-MM-DD):",13,Font.PLAIN, TEXT)); form.add(tfDate);
        form.add(label("Source Station:", 13, Font.PLAIN, TEXT));  form.add(tfSrc);
        form.add(label("Destination:",    13, Font.PLAIN, TEXT));  form.add(tfDest);

        outer.add(form, BorderLayout.CENTER);

        JButton bookBtn = btn("🎫 Book Ticket", GREEN);
        bookBtn.addActionListener(e -> doBooking());
        outer.add(bookBtn, BorderLayout.SOUTH);
        return outer;
    }

    private void doBooking() {
        String passenger = tfPassenger.getText().trim();
        String trainNo   = tfTrainNo.getText().trim();
        String trainName = tfTrainName.getText().trim();
        String classType = (String) cbClass.getSelectedItem();
        String date      = tfDate.getText().trim();
        String src       = tfSrc.getText().trim();
        String dest      = tfDest.getText().trim();

        // Validation
        if (passenger.isEmpty() || trainNo.isEmpty() || trainName.isEmpty() ||
            date.isEmpty() || src.isEmpty() || dest.isEmpty()) {
            showErr("⚠ All fields are required."); return;
        }
        if (!trainNo.matches("\\d+")) {
            showErr("⚠ Train number must be numeric."); return;
        }
        try { LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE); }
        catch (DateTimeParseException e) { showErr("⚠ Date must be in YYYY-MM-DD format."); return; }

        String pnr = "PNR" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reservations VALUES (?,?,?,?,?,?,?,?)");
            ps.setString(1, pnr);      ps.setString(2, passenger);
            ps.setString(3, trainNo);  ps.setString(4, trainName);
            ps.setString(5, classType);ps.setString(6, date);
            ps.setString(7, src);      ps.setString(8, dest);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(frame,
                "✅ Booking Confirmed!\n\n" +
                "PNR       : " + pnr         + "\n" +
                "Passenger : " + passenger   + "\n" +
                "Train     : " + trainNo + " - " + trainName + "\n" +
                "Class     : " + classType   + "\n" +
                "Date      : " + date        + "\n" +
                "From → To : " + src + " → " + dest,
                "Booking Successful", JOptionPane.INFORMATION_MESSAGE);
            clearReservationForm();
        } catch (SQLException e) { showErr("DB Error: " + e.getMessage()); }
    }

    private void clearReservationForm() {
        tfPassenger.setText(""); tfTrainNo.setText("");
        tfTrainName.setText(""); tfDate.setText("");
        tfSrc.setText(""); tfDest.setText("");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SCREEN 3 — CANCELLATION
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildCancelPanel() {
        JPanel outer = darkPanel(new BorderLayout(10, 10));
        outer.setBorder(new EmptyBorder(15, 25, 15, 25));

        // Header
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT));
        hdr.setOpaque(false);
        hdr.add(label("❌ Cancel a Booking", 18, Font.BOLD, RED));
        JButton switchBtn = btn("→ Reservation", MUTED);
        switchBtn.addActionListener(e -> cards.show(cardPanel, "RESERVE"));
        hdr.add(switchBtn);
        outer.add(hdr, BorderLayout.NORTH);

        // PNR input
        JPanel mid = darkPanel(new GridBagLayout());
        GridBagConstraints g = gbc();
        tfCancelPNR = field(20);
        g.gridy = 0; mid.add(row("PNR Number:", tfCancelPNR), g);
        JButton fetchBtn = btn("🔍 Fetch Booking", ACCENT);
        g.gridy = 1; mid.add(fetchBtn, g);

        lblCancelDetails = label("", 13, Font.PLAIN, TEXT);
        lblCancelDetails.setBorder(new EmptyBorder(10, 10, 10, 10));
        g.gridy = 2; mid.add(lblCancelDetails, g);

        btnConfirmCancel = btn("🗑 Confirm Cancellation", RED);
        btnConfirmCancel.setVisible(false);
        g.gridy = 3; mid.add(btnConfirmCancel, g);

        fetchBtn.addActionListener(e -> doFetch());
        btnConfirmCancel.addActionListener(e -> doCancel());
        outer.add(mid, BorderLayout.CENTER);
        return outer;
    }

    private void doFetch() {
        String pnr = tfCancelPNR.getText().trim();
        if (pnr.isEmpty()) { showErr("⚠ Please enter a PNR."); return; }
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM reservations WHERE pnr=?");
            ps.setString(1, pnr);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                currentCancelPNR = pnr;
                lblCancelDetails.setText("<html><b>Passenger:</b> " + rs.getString("passenger") +
                    "<br><b>Train:</b> " + rs.getString("train_no") + " - " + rs.getString("train_name") +
                    "<br><b>Class:</b> " + rs.getString("class_type") +
                    "<br><b>Date:</b> " + rs.getString("journey_date") +
                    "<br><b>Route:</b> " + rs.getString("source") + " → " + rs.getString("destination") +
                    "</html>");
                lblCancelDetails.setForeground(TEXT);
                btnConfirmCancel.setVisible(true);
            } else {
                lblCancelDetails.setText("❌ No booking found for PNR: " + pnr);
                lblCancelDetails.setForeground(RED);
                btnConfirmCancel.setVisible(false);
                currentCancelPNR = null;
            }
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    private void doCancel() {
        if (currentCancelPNR == null) return;
        int choice = JOptionPane.showConfirmDialog(frame,
            "Are you sure you want to cancel booking " + currentCancelPNR + "?",
            "Confirm Cancellation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM reservations WHERE pnr=?");
            ps.setString(1, currentCancelPNR);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(frame, "✅ Booking " + currentCancelPNR + " has been cancelled.");
            tfCancelPNR.setText("");
            lblCancelDetails.setText("");
            btnConfirmCancel.setVisible(false);
            currentCancelPNR = null;
        } catch (SQLException ex) { showErr(ex.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel darkPanel(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG);
        return p;
    }
    private JLabel label(String txt, int size, int style, Color fg) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("SansSerif", style, size));
        l.setForeground(fg);
        return l;
    }
    private JTextField field(int cols) {
        JTextField tf = new JTextField(cols);
        styleField(tf);
        return tf;
    }
    private void styleField(JTextField tf) {
        tf.setBackground(SURFACE); tf.setForeground(TEXT);
        tf.setCaretColor(TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1),
            new EmptyBorder(4, 6, 4, 6)));
        tf.setFont(new Font("SansSerif", Font.PLAIN, 13));
    }
    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(BG);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        return b;
    }
    private JPanel row(String lbl, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setOpaque(false);
        JLabel l = label(lbl, 13, Font.PLAIN, TEXT);
        l.setPreferredSize(new Dimension(150, 24));
        p.add(l); p.add(field);
        return p;
    }
    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.gridx = 0; g.anchor = GridBagConstraints.CENTER;
        return g;
    }
    private void showErr(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
