# Smart Campus API Documentation & Conceptual Answers

## API Design Overview
The Smart Campus API is a RESTful system designed to manage rooms, sensors, and sensor readings within a campus environment. It follows a Resource-oriented architecture using JAX-RS (Jersey) and is hosted via the Grizzly HTTP server. The design prioritizes:
- **Hierarchical Resources**: Rooms act as parent containers for sensors, which in turn generate readings.
- **HATEOAS**: A root discovery endpoint provides dynamic links to navigate the API without hardcoded URLs.
- **Input Validation**: Strict schema and referential integrity checks ensure that sensors are only registered in existing rooms.
- **Robust Error Handling**: Standardized JSON error responses and custom exception mappers replace raw stack traces for better security and developer experience.

## Build and Launch Instructions

### Option 1: Using the Terminal (Embedded Grizzly Server)
This is the fastest way to test the API during development without needing a full server installation.
1. Ensure you have **Java 21** and **Maven** installed.
2. Open your terminal in the project root directory.
3. Run the following command:
   ```bash
   mvn clean compile exec:java
   ```
4. The server will start at `http://localhost:8080/api/v1/`.

### Option 2: Using NetBeans IDE
1. Open **NetBeans IDE**.
2. Go to **File > Open Project** and select the folder containing the `pom.xml`.
3. Right-click the project in the sidebar and select **Run**. 
4. NetBeans is configured to use the Maven `exec` goal to launch the `GrizzlyLocalServer`.

### Option 3: Using an External Container (Apache Tomcat 9)
This project is configured as a `war` application for deployment to standard Java EE 8 containers.
1. Run the following command to build the production archive:
   ```bash
   mvn clean package
   ```
2. Locate the generated file at `target/ROOT.war`.
3. Copy this file into the `webapps` directory of your **Tomcat 9** installation.
4. Once Tomcat starts, the API will be available at `http://localhost:8080/api/v1/` (if named ROOT.war) or `http://localhost:8080/client-server-architectures-cw/api/v1/` depending on the file name.

## Sample API Interactions (CURL)
Below are five sample commands to interact with the API once the server is running.

1. **Root Discovery**: Explore the API entry point and navigation links.
   ```bash
   curl -X GET http://localhost:8080/api/v1/
   ```

2. **Register a New Room**: Add a library study room to the system.
   ```bash
   curl -X POST http://localhost:8080/api/v1/rooms \
   -H "Content-Type: application/json" \
   -d '{"id":"LIB-301", "name":"Library 3rd Floor"}'
   ```

3. **Retrieve All Rooms**: List all registered rooms.
   ```bash
   curl -X GET http://localhost:8080/api/v1/rooms
   ```

4. **Add a Sensor to a Room**: Register a temperature sensor for the newly created room.
   ```bash
   curl -X POST http://localhost:8080/api/v1/sensors \
   -H "Content-Type: application/json" \
   -d '{"id":"TEMP-001", "type":"TEMPERATURE", "roomId":"LIB-301"}'
   ```

5. **Search/Filter Sensors**: Find all sensors of type 'TEMPERATURE'.
   ```bash
   curl -X GET "http://localhost:8080/api/v1/sensors?type=TEMPERATURE"
   ```

---


## Part 1: Service Architecture & Setup

**Question 1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**

**Answer:**
The default lifecycle of a JAX-RS resource class is **per-request**. A new instance of the resource class is instantiated by the JAX-RS runtime for every single incoming HTTP request, and it is subsequently destroyed, allowing for garbage collection once the HTTP response is dispatched. It is not treated as a singleton by default.

This architectural decision has significant implications for state management, especially when using in-memory data structures instead of an external database. Because a new resource class is created per request, we cannot use standard instance variables (e.g., `private Map<String, Room> rooms = new HashMap<>();`) to safely persist data across multiple client requests; the data would be lost as soon as the request ends. 

