// Pedro Salcedo
// CEN 3024 - Software Development 1
// SessionService: does the in-memory work (CRUD, search, CSV, custom 1RM).

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class SessionService {
    public ArrayList<WorkoutSession> sessions = new ArrayList<>();

    // CREATE
    public boolean add(WorkoutSession s) {
        if (s == null || !s.isValid()) return false;
        if (findIndexById(s.id) != -1) return false;  // id must be unique
        sessions.add(s);
        return true;
    }

    // READ
    public ArrayList<WorkoutSession> listAll() { return sessions; }

    public ArrayList<WorkoutSession> findByDate(String date) {
        ArrayList<WorkoutSession> out = new ArrayList<>();
        for (WorkoutSession s : sessions) if (s.date.equals(date)) out.add(s);
        return out;
    }

    public ArrayList<WorkoutSession> findByExercise(String name) {
        ArrayList<WorkoutSession> out = new ArrayList<>();
        for (WorkoutSession s : sessions)
            if (s.exerciseName.equalsIgnoreCase(name)) out.add(s);
        return out;
    }

    public ArrayList<WorkoutSession> findByGroup(String group) {
        ArrayList<WorkoutSession> out = new ArrayList<>();
        for (WorkoutSession s : sessions)
            if (s.muscleGroup.equalsIgnoreCase(group)) out.add(s);
        return out;
    }

    // UPDATE
    public boolean updateById(int id, String field, String newValue) {
        int idx = findIndexById(id);
        if (idx == -1) return false;

        WorkoutSession cur = sessions.get(idx);
        WorkoutSession tmp = copy(cur);

        try {
            if ("date".equalsIgnoreCase(field)) tmp.date = newValue;
            else if ("exercise".equalsIgnoreCase(field)) tmp.exerciseName = newValue;
            else if ("group".equalsIgnoreCase(field)) tmp.muscleGroup = newValue;
            else if ("sets".equalsIgnoreCase(field)) tmp.sets = Integer.parseInt(newValue);
            else if ("reps".equalsIgnoreCase(field)) tmp.reps = Integer.parseInt(newValue);
            else if ("weight".equalsIgnoreCase(field)) tmp.weightLbs = Double.parseDouble(newValue);
            else if ("duration".equalsIgnoreCase(field)) tmp.durationMin = Integer.parseInt(newValue);
            else if ("rpe".equalsIgnoreCase(field)) tmp.rpe = Integer.parseInt(newValue);
            else if ("notes".equalsIgnoreCase(field)) tmp.notes = newValue;
            else return false;
        } catch (Exception e) { return false; }

        if (!tmp.isValid()) return false;
        sessions.set(idx, tmp);
        return true;
    }

    // DELETE
    public boolean deleteById(int id) {
        int idx = findIndexById(id);
        if (idx == -1) return false;
        sessions.remove(idx);
        return true;
    }

    // extra: removes all with same date
    public int deleteByDate(String date) {
        int count = 0;
        for (int i = sessions.size() - 1; i >= 0; i--) {
            if (sessions.get(i).date.equals(date)) { sessions.remove(i); count++; }
        }
        return count;
    }

    // Custom: best est. 1RM in the last 7 days for an exercise
    public double bestE1RMInLast7Days(String exercise, String todayYYYYMMDD) {
        LocalDate today;
        try { today = LocalDate.parse(todayYYYYMMDD); }
        catch (DateTimeParseException e) { return 0.0; }

        double best = 0.0;
        for (WorkoutSession s : sessions) {
            if (!s.exerciseName.equalsIgnoreCase(exercise)) continue;
            try {
                LocalDate d = LocalDate.parse(s.date);
                if (!d.isBefore(today.minusDays(7)) && !d.isAfter(today)) {
                    double x = s.estimate1RM();
                    if (x > best) best = x;
                }
            } catch (DateTimeParseException ignore) { }
        }
        return best;
    }

    // CSV load/save
    public int loadCsv(File f) {
        int loaded = 0;
        sessions.clear();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(f));
            String line = br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] t = line.split(",", -1);
                if (t.length < 10) continue;

                WorkoutSession s = new WorkoutSession();
                s.id = toInt(t[0]);
                s.date = t[1];
                s.exerciseName = t[2];
                s.muscleGroup = t[3];
                s.sets = toInt(t[4]);
                s.reps = toInt(t[5]);
                s.weightLbs = toDouble(t[6]);
                s.durationMin = toInt(t[7]);
                s.rpe = toInt(t[8]);
                s.notes = t[9];

                if (s.isValid() && findIndexById(s.id) == -1) { sessions.add(s); loaded++; }
            }
        } catch (Exception e) {
            System.out.println("Error reading file.");
        } finally {
            try { if (br != null) br.close(); } catch (Exception ignore) {}
        }
        return loaded;
    }

    public int saveCsv(File f) {
        int count = 0;
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(f));
            bw.write("id,date,exerciseName,muscleGroup,sets,reps,weightLbs,durationMin,rpe,notes");
            bw.newLine();
            for (WorkoutSession s : sessions) {
                bw.write(s.toCsvRow());
                bw.newLine();
                count++;
            }
        } catch (Exception e) {
            System.out.println("Error writing file.");
        } finally {
            try { if (bw != null) bw.close(); } catch (Exception ignore) {}
        }
        return count;
    }

    // --- small helpers for CLI ---
    public boolean existsId(int id) {
        return findIndexById(id) != -1;
    }

    // simple next available positive ID
    public int nextId() {
        int max = 0;
        for (WorkoutSession s : sessions) if (s.id > max) max = s.id;
        return Math.max(1, max + 1);
    }

    // private helpers
    private int findIndexById(int id) {
        for (int i = 0; i < sessions.size(); i++) if (sessions.get(i).id == id) return i;
        return -1;
    }
    private int toInt(String x) { try { return Integer.parseInt(x.trim()); } catch (Exception e) { return -1; } }
    private double toDouble(String x) { try { return Double.parseDouble(x.trim()); } catch (Exception e) { return -1; } }
    private WorkoutSession copy(WorkoutSession s) {
        WorkoutSession c = new WorkoutSession();
        c.id = s.id; c.date = s.date; c.exerciseName = s.exerciseName; c.muscleGroup = s.muscleGroup;
        c.sets = s.sets; c.reps = s.reps; c.weightLbs = s.weightLbs; c.durationMin = s.durationMin;
        c.rpe = s.rpe; c.notes = s.notes;
        return c;
    }
}
