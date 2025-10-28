// Pedro Salcedo
// CEN 3024 - Phase 3
// MainGUI: simple GUI for my Gym Workout Tracker

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainGUI extends JFrame {

    private final SessionService service = new SessionService();

    private final String[] cols = {"ID","Date","Exercise","Muscle","Sets","Reps","Weight","Duration","RPE","Notes"};
    private final DefaultTableModel model = new DefaultTableModel(cols, 0);
    private final JTable table = new JTable(model);

    // inputs
    private final JTextField idTxt = new JTextField();
    private final JLabel     idStatus = new JLabel(" ");
    private final JTextField dateTxt = new JTextField();
    private final JTextField exTxt = new JTextField();
    private final JTextField musTxt = new JTextField();
    private final JTextField setsTxt = new JTextField();
    private final JTextField repsTxt = new JTextField();
    private final JTextField wtTxt = new JTextField();
    private final JTextField durTxt = new JTextField();
    private final JTextField rpeTxt = new JTextField();
    private final JTextField notesTxt = new JTextField();

    private final JTextField filePathTxt = new JTextField();

    // allowed fields for update
    private final java.util.List<String> allowedFields =
            Arrays.asList("date","exercise","group","sets","reps","weight","duration","rpe","notes");

    public MainGUI() {
        setTitle("Gym Workout Tracker - GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignore) {}
        setSize(1100, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));
        ((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // left side: fields + buttons
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(title("Session"));
        form.add(row("ID (int):", idTxt));
        form.add(row("ID status:", idStatus));
        form.add(row("Date (yyyy-mm-dd):", dateTxt));
        form.add(row("Exercise:", exTxt));
        form.add(row("Muscle:", musTxt));
        form.add(row("Sets:", setsTxt));
        form.add(row("Reps:", repsTxt));
        form.add(row("Weight (lbs):", wtTxt));
        form.add(row("Duration (min):", durTxt));
        form.add(row("RPE (1-10):", rpeTxt));
        form.add(row("Notes:", notesTxt));
        form.add(Box.createVerticalStrut(6));

        JButton addBtn   = new JButton("Add");
        JButton updBtn   = new JButton("Update (field)");
        JButton applyUpd = new JButton("Apply Update");
        JButton delBtn   = new JButton("Delete");
        JButton showBtn  = new JButton("Display All");
        JButton clearBtn = new JButton("Clear");
        JButton exitBtn  = new JButton("Exit");

        JPanel btns = new JPanel(new GridLayout(3, 2, 8, 8));
        btns.add(addBtn); btns.add(updBtn);
        btns.add(applyUpd); btns.add(delBtn);
        btns.add(showBtn); btns.add(clearBtn);
        JPanel exitRow = new JPanel(new GridLayout(1,1));
        exitRow.add(exitBtn);

        form.add(btns);
        form.add(Box.createVerticalStrut(6));
        form.add(exitRow);
        form.setPreferredSize(new Dimension(380, getHeight()));
        idStatus.setForeground(Color.DARK_GRAY);

        // right side: table + custom
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.setDefaultEditor(Object.class, null);                  // read-only table
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // single selection

        JPanel right = new JPanel(new BorderLayout(8,8));
        right.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton customBtn = new JButton("Custom: 1RM + Volume (selected)");
        right.add(customBtn, BorderLayout.SOUTH);

        // bottom: file loader
        JPanel bottom = new JPanel(new BorderLayout(8,8));
        JButton loadBtn = new JButton("Load From File");
        bottom.add(new JLabel("File path (.txt/.csv):"), BorderLayout.WEST);
        bottom.add(filePathTxt, BorderLayout.CENTER);
        bottom.add(loadBtn, BorderLayout.EAST);

        add(form, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // field widths
        Dimension fieldSize = new Dimension(220, 28);
        for (JTextField tf : new JTextField[]{idTxt,dateTxt,exTxt,musTxt,setsTxt,repsTxt,wtTxt,durTxt,rpeTxt,notesTxt}) {
            tf.setPreferredSize(fieldSize);
            tf.setMaximumSize(fieldSize);
        }

        // listeners
        exitBtn.addActionListener(e -> System.exit(0));
        clearBtn.addActionListener(e -> {
            idTxt.setText(""); dateTxt.setText(""); exTxt.setText(""); musTxt.setText("");
            setsTxt.setText(""); repsTxt.setText(""); wtTxt.setText(""); durTxt.setText("");
            rpeTxt.setText(""); notesTxt.setText(""); idStatus.setText(" ");
            table.clearSelection();
        });
        showBtn.addActionListener(e -> refreshTable());
        loadBtn.addActionListener(e -> onLoad());
        addBtn.addActionListener(e -> onAdd());
        delBtn.addActionListener(e -> onDelete());

        updBtn.addActionListener(e -> JOptionPane.showMessageDialog(
                this, "Type the ID, then click 'Apply Update' to change one field."));
        applyUpd.addActionListener(e -> onApplyUpdate());

        customBtn.addActionListener(e -> onCustom());
    }

    // ADD
    private void onAdd() {
        int id;
        try { id = Integer.parseInt(idTxt.getText().trim()); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "Please enter a whole number for ID."); return; }

        if (service.existsId(id)) {
            JOptionPane.showMessageDialog(this, "That ID is already used. Pick a different ID.");
            return;
        }

        try {
            WorkoutSession s = readFormUsingId(id);
            boolean ok = service.add(s);
            if (!ok) { JOptionPane.showMessageDialog(this, "Invalid data. Please check the fields."); return; }
            refreshTable();
            JOptionPane.showMessageDialog(this, "Added.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Problem", JOptionPane.WARNING_MESSAGE);
        }
    }

    // DELETE
    private void onDelete() {
        int id;
        try { id = Integer.parseInt(idTxt.getText().trim()); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "Please enter a whole number for ID."); return; }

        if (!service.existsId(id)) {
            JOptionPane.showMessageDialog(this, "That ID does not exist.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Delete record " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        if (!service.deleteById(id)) {
            JOptionPane.showMessageDialog(this, "Delete failed.");
            return;
        }
        refreshTable();
        JOptionPane.showMessageDialog(this, "Deleted.");
    }

    // UPDATE
    private void onApplyUpdate() {
        int id;
        try { id = Integer.parseInt(idTxt.getText().trim()); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "Please enter a whole number for ID."); return; }

        if (!service.existsId(id)) {
            JOptionPane.showMessageDialog(this, "That ID does not exist.");
            return;
        }

        String field = JOptionPane.showInputDialog(this,
                "Which field? (date, exercise, group, sets, reps, weight, duration, rpe, notes)");
        if (field == null) return; // cancel
        field = field.trim().toLowerCase();

        if (!allowedFields.contains(field)) {
            JOptionPane.showMessageDialog(this, "Not a valid field. Try one of: " + String.join(", ", allowedFields));
            return;
        }

        String value = JOptionPane.showInputDialog(this, "Enter new value:");
        if (value == null) return;

        boolean ok = service.updateById(id, field, value.trim());
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Update failed. Check the value.");
            return;
        }
        refreshTable();
        JOptionPane.showMessageDialog(this, "Updated.");
    }

    // CUSTOM: uses the selected row
    private void onCustom() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a row in the table first.");
            return;
        }

        String date = String.valueOf(model.getValueAt(row, 1));   // use this as the reference date
        String exercise = String.valueOf(model.getValueAt(row, 2));
        int sets = toIntSafe(model.getValueAt(row, 4));
        int reps = toIntSafe(model.getValueAt(row, 5));
        double weight = toDoubleSafe(model.getValueAt(row, 6));

        double volume = sets * reps * weight;
        double best = service.bestE1RMInLast7Days(exercise, date);

        String msg = "Exercise: " + exercise +
                "\nDate used: " + date +
                "\nVolume (sets×reps×weight): " + round1(volume) + " lbs" +
                "\nBest est. 1RM (last 7 days): " + round1(best) + " lbs";
        JOptionPane.showMessageDialog(this, msg);
    }

    // read the form for Add
    private WorkoutSession readFormUsingId(int id) {
        WorkoutSession s = new WorkoutSession();
        s.id = id;

        String d = dateTxt.getText().trim();
        if (!d.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new IllegalArgumentException("Please enter a date (e.g., 2025-10-27).");
        s.date = d;

        s.exerciseName = mustText(exTxt.getText(), "Exercise");
        s.muscleGroup  = mustText(musTxt.getText(), "Muscle");
        s.sets         = parseIntNice(setsTxt.getText(), "Sets");
        s.reps         = parseIntNice(repsTxt.getText(), "Reps");
        s.weightLbs    = parseDoubleNice(wtTxt.getText(), "Weight");
        s.durationMin  = parseIntNice(durTxt.getText(), "Duration");
        s.rpe          = parseIntNice(rpeTxt.getText(), "RPE");
        if (s.rpe < 1 || s.rpe > 10) throw new IllegalArgumentException("RPE must be between 1 and 10.");
        s.notes        = notesTxt.getText().trim();
        return s;
    }

    // LOAD
    private void onLoad() {
        String path = filePathTxt.getText().trim();
        if (path.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter a file path."); return; }
        File f = new File(path);
        if (!f.exists()) { JOptionPane.showMessageDialog(this, "File not found."); return; }
        int n = service.loadCsv(f);
        refreshTable();
        JOptionPane.showMessageDialog(this, "Loaded " + n + " rows.");
    }

    // table refresh
    private void refreshTable() {
        ArrayList<WorkoutSession> rows = service.listAll();
        model.setRowCount(0);
        for (WorkoutSession s : rows) {
            model.addRow(new Object[]{
                    s.id, s.date, s.exerciseName, s.muscleGroup,
                    s.sets, s.reps, s.weightLbs, s.durationMin, s.rpe, s.notes
            });
        }
    }

    // small helpers
    private int parseIntNice(String s, String label) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(label + " must be a whole number."); }
    }
    private double parseDoubleNice(String s, String label) {
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException(label + " must be a number."); }
    }
    private String mustText(String s, String label) {
        String t = s.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Please enter " + label.toLowerCase() + ".");
        return t;
    }
    private int toIntSafe(Object o) {
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return 0; }
    }
    private double toDoubleSafe(Object o) {
        try { return Double.parseDouble(String.valueOf(o)); } catch (Exception e) { return 0.0; }
    }
    private String round1(double x) {
        return String.format("%.1f", x);
    }

    // UI helpers
    private JPanel row(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(8,0));
        JLabel l = new JLabel(label);
        l.setPreferredSize(new Dimension(140, 28));
        p.add(l, BorderLayout.WEST);
        p.add(comp, BorderLayout.CENTER);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return p;
    }
    private JLabel title(String t) {
        JLabel l = new JLabel(t);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 16f));
        l.setBorder(BorderFactory.createEmptyBorder(0,0,8,0));
        return l;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().setVisible(true));
    }
}
