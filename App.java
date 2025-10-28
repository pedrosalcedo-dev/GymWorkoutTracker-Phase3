// Pedro Salcedo
// CEN 3024 - Software Development 1
// App: simple CLI menu for the tracker (no DB, no GUI).

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        SessionService service = new SessionService();
        Scanner in = new Scanner(System.in);
        boolean running = true;

        while (running) {
            int pick = menu(in);

            if (pick == 1) {
                // ADD: auto-ID + early duplicate check
                System.out.print("id (leave blank for auto): ");
                String idRaw = in.nextLine().trim();
                int id;
                if (idRaw.isEmpty()) {
                    id = service.nextId();
                    System.out.println("Assigned id: " + id);
                } else {
                    id = toInt(idRaw);
                    if (id <= 0) { System.out.println("Invalid id."); continue; }
                    if (service.existsId(id)) {
                        System.out.println("ID already in use. Try another or leave blank for auto.");
                        continue;
                    }
                }

                WorkoutSession s = readNewWithGivenId(in, id);
                if (s == null) System.out.println("Invalid input.");
                else if (service.add(s)) System.out.println("Saved.");
                else System.out.println("Error saving. Check fields.");
            }
            else if (pick == 2) {
                printList(service.listAll());
            }
            else if (pick == 3) {
                System.out.print("Search by [date|exercise|group]: ");
                String key = in.nextLine().toLowerCase();
                if ("date".equals(key)) {
                    System.out.print("date: ");
                    printList(service.findByDate(in.nextLine()));
                } else if ("exercise".equals(key)) {
                    System.out.print("exercise: ");
                    printList(service.findByExercise(in.nextLine()));
                } else if ("group".equals(key)) {
                    System.out.print("group: ");
                    printList(service.findByGroup(in.nextLine()));
                } else {
                    System.out.println("Invalid option.");
                }
            }
            else if (pick == 4) {
                // UPDATE: short-circuit if ID not found
                System.out.print("id: ");
                int id = toInt(in.nextLine());
                if (!service.existsId(id)) {
                    System.out.println("No session with that ID. Update cancelled.");
                    continue;
                }
                System.out.print("field (date|exercise|group|sets|reps|weight|duration|rpe|notes): ");
                String field = in.nextLine();
                System.out.print("new value: ");
                String value = in.nextLine();
                boolean ok = service.updateById(id, field, value);
                System.out.println(ok ? "Updated." : "Update failed. Check value/field.");
            }
            else if (pick == 5) {
                System.out.print("id: ");
                boolean ok = service.deleteById(toInt(in.nextLine()));
                System.out.println(ok ? "Deleted." : "Not found.");
            }
            else if (pick == 6) {
                System.out.print("exercise: ");
                String ex = in.nextLine();
                System.out.print("today (YYYY-MM-DD): ");
                String today = in.nextLine();
                double best = service.bestE1RMInLast7Days(ex, today);
                if (best > 0) System.out.println("Best est. 1RM (7 days): " + best + " lbs");
                else System.out.println("No data in range.");
            }
            else if (pick == 7) {
                System.out.print("CSV path to load: ");
                int n = service.loadCsv(new File(in.nextLine()));
                System.out.println("Loaded: " + n);
            }
            else if (pick == 8) {
                System.out.print("CSV path to save: ");
                int n = service.saveCsv(new File(in.nextLine()));
                System.out.println("Saved rows: " + n);
            }
            else if (pick == 9) {
                running = false;
            }
            else {
                System.out.println("Invalid input.");
            }
        }
    }

    // menu and small console helpers
    private static int menu(Scanner in) {
        System.out.println("\n--- Gym Workout Tracker ---");
        System.out.println("1) Add session");
        System.out.println("2) List all");
        System.out.println("3) Search (date|exercise|group)");
        System.out.println("4) Update by id");
        System.out.println("5) Delete by id");
        System.out.println("6) Estimate 1RM (best 7-day)");
        System.out.println("7) Load CSV");
        System.out.println("8) Save CSV");
        System.out.println("9) Exit");
        System.out.print("Pick: ");
        try { return Integer.parseInt(in.nextLine()); } catch (Exception e) { return -1; }
    }

    private static WorkoutSession readNewWithGivenId(Scanner in, int id) {
        WorkoutSession s = new WorkoutSession();
        try {
            s.id = id;
            System.out.print("date (YYYY-MM-DD): "); s.date = in.nextLine();
            System.out.print("exercise: "); s.exerciseName = in.nextLine();
            System.out.print("muscle group: "); s.muscleGroup = in.nextLine();
            System.out.print("sets: "); s.sets = Integer.parseInt(in.nextLine());
            System.out.print("reps: "); s.reps = Integer.parseInt(in.nextLine());
            System.out.print("weight (lbs): "); s.weightLbs = Double.parseDouble(in.nextLine());
            System.out.print("duration (min): "); s.durationMin = Integer.parseInt(in.nextLine());
            System.out.print("rpe (1-10): "); s.rpe = Integer.parseInt(in.nextLine());
            System.out.print("notes: "); s.notes = in.nextLine();
        } catch (Exception e) { return null; }
        return s;
    }

    private static void printList(ArrayList<WorkoutSession> list) {
        if (list == null || list.isEmpty()) { System.out.println("(none)"); return; }
        for (WorkoutSession s : list) {
            System.out.println(s.id + " | " + s.date + " | " + s.exerciseName + " | " +
                    s.muscleGroup + " | sets=" + s.sets + " reps=" + s.reps +
                    " wt=" + s.weightLbs + " rpe=" + s.rpe + " dur=" + s.durationMin +
                    " | " + (s.notes == null ? "" : s.notes));
        }
    }

    private static int toInt(String x) {
        try { return Integer.parseInt(x.trim()); } catch (Exception e) { return -1; }
    }
}
