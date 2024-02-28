package banking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Accounts {
	private Connection connection;
	private Scanner scanner;

	public Accounts(Connection connection, Scanner scanner) {
		this.connection = connection;
		this.scanner = scanner;
	}

	public long openAccount(String email) {
		if (!accountExist(email)) {
			String openAccountQuery = "INSERT INTO Accounts(account_number, full_name, email, balance, security_pin) VALUES(?, ?, ?, ?, ?)";
			scanner.nextLine();
			System.out.print("Enter Full Name: ");
			String fullName = scanner.nextLine();
			double balance = 0.0; // Default value for balance
			boolean validAmount = false;
			while (!validAmount) {
				try {
					System.out.print("Enter Initial Amount: ");
					balance = scanner.nextDouble();
					validAmount = true;
				} catch (InputMismatchException e) {
					System.out.println("Invalid amount. Please enter a valid number.");
					scanner.next(); // Clear the invalid input
				}
			}
			scanner.nextLine(); // Consume newline
			System.out.print("Enter Security Pin: ");
			String securityPin = scanner.nextLine();
			try {
				long accountNumber = generateAccountNumber();
				PreparedStatement preparedStatement = connection.prepareStatement(openAccountQuery);
				preparedStatement.setLong(1, accountNumber);
				preparedStatement.setString(2, fullName);
				preparedStatement.setString(3, email);
				preparedStatement.setDouble(4, balance);
				preparedStatement.setString(5, securityPin);
				int rowsAffected = preparedStatement.executeUpdate();
				if (rowsAffected > 0) {
					return accountNumber;
				} else {
					throw new RuntimeException("Account Creation failed!!");
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		throw new RuntimeException("Account Already Exists");
	}

	public long getAccountNumber(String email) {
		String query = "SELECT account_number from Accounts WHERE email = ?";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return resultSet.getLong("account_number");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		throw new RuntimeException("Account Number Doesn't Exist!");
	}

	private long generateAccountNumber() {
		try {
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement
					.executeQuery("SELECT account_number from Accounts ORDER BY account_number DESC LIMIT 1");
			if (resultSet.next()) {
				long lastAccountNumber = resultSet.getLong("account_number");
				return lastAccountNumber + 1;
			} else {
				return 10000100;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 10000100;
	}

	public Boolean accountExist(String email) {
		String query = "SELECT account_number from Accounts WHERE email = ?";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, email);
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
}
