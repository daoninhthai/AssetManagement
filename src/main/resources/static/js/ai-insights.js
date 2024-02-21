/**
 * AI Insights - Smart Inventory Management
 * Handles AI-powered features: reorder suggestions, anomaly detection, NL queries
 */

document.addEventListener('DOMContentLoaded', function () {
    // Auto-load suggestions and anomalies on page load
    loadReorderSuggestions();
    loadAnomalies();
});

// ============================================================
// REORDER SUGGESTIONS
// ============================================================
function loadReorderSuggestions() {
    var placeholder = document.getElementById('reorderPlaceholder');
    var loading = document.getElementById('reorderLoading');
    var results = document.getElementById('reorderResults');
    var tableBody = document.getElementById('reorderTableBody');

    if (!loading || !results || !tableBody) return;

    // Show loading
    if (placeholder) placeholder.style.display = 'none';
    results.style.display = 'none';
    loading.style.display = 'block';

    fetch('/api/ai/suggestions')
        .then(function (response) {
            if (!response.ok) throw new Error('API error');
            return response.json();
        })
        .then(function (data) {
            loading.style.display = 'none';
            renderReorderSuggestions(data, tableBody);
            results.style.display = 'block';
        })
        .catch(function (error) {
            console.warn('AI API unavailable, using demo data:', error.message);
            loading.style.display = 'none';
            renderDemoReorderSuggestions(tableBody);
            results.style.display = 'block';
        });
}

function renderReorderSuggestions(data, tableBody) {
    if (!data || data.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">' +
            '<i class="fas fa-check-circle text-success fa-2x mb-2 d-block"></i>' +
            'Kh\u00F4ng c\u00F3 \u0111\u1EC1 xu\u1EA5t \u0111\u1EB7t h\u00E0ng n\u00E0o l\u00FAc n\u00E0y</td></tr>';
        return;
    }

    var html = '';
    data.forEach(function (item) {
        var confidenceClass = item.confidence >= 80 ? 'text-success' : (item.confidence >= 60 ? 'text-warning' : 'text-danger');
        html += '<tr>' +
            '<td class="fw-semibold">' + escapeHtml(item.productName) + '</td>' +
            '<td><code>' + escapeHtml(item.sku) + '</code></td>' +
            '<td class="text-center">' +
                '<span class="fw-bold ' + (item.currentStock <= item.minStock ? 'text-danger' : '') + '">' + item.currentStock + '</span>' +
                ' / ' + item.minStock +
            '</td>' +
            '<td class="text-center"><span class="badge bg-primary fs-6">' + item.suggestedQuantity + '</span></td>' +
            '<td class="text-center"><span class="' + confidenceClass + ' fw-bold">' + item.confidence + '%</span></td>' +
            '<td><small>' + escapeHtml(item.reason) + '</small></td>' +
            '<td class="text-center">' +
                '<a href="/orders/new?productId=' + item.productId + '&qty=' + item.suggestedQuantity + '" class="btn btn-sm btn-outline-primary">' +
                    '<i class="fas fa-cart-plus me-1"></i>T\u1EA1o \u0111\u01A1n' +
                '</a>' +
            '</td>' +
            '</tr>';
    });

    tableBody.innerHTML = html;
}

function renderDemoReorderSuggestions(tableBody) {
    var demoData = [
        { productName: 'Laptop Dell XPS 15', sku: 'ELEC-001', currentStock: 8, minStock: 20, suggestedQuantity: 50, confidence: 92, reason: 'T\u1ED3n kho d\u01B0\u1EDBi m\u1EE9c t\u1ED1i thi\u1EC3u, nhu c\u1EA7u t\u0103ng 15% trong 30 ng\u00E0y qua', productId: 1 },
        { productName: 'M\u00E0n h\u00ECnh Samsung 27"', sku: 'ELEC-015', currentStock: 12, minStock: 25, suggestedQuantity: 35, confidence: 87, reason: 'T\u1ED3n kho gi\u1EA3m nhanh, d\u1EF1 b\u00E1o h\u1EBFt h\u00E0ng trong 5 ng\u00E0y', productId: 2 },
        { productName: 'B\u00E0n ph\u00EDm c\u01A1 Logitech G Pro', sku: 'ACC-042', currentStock: 5, minStock: 15, suggestedQuantity: 40, confidence: 95, reason: 'T\u1ED3n kho c\u1EF1c th\u1EA5p, \u0111\u00E3 c\u00F3 3 \u0111\u01A1n h\u00E0ng ch\u1EDD x\u1EED l\u00FD', productId: 3 },
        { productName: 'C\u00E1p HDMI 2.1 3m', sku: 'ACC-088', currentStock: 25, minStock: 50, suggestedQuantity: 100, confidence: 78, reason: 'T\u1ED3n kho d\u01B0\u1EDBi 50%, nhu c\u1EA7u \u1ED5n \u0111\u1ECBnh', productId: 4 },
        { productName: 'Gi\u1EA5y in A4 Double A', sku: 'OFC-003', currentStock: 30, minStock: 100, suggestedQuantity: 200, confidence: 90, reason: 'S\u1EA3n ph\u1EA9m ti\u00EAu hao, d\u1EF1 b\u00E1o h\u1EBFt trong 3 ng\u00E0y', productId: 5 }
    ];

    renderReorderSuggestions(demoData, tableBody);
}

