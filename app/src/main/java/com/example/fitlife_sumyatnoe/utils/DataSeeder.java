package com.example.fitlife_sumyatnoe.utils;

import com.example.fitlife_sumyatnoe.models.Equipment;
import com.example.fitlife_sumyatnoe.models.Exercise;
import com.example.fitlife_sumyatnoe.models.Workout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSeeder {

    private FirebaseFirestore db;

    public DataSeeder() {
        db = FirebaseFirestore.getInstance();
    }

    public void uploadAllData() {
        uploadDefaultExercises();
        uploadDefaultEquipment();
        uploadDefaultWorkouts();
    }

    // ========== 20+ DEFAULT EXERCISES ==========

    private void uploadDefaultExercises() {
        List<Exercise> exercises = getDefaultExercises();

        for (Exercise exercise : exercises) {
            String docId = exercise.getName().toLowerCase().replace(" ", "_").replace("-", "_");
            db.collection("default_exercises")
                    .document(docId)
                    .set(exercise)
                    .addOnSuccessListener(aVoid ->
                            System.out.println("Uploaded exercise: " + exercise.getName()))
                    .addOnFailureListener(e ->
                            System.err.println("Failed to upload: " + exercise.getName()));
        }
    }

    private List<Exercise> getDefaultExercises() {
        List<Exercise> exercises = new ArrayList<>();

        // Strength Exercises (10)
        Exercise pushups = new Exercise("Push-ups", 3, 12);
        pushups.setInstructions("Start in plank position, lower chest to ground, push back up. Keep back straight.");
        pushups.setCategory("strength");
        pushups.setImageUrl("exercise_pushups");
        exercises.add(pushups);

        Exercise squats = new Exercise("Squats", 3, 15);
        squats.setInstructions("Stand with feet shoulder-width apart, lower hips as if sitting in chair, keep chest up.");
        squats.setCategory("strength");
        squats.setImageUrl("exercise_squats");
        exercises.add(squats);

        Exercise pullups = new Exercise("Pull-ups", 3, 8);
        pullups.setInstructions("Grab bar palms facing away, pull chest to bar, lower with control.");
        pullups.setCategory("strength");
        pullups.setImageUrl("exercise_pullups");
        exercises.add(pullups);

        Exercise lunges = new Exercise("Lunges", 3, 10);
        lunges.setInstructions("Step forward, lower hips until both knees at 90°, push back up. Alternate legs.");
        lunges.setCategory("strength");
        lunges.setImageUrl("exercise_lunges");
        exercises.add(lunges);

        Exercise benchPress = new Exercise("Bench Press", 3, 10);
        benchPress.setInstructions("Lie on bench, lower barbell to chest, press up. Keep shoulders stable.");
        benchPress.setCategory("strength");
        benchPress.setImageUrl("exercise_bench_press");
        exercises.add(benchPress);

        Exercise deadlifts = new Exercise("Deadlifts", 3, 8);
        deadlifts.setInstructions("Hinge at hips, keep back straight, lift barbell, squeeze glutes at top.");
        deadlifts.setCategory("strength");
        deadlifts.setImageUrl("exercise_deadlifts");
        exercises.add(deadlifts);

        Exercise shoulderPress = new Exercise("Shoulder Press", 3, 10);
        shoulderPress.setInstructions("Press dumbbells overhead, keep core tight, avoid arching back.");
        shoulderPress.setCategory("strength");
        shoulderPress.setImageUrl("exercise_shoulder_press");
        exercises.add(shoulderPress);

        Exercise rows = new Exercise("Bent-over Rows", 3, 10);
        rows.setInstructions("Hinge forward, pull dumbbells to chest, squeeze shoulder blades.");
        rows.setCategory("strength");
        rows.setImageUrl("exercise_rows");
        exercises.add(rows);

        Exercise bicepCurls = new Exercise("Bicep Curls", 3, 12);
        bicepCurls.setInstructions("Keep elbows pinned to sides, curl weights up, lower with control.");
        bicepCurls.setCategory("strength");
        bicepCurls.setImageUrl("exercise_bicep_curls");
        exercises.add(bicepCurls);

        Exercise tricepDips = new Exercise("Tricep Dips", 3, 10);
        tricepDips.setInstructions("Support body on bench, lower body, press up using triceps.");
        tricepDips.setCategory("strength");
        tricepDips.setImageUrl("exercise_tricep_dips");
        exercises.add(tricepDips);

        // Cardio Exercises (5)
        Exercise jumpingJacks = new Exercise("Jumping Jacks", 4, 45, true);
        jumpingJacks.setInstructions("Jump feet out while raising arms overhead, jump back to start.");
        jumpingJacks.setCategory("cardio");
        exercises.add(jumpingJacks);
        jumpingJacks.setImageUrl("exercise_jumping_jacks");

        Exercise burpees = new Exercise("Burpees", 4, 45, true);
        burpees.setInstructions("Drop to squat, kick feet back, push-up, jump forward, jump up.");
        burpees.setCategory("cardio");
        burpees.setImageUrl("exercise_burpees");
        exercises.add(burpees);

        Exercise mountainClimbers = new Exercise("Mountain Climbers", 4, 45, true);
        mountainClimbers.setInstructions("Start in plank, drive knees toward chest alternately, keep core tight.");
        mountainClimbers.setCategory("cardio");
        mountainClimbers.setImageUrl("exercise_mountain_climbers");
        exercises.add(mountainClimbers);

        Exercise highKnees = new Exercise("High Knees", 4, 45, true);
        highKnees.setInstructions("Run in place, drive knees up to waist height, pump arms.");
        highKnees.setCategory("cardio");
        highKnees.setImageUrl("exercise_high_knees");
        exercises.add(highKnees);

        Exercise jumpSquats = new Exercise("Jump Squats", 3, 12);
        jumpSquats.setInstructions("Perform squat, explode up into jump, land softly.");
        jumpSquats.setCategory("cardio");
        jumpSquats.setImageUrl("exercise_jump_squats");
        exercises.add(jumpSquats);

        // Core Exercises (5)
        Exercise plank = new Exercise("Plank", 3, 60, true);
        plank.setInstructions("Hold body straight from head to heels, engage core and glutes.");
        plank.setCategory("core");
        exercises.add(plank);
        plank.setImageUrl("exercise_plank");

        Exercise crunches = new Exercise("Crunches", 3, 15);
        crunches.setInstructions("Lie on back, curl shoulders off floor, crunch abs.");
        crunches.setCategory("core");
        crunches.setImageUrl("exercise_crunches");
        exercises.add(crunches);

        Exercise legRaises = new Exercise("Leg Raises", 3, 12);
        legRaises.setInstructions("Lie on back, lift legs straight up, lower with control.");
        legRaises.setCategory("core");
        legRaises.setImageUrl("exercise_leg_raises");
        exercises.add(legRaises);

        Exercise russianTwists = new Exercise("Russian Twists", 3, 10);
        russianTwists.setInstructions("Sit with feet off ground, twist torso side to side.");
        russianTwists.setCategory("core");
        russianTwists.setImageUrl("exercise_russian_twists");
        exercises.add(russianTwists);

        Exercise bicycleCrunches = new Exercise("Bicycle Crunches", 3, 12);
        bicycleCrunches.setInstructions("Alternate bringing elbow to opposite knee, twist core.");
        bicycleCrunches.setCategory("core");
        bicycleCrunches.setImageUrl("exercise_bicycle_crunches" );
        exercises.add(bicycleCrunches);

        // Yoga Exercises (5)
        Exercise downwardDog = new Exercise("Downward Dog", 2, 60, true);
        downwardDog.setInstructions("Start on hands and knees, lift hips up and back, form inverted V.");
        downwardDog.setCategory("yoga");
        downwardDog.setImageUrl("exercise_downward_dog");
        exercises.add(downwardDog);

        Exercise catCow = new Exercise("Cat-Cow Stretch", 2, 60, true);
        catCow.setInstructions("On hands and knees, alternate arching back (cat) and dipping (cow).");
        catCow.setCategory("yoga");
        catCow.setImageUrl("exercise_cat_cow");
        exercises.add(catCow);

        Exercise warrior1 = new Exercise("Warrior I", 2, 45, true);
        warrior1.setInstructions("Lunge forward, back foot flat, arms reach up, square hips.");
        warrior1.setCategory("yoga");
        warrior1.setImageUrl("exercise_warrior1");
        exercises.add(warrior1);

        Exercise warrior2 = new Exercise("Warrior II", 2, 45, true);
        warrior2.setInstructions("Open hips to side, arms extended, gaze over front hand.");
        warrior2.setCategory("yoga");
        warrior2.setImageUrl("exercise_warrior2");
        exercises.add(warrior2);

        Exercise treePose = new Exercise("Tree Pose", 2, 45, true);
        treePose.setInstructions("Stand on one leg, place other foot on inner thigh, hands at heart.");
        treePose.setCategory("yoga");
        treePose.setImageUrl("exercise_tree_pose");
        exercises.add(treePose);

        return exercises;
    }

    // ========== 15+ DEFAULT EQUIPMENT ==========

    private void uploadDefaultEquipment() {
        List<Equipment> equipment = getDefaultEquipment();

        for (Equipment item : equipment) {
            String docId = item.getName().toLowerCase().replace(" ", "_").replace("-", "_");
            db.collection("default_equipment")
                    .document(docId)
                    .set(item)
                    .addOnSuccessListener(aVoid ->
                            System.out.println("Uploaded equipment: " + item.getName()))
                    .addOnFailureListener(e ->
                            System.err.println("Failed to upload: " + item.getName()));
        }
    }
    public void updateExistingWorkoutsWithExerciseImages() {
        // First, get all exercises with their images
        Map<String, String> exerciseImageMap = new HashMap<>();

        db.collection("default_exercises")
                .get()
                .addOnSuccessListener(exerciseDocs -> {
                    for (DocumentSnapshot exDoc : exerciseDocs) {
                        String exerciseName = exDoc.getString("name");
                        String imageUrl = exDoc.getString("imageUrl");
                        if (exerciseName != null && imageUrl != null) {
                            exerciseImageMap.put(exerciseName, imageUrl);
                        }
                    }

                    // Now update all workouts
                    db.collection("workout_templates")
                            .get()
                            .addOnSuccessListener(workoutDocs -> {
                                for (DocumentSnapshot workoutDoc : workoutDocs) {
                                    List<Map<String, Object>> exercises = (List<Map<String, Object>>) workoutDoc.get("exercises");
                                    if (exercises != null) {
                                        boolean needsUpdate = false;
                                        for (Map<String, Object> exercise : exercises) {
                                            String name = (String) exercise.get("name");
                                            if (name != null && exerciseImageMap.containsKey(name)) {
                                                String currentImage = (String) exercise.get("imageUrl");
                                                if (currentImage == null || currentImage.isEmpty()) {
                                                    exercise.put("imageUrl", exerciseImageMap.get(name));
                                                    needsUpdate = true;
                                                }
                                            }
                                        }

                                        if (needsUpdate) {
                                            workoutDoc.getReference().update("exercises", exercises)
                                                    .addOnSuccessListener(aVoid ->
                                                            System.out.println("Updated workout: " + workoutDoc.getId()))
                                                    .addOnFailureListener(e ->
                                                            System.err.println("Failed: " + workoutDoc.getId()));
                                        }
                                    }
                                }
                            });
                });
    }
    private List<Equipment> getDefaultEquipment() {
        List<Equipment> equipment = new ArrayList<>();

        // Strength Equipment
        equipment.add(new Equipment("Dumbbells", "strength"));
        equipment.add(new Equipment("Barbell", "strength"));
        equipment.add(new Equipment("Kettlebell", "strength"));
        equipment.add(new Equipment("Pull-up Bar", "strength"));
        equipment.add(new Equipment("Weight Plates", "strength"));
        equipment.add(new Equipment("Bench", "strength"));
        equipment.add(new Equipment("Squat Rack", "strength"));
        equipment.add(new Equipment("Resistance Bands", "strength"));

        // Cardio Equipment
        equipment.add(new Equipment("Jump Rope", "cardio"));
        equipment.add(new Equipment("Treadmill", "cardio"));
        equipment.add(new Equipment("Exercise Bike", "cardio"));
        equipment.add(new Equipment("Rowing Machine", "cardio"));

        // Yoga Equipment
        equipment.add(new Equipment("Yoga Mat", "yoga"));
        equipment.add(new Equipment("Yoga Block", "yoga"));
        equipment.add(new Equipment("Yoga Strap", "yoga"));
        equipment.add(new Equipment("Meditation Cushion", "yoga"));

        // Accessories
        equipment.add(new Equipment("Foam Roller", "accessories"));
        equipment.add(new Equipment("Ab Wheel", "accessories"));
        equipment.add(new Equipment("Gym Gloves", "accessories"));
        equipment.add(new Equipment("Water Bottle", "accessories"));
        equipment.add(new Equipment("Towel", "accessories"));

        return equipment;
    }

    // ========== 10+ DEFAULT WORKOUTS ==========

    private void uploadDefaultWorkouts() {
        List<Workout> workouts = getDefaultWorkouts();

        for (Workout workout : workouts) {
            String docId = workout.getName().toLowerCase().replace(" ", "_");
            db.collection("workout_templates")
                    .document(docId)
                    .set(workout)
                    .addOnSuccessListener(aVoid ->
                            System.out.println("Uploaded workout: " + workout.getName()))
                    .addOnFailureListener(e ->
                            System.err.println("Failed to upload: " + workout.getName()));
        }
    }

    private List<Workout> getDefaultWorkouts() {
        List<Workout> workouts = new ArrayList<>();

        // 1. Full Body Strength
        Workout fullBody = new Workout("Full Body Strength", "Complete full body workout for strength building", 45);
        fullBody.setExercises(Arrays.asList(
                createExercise("Squats", 4, 10),
                createExercise("Push-ups", 3, 12),
                createExercise("Pull-ups", 3, 8),
                createExercise("Lunges", 3, 10),
                createExercise("Plank", 3, 60, true)
        ));
        fullBody.setEquipment(Arrays.asList("Dumbbells", "Pull-up Bar", "Yoga Mat"));
        fullBody.setDefaultWorkout(true);
        fullBody.setImageUrl("workout_full_body");
        workouts.add(fullBody);

        // 2. HIIT Cardio
        Workout hiit = new Workout("HIIT Cardio", "High intensity interval training for fat burning", 30);
        hiit.setExercises(Arrays.asList(
                createExercise("Jumping Jacks", 4, 45, true),
                createExercise("Burpees", 4, 45, true),
                createExercise("Mountain Climbers", 4, 45, true),
                createExercise("High Knees", 4, 45, true),
                createExercise("Jump Squats", 3, 12)
        ));
        hiit.setEquipment(Arrays.asList("Yoga Mat", "Jump Rope"));
        hiit.setDefaultWorkout(true);
        hiit.setImageUrl("workout_hiit");
        workouts.add(hiit);

        // 3. Morning Yoga Flow
        Workout yoga = new Workout("Morning Yoga Flow", "Gentle yoga to start your day", 25);
        yoga.setExercises(Arrays.asList(
                createExercise("Cat-Cow Stretch", 2, 60, true),
                createExercise("Downward Dog", 2, 60, true),
                createExercise("Warrior I", 2, 45, true),
                createExercise("Warrior II", 2, 45, true),
                createExercise("Tree Pose", 2, 45, true)
        ));
        yoga.setEquipment(Arrays.asList("Yoga Mat", "Yoga Block"));
        yoga.setDefaultWorkout(true);
        yoga.setImageUrl("workout_yoga");
        workouts.add(yoga);

        // 4. Upper Body Focus
        Workout upperBody = new Workout("Upper Body Focus", "Target chest, back, shoulders, and arms", 40);
        upperBody.setExercises(Arrays.asList(
                createExercise("Bench Press", 3, 10),
                createExercise("Pull-ups", 3, 8),
                createExercise("Shoulder Press", 3, 10),
                createExercise("Bicep Curls", 3, 12),
                createExercise("Tricep Dips", 3, 10)
        ));
        upperBody.setEquipment(Arrays.asList("Barbell", "Dumbbells", "Pull-up Bar"));
        upperBody.setDefaultWorkout(true);
        upperBody.setImageUrl("workout_upper_body");
        workouts.add(upperBody);

        // 5. Lower Body Focus
        Workout lowerBody = new Workout("Lower Body Focus", "Build strong legs and glutes", 40);
        lowerBody.setExercises(Arrays.asList(
                createExercise("Squats", 4, 12),
                createExercise("Deadlifts", 3, 8),
                createExercise("Lunges", 3, 10),
                createExercise("Jump Squats", 3, 12),
                createExercise("Leg Raises", 3, 12)
        ));
        lowerBody.setEquipment(Arrays.asList("Barbell", "Dumbbells"));
        lowerBody.setDefaultWorkout(true);
        lowerBody.setImageUrl("workout_lower_body");
        workouts.add(lowerBody);

        // 6. Core Crusher
        Workout core = new Workout("Core Crusher", "Strengthen your abs and obliques", 25);
        core.setExercises(Arrays.asList(
                createExercise("Plank", 3, 60, true),
                createExercise("Crunches", 3, 15),
                createExercise("Leg Raises", 3, 12),
                createExercise("Russian Twists", 3, 10),
                createExercise("Bicycle Crunches", 3, 12)
        ));
        core.setEquipment(Arrays.asList("Yoga Mat", "Ab Wheel"));
        core.setDefaultWorkout(true);
        core.setImageUrl("workout_core");
        workouts.add(core);

        // 7. Beginner Workout
        Workout beginner = new Workout("Beginner Workout", "Perfect for those new to fitness", 30);
        beginner.setExercises(Arrays.asList(
                createExercise("Push-ups (knees)", 2, 8),
                createExercise("Bodyweight Squats", 2, 12),
                createExercise("Plank", 2, 30, true),
                createExercise("Lunges", 2, 8),
                createExercise("Glute Bridges", 2, 12)
        ));
        beginner.setEquipment(Arrays.asList("Yoga Mat"));
        beginner.setDefaultWorkout(true);
        beginner.setImageUrl("workout_beginner");
        workouts.add(beginner);

        // 8. Advanced Strength
        Workout advanced = new Workout("Advanced Strength", "Challenge yourself with compound movements", 50);
        advanced.setExercises(Arrays.asList(
                createExercise("Deadlifts", 4, 6),
                createExercise("Squats", 4, 8),
                createExercise("Bench Press", 4, 8),
                createExercise("Pull-ups", 4, 6),
                createExercise("Shoulder Press", 3, 8)
        ));
        advanced.setEquipment(Arrays.asList("Barbell", "Dumbbells", "Pull-up Bar"));
        advanced.setDefaultWorkout(true);
        advanced.setImageUrl("workout_advanced");
        workouts.add(advanced);

        // 9. Mobility & Stretching
        Workout mobility = new Workout("Mobility & Stretching", "Improve flexibility and reduce tension", 20);
        mobility.setExercises(Arrays.asList(
                createExercise("Cat-Cow Stretch", 2, 60, true),
                createExercise("Downward Dog", 2, 60, true),
                createExercise("Child's Pose", 2, 60, true),
                createExercise("Pigeon Pose", 2, 60, true),
                createExercise("Seated Forward Fold", 2, 60, true)
        ));
        mobility.setEquipment(Arrays.asList("Yoga Mat"));
        mobility.setDefaultWorkout(true);
        mobility.setImageUrl("workout_mobility");
        workouts.add(mobility);

        // 10. Cardio & Core
        Workout cardioCore = new Workout("Cardio & Core", "Combine cardio and core work", 35);
        cardioCore.setExercises(Arrays.asList(
                createExercise("Burpees", 4, 45, true),
                createExercise("Mountain Climbers", 4, 45, true),
                createExercise("Plank", 3, 60, true),
                createExercise("Russian Twists", 3, 12),
                createExercise("High Knees", 4, 45, true)
        ));
        cardioCore.setEquipment(Arrays.asList("Yoga Mat"));
        cardioCore.setDefaultWorkout(true);
        cardioCore.setImageUrl("workout_cardio_core");
        workouts.add(cardioCore);

        return workouts;
    }
    private Exercise createExercise(String name, int sets, int reps) {
        Exercise exercise = new Exercise(name, sets, reps);
        exercise.setInstructions(getInstructionsForExercise(name));
        exercise.setCategory(getCategoryForExercise(name));
        return exercise;
    }

    private Exercise createExercise(String name, int sets, int duration, boolean isTimed) {
        Exercise exercise = new Exercise(name, sets, duration, true);
        exercise.setInstructions(getInstructionsForExercise(name));
        exercise.setCategory(getCategoryForExercise(name));
        return exercise;
    }

    private String getInstructionsForExercise(String name) {
        switch (name) {
            case "Squats": return "Stand with feet shoulder-width apart, lower hips back and down, keep chest up.";
            case "Push-ups": return "Keep body straight, lower chest to ground, push back up.";
            case "Pull-ups": return "Grab bar palms facing away, pull chest to bar, lower with control.";
            case "Lunges": return "Step forward, lower hips, push back up. Alternate legs.";
            case "Plank": return "Hold body straight from head to heels, engage core.";
            case "Jumping Jacks": return "Jump feet out while raising arms overhead.";
            case "Burpees": return "Drop to squat, kick feet back, push-up, jump up.";
            case "Mountain Climbers": return "Drive knees toward chest alternately in plank position.";
            case "High Knees": return "Run in place, drive knees up to waist height.";
            case "Jump Squats": return "Perform squat, explode up into jump, land softly.";
            case "Crunches": return "Curl shoulders off floor, crunch abs.";
            case "Leg Raises": return "Lift legs straight up, lower with control.";
            case "Russian Twists": return "Twist torso side to side, keep feet off ground.";
            case "Bicycle Crunches": return "Alternate bringing elbow to opposite knee.";
            case "Bench Press": return "Lower barbell to chest, press up.";
            case "Deadlifts": return "Hinge at hips, keep back straight, lift barbell.";
            case "Shoulder Press": return "Press dumbbells overhead, keep core tight.";
            case "Bicep Curls": return "Curl weights up, lower with control.";
            case "Tricep Dips": return "Lower body, press up using triceps.";
            case "Bent-over Rows": return "Pull dumbbells to chest, squeeze shoulder blades.";
            case "Downward Dog": return "Lift hips up and back, form inverted V.";
            case "Cat-Cow Stretch": return "Alternate arching and rounding your back.";
            case "Warrior I": return "Lunge forward, arms reach up, square hips.";
            case "Warrior II": return "Open hips to side, arms extended.";
            case "Tree Pose": return "Stand on one leg, other foot on inner thigh.";
            case "Child's Pose": return "Kneel and fold forward, arms extended.";
            case "Pigeon Pose": return "One leg forward, other leg back, hip stretch.";
            case "Seated Forward Fold": return "Sit with legs extended, fold forward.";
            case "Glute Bridges": return "Lift hips up, squeeze glutes at top.";
            case "Push-ups (knees)": return "Modified push-up with knees on ground.";
            default: return "Maintain proper form throughout the exercise.";
        }
    }

    private String getCategoryForExercise(String name) {
        String[] strength = {"Push-ups", "Squats", "Pull-ups", "Lunges", "Bench Press", "Deadlifts",
                "Shoulder Press", "Bicep Curls", "Tricep Dips", "Rows", "Bent-over Rows"};
        String[] cardio = {"Jumping Jacks", "Burpees", "Mountain Climbers", "High Knees", "Jump Squats"};
        String[] core = {"Plank", "Crunches", "Leg Raises", "Russian Twists", "Bicycle Crunches"};
        String[] yoga = {"Downward Dog", "Cat-Cow Stretch", "Warrior I", "Warrior II", "Tree Pose",
                "Child's Pose", "Pigeon Pose", "Seated Forward Fold"};

        if (Arrays.asList(strength).contains(name)) return "strength";
        if (Arrays.asList(cardio).contains(name)) return "cardio";
        if (Arrays.asList(core).contains(name)) return "core";
        if (Arrays.asList(yoga).contains(name)) return "yoga";
        return "strength";
    }
}