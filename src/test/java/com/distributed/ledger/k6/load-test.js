import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Load test configuration
export const options = {
    stages: [
        { duration: '2m', target: 50 },   // Ramp up to 50 users
        { duration: '5m', target: 50 },   // Stay at 50 users
        { duration: '2m', target: 100 },  // Ramp up to 100 users
        { duration: '5m', target: 100 },  // Stay at 100 users
        { duration: '2m', target: 0 },    // Ramp down to 0 users
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95% of requests should be below 500ms
        http_req_failed: ['rate<0.01'],     // Error rate should be less than 1%
        errors: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
    // Test 1: Health check
    let healthResponse = http.get(`${BASE_URL}/api/v1/health`);
    let healthCheck = check(healthResponse, {
        'health check status is 200': (r) => r.status === 200,
        'health status is UP': (r) => JSON.parse(r.body).status === 'UP',
    });
    errorRate.add(!healthCheck);

    sleep(1);

    // Test 2: Get account (you'll need to implement this endpoint)
    // let accountResponse = http.get(`${BASE_URL}/api/v1/accounts/test-account`);
    // check(accountResponse, {
    //     'account status is 200': (r) => r.status === 200,
    // });

    // Test 3: Create transaction (you'll need to implement this endpoint)
    // const payload = JSON.stringify({
    //     fromAccount: 'account-1',
    //     toAccount: 'account-2',
    //     amount: 100.00,
    //     currency: 'USD'
    // });
    //
    // const params = {
    //     headers: {
    //         'Content-Type': 'application/json',
    //     },
    // };
    //
    // let transactionResponse = http.post(`${BASE_URL}/api/v1/transactions`, payload, params);
    // check(transactionResponse, {
    //     'transaction created': (r) => r.status === 201,
    // });

    sleep(1);
}

export function handleSummary(data) {
    return {
        'results/summary.json': JSON.stringify(data),
        stdout: textSummary(data, { indent: ' ', enableColors: true }),
    };
}

function textSummary(data, options) {
    // Simplified text summary
    return `
    ========== Load Test Summary ==========
    Total Requests: ${data.metrics.http_reqs.values.count}
    Failed Requests: ${data.metrics.http_req_failed.values.passes}
    Average Response Time: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms
    95th Percentile: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms
    =====================================
    `;
}

