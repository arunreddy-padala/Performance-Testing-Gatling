# Performance_Testing_with_Gatling

This repository houses the testing framework developed for robust automated testing of our web application. Utilizing a suite of sophisticated tools and methods, this framework ensures thorough validation, dynamic data handling, and scalable load testing.

Key Features: 
1. Dynamic Data Capture and Usage
- Dynamic References: Avoids hardcoded values through CSS and regex checks, capturing dynamic data at runtime and storing these for subsequent tests.
- CSS/Regex Checks: Validates initial page load and interactive elements accurately to ensure the application's UI is rendered as expected.
2. Modular Test Architecture
- Individual Classes: Transactions and scenarios are encapsulated within individual classes, enhancing maintainability and reusability.
- Validation Checks: Uses CSS selectors to confirm transaction states and interactions within the application.
3. Data-driven Testing
- Feeder Integration: Employs feeders to externalize test data, facilitating easy updates and scalability.
- Session Management: Manages user sessions effectively, tracking changes and statuses throughout the testing process.
4. Enhanced Load Testing
- Throttling and Duration Control: Implements controlled load increments and sustained testing periods to simulate real-world usage.
- Sequential Execution: Ensures orderly execution of test scenarios with the andThen() method, maintaining the logical flow of operations.
5. Pre/Post Test Configurations
- Lifecycle Hooks: Utilizes @Before and @After annotations to set up preconditions and clean up after tests, ensuring each test starts with the correct setup.
6. Content Validation and Monitoring
- JSON Path and JMESPath: Validates API responses and extracts data dynamically to verify backend processes accurately.
- Expression Language Support: Enhances the flexibility of validation checks and assertions within the tests.
