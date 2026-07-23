public class TestFormat {
    public static void main(String[] args) {
        System.out.println(String.format("http://localhost:8181/v1/data/app/authz/finance/allow", "finance"));
        System.out.println(String.format("http://localhost:8181/v1/data/app/authz/%s/allow", "finance"));
    }
}
