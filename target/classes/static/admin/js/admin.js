// Configuration
const API_BASE_URL = 'http://192.168.11.101:8080/api/admin';

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    loadPage('dashboard');

    // Navigation - Exclure les liens avec la classe 'external-link'
    document.querySelectorAll('.nav-link:not(.external-link)').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const page = link.dataset.page;
            if (page) {
                loadPage(page);
                // Mettre à jour l'état actif
                document.querySelectorAll('.nav-link').forEach(nav => nav.classList.remove('active'));
                link.classList.add('active');
                document.getElementById('pageTitle').textContent = link.textContent.trim();
            }
        });
    });
});

function loadPage(page) {
    const content = document.getElementById('content');
    content.innerHTML = '<div class="loading"><i class="fas fa-spinner"></i><p>Chargement...</p></div>';

    switch(page) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'clients':
            loadClients();
            break;
        case 'drivers':
            loadDrivers();
            break;
        case 'rides':
            loadRides();
            break;
    }
}

function refreshData() {
    loadPage(document.querySelector('.nav-link.active').dataset.page);
}

// Dashboard
async function loadDashboard() {
    const content = document.getElementById('content');

    try {
        const response = await fetch(`${API_BASE_URL}/stats`);
        const stats = await response.json();

        const clientsResponse = await fetch(`${API_BASE_URL}/clients`);
        const driversResponse = await fetch(`${API_BASE_URL}/drivers`);
        const ridesResponse = await fetch(`${API_BASE_URL}/rides`);

        const clients = await clientsResponse.json();
        const drivers = await driversResponse.json();
        const rides = await ridesResponse.json();

        content.innerHTML = `
            <div class="row">
                <div class="col-md-3">
                    <div class="stat-card text-center">
                        <i class="fas fa-users"></i>
                        <h3>${stats.totalClients || 0}</h3>
                        <p>Total Clients</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card text-center">
                        <i class="fas fa-id-card"></i>
                        <h3>${stats.totalDrivers || 0}</h3>
                        <p>Total Chauffeurs</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card text-center">
                        <i class="fas fa-history"></i>
                        <h3>${stats.totalRides || 0}</h3>
                        <p>Total Courses</p>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="stat-card text-center">
                        <i class="fas fa-clock"></i>
                        <h3>${stats.pendingRides || 0}</h3>
                        <p>Courses en attente</p>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-6">
                    <div class="chart-container">
                        <canvas id="statusChart"></canvas>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="chart-container">
                        <canvas id="usersChart"></canvas>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="col-md-12">
                    <div class="table-container">
                        <h5 class="mb-3">📋 Dernières courses</h5>
                        <div class="table-responsive">
                            <table class="table table-striped">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Client</th>
                                        <th>Départ</th>
                                        <th>Destination</th>
                                        <th>Prix</th>
                                        <th>Statut</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${rides.slice(0, 5).map(ride => `
                                        <tr>
                                            <td>#${ride.id}</td>
                                            <td>${ride.clientName}</td>
                                            <td>${ride.pickupAddress}</td>
                                            <td>${ride.destinationAddress}</td>
                                            <td>${ride.estimatedPrice} FCFA</td>
                                            <td><span class="status-${ride.status.toLowerCase()}">${ride.status}</span></td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Charts
        const ctx1 = document.getElementById('statusChart').getContext('2d');
        new Chart(ctx1, {
            type: 'doughnut',
            data: {
                labels: ['En attente', 'Acceptées', 'Terminées'],
                datasets: [{
                    data: [stats.pendingRides || 0, stats.acceptedRides || 0, stats.completedRides || 0],
                    backgroundColor: ['#FF9800', '#2196F3', '#4CAF50']
                }]
            }
        });

        const ctx2 = document.getElementById('usersChart').getContext('2d');
        new Chart(ctx2, {
            type: 'bar',
            data: {
                labels: ['Clients', 'Chauffeurs'],
                datasets: [{
                    label: 'Nombre',
                    data: [clients.length, drivers.length],
                    backgroundColor: '#FF6F00'
                }]
            }
        });

    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Clients
async function loadClients() {
    const content = document.getElementById('content');

    try {
        const response = await fetch(`${API_BASE_URL}/clients`);
        const clients = await response.json();

        content.innerHTML = `
            <div class="table-container">
                <h5 class="mb-3">👥 Liste des clients</h5>
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nom</th>
                                <th>Email</th>
                                <th>Téléphone</th>
                                <th>Date création</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${clients.map(client => `
                                <tr>
                                    <td>#${client.id}</td>
                                    <td>${client.fullName}</td>
                                    <td>${client.email}</td>
                                    <td>${client.phone}</td>
                                    <td>${new Date(client.createdAt).toLocaleDateString()}</td>
                                    <td>
                                        <button class="btn btn-sm btn-danger btn-action" onclick="deleteClient(${client.id})">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Chauffeurs
async function loadDrivers() {
    const content = document.getElementById('content');

    try {
        const response = await fetch(`${API_BASE_URL}/drivers`);
        const drivers = await response.json();

        content.innerHTML = `
            <div class="table-container">
                <h5 class="mb-3">🚖 Liste des chauffeurs</h5>
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Nom</th>
                                <th>Email</th>
                                <th>Téléphone</th>
                                <th>Véhicule</th>
                                <th>Plaque</th>
                                <th>Statut</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${drivers.map(driver => `
                                <tr>
                                    <td>#${driver.id}</td>
                                    <td>${driver.fullName}</td>
                                    <td>${driver.email}</td>
                                    <td>${driver.phone}</td>
                                    <td>${driver.vehicleType}</td>
                                    <td>${driver.licensePlate}</td>
                                    <td><span class="badge ${driver.status === 'ONLINE' ? 'bg-success' : 'bg-secondary'}">${driver.status}</span></td>
                                    <td>
                                        <button class="btn btn-sm btn-danger btn-action" onclick="deleteDriver(${driver.id})">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Courses
async function loadRides() {
    const content = document.getElementById('content');

    try {
        const response = await fetch(`${API_BASE_URL}/rides`);
        const rides = await response.json();

        content.innerHTML = `
            <div class="table-container">
                <h5 class="mb-3">📋 Liste des courses</h5>
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Client</th>
                                <th>Départ</th>
                                <th>Destination</th>
                                <th>Distance</th>
                                <th>Prix</th>
                                <th>Statut</th>
                                <th>Chauffeur</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${rides.map(ride => `
                                <tr>
                                    <td>#${ride.id}</td>
                                    <td>${ride.clientName}</td>
                                    <td>${ride.pickupAddress}</td>
                                    <td>${ride.destinationAddress}</td>
                                    <td>${ride.distance} km</td>
                                    <td>${ride.estimatedPrice} FCFA</td>
                                    <td>
                                        <select class="form-select form-select-sm status-select" data-id="${ride.id}" style="width: 120px;">
                                            <option value="PENDING" ${ride.status === 'PENDING' ? 'selected' : ''}>En attente</option>
                                            <option value="ACCEPTED" ${ride.status === 'ACCEPTED' ? 'selected' : ''}>Acceptée</option>
                                            <option value="COMPLETED" ${ride.status === 'COMPLETED' ? 'selected' : ''}>Terminée</option>
                                            <option value="CANCELLED" ${ride.status === 'CANCELLED' ? 'selected' : ''}>Annulée</option>
                                        </select>
                                    </td>
                                    <td>${ride.driverName || '-'}</td>
                                    <td>
                                        <button class="btn btn-sm btn-danger btn-action" onclick="deleteRide(${ride.id})">
                                            <i class="fas fa-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        // Ajouter les événements pour les changements de statut
        document.querySelectorAll('.status-select').forEach(select => {
            select.addEventListener('change', (e) => {
                const rideId = select.dataset.id;
                const newStatus = select.value;
                updateRideStatus(rideId, newStatus);
            });
        });

    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// API Actions
async function deleteClient(id) {
    if (confirm('Supprimer ce client ?')) {
        try {
            await fetch(`${API_BASE_URL}/client/${id}`, { method: 'DELETE' });
            loadClients();
            alert('Client supprimé');
        } catch (error) {
            alert('Erreur: ' + error.message);
        }
    }
}

async function deleteDriver(id) {
    if (confirm('Supprimer ce chauffeur ?')) {
        try {
            await fetch(`${API_BASE_URL}/driver/${id}`, { method: 'DELETE' });
            loadDrivers();
            alert('Chauffeur supprimé');
        } catch (error) {
            alert('Erreur: ' + error.message);
        }
    }
}

async function deleteRide(id) {
    if (confirm('Supprimer cette course ?')) {
        try {
            await fetch(`${API_BASE_URL}/ride/${id}`, { method: 'DELETE' });
            loadRides();
            alert('Course supprimée');
        } catch (error) {
            alert('Erreur: ' + error.message);
        }
    }
}

async function updateRideStatus(rideId, status) {
    try {
        await fetch(`${API_BASE_URL}/ride/${rideId}/status?status=${status}`, { method: 'PUT' });
        alert('Statut mis à jour');
        loadRides();
    } catch (error) {
        alert('Erreur: ' + error.message);
        loadRides(); // Recharger pour remettre l'ancienne valeur
    }
}