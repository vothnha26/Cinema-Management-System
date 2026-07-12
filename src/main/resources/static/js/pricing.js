    // 1. Hằng số
    const DAYS = [
        { id: 'MONDAY', name: 'Thứ 2' }, { id: 'TUESDAY', name: 'Thứ 3' },
        { id: 'WEDNESDAY', name: 'Thứ 4' }, { id: 'THURSDAY', name: 'Thứ 5' },
        { id: 'FRIDAY', name: 'Thứ 6' }, { id: 'SATURDAY', name: 'Thứ 7' },
        { id: 'SUNDAY', name: 'Chủ Nhật' }
    ];

    // 2. Biến toàn cục
    let allPrices = [], allRules = [], ROOM_TYPES = [], SEAT_TYPES = [], FORMATS = [], BRANCHES = [];
    let currentBranchId = null;
    let sortableInstance = null;
    let isReordering = false;
    let temporaryOrderIds = [];

    let priceModal, ruleModal, fullMatrixModal, previewModal;

    document.addEventListener('DOMContentLoaded', () => {
        // Init Modals
        const els = { price: 'priceModal', rule: 'ruleModal', matrix: 'fullMatrixModal', prev: 'previewModal' };
        priceModal = new bootstrap.Modal(document.getElementById(els.price));
        ruleModal = new bootstrap.Modal(document.getElementById(els.rule));
        fullMatrixModal = new bootstrap.Modal(document.getElementById(els.matrix));
        previewModal = new bootstrap.Modal(document.getElementById(els.prev));

        // Lắng nghe sự kiện thay đổi loại quy tắc để cập nhật UI
        document.getElementById('ruleCategory').addEventListener('change', (e) => updateCategoryUI(e.target.value));

        loadData();
    });

    function updateCategoryUI(category) {
        const impactSelect = document.getElementById('ruleImpactType');
        const maxDiscountContainer = document.getElementById('ruleMaxDiscount')?.closest('.col-md-4');
        
        // Reset options
        Array.from(impactSelect.options).forEach(opt => opt.disabled = false);

        if (category === 'BASE') {
            impactSelect.value = 'FIXED';
            Array.from(impactSelect.options).forEach(opt => { if(opt.value !== 'FIXED') opt.disabled = true; });
            if (maxDiscountContainer) maxDiscountContainer.style.display = 'none';
        } else if (category === 'SURCHARGE') {
            if (impactSelect.value === 'SUBTRACTIVE' || impactSelect.value === 'FIXED') impactSelect.value = 'ADDITIVE';
            impactSelect.options[1].disabled = true; // Disable Subtractive
            if (maxDiscountContainer) maxDiscountContainer.style.display = 'none';
        } else if (category === 'DISCOUNT') {
            if (impactSelect.value === 'ADDITIVE' || impactSelect.value === 'FIXED') impactSelect.value = 'SUBTRACTIVE';
            impactSelect.options[0].disabled = true; // Disable Additive
            if (maxDiscountContainer) maxDiscountContainer.style.display = 'block';
        }
    }

    async function identifyBranch() {
        const role = localStorage.getItem('cinemaRole');
        const username = localStorage.getItem('cinemaUsername');
        const cachedBranchId = localStorage.getItem('cinemaBranchId');
        
        if (role !== 'MANAGER') return;
        if (cachedBranchId && cachedBranchId !== 'null') {
            currentBranchId = cachedBranchId;
        }

        try {
            console.log(">>> Identifying branch for manager:", username);
            const res = await api.get('/admin/staffs/assignments');
            if (res.success) {
                const staff = Object.values(res.data).find(s => s.user.username === username);
                if (staff && staff.branch) {
                    currentBranchId = staff.branch.id;
                    localStorage.setItem('cinemaBranchId', currentBranchId);
                    console.log(">>> Branch identified:", currentBranchId);
                }
            }
        } catch (e) { console.error(">>> identifyBranch Error:", e); }
    }

    async function loadData() {
        await identifyBranch();
        const role = localStorage.getItem('cinemaRole');
        const reorderSwitch = document.getElementById('enableReorder')?.closest('.form-check');
        if (reorderSwitch) {
            reorderSwitch.style.display = role === 'ADMIN' ? 'none' : 'block';
        }

        const rulesUrl = currentBranchId ? '/admin/pricing-rules?branchId=' + currentBranchId : '/admin/pricing-rules';
        console.log(">>> Loading pricing data. Rules URL:", rulesUrl);

        try {
            const [pRes, rRes, rtRes, stRes, fRes, bRes] = await Promise.all([
                api.get('/admin/pricing'), 
                api.get(rulesUrl),
                api.get('/public/master-data/room-types'), 
                api.get('/public/master-data/seat-types'),
                api.get('/public/master-data/formats'), 
                api.get('/admin/branches')
            ]);
            ROOM_TYPES = rtRes.data || []; 
            SEAT_TYPES = (stRes.data || []).filter(s => s.id !== 'EMPTY' && s.id !== 'DISABLED');
            FORMATS = fRes.data || [];
            BRANCHES = bRes.data || [];
            allPrices = pRes.data || []; 
            allRules = rRes.data || [];
            
            renderMatrix(); 
            renderRules();
            runQuickSim();
            
            const toggle = document.getElementById('enableReorder');
            if (toggle) { toggle.checked = false; toggleReorderMode(false); }
        } catch (err) { console.error(err); }
    }

    function renderMatrix() {
        const header = document.getElementById('matrixHeader'), tbody = document.getElementById('pricingMatrixBody');
        if (!header || !tbody) return;
        header.innerHTML = '<th>Phòng \\ Ghế</th>' + SEAT_TYPES.map(s => `<th>${s.name}</th>`).join('');
        tbody.innerHTML = ROOM_TYPES.map(room => `
            <tr>
                <td class="room-type-cell">${room.name}</td>
                ${SEAT_TYPES.map(seat => {
                    const p = allPrices.find(x => x.roomTypeId === room.id && x.seatTypeId === seat.id);
                    const price = p ? p.price : 80000;
                    return `<td onclick="openPriceModal('${room.id}', '${seat.id}', ${price}, '${room.name}', '${seat.name}')" style="cursor:pointer">
                        <div class="price-val-sm">${p ? new Intl.NumberFormat().format(p.price) : '---'}đ</div>
                    </td>`;
                }).join('')}
            </tr>`).join('');
    }

    function renderRules() {
        const role = localStorage.getItem('cinemaRole');
        const container = document.getElementById('globalRulesContainer');
        if (!container) return;
        if (!allRules.length) { 
            document.getElementById('noRulesMsg').style.display='block'; 
            document.getElementById('globalHeader').style.display='none';
            container.innerHTML=''; return; 
        }
        document.getElementById('noRulesMsg').style.display='none';
        document.getElementById('globalHeader').style.display='block';
        
        const displayOrder = [...allRules].sort((a, b) => a.priority - b.priority);

        container.innerHTML = displayOrder.map(r => {
            const branchCond = r.conditions.find(c => c.type === 'BRANCH');
            const isLocal = !!branchCond;
            const canEdit = role === 'ADMIN' || isLocal;
            
            const impactSign = r.impactType === 'ADDITIVE' ? '+' : r.impactType === 'SUBTRACTIVE' ? '-' : r.impactType === 'PERCENTAGE' ? 'x' : '=';
            const impactVal = r.impactType === 'PERCENTAGE' ? r.impactValue : new Intl.NumberFormat().format(r.impactValue);

            // Phân loại màu sắc và icon
            let categoryClass = 'border-secondary', categoryIcon = 'bi-gear', categoryLabel = 'KHÁC';
            if (r.category === 'BASE') { categoryClass = 'border-primary'; categoryIcon = 'bi-cash-stack'; categoryLabel = 'GIÁ GỐC'; }
            else if (r.category === 'SURCHARGE') { categoryClass = 'border-warning'; categoryIcon = 'bi-plus-circle'; categoryLabel = 'PHỤ THU'; }
            else if (r.category === 'DISCOUNT') { categoryClass = 'border-success'; categoryIcon = 'bi-arrow-down-circle'; categoryLabel = 'GIẢM GIÁ'; }

            return `
            <div class="col-md-6 rule-wrapper" data-id="${r.id}">
                <div class="rule-card ${!r.active ? 'inactive opacity-50' : ''} ${categoryClass} border-start border-4">
                    <div class="drag-handle text-muted position-absolute" style="top:12px; right:12px; display:none"><i class="bi bi-grip-vertical fs-4"></i></div>
                    <div class="d-flex justify-content-between align-items-start">
                        <div class="flex-grow-1">
                            <div class="d-flex align-items-center gap-2 mb-2">
                                <span class="badge ${r.category==='BASE'?'bg-primary':r.category==='SURCHARGE'?'bg-warning':'bg-success'} bg-opacity-10 ${r.category==='BASE'?'text-primary':r.category==='SURCHARGE'?'text-warning':'text-success'}" style="font-size:0.6rem">
                                    <i class="bi ${categoryIcon} me-1"></i>${categoryLabel}
                                </span>
                                <span class="badge ${isLocal?'bg-info':'bg-danger'} bg-opacity-10 ${isLocal?'text-info':'text-danger'}" style="font-size:0.6rem">${isLocal?'CHI NHÁNH':'HỆ THỐNG'}</span>
                                <span class="badge ${r.active?'bg-success':'bg-secondary'} bg-opacity-10 ${r.active?'text-success':'text-secondary'}" style="font-size:0.6rem">${r.active?'ĐANG CHẠY':'TẠM NGƯNG'}</span>
                            </div>
                            <h6 class="rule-name ${r.category==='BASE'?'text-primary':r.category==='SURCHARGE'?'text-warning':'text-success'}">${r.name}</h6>
                            <div class="rule-details">
                                <p class="rule-desc small mb-2">${r.description || 'Quy tắc tự động'}</p>
                                <div class="d-flex flex-wrap gap-1">
                                    ${r.conditions.map(c => `<span class="badge-rule bg-dark text-muted border border-secondary border-opacity-25">${c.type}: ${c.value}</span>`).join('')}
                                </div>
                            </div>
                        </div>
                        <div class="text-end ms-3">
                            <div class="rule-impact-lg ${r.category==='BASE'?'text-primary':r.category==='SURCHARGE'?'text-warning':'text-success'}">${impactSign}${impactVal}${r.impactType!=='PERCENTAGE'?'đ':''}</div>
                            <div class="rule-details mt-2 d-flex gap-1 justify-content-end align-items-center">
                                ${canEdit ? `
                                <div class="form-check form-switch me-2">
                                    <input class="form-check-input" type="checkbox" ${r.active?'checked':''} onchange="toggleRuleActive(${r.id}, this.checked)">
                                </div>
                                <button class="btn btn-sm btn-dark px-3 fw-bold" onclick="openEditRule(${r.id})">SỬA</button>
                                ${r.system ? '' : `<button class="btn btn-sm btn-outline-danger border-0" onclick="deleteRule(${r.id})"><i class="bi bi-trash"></i></button>`}
                                ` : `
                                <div class="small text-muted italic" style="font-size: 0.7rem">Chỉ Admin mới có quyền sửa</div>
                                `}
                            </div>
                        </div>
                    </div>
                </div>
            </div>`;
        }).join('');

        // Khởi tạo Sortable nếu chưa có
        if (!sortableInstance) {
            sortableInstance = Sortable.create(container, {
                animation: 150, handle: '.drag-handle', ghostClass: 'sortable-ghost',
                onEnd: () => {
                    temporaryOrderIds = Array.from(container.querySelectorAll('.rule-wrapper')).map(el => el.dataset.id);
                    runQuickSim();
                }
            });
        }
    }

    function toggleReorderMode(enabled) {
        isReordering = enabled;
        const container = document.getElementById('globalRulesContainer');
        const handles = document.querySelectorAll('.drag-handle');
        const details = document.querySelectorAll('.rule-details');
        const wrappers = document.querySelectorAll('.rule-wrapper');
        const cards = document.querySelectorAll('.rule-card');

        document.getElementById('reorderHint').style.display = enabled ? 'block' : 'none';
        document.getElementById('reorderActions').style.display = enabled ? 'flex' : 'none';

        wrappers.forEach(w => {
            w.classList.toggle('col-md-6', !enabled);
            w.classList.toggle('col-md-12', enabled);
        });

        cards.forEach(c => c.classList.toggle('rule-card-compact', enabled));
        handles.forEach(h => h.style.display = enabled ? 'block' : 'none');
        details.forEach(d => d.style.display = enabled ? 'none' : 'block');

        if (sortableInstance) {
            sortableInstance.option('disabled', !enabled);
        }
        
        if (enabled) {
            temporaryOrderIds = Array.from(container.querySelectorAll('.rule-wrapper')).map(el => el.dataset.id);
        }
    }

    async function saveReorder() {
        if (!temporaryOrderIds.length) return;
        const url = currentBranchId ? `/admin/pricing-rules/reorder?branchId=${currentBranchId}` : '/admin/pricing-rules/reorder';
        const res = await api.post(url, temporaryOrderIds);
        if (res.success) { alert('Đã lưu tháp ưu tiên!'); loadData(); }
        else alert('Lỗi: ' + res.message);
    }

    function cancelReorder() { loadData(); }

    function runQuickSim() {
        let current = parseFloat(document.getElementById('quickSimBase').value) || 0;
        const container = document.getElementById('globalRulesContainer');
        const currentOrderIds = isReordering ? temporaryOrderIds : Array.from(container.querySelectorAll('.rule-wrapper')).map(el => el.dataset.id);
        
        const sorted = currentOrderIds.map(id => allRules.find(r => r.id == id)).filter(r => r && r.active);

        for (const rule of sorted) {
            if (rule.impactType === 'FIXED') current = rule.impactValue;
            else if (rule.impactType === 'ADDITIVE') current += rule.impactValue;
            else if (rule.impactType === 'SUBTRACTIVE') current -= rule.impactValue;
            else if (rule.impactType === 'PERCENTAGE') current *= rule.impactValue;
            if (!rule.stackable) break;
        }
        document.getElementById('quickSimResult').innerText = new Intl.NumberFormat().format(Math.round(current)) + 'đ';
    }

    function openFullMatrixPreview() {
        const header = document.getElementById('fullMatrixHeader'), tbody = document.getElementById('fullMatrixBody');
        header.innerHTML = '<th>Loại Phòng \\ Ghế</th>' + SEAT_TYPES.map(s => `<th>${s.name}</th>`).join('');
        tbody.innerHTML = ROOM_TYPES.map(room => `
            <tr>
                <td class="room-type-cell">${room.name}</td>
                ${SEAT_TYPES.map(seat => {
                    const p = allPrices.find(x => x.roomTypeId === room.id && x.seatTypeId === seat.id);
                    const basePrice = p ? p.price : 80000;
                    const finalPrice = calculateFinalPrice(basePrice, room.id, seat.id);
                    return `<td><div class="text-muted small strike">${new Intl.NumberFormat().format(basePrice)}đ</div><div class="fw-bold text-success fs-5">${new Intl.NumberFormat().format(Math.round(finalPrice))}đ</div></td>`;
                }).join('')}
            </tr>`).join('');
        fullMatrixModal.show();
    }

    function calculateFinalPrice(basePrice, roomTypeId, seatTypeId) {
        let current = basePrice;
        const sorted = [...allRules].filter(r => r.active).sort((a, b) => a.priority - b.priority);
        for (const rule of sorted) {
            const matches = rule.conditions.every(c => {
                if (c.type === 'ROOM_TYPE' && c.value !== roomTypeId) return false;
                if (c.type === 'SEAT_TYPE' && c.value !== seatTypeId) return false;
                if (c.type === 'BRANCH' && currentBranchId && c.value != currentBranchId) return false;
                return true;
            });
            if (matches) {
                if (rule.impactType === 'FIXED') current = rule.impactValue;
                else if (rule.impactType === 'ADDITIVE') current += rule.impactValue;
                else if (rule.impactType === 'SUBTRACTIVE') current -= rule.impactValue;
                else if (rule.impactType === 'PERCENTAGE') current *= rule.impactValue;
                if (!rule.stackable) break;
            }
        }
        return current;
    }

    function openEditRule(id) { const r = allRules.find(x => x.id == id); if (r) openRuleModal(r); }
    function openRuleModal(r = null) {
        const role = localStorage.getItem('cinemaRole');
        document.getElementById('ruleForm').reset();
        document.getElementById('conditionsContainer').innerHTML = '';
        
        let isGlobal = false;
        if (r) {
            isGlobal = !r.conditions.find(c => c.type === 'BRANCH');
            document.getElementById('ruleId').value = r.id; document.getElementById('ruleName').value = r.name;
            document.getElementById('ruleCategory').value = r.category; document.getElementById('rulePriority').value = r.priority;
            document.getElementById('ruleImpactType').value = r.impactType; document.getElementById('ruleImpactValue').value = r.impactValue;
            document.getElementById('ruleStackable').checked = r.stackable; 
            document.getElementById('ruleActive').checked = r.active;
            document.getElementById('ruleDesc').value = r.description || '';
            if (r.conditions) r.conditions.forEach(c => addConditionRow(c));
        } else {
            document.getElementById('ruleId').value = ""; document.getElementById('rulePriority').value = "100";
            document.getElementById('ruleStackable').checked = true;
            document.getElementById('ruleActive').checked = true;
            if (role === 'MANAGER' && currentBranchId) addConditionRow({ type: 'BRANCH', value: currentBranchId.toString() });
        }

        // Khóa nút lưu nếu Manager cố tình mở quy tắc hệ thống
        const saveBtn = document.querySelector('#ruleModal .btn-danger');
        if (role === 'MANAGER' && isGlobal) {
            saveBtn.disabled = true;
            saveBtn.innerText = 'CHỈ ADMIN MỚI CÓ QUYỀN SỬA';
        } else {
            saveBtn.disabled = false;
            saveBtn.innerText = 'LƯU QUY TẮC';
        }

        ruleModal.show();
    }

    function addConditionRow(condition = null) {
        const role = localStorage.getItem('cinemaRole');
        const template = document.getElementById('conditionRowTemplate');
        if (!template) return;

        const clone = template.content.cloneNode(true);
        const row = clone.querySelector('.condition-row');
        const typeSel = row.querySelector('.cond-type');
        const delBtn = row.querySelector('.text-danger');

        if (condition) {
            typeSel.value = condition.type;
        }

        if (role === 'MANAGER' && condition?.type === 'BRANCH') {
            typeSel.disabled = true;
            if (delBtn) delBtn.style.display = 'none';
        }

        updateConditionValueInput(typeSel, condition ? condition.value : '');
        document.getElementById('conditionsContainer').appendChild(row);
    }

    function updateConditionValueInput(select, value = '') {
        const container = select.closest('.condition-row').querySelector('.cond-value-container');
        const type = select.value;
        const role = localStorage.getItem('cinemaRole');
        let html = '';

        if (type === 'BRANCH') {
            html = `<select class="form-select form-select-sm cond-value" ${role==='MANAGER'?'disabled':''}>
                ${BRANCHES.map(b => `<option value="${b.id}" ${value==b.id?'selected':''}>${b.name}</option>`).join('')}
            </select>`;
        } else if (type === 'DAY_OF_WEEK') {
            const sels = value ? value.split(',') : [];
            html = `<div class="d-flex flex-wrap gap-2 pt-1">
                ${DAYS.map(d => `
                    <div class="form-check">
                        <input class="form-check-input cond-day-check" type="checkbox" value="${d.id}" id="day_${d.id}_${Math.random()}" ${sels.includes(d.id)?'checked':''}>
                        <label class="form-check-label small" style="font-size:0.7rem" for="day_${d.id}">${d.name}</label>
                    </div>
                `).join('')}
            </div>`;
        } else if (type === 'TIME_RANGE') {
            const [s, e] = value ? (value.includes('-') ? value.split('-') : value.split(',')) : ['08:00', '22:00'];
            html = `<div class="d-flex align-items-center gap-2">
                <input type="time" class="form-control form-control-sm cond-time-start" value="${s || '08:00'}">
                <span class="small text-muted">đến</span>
                <input type="time" class="form-control form-control-sm cond-time-end" value="${e || '22:00'}">
            </div>`;
        } else if (type === 'DATE_RANGE') {
            const [s, e] = value ? (value.includes(',') ? value.split(',') : value.split(':')) : ['', ''];
            html = `<div class="d-flex align-items-center gap-2">
                <input type="date" class="form-control form-control-sm cond-date-start" value="${s || ''}">
                <span class="small text-muted">đến</span>
                <input type="date" class="form-control form-control-sm cond-date-end" value="${e || ''}">
            </div>`;
        } else if (type === 'MEMBER_TIER') {
            html = `<select class="form-select form-select-sm cond-value">
                ${['GUEST','STANDARD','SILVER','GOLD','PLATINUM'].map(t => `<option value="${t}" ${value==t?'selected':''}>${t}</option>`).join('')}
            </select>`;
        } else if (['ROOM_TYPE','SEAT_TYPE','SHOW_FORMAT'].includes(type)) {
            const data = type==='ROOM_TYPE' ? ROOM_TYPES : type==='SEAT_TYPE' ? SEAT_TYPES : [{id:'2D',name:'2D'},{id:'3D',name:'3D'},{id:'IMAX',name:'IMAX'},{id:'4DX',name:'4DX'}];
            html = `<select class="form-select form-select-sm cond-value">
                ${data.map(x => `<option value="${x.id}" ${value==x.id?'selected':''}>${x.name}</option>`).join('')}
            </select>`;
        } else {
            html = `<input type="text" class="form-control form-control-sm cond-value" value="${value}" placeholder="Giá trị...">`;
        }
        container.innerHTML = html;
    }

    async function saveRule() {
        const role = localStorage.getItem('cinemaRole'), conds = Array.from(document.querySelectorAll('.condition-row')).map(row => {
            const type = row.querySelector('.cond-type').value;
            let val = type === 'DAY_OF_WEEK' ? Array.from(row.querySelectorAll('.cond-day-check:checked')).map(cb => cb.value).join(',') : type === 'TIME_RANGE' ? row.querySelector('.cond-time-start').value + '-' + row.querySelector('.cond-time-end').value : type === 'DATE_RANGE' ? row.querySelector('.cond-date-start').value + ':' + row.querySelector('.cond-date-end').value : row.querySelector('.cond-value').value;
            return { type, value: val };
        }).filter(c => c.value && c.value !== '-' && c.value !== ':');
        
        if (role === 'MANAGER' && !conds.some(c => c.type === 'BRANCH')) conds.push({ type: 'BRANCH', value: currentBranchId.toString() });
        
        const data = { 
            name: document.getElementById('ruleName').value, 
            category: document.getElementById('ruleCategory').value, 
            priority: parseInt(document.getElementById('rulePriority').value) || 1, 
            impactType: document.getElementById('ruleImpactType').value, 
            impactValue: parseFloat(document.getElementById('ruleImpactValue').value), 
            stackable: document.getElementById('ruleStackable').checked, 
            active: document.getElementById('ruleActive').checked,
            description: document.getElementById('ruleDesc').value, 
            conditions: conds 
        };

        // CẢNH BÁO GIÁ ÂM
        if (data.active) {
            let hasNegative = false;
            // Giả lập trên ma trận giá gốc
            for (const room of ROOM_TYPES) {
                for (const seat of SEAT_TYPES) {
                    const p = allPrices.find(x => x.roomTypeId === room.id && x.seatTypeId === seat.id);
                    const basePrice = p ? p.price : 80000;
                    
                    // Tạo tập quy tắc giả định (thay thế quy tắc hiện tại nếu đang sửa)
                    const id = document.getElementById('ruleId').value;
                    let tempRules = [...allRules];
                    if (id) {
                        const idx = tempRules.findIndex(r => r.id == id);
                        if (idx !== -1) tempRules[idx] = { ...data, id: id };
                    } else {
                        tempRules.push({ ...data, id: -1 });
                    }

                    // Tính toán thử
                    let current = basePrice;
                    const sorted = tempRules.filter(r => r.active).sort((a, b) => a.priority - b.priority);
                    for (const rule of sorted) {
                        const matches = rule.conditions.every(c => {
                            if (c.type === 'ROOM_TYPE' && c.value !== room.id) return false;
                            if (c.type === 'SEAT_TYPE' && c.value !== seat.id) return false;
                            if (c.type === 'BRANCH' && currentBranchId && c.value != currentBranchId) return false;
                            return true;
                        });
                        if (matches) {
                            if (rule.impactType === 'FIXED') current = rule.impactValue;
                            else if (rule.impactType === 'ADDITIVE') current += rule.impactValue;
                            else if (rule.impactType === 'SUBTRACTIVE') current -= rule.impactValue;
                            else if (rule.impactType === 'PERCENTAGE') current *= rule.impactValue;
                            if (!rule.stackable) break;
                        }
                    }
                    if (current < 0) { hasNegative = true; break; }
                }
                if (hasNegative) break;
            }

            if (hasNegative) {
                if (!confirm('CẢNH BÁO: Quy tắc này có thể khiến giá vé bị ÂM ở một số cấu hình phòng/ghế. Bạn có chắc chắn muốn lưu?')) return;
            }
        }

        const id = document.getElementById('ruleId').value, res = id ? await api.put('/admin/pricing-rules/' + id, data) : await api.post('/admin/pricing-rules', data);
        if (res.success) { ruleModal.hide(); loadData(); } else alert('Lỗi: ' + res.message);
    }

    async function toggleRuleActive(id, active) {
        const role = localStorage.getItem('cinemaRole');
        const rule = allRules.find(r => r.id == id);
        if (!rule) return;
        
        const isGlobal = !rule.conditions.find(c => c.type === 'BRANCH');
        if (role === 'MANAGER' && isGlobal) {
            alert('Bạn không có quyền thay đổi quy tắc hệ thống!');
            loadData();
            return;
        }

        const data = { ...rule, active: active };
        const res = await api.put('/admin/pricing-rules/' + id, data);
        if (res.success) {
            loadData();
        } else {
            alert('Lỗi: ' + res.message);
            loadData();
        }
    }

    async function savePrice() {
        const data = { roomTypeId: document.getElementById('roomType').value, seatTypeId: document.getElementById('seatType').value, price: parseFloat(document.getElementById('priceInput').value) };
        const res = await api.put('/admin/pricing', data);
        if (res.success) { priceModal.hide(); loadData(); } else alert('Lỗi: ' + res.message);
    }
    function openPriceModal(roomId, seatId, price, roomName, seatName) { document.getElementById('roomType').value = roomId; document.getElementById('seatType').value = seatId; document.getElementById('targetDisplay').innerText = `${roomName} - ${seatName}`; document.getElementById('priceInput').value = price; priceModal.show(); }
    async function deleteRule(id) { 
        const role = localStorage.getItem('cinemaRole');
        const rule = allRules.find(r => r.id == id);
        if (!rule) return;
        
        const isGlobal = !rule.conditions.find(c => c.type === 'BRANCH');
        if (role === 'MANAGER' && isGlobal) {
            alert('Bạn không có quyền xóa quy tắc hệ thống!');
            return;
        }

        if(confirm('Xóa quy tắc này?')) { 
            const res = await api.delete('/admin/pricing-rules/' + id); 
            if(res.success) loadData(); 
        } 
    }
