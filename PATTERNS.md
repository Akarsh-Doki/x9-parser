# Design Patterns in the X9 Parser

This project uses a small number of patterns that fit the problem, applied mostly through the Spring framework, rather than adding structure for its own sake.

## Patterns used in this project

### 1. Layered architecture

**What it is:** the code is split into layers, and each layer has one job.

The layers here:
- Controller (`ParseController`, `GlobalExceptionHandler`): handles the web request and response.
- Service (`X9StreamProcessor`, `CheckCsvWriter`, `ImageWriter`): does the real work of reading the file and writing the outputs.
- Model (`CheckRecord`, `ProcessSummary`): plain objects that carry data.
- Config (`X9Config`): the settings loaded from the properties file.

**Trade-off:** more classes and indirection than putting everything in one file. That cost is worth paying once the logic is non-trivial, and here it is what lets the parser be tested on its own, without the web layer.

**Where to see it:** the folder structure (`controller`, `service`, `model`, `config`) maps directly to the layers.

**Sample code:**
```
controller/   ParseController, GlobalExceptionHandler
service/      X9StreamProcessor, CheckCsvWriter, ImageWriter
model/        CheckRecord, ProcessSummary
config/       X9Config
```

### 2. MVC (Model, View, Controller)

**What it is:** the web-specific version of the same idea, splitting a request into three roles. Spring MVC provides it.

- Controller: `ParseController` receives the request and decides what to do.
- Model: `ProcessSummary` carries the result (counts and output locations).
- View: Thymeleaf pages (`index.html`, `result.html`) render what the user sees.

**Trade-off:** an extra separation to maintain (logic in one place, templates in another). Worth it because the page and the logic change for different reasons and at different times.

**Where to see it:** `ParseController` returns a view name and puts a `ProcessSummary` in the model, and Thymeleaf renders it.

**Sample code:**
```java
@PostMapping("/parse")
public String parse(@RequestParam("filePath") String filePath, Model model, RedirectAttributes redirectAttributes) {
    ProcessSummary summary = processor.process(filePath.trim());
    model.addAttribute("summary", summary);  // model
    return "result";                         // view name points to result.html
}
```

### 3. Dependency Injection

**What it is:** a class does not create the things it needs. They are passed in from outside, through the constructor, and Spring supplies them.

**Trade-off:** it needs a framework (Spring) to wire everything together, and the wiring is less obvious than a direct `new`. In return the classes are decoupled and can be tested with fakes.

**Where to see it:**
- `ParseController(X9StreamProcessor processor)`
- `X9StreamProcessor(X9Config config)`

Neither class uses `new` to build its dependency. Spring creates each object and hands it to whoever needs it.

**Sample code:**
```java
public ParseController(X9StreamProcessor processor) {
    this.processor = processor;   // passed in by Spring, we never call new
}
```

### 4. Singleton

**What it is:** one shared instance of a class exists, reused everywhere it is needed.

**Trade-off:** a single shared instance is used by many requests at the same time, so it must not hold changing state between requests, or those requests could interfere with each other. These services keep no state between calls (each `process` call uses only local variables), so one shared instance is safe under concurrent load.

**Where to see it:** every Spring bean in the project is a singleton by default. `X9Config`, `X9StreamProcessor`, `CheckCsvWriter`, `ImageWriter`, and `ParseController` are each created once by Spring and shared.

**Note:** Singleton and Dependency Injection are provided by the Spring framework. Spring creates one instance of each class (singleton) and passes it to the classes that need it (injection).

**Sample code:**
```java
@Service   // Spring creates ONE X9StreamProcessor and shares it
public class X9StreamProcessor { ... }
```

### 5. Externalized Configuration

**What it is:** the settings live in an external properties file, not hardcoded in Java, and are bound into a typed object at startup.

**Trade-off:** one more file to manage, but the field positions, output folder, and column mapping can change without editing or recompiling the code.

**Where to see it:** `X9Config` binds the `x9.*` keys from `application.properties`.

