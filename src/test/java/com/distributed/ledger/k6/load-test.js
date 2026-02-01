import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';
import { randomItem } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import encoding from 'k6/encoding';

const errorRate = new Rate('errors');

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '2m', target: 50 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const ACCOUNTS = [
    '11111111-1111-1111-1111-111111111111', // Alice
    '22222222-2222-2222-2222-222222222222', // Bob
    '33333333-3333-3333-3333-333333333333', // Charlie
    '44444444-4444-4444-4444-444444444444', // Dave
];

export default function () {
    let fromAccount = randomItem(ACCOUNTS);
    let toAccount;
    do {
        toAccount = randomItem(ACCOUNTS);
    } while (toAccount === fromAccount);

    const payload = JSON.stringify({
        fromAccountId: fromAccount,
        toAccountId: toAccount,
        amount: 10.00,
        currency: 'TRY',
        referenceNumber: uuidv4()
    });


    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Basic ' + encoding.b64encode('admin:admin'),
        },
    };

    let transactionResponse = http.post(`${BASE_URL}/api/v1/transactions/send`, payload, params);

    let transactionCheck = check(transactionResponse, {
        'transaction status is 200': (r) => r.status === 200,
    });

    if (!transactionCheck) {
        errorRate.add(1);
    }

    if (Math.random() < 0.1) {
        let healthResponse = http.get(`${BASE_URL}/api/v1/health`);
        check(healthResponse, { 'health is UP': (r) => r.status === 200 });
    }

    sleep(1);
}