To prevent data loss, the data structures must be shared across all request instances. This is typically achieved by making the maps/lists `static`, or by using Dependency Injection (like HK2 in Jersey) to inject a Singleton data store instance into the resource. Furthermore, since multiple HTTP requests (handled by separate threads in the servlet container) might attempt to read from or write to these shared structures concurrently, we must use thread-safe data structures, such as `ConcurrentHashMap` and `CopyOnWriteArrayList`, to prevent race conditions and ensure data integrity.

---

**Question 2: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

**Answer:**
The provision of Hypermedia (Hypermedia As The Engine Of Application State, or HATEOAS) is considered a hallmark of advanced RESTful design because it allows the API to become self-descriptive and dynamically navigable. Instead of relying purely on out-of-band knowledge to guess URL endpoints, the server embeds contextual navigation links directly into its JSON responses, effectively guiding the client on what actions are possible next.

Compared to static documentation, HATEOAS offers several substantial benefits for client developers:
1. **Reduced Coupling:** Clients do not need to hard-code complex URL structures or base paths. They only rely on the initial entry point, and dynamically parse the provided links (like `['links']['rooms']`) to traverse the API.
2. **Resilience to Change:** If the backend developers need to refactor or reorganize the internal URL hierarchy, they simply update the hypermedia links being returned. Compliant client applications will automatically adapt to the new URLs without breaking or requiring code changes.
3. **State Discoverability:** The server can selectively include or omit links based on the current state of a resource. For instance, if an action is currently forbidden, the corresponding link just won't be provided to the client, simplifying the client's internal display logic (e.g., automatically hiding a "delete" button).

## Part 2: Room Management

**Question 1: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**

**Answer:**
Returning an entire object listing (like returning the `name`, `capacity`, and complete inner properties for 1000 rooms) rapidly inflates the overall HTTP response payload size. This consumes significant server bandwidth, causes latency, and takes longer for clients—especially mobile or edge clients—to parse and deserialize into memory. 

Conversely, returning just an array of ID references (`["LIB-301", "LAB-5"]`) minimizes immediate bandwidth transfer. The payload is tiny, and enables the client to selectively fetch full objects only for the specific rooms they require via isolated `GET /rooms/{id}` requests later. However, this optimization introduces the "N+1 query problem" overhead. If a client UI genuinely requires displaying the complete names of all 1000 rooms immediately, forcing them to make 1 initial request for the IDs, followed by 1000 separate `GET` requests for the objects, will ultimately cause much worse network latency than if the server had just returned the full objects initially.

---

**Question 2: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

**Answer:**
Yes, the `DELETE` operation in this implementation fulfills the constraints of idempotency. Idempotency guarantees that executing the same request multiple times achieves the exact same terminal effect on the server state as executing it exactly once. 

In our implementation, if a client sends the first `DELETE /rooms/LIB-301` request for a valid room, the server successfully removes the room from the `ConcurrentHashMap` and returns a `200 OK` status. If the client inadvertently sends that exact same `DELETE` request again immediately after (e.g., due to a network retry loop), the server checks the map, realizes the room no longer exists, and subsequently fires a `404 Not Found` exception. 

Crucially, even though the returned HTTP status code changed for the client (from 200 to 404), the *underlying server state*—that the target room is deleted/does not exist—remains identical after the first request and after the thousandth request. Therefore, the architectural principle of idempotency is preserved entirely.

## Part 3: Sensor Operations & Linking

**Question 1: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

**Answer:**
When `@Consumes(MediaType.APPLICATION_JSON)` is applied to an endpoint, the JAX-RS framework strictly restricts incoming requests to those providing a `Content-Type: application/json` header. If a client attempts to supply a different data format like `text/plain` or `application/xml` (or omits the header altogether), JAX-RS immediately intercepts the request before it even reaches the method's logic. 

It handles this mismatch gracefully by automatically throwing an exception internally and generating an HTTP `415 Unsupported Media Type` response back to the client. This enforces a strict contract, ensuring that the backend logic safely assumes it will only ever need to parse JSON inputs for that endpoint, avoiding unexpected serialization errors.

---

**Question 2: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**

