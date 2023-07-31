package Launcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class Launcher extends JFrame implements ActionListener, Runnable {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);
    private Path workingDir;
    private final VersionController controller;
    private final VersionRepairer repairer;
    private JLabel nameLabel;
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem configItem;
    private JTextField nameField;
    private JProgressBar progressBar;
    private JProgressBar totalProgressBar;
    private JTextArea infoArea;
    private JComboBox<VersionEntry> versionSelector;
    private JButton playButton;
    private JButton repairButton;
    private VersionLauncher launcher;
    private OutputWrapper wrapper;
    private String preferredVersion;
    private String jvmArgs;

    public Launcher(Path workingDir, OutputWrapper wrapper) {
        this.wrapper = wrapper;
        this.infoArea = wrapper.getArea();
        this.workingDir = workingDir;
        this.jvmArgs = "-Xmx2048M -Xms1048M -XX:+UseG1GC -Dsun.rmi.dgc.server.gcInterval=2147483646 -XX:+UnlockExperimentalVMOptions -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";
        this.controller = new VersionController(workingDir);
        initComponents();
        this.launcher = new VersionLauncher(progressBar, totalProgressBar, workingDir, wrapper, this);
        this.repairer = new VersionRepairer(progressBar, totalProgressBar, workingDir);
        this.setTitle("Emerald Launcher 1.11");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(600, 400));
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        new Thread(this).start();
    }

    private void initComponents() {
        this.menuBar = new JMenuBar();
        this.menu = new JMenu("Settings");
        this.configItem = new JMenuItem("JVM parameters");
        this.configItem.addActionListener(this);
        this.nameLabel = new JLabel("Name: ");
        this.nameField = new JTextField(20);
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(true);
        this.totalProgressBar = new JProgressBar();
        this.totalProgressBar.setStringPainted(true);
        this.infoArea.setEditable(false);
        this.playButton = new JButton("Play");
        this.playButton.addActionListener(this);
        this.repairButton = new JButton("Repair");
        this.repairButton.addActionListener(this);
        ((DefaultCaret)infoArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        this.menuBar.add(menu);
        this.menu.add(configItem);
        this.setJMenuBar(menuBar);

        this.versionSelector = new JComboBox<>();
        JPanel bottomPanel = new JPanel();
        JPanel settingsPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.add(totalProgressBar);
        bottomPanel.add(progressBar);
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.X_AXIS));
        settingsPanel.add(versionSelector);
        settingsPanel.add(nameLabel);
        settingsPanel.add(nameField);
        settingsPanel.add(playButton);
        settingsPanel.add(repairButton);
        bottomPanel.add(settingsPanel);

        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        this.getContentPane().add(new JScrollPane(this.infoArea), BorderLayout.CENTER);

        loadConfigs();

        controller.loadVersionListFromDisk();
        List<VersionEntry> versionEntries = controller.getVersionEntries();
        Iterator<VersionEntry> itr = versionEntries.iterator();
        int index = 0;
        boolean found = false;
        while(itr.hasNext()) {
            VersionEntry entry = itr.next();
            this.versionSelector.addItem(entry);
            if(entry.toString().equals(preferredVersion)) {
                this.versionSelector.setSelectedIndex(index);
                found = true;
            }
            index++;
        }
        if(!found && this.versionSelector.getItemCount() > 0)
            this.versionSelector.setSelectedIndex(0);
    }

    private void loadConfigs() {
        Path config = this.workingDir.resolve("config.dat");
        Path jvmArgs = this.workingDir.resolve("jvm.dat");
        try {
            String conf = Files.readString(config);
            if(conf.contains(";")) {
                String[] parts = conf.split(";");
                this.preferredVersion = parts[0];
                this.nameField.setText(parts[1]);
            }
        } catch (IOException e) { //
        }

        try {
            this.jvmArgs = Files.readString(jvmArgs);
        } catch (IOException e) { //
        }
    }

    private void writeConfigs(String name, VersionEntry versionEntry) {
        Path config = this.workingDir.resolve("config.dat");
        try {
            Files.write(config, (versionEntry.toString() + ";" + name).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) { //
        }
    }

    private void saveJVMArgs() {
        Path config = this.workingDir.resolve("jvm.dat");
        try {
            Files.write(config, jvmArgs.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) { //
        }
    }

    private void loadVersionList() {
        log.info("Loading list of available game versions..");
        if(this.controller.loadVersionList()) {
            log.info("Game version list downloaded");
        } else {
            log.error("Failed to load list of minecraft versions! Maybe a network issue?");
        }
    }

    public void gameExited() {
        this.setVisible(true);
    }

    public void gameStarted() {
        this.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JButton) {
            JButton source = (JButton) e.getSource();
            if(source.getText().equals("Play")) {
                if (this.nameField.getText().isEmpty()) {
                    log.info("Name field was empty");
                    JOptionPane.showMessageDialog(null, "Name field cannot be empty!", "Empty name", JOptionPane.ERROR_MESSAGE);
                } else if (this.versionSelector.getSelectedItem() == null) {
                    log.info("Game version was not selected");
                    JOptionPane.showMessageDialog(null, "You need to select minecraft version to play!", "Empty version", JOptionPane.ERROR_MESSAGE);
                } else {
                    log.info("Trying to launch version: {}", this.versionSelector.getSelectedItem());
                    writeConfigs(this.nameField.getText(), (VersionEntry) this.versionSelector.getSelectedItem());
                    this.launcher.launch((VersionEntry) this.versionSelector.getSelectedItem(), this.nameField.getText(), jvmArgs);
                }
            } else {
                // repair
                this.repairer.repair((VersionEntry) this.versionSelector.getSelectedItem());
            }
        } else if(e.getSource() instanceof JMenuItem) {
            String args = JOptionPane.showInputDialog("Input jvm args", jvmArgs);
            if(args != null && !args.isEmpty()) {
                this.jvmArgs = args;
                log.info("JVM arguments set to: {}", args);
                saveJVMArgs();
            } else {
                log.info("JVM arguments not saved");
            }
        }
    }

    @Override
    public void run() {
        loadVersionList();
        List<VersionEntry> versionEntries = controller.getVersionEntries();
        Iterator<VersionEntry> itr = versionEntries.iterator();
        int index = 0;
        boolean found = false;
        this.versionSelector.removeAllItems();
        while(itr.hasNext()) {
            VersionEntry entry = itr.next();
            this.versionSelector.addItem(entry);
            if(entry.toString().equals(preferredVersion)) {
                this.versionSelector.setSelectedIndex(index);
                found = true;
            }
            index++;
        }
        if(!found)
            this.versionSelector.setSelectedIndex(0);
    }
}