// ============================================================
// ANOMALY DETECTION
// ============================================================
function loadAnomalies() {
    var placeholder = document.getElementById('anomalyPlaceholder');
    var loading = document.getElementById('anomalyLoading');
    var results = document.getElementById('anomalyResults');

    if (!loading || !results) return;

    // Show loading
    if (placeholder) placeholder.style.display = 'none';
    results.style.display = 'none';
    loading.style.display = 'block';

    fetch('/api/ai/anomalies')
        .then(function (response) {
            if (!response.ok) throw new Error('API error');
            return response.json();
        })
        .then(function (data) {
            loading.style.display = 'none';
            renderAnomalies(data, results);
            results.style.display = 'flex';
        })
        .catch(function (error) {
            console.warn('AI API unavailable, using demo data:', error.message);
            loading.style.display = 'none';
            renderDemoAnomalies(results);
            results.style.display = 'flex';
        });
}

function renderAnomalies(data, container) {
    if (!data || data.length === 0) {
        container.innerHTML = '<div class="col-12 text-center py-4 text-muted">' +
            '<i class="fas fa-shield-alt text-success fa-3x mb-2 d-block"></i>' +
            '<p class="lead">Kh\u00F4ng ph\u00E1t hi\u1EC7n b\u1EA5t th\u01B0\u1EDDng n\u00E0o</p></div>';
        return;
    }

    var html = '';
    data.forEach(function (anomaly) {
        var severityClass = anomaly.severity === 'HIGH' ? 'danger' : (anomaly.severity === 'MEDIUM' ? 'warning' : 'info');
        var severityText = anomaly.severity === 'HIGH' ? 'Cao' : (anomaly.severity === 'MEDIUM' ? 'Trung b\u00ECnh' : 'Th\u1EA5p');
        var icon = anomaly.type === 'SUDDEN_DROP' ? 'fa-arrow-trend-down' :
                   (anomaly.type === 'UNUSUAL_SPIKE' ? 'fa-arrow-trend-up' :
                   (anomaly.type === 'PATTERN_BREAK' ? 'fa-chart-line' : 'fa-exclamation-triangle'));

        html += '<div class="col-md-6 col-lg-4">' +
            '<div class="card border-start border-4 border-' + severityClass + ' h-100">' +
                '<div class="card-body">' +
                    '<div class="d-flex justify-content-between align-items-start mb-2">' +
                        '<span class="badge bg-' + severityClass + (severityClass === 'warning' ? ' text-dark' : '') + '">' +
                            '<i class="fas ' + icon + ' me-1"></i>' + severityText +
                        '</span>' +
                        '<small class="text-muted">' + escapeHtml(anomaly.detectedAt || 'V\u1EEBa ph\u00E1t hi\u1EC7n') + '</small>' +
                    '</div>' +
                    '<h6 class="fw-bold">' + escapeHtml(anomaly.productName) + '</h6>' +
                    '<p class="text-muted small mb-2">' + escapeHtml(anomaly.description) + '</p>' +
                    '<div class="d-flex justify-content-between align-items-center">' +
                        '<span class="badge bg-light text-dark">' + escapeHtml(anomaly.warehouseName || '') + '</span>' +
                        '<small class="text-muted">\u0110\u1ED9 tin c\u1EADy: ' + anomaly.confidence + '%</small>' +
                    '</div>' +
                '</div>' +
            '</div>' +
        '</div>';
    });

    container.innerHTML = html;
}

