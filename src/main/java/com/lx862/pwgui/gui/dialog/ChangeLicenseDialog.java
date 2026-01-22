package com.lx862.pwgui.gui.dialog;

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.lx862.pwgui.PWGUI;
import com.lx862.pwgui.core.Config;
import com.lx862.pwgui.gui.ImageUtil;
import com.lx862.pwgui.gui.action.CloseWindowAction;
import com.lx862.pwgui.gui.action.OKAction;
import com.lx862.pwgui.gui.listener.DocumentChangedListener;
import com.lx862.pwgui.gui.components.kui.*;
import com.lx862.pwgui.gui.GUIConfiguration;
import com.lx862.pwgui.util.Util;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

public class ChangeLicenseDialog extends BaseDialog {
    private LicenseModel selectedLicenseModel = null;
    private JPanel overviewPanel = null;
    private KTextArea licenseTextArea = null;

    static ImageIcon licensePermissionsIcon = new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/license_permissions.png"), 12));
    static ImageIcon licenseConditionsIcon = new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/license_conditions.png"), 12));
    static ImageIcon licenseLimitationsIcon = new ImageIcon(ImageUtil.convertImage(Util.getAssets("/assets/ui/license_limitations.png"), 12));

    public ChangeLicenseDialog(JFrame parent, File licenseFile) {
        super(parent, Util.withTitlePrefix("Change License"), true);
        setSize(740, 520);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        List<LicenseModel> licenses = getAvailableLicenses();

        KRootContentPanel contentPanel = new KRootContentPanel(10);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.PAGE_AXIS));

        JLabel titleLabel = new JLabel("Change License");
        titleLabel.setFont(FlatUIUtils.nonUIResource(UIManager.getFont("h2.font")));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(titleLabel);

        JLabel descriptionLabel = new JLabel("Here you may pick a license of your choice, or enter a custom one.");
        descriptionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(descriptionLabel);

        contentPanel.add(GUIConfiguration.createVerticalPadding(5));

        // Allow entering placeholder values
        JPanel placeholdersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        placeholdersPanel.setBorder(new CompoundBorder(GUIConfiguration.getSeparatorBorder(true, true), GUIConfiguration.getPaddedBorder(4, 0, 4, 0)));
        placeholdersPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel nameLabel = new JLabel("Your name: ");
        JTextField nameTextField = new KTextField();
        nameTextField.setText(Config.getInstance().authorName.getValue() == null ? "<Your name>" : Config.getInstance().authorName.getValue());

        JLabel yearLabel = new JLabel("Copyright year: ");
        JTextField yearTextField = new KTextField();
        yearTextField.setText(String.valueOf(Year.now().getValue()));

        nameTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> {
            setLicense(nameTextField.getText(), yearTextField.getText(), true);
        }));
        yearTextField.getDocument().addDocumentListener(new DocumentChangedListener(() -> {
            setLicense(nameTextField.getText(), yearTextField.getText(), true);
        }));
        placeholdersPanel.add(nameLabel);
        placeholdersPanel.add(nameTextField);
        placeholdersPanel.add(yearLabel);
        placeholdersPanel.add(yearTextField);

        contentPanel.add(placeholdersPanel);
        contentPanel.add(GUIConfiguration.createVerticalPadding(5));

        // Left Pane
        JPanel leftPane = new JPanel(new BorderLayout());
        leftPane.add(new JLabel("Selected License:"), BorderLayout.NORTH);

        JList<LicenseModel> licenseJList = new JList<>();
//        licenseJList.setCellRenderer(new KListCellRenderer());

        DefaultListModel<LicenseModel> licenseListModel = new DefaultListModel<>();
        licenseListModel.addAll(licenses);

        licenseJList.setModel(licenseListModel);
        licenseJList.addListSelectionListener(listSelectionEvent -> {
            selectedLicenseModel = licenseJList.getSelectedValue();
            setLicense(nameTextField.getText(), yearTextField.getText(), false);
        });

        leftPane.add(new JScrollPane(licenseJList), BorderLayout.CENTER);

        // Right Pane
        overviewPanel = new JPanel();
        overviewPanel.setBorder(GUIConfiguration.getPaddedBorder(6, 3, 6, 6));
        overviewPanel.setLayout(new BoxLayout(overviewPanel, BoxLayout.LINE_AXIS));

        JPanel rightTopPanel = new JPanel(new BorderLayout());