**Sample code:**
```java
@Component
@ConfigurationProperties(prefix = "x9")   // fills from the x9.* lines in application.properties
public class X9Config {
    private String outputDir = "./output";
    private List<FieldDef> checkFields = new ArrayList<>();
    // getters and setters
}
```
```properties
x9.output-dir=./output
x9.check-fields[6].name=Item Amount
x9.check-fields[6].start=48
x9.check-fields[6].end=57
```

### 6. DTO / data-carrier objects

**What it is:** plain objects that carry data between layers and hold no logic. `ProcessSummary` carries the result to the view, and `CheckRecord` carries one check's fields.

**Trade-off:** an extra small class instead of passing loose values, but the data stays structured and named as it moves between layers.

**Where to see it:** `X9StreamProcessor` returns a `ProcessSummary`, and the controller puts it in the model for the view.

**Sample code:**
```java
public class ProcessSummary {   // just carries results to the view, no logic
    private final int checksParsed;
    private final int rowsWritten;
    private final int imagesWritten;
    private String bigCsvPath;
    // getters
}
```

### 7. Facade

**What it is:** one simple entry point that hides a multi-step subsystem. `X9StreamProcessor.process(...)` hides record reading, CSV writing, and image writing behind a single call.

**Trade-off:** the caller loses fine-grained control of the steps, but gains a simple contract, which is give a path and get a summary.

**Where to see it:** the controller makes one call and never touches the readers or writers itself.

**Sample code:**
```java
// one call hides the whole pipeline: read records, write CSVs, write images
ProcessSummary summary = processor.process(filePath);
```

### 8. Iterator-style streaming

**What it is:** records are read one at a time until the file ends, instead of loading them all at once. `readRecord` returns the next record, or null when the file is done.

**Trade-off:** slightly more code than reading everything at once, but memory stays flat because only one record is held at a time.

**Where to see it:** the main loop in `X9StreamProcessor`.

**Sample code:**
```java
byte[] record = readRecord(in);   // next record, or null at end of file
while (record != null) {
    // handle this one record, then let it go
    record = readRecord(in);
}
```

### 9. Chain of Responsibility (the security filter chain)

**What it is:** a request passes through a line of handlers, and each one either
deals with it or passes it along. Spring Security is built this way: every
request goes through a chain of filters (check the session, handle the login
form, check the URL rules) before reaching a controller.

**Trade-off:** the path a request takes is distributed across many filters rather than being in one place, which is harder to follow, but each filter stays small and the chain can be reordered or extended without touching the others. Adding Okta only meant swapping which filter handles login (`oauth2Login` instead of `formLogin`); the URL rules and the denied-request handling did not move.

**Where to see it:** `SecurityConfig.securityFilterChain` builds the chain, and the access-denied handler hooks into it through `exceptionHandling`.

**Sample code:**
```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                               ClientRegistrationRepository clientRegistrations) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/parse").hasAuthority("FCRMADMIN")
            .anyRequest().authenticated())
        .oauth2Login(oauth2 -> oauth2.defaultSuccessUrl("/", true))
        .exceptionHandling(handling -> handling
            .accessDeniedHandler(this::sendToNoPermissionPage));
    return http.build();
}
```

### 10. Role-Based Access Control (RBAC)

**What it is:** permissions are attached to a group rather than to individual
users, and a user gets access by being a member of that group. Here membership
in the `FCRMADMIN` group may parse files, and everyone else may not.

**Trade-off:** adding a new kind of access still means one new group and one
more `requestMatchers` rule, but who counts as an admin now lives in the
identity provider instead of this app's own properties file, so changing it
never touches this codebase.

**Where to see it:** the group arrives on the `groups` claim of the ID token
Okta issues, read by the `userAuthoritiesMapper` bean in `SecurityConfig`, and
checked by the rule in `securityFilterChain`.

**Sample code:**
```java
@Bean
public GrantedAuthoritiesMapper userAuthoritiesMapper() {
    return authorities -> {
        Set<GrantedAuthority> mapped = new HashSet<>();
        for (GrantedAuthority authority : authorities) {
            if (authority instanceof OidcUserAuthority oidcAuth) {
                Object groups = oidcAuth.getIdToken().getClaims().get("groups");
                if (groups instanceof List<?> groupList) {
                    for (Object group : groupList) {
                        mapped.add(new SimpleGrantedAuthority(group.toString()));
                    }
                }
            }
        }
        return mapped;   // FCRMADMIN becomes an authority the filter chain can check
    };
}
```

