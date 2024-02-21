/**
 * Stock Movement Form - Smart Inventory Management
 * Handles dynamic field visibility, validation, and auto-calculation
 */

document.addEventListener('DOMContentLoaded', function () {
    // Initialize field visibility based on current type value
    toggleWarehouseFields();

    // Form validation
    var form = document.getElementById('movementForm');
    if (form) {
        form.addEventListener('submit', function (event) {
            if (!validateMovementForm()) {
                event.preventDefault();
                event.stopPropagation();
            }
        });
    }
});

/**
 * Toggle visibility of warehouse fields based on movement type
 */
function toggleWarehouseFields() {
    var typeSelect = document.getElementById('type');
    if (!typeSelect) return;

    var type = typeSelect.value;

    var fromGroup = document.getElementById('fromWarehouseGroup');
    var toGroup = document.getElementById('toWarehouseGroup');
    var warehouseGroup = document.getElementById('warehouseGroup');
    var submitBtn = document.getElementById('submitBtn');
    var summaryEl = document.getElementById('movementSummary');
    var summaryText = document.getElementById('summaryText');

    // Hide all first
    if (fromGroup) fromGroup.style.display = 'none';
    if (toGroup) toGroup.style.display = 'none';
    if (warehouseGroup) warehouseGroup.style.display = 'none';

    // Reset required attributes
    var fromSelect = document.getElementById('fromWarehouseId');
    var toSelect = document.getElementById('toWarehouseId');
    var warehouseSelect = document.getElementById('warehouseId');

    if (fromSelect) fromSelect.removeAttribute('required');
    if (toSelect) toSelect.removeAttribute('required');
    if (warehouseSelect) warehouseSelect.removeAttribute('required');

    switch (type) {
        case 'INBOUND':
            // Nhap kho: only show "To warehouse" (destination)
            if (toGroup) toGroup.style.display = 'block';
            if (toSelect) toSelect.setAttribute('required', 'required');
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-arrow-down me-1"></i>X\u00E1c nh\u1EADn nh\u1EADp kho';
                submitBtn.className = 'btn btn-success';
            }
            if (summaryEl && summaryText) {
                summaryEl.style.display = 'block';
                summaryText.textContent = 'Phi\u1EBFu NH\u1EACP KHO: H\u00E0ng s\u1EBD \u0111\u01B0\u1EE3c nh\u1EADp v\u00E0o kho \u0111\u00EDch.';
            }
            break;

        case 'OUTBOUND':
            // Xuat kho: only show "From warehouse" (source)
            if (fromGroup) fromGroup.style.display = 'block';
            if (fromSelect) fromSelect.setAttribute('required', 'required');
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-arrow-up me-1"></i>X\u00E1c nh\u1EADn xu\u1EA5t kho';
                submitBtn.className = 'btn btn-danger';
            }
            if (summaryEl && summaryText) {
                summaryEl.style.display = 'block';
                summaryText.textContent = 'Phi\u1EBFu XU\u1EA4T KHO: H\u00E0ng s\u1EBD \u0111\u01B0\u1EE3c xu\u1EA5t t\u1EEB kho ngu\u1ED3n.';
            }
            break;

        case 'TRANSFER':
            // Chuyen kho: show both from and to
            if (fromGroup) fromGroup.style.display = 'block';
            if (toGroup) toGroup.style.display = 'block';
            if (fromSelect) fromSelect.setAttribute('required', 'required');
            if (toSelect) toSelect.setAttribute('required', 'required');
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-exchange-alt me-1"></i>X\u00E1c nh\u1EADn chuy\u1EC3n kho';
                submitBtn.className = 'btn btn-info';
            }
            if (summaryEl && summaryText) {
                summaryEl.style.display = 'block';
                summaryText.textContent = 'Phi\u1EBFu CHUY\u1EC2N KHO: H\u00E0ng s\u1EBD \u0111\u01B0\u1EE3c chuy\u1EC3n t\u1EEB kho ngu\u1ED3n sang kho \u0111\u00EDch.';
            }
            break;

        case 'ADJUSTMENT':
            // Dieu chinh: show single warehouse
            if (warehouseGroup) warehouseGroup.style.display = 'block';
            if (warehouseSelect) warehouseSelect.setAttribute('required', 'required');
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-sliders-h me-1"></i>X\u00E1c nh\u1EADn \u0111i\u1EC1u ch\u1EC9nh';
                submitBtn.className = 'btn btn-warning';
            }
            if (summaryEl && summaryText) {
                summaryEl.style.display = 'block';
                summaryText.textContent = 'Phi\u1EBFu \u0110I\u1EC0U CH\u1EC8NH: S\u1ED1 l\u01B0\u1EE3ng t\u1ED3n kho s\u1EBD \u0111\u01B0\u1EE3c \u0111i\u1EC1u ch\u1EC9nh theo gi\u00E1 tr\u1ECB nh\u1EADp.';
            }
            break;

        default:
            if (submitBtn) {
                submitBtn.innerHTML = '<i class="fas fa-check me-1"></i>X\u00E1c nh\u1EADn';
                submitBtn.className = 'btn btn-primary';
            }
            if (summaryEl) summaryEl.style.display = 'none';
            break;
    }
}

