package com.motorph.ms2motorph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class MOIT101Group6 {

    private static final String EMPLOYEE_FILE = "Resources/MotorPH_Employee Data.csv";
    private static final String ATTENDANCE_FILE = "Resources/Attendance Record.csv";
    private static final int PAYROLL_YEAR = 2024;
    private static final int FIRST_DISPLAY_MONTH = 6;
    private static final int LAST_DISPLAY_MONTH = 12;

    // Employee record indexes: [id, firstName, lastName, birthday, monthlySalary, hourlyRate]
    private static final int EMPLOYEE_ID = 0;
    private static final int EMPLOYEE_FIRST_NAME = 1;
    private static final int EMPLOYEE_LAST_NAME = 2;
    private static final int EMPLOYEE_BIRTHDAY = 3;
    private static final int EMPLOYEE_HOURLY_RATE = 5;

    // Attendance record indexes: [employeeId, year, month, day, loginTime, logoutTime]
    private static final int ATTENDANCE_YEAR = 1;
    private static final int ATTENDANCE_MONTH = 2;
    private static final int ATTENDANCE_DAY = 3;
    private static final int ATTENDANCE_LOGIN_TIME = 4;
    private static final int ATTENDANCE_LOGOUT_TIME = 5;

    // Payroll summary indexes stored in a simple array for each month.
    private static final int PAYROLL_FIRST_CUTOFF_DAYS = 0;
    private static final int PAYROLL_FIRST_CUTOFF_HOURS = 1;
    private static final int PAYROLL_FIRST_CUTOFF_GROSS = 2;
    private static final int PAYROLL_SECOND_CUTOFF_DAYS = 3;
    private static final int PAYROLL_SECOND_CUTOFF_HOURS = 4;
    private static final int PAYROLL_SECOND_CUTOFF_GROSS = 5;
    private static final int PAYROLL_SECOND_CUTOFF_NET = 6;
    private static final int PAYROLL_SSS = 7;
    private static final int PAYROLL_PHILHEALTH = 8;
    private static final int PAYROLL_PAGIBIG = 9;
    private static final int PAYROLL_TAX = 10;
    private static final int PAYROLL_TOTAL_DEDUCTIONS = 11;

    private static final List<Object[]> employeeRecords = new ArrayList<>();
    private static final Map<Integer, List<Object[]>> attendanceByEmployeeId = new HashMap<>();

    private static final String[] MONTH_NAMES = {
            "",
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December"
    };

    private static final int[] DAYS_IN_MONTH = {
            0,
            31,
            28,
            31,
            30,
            31,
            30,
            31,
            31,
            30,
            31,
            30,
            31
    };


    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("                        MOTORPH PAYROLL SYSTEM");
        System.out.println("======================================================================");

        // Load the CSV data before showing any menu so payroll calculations are ready.
        loadEmployeeData();
        loadAttendanceData();

        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("---------------------------------------------------------------------");
        System.out.println("                             LOGIN");
        System.out.println("---------------------------------------------------------------------");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (!isValidLogin(username, password)) {
            System.out.println();
            System.out.println("Invalid username or password.");
            System.out.println("Access denied.");
            scanner.close();
            return;
        }

        System.out.println();
        System.out.println("Welcome, " + username + "!");

        if ("employee".equals(username)) {
            employeeMenu(scanner);
        } else {
            payrollStaffMenu(scanner);
        }

        scanner.close();
    }

    // Read employee data once and store it in simple array-based records.
    private static void loadEmployeeData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] employeeFields = splitCsvLine(line);
                if (employeeFields.length < 14) {
                    continue;
                }

                for (int fieldIndex = 0; fieldIndex < employeeFields.length; fieldIndex++) {
                    employeeFields[fieldIndex] = employeeFields[fieldIndex].trim();
                }

                String employeeNumberText = employeeFields[0];
                if (employeeNumberText.contains(".")) {
                    employeeNumberText = employeeNumberText.substring(0, employeeNumberText.indexOf('.'));
                }
                employeeNumberText = employeeNumberText.replace(",", "");
                if (employeeNumberText.isEmpty()) {
                    continue;
                }

                int employeeId;
                try {
                    employeeId = Integer.parseInt(employeeNumberText);
                } catch (NumberFormatException e) {
                    continue;
                }

                String lastName = employeeFields[1];
                String firstName = employeeFields[2];
                String birthday = employeeFields[3];

                double monthlySalary = parseDoubleSafe(employeeFields[13].replace(",", ""));
                double hourlyRate = monthlySalary > 0 ? monthlySalary / 21.0 / 8.0 : 0.0;

                employeeRecords.add(new Object[]{
                        employeeId,
                        firstName,
                        lastName,
                        birthday,
                        monthlySalary,
                        hourlyRate
                });
            }

            if (employeeRecords.isEmpty()) {
                System.out.println("ERROR: No employee data found in CSV file");
                System.exit(1);
            }
        } catch (IOException e) {
            System.out.println("ERROR: Cannot find or read employee CSV file");
            System.out.println("Make sure 'MotorPH_Employee Data.csv' is in the Resources folder");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Read attendance records and group them by employee for faster payroll searches.
    private static void loadAttendanceData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] attendanceFields = splitCsvLine(line);
                if (attendanceFields.length < 6) {
                    continue;
                }

                for (int fieldIndex = 0; fieldIndex < attendanceFields.length; fieldIndex++) {
                    attendanceFields[fieldIndex] = attendanceFields[fieldIndex].trim();
                }

                String employeeNumberText = attendanceFields[0];
                if (employeeNumberText.contains(".")) {
                    employeeNumberText = employeeNumberText.substring(0, employeeNumberText.indexOf('.'));
                }
                if (employeeNumberText.isEmpty()) {
                    continue;
                }

                int employeeId;
                try {
                    employeeId = Integer.parseInt(employeeNumberText);
                } catch (NumberFormatException e) {
                    continue;
                }

                int[] dateParts = parseAttendanceDate(attendanceFields[3]);
                int year = dateParts[0];
                int month = dateParts[1];
                int day = dateParts[2];

                if (year == 0 || month == 0 || day == 0) {
                    continue;
                }

                Object[] attendanceRecord = new Object[]{
                        employeeId,
                        year,
                        month,
                        day,
                        normalizeTime(attendanceFields[4]),
                        normalizeTime(attendanceFields[5])
                };

                attendanceByEmployeeId
                        .computeIfAbsent(employeeId, key -> new ArrayList<>())
                        .add(attendanceRecord);
            }
        } catch (IOException e) {
            System.out.println("ERROR reading attendance CSV: " + e.getMessage());
            System.out.println("Make sure 'Attendance Record.csv' is in the Resources folder");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Split a CSV row while respecting quoted values with commas inside them.
    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean insideQuotes = false;

        for (int characterIndex = 0; characterIndex < line.length(); characterIndex++) {
            char currentCharacter = line.charAt(characterIndex);
            if (currentCharacter == '"') {
                insideQuotes = !insideQuotes;
            } else if (currentCharacter == ',' && !insideQuotes) {
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(currentCharacter);
            }
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    private static int[] parseAttendanceDate(String dateText) {
        int year = 0;
        int month = 0;
        int day = 0;

        if (dateText.contains("/")) {
            String[] dateFields = dateText.split("/");
            if (dateFields.length == 3) {
                month = parseIntSafe(dateFields[0]);
                day = parseIntSafe(dateFields[1]);
                year = parseIntSafe(dateFields[2]);
            }
        } else if (dateText.contains("-")) {
            String[] dateFields = dateText.split("-");
            if (dateFields.length == 3) {
                year = parseIntSafe(dateFields[0]);
                month = parseIntSafe(dateFields[1]);
                day = parseIntSafe(dateFields[2]);
            }
        }

        return new int[]{year, month, day};
    }

    private static String normalizeTime(String rawTime) {
        String trimmedTime = rawTime == null ? "" : rawTime.trim();
        if (trimmedTime.isEmpty()) {
            return trimmedTime;
        }
        if (!trimmedTime.contains(":")) {
            return trimmedTime + ":00";
        }
        return trimmedTime;
    }

    private static int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double parseDoubleSafe(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0.0;
        }

        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private static boolean isValidLogin(String username, String password) {
        return ("employee".equals(username) || "payroll_staff".equals(username))
                && "12345".equals(password);
    }

    private static void employeeMenu(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("---------------------------------------------------------------------");
            System.out.println("                           EMPLOYEE MENU");
            System.out.println("---------------------------------------------------------------------");
            System.out.println("1. View My Employee Profile");
            System.out.println("2. Exit");
            System.out.print("Choice: ");

            int choice = readInt(scanner);

            switch (choice) {
                case 1:
                    System.out.print("Enter your employee number: ");
                    int employeeId = readInt(scanner);
                    Object[] employeeRecord = findEmployeeById(employeeId);
                    if (employeeRecord == null) {
                        System.out.println("Employee number does not exist.");
                    } else {
                        showEmployeeDetails(employeeRecord);
                    }
                    break;
                case 2:
                    System.out.println();
                    System.out.println("Thank you for using MotorPH System.");
                    System.exit(0);
                    return;
                default:
                    System.out.println("Invalid option. Please choose 1 or 2.");
            }
        }
    }

    private static void payrollStaffMenu(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("---------------------------------------------------------------------");
            System.out.println("                         PAYROLL STAFF MENU");
            System.out.println("---------------------------------------------------------------------");
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit");
            System.out.print("Choice: ");

            int choice = readInt(scanner);

            switch (choice) {
                case 1:
                    processPayrollMenu(scanner);
                    break;
                case 2:
                    System.out.println();
                    System.out.println("Thank you for using MotorPH Payroll System.");
                    System.exit(0);
                    return;
                default:
                    System.out.println("Invalid option. Please choose 1 or 2.");
            }
        }
    }

    private static void processPayrollMenu(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("---------------------------------------------------------------------");
            System.out.println("                           PROCESS PAYROLL");
            System.out.println("---------------------------------------------------------------------");
            System.out.println("1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Exit");
            System.out.print("Choice: ");

            int choice = readInt(scanner);

            switch (choice) {
                case 1:
                    processOneEmployee(scanner);
                    break;
                case 2:
                    displayAllEmployeesPayroll();
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option. Please choose 1-3.");
            }
        }
    }

    private static void processOneEmployee(Scanner scanner) {
        System.out.print("Enter employee number: ");
        int employeeId = readInt(scanner);

        Object[] employeeRecord = findEmployeeById(employeeId);
        if (employeeRecord == null) {
            System.out.println("Employee number does not exist.");
            return;
        }

        displaySingleEmployeePayroll(employeeRecord);
    }

    private static int readInt(Scanner scanner) {
        while (true) {
            try {
                int value = scanner.nextInt();
                scanner.nextLine(); // clear newline
                return value;
            } catch (Exception e) {
                System.out.print("Please enter a number: ");
                scanner.nextLine();
            }
        }
    }

    // Search the in-memory employee list for the requested employee number.
    private static Object[] findEmployeeById(int employeeId) {
        for (Object[] employeeRecord : employeeRecords) {
            if (getEmployeeId(employeeRecord) == employeeId) {
                return employeeRecord;
            }
        }
        return null;
    }

    private static void showEmployeeDetails(Object[] employeeRecord) {
        System.out.println();
        System.out.println("======================================================================");
        System.out.println("                           EMPLOYEE PROFILE");
        System.out.println("======================================================================");
        System.out.println("Employee ID  : " + getEmployeeId(employeeRecord));
        System.out.println("Name         : " + safe(getEmployeeFirstName(employeeRecord))
                + " " + safe(getEmployeeLastName(employeeRecord)));
        System.out.println("Birthday     : " + safe(getEmployeeBirthday(employeeRecord)));
        System.out.println("======================================================================");
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void displaySingleEmployeePayroll(Object[] employeeRecord) {
        System.out.println();
        System.out.println("========================================================================");
        System.out.println("                        EMPLOYEE PAYROLL SUMMARY");
        System.out.println("========================================================================");
        System.out.println("Employee #      : " + getEmployeeId(employeeRecord));
        System.out.println("Employee Name   : " + getEmployeeFirstName(employeeRecord)
                + " " + getEmployeeLastName(employeeRecord));
        System.out.println("Birthday        : " + getEmployeeBirthday(employeeRecord));
        System.out.println("Hourly Rate     : Php " + getEmployeeHourlyRate(employeeRecord));

        boolean hasData = false;

        for (int month = FIRST_DISPLAY_MONTH; month <= LAST_DISPLAY_MONTH; month++) {
            double[] payrollSummary = calculatePayrollFor(employeeRecord, month, PAYROLL_YEAR);

            if (!hasAttendanceData(payrollSummary)) {
                continue;
            }

            hasData = true;

            int lastDay = getLastDayOfMonth(month, PAYROLL_YEAR);
            String monthName = getMonthName(month);

            System.out.println();
            System.out.println("------------------------------------------------------------------------");
            System.out.println("MONTH: " + monthName + " " + PAYROLL_YEAR);
            System.out.println("------------------------------------------------------------------------");

            if ((int) payrollSummary[PAYROLL_FIRST_CUTOFF_DAYS] > 0) {
                System.out.println("Cutoff Date         : " + monthName + " 1 to 15");
                System.out.println("Total Hours Worked  : " + payrollSummary[PAYROLL_FIRST_CUTOFF_HOURS]);
                System.out.println("Gross Salary        : Php " + payrollSummary[PAYROLL_FIRST_CUTOFF_GROSS]);
                System.out.println("Net Salary          : Php " + payrollSummary[PAYROLL_FIRST_CUTOFF_GROSS]);
            } else {
                System.out.println("Cutoff Date         : " + monthName + " 1 to 15");
                System.out.println("No days recorded for this period.");
            }

            System.out.println();

            if ((int) payrollSummary[PAYROLL_SECOND_CUTOFF_DAYS] > 0) {
                System.out.println("Cutoff Date         : " + monthName + " 16 to " + lastDay);
                System.out.println("Total Hours Worked  : " + payrollSummary[PAYROLL_SECOND_CUTOFF_HOURS]);
                System.out.println("Gross Salary        : Php " + payrollSummary[PAYROLL_SECOND_CUTOFF_GROSS]);
                System.out.println("Each Deduction:");
                System.out.println("    SSS             : Php " + payrollSummary[PAYROLL_SSS]);
                System.out.println("    PhilHealth      : Php " + payrollSummary[PAYROLL_PHILHEALTH]);
                System.out.println("    Pag-IBIG        : Php " + payrollSummary[PAYROLL_PAGIBIG]);
                System.out.println("    Tax             : Php " + payrollSummary[PAYROLL_TAX]);
                System.out.println("Total Deductions    : Php " + payrollSummary[PAYROLL_TOTAL_DEDUCTIONS]);
                System.out.println("Net Salary          : Php " + payrollSummary[PAYROLL_SECOND_CUTOFF_NET]);
            } else {
                System.out.println("Cutoff Date         : " + monthName + " 16 to " + lastDay);
                System.out.println("No days recorded for this period.");
            }
        }

        if (!hasData) {
            System.out.println();
            System.out.println("No attendance data found for this employee.");
        }

        System.out.println("========================================================================");
    }

    private static void displayAllEmployeesPayroll() {
        System.out.println();
        System.out.println("=======================================================================");
        System.out.println("                      PAYROLL SUMMARY - ALL EMPLOYEES");
        System.out.println("=======================================================================");

        for (Object[] employeeRecord : employeeRecords) {
            System.out.println();
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("EMPLOYEE: " + getEmployeeFirstName(employeeRecord) + " "
                    + getEmployeeLastName(employeeRecord) + " (ID: " + getEmployeeId(employeeRecord) + ")");
            System.out.println("Birthday: " + getEmployeeBirthday(employeeRecord));
            System.out.println("-----------------------------------------------------------------------");

            boolean hasData = false;

            for (int month = FIRST_DISPLAY_MONTH; month <= LAST_DISPLAY_MONTH; month++) {
                double[] payrollSummary = calculatePayrollFor(employeeRecord, month, PAYROLL_YEAR);

                if (!hasAttendanceData(payrollSummary)) {
                    continue;
                }

                hasData = true;

                int lastDay = getLastDayOfMonth(month, PAYROLL_YEAR);
                String monthName = getMonthName(month);

                System.out.println();
                System.out.println("  MONTH: " + monthName + " " + PAYROLL_YEAR);

                if ((int) payrollSummary[PAYROLL_FIRST_CUTOFF_DAYS] > 0) {
                    System.out.println("    First Cutoff (1-15): Hours=" + payrollSummary[PAYROLL_FIRST_CUTOFF_HOURS]
                            + ", Gross=Php " + payrollSummary[PAYROLL_FIRST_CUTOFF_GROSS]
                            + ", Net=Php " + payrollSummary[PAYROLL_FIRST_CUTOFF_GROSS]);
                }

                if ((int) payrollSummary[PAYROLL_SECOND_CUTOFF_DAYS] > 0) {
                    System.out.println("    Second Cutoff (16-" + lastDay + "): Hours="
                            + payrollSummary[PAYROLL_SECOND_CUTOFF_HOURS]
                            + ", Gross=Php " + payrollSummary[PAYROLL_SECOND_CUTOFF_GROSS]);
                    System.out.println("        Deductions: SSS=Php " + payrollSummary[PAYROLL_SSS]
                            + ", PhilHealth=Php " + payrollSummary[PAYROLL_PHILHEALTH]
                            + ", Pag-IBIG=Php " + payrollSummary[PAYROLL_PAGIBIG]
                            + ", Tax=Php " + payrollSummary[PAYROLL_TAX]);
                    System.out.println("        Total Deductions=Php "
                            + payrollSummary[PAYROLL_TOTAL_DEDUCTIONS]
                            + ", Net=Php " + payrollSummary[PAYROLL_SECOND_CUTOFF_NET]);
                }
            }

            if (!hasData) {
                System.out.println("  No attendance data found for this employee.");
            }
        }

        System.out.println();
        System.out.println("=======================================================================");
    }

    // Build monthly payroll totals from grouped attendance of one employee.
    private static double[] calculatePayrollFor(Object[] employeeRecord, int month, int year) {
        double[] payrollSummary = new double[12];
        List<Object[]> employeeAttendance = attendanceByEmployeeId.get(getEmployeeId(employeeRecord));

        if (employeeAttendance == null) {
            return payrollSummary;
        }

        for (Object[] attendanceRecord : employeeAttendance) {
            if (getAttendanceYear(attendanceRecord) != year || getAttendanceMonth(attendanceRecord) != month) {
                continue;
            }

            double hoursWorked = computeHours(
                    getAttendanceLoginTime(attendanceRecord),
                    getAttendanceLogoutTime(attendanceRecord)
            );

            if (getAttendanceDay(attendanceRecord) <= 15) {
                payrollSummary[PAYROLL_FIRST_CUTOFF_HOURS] += hoursWorked;
                payrollSummary[PAYROLL_FIRST_CUTOFF_DAYS]++;
            } else {
                payrollSummary[PAYROLL_SECOND_CUTOFF_HOURS] += hoursWorked;
                payrollSummary[PAYROLL_SECOND_CUTOFF_DAYS]++;
            }
        }

        payrollSummary[PAYROLL_FIRST_CUTOFF_GROSS] =
                payrollSummary[PAYROLL_FIRST_CUTOFF_HOURS] * getEmployeeHourlyRate(employeeRecord);
        payrollSummary[PAYROLL_SECOND_CUTOFF_GROSS] =
                payrollSummary[PAYROLL_SECOND_CUTOFF_HOURS] * getEmployeeHourlyRate(employeeRecord);

        double totalMonthlyGross = payrollSummary[PAYROLL_FIRST_CUTOFF_GROSS]
                + payrollSummary[PAYROLL_SECOND_CUTOFF_GROSS];

        if (totalMonthlyGross > 0) {
            // Deductions are based on the combined gross of both cutoffs.
            payrollSummary[PAYROLL_SSS] = computeSSS(totalMonthlyGross);
            payrollSummary[PAYROLL_PHILHEALTH] = computePhilhealth(totalMonthlyGross);
            payrollSummary[PAYROLL_PAGIBIG] = computePagibig(totalMonthlyGross);

            double governmentDeductions = payrollSummary[PAYROLL_SSS]
                    + payrollSummary[PAYROLL_PHILHEALTH]
                    + payrollSummary[PAYROLL_PAGIBIG];
            double taxableIncome = totalMonthlyGross - governmentDeductions;

            if (taxableIncome > 0) {
                payrollSummary[PAYROLL_TAX] = computeMonthlyTax(taxableIncome);
            }
        }

        payrollSummary[PAYROLL_TOTAL_DEDUCTIONS] = payrollSummary[PAYROLL_SSS]
                + payrollSummary[PAYROLL_PHILHEALTH]
                + payrollSummary[PAYROLL_PAGIBIG]
                + payrollSummary[PAYROLL_TAX];
        payrollSummary[PAYROLL_SECOND_CUTOFF_NET] =
                payrollSummary[PAYROLL_SECOND_CUTOFF_GROSS] - payrollSummary[PAYROLL_TOTAL_DEDUCTIONS];

        return payrollSummary;
    }

    private static boolean hasAttendanceData(double[] payrollSummary) {
        return (int) payrollSummary[PAYROLL_FIRST_CUTOFF_DAYS] > 0
                || (int) payrollSummary[PAYROLL_SECOND_CUTOFF_DAYS] > 0;
    }

    // Apply grace-period and lunch-break logic before computing paid hours.
    private static double computeHours(String loginStr, String logoutStr) {
        try {
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

            LocalTime login = LocalTime.parse(loginStr.trim(), timeFormat);
            LocalTime logout = LocalTime.parse(logoutStr.trim(), timeFormat);

            LocalTime workStart = LocalTime.of(8, 0);
            LocalTime graceEnd = LocalTime.of(8, 5);
            LocalTime lunchStart = LocalTime.of(12, 0);
            LocalTime lunchEnd = LocalTime.of(13, 0);
            LocalTime workEnd = LocalTime.of(17, 0);

            if (logout.isAfter(workEnd)) {
                logout = workEnd;
            }

            LocalTime effectiveLogin = login;
            if (!login.isBefore(workStart) && !login.isAfter(graceEnd)) {
                effectiveLogin = workStart;
            }

            long minutesWorked = Duration.between(effectiveLogin, logout).toMinutes();

            if (minutesWorked < 0) {
                return 0.0;
            }

            if (effectiveLogin.isBefore(lunchEnd) && logout.isAfter(lunchStart)) {
                minutesWorked -= 60;
            }

            double hours = minutesWorked / 60.0;
            return Math.min(hours, 8.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static double computeSSS(double salary) {
        if (salary < 3250) return 135.00;
        else if (salary < 3750) return 157.50;
        else if (salary < 4250) return 180.00;
        else if (salary < 4750) return 202.50;
        else if (salary < 5250) return 225.00;
        else if (salary < 5750) return 247.50;
        else if (salary < 6250) return 270.00;
        else if (salary < 6750) return 292.50;
        else if (salary < 7250) return 315.00;
        else if (salary < 7750) return 337.50;
        else if (salary < 8250) return 360.00;
        else if (salary < 8750) return 382.50;
        else if (salary < 9250) return 405.00;
        else if (salary < 9750) return 427.50;
        else if (salary < 10250) return 450.00;
        else if (salary < 10750) return 472.50;
        else if (salary < 11250) return 495.00;
        else if (salary < 11750) return 517.50;
        else if (salary < 12250) return 540.00;
        else if (salary < 12750) return 562.50;
        else if (salary < 13250) return 585.00;
        else if (salary < 13750) return 607.50;
        else if (salary < 14250) return 630.00;
        else if (salary < 14750) return 652.50;
        else if (salary < 15250) return 675.00;
        else if (salary < 15750) return 697.50;
        else if (salary < 16250) return 720.00;
        else if (salary < 16750) return 742.50;
        else if (salary < 17250) return 765.00;
        else if (salary < 17750) return 787.50;
        else if (salary < 18250) return 810.00;
        else if (salary < 18750) return 832.50;
        else if (salary < 19250) return 855.00;
        else if (salary < 19750) return 877.50;
        else if (salary < 20250) return 900.00;
        else if (salary < 20750) return 922.50;
        else if (salary < 21250) return 945.00;
        else if (salary < 21750) return 967.50;
        else if (salary < 22250) return 990.00;
        else if (salary < 22750) return 1012.50;
        else if (salary < 23250) return 1035.00;
        else if (salary < 23750) return 1057.50;
        else if (salary < 24250) return 1080.00;
        else if (salary < 24750) return 1102.50;
        else return 1125.00;
    }

    private static double computePhilhealth(double salary) {
        double premium;

        if (salary <= 10000) {
            premium = 300;
        } else if (salary >= 60000) {
            premium = 1800;
        } else {
            premium = salary * 0.03;
        }

        return premium / 2;
    }

    private static double computePagibig(double salary) {
        double contribution;

        if (salary <= 1500) {
            contribution = salary * 0.01;
        } else {
            contribution = salary * 0.02;
        }

        if (contribution > 100) {
            contribution = 100;
        }

        return contribution;
    }

    private static double computeMonthlyTax(double taxableIncome) {
        if (taxableIncome <= 20832) {
            return 0;
        } else if (taxableIncome <= 33332) {
            return (taxableIncome - 20833) * 0.20;
        } else if (taxableIncome <= 66666) {
            return 2500 + (taxableIncome - 33333) * 0.25;
        } else if (taxableIncome <= 166666) {
            return 10833 + (taxableIncome - 66667) * 0.30;
        } else if (taxableIncome <= 666666) {
            return 40833.33 + (taxableIncome - 166667) * 0.32;
        } else {
            return 200833.33 + (taxableIncome - 666667) * 0.35;
        }
    }

    private static String getMonthName(int month) {
        if (month < 1 || month > 12) {
            return "Unknown";
        }
        return MONTH_NAMES[month];
    }

    private static int getLastDayOfMonth(int month, int year) {
        if (month < 1 || month > 12) {
            return 30;
        }
        if (month == 2) {
            return 28;
        }
        return DAYS_IN_MONTH[month];
    }

    private static int getEmployeeId(Object[] employeeRecord) {
        return (int) employeeRecord[EMPLOYEE_ID];
    }

    private static String getEmployeeFirstName(Object[] employeeRecord) {
        return (String) employeeRecord[EMPLOYEE_FIRST_NAME];
    }

    private static String getEmployeeLastName(Object[] employeeRecord) {
        return (String) employeeRecord[EMPLOYEE_LAST_NAME];
    }

    private static String getEmployeeBirthday(Object[] employeeRecord) {
        return (String) employeeRecord[EMPLOYEE_BIRTHDAY];
    }

    private static double getEmployeeHourlyRate(Object[] employeeRecord) {
        return (double) employeeRecord[EMPLOYEE_HOURLY_RATE];
    }

    private static int getAttendanceYear(Object[] attendanceRecord) {
        return (int) attendanceRecord[ATTENDANCE_YEAR];
    }

    private static int getAttendanceMonth(Object[] attendanceRecord) {
        return (int) attendanceRecord[ATTENDANCE_MONTH];
    }

    private static int getAttendanceDay(Object[] attendanceRecord) {
        return (int) attendanceRecord[ATTENDANCE_DAY];
    }

    private static String getAttendanceLoginTime(Object[] attendanceRecord) {
        return (String) attendanceRecord[ATTENDANCE_LOGIN_TIME];
    }

    private static String getAttendanceLogoutTime(Object[] attendanceRecord) {
        return (String) attendanceRecord[ATTENDANCE_LOGOUT_TIME];
    }
}