### 11. Page Object Model (Selenium tests)

**What it is:** one class per page in the browser tests. That class holds the
locators for that page and exposes actions in plain terms, so the tests say what
to do and the page class knows how to do it.

**Trade-off:** three extra classes instead of calling `findElement` in the tests,
but a test reads as `loginPage.loginAs(user, password)` rather than a list of
element lookups, and when the markup changes only one class needs editing.

**Where to see it:** `selenium/pages/LoginPage`, `ParsePage`, and `ResultPage`.
The locators are stored as `By` constants rather than as `WebElement` fields, so
each element is looked up fresh and a page reload cannot leave a stale reference.

**Sample code:**
```java
public class LoginPage {

    private static final By USERNAME = By.id("username");
    private static final By PASSWORD = By.id("password");

    public void loginAs(String username, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME)).sendKeys(username);
        driver.findElement(PASSWORD).sendKeys(password);
        driver.findElement(PASSWORD).submit();
    }
}
```

## How a request flows through the patterns

A single request touches every pattern. This is the path it takes, from the browser to the output files and back:

```mermaid
flowchart TD
    browser["Browser"]
    subgraph app["X9 Parser, one deployed unit (monolith)"]
        controller["ParseController, Controller (MVC)"]
        config["X9Config, externalized configuration"]
        processor["X9StreamProcessor, service, facade, streaming read"]
        csv["CheckCsvWriter, writes CSV rows"]
        img["ImageWriter, writes images"]
        summary["ProcessSummary, model / DTO"]
        view["result.html, view"]
    end
    disk[("Output folder, 2 CSVs and images")]

    browser -->|"POST /parse with a file path"| controller
    controller -->|"calls, dependency injected"| processor
    processor -->|"reads field layout and mapping"| config
    processor -->|"per check"| csv
    processor -->|"per image"| img
    csv -->|"writes"| disk
    img -->|"writes"| disk
    processor -->|"returns"| summary
    summary --> view
    controller -->|"adds summary to model"| view
    view -->|"success summary page"| browser
```

Every object inside the box (the controller, the services, and the config) is a singleton. Spring creates one of each and injects it where it is needed. Because the services keep no state between requests, one shared instance handles many requests safely.


### 12. Federated Identity / Delegated Authentication

**What it is:** the app does not check passwords itself. It redirects the user
to a separate identity provider (Okta), the provider does the actual login,
and the app trusts a signed token handed back instead of a password. Inside
that, Okta does not store the password either; it forwards the check to
Active Directory (delegated authentication), so AD is still the one deciding
whether a password is right.

**Trade-off:** the app gives up direct control of the login screen and depends on an outside service being reachable, but it gains MFA, one place to disable a compromised account, and no passwords ever touching this codebase. It also means one bean is all that changes when the identity provider changes. This project went from a properties file, to a direct LDAP bind, to Okta, without touching the parsing code, the controllers, or the pages at all.

**Where to see it:** `SecurityConfig.securityFilterChain`'s `oauth2Login`
block, and the `spring.security.oauth2.client.*` keys in
`application.properties`, which point at Okta's issuer URL rather than at AD
directly.

**Sample code:**
```properties
spring.security.oauth2.client.registration.okta.client-id=${OKTA_CLIENT_ID}
spring.security.oauth2.client.registration.okta.client-secret=${OKTA_CLIENT_SECRET}
spring.security.oauth2.client.provider.okta.issuer-uri=https://integrator-2451275.okta.com/oauth2/default
```

## Architecture and deployment

- **Monolith:** the entire application (web layer, parsing, CSV writing, image writing) is one codebase, built and deployed as a single unit. For a focused, single-purpose tool this is the right choice.
- **Independent core:** the parsing logic in `X9StreamProcessor` does not depend on the web layer. It works from a file path and writes output, and the controller is a thin layer on top of it.
