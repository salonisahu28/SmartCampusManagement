(() => {
  "use strict";

  
const API_BASE =
  window.location.hostname === "localhost" ||
  window.location.hostname === "127.0.0.1"
    ? "http://localhost:8080"
    : "https://smart-campus-management-wm5g-4gczlhy4s-g-46.vercel.app";

  const ROLES = ["Student", "Admin", "Faculty"];
  const CATEGORIES = ["Electricity", "Cleanliness", "Infrastructure", "Maintenance", "Other"];
  const STATUSES = [
    { value: "OPEN", label: "Open" },
    { value: "IN_PROGRESS", label: "In Progress" },
    { value: "RESOLVED", label: "Resolved" }
  ];
  const SESSION_KEY = "smart-campus-session";
  let selectedRole = "Student";
  let activeTab = "";
  let departments = [];

  const escapeHtml = (value) => String(value ?? "").replace(/[&<>"']/g, (char) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  })[char]);

  function session() {
    try {
      return JSON.parse(localStorage.getItem(SESSION_KEY) || "null");
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }

  async function request(path, options = {}) {
    const token = session()?.token;
    const headers = new Headers(options.headers || {});
    if (options.body) headers.set("Content-Type", "application/json");
    if (token) headers.set("Authorization", `Bearer ${token}`);

    const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
    const body = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
      if (response.status === 401 && token) {
        localStorage.removeItem(SESSION_KEY);
      }
      throw new Error(body?.detail || body?.message || body?.error || `Request failed (${response.status})`);
    }
    return body;
  }

  function toast(message) {
    const element = document.getElementById("toast");
    if (!element) return;
    element.textContent = message;
    element.classList.add("show");
    clearTimeout(window._tt);
    window._tt = setTimeout(() => element.classList.remove("show"), 3600);
  }

  function showError(error) {
    toast(error instanceof Error ? error.message : "Unexpected error");
  }

  async function loadDepartments() {
    if (departments.length) return departments;
    departments = await request("/api/departments");
    return departments;
  }

  function renderRoleTabs() {
    const container = document.getElementById("roleTabs");
    if (!container) return;
    container.innerHTML = ROLES.map((role) => {
      const active = role === selectedRole;
      return `<button type="button" class="${active ? "active" : ""}" ${active ? 'aria-pressed="true"' : ""}
        onclick="selectRole('${role}')">${role} Login</button>`;
    }).join("");
  }

  async function renderLoginCard() {
    const card = document.getElementById("loginCard");
    if (!card) return;
    const allDepartments = await loadDepartments().catch(() => []);
    const colors = { Student: "var(--navy)", Admin: "var(--maroon)", Faculty: "var(--green)" };
    const departmentControl = selectedRole === "Faculty"
      ? `<label for="loginDept">Department</label><select id="loginDept">
          ${allDepartments.map((dept) => `<option value="${dept.departmentId}">${escapeHtml(dept.departmentName)}</option>`).join("")}
        </select>`
      : "";
    card.innerHTML = `
      <span class="role-label" style="background:${colors[selectedRole]}">${selectedRole} Portal Access</span>
      <label for="loginEmail">Campus email</label>
      <input id="loginEmail" type="email" autocomplete="username" required placeholder="${selectedRole === 'Admin' ? 'admin@campus.edu' : 'name@campus.edu'}">
      <label for="loginPassword">Password</label>
      <input id="loginPassword" type="password" autocomplete="current-password" required>
      ${departmentControl}
      <div style="margin-top:16px;"><button class="btn" style="background:${colors[selectedRole]};border-color:${colors[selectedRole]}"
        onclick="login()">Sign In</button></div>
      ${selectedRole === "Student" ? '<p class="small-note" style="margin-top:16px;">New student? <button class="btn outline small" onclick="showRegistration()">Create an account</button></p>' : ""}
      <p class="small-note" style="margin-top:12px;">Sign-in is verified by the campus server. Staff accounts must be provisioned by an administrator.</p>`;
  }

  async function renderRegistration() {
    const allDepartments = await loadDepartments();
    const roleTabs = document.getElementById("roleTabs");
    if (roleTabs) roleTabs.innerHTML = "";
    const card = document.getElementById("loginCard");
    if (!card) return;
    card.innerHTML = `
      <span class="role-label" style="background:var(--navy)">Student Registration</span>
      <label for="registerName">Full name</label><input id="registerName" maxlength="100" required>
      <label for="registerEmail">Campus email</label><input id="registerEmail" type="email" maxlength="150" required>
      <label for="registerPassword">Password</label><input id="registerPassword" type="password" minlength="8" maxlength="72" required>
      <label for="registerDept">Department (optional)</label>
      <select id="registerDept"><option value="">Select a department</option>
        ${allDepartments.map((dept) => `<option value="${dept.departmentId}">${escapeHtml(dept.departmentName)}</option>`)}
      </select>
      <div style="margin-top:16px;display:flex;gap:8px;">
        <button class="btn" onclick="registerStudent()">Create Account</button>
        <button class="btn outline" onclick="returnToLogin()">Back to Login</button>
      </div>`;
  }

  function selectRole(role) {
    if (!ROLES.includes(role)) return;
    selectedRole = role;
    renderRoleTabs();
    renderLoginCard().catch(showError);
  }

  function showRegistration() {
    renderRegistration().catch(showError);
  }

  function returnToLogin() {
    selectedRole = "Student";
    renderRoleTabs();
    renderLoginCard().catch(showError);
  }

  async function registerStudent() {
    try {
      const name = document.getElementById("registerName").value.trim();
      const email = document.getElementById("registerEmail").value.trim();
      const password = document.getElementById("registerPassword").value;
      const departmentId = document.getElementById("registerDept").value;
      await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
          name,
          email,
          password,
          role: "STUDENT",
          departmentId: departmentId ? Number(departmentId) : null
        })
      });
      toast("Student account created. Sign in with your email and password.");
      returnToLogin();
      const emailInput = document.getElementById("loginEmail");
      if (emailInput) emailInput.value = email;
    } catch (error) {
      showError(error);
    }
  }

  async function login() {
    try {
      const email = document.getElementById("loginEmail").value.trim();
      const password = document.getElementById("loginPassword").value;
      const result = await request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password })
      });
      if (result.role !== selectedRole.toUpperCase()) {
        throw new Error(`This account is registered as ${result.role}, not ${selectedRole}. Select the matching login.`);
      }
      const chosenDepartmentId = document.getElementById("loginDept")?.value;
      if (selectedRole.toUpperCase() === "FACULTY" && result.departmentId
          && Number(chosenDepartmentId) !== result.departmentId) {
        throw new Error("Select the department assigned to your faculty account.");
      }
      localStorage.setItem(SESSION_KEY, JSON.stringify(result));
      activeTab = "";
      await boot();
    } catch (error) {
      showError(error);
    }
  }

  function logout() {
    localStorage.removeItem(SESSION_KEY);
    activeTab = "";
    boot().catch(showError);
  }

  function setTab(tab) {
    activeTab = tab;
    boot().catch(showError);
  }

  function prettyStatus(status) {
    return ({ OPEN: "Open", IN_PROGRESS: "In Progress", RESOLVED: "Resolved" })[status] || status;
  }

  function statusClass(status) {
    return prettyStatus(status).replaceAll(" ", "-");
  }

  function formatDate(value) {
    if (!value) return "—";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? escapeHtml(value) : date.toLocaleString();
  }

  async function loadComplaints() {
    const current = session();
    return request(current.role === "STUDENT" ? "/api/complaints/mine" : "/api/complaints");
  }

  function complaintHistory(complaint) {
    const history = complaint.history || [];
    return `<div class="hist">${history.map((item) =>
      `<div>${formatDate(item.updatedAt)} — ${escapeHtml(prettyStatus(item.status))}</div>`
    ).join("")}</div>`;
  }

  async function complaintFormView(current) {
    const allDepartments = await loadDepartments();
    return `<div class="card">
      <h2 style="font-size:18px;margin-bottom:6px;">Register a Complaint</h2>
      <p class="small-note">Submitted as ${escapeHtml(current.name)} (${escapeHtml(current.email)})</p>
      <label for="f_cat">Category</label>
      <select id="f_cat">${CATEGORIES.map((category) => `<option>${escapeHtml(category)}</option>`).join("")}</select>
      <label for="f_dept">Department</label>
      <select id="f_dept">${allDepartments.map((dept) =>
        `<option value="${dept.departmentId}">${escapeHtml(dept.departmentName)}</option>`
      ).join("")}</select>
      <label for="f_desc">Description</label>
      <textarea id="f_desc" maxlength="4000" placeholder="Describe the issue, location, and relevant details..."></textarea>
      <div style="margin-top:14px;"><button class="btn" onclick="submitComplaint()">Submit Complaint</button></div>
    </div>`;
  }

  async function submitComplaint() {
    try {
      const description = document.getElementById("f_desc").value.trim();
      if (!description) throw new Error("Please describe the issue before submitting.");
      const result = await request("/api/complaints", {
        method: "POST",
        body: JSON.stringify({
          category: document.getElementById("f_cat").value,
          departmentId: Number(document.getElementById("f_dept").value),
          description
        })
      });
      toast(`Complaint #${result.complaintId} registered.`);
      activeTab = "status";
      await boot();
    } catch (error) {
      showError(error);
    }
  }

  function statusView(list) {
    if (!list.length) return '<div class="card"><p class="small-note">No complaints yet. Use "New Complaint" to report one.</p></div>';
    return `<div class="card"><h2 style="font-size:18px;margin-bottom:10px;">My Complaints</h2>
      <div style="overflow-x:auto"><table><thead><tr><th>ID</th><th>Category</th><th>Department</th><th>Status</th><th>Filed</th><th>History</th></tr></thead>
      <tbody>${list.map((complaint) => `<tr>
        <td>#${complaint.complaintId}</td><td>${escapeHtml(complaint.category)}</td>
        <td>${escapeHtml(complaint.departmentName)}</td>
        <td><span class="badge ${statusClass(complaint.status)}">${escapeHtml(prettyStatus(complaint.status))}</span></td>
        <td>${formatDate(complaint.createdAt)}</td><td>${complaintHistory(complaint)}</td>
      </tr>`).join("")}</tbody></table></div></div>`;
  }

  function dashboardView(list, faculty = false) {
    const rows = list.map((complaint) => `<tr>
      <td>#${complaint.complaintId}</td><td>${escapeHtml(complaint.studentName)}</td>
      <td>${escapeHtml(complaint.category)}</td><td>${escapeHtml(complaint.departmentName)}</td>
      <td><span class="badge ${statusClass(complaint.status)}">${escapeHtml(prettyStatus(complaint.status))}</span></td>
      ${faculty ? `<td>${formatDate(complaint.createdAt)}</td>` : `<td>
        <select id="status_${complaint.complaintId}" style="width:auto;display:inline-block">
          ${STATUSES.map(({ value, label }) => `<option value="${value}" ${value === complaint.status ? "selected" : ""}>${label}</option>`).join("")}
        </select>
        <button class="btn small" onclick="updateStatus(${complaint.complaintId})">Save</button>
      </td>`}
    </tr>`).join("");
    return `<div class="card"><h2 style="font-size:18px;margin-bottom:10px;">${faculty ? `${escapeHtml(session().department || "Department")} — Monitoring` : "All Complaints"}</h2>
      ${list.length ? `<div style="overflow-x:auto"><table><thead><tr>
        <th>ID</th><th>Student</th><th>Category</th><th>Department</th><th>Status</th><th>${faculty ? "Filed" : "Update"}</th>
      </tr></thead><tbody>${rows}</tbody></table></div>` : '<p class="small-note">No complaints found.</p>'}</div>`;
  }

  async function updateStatus(id) {
    try {
      const status = document.getElementById(`status_${id}`).value;
      await request(`/api/complaints/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({ status })
      });
      toast(`Complaint #${id} status updated.`);
      await boot();
    } catch (error) {
      showError(error);
    }
  }

  function analyticsView(list) {
    const counts = { OPEN: 0, IN_PROGRESS: 0, RESOLVED: 0 };
    const grouped = new Map(departments.map((department) => [
      department.departmentName, { pending: 0, total: 0 }
    ]));
    for (const complaint of list) {
      counts[complaint.status] = (counts[complaint.status] || 0) + 1;
      const totals = grouped.get(complaint.departmentName) || { pending: 0, total: 0 };
      totals.total++;
      if (complaint.status !== "RESOLVED") totals.pending++;
      grouped.set(complaint.departmentName, totals);
    }
    return `<div class="grid" style="margin-bottom:16px;">
      ${[
        ["Total Complaints", list.length, "var(--navy)"],
        ["Open", counts.OPEN, "var(--brick)"],
        ["In Progress", counts.IN_PROGRESS, "var(--gold)"],
        ["Resolved", counts.RESOLVED, "var(--green)"]
      ].map(([label, number, color]) => `<div class="stat" style="border-left-color:${color}">
        <div class="n">${number}</div><div class="l">${label}</div></div>`).join("")}
      </div><div class="card"><h2 style="font-size:18px;margin-bottom:10px;">Department-wise Pending Workload</h2>
      <div style="overflow-x:auto"><table><thead><tr><th>Department</th><th>Pending</th><th>Total</th></tr></thead>
      <tbody>${departments.map((department) => {
        const totals = grouped.get(department.departmentName) || { pending: 0, total: 0 };
        return `<tr><td>${escapeHtml(department.departmentName)}</td><td>${totals.pending}</td><td>${totals.total}</td></tr>`;
      }).join("")}</tbody></table></div></div>`;
  }

  async function renderView(current) {
    const view = document.getElementById("view");
    if (!view) return;
    view.innerHTML = '<div class="card"><p class="small-note">Loading campus data…</p></div>';
    try {
      const needsDepartments = current.role !== "STUDENT" || activeTab === "form";
      const tasks = [loadComplaints()];
      if (needsDepartments) tasks.push(loadDepartments());
      const [list] = await Promise.all(tasks);
      if (activeTab === "form") view.innerHTML = await complaintFormView(current);
      else if (activeTab === "status") view.innerHTML = statusView(list);
      else if (activeTab === "dashboard") view.innerHTML = dashboardView(list);
      else if (activeTab === "analytics") view.innerHTML = analyticsView(list);
      else if (activeTab === "monitor") view.innerHTML = dashboardView(list, true);
      else if (activeTab === "staff") view.innerHTML = staffView();
    } catch (error) {
      view.innerHTML = `<div class="card"><p class="small-note">${escapeHtml(error.message)}</p>
        <button class="btn small" onclick="setTab('${activeTab}')">Retry</button></div>`;
      if (error.message.includes("401") || error.message.includes("Unauthorized")) logout();
    }
  }

  function staffView() {
    return `<div class="card"><h2 style="font-size:18px;margin-bottom:6px;">Provision Staff Account</h2>
      <p class="small-note">Staff accounts cannot be created from public registration.</p>
      <label for="staffName">Full name</label><input id="staffName" maxlength="100" required>
      <label for="staffEmail">Official email</label><input id="staffEmail" type="email" maxlength="150" required>
      <label for="staffPassword">Temporary password</label><input id="staffPassword" type="password" minlength="8" maxlength="72" required>
      <label for="staffRole">Role</label><select id="staffRole" onchange="toggleStaffDepartment()">
        <option value="FACULTY">Faculty</option><option value="ADMIN">Admin</option>
      </select>
      <div id="staffDepartmentField"><label for="staffDepartment">Department</label>
        <select id="staffDepartment">${departments.map((department) =>
          `<option value="${department.departmentId}">${escapeHtml(department.departmentName)}</option>`
        ).join("")}</select></div>
      <div style="margin-top:14px;"><button class="btn" onclick="createStaff()">Create Staff Account</button></div>
    </div>`;
  }

  function toggleStaffDepartment() {
    const field = document.getElementById("staffDepartmentField");
    if (field) {
      field.classList.toggle("hidden", document.getElementById("staffRole").value === "ADMIN");
    }
  }

  async function createStaff() {
    try {
      const role = document.getElementById("staffRole").value;
      const department = document.getElementById("staffDepartment");
      await request("/api/admin/users", {
        method: "POST",
        body: JSON.stringify({
          name: document.getElementById("staffName").value.trim(),
          email: document.getElementById("staffEmail").value.trim(),
          password: document.getElementById("staffPassword").value,
          role,
          departmentId: role === "FACULTY" ? Number(department.value) : null
        })
      });
      document.getElementById("staffName").value = "";
      document.getElementById("staffEmail").value = "";
      document.getElementById("staffPassword").value = "";
      toast(`${role} account created.`);
    } catch (error) {
      showError(error);
    }
  }

  async function boot() {
    const current = session();
    const loginScreen = document.getElementById("login-screen");
    const app = document.getElementById("app");

    if (!current?.token) {
      if (loginScreen) loginScreen.classList.remove("hidden");
      if (app) app.classList.add("hidden");
      try {
        await loadDepartments();
      } catch (error) {
        departments = [];
      }
      renderRoleTabs();
      await renderLoginCard();
      return;
    }

    if (loginScreen) loginScreen.classList.add("hidden");
    if (app) app.classList.remove("hidden");
    document.getElementById("userName").textContent = current.name;
    document.getElementById("userRole").textContent = current.role;
    const tabsFor = {
      STUDENT: [["form", "New Complaint"], ["status", "My Complaints"]],
      ADMIN: [["dashboard", "Dashboard"], ["analytics", "Analytics"], ["staff", "Staff Accounts"]],
      FACULTY: [["monitor", "Monitoring Dashboard"]]
    }[current.role];
    if (!tabsFor) {
      localStorage.removeItem(SESSION_KEY);
      await boot();
      return;
    }
    if (!tabsFor.some(([id]) => id === activeTab)) activeTab = tabsFor[0][0];
    document.getElementById("tabs").innerHTML = tabsFor.map(([id, label]) =>
      `<button class="tab ${id === activeTab ? "active" : ""}" onclick="setTab('${id}')">${label}</button>`
    ).join("");
    await renderView(current);
  }

  Object.assign(window, {
    boot, login, logout, selectRole, setTab, showRegistration, returnToLogin,
    registerStudent, submitComplaint, updateStatus, createStaff, toggleStaffDepartment
  });
  boot().catch(showError);
})();
