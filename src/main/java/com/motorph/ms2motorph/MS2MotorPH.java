/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.motorph.ms2motorph;


import java.io.BufferedReader;      // For reading files efficiently
import java.io.FileReader;          // For opening files
import java.util.Scanner;           // For getting input from user
import java.util.ArrayList;         // For creating flexible lists
import java.util.List;              // For working with lists
import java.time.Duration;          // For calculating time differences
import java.time.LocalTime;         // For working with time values
import java.time.format.DateTimeFormatter;  // For formatting time

public class MS2MotorPH {
    
    // These are arrays that will store employee information
    static int[] empIds;              // Will store employee ID numbers
    static String[] firstNames;        // Will store first names
    static String[] lastNames;         // Will store last names
    static String[] birthdays;         // Will store birth dates
    static double[] basicSalaries;     // Will store salary amounts
    static double[] hourlyRates;       // Will store how much they earn per hour
    
    // MAX_ATTENDANCE is a constant (final means it can't be changed)
    // Set to 6000 since attendance logs have around 5800+ entries
    static final int MAX_ATTENDANCE = 6000;
    
    // These arrays will store attendance data
    static int[] attEmpIds = new int[MAX_ATTENDANCE];        // Employee IDs from attendance
    static String[] attDates = new String[MAX_ATTENDANCE];    // Dates they worked
    static String[] attLogins = new String[MAX_ATTENDANCE];   // Time they clocked in
    static String[] attLogouts = new String[MAX_ATTENDANCE];  // Time they clocked out
   
    static int attendanceCount = 0;  // Counter for how many attendance records we have
    
    /**
     * This method reads a line from a CSV file and splits it into parts
     * It handles quotes specially because sometimes data has commas inside quotes
     */
    static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();  // Create a list to store the parts
        StringBuilder currentField = new StringBuilder();  // Build each part character by character
        boolean inQuotes = false;  // Flag to track if we're inside quotes
        
