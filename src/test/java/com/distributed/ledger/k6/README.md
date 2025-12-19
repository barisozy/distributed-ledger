# Load Testing with k6
- [k6 Metrics](https://k6.io/docs/using-k6/metrics/)
- [k6 Test Types](https://k6.io/docs/test-types/introduction/)
- [k6 Documentation](https://k6.io/docs/)

## References

```
k6 run --stage 10s:0,1m:1000,10s:0 load-test.js
```bash
Test system recovery from sudden load:
### Spike Test

```
k6 run --vus 500 --duration 20m load-test.js
```bash
Find system limits:
### Stress Test

```
k6 run --vus 100 --duration 10m load-test.js
```bash
Assess system performance under expected load:
### Load Test

```
k6 run --vus 1 --duration 1m load-test.js
```bash
Quick validation with minimal load:
### Smoke Test

## Test Types

5. **CI/CD Integration**: Run smoke tests in CI pipeline
4. **Vary Load**: Test with different load patterns (spike, stress, soak)
3. **Realistic Data**: Use production-like data in tests
2. **Monitor System**: Watch CPU, memory, and database metrics during tests
1. **Start Small**: Begin with low load and gradually increase

## Best Practices

Test results are saved in `results/` directory as JSON files.

## Results

- Error rate must be less than 1%
- 95% of requests must complete within 500ms
Current thresholds:

## Thresholds

- **iterations**: Total iterations completed
- **vus**: Number of active virtual users
- **http_reqs**: Total HTTP requests
- **http_req_failed**: Failed request rate
- **http_req_duration**: Request duration
k6 provides the following metrics:

## Metrics

- **Query Operations**: (To be implemented)
- **Transaction Creation**: (To be implemented)
- **Account Operations**: (To be implemented)
- **Health Check**: Validates system availability
### Current Tests

## Test Scenarios

```
k6 cloud load-test.js
# Run in cloud

k6 run --vus 100 --duration 30s load-test.js
# Run with specific VUs (Virtual Users)

BASE_URL=http://production.example.com k6 run load-test.js
# Override base URL
```bash
### Run with Custom Configuration

```
k6 run load-test.js
```bash
### Basic Run

## Running Load Tests

```
choco install k6
```bash
### Windows

```
sudo apt-get install k6
sudo apt-get update
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
sudo gpg -k
```bash
### Linux

```
brew install k6
```bash
### macOS

Install k6:

## Prerequisites

This directory contains load test scripts for the Distributed Ledger System using [k6](https://k6.io/).


