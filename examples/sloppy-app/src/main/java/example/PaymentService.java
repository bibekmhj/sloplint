package example;

/**
 * Intentionally sloppy — this is the kind of file sloplint is designed to catch.
 * Every issue below is something an AI coding assistant has emitted at least once.
 *
 * Run:
 *   java -jar sloplint.jar examples/sloppy-app/src
 */
public class PaymentService {

    // SL001 — hardcoded AWS access key
    private static final String AWS_KEY = "AKIAIOSFODNN7EXAMPLE";

    // SL002 — placeholder marker
    private static final String WEBHOOK_SECRET = "YOUR_WEBHOOK_SECRET_HERE";

    // SL003 — placeholder URL as API base
    private static final String API_BASE = "https://api.example.com/v1";

    public void charge(String customerId, long cents) {
        try {
            doCharge(customerId, cents);
        } catch (Exception e) {
            // SL005 — silent catch
            e.printStackTrace();
        }
    }

    private void doCharge(String customerId, long cents) {
        // SL006 — stray println in a service class
        System.out.println("charging " + customerId + " for " + cents);

        // SL004 — TODO throw pretending the method works
        throw new UnsupportedOperationException("TODO: call Stripe API");
    }
}
