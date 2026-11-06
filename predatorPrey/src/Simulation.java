
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

public class Simulation {

    private static final int RENDER_INTERVAL_MS = 33;
    private static final int TICKS_PER_LOGIC_UPDATE = 9;
    private static final int GRASS_SIZE = 20;
    private static final int GRASS_GROWTH_TIME = 1000;

    // Limits to prevent excessive population growth
    private static final int MAX_STARTING_PREDATORS = 30;
    private static final int MAX_STARTING_PREY = 100;
    private static final int MAX_POPULATION = 200;

    private int frameCounter = 0;
    private final Random random = new Random();

    private final List<Creature> sim;
    private final List<Grass> grassList;

    // Starting window and controls
    private JFrame simStart;
    private JPanel panel;

    private JButton begin;
    private JButton savePred;
    private JButton savePrey;

    private JLabel pred;
    private JLabel prey;
    private JLabel warning;

    private JTextField predAmount;
    private JTextField preyAmount;

    private JPanel row1;
    private JPanel row2;
    private JPanel row3;
    private JPanel row4;

    private Dimension rowSize;

    // Prevent duplicate starting populations
    private boolean predatorsSaved = false;
    private boolean preySaved = false;

    // Running simulation window
    private JFrame runningSim;
    private SimPanel simPanel;
    private Timer gameTimer;

    // Individual improvement: live ecosystem statistics
    private JLabel statsLabel;

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            List<Creature> creatures = new ArrayList<>();

