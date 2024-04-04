import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects;

public class Main {
    // Constants for file operations
    private static final String FILE_NAME = "db.csv";
    private static final String DELIMITER = ";";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));

    // Enum for supported commands
    enum Command {
        PRINT,
        ADD,
        DEL,
        EDIT,
        SORT,
        FIND,
        AVG,
        EXIT
    }

    // Enum for vehicle types
    enum VehicleType {
        PLANE,
        BUS,
        TRAIN,
        BOAT
    }

    // Map to store command arguments
    private static final Map<Command, Integer> COMMAND_ARGUMENTS = new HashMap<>();
    static {
        COMMAND_ARGUMENTS.put(Command.PRINT, 0);
        COMMAND_ARGUMENTS.put(Command.ADD, 6);
        COMMAND_ARGUMENTS.put(Command.DEL, 1);
        COMMAND_ARGUMENTS.put(Command.EDIT, 6);
        COMMAND_ARGUMENTS.put(Command.SORT, 0);
        COMMAND_ARGUMENTS.put(Command.FIND, 1);
        COMMAND_ARGUMENTS.put(Command.AVG, 0);
        COMMAND_ARGUMENTS.put(Command.EXIT, 0);
    }

    // Main method
    public static void main(String[] mainArgs) {
        // Scanner for user input
        Scanner scanner = new Scanner(System.in);
        // Read data from file
        List<Travel> travels = readDataFromFile();

        // If the file does not exist, create it with default data
        if (travels.isEmpty()) {
            createDefaultFile();
            travels = readDataFromFile();
        }

        while (true) {
            // Display command menu
            /*System.out.println("--------------------------------------------------------------------");
            System.out.println("|                  Travel Information System                       |");
            System.out.println("--------------------------------------------------------------------");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "Command", "Arguments", "Description");
            System.out.println("--------------------------------------------------------------------");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "print", "", "View travels");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "add", "id;city;date;days;price;vehicle", "Add new travel");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "del", "id", "Delete travel");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "edit", "id;city;date;days;price;vehicle", "Update travel");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "sort", "", "Sort travels by date");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "find", "price", "Filter travels by price");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "avg", "", "Calculate average price");
            System.out.printf("| %-7s| %-31s| %-23s|%n", "exit", "", "Exit");
            System.out.println("--------------------------------------------------------------------");

            // Prompt for command input
            System.out.print("Enter command: ");*/

            // Read user input
            String input = scanner.nextLine().trim().toLowerCase();

            try {
                String[] parts = input.split("\\s+", 2);
                Command command = Command.valueOf(parts[0].toUpperCase());
                String[] args = parts.length > 1 ? parts[1].split(";") : new String[0];

                // Process user command
                switch (command) {
                    case PRINT:
                        printTable(travels);
                        break;
                    case ADD:
                        addTravel(travels, args);
                        break;
                    case DEL:
                        deleteTravel(travels, args);
                        break;
                    case EDIT:
                        editTravel(travels, args);
                        break;
                    case SORT:
                        sortTravels(travels);
                        break;
                    case FIND:
                        findTravels(args, travels);
                        break;
                    case AVG:
                        calculateAveragePrice(travels);
                        break;
                    case EXIT:
                        //System.out.println("Exiting...");
                        System.exit(0);
                        break;
                }
            } catch (IllegalArgumentException e) {
                System.out.println("wrong command");
            }
        }
    }

    // Read data from file
    private static List<Travel> readDataFromFile() {
        List<Travel> travels = new ArrayList<>();
        File file = new File(FILE_NAME);

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(DELIMITER);
                if (parts.length == 6) {
                    int id = Integer.parseInt(parts[0]);
                    String city = parts[1];
                    LocalDate date = LocalDate.parse(parts[2], DATE_FORMATTER);
                    int days = Integer.parseInt(parts[3]);
                    double price = Double.parseDouble(parts[4].replace(',', '.'));
                    VehicleType vehicle = VehicleType.valueOf(parts[5]);
                    Travel travel = new Travel(id, city, date, days, price, vehicle);
                    travels.add(travel);
                } else {
                    System.out.println("Invalid data format: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            //System.out.println("File not found. Creating a new file...");
            createDefaultFile();
        }

        return travels;
    }

    // Create default data file
    private static void createDefaultFile() {
        try (PrintWriter writer = new PrintWriter(FILE_NAME)) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            writer.println("101;Daugavpils;" + LocalDate.of(2021, 7, 3).format(formatter) + ";5;150.50;TRAIN");
            writer.println("102;Rome;" + LocalDate.of(2021, 5, 15).format(formatter) + ";7;300.00;BUS");
            writer.println("103;Hamburg;" + LocalDate.of(2021, 9, 15).format(formatter) + ";10;500.50;PLANE");
            writer.println("104;Helsinki;" + LocalDate.of(2021, 6, 10).format(formatter) + ";3;250.00;BOAT");
            writer.println("105;New York;" + LocalDate.of(2021, 8, 16).format(formatter) + ";5;1000.00;PLANE");
        } catch (FileNotFoundException e) {
            System.out.println("Error creating file.");
        }
    }

    // Print table of travels
    private static void printTable(List<Travel> travels) {
        // Divider
        String divider = "------------------------------------------------------------";

        // Print divider
        System.out.println(divider);

        // Print column headers
        System.out.printf("%-4s%-21s%-11s%-6s%-10s%-9s%n",
                "ID", "City", "Date", "Days", "Price", " Vehicle");

        // Print divider
        System.out.println(divider);

        // Formatter for date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Iterate over all travels
        travels.forEach(travel -> {
            // Trim each field to remove leading and trailing spaces
            int id = travel.getId();
            String city = travel.getCity().trim();
            String date = travel.getDate().format(formatter).trim();
            int days = travel.getDays();
            String price = DECIMAL_FORMAT.format(travel.getPrice()).trim();
            String vehicle = " " + travel.getVehicle().toString().trim(); // Add space before Vehicle

            // Print each travel with formatting
            System.out.printf("%-4d%-21s%-11s%6d%10s%-9s%n",
                    id, city, date, days, price, vehicle);
        });

        // Print divider
        System.out.println(divider);
    }

    // Add a new travel
    private static void addTravel(List<Travel> travels, String[] commandArgs) {

        // Check if any argument is empty or if the number of arguments is incorrect
        if (Arrays.stream(commandArgs).anyMatch(String::isEmpty) || commandArgs.length != 6) {
            System.out.println("wrong field count");
            return;
        }

        // Validate ID format
        String id = commandArgs[0];
        if (!isThreeDigitNumeric(id) || travels.stream().anyMatch(travel -> travel.getId() == Integer.parseInt(id))) {
            System.out.println("wrong id");
            return;
        }

        // Format city name
        String city = formatCityName(commandArgs[1]);

        // Parse travel date
        LocalDate travelDate;
        try {
            travelDate = LocalDate.parse(commandArgs[2], DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("wrong date");
            return;
        }

        // Validate day count and price input
        String daysStr = commandArgs[3];
        String priceStr = commandArgs[4].replace(',', '.');

        if (!isNumeric(daysStr) || Integer.parseInt(daysStr) <= 0) {
            System.out.println("wrong day count");
            return;
        }

        if (!isNumeric(priceStr) || Double.parseDouble(priceStr) < 0) {
            System.out.println("wrong price");
            return;
        }

        // Convert vehicle type
        VehicleType vehicle;
        try {
            vehicle = VehicleType.valueOf(commandArgs[5].toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("wrong vehicle");
            return;
        }

// Create new travel object
        Travel newTravel = new Travel(Integer.parseInt(id), city, travelDate,
                Integer.parseInt(daysStr), Double.parseDouble(priceStr), vehicle);

        // Find the index where the new travel should be inserted based on its ID
        int insertionIndex = 0;
        for (int i = 0; i < travels.size(); i++) {
            if (travels.get(i).getId() > newTravel.getId()) {
                insertionIndex = i;
                break;
            } else {
                insertionIndex = i + 1;
            }
        }

        // Add the new travel to the list at the correct index
        travels.add(insertionIndex, newTravel);

        // Update file
        updateFile(travels);
        System.out.println("added");
    }

    // Check if a string is a three-digit numeric value
    private static boolean isThreeDigitNumeric(String str) {
        return str.matches("\\d{3}");
    }

    // Check if a string is numeric
    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    // Format city name
    private static String formatCityName(String cityName) {
        StringBuilder formattedCityName = new StringBuilder();

        // Split string into words considering hyphen
        String[] words = cityName.split("\\s|-");
        for (String word : words) {
            if (!word.isEmpty()) {
                // Convert first letter of each word to uppercase and the rest to lowercase
                String formattedWord = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                formattedCityName.append(formattedWord).append(" ");
            }
        }

        return formattedCityName.toString().trim();
    }

    // Edit an existing travel
    private static void editTravel(List<Travel> travels, String[] args) {
        // Validate number of arguments
        if (args.length < 2 || args.length > 7) {
            System.out.println("wrong field count");
            return;
        }

        // Parse ID
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("wrong id");
            return;
        }

        // Find travel by ID
        Travel originalTravel = null;
        for (Travel travel : travels) {
            if (travel.getId() == id) {
                originalTravel = travel;
                break;
            }
        }

        // If travel with specified ID not found
        if (originalTravel == null) {
            System.out.println("wrong id");
            return;
        }

        // Create a copy of the original travel
        Travel updatedTravel = new Travel(originalTravel.getId(), originalTravel.getCity(),
                originalTravel.getDate(), originalTravel.getDays(),
                originalTravel.getPrice(), originalTravel.getVehicle());

        // Update travel fields based on user input
        if (args.length >= 2 && !args[1].isEmpty()) {
            updatedTravel.setCity(formatCityName(args[1]));
        }
        if (args.length >= 3 && !args[2].isEmpty()) {
            try {
                updatedTravel.setDate(LocalDate.parse(args[2], DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                System.out.println("wrong date");
                return;
            }
        }
        if (args.length >= 4 && !args[3].isEmpty()) {
            try {
                updatedTravel.setDays(Integer.parseInt(args[3]));
            } catch (NumberFormatException e) {
                System.out.println("wrong day count");
                return;
            }
        }
        if (args.length >= 5 && !args[4].isEmpty()) {
            try {
                updatedTravel.setPrice(Double.parseDouble(args[4].replace(',', '.')));
            } catch (NumberFormatException e) {
                System.out.println("wrong price");
                return;
            }
        }
        if (args.length >= 6 && !args[5].isEmpty()) {
            try {
                updatedTravel.setVehicle(VehicleType.valueOf(args[5].toUpperCase()));
            } catch (IllegalArgumentException e) {
                System.out.println("wrong vehicle");
                return;
            }
        }

        // Replace original travel with the updated one
        int index = travels.indexOf(originalTravel);
        travels.set(index, updatedTravel);

        // Update file with the modified list of travels
        updateFile(travels);
        System.out.println("changed");
    }

    // Delete a travel
    private static void deleteTravel(List<Travel> travels, String[] args) {
        // Validate number of arguments
        if (args.length != 1) {
            System.out.println("wrong field count");
            return;
        }

        // Parse ID
        int id;
        try {
            id = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.out.println("wrong id");
            return;
        }

        // Find travel by ID and remove it
        boolean removed = travels.removeIf(travel -> travel.getId() == id);
        if (removed) {
            updateFile(travels);
            System.out.println("deleted");
        } else {
            System.out.println("wrong id");
        }
    }

    // Update file with list of travels
    private static void updateFile(List<Travel> travels) {
        try (PrintWriter writer = new PrintWriter(FILE_NAME)) {
            for (Travel travel : travels) {
                // Format price with DecimalFormat and store it in priceStr
                String priceStr = DECIMAL_FORMAT.format(travel.getPrice());
                // Write travel data to file, separating fields with DELIMITER
                writer.println(travel.getId() + DELIMITER + travel.getCity() + DELIMITER +
                        travel.getDate().format(DATE_FORMATTER) + DELIMITER +
                        travel.getDays() + DELIMITER + priceStr + DELIMITER + travel.getVehicle());
            }
            writer.flush(); // Flush the writer to ensure all data is written to the file
        } catch (FileNotFoundException e) {
            System.out.println("Error updating file.");
        }
    }

    // Sort travels by date
    private static void sortTravels(List<Travel> travels) {
        // Sort travels by date
        travels.sort(Comparator.comparing(Travel::getDate));

        // Update file with sorted list of travels
        updateFile(travels);
        System.out.println("sorted");
    }

    // Filter travels by maximum price
    private static void findTravels(String[] args, List<Travel> travels) {
        // Checking the presence of arguments
        if (args.length < 1) {
            System.out.println("wrong field count");
            return;
        }
        try {
            // Replace comma with dot for correct parsing of floating-point numbers
            double maxPrice = Double.parseDouble(args[0].replace(',', '.'));
            // Filtering travels by maximum price and printing the result
            List<Travel> filteredTravels = travels.stream()
                    .filter(travel -> travel.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
            printTable(filteredTravels);
        } catch (NumberFormatException e) {
            System.out.println("wrong price");
        }
    }

    // Calculate average price of travels
    private static void calculateAveragePrice(List<Travel> travels) {
        if (travels.isEmpty()) {
            System.out.println("No travels found.");
            return;
        }

        // Calculate average price
        double total = travels.stream().mapToDouble(Travel::getPrice).sum();
        double average = total / travels.size();

        // Print average price
        System.out.println("average=" + DECIMAL_FORMAT.format(average));
    }

    // Class to represent a travel
    private static class Travel {
        private int id;
        private String city;
        private LocalDate date;
        private int days;
        private double price;
        private VehicleType vehicle;

        public Travel(int id, String city, LocalDate date, int days, double price, VehicleType vehicle) {
            this.id = id;
            this.city = city;
            this.date = date;
            this.days = days;
            this.price = price;
            this.vehicle = vehicle;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public int getDays() {
            return days;
        }

        public void setDays(int days) {
            this.days = days;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public VehicleType getVehicle() {
            return vehicle;
        }

        public void setVehicle(VehicleType vehicle) {
            this.vehicle = vehicle;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Travel travel = (Travel) o;
            return id == travel.id &&
                    days == travel.days &&
                    Double.compare(travel.price, price) == 0 &&
                    city.equals(travel.city) &&
                    date.equals(travel.date) &&
                    vehicle == travel.vehicle;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, city, date, days, price, vehicle);
        }
    }
}