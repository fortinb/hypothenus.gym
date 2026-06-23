# Moneris REST API – OpenAPI spec

## How to get the YAML file

1. Log in to the **Moneris Developer Portal**: <https://developer.moneris.com>
2. Navigate to **API Reference** → **REST API** and download the OpenAPI spec
   (it is usually offered as `openapi.yaml` or `openapi.json`).
3. Rename/copy the file to this directory as:

```
src/main/openapi/moneris-openapi.yaml
```

## Alternatively – use the Swagger UI "Download" link

If Moneris exposes a live Swagger UI you can get the spec directly from:

```
https://<moneris-host>/v3/api-docs          # JSON
https://<moneris-host>/v3/api-docs.yaml     # YAML
```

## Build behaviour

Once the file is present, running `./gradlew compileJava` (or just building the
project) will automatically:

1. Invoke `openApiGenerate` (the `org.openapi.generator` plugin).
2. Write generated sources into `build/generated/moneris-client/src/main/java/`.
3. Compile them together with the rest of your domain-services code.

### Generated packages

| Purpose          | Package                                                       |
|------------------|---------------------------------------------------------------|
| API interfaces   | `com.iso.hypo.finance.infrastructure.moneris.api`            |
| Model DTOs       | `com.iso.hypo.finance.infrastructure.moneris.model`          |
| `ApiClient` base | `com.iso.hypo.finance.infrastructure.moneris.invoker`        |

## Using the generated client

```java
import com.iso.hypo.finance.infrastructure.moneris.invoker.ApiClient;
import com.iso.hypo.finance.infrastructure.moneris.api.PaymentsApi;

// 1. Configure the ApiClient (RestTemplate-based)
ApiClient apiClient = new ApiClient();
apiClient.setBasePath("https://api.moneris.com"); // adjust to real base URL
apiClient.addDefaultHeader("store-id",  config.getStoreId());
apiClient.addDefaultHeader("api-token", config.getApiKey());

// 2. Instantiate the API stub you need
PaymentsApi paymentsApi = new PaymentsApi(apiClient);

// 3. Call an endpoint
var response = paymentsApi.purchase(purchaseRequest);
```

> **Tip:** Wrap the `ApiClient` construction in a `@Configuration` / `@Bean`
> so you can inject the correct `PaymentProviderConfigurationEntry` per brand.
