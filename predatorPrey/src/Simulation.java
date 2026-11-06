
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Simulation {

    private static final int RENDER_INTERVAL_MS = 33;
    private static final int TICKS_PER_LOGIC_UPDATE = 9;
    private static final int GRASS_SIZE = 20;
    private static final int GRASS_GROWTH_TIME = 1000;

    private int frameCounter = 0;
    private final Random random = new Random();

    List<Creature> sim;
    List<Grass> grassList;

    // Simulation windows and controls
    private JFrame simStart;
    private JPanel panel;
    private JButton begin;
    private JButton savePred;
    private JButton savePrey;

    private JFrame runningSim;
    private SimPanel simPanel;
    private Timer gameTimer;

    private JLabel pred;
    private JLabel prey;
    private JTextField predAmount;
    private JTextField preyAmount;

    private int entPredAmount;
    private int entPreyAmount;

    private JLabel warning;
    private JPanel row1;
    private JPanel row2;
    private JPanel row3;
    private JPanel row4;
    private Dimension rowSize;

    // Individual improvement: live ecosystem statistics
    private JLabel statsLabel;

    public static void main(String[] args) throws Exception {

        List<Creature> sim = new ArrayList<>();

        Simulation mainSim = new Simulation(sim);
        mainSim.frameInitialise();
    }

    public Simulation(List<Creature> sim) {
        this.sim = sim;
        this.grassList = new ArrayList<>();
    }

    public void frameInitialise() {

        simStart = new JFrame("Predator, Prey Simulation");
        simStart.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        simStart.setSize(800, 700);
        simStart.setLocationRelativeTo(null);
        simStart.setResizable(false);

        panel = new JPanel();

        warning = new JLabel("DO NOT SAVE NUMBERS MULTIPLE TIMES");

        begin = new JButton("Start");

        pred = new JLabel("Starting predator amount");
        prey = new JLabel("Starting prey amount");

        predAmount = new JTextField(10);
        preyAmount = new JTextField(10);

        savePred = new JButton("Save");
        savePrey = new JButton("Save");

        row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row3 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        rowSize = new Dimension(800, 80);

        // Start the running simulation
        begin.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                spawnGrass();

                simStart.setDefaultCloseOperation(
                        JFrame.DO_NOTHING_ON_CLOSE
                );

                runningSim = new JFrame(
                        "Predator, Prey Simulation. RUNNING"
                );

                runningSim.setDefaultCloseOperation(
                        JFrame.EXIT_ON_CLOSE
                );

                simStart.dispose();
                simStart.setVisible(false);

                // Create simulation display
                simPanel = new SimPanel();

                simPanel.setPreferredSize(
                        new Dimension(
                                Creature.WORLD_WIDTH,
                                Creature.WORLD_HEIGHT
                        )
                );

                simPanel.setBackground(new Color(18, 84, 13));

                runningSim.add(simPanel, BorderLayout.CENTER);

                // New: create the live statistics bar
                statsLabel = new JLabel();

                statsLabel.setOpaque(true);
                statsLabel.setBackground(new Color(24, 55, 30));
                statsLabel.setForeground(Color.WHITE);

                statsLabel.setFont(
                        statsLabel.getFont().deriveFont(15f)
                );

                statsLabel.setBorder(
                        BorderFactory.createEmptyBorder(
                                10, 15, 10, 15
                        )
                );

                runningSim.add(statsLabel, BorderLayout.SOUTH);

                // Display initial population statistics
                updateStatistics();

                runningSim.setResizable(false);
                runningSim.pack();
                runningSim.setLocationRelativeTo(null);
                runningSim.setVisible(true);

                System.out.println("Button");

                startGameLoop();
            }
        });

        // Save starting predator amount
        savePred.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String strInput = predAmount.getText();
                int input;

                try {
                    input = Integer.parseInt(strInput);
                } catch (NumberFormatException ex) {
                    System.out.println(
                            "Invalid predator amount: " + strInput
                    );
                    return;
                }

                for (int i = 0; i < input; i++) {

                    int x = random.nextInt(
                            Creature.WORLD_WIDTH - 20
                    );

                    int y = random.nextInt(
                            Creature.WORLD_HEIGHT - 20
                    );

                    sim.add(new Predator(5, 0, x, y, sim) { });

                    System.out.println(
                            "predator added: " + (i + 1) + " times"
                    );
                }
            }
        });

        // Save starting prey amount
        savePrey.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String strInput = preyAmount.getText();
                int input;

                try {
                    input = Integer.parseInt(strInput);
                } catch (NumberFormatException ex) {
                    System.out.println(
                            "Invalid prey amount: " + strInput
                    );
                    return;
                }

                for (int i = 0; i < input; i++) {

                    int x = random.nextInt(
                            Creature.WORLD_WIDTH - 20
                    );

                    int y = random.nextInt(
                            Creature.WORLD_HEIGHT - 20
                    );

                    sim.add(
                            new Prey(
                                    5, 10, x, y,
                                    sim, grassList
                            )
                    );

                    System.out.println(
                            "Prey added: " + (i + 1) + " Times"
                    );
                }
            }
        });

        // Build the start window
        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(80, 20, 20, 20)
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
        simStart.add(panel);

        simStart.setVisible(true);
    }

    // Create grass patches across the world
    private void spawnGrass() {

        for (int i = 0; i < Creature.WORLD_WIDTH; i += GRASS_SIZE) {

            for (int j = 0; j < Creature.WORLD_HEIGHT; j += GRASS_SIZE) {

                int randomGrowth = random.nextInt(
                        GRASS_GROWTH_TIME
                );

                int randomWeight = random.nextInt(20);

                boolean isEaten = true;

                if (randomWeight < 1) {
                    isEaten = false;
                }

                grassList.add(
                        new Grass(i, j, randomGrowth, isEaten)
                );
            }
        }
    }

    // Run creature movement, eating and simulation updates
    private void startGameLoop() {

        gameTimer = new Timer(
                RENDER_INTERVAL_MS,
                new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {

                        List<Creature> snapshot =
                                new ArrayList<>(sim);

                        for (Creature c : snapshot) {
                            c.movement();
                            c.eat();
                        }

                        frameCounter++;

                        if (frameCounter >= TICKS_PER_LOGIC_UPDATE) {

                            runSimulationTick();

                            // New: refresh live ecosystem statistics
                            updateStatistics();

                            frameCounter = 0;
                        }

                        repaint();
                    }
                }
        );

        gameTimer.start();
    }

    // Handle starvation, reproduction and grass regrowth
    public void runSimulationTick() {

        List<Creature> snapshot = new ArrayList<>(sim);
        List<Creature> toRemove = new ArrayList<>();

        for (Creature c : snapshot) {

            if (c.starve()) {
                toRemove.add(c);
            }
        }

        sim.removeAll(toRemove);

        List<Creature> reproSnapshot = new ArrayList<>(sim);

        for (Creature c : reproSnapshot) {
            c.reproduce();
        }

        for (Grass g : grassList) {
            g.tickGrowth();
        }
    }

    // Individual improvement:
    // Use Java Streams and Collectors for live ecosystem statistics
    private void updateStatistics() {

        // Group creatures into predators and prey
        Map<String, Long> populationCounts = sim.stream()
                .collect(
                        Collectors.groupingBy(
                                creature -> creature instanceof Predator
                                        ? "Predator"
                                        : "Prey",
                                Collectors.counting()
                        )
                );

        long predatorCount = populationCounts.getOrDefault(
                "Predator", 0L
        );

        long preyCount = populationCounts.getOrDefault(
                "Prey", 0L
        );

        // Count edible grass using a Stream
        long edibleGrassCount = grassList.stream()
                .filter(Grass::isEdible)
                .count();

        // Update the statistics displayed below the world
        if (statsLabel != null) {

            statsLabel.setText(
                    "Predators: " + predatorCount
                    + "       |       Prey: " + preyCount
                    + "       |       Edible grass: " + edibleGrassCount
            );
        }
    }

    public void repaint() {

        if (simPanel != null) {
            simPanel.repaint();
        }
    }

    // Render grass, predators and prey
    private class SimPanel extends JPanel {

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            // Edible grass is green
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

            // Predators are red and prey are blue
            for (Creature c : new ArrayList<>(sim)) {

                g.setColor(
                        c instanceof Predator
                                ? Color.RED
                                : Color.BLUE
                );

                g.fillOval(
                        c.getX(),
                        c.getY(),
                        20,
                        20
                );
            }
        }
    }
}
