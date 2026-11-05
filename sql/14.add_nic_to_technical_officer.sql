-- ============================================================================
-- FILE: 14.add_nic_to_technical_officer.sql
-- PURPOSE: Add NIC field to technical_officer table and populate with mock data
-- NOTES: NIC supports both old format (9 digits + V) and new format (12 digits)
--        The mock data is aligned with the DOB of existing users
-- ============================================================================

-- Add nic column to technical_officer table
ALTER TABLE technical_officer
ADD COLUMN nic VARCHAR(12) NULL
COMMENT 'National Identity Card number (old: 9 digits + V, new: 12 digits)';

-- ============================================================================
-- MOCK DATA: Update NIC for existing technical officers
-- ============================================================================

-- Technical Officer 7: Pradeep Bandara, DOB: 1990-01-25 (Male)
-- Old format: 90025XXXXX (Year: 90, Day: 025)
UPDATE technical_officer SET nic = '900251234V' WHERE user_id = 7;

-- Technical Officer 8: Thilini Herath, DOB: 1992-04-08 (Female)
-- Old format: 92099XXXXX (Year: 92, Day: 099 = 08 April + 500 for female)
-- Day 98 = April 8, but for females we add 500, so 98 + 500 = 598, but in old format we use 099
-- Correction: Day of year for April 8 = 31+28+31+8 = 98, female = 98+500 = 598
UPDATE technical_officer SET nic = '925981234V' WHERE user_id = 8;

-- Technical Officer 21: Saman Kumara, DOB: 1991-07-14 (Male)
-- Old format: 91195XXXXX (Year: 91, Day: 195 = July 14)
-- Day of year: 31+28+31+30+31+30+14 = 195
UPDATE technical_officer SET nic = '911955678V' WHERE user_id = 21;

-- Technical Officer 22: Nimali Karunaratne, DOB: 1993-02-06 (Female)
-- New format: 199353700123 (Year: 1993, Day: 537 = Feb 6 + 500 for female)
-- Day of year: 31+6 = 37, female = 37+500 = 537
UPDATE technical_officer SET nic = '199353700123' WHERE user_id = 22;

-- ============================================================================
-- VERIFICATION
-- ============================================================================

-- Display technical officers with their NIC and DOB for verification
SELECT 
    t.user_id,
    u.name,
    u.dob,
    u.gender,
    t.nic
FROM technical_officer t
JOIN users u ON u.user_id = t.user_id
ORDER BY t.user_id;
