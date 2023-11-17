import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testng.Assert;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class TestAPI {

	private static String jdbcUrl = "jdbc:sqlserver://192.168.110.195\\MSSQL2016;databaseName=MELUnified_Configuration_V20230507";
	private static String username = "sa";
	private static String password = "sqlqa@195";
	private static String RegisteredVerifiedID = "F0076628-5B0D-47E2-A28A-0254F980F604";
	private static String authToken;
	private static String randomEmail;
	private static String randomMobileNumber;
	private static String MobileregistrationType = "B58FFF7A-4D89-4CFE-80BE-42E93D177C30";
	private static String EmailregistrationType = "D3DC29F1-C5D0-450B-928F-A1BBFB5F4C37";

	private static String VisaID = "9F157504-CE96-49AA-828D-0AB6DFFEC89E";
	private static String MadaID = "5CE0C487-98B8-47D7-935F-6ACFD83C2A5C";

	private static String transactionID;
	private static String RandomNumber;
	private static String TimeZone = "Middle East Standard Time";
	private static String ID;
	private static String cardNumber = "4111111111111111"; // Here we identify if we are using Mada or VISA
	// private static String cardNumber="5297412484442387";
	// private static String payment;
	private static Response paymentURL;
	private static String URLToPay;
	private static final int MAX_RETRIES = 3;
	// WebDriver driver = new ChromeDriver();
	// driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

	// Rest of your test script

	@BeforeClass
	public static void setup() {
		// Set the base URI for your API requests
		RestAssured.baseURI = "http://192.168.110.195:9059";
	}

	@Test
	public void testAuthentication() {

		// Define the JSON request body
		String requestBody = "{\"username\":\"admin@vanrise.com\", \"password\":\"1\"}";

		authToken = given().contentType("application/json").body(requestBody).when()
				.post("/api/security/user/authenticate").then().statusCode(200) // Adjust the expected status code as
																				// needed
				.extract().path("data.token");

		// Print the token (for verification, you can remove this in production)
		System.out.println("Token: " + authToken);

	}

	@Test
	public void TestAllAPIs() {
		
		//Test Delete Customer for a user without a subscription
		System.out.println("\n---------------------Email Delete Test Case---------------------");
		randomEmail = generateRandomEmail();
		RegisterCustomer(randomEmail, EmailregistrationType);
		activateCustomerWithRetry(randomEmail);
		System.out.println("Email to delete: " + randomEmail);
		DeleteCustomer(ID);
		
		System.out.println("\n---------------------Mobile Delete Test Case---------------------");
		randomMobileNumber = generateRandomLebaneseNumber();
		RegisterCustomer(randomMobileNumber, MobileregistrationType);
		activateCustomerWithRetry(randomMobileNumber);
		System.out.println("Mobile Number to delete: " + randomMobileNumber);
		DeleteCustomer(ID);
		
		
		//Create a Email User and execute all APIs
		System.out.println("---------------------Email Test Cases---------------------");
		randomEmail = generateRandomEmail();
		System.out.println("Email: " + randomEmail);
		

		RegisterCustomer(randomEmail, EmailregistrationType);
		activateCustomerWithRetry(randomEmail);
		CreateSubscriptionWithRetry(ID);
		PayVisa(URLToPay);
		String ChangePaymentURLForEmail = ChangePaymentMethod(ID, VisaID);
		PayVisa(ChangePaymentURLForEmail);
		CancelSubscription(ID);
		String SubscriptionReactivationURLEmail =ReactivateSubscription(ID,VisaID);
		PayVisa(SubscriptionReactivationURLEmail);
		CheckOrderStatus("Reactivate Vianeos Retail Product");
		CheckOrderStatus("Evaluate Payment for Transaction");
		 
		if (CheckIfSubscriptionActive(ID)) {
			System.out.println("The subscription is set to Active!");
			} 
		else {
			System.out.println("The subscription status didn't change!");
			}
		
		UpdateEmail(ID);//for Email only
		UpdateCustomerTimeZone(ID, "GMT Standard Time");
		//AddEmailforMobileUser(ID);
		AddCustMobileNumberForEmail(ID);//for Email only
		changePassEmail(randomEmail);
		changeCustName("HassannEmail");
		RedeemVoucher("0485",ID);
		
		GetPayment(ID);
		GetPaymentReceipt(ID);
		GetPaymentMethod(ID);
		GetSubscriptionDetails(ID);
		
		
		
		
		//Create a Mobile User and execute all APIs
		System.out.println("---------------------Mobile Test Cases---------------------");
		randomMobileNumber = generateRandomLebaneseNumber();
		System.out.println("Mobile Number: " + randomMobileNumber);
		RegisterCustomer(randomMobileNumber, MobileregistrationType);
		activateCustomerWithRetry(randomMobileNumber);
		CreateSubscriptionWithRetry(ID);
		PayVisa(URLToPay);
		String ChangePaymentURLForMobile = ChangePaymentMethod(ID, VisaID);
		PayVisa(ChangePaymentURLForMobile);
		CancelSubscription(ID);
		String SubscriptionReactivationURL =ReactivateSubscription(ID,VisaID);
		PayVisa(SubscriptionReactivationURL);
		CheckOrderStatus("Reactivate Vianeos Retail Product");
		CheckOrderStatus("Evaluate Payment for Transaction");
		 
		if (CheckIfSubscriptionActive(ID)) {
			System.out.println("The subscription is set to Active!");
			}
		else {
			System.out.println("The subscription status didn't change!");
			}
		
		//UpdateEmail(ID);for Email only
		UpdateCustomerTimeZone(ID, "GMT Standard Time");
		AddEmailforMobileUser(ID);
		//AddCustMobileNumberForEmail(ID);for Email only
		changePassMobile(randomMobileNumber);
		changeCustName("HassanMobile");
		RedeemVoucher("0485",ID);
		
		GetPayment(ID);
		GetPaymentReceipt(ID);
		GetPaymentMethod(ID);
		GetSubscriptionDetails(ID);
		
		
		 

		//Common
		System.out.println("---------------------Common Test Cases---------------------");
		GetSubscriptionCancellationSurvey("en");
		GetSubscriptionCancellationSurvey("ar");
		GetProducts("lb");
		GetVoucher("0485", "en-US");
		SendOTP("hhmaidan@vanrise.com", "9999");
		SendPinCode("hhmaidan@vanrise.com", "7777");
		
		
		

	}

	public void RegisterCustomer(String customer, String registrationType) {

		// String customer = "hhmaidan327@vanrise.com";
		try {
			String requestBody = "";
			if (registrationType.equals("D3DC29F1-C5D0-450B-928F-A1BBFB5F4C37")) {
				requestBody = "{" + "\"registrationType\": \"" + registrationType + "\"," + "\"email\": \"" + customer
						+ "\"," + "\"password\": \"H@ssan\"," + "\"countryCode\": \"lb\"," + "\"userTimezone\": \""
						+ TimeZone + "\"" + "}";
			} else if (registrationType.equals("B58FFF7A-4D89-4CFE-80BE-42E93D177C30")) {
				requestBody = "{" + "\"registrationType\": \"" + registrationType + "\"," + "\"mobileNumber\": \""
						+ customer + "\"," + "\"password\": \"H@ssan\"," + "\"countryCode\": \"lb\","
						+ "\"userTimezone\": \"" + TimeZone + "\"" + "}";
			}

			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.post("/api/orbitNow/customer/register").then().statusCode(202);

			System.out.println(customer + " is registered!");
		} catch (Exception ex) {
			System.out.println("Registeration API failed with this exception" + ex);
		}

	}

	public Response ActivateCustomer(String customer) {

		ID = GetForeignID(customer);

		String requestBody = "{\"customerId\":\"" + ID + "\"}";

		Response response = RestAssured.given().contentType("application/json").body(requestBody)
				.header("Auth-Token", authToken).when().put("/api/orbitNow/customer/activate");

		return response;

	}

	public Response CreateSubsc() {

		String requestBody = "{\"customerId\":\"" + ID + "\",\r\n" + "\"productId\":\"T6wIUqrVLUl6_4BWKQgDt\",\r\n"
				+ "\"paymentCompleteReturnURL\":\"https://www.google.com/\",\r\n" + "\"languageCode\":\"en-US\",\r\n"
				+ "\"email\":\"hhmaidan241@vanrise.com\",\r\n" + "\"givenName\":\"Hassan\",\r\n"
				+ "\"surname\":\"Hm\",\r\n" + "//\"promoCode\":\"1013\",\r\n"
				+ "\"paymentMethodTypeId\":\"9F157504-CE96-49AA-828D-0AB6DFFEC89E\"}";

		Response response = RestAssured.given().contentType("application/json").body(requestBody)
				.header("Auth-Token", authToken).when().post("/api/orbitNow/subscription/create");

		return response;

	}

	public void PayVisa(String URLToPay) {

		System.setProperty("webdriver.chrome.driver", "C:\\web\\LatestChrome\\chromedriver.exe");

		// ChromeOptions
		ChromeOptions options = new ChromeOptions();

		// Maximize browser
		options.addArguments("--start-maximized");

		// WebDriver
		WebDriver driver = new ChromeDriver(options);

		// Actual payment URL
		String paymentURL = URLToPay;

		// Navigate to payment URL
		driver.get(paymentURL);

		WebDriverWait wait = new WebDriverWait(driver, 10);

		try {
			// Do the Payment
			Thread.sleep(2000);
			WebElement cardIframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("card.number")));
			driver.switchTo().frame(cardIframe);

			WebElement cardNumberField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("input")));
			cardNumberField.sendKeys(cardNumber); // Replace with the actual card number

			// Switch back to the default content
			driver.switchTo().defaultContent();

			// Find and fill in the Expiry Date field
			WebElement expiryDateField = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".wpwl-control-expiry")));
			expiryDateField.sendKeys("12/25"); // Replace with the desired expiry date

			// Find and fill in the Card Holder field
			WebElement cardHolderField = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.name("card.holder")));
			cardHolderField.sendKeys("Hassan"); // Replace with the cardholder's name

			// Switch to the CVV IFrame
			WebElement cvvIframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("card.cvv")));
			driver.switchTo().frame(cvvIframe);

			// Find and fill in the CVV field
			WebElement cvvField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("input")));
			cvvField.sendKeys("123"); // Replace with the CVV

			// Switch back to the default content
			driver.switchTo().defaultContent();

			// Select Lebanon from the country dropdown
			WebElement countryDropdown = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.name("billing.country")));
			Select countrySelect = new Select(countryDropdown);
			countrySelect.selectByValue("LB"); // Replace with the appropriate value for Lebanon

			// Find and fill in the state field
			WebElement stateField = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".wpwl-control-stateText")));
			stateField.sendKeys("Beirut"); // Replace with the desired state

			// Find and fill in the city field
			WebElement cityField = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.cssSelector(".wpwl-sup-wrapper-city > .wpwl-control")));
			cityField.sendKeys("Beirut"); // Replace with the desired city

			// Find and fill in the postcode field
			WebElement postcodeField = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.cssSelector(".wpwl-sup-wrapper-postcode > .wpwl-control")));
			postcodeField.sendKeys("0000"); // Replace with the desired postcode

			// Find and fill in the street address field
			WebElement streetField = wait.until(ExpectedConditions
					.visibilityOfElementLocated(By.cssSelector(".wpwl-sup-wrapper-street1 > .wpwl-control")));
			streetField.sendKeys("Beirut"); // Replace with the desired street address

			// Find and submit the form
			WebElement payButton = wait
					.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".wpwl-button-pay")));
			payButton.click();

			// Find all iframes and wait for them to be available
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("iframe")));

			// Choose the iframe you want to interact with by its index (0, 1, 2, or 3)
			int iframeIndex = 2; // Change this to the index of the iframe you want to use

			// Switch to the selected iframe
			driver.switchTo().frame(iframeIndex);

			if (cardNumber == "4111111111111111") {

				// Find and wait for the element with id "returnCode"
				WebElement selectElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("returnCode")));

				Select dropdown = new Select(selectElement);

				// Select the option by its visible text
				dropdown.selectByVisibleText("Successful");

				// Find the <input> element by its value attribute
				WebElement submitButton = wait
						.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[value='   Submit   ']")));

				// Click on the submit button
				submitButton.click();

			}

			else {

				WebElement selectElement = wait
						.until(ExpectedConditions.presenceOfElementLocated(By.name("returnCode")));

				// Create a Select object to interact with the <select> element
				Select dropdown = new Select(selectElement);

				// Select the option by its visible text
				dropdown.selectByVisibleText("Success");

				// Locate and click the "Pay" button
				WebElement pay = wait.until(ExpectedConditions.elementToBeClickable(By.name("submit")));

				pay.click();

			}

			// Switch back to the main content of the page
			driver.switchTo().defaultContent();

		} catch (Exception ex) {

			System.out.println(ex);
		}

	}

	public String GetForeignID(String customer) {

		// String jdbcUrl =
		// "jdbc:sqlserver://192.168.110.195\\MSSQL2016;databaseName=MELUnified_Configuration_V20230507";
		// String username = "sa";
		// String password = "sqlqa@195";

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "Select ForeignID\r\n"
					+ "  FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
					+ "  where PartyID = (Select TOP (1) ID FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[Party]\r\n"
					+ "  where FirstName='" + customer + "' and IsInactive=0 order by CreatedTime desc)";

			/*
			 * // Define your SQL query to retrieve the ID String sqlQuery =
			 * "Select ForeignID\r\n" +
			 * "    FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
			 * +
			 * "    where PartyID = (Select ID  FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[Party]\r\n"
			 * + "    where FirstName='" + customer + "' and IsInactive=0)";
			 */

			// Create a statement and execute the query
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String id = "";
			if (resultSet.next()) {
				id = resultSet.getString("ForeignID");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			// Use the retrieved ID in your API request
			String jsonBody = "{\"Id\":\"" + id + "\"}";
			System.out.println("ForeignID: " + jsonBody);
			return id;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	public static String generateRandomEmail() {
		Random random = new Random();
		int randomNumber = random.nextInt(10000); // Generate a random number

		// Concatenate the random number with the email format
		String email = "user" + randomNumber + "@exampleee.com";

		return email;
	}

	public void activateCustomerWithRetry(String customer) {
		int maxRetries = 10; // Maximum number of retry attempts
		int retryCount = 0;

		while (retryCount < maxRetries) {
			// Perform the activation request
			Response response = ActivateCustomer(customer);

			if (response.statusCode() == 202) {
				// Success, the desired response is received
				System.out.println("Activation successful.");
				break; // Exit the loop
			} else {
				// Retry after a delay (e.g., sleep for a while)
				retryCount++;
				System.out.println(
						"Activation failed. Retrying attempt " + retryCount + "..." + response.getBody().asString());
				sleep(10000); // Sleep for 5 seconds before the next retry (adjust as needed)
			}
		}

		if (retryCount == maxRetries) {
			System.out.println("Maximum retries reached. Activation failed.");
		}
	}

	public void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	public void CreateSubscriptionWithRetry(String customerId) {
		int maxRetries = 20; // Maximum number of retry attempts
		int retryCount = 0;

		while (retryCount < maxRetries) {
			// Perform the activation request
			Response response = CreateSubsc();

			if (response.statusCode() == 200) {
				// Success, the desired response is received
				System.out.println("Subscription created successfully.");
				System.out.println("PaymentURL: " + response.getBody().asString());

				URLToPay = response.path("data.paymentURL");

				// Print the paymentURL
				System.out.println("PaymentURL: " + URLToPay);

				// Extract the transaction ID in order to check the Evaluate instance in the
				// BPinstance table
				// String jsonResponse = response.asString();
				transactionID = response.jsonPath().getString("data.paymentURL").split("transaction=")[1];
				// Print the transaction ID
				System.out.println("Transaction ID: " + transactionID);

				break; // Exit the loop
			} else {
				// Retry after a delay (e.g., sleep for a while)
				retryCount++;
				System.out.println(
						"Subscription failed. Retrying attempt " + retryCount + "..." + response.getBody().asString());
				sleep(10000); // Sleep for 5 seconds before the next retry (adjust as needed)
			}
		}

		if (retryCount == maxRetries) {
			System.out.println("Maximum retries reached. Activation failed.");
		}
	}

	public void changePassEmail(String email) {

		String oldPass = GetPass();

		String requestBody = "{\"email\":\"" + email + "\",\r\n" + "\"password\":\"P@ssw0rd\"}";
		try {
			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.put("/api/orbitNow/customer/changePassword").then().statusCode(200);

			String newPass = GetPass();

			if (!oldPass.equals(newPass)) {
				System.out.println("Password changed successfully!");
			}
		} catch (Exception ex) {
			System.out.println("Issue in the API");
			System.out.println(ex);
		}

	}

	public void changePassMobile(String mobileNumber) {

		String oldPass = GetPass();

		String requestBody = "{\"mobileNumber\":\"" + mobileNumber + "\",\r\n" + "\"password\":\"P@ssw0rd\"}";
		try {
			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.put("/api/orbitNow/customer/changePassword").then().statusCode(200);

			String newPass = GetPass();

			if (!oldPass.equals(newPass)) {
				System.out.println("Password changed successfully!");
			}
		} catch (Exception ex) {
			System.out.println("Issue in the API");
			System.out.println(ex);
		}

	}

	public String GetPass() {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "Select Password\r\n"
					+ "  FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
					+ "  where ForeignID='" + ID + "'";


			// Create a statement and execute the query
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String password = "";
			if (resultSet.next()) {
				password = resultSet.getString("Password");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			// Use the retrieved ID in your API request
			String jsonBody = "{\"Password\":\"" + password + "\"}";
			System.out.println("Password: " + jsonBody);
			return password;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";

	}

	public void changeCustName(String name) {

		String oldCustName = GetCustName();

		String requestBody = "{\"customerId\":\"" + ID + "\",\r\n" + "\"name\":\""+	name+"\"}";
		try {
			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.put("/api/orbitNow/customer/changeName").then().statusCode(202);

			CheckOrderStatus("Change Customer Name");

			String newCustName = GetCustName();

			if (!oldCustName.equals(newCustName)) {
				System.out.println("Name changed successfully!");
			} else {
				System.out.println("Name didn't change!");
			}
		} catch (Exception ex) {
			System.out.println("Issue in the API");
			System.out.println(ex);
		}

	}

	public String GetCustName() {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "Select FirstName\r\n"
					+ "  FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[Party]\r\n"
					+ "  where ID in (Select PartyID\r\n"
					+ "  FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
					+ "  where ForeignID='" + ID + "')";

			/*
			 * // Define your SQL query to retrieve the ID String sqlQuery =
			 * "Select ForeignID\r\n" +
			 * "    FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
			 * +
			 * "    where PartyID = (Select ID  FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[Party]\r\n"
			 * + "    where FirstName='" + customer + "' and IsInactive=0)";
			 */

			// Create a statement and execute the query
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String FirstName = "";
			if (resultSet.next()) {
				FirstName = resultSet.getString("FirstName");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			// Use the retrieved ID in your API request
			String jsonBody = "{\"FirstName\":\"" + FirstName + "\"}";
			System.out.println("FirstName: " + jsonBody);
			return FirstName;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";

	}

	public void CheckOrderStatus(String ProcessTitle) {

		int maxRetries = 20; // Maximum number of retry attempts
		int retryCount = 0;

		while (retryCount < maxRetries) {

			String status = GetOrderStatus(ProcessTitle);

			if (status.equals("50")) {
				// Success, the desired response is received
				System.out.println(ProcessTitle + " Order Completed.");

				break; // Exit the loop
			} else {
				// Retry after a delay (e.g., sleep for a while)
				retryCount++;
				System.out.println(ProcessTitle + " Order still pending. Retrying attempt " + retryCount + "...");
				sleep(10000); // Sleep for 5 seconds before the next retry (adjust as needed)
			}
		}

		if (retryCount == maxRetries) {
			System.out.println("Maximum retries reached. Activation failed.");
		}

	}

	public String GetOrderStatus(String ProcessTitle) {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
			String sqlQuery = "";
			if (ProcessTitle.contains("Evaluate Payment for Transaction")) {
				sqlQuery = "SELECT ExecutionStatus\r\n"
						+ "FROM [SOMUnified_Transaction_V20230507].[bp].[BPInstance]\r\n"
						+ "WHERE Title = 'Evaluate Payment for Transaction ''" + transactionID + "''';";
			} else if (ProcessTitle.contains("Deactivate Vianeos Retail Subscription")) {
				sqlQuery = "SELECT ExecutionStatus\r\n"
						+ "FROM [SOMUnified_Transaction_V20230507].[bp].[BPInstance]\r\n"
						+ "WHERE Title = 'Deactivate Vianeos Retail Subscription' \r\n"
						+ "    AND InputArgument LIKE '%' +\r\n"
						+ "    (SELECT TOP(1) CONVERT(NVARCHAR(36), ID)  \r\n"
						+ "     FROM [MELUnified_Configuration_V20230507].[VRPartyOrderABE].[PartyOrder]\r\n"
						+ "     WHERE CustomerID IN     (SELECT PartyID\r\n"
						+ "          FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
						+ "          WHERE ForeignID = '"+ID+"')\r\n"
						+ "     AND SpecificationID = '1EFDC6A5-5894-4951-A5B6-E36595297CD0')   +   '%' \r\n"
						+ "ORDER BY ID DESC;";
			} else if (ProcessTitle.contains("Reactivate Vianeos Retail Product")) {
				sqlQuery = "SELECT ExecutionStatus\r\n"
						+ "FROM [SOMUnified_Transaction_V20230507].[bp].[BPInstance]\r\n"
						+ "WHERE Title = 'Reactivate Vianeos Retail Product' \r\n"
						+ "    AND InputArgument LIKE '%' + \r\n" + "    (SELECT TOP(1) CONVERT(NVARCHAR(36), ID)  \r\n"
						+ "     FROM [MELUnified_Configuration_V20230507].[VRPartyOrderABE].[PartyOrder]\r\n"
						+ "     WHERE CustomerID IN \r\n" + "         (SELECT PartyID\r\n"
						+ "          FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
						+ "          WHERE ForeignID = '"+ID+"') \r\n"
						+ "     AND SpecificationID = '8B52A8AA-F8B8-4A4E-9DA2-6E0E5FF197ED') + \r\n" + "     '%' \r\n"
						+ "ORDER BY ID DESC;";
			} else {

				sqlQuery = "DECLARE @id VARCHAR(255);\r\n" + "\r\n" + "SELECT @id = ID\r\n"
						+ "FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[Party]\r\n" + "WHERE ID IN (\r\n"
						+ "    SELECT PartyID\r\n"
						+ "    FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
						+ "    WHERE ForeignID = '" + ID + "'\r\n" + ");\r\n" + "\r\n" + "\r\n"
						+ "DECLARE @sql NVARCHAR(MAX);\r\n" + "\r\n" + "SET @sql = N'\r\n"
						+ "    SELECT ExecutionStatus\r\n"
						+ "    FROM [SOMUnified_Transaction_V20230507].[bp].[BPInstance]\r\n" + "    WHERE Title=''"
						+ ProcessTitle + "'' and InputArgument LIKE ''%' + @id + '%''\r\n" + "    ORDER BY ID DESC\r\n"
						+ "';\r\n" + "\r\n" + "EXEC sp_executesql @sql;";
			}


			// Create a statement and execute the query
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String ExecutionStatus = "";
			if (resultSet.next()) {
				ExecutionStatus = resultSet.getString("ExecutionStatus");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			// Use the retrieved ID in your API request
			String jsonBody = "{\"ExecutionStatus\":\"" + ExecutionStatus + "\"}";
			System.out.println("ExecutionStatus: " + jsonBody);
			return ExecutionStatus;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";

	}

	// Send OTP Email
	public void SendOTP(String email, String OTP) {

		try {

			String requestBody = "{\"email\":\"" + email + "\",\r\n" + "\"otp\":\"" + OTP + "\"}";

			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.post("/api/orbitNow/notification/sendOTPEmail").then().statusCode(202);
			System.out.println("Send OTP Email sent succesfully!");

		} catch (Exception ex) {

			System.out.println("Send OTP Email Failed!... Exception: " + ex);

		}
	}

	// Send PIN Code Email
	public void SendPinCode(String email, String Pincode) {

		try {

			String requestBody = "{\"email\":\"" + email + "\",\r\n" + "\"pinCode\":\"" + Pincode + "\"}";

			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.post("/api/orbitNow/notification/sendPINCodeEmail").then().statusCode(202);

			System.out.println("Pin Code Email sent succesfully!");
		} catch (Exception ex) {

			System.out.println("Pin Code Email Failed!... Exception: " + ex);

		}

	}

	public void AddCustMobileNumberForEmail(String CustID) {

		try {

			String CustStatus = "";

			while (CustStatus.equals("")) {
				sleep(20000);
				CustStatus = CheckifCustActivated();
			}
			if (CustStatus.equals(RegisteredVerifiedID)) {
				RandomNumber = generateRandomLebaneseNumber();
				System.out.println("The new number to be added: " + RandomNumber);

				String requestBody = "{\"customerId\":\"" + CustID + "\",\r\n" + "\"mobileNumber\":\"" + RandomNumber
						+ "\"}";

				given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
						.put("/api/orbitNow/customer/addMobileNumber").then().statusCode(202);

				CheckOrderStatus("Add Mobile Number");

				System.out.println("Add Customer Mobile Number was succesfully added!");
			}

		} catch (Exception ex) {

			System.out.println("Add Customer Mobile Number Failed!... Exception: " + ex);

		}

	}

	public static String generateRandomLebaneseNumber() {
		Random random = new Random();
		int min = 100000; // Minimum 6-digit number
		int max = 999999; // Maximum 6-digit number
		int randomNumber = random.nextInt(max - min + 1) + min;

		String number = "96171" + randomNumber;

		return number;
	}

	public String CheckifCustActivated() {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "  Select StatusID\r\n"
					+ "  FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
					+ "  where ForeignID='" + ID + "'";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String statusID = "";
			if (resultSet.next()) {
				statusID = resultSet.getString("StatusID");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			// Use the retrieved ID in your API request
			// String jsonBody = "{\"Id\":\"" + id + "\"}";
			if (statusID.equals(RegisteredVerifiedID)) {
				System.out.println("Customer is Register Verfied!");
				return statusID;
			} else {
				System.out.println("Customer still Register Not Verfied!");
			}
			return "";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";

	}

	public void UpdateEmail(String CustID) {

		try {

			String newEmail = "";

			String CustStatus = "";

			while (CustStatus.equals("")) {
				sleep(20000);
				CustStatus = CheckifCustActivated();
			}
			if (CustStatus.equals(RegisteredVerifiedID)) {
				newEmail = generateRandomEmail();
				System.out.println("The new Email to be added is: " + newEmail);

				String requestBody = "{\"customerId\":\"" + CustID + "\",\r\n" + "\"email\":\"" + newEmail + "\"}";

				given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
						.put("/api/orbitNow/customer/updateEmail").then().statusCode(200);
			}
			CheckifEmailUpdated(CustID, newEmail);
			randomEmail = newEmail;
		} catch (Exception ex) {
			System.out.println("UpdateEmail Failed with this exception: " + ex);

		}

	}

	public boolean CheckifEmailUpdated(String CustID, String newEmail) {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "  Select Username\r\n"
					+ "  FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
					+ "  where ForeignID='" + CustID + "'";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String Username = "";
			if (resultSet.next()) {
				Username = resultSet.getString("Username");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			if (Username.equals(newEmail)) {
				System.out.println("Customer Email been updated successfully!");
				return true;
			} else {
				System.out.println("Customer Email Update Failed, didn't update in database!");
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;

	}

	public void UpdateCustomerTimeZone(String CustID, String NewTimeZone) {

		try {
			System.out.println("TimeZone before update API: " + TimeZone);

			String requestBody = "{\"customerId\":\"" + CustID + "\",\r\n" + "\"userTimezone\":\"" + NewTimeZone
					+ "\"}";

			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.put("/api/orbitNow/customer/updateTimeZone").then().statusCode(200);

			String NewUpdatedTimeZone = CheckifTimeZoneUpdated(CustID);

			System.out.println("TimeZone after update API: " + NewUpdatedTimeZone);

		} catch (Exception ex) {
			System.out.println("UpdateCustomerTimeZone Failed with this exception: " + ex);

		}

	}

	public String CheckifTimeZoneUpdated(String CustID) {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "  SELECT tz.Name\r\n"
					+ "FROM [MELUnified_Configuration_V20230507].[common].[VRTimeZone] AS tz\r\n"
					+ "INNER JOIN [MELUnified_Configuration_V20230507].[VRPartyABE].[Party] AS p\r\n"
					+ "    ON tz.ID = p.TimeZoneID\r\n"
					+ "INNER JOIN [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails] AS pd\r\n"
					+ "    ON p.ID = pd.PartyID\r\n" + "WHERE pd.ForeignID = '" + CustID + "';";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String TZName = "";
			if (resultSet.next()) {
				TZName = resultSet.getString("Name");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			return TZName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";

	}

	public void CancelSubscription(String CustID) {

		try {

			CheckOrderStatus("Evaluate Payment for Transaction");

			String subscriptionId = GetSubscriptionId(CustID);

			System.out.println(subscriptionId + " - " + CustID);

			String requestBody = "{\"subscriptionId\":\"" + subscriptionId + "\",\"customerId\":\"" + CustID + "\"}";

			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.put("/api/orbitNow/subscription/cancel").then().statusCode(202);

			CheckOrderStatus("Deactivate Vianeos Retail Subscription");
			if (CheckIfCanceled(CustID)) {
				System.out.println("The subscription is set to Active Temporarily");
			} else {
				System.out.println("The subscription is Active");
			}

		} catch (Exception ex) {
			System.out.println("Cancel Subscription Failed with this exception: " + ex);

		}
	}

	public String GetSubscriptionId(String CustID) {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "SELECT RC.Name\r\n"
					+ "FROM [MELUnified_Configuration_V20230507].[ProductCatalog].[Resource] RC\r\n"
					+ "INNER JOIN [MELUnified_Configuration_V20230507].[ProductCatalog].[ProductResource] PR\r\n"
					+ "    ON RC.ID = PR.ResourceID\r\n"
					+ "INNER JOIN [MELUnified_Configuration_V20230507].[ProductCatalog].[Product] P\r\n"
					+ "    ON PR.ProductID = P.ID\r\n"
					+ "INNER JOIN [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails] PD\r\n"
					+ "    ON P.CustomerID = PD.PartyID\r\n" + "WHERE PD.ForeignID = '" + CustID + "';";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String SubscriptionId = "";
			if (resultSet.next()) {
				SubscriptionId = resultSet.getString("Name");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			return SubscriptionId;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";

	}

	public boolean CheckIfCanceled(String CustID) {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "	SELECT P.StatusID\r\n"
					+ "FROM [MELUnified_Configuration_V20230507].[ProductCatalog].[Product] P\r\n"
					+ "INNER JOIN [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails] PD\r\n"
					+ "    ON P.CustomerID = PD.PartyID\r\n" + "WHERE PD.ForeignID = '" + CustID + "'\r\n"
					+ "    AND P.ParentProductID IS NULL\r\n" + "    AND P.ProductNumber IS NOT NULL;";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String SubscriptionStatus = "";
			if (resultSet.next()) {
				SubscriptionStatus = resultSet.getString("StatusID");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			if (SubscriptionStatus.equals("8E9A4792-7CEE-43EE-9353-AA8924A847BC")) {
				return true;
			}
			return false;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public void RedeemVoucher(String code, String CustID) {

		try {
			String subsID = GetSubscriptionId(CustID);

			String requestBody = "{\"subscriptionId\":\"" + subsID + "\",\r\n" + "\"promoCode\":\"" + code + "\"}";

			given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
					.post("/api/orbitNow/subscription/redeem").then().statusCode(202);

			System.out.println("Voucher Redeemed!");
		} catch (Exception ex) {
			System.out.println("Redeem Voucher API failed with this Exceprion: " + ex);
		}

	}

	public String ChangePaymentMethod(String CustID, String paymentMethodTypeId) {

		try {

			CheckOrderStatus("Evaluate Payment for Transaction");

			String requestBody = "{\"customerId\":\"" + CustID + "\",\r\n"
					+ "\"paymentCompleteReturnURL\":\"https://www.google.com/\",\r\n"
					+ "\"languageCode\":\"en-US\",\r\n" + "\"email\":\"hhmaidan@vanrise.com\",\r\n"
					+ "\"givenName\":\"Hassan\",\r\n" + "\"surname\":\"Hm\",\r\n" + "\"paymentMethodTypeId\":\""
					+ paymentMethodTypeId + "\"\r\n" + "}";

			Response response = RestAssured.given().contentType("application/json").body(requestBody)
					.header("Auth-Token", authToken).when().put("/api/orbitNow/customer/changePaymentMethod");

			if (response.getBody().asString().contains("\"data\":{\"requiresPayment\":true,\"paymentURL\"")) {
				System.out.println("Payment Method changed successfully!");
				System.out.println("The new PaymentURL: " + response.getBody().asString());

				String ChangeURLToPay = response.path("data.paymentURL");

				// Print the paymentURL
				System.out.println("The new PaymentURL: " + ChangeURLToPay);

				return ChangeURLToPay;
			} else {
				System.out.println("Change Payment Method failed with this Error: " + response.getBody().asString());
			}
		} catch (Exception ex) {
			System.out.println("Change Payment Method failed with this Exceprion: " + ex);
		}
		return "";

	}

	public String ReactivateSubscription(String CustID, String PaymentMethod) {

		try {
			String subsID = GetSubscriptionId(CustID);

			String requestBody = "{\"subscriptionId\":\"" + subsID + "\",\r\n" + "\"customerId\":\"" + CustID
					+ "\",\r\n" + "\"paymentCompleteReturnURL\":\"https://www.google.com/\",\r\n"
					+ "\"languageCode\":\"en-US\",\r\n" + "\"email\":\"hhmaidan98@vanrise.com\",\r\n"
					+ "\"givenName\":\"Hassan\",\r\n" + "\"surname\":\"Hm\",\r\n" + "\"paymentMethodTypeId\":\""
					+ PaymentMethod + "\"\r\n" + "}\r\n" + "";

			Response response = RestAssured.given().contentType("application/json").body(requestBody)
					.header("Auth-Token", authToken).when().put("/api/orbitNow/subscription/reactivate");

			if (response.getBody().asString().contains("\"data\":{\"requiresPayment\":true,\"paymentURL\"")) {
				System.out.println("Reactivate Subscription was Sucessfull!");
				System.out.println("PaymentURL for Reactivation: " + response.getBody().asString());

				String ReactivationURLToPay = response.path("data.paymentURL");

				// Print the paymentURL
				System.out.println("PaymentURL for Reactivation: " + ReactivationURLToPay);

				return ReactivationURLToPay;
			} else {
				System.out
						.println("Subscription Reactivation failed with this Error: " + response.getBody().asString());
			}
		} catch (Exception ex) {
			System.out.println("Subscription Reactivation failed with this Exceprion: " + ex);
		}
		return "";

	}

	public boolean CheckIfSubscriptionActive(String CustID) {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "	SELECT P.StatusID\r\n"
					+ "FROM [MELUnified_Configuration_V20230507].[ProductCatalog].[Product] P\r\n"
					+ "INNER JOIN [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails] PD\r\n"
					+ "    ON P.CustomerID = PD.PartyID\r\n" + "WHERE PD.ForeignID = '" + CustID + "'\r\n"
					+ "    AND P.ParentProductID IS NULL\r\n" + "    AND P.ProductNumber IS NOT NULL;";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String SubscriptionStatus = "";
			if (resultSet.next()) {
				SubscriptionStatus = resultSet.getString("StatusID");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			if (SubscriptionStatus.equals("11FDC8A1-F6D0-4021-BC1F-5A067B96D745")) {
				return true;
			}
			return false;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;

	}

	public void GetPayment(String CustID) {

		try {

			Response response = given().contentType("application/json").param("customerId", CustID).param("limit", "6")
					.header("Auth-Token", authToken).when().get("/api/orbitNow/payment/customerPayment").then()
					.extract().response();

			System.out.println("The Get Payment API: " + response.getBody().asString());

		} catch (Exception ex) {
			System.out.println("Get Payment API failed with this Exceprion: " + ex);
		}

	}

	public void GetPaymentReceipt(String CustID) {

		try {

			Response response = given().contentType("application/json").param("customerId", CustID)
					.param("paymentId", GetPaymentID(CustID)).header("Auth-Token", authToken).when()
					.get("/api/orbitNow/payment/receipt").then().extract().response();

			System.out.println("The Get Payment Receipt API: " + response.getBody().asString());

		} catch (Exception ex) {
			System.out.println("Get Payment Receipt API failed with this Exceprion: " + ex);
		}

	}

	public String GetPaymentID(String CustID) {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

			String sqlQuery = "Select ID\r\n"
					+ "FROM [MELUnified_Configuration_V20230507].[VRPartyPaymentABE].[PartyPayment]\r\n"
					+ "  where CustomerID=(SELECT PartyID\r\n"
					+ "          FROM [MELUnified_Configuration_V20230507].[VRPartyABE].[PartyDetails]\r\n"
					+ "          WHERE ForeignID = '" + CustID + "')";

			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String PaymentId = "";
			if (resultSet.next()) {
				PaymentId = resultSet.getString("ID");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			return PaymentId;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";

	}

	public void GetPaymentMethod(String CustID) {

		try {

			Response response = given().contentType("application/json").param("customerId", CustID)
					.header("Auth-Token", authToken).when().get("/api/orbitNow/payment/paymentMethodInfo").then()
					.extract().response();

			System.out.println("The Get Payment Method API: " + response.getBody().asString());

		} catch (Exception ex) {
			System.out.println("Get Payment Method API failed with this Exceprion: " + ex);
		}
	}

	public void GetSubscriptionDetails(String CustID) {

		try {

			Response response = given().contentType("application/json").param("customerId", CustID)
					.header("Auth-Token", authToken).when().get("/api/orbitNow/subscription/details").then().extract()
					.response();

			System.out.println("The Get Subscription Details API: " + response.getBody().asString());

		} catch (Exception ex) {
			System.out.println("Get Subscription Details API failed with this Exceprion: " + ex);
		}
	}

	public void GetSubscriptionCancellationSurvey(String languageCode) {

		try {

			Response response = given().contentType("application/json").param("languageCode", languageCode)
					.header("Auth-Token", authToken).when().get("/api/orbitNow/survey/subscriptionCancellation").then()
					.extract().response();

			System.out.println("The Get Subscription Cancellation Survey API: " + response.getBody().asString());

		} catch (Exception ex) {
			System.out.println("Get Subscription Cancellation Survey API failed with this Exceprion: " + ex);
		}
	}

	public void GetProducts(String countryCode) {

		try {

			Response response = given().contentType("application/json").param("countryCode", countryCode)
					.header("Auth-Token", authToken).when().get("/api/orbitNow/catalog/product").then().extract()
					.response();

			System.out.println("The Get Product API: " + response.getBody().asString());

		} catch (Exception ex) {
			System.out.println("Get Product API failed with this Exceprion: " + ex);
		}
	}

	public void GetVoucher(String promoCode, String languageCode) {

		try {

			Response response = given().contentType("application/json").param("promoCode", promoCode)
					.param("languageCode", languageCode).header("Auth-Token", authToken).when()
					.get("/api/orbitNow/voucher/get").then().extract().response();

			System.out.println("The Get Voucher API: " + response.getBody().asString());

		} catch (Exception ex) {
			System.out.println("Get Voucher API failed with this Exceprion: " + ex);
		}
	}

	public void DeleteCustomer(String CustID) {

		try {

			Response response = given().contentType("application/json").param("customerId", CustID)
					.header("Auth-Token", authToken).when().get("/api/orbitNow/customer/delete").then().extract()
					.response();

			if (response.getBody().asString().contains("ACTV-SUBS-EXST")) {
				System.out.println("Active Subscription Exists: " + response.getBody().asString());
			} else if (response.getBody().asString().contains("CUST-INACTIVE")) {
				System.out.println("Customer is inactive: " + response.getBody().asString());
			} else {

				CheckDelecteOrderStatus("Delete Customer", CustID);

				System.out.println("Customer will be deleted! \n" + response.getBody().asString());
			}

		} catch (Exception ex) {
			System.out.println("Delete Customer API failed with this Exceprion: " + ex);
		}

	}

	public void CheckDelecteOrderStatus(String ProcessTitle, String CustID) {

		int maxRetries = 20; // Maximum number of retry attempts
		int retryCount = 0;

		while (retryCount < maxRetries) {

			String status = GetDeleteOrderStatus(ProcessTitle, CustID);

			if (status.equals("50")) {
				// Success, the desired response is received
				System.out.println(ProcessTitle + " Order Completed.");

				break; // Exit the loop
			} else {
				// Retry after a delay (e.g., sleep for a while)
				retryCount++;
				System.out.println(ProcessTitle + " Order still pending. Retrying attempt " + retryCount + "...");
				sleep(10000); // Sleep for 5 seconds before the next retry (adjust as needed)
			}
		}

		if (retryCount == maxRetries) {
			System.out.println("Maximum retries reached. Activation failed.");
		}

	}

	public String GetDeleteOrderStatus(String ProcessTitle, String CustID) {

		try {
			// Connect to the database
			Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
			String sqlQuery = "Select ExecutionStatus\r\n"
					+ "FROM [SOMUnified_Transaction_V20230507].[bp].[BPInstance]\r\n"
					+ "WHERE Title = 'Delete Customer' AND InputArgument LIKE '%"+CustID+"%'";

			// Create a statement and execute the query
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sqlQuery);

			// Retrieve the ID from the result set
			String ExecutionStatus = "";
			if (resultSet.next()) {
				ExecutionStatus = resultSet.getString("ExecutionStatus");
			}

			// Close the database resources
			resultSet.close();
			statement.close();
			connection.close();

			// Use the retrieved ID in your API request
			String jsonBody = "{\"ExecutionStatus\":\"" + ExecutionStatus + "\"}";
			System.out.println("ExecutionStatus: " + jsonBody);
			return ExecutionStatus;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";

	}

	public void AddEmailforMobileUser(String CustID) {
		try {

			String CustStatus = "";
			String newEmail="";
			while (CustStatus.equals("")) {
				sleep(10000);
				CustStatus = CheckifCustActivated();
			}
			if (CustStatus.equals(RegisteredVerifiedID)) {
				newEmail = generateRandomEmail();
				System.out.println("The new Email to be added: " + newEmail);
				String requestBody = "{\"customerId\":\"" + CustID + "\",\"email\":\"" + newEmail + "\"}";

				given().contentType("application/json").body(requestBody).header("Auth-Token", authToken).when()
						.put("/api/orbitNow/customer/changeEmail").then().statusCode(202);

				CheckOrderStatus("Change Email");

				System.out.println("Change Customer Email API used for Mobile was sucessfull!");
			}

		} catch (Exception ex) {
			System.out.println(
					"Change Customer Email API used for Mobile Failed with this exception: " + ex.getMessage());

		}

	}

}