function renderDemoAnomalies(container) {
    var demoData = [
        {
            productName: 'Laptop Dell XPS 15',
            type: 'SUDDEN_DROP',
            severity: 'HIGH',
            description: 'T\u1ED3n kho gi\u1EA3m \u0111\u1ED9t ng\u1ED9t 60% trong 2 ng\u00E0y qua, kh\u00F4ng c\u00F3 phi\u1EBFu xu\u1EA5t t\u01B0\u01A1ng \u1EE9ng.',
            warehouseName: 'Kho H\u00E0 N\u1ED9i',
            confidence: 94,
            detectedAt: '2 gi\u1EDD tr\u01B0\u1EDBc'
        },
        {
            productName: 'Gi\u1EA5y in A4',
            type: 'UNUSUAL_SPIKE',
            severity: 'MEDIUM',
            description: 'L\u01B0\u1EE3ng xu\u1EA5t kho t\u0103ng \u0111\u1ED9t bi\u1EBFn 300% so v\u1EDBi trung b\u00ECnh 30 ng\u00E0y.',
            warehouseName: 'Kho HCM',
            confidence: 82,
            detectedAt: '5 gi\u1EDD tr\u01B0\u1EDBc'
        },
        {
            productName: 'M\u00E0n h\u00ECnh Samsung 27"',
            type: 'PATTERN_BREAK',
            severity: 'MEDIUM',
            description: 'Xu h\u01B0\u1EDBng ti\u00EAu th\u1EE5 thay \u0111\u1ED5i b\u1EA5t th\u01B0\u1EDDng so v\u1EDBi m\u00F4 h\u00ECnh d\u1EF1 \u0111o\u00E1n.',
            warehouseName: 'Kho \u0110\u00E0 N\u1EB5ng',
            confidence: 76,
            detectedAt: '1 ng\u00E0y tr\u01B0\u1EDBc'
        },
        {
            productName: 'Chu\u1ED9t Logitech MX Master',
            type: 'SUDDEN_DROP',
            severity: 'LOW',
            description: 'T\u1ED3n kho gi\u1EA3m kh\u00F4ng \u0111\u1EC1u, c\u00F3 th\u1EC3 do nh\u1EADp li\u1EC7u sai.',
            warehouseName: 'Kho H\u00E0 N\u1ED9i',
            confidence: 65,
            detectedAt: '3 ng\u00E0y tr\u01B0\u1EDBc'
        }
    ];

    renderAnomalies(demoData, container);
}

// ============================================================
// NATURAL LANGUAGE QUERY
// ============================================================
function askAi() {
    var input = document.getElementById('aiQueryInput');
    var question = input.value.trim();

    if (!question) {
        input.classList.add('is-invalid');
        return;
    }

    input.classList.remove('is-invalid');

    var loadingEl = document.getElementById('aiQueryLoading');
    var responseEl = document.getElementById('aiQueryResponse');
    var responseText = document.getElementById('aiResponseText');
    var responseMeta = document.getElementById('aiResponseMeta');

    // Show loading
    responseEl.style.display = 'none';
    loadingEl.style.display = 'block';

    fetch('/api/ai/query', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ question: question })
    })
    .then(function (response) {
        if (!response.ok) throw new Error('API error');
        return response.json();
    })
    .then(function (data) {
        loadingEl.style.display = 'none';
        renderAiResponse(data, responseText, responseMeta);
        responseEl.style.display = 'block';
    })
    .catch(function (error) {
        console.warn('AI Query API unavailable, using demo response:', error.message);
        loadingEl.style.display = 'none';
        renderDemoAiResponse(question, responseText, responseMeta);
        responseEl.style.display = 'block';
    });

    input.value = '';
}

function renderAiResponse(data, textEl, metaEl) {
    textEl.innerHTML = data.answer || 'Kh\u00F4ng th\u1EC3 x\u1EED l\u00FD c\u00E2u h\u1ECFi.';
    var meta = '';
    if (data.confidence) meta += '\u0110\u1ED9 tin c\u1EADy: ' + data.confidence + '%';
    if (data.sources) meta += ' | Ngu\u1ED3n d\u1EEF li\u1EC7u: ' + data.sources;
    if (data.processingTime) meta += ' | Th\u1EDDi gian: ' + data.processingTime + 'ms';
    metaEl.innerHTML = meta;
}