/**
 * Validate the movement form before submission
 */
function validateMovementForm() {
    var type = document.getElementById('type').value;
    var productId = document.getElementById('productId').value;
    var quantity = document.getElementById('quantity').value;
    var reason = document.getElementById('reason').value;
    var isValid = true;
    var errors = [];

    // Type validation
    if (!type) {
        errors.push('Vui l\u00F2ng ch\u1ECDn lo\u1EA1i phi\u1EBFu');
        isValid = false;
    }

    // Product validation
    if (!productId) {
        errors.push('Vui l\u00F2ng ch\u1ECDn s\u1EA3n ph\u1EA9m');
        isValid = false;
    }

    // Quantity validation
    if (!quantity || parseInt(quantity) <= 0) {
        errors.push('S\u1ED1 l\u01B0\u1EE3ng ph\u1EA3i l\u1EDBn h\u01A1n 0');
        isValid = false;
    }

    // Reason validation
    if (!reason || reason.trim().length < 3) {
        errors.push('Vui l\u00F2ng nh\u1EADp l\u00FD do (t\u1ED1i thi\u1EC3u 3 k\u00FD t\u1EF1)');
        isValid = false;
    }

    // Warehouse validation based on type
    if (type === 'INBOUND') {
        var toWh = document.getElementById('toWarehouseId').value;
        if (!toWh) {
            errors.push('Vui l\u00F2ng ch\u1ECDn kho \u0111\u00EDch');
            isValid = false;
        }
    } else if (type === 'OUTBOUND') {
        var fromWh = document.getElementById('fromWarehouseId').value;
        if (!fromWh) {
            errors.push('Vui l\u00F2ng ch\u1ECDn kho ngu\u1ED3n');
            isValid = false;
        }
    } else if (type === 'TRANSFER') {
        var fromWhT = document.getElementById('fromWarehouseId').value;
        var toWhT = document.getElementById('toWarehouseId').value;
        if (!fromWhT) {
            errors.push('Vui l\u00F2ng ch\u1ECDn kho ngu\u1ED3n');
            isValid = false;
        }
        if (!toWhT) {
            errors.push('Vui l\u00F2ng ch\u1ECDn kho \u0111\u00EDch');
            isValid = false;
        }
        if (fromWhT && toWhT && fromWhT === toWhT) {
            errors.push('Kho ngu\u1ED3n v\u00E0 kho \u0111\u00EDch kh\u00F4ng \u0111\u01B0\u1EE3c tr\u00F9ng nhau');
            isValid = false;
        }
    } else if (type === 'ADJUSTMENT') {
        var wh = document.getElementById('warehouseId').value;
        if (!wh) {
            errors.push('Vui l\u00F2ng ch\u1ECDn kho');
            isValid = false;
        }
    }

    if (!isValid) {
        alert('Vui l\u00F2ng ki\u1EC3m tra l\u1EA1i:\n- ' + errors.join('\n- '));
    }

    return isValid;
}

/**
 * Load current stock info when product is selected
 */
var productSelect = document.getElementById('productId');
if (productSelect) {
    productSelect.addEventListener('change', function () {
        var productId = this.value;
        var stockInfo = document.getElementById('currentStockInfo');

        if (!productId || !stockInfo) {
            if (stockInfo) stockInfo.innerHTML = '<span class="text-muted">Ch\u1ECDn s\u1EA3n ph\u1EA9m</span>';
            return;
        }

        stockInfo.innerHTML = '<span class="spinner-border spinner-border-sm" role="status"></span> \u0110ang t\u1EA3i...';

        fetch('/api/products/' + productId + '/stock')
            .then(function (response) {
                if (!response.ok) throw new Error('API error');
                return response.json();
            })
            .then(function (data) {
                stockInfo.innerHTML = '<span class="fw-bold">' + data.totalStock + '</span> <small>' + (data.unit || '\u0111\u01A1n v\u1ECB') + '</small>';
            })
            .catch(function () {
                stockInfo.innerHTML = '<span class="text-muted">Kh\u00F4ng th\u1EC3 t\u1EA3i</span>';
            });
    });
}
