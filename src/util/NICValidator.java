package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for validating Sri Lankan National Identity Card (NIC) numbers
 * and extracting Date of Birth from NIC.
 * 
 * Supports both formats:
 * - Old format: 9 digits + 'V' or 'X' (e.g., 900251234V)
 * - New format: 12 digits (e.g., 199002512345)
 */
public class NICValidator {

    /**
     * Validates a Sri Lankan NIC number.
     * 
     * @param nic The NIC number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidNIC(String nic) {
        if (nic == null || nic.trim().isEmpty()) {
            return false;
        }
        
        nic = nic.trim().toUpperCase();
        
        // Check old format: 9 digits + V or X
        if (nic.matches("^\\d{9}[VX]$")) {
            return true;
        }
        
        // Check new format: 12 digits
        if (nic.matches("^\\d{12}$")) {
            return true;
        }
        
        return false;
    }

    /**
     * Extracts the Date of Birth from a Sri Lankan NIC number.
     * 
     * @param nic The NIC number
     * @return LocalDate representing the date of birth, or null if extraction fails
     */
    public static LocalDate extractDOB(String nic) {
        if (!isValidNIC(nic)) {
            return null;
        }
        
        nic = nic.trim().toUpperCase();
        
        try {
            if (nic.matches("^\\d{9}[VX]$")) {
                // Old format: YYDDDXXXXV
                return extractDOBFromOldFormat(nic);
            } else if (nic.matches("^\\d{12}$")) {
                // New format: YYYYDDDXXXXX
                return extractDOBFromNewFormat(nic);
            }
        } catch (Exception e) {
            return null;
        }
        
        return null;
    }

    /**
     * Extracts DOB from old format NIC (9 digits + V/X).
     */
    private static LocalDate extractDOBFromOldFormat(String nic) {
        // Extract year (first 2 digits)
        int yy = Integer.parseInt(nic.substring(0, 2));
        
        // Determine century (assume 1900s for >= 20, 2000s for < 20)
        int year = (yy >= 20) ? 1900 + yy : 2000 + yy;
        
        // Extract day of year (next 3 digits)
        int dayOfYear = Integer.parseInt(nic.substring(2, 5));
        
        // For females, 500 is added to the day number
        if (dayOfYear > 500) {
            dayOfYear -= 500;
        }
        
        // Convert day of year to date
        return LocalDate.ofYearDay(year, dayOfYear);
    }

    /**
     * Extracts DOB from new format NIC (12 digits).
     */
    private static LocalDate extractDOBFromNewFormat(String nic) {
        // Extract year (first 4 digits)
        int year = Integer.parseInt(nic.substring(0, 4));
        
        // Extract day of year (next 3 digits)
        int dayOfYear = Integer.parseInt(nic.substring(4, 7));
        
        // For females, 500 is added to the day number
        if (dayOfYear > 500) {
            dayOfYear -= 500;
        }
        
        // Convert day of year to date
        return LocalDate.ofYearDay(year, dayOfYear);
    }

    /**
     * Validates if the NIC matches the given Date of Birth.
     * 
     * @param nic The NIC number
     * @param dob The date of birth to compare (java.sql.Date)
     * @return true if they match, false otherwise
     */
    public static boolean validateNICWithDOB(String nic, java.sql.Date dob) {
        if (dob == null) {
            return false;
        }
        
        LocalDate extractedDOB = extractDOB(nic);
        if (extractedDOB == null) {
            return false;
        }
        
        LocalDate givenDOB = dob.toLocalDate();
        
        return extractedDOB.equals(givenDOB);
    }

    /**
     * Gets a user-friendly error message for NIC-DOB mismatch.
     * 
     * @param nic The NIC number
     * @param dob The date of birth
     * @return Error message, or null if they match
     */
    public static String getValidationMessage(String nic, java.sql.Date dob) {
        if (!isValidNIC(nic)) {
            return "Invalid NIC format. Use old format (9 digits + V) or new format (12 digits).";
        }
        
        LocalDate extractedDOB = extractDOB(nic);
        if (extractedDOB == null) {
            return "Could not extract date of birth from NIC.";
        }
        
        if (dob == null) {
            return "Date of birth is required for NIC validation.";
        }
        
        LocalDate givenDOB = dob.toLocalDate();
        
        if (!extractedDOB.equals(givenDOB)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return String.format("NIC date of birth (%s) does not match the given date of birth (%s).",
                    extractedDOB.format(formatter),
                    givenDOB.format(formatter));
        }
        
        return null; // Valid
    }
}