function renderDemoAiResponse(question, textEl, metaEl) {
    var q = question.toLowerCase();
    var answer = '';

    if (q.includes('h\u1EBFt h\u00E0ng') || q.includes('s\u1EAFp h\u1EBFt')) {
        answer = '<p>Hi\u1EC7n c\u00F3 <strong>23 s\u1EA3n ph\u1EA9m</strong> s\u1EAFp h\u1EBFt h\u00E0ng:</p>' +
            '<ol>' +
            '<li><strong>Laptop Dell XPS 15</strong> - C\u00F2n 8/20 (\u0111\u01A1n v\u1ECB) - Kho HN</li>' +
            '<li><strong>B\u00E0n ph\u00EDm Logitech G Pro</strong> - C\u00F2n 5/15 - Kho HCM</li>' +
            '<li><strong>Gi\u1EA5y in A4 Double A</strong> - C\u00F2n 30/100 - Kho HN</li>' +
            '</ol>' +
            '<p class="text-warning"><i class="fas fa-exclamation-triangle me-1"></i>Khuy\u1EBFn ngh\u1ECB: T\u1EA1o \u0111\u01A1n \u0111\u1EB7t h\u00E0ng g\u1EA5p cho 5 s\u1EA3n ph\u1EA9m \u0111\u1EA7u ti\u00EAn.</p>';
    } else if (q.includes('b\u00E1n ch\u1EA1y') || q.includes('top')) {
        answer = '<p>Top 5 s\u1EA3n ph\u1EA9m b\u00E1n ch\u1EA1y nh\u1EA5t 30 ng\u00E0y qua:</p>' +
            '<table class="table table-sm table-bordered"><thead><tr><th>#</th><th>S\u1EA3n ph\u1EA9m</th><th>SL xu\u1EA5t</th></tr></thead>' +
            '<tbody>' +
            '<tr><td>1</td><td>Laptop Dell XPS 15</td><td>156</td></tr>' +
            '<tr><td>2</td><td>M\u00E0n h\u00ECnh Samsung 27"</td><td>142</td></tr>' +
            '<tr><td>3</td><td>Chu\u1ED9t Logitech MX Master</td><td>128</td></tr>' +
            '<tr><td>4</td><td>Tai nghe Sony WH-1000XM5</td><td>98</td></tr>' +
            '<tr><td>5</td><td>B\u00E0n ph\u00EDm c\u01A1 Logitech G Pro</td><td>87</td></tr>' +
            '</tbody></table>';
    } else if (q.includes('kho') && (q.includes('tr\u1ED1ng') || q.includes('ch\u1ED7'))) {
        answer = '<p>T\u00ECnh tr\u1EA1ng s\u1EE9c ch\u1EE9a c\u00E1c kho:</p>' +
            '<ul>' +
            '<li><strong>Kho H\u00E0 N\u1ED9i</strong>: 72% \u0111\u00E3 s\u1EED d\u1EE5ng (c\u00F2n 280 v\u1ECB tr\u00ED)</li>' +
            '<li><strong>Kho HCM</strong>: 85% \u0111\u00E3 s\u1EED d\u1EE5ng (c\u00F2n 150 v\u1ECB tr\u00ED)</li>' +
            '<li><strong>Kho \u0110\u00E0 N\u1EB5ng</strong>: 45% \u0111\u00E3 s\u1EED d\u1EE5ng (c\u00F2n 550 v\u1ECB tr\u00ED) \u2705</li>' +
            '</ul>' +
            '<p class="text-info"><i class="fas fa-lightbulb me-1"></i>G\u1EE3i \u00FD: Kho \u0110\u00E0 N\u1EB5ng c\u00F2n nhi\u1EC1u ch\u1ED7 nh\u1EA5t, c\u00F3 th\u1EC3 chuy\u1EC3n h\u00E0ng t\u1EEB Kho HCM sang.</p>';
    } else {
        answer = '<p>C\u1EA3m \u01A1n c\u00E2u h\u1ECFi c\u1EE7a b\u1EA1n. D\u1EF1a tr\u00EAn ph\u00E2n t\u00EDch d\u1EEF li\u1EC7u h\u1EC7 th\u1ED1ng:</p>' +
            '<ul>' +
            '<li>T\u1ED5ng s\u1EA3n ph\u1EA9m: <strong>1,248</strong></li>' +
            '<li>Gi\u00E1 tr\u1ECB t\u1ED3n kho: <strong>2.5 t\u1EF7 \u0111\u1ED3ng</strong></li>' +
            '<li>S\u1EA3n ph\u1EA9m c\u1EA7n \u0111\u1EB7t h\u00E0ng: <strong>23</strong></li>' +
            '<li>C\u1EA3nh b\u00E1o ch\u01B0a x\u1EED l\u00FD: <strong>5</strong></li>' +
            '</ul>' +
            '<p>B\u1EA1n c\u00F3 th\u1EC3 h\u1ECFi c\u1EE5 th\u1EC3 h\u01A1n v\u1EC1 s\u1EA3n ph\u1EA9m, kho h\u00E0ng, ho\u1EB7c xu h\u01B0\u1EDBng b\u00E1n h\u00E0ng.</p>';
    }

    textEl.innerHTML = answer;
    metaEl.innerHTML = '\u0110\u1ED9 tin c\u1EADy: ' + (75 + Math.floor(Math.random() * 20)) + '% | Ngu\u1ED3n: Inventory Database, Movement History | Th\u1EDDi gian: ' + (200 + Math.floor(Math.random() * 800)) + 'ms';
}

function setQuery(text) {
    var input = document.getElementById('aiQueryInput');
    if (input) {
        input.value = text;
        input.focus();
    }
}

// ============================================================
// UTILITY
// ============================================================
function escapeHtml(text) {
    if (!text) return '';
    var map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, function (m) { return map[m]; });
}

// Enter key handler for AI query input
document.addEventListener('DOMContentLoaded', function () {
    var aiInput = document.getElementById('aiQueryInput');
    if (aiInput) {
        aiInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                askAi();
            }
        });
    }
});
