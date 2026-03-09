/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.motorph.ms2motorph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
 
public class MOIT101Group6 {

   
    private static final String EMPLOYEE_FILE = "Resources/MotorPH_Employee Data.csv";
    private static final String ATTENDANCE_FILE = "Resources/Attendance Record.csv";

    private static final List<Employee> employees = new ArrayList<>();
    private static final List<AttendanceRecord> attendanceRecords = new ArrayList<>();

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
            31, // January
            28, // February (no leap year handling needed for 2024 logic here)
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

    private static class Employee {
        int id;
        String firstName;
        String lastName;
        String birthday;
        double monthlySalary;
        double hourlyRate;

        Employee(int id,
                 String firstName,
                 String lastName,
                 String birthday,
                 double monthlySalary,
                 double hourlyRate) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthday = birthday;
            this.monthlySalary = monthlySalary;
            this.hourlyRate = hourlyRate;
        }
    }

    private static class AttendanceRecord {
        int employeeId;
        int year;
        int month;
        int day;
        String loginTime;
        String logoutTime;

        AttendanceRecord(int employeeId,
                         int year,
                         int month,
                         int day,
                         String loginTime,
                         String logoutTime) {
            this.employeeId = employeeId;
            this.year = year;
            this.month = month;
            this.day = day;
            this.loginTime = loginTime;
            this.logoutTime = logoutTime;
        }
    }

    private static class PayrollForMonth {
        int firstCutoffDays;
        double firstCutoffHours;
        double firstCutoffGross;

        int secondCutoffDays;
        double secondCutoffHours;
        double secondCutoffGross;
        double secondCutoffNet;

        double sss;
        double philhealth;
        double pagibig;
        double tax;
        double totalDeductions;

        PayrollForMonth(int firstCutoffDays,
                        double firstCutoffHours,
                        double firstCutoffGross,
                        int secondCutoffDays,
                        double secondCutoffHours,
                        double secondCutoffGross,
                        double secondCutoffNet,
                        double sss,
                        double philhealth,
                        double pagibig,
                        double tax,
                        double totalDeductions) {
            this.firstCutoffDays = firstCutoffDays;
            this.firstCutoffHours = firstCutoffHours;
            this.firstCutoffGross = firstCutoffGross;
            this.secondCutoffDays = secondCutoffDays;
            this.secondCutoffHours = secondCutoffHours;
            this.secondCutoffGross = secondCutoffGross;
            this.secondCutoffNet = secondCutoffNet;
            this.sss = sss;
            this.philhealth = philhealth;
            this.pagibig = pagibig;
            this.tax = tax;
            this.totalDeductions = totalDeductions;
        }
    }

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("                        MOTORPH PAYROLL SYSTEM");
        System.out.println("======================================================================");

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

    private static void loadEmployeeData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEE_FILE))) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = splitCsvLine(line);
                if (columns.length < 14) {
                    continue;
                }

                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i].trim();
                }

                String idText = columns[0];
                if (idText.contains(".")) {
                    idText = idText.substring(0, idText.indexOf('.'));
                }
                idText = idText.replace(",", "");
                if (idText.isEmpty()) {
                    continue;
                }

                int id;
                try {
                    id = Integer.parseInt(idText);
                } catch (NumberFormatException e) {
                    continue;
                }

                String lastName = columns[1];
                String firstName = columns[2];
                String birthday = columns[3];

                String salaryText = columns[13].replace(",", "");
                double monthlySalary = 0.0;
                if (!salaryText.isEmpty()) {
                    try {
                        monthlySalary = Double.parseDouble(salaryText);
                    } catch (NumberFormatException e) {
                        monthlySalary = 0.0;
                    }
                }

                double hourlyRate = 0.0;
                if (monthlySalary > 0) {
                    hourlyRate = monthlySalary / 21.0 / 8.0;
                }

                employees.add(new Employee(id, firstName, lastName, birthday, monthlySalary, hourlyRate));
            }

            if (employees.isEmpty()) {
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

    private static void loadAttendanceData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ATTENDANCE_FILE))) {
            // Skip header
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = splitCsvLine(line);
                if (columns.length < 6) {
                    continue;
                }

                for (int i = 0; i < columns.length; i++) {
                    columns[i] = columns[i].trim();
                }

                String idText = columns[0];
                if (idText.contains(".")) {
                    idText = idText.substring(0, idText.indexOf('.'));
                }
                if (idText.isEmpty()) {
                    continue;
                }

                int id;
                try {
                    id = Integer.parseInt(idText);
                } catch (NumberFormatException e) {
                    continue;
                }

                String dateText = columns[3];
                int year;
                int month;
                int day;

                if (dateText.contains("/")) {
                    String[] parts = dateText.split("/");
                    if (parts.length != 3) {
                        continue;
                    }
                    month = parseIntSafe(parts[0]);
                    day = parseIntSafe(parts[1]);
                    year = parseIntSafe(parts[2]);
                } else if (dateText.contains("-")) {
                    String[] parts = dateText.split("-");
                    if (parts.length != 3) {
                        continue;
                    }
                    year = parseIntSafe(parts[0]);
                    month = parseIntSafe(parts[1]);
                    day = parseIntSafe(parts[2]);
                } else {
                    continue;
                }

                if (year == 0 || month == 0 || day == 0) {
                    continue;
                }

                String login = normalizeTime(columns[4]);
                String logout = normalizeTime(columns[5]);

                attendanceRecords.add(new AttendanceRecord(id, year, month, day, login, logout));
            }
        } catch (IOException e) {
            System.out.println("ERROR reading attendance CSV: " + e.getMessage());
            System.out.println("Make sure 'Attendance Record.csv' is in the Resources folder");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    private static String normalizeTime(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        if (!trimmed.contains(":")) {
            return trimmed + ":00";
        }
        return trimmed;
    }

    private static int parseIntSafe(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
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
                    int empId = readInt(scanner);
                    Employee employee = findEmployeeById(empId);
                    if (employee == null) {
                        System.out.println("Employee number does not exist.");
                    } else {
                        showEmployeeDetails(employee);
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
        int empId = readInt(scanner);

        Employee employee = findEmployeeById(empId);
        if (employee == null) {
            System.out.println("Employee number does not exist.");
            return;
        }

        displaySingleEmployeePayroll(employee);
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

    private static Employee findEmployeeById(int id) {
        for (Employee employee : employees) {
            if (employee.id == id) {
                return employee;
            }
        }
        return null;
    }

    private static void showEmployeeDetails(Employee employee) {
        System.out.println();
        System.out.println("======================================================================");
        System.out.println("                           EMPLOYEE PROFILE");
        System.out.println("======================================================================");
        System.out.println("Employee ID  : " + employee.id);
        System.out.println("Name         : " + safe(employee.firstName) + " " + safe(employee.lastName));
        System.out.println("Birthday     : " + safe(employee.birthday));
        System.out.println("======================================================================");
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void displaySingleEmployeePayroll(Employee employee) {
        System.out.println();
        System.out.println("========================================================================");
        System.out.println("                        EMPLOYEE PAYROLL SUMMARY");
        System.out.println("========================================================================");
        System.out.println("Employee #      : " + employee.id);
        System.out.println("Employee Name   : " + employee.firstName + " " + employee.lastName);
        System.out.println("Birthday        : " + employee.birthday);
        System.out.println("Hourly Rate     : Php " + employee.hourlyRate);

        boolean hasData = false;

        for (int month = 1; month <= 12; month++) {
            int year = 2024;
            PayrollForMonth payroll = calculatePayrollFor(employee, month, year);

            if (payroll.firstCutoffDays == 0 && payroll.secondCutoffDays == 0) {
                continue;
            }

            hasData = true;

            int lastDay = getLastDayOfMonth(month, year);
            String monthName = getMonthName(month);

            System.out.println();
            System.out.println("------------------------------------------------------------------------");
            System.out.println("MONTH: " + monthName + " " + year);
            System.out.println("------------------------------------------------------------------------");

            if (payroll.firstCutoffDays > 0) {
                System.out.println("Cutoff Date         : " + monthName + " 1 to 15");
                System.out.println("Total Hours Worked  : " + payroll.firstCutoffHours);
                System.out.println("Gross Salary        : Php " + payroll.firstCutoffGross);
                System.out.println("Net Salary          : Php " + payroll.firstCutoffGross);
            } else {
                System.out.println("Cutoff Date         : " + monthName + " 1 to 15");
                System.out.println("No days recorded for this period.");
            }

            System.out.println();

            if (payroll.secondCutoffDays > 0) {
                System.out.println("Cutoff Date         : " + monthName + " 16 to " + lastDay);
                System.out.println("Total Hours Worked  : " + payroll.secondCutoffHours);
                System.out.println("Gross Salary        : Php " + payroll.secondCutoffGross);
                System.out.println("Each Deduction:");
                System.out.println("    SSS             : Php " + payroll.sss);
                System.out.println("    PhilHealth      : Php " + payroll.philhealth);
                System.out.println("    Pag-IBIG        : Php " + payroll.pagibig);
                System.out.println("    Tax             : Php " + payroll.tax);
                System.out.println("Total Deductions    : Php " + payroll.totalDeductions);
                System.out.println("Net Salary          : Php " + payroll.secondCutoffNet);
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

        for (Employee employee : employees) {
            System.out.println();
            System.out.println("-----------------------------------------------------------------------");
            System.out.println("EMPLOYEE: " + employee.firstName + " " + employee.lastName + " (ID: " + employee.id + ")");
            System.out.println("Birthday: " + employee.birthday);
            System.out.println("-----------------------------------------------------------------------");

            boolean hasData = false;

            for (int month = 1; month <= 12; month++) {
                int year = 2024;
                PayrollForMonth payroll = calculatePayrollFor(employee, month, year);

                if (payroll.firstCutoffDays == 0 && payroll.secondCutoffDays == 0) {
                    continue;
                }

                hasData = true;

                int lastDay = getLastDayOfMonth(month, year);
                String monthName = getMonthName(month);

                System.out.println();
                System.out.println("  MONTH: " + monthName + " " + year);

                if (payroll.firstCutoffDays > 0) {
                    System.out.println("    First Cutoff (1-15): Hours=" + payroll.firstCutoffHours
                            + ", Gross=Php " + payroll.firstCutoffGross
                            + ", Net=Php " + payroll.firstCutoffGross);
                }

                if (payroll.secondCutoffDays > 0) {
                    System.out.println("    Second Cutoff (16-" + lastDay + "): Hours=" + payroll.secondCutoffHours
                            + ", Gross=Php " + payroll.secondCutoffGross);
                    System.out.println("        Deductions: SSS=Php " + payroll.sss
                            + ", PhilHealth=Php " + payroll.philhealth
                            + ", Pag-IBIG=Php " + payroll.pagibig
                            + ", Tax=Php " + payroll.tax);
                    System.out.println("        Total Deductions=Php " + payroll.totalDeductions
                            + ", Net=Php " + payroll.secondCutoffNet);
                }
            }

            if (!hasData) {
                System.out.println("  No attendance data found for this employee.");
            }
        }

        System.out.println();
        System.out.println("=======================================================================");
    }

    private static PayrollForMonth calculatePayrollFor(Employee employee, int month, int year) {
        double firstCutoffHours = 0.0;
        int firstCutoffDays = 0;
        double secondCutoffHours = 0.0;
        int secondCutoffDays = 0;

        for (AttendanceRecord record : attendanceRecords) {
            if (record.employeeId != employee.id) {
                continue;
            }
            if (record.year != year || record.month != month) {
                continue;
            }

            double hours = computeHours(record.loginTime, record.logoutTime);

            if (record.day <= 15) {
                firstCutoffHours += hours;
                firstCutoffDays++;
            } else {
                secondCutoffHours += hours;
                secondCutoffDays++;
            }
        }

        double firstCutoffGross = firstCutoffHours * employee.hourlyRate;
        double secondCutoffGross = secondCutoffHours * employee.hourlyRate;
        double totalMonthlyGross = firstCutoffGross + secondCutoffGross;

        double sss = 0.0;
        double philhealth = 0.0;
        double pagibig = 0.0;
        double tax = 0.0;

        if (totalMonthlyGross > 0) {
            sss = computeSSS(employee.monthlySalary);
            philhealth = computePhilhealth(employee.monthlySalary);
            pagibig = computePagibig(employee.monthlySalary);

            double governmentDeductions = sss + philhealth + pagibig;
            double taxableIncome = totalMonthlyGross - governmentDeductions;

            if (taxableIncome > 0) {
                tax = computeMonthlyTax(taxableIncome);
            }
        }

        double totalDeductions = sss + philhealth + pagibig + tax;
        double secondCutoffNet = secondCutoffGross - totalDeductions;

        return new PayrollForMonth(
                firstCutoffDays,
                firstCutoffHours,
                firstCutoffGross,
                secondCutoffDays,
                secondCutoffHours,
                secondCutoffGross,
                secondCutoffNet,
                sss,
                philhealth,
                pagibig,
                tax,
                totalDeductions
        );
    }

    private static double computeHours(String loginStr, String logoutStr) {
        try {
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");

            LocalTime login = LocalTime.parse(loginStr.trim(), timeFormat);
            LocalTime logout = LocalTime.parse(logoutStr.trim(), timeFormat);

            LocalTime workStart = LocalTime.of(8, 0);
            LocalTime graceEnd = LocalTime.of(8, 10);
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

    private static boolean isLate(String login) {
        if (login == null || login.isEmpty()) {
            return false;
        }

        try {
            String[] parts = login.split(":");
            if (parts.length < 2) {
                return false;
            }

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            int totalMinutes = hour * 60 + minute;
            int graceEnd = 8 * 60 + 10;

            return totalMinutes > graceEnd;
        } catch (Exception e) {
            return false;
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
}