            Simulation mainSim = new Simulation(creatures);
            mainSim.frameInitialise();
        });
    }

    public Simulation(List<Creature> sim) {

        this.sim = sim;
        this.grassList = new ArrayList<>();
    }

    public void frameInitialise() {

        simStart = new JFrame("Predator, Prey Simulation");

        simStart.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        simStart.setSize(800, 700);
        simStart.setLocationRelativeTo(null);
        simStart.setResizable(false);

        panel = new JPanel();

        warning = new JLabel(
                "Enter both populations and click Save."
        );

        begin = new JButton("Start");

        // Start only after both populations have been saved
        begin.setEnabled(false);

        pred = new JLabel("Starting predator amount");
        prey = new JLabel("Starting prey amount");

        predAmount = new JTextField(10);
        preyAmount = new JTextField(10);

        savePred = new JButton("Save");
        savePrey = new JButton("Save");

        row1 = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 0)
        );

        row2 = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 0)
        );

        row3 = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 0)
        );

        row4 = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 0)
        );

        rowSize = new Dimension(800, 80);

        // Save starting predator population
        savePred.addActionListener(e -> {

            if (predatorsSaved) {
                return;
            }

            int input;

            try {

                input = Integer.parseInt(
                        predAmount.getText().trim()
                );

            } catch (NumberFormatException ex) {

                warning.setText(
                        "Enter a valid whole number for predators."
                );

                return;
            }

            if (input < 0 || input > MAX_STARTING_PREDATORS) {

                warning.setText(
                        "Predators must be between 0 and "
                        + MAX_STARTING_PREDATORS + "."
                );

                return;
            }

            for (int i = 0; i < input; i++) {

                int x = random.nextInt(
                        Creature.WORLD_WIDTH - Creature.SIZE + 1
                );

                int y = random.nextInt(
                        Creature.WORLD_HEIGHT - Creature.SIZE + 1
                );

                sim.add(
                        new Predator(5, 0, x, y, sim)
                );
            }

            predatorsSaved = true;

            // Stop the user from adding the population again
            savePred.setEnabled(false);
            predAmount.setEditable(false);

            warning.setText(
                    "Saved " + input + " predators."
            );

            updateStartButton();
        });

        // Save starting prey population
        savePrey.addActionListener(e -> {

            if (preySaved) {
                return;
            }

            int input;

            try {

                input = Integer.parseInt(
                        preyAmount.getText().trim()
                );

            } catch (NumberFormatException ex) {

                warning.setText(
                        "Enter a valid whole number for prey."
                );

                return;
            }

            if (input < 0 || input > MAX_STARTING_PREY) {

                warning.setText(
                        "Prey must be between 0 and "
                        + MAX_STARTING_PREY + "."
                );

                return;
            }

            for (int i = 0; i < input; i++) {

                int x = random.nextInt(
                        Creature.WORLD_WIDTH - Creature.SIZE + 1
                );

                int y = random.nextInt(
                        Creature.WORLD_HEIGHT - Creature.SIZE + 1
                );

                sim.add(
                        new Prey(
                                5,
                                10,
                                x,
                                y,
                                sim,
                                grassList
                        )
                );
            }

            preySaved = true;

            // Prevent duplicate saves
            savePrey.setEnabled(false);
            preyAmount.setEditable(false);

            warning.setText(
                    "Saved " + input + " prey."
            );

            updateStartButton();
        });

        // Start the running simulation
        begin.addActionListener(e -> {

            if (!predatorsSaved || !preySaved) {

                warning.setText(
                        "Save both populations before starting."
                );

                return;
            }

            begin.setEnabled(false);

            spawnGrass();

            runningSim = new JFrame(
                    "Predator, Prey Simulation - Running"
            );

            runningSim.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            simPanel = new SimPanel();

            simPanel.setPreferredSize(
                    new Dimension(
                            Creature.WORLD_WIDTH,
                            Creature.WORLD_HEIGHT
                    )
            );

            simPanel.setBackground(
                    new Color(18, 84, 13)
            );

            runningSim.add(
                    simPanel,
                    BorderLayout.CENTER
            );

            // Live population statistics
            statsLabel = new JLabel();

            statsLabel.setOpaque(true);

            statsLabel.setBackground(
                    new Color(24, 55, 30)
            );

            statsLabel.setForeground(Color.WHITE);

            statsLabel.setFont(
                    statsLabel.getFont().deriveFont(15f)
            );

            statsLabel.setBorder(
                    BorderFactory.createEmptyBorder(
                            10, 15, 10, 15
                    )
            );

            runningSim.add(
                    statsLabel,
                    BorderLayout.SOUTH
            );

            // Show starting population counts
            updateStatistics();

            runningSim.setResizable(false);
            runningSim.pack();
            runningSim.setLocationRelativeTo(null);

            simStart.dispose();
            runningSim.setVisible(true);

            startGameLoop();
        });

        // Build the starting window
        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        80, 20, 20, 20
                )
        );

        row1.add(begin);
        row1.setMaximumSize(rowSize);

        row2.add(warning);
        row2.setMaximumSize(rowSize);

        row3.add(pred);
        row3.add(predAmount);
        row3.add(savePred);
        row3.setMaximumSize(rowSize);

        row4.add(prey);
        row4.add(preyAmount);
        row4.add(savePrey);
        row4.setMaximumSize(rowSize);

        panel.add(row1);
        panel.add(row2);
        panel.add(row3);
        panel.add(row4);

        simStart.add(panel, BorderLayout.CENTER);

        simStart.setVisible(true);
    }

    // Enable Start only when both populations are saved
    private void updateStartButton() {

        begin.setEnabled(
                predatorsSaved && preySaved
        );
    }

    // Create grass patches throughout the world
    private void spawnGrass() {

        for (int i = 0;
             i < Creature.WORLD_WIDTH;
             i += GRASS_SIZE) {

            for (int j = 0;
                 j < Creature.WORLD_HEIGHT;
                 j += GRASS_SIZE) {

                int randomGrowth = random.nextInt(
                        GRASS_GROWTH_TIME
                );

                int randomWeight = random.nextInt(20);

                boolean isEaten = randomWeight >= 1;

                grassList.add(
                        new Grass(
                                i,
                                j,
                                randomGrowth,
                                isEaten
                        )
                );
            }
        }
    }

    // Start the simulation timer
    private void startGameLoop() {

        gameTimer = new Timer(
                RENDER_INTERVAL_MS,
                e -> {

                    // Snapshot prevents collection modification
                    // errors while predators eat prey
                    List<Creature> snapshot =
                            new ArrayList<>(sim);

                    for (Creature creature : snapshot) {

                        // Skip creatures already eaten
                        if (!sim.contains(creature)) {
                            continue;
                        }

                        creature.movement();
                        creature.eat();
                    }

                    frameCounter++;

                    if (frameCounter >= TICKS_PER_LOGIC_UPDATE) {

                        runSimulationTick();

                        // Refresh population statistics
                        updateStatistics();

                        frameCounter = 0;
                    }

                    repaint();
                }
        );

        gameTimer.start();
    }

    // Handle starvation, reproduction and grass regrowth
    public void runSimulationTick() {

        List<Creature> snapshot = new ArrayList<>(sim);

        List<Creature> toRemove = new ArrayList<>();

        // Remove creatures that starve
        for (Creature creature : snapshot) {

            if (creature.starve()) {
                toRemove.add(creature);
            }
        }

        sim.removeAll(toRemove);

        // Use a snapshot because reproduction adds creatures
        List<Creature> reproductionSnapshot =
                new ArrayList<>(sim);

        for (Creature creature : reproductionSnapshot) {

            // Prevent unlimited population growth
            if (sim.size() >= MAX_POPULATION) {
                break;
            }

            creature.reproduce();
        }

        // Allow eaten grass to grow again
        for (Grass grass : grassList) {

            grass.tickGrowth();
        }
    }

    // Individual improvement:
    // Java Streams and Collectors for live statistics
    private void updateStatistics() {

        Map<String, Long> populationCounts =
                sim.stream()
                        .collect(
                                Collectors.groupingBy(
                                        creature ->
                                                creature instanceof Predator
                                                        ? "Predator"
                                                        : "Prey",
                                        Collectors.counting()
                                )
                        );

        long predatorCount =
                populationCounts.getOrDefault(
                        "Predator",
                        0L
                );

        long preyCount =
                populationCounts.getOrDefault(
                        "Prey",
                        0L
                );

        // Count edible grass using a Stream
        long edibleGrassCount =
                grassList.stream()
                        .filter(Grass::isEdible)
                        .count();

        if (statsLabel != null) {

            statsLabel.setText(
                    "Predators: " + predatorCount
                    + "       |       Prey: " + preyCount
                    + "       |       Edible grass: "
                    + edibleGrassCount
            );
        }
    }

    // Refresh the simulation display
    public void repaint() {

        if (simPanel != null) {
            simPanel.repaint();
        }
    }

    // Draw grass, predators and prey
    private class SimPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            // Draw edible grass
            g.setColor(new Color(80, 200, 80));

            for (Grass grass : grassList) {

                if (grass.isEdible()) {

                    g.fillRect(
                            grass.getX(),
                            grass.getY(),
                            GRASS_SIZE,
                            GRASS_SIZE
                    );
                }
            }

            // Draw predators in red and prey in blue
            for (Creature creature : new ArrayList<>(sim)) {

                if (creature instanceof Predator) {

                    g.setColor(Color.RED);

                } else {

                    g.setColor(Color.BLUE);
                }

                g.fillOval(
                        creature.getX(),
                        creature.getY(),
                        Creature.SIZE,
                        Creature.SIZE
                );
            }
        }
    }
}
