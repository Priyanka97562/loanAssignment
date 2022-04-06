# LoanAssignment

Project Details:

- Pre-Requisites:
Java11 and Gradle7

- Build Project - ./gradlew clean build
- Run the project: ./gradlew bootRun
  (or) 
Run the application file in IntelliJ
    
Running this project auto generated assignments.csv and yields.csv in folder output in the root directory of the project

To force another loan assignment process, hit the endpoint 

- POST call - http://localhost:8080/assign_loan with no body in the request (or) curl --location --request POST 'http://localhost:8080/assign_loan'

This wil force another quick loan assignment and generate the latest files

Other details:

1. How long did you spend working on the problem? What did you find to be the most difficult part?

- Spent around 5-6 hours on the project.
Few challenges that I had to reiterate my work on:
- Covenants can have blank facility id, blank max_default_likelihood and 
blank banned_state, tried to have all these blanks and generate a new covenant for a facility
but while parsing 1st and last columns that are blank were not being stored in the string array 
with default delimiter of "," . Had to reiterate on this a bit to make this work.
This scenario described is not present in the lager data set too but could very well be a scenario 
in real life according to problem description
- Initially implemented in a way where a Covenant for a facility if occurred again in covenants file
, I assumed this would override the previous mentioned covenant but later reiterate to keep adding covenants
for a specific facility id
- Very small thing that took me a while was me missing the pointing of rounding the yield to nearest cent, due to which 
everytime I tried to test my code with small data I was off by 1 or 2 and spent a while debugging what I was missing
to land to the right answer

2. How would you modify your data model or code to account for an eventual introduction of new, as-of-yet unknown types of covenants, 
beyond just maximum default likelihood and state restrictions?

In the current algorithm, the only way to do is modify the covenant model and add it 
the additional check in AssignLoanService.class.

3. How would you architect your solution as a production service wherein new facilities can be introduced at arbitrary points in time. 
Assume these facilities become available by the finance team emailing your team and describing the addition with a new set of CSVs.

- It can be done in multiple ways, one of the easiest being exposing an endpoint to 
add/update additional facilities that will get append to the end of the facilities file
- Design the application in a way that it listens to change event.
Example: write a kafka producer/consumer service to produce an event everytime file 
is changed which is then consumed by the loanAssignment service and gets updated without
the engineering team having to update
- For critical applications, having these files map to database tables is one way to go,so it is easier
to perform CRUD operations and deployed service need not be touched. And for the tables that does not change
often, they can be cached at application layer
- Not very intuitive but this approach can be taken for non-critical applications -
stores these files in application config and not in source code, so that these files can be changed
and redeployed during off hours.

4. Your solution most likely simulates the streaming process by directly calling a method in your code to process the loans 
inside of a for loop. What would a REST API look like for this same service? 
Stakeholders using the API will need, at a minimum, to be able to request a loan be 
assigned to a facility, and read the funding status of a loan, as well as query the capacities remaining in facilities.


Request a loan would be
POST - api/v1/loan
Request body - {
  amount : 10000,
  default_likelihood: 0.02,
  interest_rate: 0.25,
  state: "MN"
} 

With the above information, loanAssignment service should be able to assign a
facility and respond with loan status

Read the funding status of a loan
Get - api/v1/loan/{loan_id}

Response - {
  facility_id : 1,
  status: "",
  interest_rate: 0.25
} 

Capacities remaining in the facilities
Get - api/v1/facility/{facility_id}

Response - {
  facility_id : 1,
  remaining_Amount: 120000,
  interest_rate: 0.25,
  covenants : [
  {
    "default_likelihood": 0.5,
    "banned_state": "MN"
  }
  ]
} 

5. How might you improve your assignment algorithm if you were permitted to assign loans in batch rather than streaming? 
We are not looking for code here, but pseudo code or description of a revised algorithm appreciated.

Would make use of Kafka consumer to consumer loans as they come in and batch process them
asynchronously

6. Discuss your solution’s runtime complexity.

- parsing CSV files is O(n) where n is size of input 
+ storing the read files into DS is O(n)
- Collection.sort used here is O(nlogn) with n being number of facilities + Calculate assignment is O(m*n) where m is number of loans and n is number of facilities => O(m*n)
as m which is number of loans is fairly larger.













  