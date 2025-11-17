package gym;

// Pedro Salcedo
// CEN 3024 - Phase 3
// GUI for my workout tracker. Very simple layout and easy to use.

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class MainGUI extends JFrame {

    private final SessionService service = new SessionService();

    private final String[] cols = {"ID","Date","Exercise","Muscle","Sets","Reps","Weight","Duration","RPE","Notes"};
    private final DefaultTableModel model = new DefaultTableModel(cols, 0);
    private final JTable table = new JTable(model);

    // text fields
    private final JTextField idTxt = new JTextField();
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

    public MainGUI() {
        setTitle("Gym Workout Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); } catch (Exception ignore) {}
        setSize(1100, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JComponent)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // left side form
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(title("Workout Info"));
        form.add(row("ID:", idTxt));
        form.add(row("Date (yyyy-mm-dd):", dateTxt));
        form.add(row("Exercise:", exTxt));
        form.add(row("Muscle Group:", musTxt));
        form.add(row("Sets:", setsTxt));
        form.add(row("Reps:", repsTxt));
        form.add(row("Weight (lbs):", wtTxt));
        form.add(row("Duration (min):", durTxt));
        form.add(row("RPE (1-10):", rpeTxt));
        form.add(row("Notes:", notesTxt));

        // buttons
        JButton addBtn = new JButton("Add");
        JButton updBtn = new JButton("Update");
        JButton delBtn = new JButton("Delete");
        JButton showBtn = new JButton("Show");
        JButton clearBtn = new JButton("Clear");
        JButton exitBtn = new JButton("Exit");

        JPanel btns = new JPanel(new GridLayout(3, 2, 10, 10));
        btns.add(addBtn); btns.add(updBtn);
        btns.add(delBtn); btns.add(showBtn);
        btns.add(clearBtn); btns.add(exitBtn);
        form.add(Box.createVerticalStrut(10));
        form.add(btns);

        // table setup
        table.setFillsViewportHeight(true);
        table.setRowHeight(22);
        table.setDefaultEditor(Object.class, null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel right = new JPanel(new BorderLayout(8,8));
        right.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton customBtn = new JButton("Custom: 1RM + Volume");
        right.add(customBtn, BorderLayout.SOUTH);

        // bottom loader
        JPanel bottom = new JPanel(new BorderLayout(8,8));
        JButton loadBtn = new JButton("Load File");
        bottom.add(new JLabel("File path (.csv):"), BorderLayout.WEST);
        bottom.add(filePathTxt, BorderLayout.CENTER);
        bottom.add(loadBtn, BorderLayout.EAST);

        add(form, BorderLayout.WEST);
        add(right, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // text field sizes
        Dimension fieldSize = new Dimension(220, 28);
        for (JTextField tf : new JTextField[]{idTxt,dateTxt,exTxt,musTxt,setsTxt,repsTxt,wtTxt,durTxt,rpeTxt,notesTxt}) {
            tf.setPreferredSize(fieldSize);
            tf.setMaximumSize(fieldSize);
        }

        // button actions
        exitBtn.addActionListener(e -> System.exit(0));
        showBtn.addActionListener(e -> refreshTable());
        loadBtn.addActionListener(e -> onLoad());
        clearBtn.addActionListener(e -> clearForm());

        addBtn.addActionListener(e -> onAdd());
        delBtn.addActionListener(e -> onDelete());
        updBtn.addActionListener(e -> onUpdate());

        customBtn.addActionListener(e -> onCustom());

        // clicking a row fills the fields
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadRowIntoFields();
        });
    }

    // take table row and push values into the text fields
    private void loadRowIntoFields() {
        int row = table.getSelectedRow();
        if (row == -1) return;

        idTxt.setText(String.valueOf(model.getValueAt(row, 0)));
        dateTxt.setText(String.valueOf(model.getValueAt(row, 1)));
        exTxt.setText(String.valueOf(model.getValueAt(row, 2)));
        musTxt.setText(String.valueOf(model.getValueAt(row, 3)));
        setsTxt.setText(String.valueOf(model.getValueAt(row, 4)));
        repsTxt.setText(String.valueOf(model.getValueAt(row, 5)));
        wtTxt.setText(String.valueOf(model.getValueAt(row, 6)));
        durTxt.setText(String.valueOf(model.getValueAt(row, 7)));
        rpeTxt.setText(String.valueOf(model.getValueAt(row, 8)));
        notesTxt.setText(String.valueOf(model.getValueAt(row, 9)));
    }

    // add a new workout
    private void onAdd() {
        try {
            WorkoutSession s = readForm();
            if (!service.add(s)) {
                JOptionPane.showMessageDialog(this, "ID already exists.");
                return;
            }
            refreshTable();
            JOptionPane.showMessageDialog(this, "Added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // update an existing workout
    private void onUpdate() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pick a row first.");
            return;
        }

        try {
            WorkoutSession s = readForm();
            if (!service.updateFromFull(s)) {
                JOptionPane.showMessageDialog(this, "Update failed.");
                return;
            }
            refreshTable();
            JOptionPane.showMessageDialog(this, "Updated.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    // delete by id
    private void onDelete() {
        int id;
        try { id = Integer.parseInt(idTxt.getText().trim()); }
        catch (Exception e) { JOptionPane.showMessageDialog(this, "Enter a number for ID."); return; }

        if (!service.existsId(id)) {
            JOptionPane.showMessageDialog(this, "ID not found.");
            return;
        }

        service.deleteById(id);
        refreshTable();
        JOptionPane.showMessageDialog(this, "Deleted.");
    }

    // custom stats
    private void onCustom() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Pick a row first.");
            return;
        }

        String date = String.valueOf(model.getValueAt(row, 1));
        String ex = String.valueOf(model.getValueAt(row, 2));
        int sets = toIntSafe(model.getValueAt(row, 4));
        int reps = toIntSafe(model.getValueAt(row, 5));
        double weight = toDoubleSafe(model.getValueAt(row, 6));

        double volume = sets * reps * weight;
        double best = service.bestE1RMInLast7Days(ex, date);

        JOptionPane.showMessageDialog(this,
                "Exercise: " + ex +
                        "\nDate: " + date +
                        "\nVolume: " + volume +
                        "\nBest 1RM (7 days): " + best);
    }

    // read form data
    private WorkoutSession readForm() {
        WorkoutSession s = new WorkoutSession();

        s.id = parseIntNice(idTxt.getText(), "ID");

        String d = dateTxt.getText().trim();
        if (!d.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new IllegalArgumentException("Date must be yyyy-mm-dd.");
        s.date = d;

        s.exerciseName = mustText(exTxt.getText(), "exercise");
        s.muscleGroup  = mustText(musTxt.getText(), "muscle group");
        s.sets         = parseIntNice(setsTxt.getText(), "sets");
        s.reps         = parseIntNice(repsTxt.getText(), "reps");
        s.weightLbs    = parseDoubleNice(wtTxt.getText(), "weight");
        s.durationMin  = parseIntNice(durTxt.getText(), "duration");
        s.rpe          = parseIntNice(rpeTxt.getText(), "RPE");
        s.notes        = notesTxt.getText().trim();

        return s;
    }

    // load csv
    private void onLoad() {
        File f = new File(filePathTxt.getText().trim());
        if (!f.exists()) {
            JOptionPane.showMessageDialog(this, "File not found.");
            return;
        }

        int n = service.loadCsv(f);
        refreshTable();
        JOptionPane.showMessageDialog(this, "Loaded " + n + " rows.");
    }

    private void clearForm() {
        idTxt.setText(""); dateTxt.setText(""); exTxt.setText(""); musTxt.setText("");
        setsTxt.setText(""); repsTxt.setText(""); wtTxt.setText(""); durTxt.setText("");
        rpeTxt.setText(""); notesTxt.setText(""); table.clearSelection();
    }

    // refresh table
    private void refreshTable() {
        model.setRowCount(0);
        for (WorkoutSession s : service.listAll()) {
            model.addRow(new Object[]{
                    s.id, s.date, s.exerciseName, s.muscleGroup,
                    s.sets, s.reps, s.weightLbs, s.durationMin, s.rpe, s.notes
            });
        }
    }

    // small helpers
    private int parseIntNice(String x, String label) {
        try { return Integer.parseInt(x.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Enter a number for " + label + "."); }
    }

    private double parseDoubleNice(String x, String label) {
        try { return Double.parseDouble(x.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("Enter a number for " + label + "."); }
    }

    private String mustText(String x, String label) {
        String t = x.trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Please enter " + label + ".");
        return t;
    }

    private int toIntSafe(Object o) {
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
    }

    private double toDoubleSafe(Object o) {
        try { return Double.parseDouble(o.toString()); } catch (Exception e) { return 0; }
    }

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
