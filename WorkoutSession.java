// Pedro Salcedo
// CEN 3024 - Software Development 1
// WorkoutSession: one workout record for the tracker

public class WorkoutSession {
    // keeping fields public for simple console use
    public int id;
    public String date;          // YYYY-MM-DD
    public String exerciseName;
    public String muscleGroup;
    public int sets;
    public int reps;
    public double weightLbs;
    public int durationMin;
    public int rpe;              // effort 1–10
    public String notes;

    public WorkoutSession() { }

    // basic checks
    public boolean isValid() {
        if (id <= 0) return false;
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) return false;
        if (exerciseName == null || exerciseName.trim().isEmpty()) return false;
        if (muscleGroup == null || muscleGroup.trim().isEmpty()) return false;
        if (sets <= 0) return false;
        if (reps <= 0 || reps > 50) return false;
        if (weightLbs <= 0 || weightLbs > 1000) return false;
        if (durationMin < 0) return false;
        if (rpe < 1 || rpe > 10) return false;
        return true;
    }

    // 1RM estimate
    public double estimate1RM() {
        return weightLbs * (1.0 + (reps / 30.0));
    }

    // simple CSV line (no commas inside fields)
    public String toCsvRow() {
        return id + "," + date + "," + clean(exerciseName) + "," + clean(muscleGroup) + ","
                + sets + "," + reps + "," + weightLbs + "," + durationMin + "," + rpe + ","
                + clean(notes);
    }

    private String clean(String s) {
        if (s == null) return "";
        return s.replace(",", " ");
    }
}
