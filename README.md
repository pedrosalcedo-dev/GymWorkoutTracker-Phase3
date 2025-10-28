# Gym Workout Tracker (Phase 3 - GUI)
**Pedro Salcedo**  
CEN 3024 - Software Development I  

This is my Phase 3 version of the **Gym Workout Tracker**, now upgraded with a full **Java Swing GUI**.  
It lets users add, update, delete, and display workout sessions, load data from a CSV file, and calculate a **1RM (One Rep Max)** and **total volume** for a selected exercise.

---

## How to Run
- Requires **Java 17 or higher**
- Download the `.jar` file located in the **jar/** folder  
- Double-click the JAR file or run this in a terminal:
  ```
  java -jar GymWorkoutTracker.jar
  ```

---

## Features
- Add, update, delete, and display workout sessions  
- Load workout data from a CSV file  
- Input validation for clean data handling  
- Custom “1RM + Volume” feature that calculates both values for the selected exercise  
- Readable messages for user guidance and errors  

---

## Data
Example file:  
```
/data/workouts.csv
```

This CSV file holds all the saved workouts, which load directly into the GUI when selected.

