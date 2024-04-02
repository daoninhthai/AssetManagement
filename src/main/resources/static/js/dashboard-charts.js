/**
 * Dashboard Charts - Smart Inventory Management
 * Uses Chart.js 4.4 for rendering dashboard visualizations
 */

document.addEventListener('DOMContentLoaded', function () {

    // ============================================================
    // COLOR PALETTE
    // ============================================================
    var colors = {
        primary: '#4e73df',
        primaryLight: 'rgba(78, 115, 223, 0.1)',
        success: '#1cc88a',
        successLight: 'rgba(28, 200, 138, 0.1)',
        danger: '#e74a3b',
        dangerLight: 'rgba(231, 74, 59, 0.1)',
        warning: '#f6c23e',
        info: '#36b9cc',
        purple: '#6f42c1',
        gray: '#858796',
        pink: '#e83e8c',
        teal: '#20c9a6',
        orange: '#fd7e14',
        indigo: '#6610f2'
    };

    // ============================================================
    // STOCK TREND CHART (Line Chart - 30 days)
    // ============================================================
    var stockTrendCtx = document.getElementById('stockTrendChart');
    if (stockTrendCtx) {
        // Generate sample 30-day labels
        var trendLabels = [];
        var inboundData = [];
        var outboundData = [];
        var today = new Date();

        for (var i = 29; i >= 0; i--) {
            var date = new Date(today);
            date.setDate(date.getDate() - i);
            trendLabels.push(date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' }));

            // Generate realistic sample data
            var baseInbound = 45 + Math.floor(Math.random() * 30);
            var baseOutbound = 35 + Math.floor(Math.random() * 25);

            // Weekend dip
            if (date.getDay() === 0 || date.getDay() === 6) {
                baseInbound = Math.floor(baseInbound * 0.4);
                baseOutbound = Math.floor(baseOutbound * 0.5);
            }

            inboundData.push(baseInbound);
            outboundData.push(baseOutbound);
        }

        var stockTrendChart = new Chart(stockTrendCtx, {
            type: 'line',
            data: {
                labels: trendLabels,
                datasets: [
                    {
                        label: 'Nh\u1EADp kho',
                        data: inboundData,
                        borderColor: colors.primary,
                        backgroundColor: colors.primaryLight,
                        fill: true,
                        tension: 0.4,
                        borderWidth: 2.5,
                        pointRadius: 1,
                        pointHoverRadius: 5,
                        pointBackgroundColor: colors.primary,
                        pointHoverBackgroundColor: colors.primary,
                        pointBorderWidth: 0
                    },
                    {
                        label: 'Xu\u1EA5t kho',
                        data: outboundData,
                        borderColor: colors.danger,
                        backgroundColor: colors.dangerLight,
                        fill: true,
                        tension: 0.4,
                        borderWidth: 2.5,
                        pointRadius: 1,
                        pointHoverRadius: 5,
                        pointBackgroundColor: colors.danger,
                        pointHoverBackgroundColor: colors.danger,
                        pointBorderWidth: 0
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    mode: 'index',
                    intersect: false
                },
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20,
                            font: { size: 13 }
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        titleFont: { size: 13 },
                        bodyFont: { size: 12 },
                        padding: 12,
                        cornerRadius: 8,
                        callbacks: {
                            label: function (context) {
                                return context.dataset.label + ': ' + context.parsed.y + ' \u0111\u01A1n v\u1ECB';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: {
                            display: false
                        },
                        ticks: {
                            maxTicksLimit: 15,
                            font: { size: 11 }
                        }
                    },
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.04)',
                            drawBorder: false
                        },
                        ticks: {
                            font: { size: 11 },
                            callback: function (value) {
                                return value + ' \u0111v';
                            }
                        }
                    }
                }
            }
        });

        // Period switcher buttons
        document.querySelectorAll('[data-period]').forEach(function (btn) {
            btn.addEventListener('click', function () {
                document.querySelectorAll('[data-period]').forEach(function (b) {
                    b.classList.remove('active');
                });
                this.classList.add('active');

                var period = parseInt(this.getAttribute('data-period'));
                var newLabels = trendLabels.slice(30 - period);
                var newInbound = inboundData.slice(30 - period);
                var newOutbound = outboundData.slice(30 - period);

                stockTrendChart.data.labels = newLabels;
                stockTrendChart.data.datasets[0].data = newInbound;
                stockTrendChart.data.datasets[1].data = newOutbound;
                stockTrendChart.update();
            });
        });
    }

    // ============================================================
    // CATEGORY DISTRIBUTION CHART (Doughnut)
    // ============================================================
    var categoryCtx = document.getElementById('categoryChart');
    if (categoryCtx) {
        var categoryLabels = [
            '\u0110i\u1EC7n t\u1EED',
            'Th\u1EF1c ph\u1EA9m',
            'May m\u1EB7c',
            'N\u1ED9i th\u1EA5t',
            'V\u0103n ph\u00F2ng ph\u1EA9m',
            'D\u1EE5ng c\u1EE5',
            'H\u00F3a ch\u1EA5t',
            'Bao b\u00EC',
            'Ph\u1EE5 ki\u1EC7n',
            'Kh\u00E1c'
        ];

        var categoryData = [245, 198, 167, 145, 132, 98, 87, 76, 65, 35];

        var categoryColors = [
            colors.primary,
            colors.success,
            colors.info,
            colors.warning,
            colors.danger,
            colors.purple,
            colors.pink,
            colors.teal,
            colors.orange,
            colors.gray
        ];

        new Chart(categoryCtx, {
            type: 'doughnut',
            data: {
                labels: categoryLabels,
                datasets: [{
                    data: categoryData,
                    backgroundColor: categoryColors,
                    borderWidth: 2,
                    borderColor: '#fff',
                    hoverBorderWidth: 3,
                    hoverOffset: 8
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '65%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 12,
                            font: { size: 11 },
                            generateLabels: function (chart) {
                                var data = chart.data;
                                var total = data.datasets[0].data.reduce(function (a, b) { return a + b; }, 0);
                                return data.labels.map(function (label, i) {
                                    var value = data.datasets[0].data[i];
                                    var percent = ((value / total) * 100).toFixed(1);
                                    return {
                                        text: label + ' (' + percent + '%)',
                                        fillStyle: data.datasets[0].backgroundColor[i],
                                        hidden: false,
                                        index: i,
                                        pointStyle: 'circle'
                                    };
                                });
                            }
                        }
                    },
                    tooltip: {
                        backgroundColor: 'rgba(0, 0, 0, 0.8)',
                        padding: 12,
                        cornerRadius: 8,
                        callbacks: {
                            label: function (context) {
                                var total = context.dataset.data.reduce(function (a, b) { return a + b; }, 0);
                                var value = context.parsed;
                                var percent = ((value / total) * 100).toFixed(1);
                                return context.label + ': ' + value + ' s\u1EA3n ph\u1EA9m (' + percent + '%)';
                            }
                        }
                    }
                }
            }
        });
    }
});


/**
 * Formats a date string for display purposes.
 * @param {string} dateStr - The date string to format
 * @returns {string} Formatted date string
 */
const formatDisplayDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
};

