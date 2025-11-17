package gym;

// Pedro Salcedo
// CEN 3024 - Phase 3
// Handles the workout list in memory (add, delete, update, load file)

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class SessionService {

    // in-memory list of workouts
    private final ArrayList<WorkoutSession> sessions = new ArrayList<>();

    // send whole list back to the GUI
    public ArrayList<WorkoutSession> listAll() {
        return sessions;
    }

    // check if an id is already in use
    public boolean existsId(int id) {
        return findIndexById(id) != -1;
    }

    // add a new workout if id is not repeated
    public boolean add(WorkoutSession s) {
        if (s == null) return false;
        if (existsId(s.id)) return false;
        sessions.add(s);
        return true;
    }

    // delete one workout based on id
    public boolean deleteById(int id) {
        int index = findIndexById(id);
        if (index == -1) return false;
        sessions.remove(index);
        return true;
    }

    // replace a workout with the same id (used by the Update button)
    public boolean updateFromFull(WorkoutSession updated) {
        int index = findIndexById(updated.id);
        if (index == -1) return false;

        sessions.set(index, updated);
        return true;
    }

    // find the best estimated 1RM in the last 7 days for an exercise
    public double bestE1RMInLast7Days(String exercise, String today) {
        LocalDate now;
        try { now = LocalDate.parse(today); }
        catch (Exception e) { return 0; }

        double best = 0;

        for (WorkoutSession s : sessions) {
            if (!s.exerciseName.equalsIgnoreCase(exercise)) continue;

            try {
                LocalDate d = LocalDate.parse(s.date);
                if (!d.isBefore(now.minusDays(7)) && !d.isAfter(now)) {
                    double est = s.weightLbs * (1 + s.reps / 30.0);
                    if (est > best) best = est;
                }
            } catch (Exception ignore) {}
        }

        return best;
    }

    // load workouts from a CSV file
    public int loadCsv(File f) {
        int count = 0;
        sessions.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
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

                if (!existsId(s.id)) {
                    sessions.add(s);
                    count++;
                }
            }
        } catch (Exception e) {
            System.out.println("File read error.");
        }

        return count;
    }

    // find index of a workout by id
    private int findIndexById(int id) {
        for (int i = 0; i < sessions.size(); i++) {
            if (sessions.get(i).id == id) return i;
        }
        return -1;
    }

    // safe int parse
    private int toInt(String x) {
        try { return Integer.parseInt(x.trim()); } catch (Exception e) { return 0; }
    }

    // safe double parse
    private double toDouble(String x) {
        try { return Double.parseDouble(x.trim()); } catch (Exception e) { return 0; }
    }
}
