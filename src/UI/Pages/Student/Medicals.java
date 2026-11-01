
package UI.Pages.Student;

import UI.Pages.Components.Card;
import UI.Pages.Components.Chip;
import UI.Pages.Components.Page;
import data.MedicalData;
import Exception.DataAccessException;
import model.Medical;
import model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

import static UI.Kit.Palette.*;

public class Medicals extends Page {

    private final User user;
    private final MedicalData medicalData = new MedicalData();

    private final JPanel list = new JPanel();
    private final JLabel emptyLabel = new JLabel(
            "No medical records found.",
            SwingConstants.CENTER
    );

    public Medicals(User user) {
        super("Medicals", "View your medical leave submissions and approvals", null);

        this.user = user;

        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(10, 0, 0, 0));

        emptyLabel.setFont(font(Font.PLAIN, 14));
        emptyLabel.setForeground(Color.GRAY);
        emptyLabel.setBorder(new EmptyBorder(30, 10, 30, 10));

        body.add(list, BorderLayout.NORTH);
        body.add(emptyLabel, BorderLayout.CENTER);

        setScrollBody(body);

        loadMedicalRecords();
    }

    private void loadMedicalRecords() {

        list.removeAll();

        if (user == null) {
            showEmptyMessage("Unable to identify the logged-in student.");
            return;
        }

        try {
            int studentId = user.getUser_id();

            List<Medical> records =
                    medicalData.findByStudent(studentId);

            if (records == null || records.isEmpty()) {
                showEmptyMessage("No medical records found.");
                return;
            }

            emptyLabel.setVisible(false);

            for (Medical medical : records) {
                list.add(medicalCard(medical));
                list.add(Box.createVerticalStrut(15));
            }

            list.revalidate();
            list.repaint();

        } catch (DataAccessException e) {
            e.printStackTrace();
            showEmptyMessage("Unable to load medical records.");

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to load medical records.\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (Exception e) {
            e.printStackTrace();
            showEmptyMessage("Something went wrong.");

            JOptionPane.showMessageDialog(
                    this,
                    "An unexpected error occurred.\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showEmptyMessage(String message) {
        list.removeAll();
        emptyLabel.setText(message);
        emptyLabel.setVisible(true);

        list.revalidate();
        list.repaint();
    }

    private JComponent medicalCard(Medical medical) {

        Card card = new Card(false);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 25, 18, 25));

        // Date block
        String day = "--";
        String month = "---";

        if (medical.getMedicalDate() != null) {
            java.time.LocalDate date =
                    medical.getMedicalDate().toLocalDate();

            day = String.format("%02d", date.getDayOfMonth());

            month = date.getMonth()
                    .name()
                    .substring(0, 3);
        }

        JPanel dateBlock = new JPanel(new GridLayout(2, 1));
        dateBlock.setOpaque(false);

        JLabel dayLbl = new JLabel(day, SwingConstants.CENTER);
        dayLbl.setFont(font(Font.BOLD, 22));
        dayLbl.setForeground(INK);

        JLabel monthLbl = new JLabel(
                month,
                SwingConstants.CENTER
        );
        monthLbl.setFont(font(Font.PLAIN, 11));
        monthLbl.setForeground(Color.GRAY);

        dateBlock.add(dayLbl);
        dateBlock.add(monthLbl);

        JPanel dateContainer = new JPanel(new BorderLayout());
        dateContainer.setOpaque(false);

        dateContainer.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 0, 0, 1, LINE
                        ),
                        new EmptyBorder(0, 5, 0, 25)
                )
        );

        dateContainer.add(dateBlock, BorderLayout.NORTH);

        // Information block
        JPanel infoBlock = new JPanel();
        infoBlock.setLayout(
                new BoxLayout(infoBlock, BoxLayout.Y_AXIS)
        );
        infoBlock.setOpaque(false);
        infoBlock.setBorder(new EmptyBorder(0, 15, 0, 0));

        // Status and reason
        JPanel titleRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 0)
        );
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        String status = medical.getStatus();

        if (status == null || status.trim().isEmpty()) {
            status = "UNKNOWN";
        }

        Color statusColor = getStatusColor(status);

        titleRow.add(
                new Chip("● " + status.toUpperCase(), statusColor)
        );

        String reason = medical.getReason();

        if (reason == null || reason.trim().isEmpty()) {
            reason = "Medical leave request";
        }

        JLabel reasonLbl = new JLabel(reason);
        reasonLbl.setFont(font(Font.BOLD, 15));
        reasonLbl.setForeground(Color.BLACK);

        titleRow.add(reasonLbl);

        // Certificate
        JPanel certificateRow = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0)
        );
        certificateRow.setOpaque(false);
        certificateRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        certificateRow.setBorder(
                new EmptyBorder(10, 0, 5, 0)
        );

        String certificatePath = medical.getCertificatePath();

        if (certificatePath != null
                && !certificatePath.trim().isEmpty()) {

            String fileName = new File(certificatePath)
                    .getName();

            JLabel certificateLabel = new JLabel(
                    "Certificate: " + fileName
            );

            certificateLabel.setFont(font(Font.PLAIN, 12));
            certificateLabel.setForeground(Color.GRAY);

            JButton openButton = new JButton("Open");
            openButton.setFont(font(Font.BOLD, 11));
            openButton.setFocusPainted(false);
            openButton.addActionListener(e ->
                    openCertificate(certificatePath)
            );

            certificateRow.add(certificateLabel);
            certificateRow.add(openButton);

        } else {
            JLabel noCertificate = new JLabel(
                    "No certificate uploaded"
            );
            noCertificate.setFont(font(Font.PLAIN, 12));
            noCertificate.setForeground(Color.GRAY);

            certificateRow.add(noCertificate);
        }

        // Reviewer information
        String reviewerNote = medical.getReviewerNote();

        if (reviewerNote != null && !reviewerNote.trim().isEmpty()) {
            JLabel noteLabel = new JLabel(
                    "Reviewer note: " + reviewerNote
            );

            noteLabel.setFont(font(Font.PLAIN, 12));
            noteLabel.setForeground(Color.GRAY);
            noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            certificateRow.add(noteLabel);
        }

        if (medical.getReviewedDate() != null) {
            JLabel reviewedLabel = new JLabel(
                    "Reviewed: " + medical.getReviewedDate()
            );

            reviewedLabel.setFont(font(Font.PLAIN, 12));
            reviewedLabel.setForeground(Color.GRAY);
            reviewedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            certificateRow.add(reviewedLabel);
        }

        infoBlock.add(titleRow);
        infoBlock.add(certificateRow);

        card.add(dateContainer, BorderLayout.WEST);
        card.add(infoBlock, BorderLayout.CENTER);

        card.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 160)
        );

        return card;
    }

    private Color getStatusColor(String status) {

        switch (status.toUpperCase()) {
            case "APPROVED":
                return OK;

            case "PENDING":
                return new Color(230, 160, 45);

            case "REJECTED":
                return new Color(190, 65, 65);

            default:
                return Color.GRAY;
        }
    }

    private void openCertificate(String certificatePath) {

        try {
            File file = new File(certificatePath);

            if (!file.isAbsolute()) {
                file = new File(
                        System.getProperty("user.dir"),
                        certificatePath.replace(
                                "/",
                                File.separator
                        )
                );
            }

            if (!file.exists()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Certificate file was not found:\n"
                                + file.getAbsolutePath(),
                        "File Not Found",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Opening files is not supported on this computer.",
                        "Not Supported",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            Desktop.getDesktop().open(file);

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open certificate.\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