//        rightTopPanel.add(new JLabel("License preview:"), BorderLayout.NORTH); // Doesn't look that good?
        rightTopPanel.add(overviewPanel, BorderLayout.SOUTH);

        JPanel rightPane = new JPanel(new BorderLayout());
        licenseTextArea = new KTextArea();
        licenseTextArea.useMonospacedFont();

        rightPane.add(new JScrollPane(licenseTextArea), BorderLayout.CENTER);
        rightPane.add(rightTopPanel, BorderLayout.NORTH);


        JSplitPane splitPane = new KSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane, 0.35);
        splitPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(splitPane);

        JButton changeLicenseButton = new KButton(new ChangeLicenseAction(licenseFile));
        JButton cancelButton = new KButton(new CloseWindowAction("Cancel", this));

        KActionPanel actionPanel = new KActionPanel.Builder().setNegativeButton(cancelButton).setPositiveButton(changeLicenseButton).build();
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        contentPanel.add(actionPanel);
        add(contentPanel);

        setLicense(nameTextField.getText(), yearTextField.getText(), false);
        SwingUtilities.invokeLater(splitPane::requestFocus);
    }

    private void setLicense(String name, String year, boolean updateMetadata) {
        if(selectedLicenseModel == null) {
            licenseTextArea.setText("Please select a license from the left to preview them.\n<==============");
            licenseTextArea.setEditable(false);
        } else {
            if(!updateMetadata || !selectedLicenseModel.editable()) {
                String finalLicenseContent = selectedLicenseModel.content().replace("[fullname]", name).replace("[year]", year);
                licenseTextArea.setText(finalLicenseContent, true);
                licenseTextArea.setEditable(selectedLicenseModel.editable());
            }
            updateOverviewPanel(overviewPanel, selectedLicenseModel.licenseOverview());
        }
    }

    private List<LicenseModel> getAvailableLicenses() {
        List<LicenseModel> licenses = new ArrayList<>();
        try(InputStream is = Util.getAssets("/assets/licenses/licenses.json")) {
            if(is != null) {
                JsonArray licensesArray = new Gson().fromJson(new JsonReader(new InputStreamReader(is)), JsonArray.class);
                for(JsonElement license : licensesArray) {
                    JsonObject licenseObject = license.getAsJsonObject();
                    String name = licenseObject.get("name").getAsString();
                    String file = licenseObject.get("file").getAsString();
                    boolean editable = licenseObject.get("editable").getAsBoolean();
                    String content;

                    try(InputStream licenseIS = Util.getAssets(String.format("/assets/licenses/%s", file))) {
                        content = new String(licenseIS.readAllBytes());
                    } catch (NullPointerException | IOException e) {
                        PWGUI.LOGGER.error("", e);
                        content = String.format("Failed to read license file %s:\n%s", file, e.getMessage());
                    }

                    LicenseOverview licenseOverview = LicenseOverview.fromJson(licenseObject.get("overview"));
                    licenses.add(new LicenseModel(name, content, licenseOverview, editable));
                }
            } else {
                licenses.add(new LicenseModel("Error!", "Failed to find available licenses for display!\nThis is either a bug or a semi-corrupted installation of the program!", null, false));
            }
        } catch (IOException e) {
            PWGUI.LOGGER.error("", e);
            licenses.add(new LicenseModel("Error!", "Failed to read available licenses!\nThis is either a bug or a semi-corrupted installation of the program!", null, false));
        }
        return licenses;
    }

    private void updateOverviewPanel(JPanel overviewPanel, LicenseOverview licenseOverview) {
        overviewPanel.removeAll();
        if(licenseOverview != null) {
            addOverviewCategoryPanel(overviewPanel, "Permissions", licenseOverview.permissions(), licensePermissionsIcon);
            overviewPanel.add(GUIConfiguration.createHorizontalPadding(15));
            addOverviewCategoryPanel(overviewPanel, "Conditions", licenseOverview.conditions(), licenseConditionsIcon);
            overviewPanel.add(GUIConfiguration.createHorizontalPadding(15));
            addOverviewCategoryPanel(overviewPanel, "Limitations", licenseOverview.limitations(), licenseLimitationsIcon);
        }

        overviewPanel.updateUI();
    }

    private void addOverviewCategoryPanel(JPanel overviewPanel, String categoryName, List<String> strings, ImageIcon labelIcon) {
        JPanel panel = new JPanel();
        panel.setAlignmentY(Component.TOP_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        JLabel categoryLabel = new JLabel(categoryName);
        categoryLabel.setFont(categoryLabel.getFont().deriveFont(Font.BOLD));
        panel.add(categoryLabel);

        for(String str : strings) {
            panel.add(new JLabel(str, labelIcon, SwingConstants.LEFT));
        }

        overviewPanel.add(panel);
    }

    private record LicenseModel(String name, String content, LicenseOverview licenseOverview, boolean editable) {
        @Override
        public String toString() {
            return name;
        }
    }

    private record LicenseOverview(List<String> permissions, List<String> conditions, List<String> limitations) {
        public static LicenseOverview fromJson(JsonElement overviewJson) {
            if(overviewJson.isJsonNull()) return null;
            JsonObject jsonObject = overviewJson.getAsJsonObject();
            List<String> permissions = new ArrayList<>();
            List<String> conditions = new ArrayList<>();
            List<String> limitations = new ArrayList<>();

            for(JsonElement jsonElement : jsonObject.get("permissions").getAsJsonArray()) {
                permissions.add(jsonElement.getAsString());
            }
            for(JsonElement jsonElement : jsonObject.get("conditions").getAsJsonArray()) {
                conditions.add(jsonElement.getAsString());
            }
            for(JsonElement jsonElement : jsonObject.get("limitations").getAsJsonArray()) {
                limitations.add(jsonElement.getAsString());
            }

            return new LicenseOverview(permissions, conditions, limitations);
        }
    }

    class ChangeLicenseAction extends OKAction {
        private final File licenseFile;

        public ChangeLicenseAction(File licenseFile) {
            super(() -> {});
            this.licenseFile = licenseFile;
        }

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String licenseContent = licenseTextArea.getText();
            try {
                FileUtils.write(licenseFile, licenseContent, StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(ChangeLicenseDialog.this, String.format("License changed to %s!", selectedLicenseModel), Util.withTitlePrefix("Change License"), JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                PWGUI.LOGGER.error("", e);
                JOptionPane.showMessageDialog(ChangeLicenseDialog.this, "Failed to save new license, see program logs for detail!", Util.withTitlePrefix("Change License"), JOptionPane.ERROR_MESSAGE);
            }
            dispose();
        }
    }
}