        // Loop through each character in the line
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            // If we find a quote, switch between inside/outside quotes mode
            if (c == '"') {
                inQuotes = !inQuotes;
            } 
            // If we find a comma and we're NOT inside quotes, that's the end of a field
            else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());  // Save the current field
                currentField = new StringBuilder();    // Start a new field
            } 
            // Otherwise, just add the character to the current field
            else {
                currentField.append(c);
            }
        }
        
        // Add the last field (after the loop ends)
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);  // Convert list to array and return
    }
    
    /**
     * This method loads all employee data from the CSV file
     * It reads the file twice: first to count rows, then to actually load the data
     */
    static void loadEmployeeData() {
        
        
        String empFile = "Resources/MotorPH_Employee Data.csv";  // File path
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(empFile));
            reader.readLine();  // Skip the header row (first line with column names)
            
            int rowCount = 0;
            String line;
            
            // First pass: count how many data rows there are
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {  // Skip empty lines
                    rowCount++;
                }
            }
            
            reader.close();  // Close the file after counting
            
            // If no data found, show error and exit
            if (rowCount == 0) {
                System.out.println("ERROR: No employee data found in CSV file");
                System.exit(1);
                return;
            }
            
            // Now we know how many rows, create arrays with that size
            empIds = new int[rowCount];
            firstNames = new String[rowCount];
            lastNames = new String[rowCount];
            birthdays = new String[rowCount];
            basicSalaries = new double[rowCount];
            hourlyRates = new double[rowCount];
            
            // Second pass: actually read the data
            reader = new BufferedReader(new FileReader(empFile));
            reader.readLine();  // Skip header again
            
            int index = 0;
            // Read each line until we run out or fill all arrays
            while ((line = reader.readLine()) != null && index < rowCount) {
                if (line.trim().isEmpty()) continue;  // Skip empty lines
                
                String[] data = parseCSVLine(line);  // Split the line into parts
                
                if (data.length < 14) continue;  // Skip if not enough columns
                // data[0] is Employee ID
                // data[1] is Last Name
                // data[2] is First Name
                // data[3] is Birthday
                // ... up to data[13] which is Basic Salary.data 13 is the 14th item (0-13)
                try {
                    // Clean up the data by removing extra spaces
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i].trim();
                    }
                    
                    // Handle employee ID (remove decimal point if present)
                    /** This is implemented because we encounter a problem upon running the program that it reads the employee ID data having a decimal points.
                     * Before only the 10001 is read, implementing this make sure all employee ID is read correctly.
                     */
                    String idStr = data[0];
                    if (idStr.contains(".")) {
                        idStr = idStr.substring(0, idStr.indexOf(".")); // Take everything before the dot
                    }
                    idStr = idStr.replace(",", ""); // Remove any commas
                    
                    if (!idStr.isEmpty()) {
                        empIds[index] = Integer.parseInt(idStr);  // Convert string to integer
                    } else {
                        continue; // Skip this row if no ID
                    }
                    
                    // Store the data in arrays
                    lastNames[index] = data[1]; // Second column is last name
                    firstNames[index] = data[2]; // Third column is first name
                    birthdays[index] = data[3]; // Third column is first name
                    
                    // Handle salary (remove commas and convert to number)
                    String salaryStr = data[13].replace(",", ""); // 14th column is salary (index 13)
                    if (!salaryStr.isEmpty()) {
                        basicSalaries[index] = Double.parseDouble(salaryStr); // Convert to decimal number
                    }
                    
                    // Calculate hourly rate: salary / 21 working days / 8 hours per day
                    if (basicSalaries[index] > 0) {
                        hourlyRates[index] = basicSalaries[index] / 21 / 8;
                    }
                    
                    index++;  // Move to next array position
                    
                } catch (Exception e) {
                    // If something goes wrong with this row, just skip it
                }
            }
            
            reader.close();  // Close the file
            
           
            
        } catch (Exception e) {
            // If file can't be found or read
            System.out.println("ERROR: Cannot find or read employee CSV file");
            System.out.println("Make sure 'MotorPH_Employee Data.csv' is in the Resources folder");
            e.printStackTrace();
            System.exit(1);  // Exit program with error
        }
    }
    
    /**
     * This method loads all attendance data from the CSV file
     * Similar to loadEmployeeData but for attendance records
     */
    static void loadAttendanceData() {
        
        
        String attFile = "Resources/Attendance Record.csv";  // File path for attendance
        
        attendanceCount = 0;  // Reset counter to 0 before loading
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(attFile));
            reader.readLine();  // Skip header row
            
            String line;
            
            // Read each line until we run out or hit the maximum 6000
            while ((line = reader.readLine()) != null && attendanceCount < MAX_ATTENDANCE) {
                if (line.trim().isEmpty()) continue;  // Skip empty lines
                
                String[] data = parseCSVLine(line);  // Split the line
                // data[0] is Employee ID
                // data[1] is Last Name (we don't actually store this as this already reflected from employee data)
                // data[2] is First Name (we don't store this either)
                // data[3] is Date
                // data[4] is Login Time
                // data[5] is Logout Time
                if (data.length < 6) continue;  // Skip if not enough columns
                
                try {
                    // Clean up the data
                    for (int i = 0; i < data.length; i++) {
                        data[i] = data[i].trim();
                    }
                    
                    // Handle employee ID (remove decimal point)
                    String empIdStr = data[0];
                    if (empIdStr.contains(".")) {
                        empIdStr = empIdStr.substring(0, empIdStr.indexOf("."));
                    }
                    attEmpIds[attendanceCount] = Integer.parseInt(empIdStr);
                    
                    // Handle date format - convert to YYYY-MM-DD format
                    String dateStr = data[3];
                    if (dateStr.contains("/")) { // Check if it's using slash format
                        String[] dateParts = dateStr.split("/"); // Split by slash
                        // Handle date format - convert to YYYY-MM-DD format for consistency
                        // since CSV format is in "06/03/2024" (MM/DD/YYYY
                        if (dateParts.length == 3) {
                            // Convert from MM/DD/YYYY to YYYY-MM-DD
                            attDates[attendanceCount] = dateParts[2] + "-" + dateParts[0] + "-" + dateParts[1];
                        } else {
                            attDates[attendanceCount] = dateStr; // Keep as is if format is different
                        }
                    } else {
                        attDates[attendanceCount] = dateStr; 
                    }
                    
                    // Handle login time - add :00 if missing seconds
                    String loginStr = data[4];
                    if (!loginStr.contains(":")) {
                        loginStr = loginStr + ":00"; // Add seconds
                    }
                    attLogins[attendanceCount] = loginStr;
                    
                    // Handle logout time similarly
                    String logoutStr = data[5];
                    if (!logoutStr.contains(":")) {
                        logoutStr = logoutStr + ":00";
                    }
                    attLogouts[attendanceCount] = logoutStr;
                    
                    attendanceCount++;  // Increment counter
                    
                } catch (Exception e) {
                    // Skip problematic rows
                }
            }
            
            reader.close();  // Close file
            
           
            
        } catch (Exception e) {
            System.out.println("ERROR reading attendance CSV: " + e.getMessage());
            System.out.println("Make sure 'Attendance Record.csv' is in the Resources folder");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Calculates the number of hours worked based on login and logout times
     * Also handles:
     * - Grace period (if they arrive by 8:10, count as starting at 8:00)
     * - Lunch break deduction (1 hour unpaid if they work through lunch)
     * - Cannot exceed 8 hours per day
     */
    static double computeHours(String loginStr, String logoutStr) {
        try {
            DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("H:mm");  // Format like "8:30"
            
            // Convert strings to time objects
            LocalTime login = LocalTime.parse(loginStr.trim(), timeFormat);
            LocalTime logout = LocalTime.parse(logoutStr.trim(), timeFormat);
            
            // Define important times
            LocalTime workStart = LocalTime.of(8, 0);      // 8:00 AM
            LocalTime graceEnd = LocalTime.of(8, 10);      // 8:10 AM (grace period ends)
            LocalTime lunchStart = LocalTime.of(12, 0);    // 12:00 NN (lunch starts)
            LocalTime lunchEnd = LocalTime.of(13, 0);      // 1:00 PM  (end of lunch)
            LocalTime workEnd = LocalTime.of(17, 0);       // 5:00 PM  (end of work)
            
            // If they logged out after work ends, count only until 5:00
            if (logout.isAfter(workEnd)) {
                logout = workEnd;
            }
            
            // Apply grace period, if they arrived by 8:10, count as 8:00 start
            LocalTime effectiveLogin = login;
            // If they arrived during grace period, count them as on time
            // Treat it as if they arrived at exactly 8:00
            if (!login.isBefore(workStart) && !login.isAfter(graceEnd)) {
                effectiveLogin = workStart;
            }
            
            // Calculate minutes between login and logout
            // ".toMinutes()" converts that difference to minutes
            // This tells us total minutes between start and end
            long minutesWorked = Duration.between(effectiveLogin, logout).toMinutes();
            
            // If negative (logout before login), return 0
            if (minutesWorked < 0) {
                return 0;
            }
            
            // Subtract 60 minutes for lunch if they worked through lunch
            if (effectiveLogin.isBefore(lunchEnd) && logout.isAfter(lunchStart)) {
                minutesWorked -= 60;
            }
            
            double hours = minutesWorked / 60.0;  // Convert minutes to hours
            
            return Math.min(hours, 8.0);  // Can't exceed 8 hours
            
        } catch (Exception e) {
            return 0;  // If any error, return 0 hours
        }
    }
    
    /**
     * Calculates SSS contribution based on salary
     * Uses the SSS contribution table with brackets
     */
    static double computeSSS(double salary) {
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
        else return 1125.00;  // Highest bracket
    }
    
    /**
     * Calculates PhilHealth contribution
     * Premium is 3% of salary, split equally between employee and employer
     * Minimum premium: 300, Maximum: 1800
     */
    static double computePhilhealth(double salary) {
        double premium;
        
        if (salary <= 10000) {
            premium = 300;  // Minimum
        } else if (salary >= 60000) {
            premium = 1800;  // Maximum
        } else {
            premium = salary * 0.03;  // 3% of salary
        }
        
        return premium / 2;  // Employee pays half
    }
    
    /**
     * Calculates Pag-IBIG contribution
     * 1% if salary <= 1500, 2% if higher
     * Maximum contribution is 100
     */
    static double computePagibig(double salary) {
        double contribution;
        
        if (salary <= 1500) {
            contribution = salary * 0.01;  // 1%
        } else {
            contribution = salary * 0.02;  // 2%
        }
        
        if (contribution > 100) {
            contribution = 100;  // Cap at 100
        }
        
        return contribution;
    }

    /**
     * Calculates monthly withholding tax based on taxable income
     * Uses the graduated tax table (2024 rates)
     */
    static double computeMonthlyTax(double taxableIncome) {
        if (taxableIncome <= 20832) {
            return 0;  // No tax
        } 
        else if (taxableIncome <= 33332) {
            return (taxableIncome - 20833) * 0.20;  // 20% over 20,833
        } 
        else if (taxableIncome <= 66666) {
            return 2500 + (taxableIncome - 33333) * 0.25;  // 2,500 + 25% over 33,333
        } 
        else if (taxableIncome <= 166666) {
            return 10833 + (taxableIncome - 66667) * 0.30;  // 10,833 + 30% over 66,667
        } 
        else if (taxableIncome <= 666666) {
            return 40833.33 + (taxableIncome - 166667) * 0.32;  // 40,833.33 + 32% over 166,667
        } 
        else {
            return 200833.33 + (taxableIncome - 666667) * 0.35;  // 200,833.33 + 35% over 666,667
        }
    }
    
    /**
     * Finds an employee in the array by their ID
     * Returns the index position or -1 if not found
     */
    static int findEmployeeIndex(int id) {
        // Loop through all employee IDs
        for (int i = 0; i < empIds.length; i++) {
            // Check if current employee ID matches the one we're looking for
            if (empIds[i] == id) {
                return i;  // Found, return position
            }
        }
        return -1;  // Not found
    }
    
    /**
     * Checks if an employee was late (after 8:10 AM)
     */
    // Takes login time, returns true if late, false if on time
    static boolean isLate(String login) {
         // If login is null (doesn't exist) or empty (no text)
        // Return false (not late)
        if (login == null || login.isEmpty()) return false;
        
        // Try to parse the time
        try {
            // Split the time string by colon
            // "8:30" becomes ["8", "30"]
            String[] timeParts = login.split(":");
            
            // If we don't get at least 2 parts (hours and minutes)
            // Invalid time format, so assume not late
            if (timeParts.length < 2) return false;
            
            
            int loginHour = Integer.parseInt(timeParts[0]); // Convert the hour part to an integer
            int loginMinute = Integer.parseInt(timeParts[1]); // Convert the minute part to an integer
            
            int loginMinutes = loginHour * 60 + loginMinute;  // Convert to minutes since midnight Example: 8:30 = 8*60 + 30 = 480 + 30 = 510 minutes
            int graceEnd = 8 * 60 + 10;  // 8:10 in minutes - Grace period ends at 8:10
            
            return loginMinutes > graceEnd;  // True if after grace period
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Converts month number to month name
     */
    static String getMonthName(int month) {
        if (month == 1) return "January";
        if (month == 2) return "February";
        if (month == 3) return "March";
        if (month == 4) return "April";
        if (month == 5) return "May";
        if (month == 6) return "June";
        if (month == 7) return "July";
        if (month == 8) return "August";
        if (month == 9) return "September";
        if (month == 10) return "October";
        if (month == 11) return "November";
        if (month == 12) return "December";
        return "Unknown";
    }
    
    /**
     * Gets the last day of a month
     */
    static int getLastDayOfMonth(int month, int year) {
    
    // April, June, September, November have 30 days
    if (month == 4 || month == 6 || month == 9 || month == 11) {
        return 30;
    } 
    // February
    else if (month == 2) {
        return 28;
    }
    // All other months (Jan, Mar, May, July, Aug, Oct, Dec) have 31
    else {
        return 31;
    }
}
    
    /**
     * Validates login credentials
     * Only two valid users: "employee" and "payroll_staff", both with password "12345"
     */
    static boolean isValidLogin(String username, String password) {
        return (username.equals("employee") || username.equals("payroll_staff")) && password.equals("12345");
    }
    
    /**
     * Displays employee profile information
     */
    static void showEmployeeDetails(int index) {
       
        System.out.println("                         EMPLOYEE PROFILE");
        System.out.println("-".repeat(70));
        
        System.out.println("Employee ID  : " + empIds[index]);
        System.out.println("Name         : " + 
            (firstNames[index] != null ? firstNames[index] : "") + " " + 
            (lastNames[index] != null ? lastNames[index] : ""));
        System.out.println("Birthday     : " + (birthdays[index] != null ? birthdays[index] : ""));
        System.out.println("-".repeat(70));
    }
    
    /**
     * Menu for regular employees
     * They can only view their own profile
     */
    static void employeeMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n" + "-".repeat(70));
            System.out.println("                         EMPLOYEE MENU");
            System.out.println("-".repeat(70));
            System.out.println("1. View My Employee Profile");
            System.out.println("2. Exit");
            System.out.print("Choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();  // Clear the newline character
            } catch (Exception e) {
                System.out.println("Please enter a number.");
                scanner.nextLine();
                continue;
            }
            
            if (choice == 2) {
                System.out.println("\nThank you for using MotorPH System.");
                System.exit(0);  // Exit program
            } else if (choice == 1) {
                System.out.print("\nEnter your employee number: ");
                int empId;
                try {
                    empId = scanner.nextInt();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Please enter a number.");
                    scanner.nextLine();
                    continue;
                }
                
                int index = findEmployeeIndex(empId);
                if (index == -1) {
                    System.out.println("\nEmployee number does not exist.");
                } else {
                    showEmployeeDetails(index);
                }
            } else {
                System.out.println("Invalid option. Please choose 1 or 2.");
            }
        }
    }
    
    /**
     * Calculates payroll for a specific employee and month
     * Returns a 2D array with first and second cutoff data
     */
    static Object[][] calculateEmployeePayroll(int empIndex, int month, int year) {
        double hourlyRate = basicSalaries[empIndex] / 21.0 / 8.0;
        double monthlySalary = basicSalaries[empIndex];
        
        // Variables for first cutoff (days 1-15)
        double firstCutoffHours = 0;
        int firstCutoffDays = 0;
        
        // Variables for second cutoff (days 16-end)
        double secondCutoffHours = 0;
        int secondCutoffDays = 0;
        
        // Loop through all attendance records
        for (int i = 0; i < attendanceCount; i++) {
            if (attEmpIds[i] != empIds[empIndex]) continue;  // Skip if not this employee
            
            String date = attDates[i];
            if (date == null) continue;
            
            // Parse the date
            String[] dateParts = date.split("-");
            if (dateParts.length < 3) continue;
            
            int recordYear = Integer.parseInt(dateParts[0]);
            int recordMonth = Integer.parseInt(dateParts[1]);
            int recordDay = Integer.parseInt(dateParts[2]);
            
            // Skip if not the month we want
            if (recordYear != year || recordMonth != month) continue;
            
            double hours = computeHours(attLogins[i], attLogouts[i]);
            
            // Check which cutoff period
            if (recordDay <= 15) {
                firstCutoffHours += hours;
                firstCutoffDays++;
            } else {
                secondCutoffHours += hours;
                secondCutoffDays++;
            }
        }
        
        // Calculate gross pay for each cutoff
        double firstCutoffGross = firstCutoffHours * hourlyRate;
        double secondCutoffGross = secondCutoffHours * hourlyRate;
        
        double totalMonthlyGross = firstCutoffGross + secondCutoffGross;
        
        // Calculate deductions (only if they worked at all)
        double sss = 0, philhealth = 0, pagibig = 0, tax = 0;
        
        if (totalMonthlyGross > 0) {
            sss = computeSSS(monthlySalary);
            philhealth = computePhilhealth(monthlySalary);
            pagibig = computePagibig(monthlySalary);
            
            double totalGovDeductions = sss + philhealth + pagibig;
            double taxableIncome = totalMonthlyGross - totalGovDeductions;
            
            if (taxableIncome > 0) {
                tax = computeMonthlyTax(taxableIncome);
            } else {
                tax = 0;
            }
        }
        
        double totalDeductions = sss + philhealth + pagibig + tax;
        
        // Net pay for second cutoff (deductions are taken from second cutoff)
        double secondCutoffNet = secondCutoffGross - totalDeductions;
        
        // Return both cutoff data in a 2D array
        return new Object[][] {
            {firstCutoffDays, firstCutoffHours, firstCutoffGross},
            {secondCutoffDays, secondCutoffHours, secondCutoffGross, secondCutoffNet,
             sss, philhealth, pagibig, tax, totalDeductions, totalMonthlyGross}
        };
    }
    
    /**
     * Displays detailed payroll for a single employee
     */
    static void displaySingleEmployeePayroll(int empIndex) {
        System.out.println("\n" + "-".repeat(80));
        System.out.println("                         EMPLOYEE PAYROLL SUMMARY");
        System.out.println("-".repeat(80));
        
        System.out.println("Employee #      : " + empIds[empIndex]);
        System.out.println("Employee Name   : " + firstNames[empIndex] + " " + lastNames[empIndex]);
        System.out.println("Birthday        : " + birthdays[empIndex]);
        System.out.println("Hourly Rate     : Php " + hourlyRates[empIndex]);
        
        boolean hasData = false;
        
        // Loop through all months January to December 2024
        for (int month = 1; month <= 12; month++) {
            int year = 2024;
            
            Object[][] payrollData = calculateEmployeePayroll(empIndex, month, year);
            
            // Extract data from the returned array
            int firstCutoffDays = (int) payrollData[0][0];
            double firstCutoffHours = (double) payrollData[0][1];
            double firstCutoffGross = (double) payrollData[0][2];
            
            int secondCutoffDays = (int) payrollData[1][0];
            double secondCutoffHours = (double) payrollData[1][1];
            double secondCutoffGross = (double) payrollData[1][2];
            double secondCutoffNet = (double) payrollData[1][3];
            double sss = (double) payrollData[1][4];
            double philhealth = (double) payrollData[1][5];
            double pagibig = (double) payrollData[1][6];
            double tax = (double) payrollData[1][7];
            double totalDeductions = (double) payrollData[1][8];
            
            int lastDay = getLastDayOfMonth(month, year);
            String monthName = getMonthName(month);
            
            // Skip if no data for this month
            if (firstCutoffDays == 0 && secondCutoffDays == 0) {
                continue;
            }
            
            hasData = true;
            
            System.out.println("\n" + "-".repeat(80));
            System.out.println("MONTH: " + monthName + " " + year);
            System.out.println("-".repeat(80));
            
            // Display first cutoff
            if (firstCutoffDays > 0) {
                System.out.println("Cutoff Date     : " + monthName + " 1 to 15");
                System.out.println("Total Hours Worked : " + firstCutoffHours);
                System.out.println("Gross Salary    : Php " + firstCutoffGross);
                System.out.println("Net Salary      : Php " + firstCutoffGross);
            } else {
                System.out.println("Cutoff Date     : " + monthName + " 1 to 15");
                System.out.println("No days recorded for this period.");
            }
            
            System.out.println();
            
            // Display second cutoff
            if (secondCutoffDays > 0) {
                System.out.println("Cutoff Date     : " + monthName + " 16 to " + lastDay);
                System.out.println("Total Hours Worked : " + secondCutoffHours);
                System.out.println("Gross Salary    : Php " + secondCutoffGross);
                System.out.println("Each Deduction:");
                System.out.println("    SSS         : Php " + sss);
                System.out.println("    PhilHealth  : Php " + philhealth);
                System.out.println("    Pag-IBIG    : Php " + pagibig);
                System.out.println("    Tax         : Php " + tax);
                System.out.println("Total Deductions: Php " + totalDeductions);
                System.out.println("Net Salary      : Php " + secondCutoffNet);
            } else {
                System.out.println("Cutoff Date     : " + monthName + " 16 to " + lastDay);
                System.out.println("No days recorded for this period.");
            }
        }
        
        if (!hasData) {
            System.out.println("\nNo attendance data found for this employee.");
        }
        
        System.out.println("-".repeat(80));
    }
    
    /**
     * Displays payroll summary for all employees
     * More compact than single employee view
     */
    static void displayAllEmployeesPayroll() {
        System.out.println("\n" + "-".repeat(100));
        System.out.println("                    PAYROLL - ALL EMPLOYEES");
        System.out.println("-".repeat(100));
        
        // Loop through all employees
        for (int i = 0; i < empIds.length; i++) {
            if (empIds[i] == 0) continue;  // Skip empty slots
            
            System.out.println("\n" + "-".repeat(100));
            System.out.println("EMPLOYEE: " + firstNames[i] + " " + lastNames[i] + " (ID: " + empIds[i] + ")");
            System.out.println("Birthday: " + birthdays[i]);
            System.out.println("-".repeat(100));
            
            boolean hasData = false;
            
            // Loop through all months January to December
            for (int month = 1; month <= 12; month++) {
                int year = 2024;
                
                Object[][] payrollData = calculateEmployeePayroll(i, month, year);
                
                int firstCutoffDays = (int) payrollData[0][0];
                double firstCutoffHours = (double) payrollData[0][1];
                double firstCutoffGross = (double) payrollData[0][2];
                
                int secondCutoffDays = (int) payrollData[1][0];
                double secondCutoffHours = (double) payrollData[1][1];
                double secondCutoffGross = (double) payrollData[1][2];
                double secondCutoffNet = (double) payrollData[1][3];
                double sss = (double) payrollData[1][4];
                double philhealth = (double) payrollData[1][5];
                double pagibig = (double) payrollData[1][6];
                double tax = (double) payrollData[1][7];
                double totalDeductions = (double) payrollData[1][8];
                
                int lastDay = getLastDayOfMonth(month, year);
                String monthName = getMonthName(month);
                
                if (firstCutoffDays == 0 && secondCutoffDays == 0) {
                    continue;  // Skip months with no data
                }
                
                hasData = true;
                
                System.out.println("\n  MONTH: " + monthName + " " + year);
                
                // First cutoff summary
                if (firstCutoffDays > 0) {
                    System.out.println("    First Cutoff (1-15): Hours=" + firstCutoffHours +
                                     ", Gross=Php " + firstCutoffGross +
                                     ", Net=Php " + firstCutoffGross);
                }
                
                // Second cutoff summary
                if (secondCutoffDays > 0) {
                    System.out.println("    Second Cutoff (16-" + lastDay + "): Hours=" + secondCutoffHours +
                                     ", Gross=Php " + secondCutoffGross);
                    System.out.println("        Deductions: SSS=Php " + sss +
                                     ", PhilHealth=Php " + philhealth +
                                     ", Pag-IBIG=Php " + pagibig +
                                     ", Tax=Php " + tax);
                    System.out.println("        Total Deductions=Php " + totalDeductions +
                                     ", Net=Php " + secondCutoffNet);
                }
            }
            
            if (!hasData) {
                System.out.println("  No attendance data found for this employee.");
            }
        }
        
        System.out.println("\n" + "-".repeat(100));
    }
    
    /**
     * Menu for payroll staff users
     * They can process payroll
     */
    static void payrollStaffMenu(Scanner scanner) {
        while (true) {
            
            System.out.println("                       PAYROLL STAFF MENU");
            System.out.println("-".repeat(70));
            System.out.println("1. Process Payroll");
            System.out.println("2. Exit");
            System.out.print("Choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Please enter a number.");
                scanner.nextLine();
                continue;
            }
            
            if (choice == 2) {
                System.out.println("\nThank you for using MotorPH Payroll System.");
                System.exit(0);
            } else if (choice == 1) {
                processPayrollMenu(scanner);
            } else {
                System.out.println("Invalid option. Please choose 1 or 2.");
            }
        }
    }
    
    /**
     * Sub-menu for processing payroll
     * Choose single employee or all employees
     */
    static void processPayrollMenu(Scanner scanner) {
        while (true) {
            
            System.out.println("                       PROCESS PAYROLL");
            System.out.println("-".repeat(70));
            System.out.println("1. One employee");
            System.out.println("2. All employees");
            System.out.println("3. Exit");
            System.out.print("Choice: ");
            
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Please enter a number.");
                scanner.nextLine();
                continue;
            }
            
            if (choice == 3) {
                return;  // Go back to previous menu
            } else if (choice == 1) {
                processOneEmployee(scanner);
            } else if (choice == 2) {
                displayAllEmployeesPayroll();
            } else {
                System.out.println("Invalid option. Please choose 1-3.");
            }
        }
    }
    
    /**
     * Handles payroll processing for a single employee
     * Asks for employee number then displays their payroll
     */
    static void processOneEmployee(Scanner scanner) {
        System.out.print("\nEnter employee number: ");
        int empId;
        try {
            empId = scanner.nextInt();
            scanner.nextLine();
        } catch (Exception e) {
            System.out.println("Please enter a number.");
            scanner.nextLine();
            return;
        }
        
        int index = findEmployeeIndex(empId);
        if (index == -1) {
            System.out.println("\nEmployee number does not exist.");
            return;
        }
        
        displaySingleEmployeePayroll(index);
    }
    
    /**
     * The main method - where the program starts running
     */
    public static void main(String[] args) {
        // Display welcome banner
       
        System.out.println("                        MOTORPH PAYROLL SYSTEM");
        System.out.println("-".repeat(70));
        
        // Load data from CSV files
        loadEmployeeData();
        loadAttendanceData();
        
        Scanner scanner = new Scanner(System.in);  // Create scanner for user input
        
        
        System.out.println("                     MOTORPH PAYROLL SYSTEM");
        System.out.println("-".repeat(70));
        
        // Ask for login
        System.out.print("\nUsername: ");
        String username = scanner.nextLine();
        
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        // Check if login is valid
        if (!isValidLogin(username, password)) {
            System.out.println("\nPlease enter a valid username and password.");
            System.out.println("Access denied.");
            scanner.close();
            return;  // Exit program
        }
        
        System.out.println("\nWelcome, " + username + "!");
        
        // Show appropriate menu based on user type
        if (username.equals("employee")) {
            employeeMenu(scanner);
        } else {
            payrollStaffMenu(scanner);
        }
        
        scanner.close();  // Close scanner when done
    }
}