**Answer:**
RESTful architectural principles dictate that the URL Path should be reserved to uniquely identify a specific *Resource* or a distinct hierarchical collection of resources (e.g., `/api/v1/sensors/TEMP-001` explicitly identifies a single sensor). Variables that merely shape, order, or filter the representation of a base collection are not distinct resources themselves. This is why query parameters (`?type=CO2`) are the preferred semantics for search and filtration operations.

By leveraging `@QueryParam`, the primary entry point `/api/v1/sensors` correctly remains the one-stop collection. It naturally supports combining multiple optional filters simultaneously directly from the client (e.g., `?type=CO2&status=ACTIVE`) without forcing backend developers to arbitrarily nest combinations of massive path permutations (`/sensors/type/CO2/status/ACTIVE`), which breaks routing flexibility and violates the core meaning of hierarchical Path URIs.

## Part 4: Deep Nesting with Sub-Resources

**Question 1: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?**

**Answer:**
The Sub-Resource Locator pattern allows JAX-RS to dynamically delegate requests to child classes. Instead of one massive "God Object" controller, this modular approach offers several benefits:
1. **Focus and Modularity:** Each class handles its specific domain. `SensorResource` manages sensors, while `SensorReadingResource` manages the nested reading history. This keeps the codebase clean and easy to navigate.
2. **Context Injection:** Parent context (like a `sensorId`) can be passed to the sub-resource via the constructor, removing the need to repeatedly parse path parameters in every child method.
3. **Reusability:** Developers can reuse the same sub-resource controller across different logical branches of the API, promoting DRY principles and consistent behavior.

## Part 5: Error Handling, Exception Mapping & Logging

**Question 1 (Part 5.2): Why is HTTP 422 Unprocessable Entity often considered more semantically accurate than a standard 404 Not Found when the issue is a missing reference inside a valid JSON payload?**

**Answer:**
HTTP `404 Not Found` communicates that the *target resource of the request itself* — the URL endpoint being addressed — could not be found. In the context of `POST /api/v1/sensors`, the endpoint clearly does exist; the server found and processed it. Using a `404` in this scenario would therefore be misleading and technically inaccurate.

HTTP `422 Unprocessable Entity` is semantically far more precise for this situation. It signals that the server successfully received the request, successfully parsed the JSON body syntactically, but could not semantically process the instruction because the *content of the payload* is logically invalid. Specifically, the `roomId` field contains a value that references a non-existent entity. The JSON structure is perfectly formed; the reasoning behind the payload is simply impossible to fulfill. This is a distinction between a *syntactic* error (malformed JSON → `400`) and a *semantic* or *referential integrity* error (valid JSON, but impossible business operation → `422`).

---

**Question 2 (Part 5.4): From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

**Answer:**
Exposing Java stack traces constitutes a significant **information disclosure vulnerability**. Attacker can gather sensitive details such as:
1. **Technology Fingerprinting:** Package names reveal exact library versions (e.g., Jersey, Tomcat), helping attackers find specific known CVEs.
2. **Architecture Leaks:** Method names disclose internal package structures and data access layers, giving a map of the application's internals.
3. **Attack Surface Mapping:** Knowing exact line numbers for errors allows attackers to craft targeted payloads to exploit specific code paths.
4. **Environment Details:** Traces can leak absolute file paths or directory structures on the server that should remain private.

---

**Question 3 (Part 5.5): Why is it advantageous to use JAX-RS Filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**

**Answer:**
Using Filters for logging is a superior **Aspect-Oriented** approach compared to manual logging statements:
1. **Centralization:** Logging logic exists in one place. Changing formats or frameworks requires modifying only one file, not dozens of resource classes.
2. **Guaranteed Coverage:** Filters intercept *every* request and response automatically. Manual logging is error-prone and easy to forget when adding new endpoints.
3. **Clean Business Logic:** Resource methods remain focused on their core domain responsibility, keeping the code readable and easy to test.
4. **Extensibility:** The same filter pattern can easily be adapted for authentication, CORS, or rate-limiting across the entire application.
