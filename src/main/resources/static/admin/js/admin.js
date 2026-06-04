// Configuration
const API_BASE_URL = 'http://localhost:8080/api/admin';
const API_BASE_URL_TAXI = 'http://localhost:8080/api/taxi';
const API_NOTIFICATIONS_URL = 'http://localhost:8080/api/notifications';

// Initialisation
document.addEventListener('DOMContentLoaded', function() {
    loadPage('dashboard');

    document.querySelectorAll('.nav-link:not(.external-link)').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const page = link.dataset.page;
            if (page) {
                loadPage(page);
                document.querySelectorAll('.nav-link').forEach(nav => nav.classList.remove('active'));
                link.classList.add('active');
                const pageTitle = document.getElementById('pageTitle');
                if (pageTitle) {
                    pageTitle.textContent = link.textContent.trim();
                }
            }
        });
    });
});

function loadPage(page) {
    const content = document.getElementById('content');
    if (!content) return;

    content.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i><p>Chargement...</p></div>';

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
        case 'reviews':
            loadReviews();
            break;
        case 'campaigns':
            loadCampaigns();
            break;
        case 'version':
            loadVersionManagement();
            break;
        case 'heatmap':
            loadHeatmapAdmin();
            break;
        case 'scheduled':
            loadScheduledRides();
            break;
        case 'disputes':
            loadDisputes();
            break;
        case 'advertising':
            loadAdvertising();
            break;
        default:
            loadDashboard();
            break;
    }
}

function refreshData() {
    const activeLink = document.querySelector('.nav-link.active');
    if (activeLink && activeLink.dataset.page) {
        loadPage(activeLink.dataset.page);
    } else {
        loadPage('dashboard');
    }
}

// Dashboard
async function loadDashboard() {
    const content = document.getElementById('content');
    if (!content) return;

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
                                    </td>
                                </thead>
                                <tbody>
                                    ${rides.slice(0, 5).map(ride => `
                                        <tr>
                                            <td>#${ride.id}</td>
                                            <td>${ride.clientName || '-'}</td>
                                            <td>${ride.pickupAddress || '-'}</td>
                                            <td>${ride.destinationAddress || '-'}</td>
                                            <td>${ride.estimatedPrice || 0} FCFA</td>
                                            <td><span class="status-${(ride.status || 'PENDING').toLowerCase()}">${ride.status || 'PENDING'}</span></td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        `;

        const statusChart = document.getElementById('statusChart');
        if (statusChart) {
            const ctx1 = statusChart.getContext('2d');
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
        }

        const usersChart = document.getElementById('usersChart');
        if (usersChart) {
            const ctx2 = usersChart.getContext('2d');
            new Chart(ctx2, {
                type: 'bar',
                data: {
                    labels: ['Clients', 'Chauffeurs'],
                    datasets: [{
                        label: 'Nombre',
                        data: [clients.length || 0, drivers.length || 0],
                        backgroundColor: '#FF6F00'
                    }]
                }
            });
        }

    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Clients
async function loadClients() {
    const content = document.getElementById('content');
    if (!content) return;

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
                                    <td>${client.fullName || '-'}</td>
                                    <td>${client.email || '-'}</td>
                                    <td>${client.phone || '-'}</td>
                                    <td>${client.createdAt ? new Date(client.createdAt).toLocaleDateString() : '-'}</td>
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
    if (!content) return;

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
                                <th>Note moyenne</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${drivers.map(driver => {
                                const ratingDisplay = driver.rating ? driver.rating.toFixed(1) : '0.0';
                                const stars = '⭐'.repeat(Math.round(driver.rating || 0));

                                let statusClass = '';
                                let statusText = '';
                                let pauseInfo = '';

                                if (driver.isOnPause) {
                                    statusClass = 'bg-warning text-dark';
                                    statusText = '⏸️ EN PAUSE';
                                    pauseInfo = `<br><small class="text-warning"><i class="fas fa-coffee"></i> ${driver.pauseReason || 'Pause'}</small>`;
                                } else if (driver.status === 'ONLINE') {
                                    statusClass = 'bg-success';
                                    statusText = 'EN LIGNE';
                                } else if (driver.status === 'ON_TRIP') {
                                    statusClass = 'bg-primary';
                                    statusText = 'EN COURSE';
                                } else {
                                    statusClass = 'bg-secondary';
                                    statusText = 'HORS LIGNE';
                                }

                                return `
                                    <tr>
                                        <td>#${driver.id}</td>
                                        <td>${driver.fullName || '-'}</td>
                                        <td>${driver.email || '-'}</td>
                                        <td>${driver.phone || '-'}</td>
                                        <td>${driver.vehicleType || '-'}</td>
                                        <td>${driver.licensePlate || '-'}</td>
                                        <td>
                                            <span class="badge ${statusClass}">${statusText}</span>
                                            ${pauseInfo}
                                        </td>
                                        <td>${ratingDisplay} ${stars} (${driver.ratingCount || 0} avis)</td>
                                        <td>
                                            <button class="btn btn-sm btn-danger btn-action" onclick="deleteDriver(${driver.id})">
                                                <i class="fas fa-trash"></i>
                                            </button>
                                            <button class="btn btn-sm btn-info btn-action" onclick="viewDriverReviews(${driver.id}, '${driver.fullName}')">
                                                <i class="fas fa-star"></i> Voir avis
                                            </button>
                                        </td>
                                    </tr>
                                `;
                            }).join('')}
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
    if (!content) return;

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
                                <th>Téléphone</th>
                                <th>Départ</th>
                                <th>Destination</th>
                                <th>Distance</th>
                                <th>Prix</th>
                                <th>Statut</th>
                                <th>Changer statut</th>
                                <th>Motif d'annulation</th>
                                <th>Chauffeur</th>
                                <th>Note</th>
                                <th>Date</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${rides.map(ride => {
                                let statusBadge = '';
                                let cancelReason = '';
                                let statusOptions = '';
                                let ratingDisplay = '';

                                const status = ride.status || 'PENDING';

                                switch(status) {
                                    case 'PENDING':
                                        statusBadge = '<span class="badge bg-warning">En attente</span>';
                                        statusOptions = `
                                            <select class="form-select form-select-sm" onchange="updateRideStatus(${ride.id}, this.value)">
                                                <option value="">Changer...</option>
                                                <option value="ACCEPTED">✅ Accepter</option>
                                                <option value="CANCELLED">❌ Annuler</option>
                                            </select>
                                        `;
                                        break;
                                    case 'ACCEPTED':
                                        statusBadge = '<span class="badge bg-info">Acceptée</span>';
                                        statusOptions = `
                                            <select class="form-select form-select-sm" onchange="updateRideStatus(${ride.id}, this.value)">
                                                <option value="">Changer...</option>
                                                <option value="STARTED">🚖 Démarrer</option>
                                                <option value="CANCELLED">❌ Annuler</option>
                                            </select>
                                        `;
                                        break;
                                    case 'STARTED':
                                        statusBadge = '<span class="badge bg-primary">En cours</span>';
                                        statusOptions = `
                                            <select class="form-select form-select-sm" onchange="updateRideStatus(${ride.id}, this.value)">
                                                <option value="">Changer...</option>
                                                <option value="COMPLETED">🏁 Terminer</option>
                                                <option value="CANCELLED">❌ Annuler</option>
                                            </select>
                                        `;
                                        break;
                                    case 'COMPLETED':
                                        statusBadge = '<span class="badge bg-success">Terminée</span>';
                                        statusOptions = '<span class="text-muted">✓ Finalisée</span>';
                                        ratingDisplay = '<button class="btn btn-sm btn-outline-warning" onclick="viewRideReview(' + ride.id + ')">⭐ Voir avis</button>';
                                        break;
                                    case 'CANCELLED':
                                        statusBadge = '<span class="badge bg-danger">Annulée</span>';
                                        cancelReason = ride.cancellationReason || '-';
                                        statusOptions = '<span class="text-muted">✗ Annulée</span>';
                                        break;
                                    default:
                                        statusBadge = '<span class="badge bg-secondary">' + status + '</span>';
                                        statusOptions = '';
                                }

                                return `
                                    <tr>
                                        <td>#${ride.id}</td>
                                        <td>${ride.clientName || '-'}</td>
                                        <td>${ride.clientPhone || '-'}</td>
                                        <td>${ride.pickupAddress || '-'}</td>
                                        <td>${ride.destinationAddress || '-'}</td>
                                        <td>${ride.distance || 0} km</td>
                                        <td>${ride.estimatedPrice || 0} FCFA</td>
                                        <td>${statusBadge}</td>
                                        <td>${statusOptions}</td>
                                        <td><small class="text-danger">${cancelReason}</small></td>
                                        <td>${ride.driverName || '-'}</td>
                                        <td>${ratingDisplay}</td>
                                        <td>${ride.createdAt ? new Date(ride.createdAt).toLocaleString() : '-'}</td>
                                        <td>
                                            <button class="btn btn-sm btn-danger btn-action" onclick="deleteRide(${ride.id})">
                                                <i class="fas fa-trash"></i>
                                            </button>
                                        </td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;

    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// ==================== AVIS ====================
async function loadReviews() {
    const content = document.getElementById('content');
    if (!content) return;

    try {
        const response = await fetch(`${API_BASE_URL_TAXI}/reviews/all`);
        const reviews = await response.json();

        if (!reviews || !reviews.length) {
            content.innerHTML = `<div class="alert alert-info">📭 Aucun avis pour le moment</div>`;
            return;
        }

        content.innerHTML = `
            <div class="table-container">
                <h5 class="mb-3">⭐ Tous les avis clients</h5>
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Course ID</th>
                                <th>Chauffeur</th>
                                <th>Note</th>
                                <th>Commentaire</th>
                                <th>Date</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${reviews.map(review => {
                                const stars = '⭐'.repeat(review.rating || 0);
                                return `
                                    <tr>
                                        <td>${review.id}</td>
                                        <td>#${review.rideId}</td>
                                        <td>${review.driverName || `Chauffeur #${review.driverId}`}</td>
                                        <td>${stars} (${review.rating || 0}/5)</td>
                                        <td><em>${review.comment || '-'}</em></td>
                                        <td>${review.createdAt ? new Date(review.createdAt).toLocaleString() : '-'}</td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

// Voir les avis d'un chauffeur spécifique
async function viewDriverReviews(driverId, driverName) {
    try {
        const response = await fetch(`${API_BASE_URL_TAXI}/reviews/driver/${driverId}`);
        const reviews = await response.json();

        if (!reviews || !reviews.length) {
            alert(`Aucun avis pour ${driverName}`);
            return;
        }

        let message = `📋 Avis pour ${driverName}:\n\n`;
        reviews.forEach(r => {
            const stars = '⭐'.repeat(r.rating || 0);
            message += `${stars} (${r.rating || 0}/5) - "${r.comment || 'Pas de commentaire'}"\n`;
        });
        alert(message);
    } catch (error) {
        alert('Erreur: ' + error.message);
    }
}

// Voir l'avis d'une course spécifique
async function viewRideReview(rideId) {
    try {
        const response = await fetch(`${API_BASE_URL_TAXI}/reviews/ride/${rideId}`);
        const review = await response.json();

        if (!review || !review.id) {
            alert(`Aucun avis pour la course #${rideId}`);
            return;
        }

        const stars = '⭐'.repeat(review.rating || 0);
        alert(`⭐ Course #${rideId}\nNote: ${stars} (${review.rating || 0}/5)\nCommentaire: "${review.comment || 'Pas de commentaire'}"`);
    } catch (error) {
        alert('Aucun avis pour cette course');
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
    if (!status) return;

    try {
        const response = await fetch(`${API_BASE_URL}/ride/${rideId}/status?status=${status}`, { method: 'PUT' });
        if (response.ok) {
            alert(`✅ Statut changé en ${status}`);
            loadRides();
        } else {
            alert('❌ Erreur lors du changement de statut');
        }
    } catch (error) {
        alert('Erreur: ' + error.message);
        loadRides();
    }
}

// ==================== CAMPAGNES PUSH ====================
function loadCampaigns() {
    const content = document.getElementById('content');
    if (!content) return;

    content.innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <div class="table-container">
                    <h5 class="mb-3">📢 Envoyer une notification</h5>
                    <div class="card">
                        <div class="card-body">
                            <div class="mb-3">
                                <label class="form-label">Cibler</label>
                                <select class="form-select" id="campaignTarget">
                                    <option value="ALL">📱 Tous les utilisateurs</option>
                                    <option value="CLIENTS">👥 Clients uniquement</option>
                                    <option value="DRIVERS">🚖 Chauffeurs uniquement</option>
                                </select>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Titre</label>
                                <input type="text" class="form-control" id="campaignTitle" placeholder="Ex: Promo exceptionnelle!">
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Message</label>
                                <textarea class="form-control" id="campaignBody" rows="4" placeholder="Votre message ici..."></textarea>
                            </div>
                            <div class="mb-3">
                                <label class="form-label">Image (optionnelle)</label>
                                <input type="url" class="form-control" id="campaignImage" placeholder="https://...">
                            </div>
                            <button class="btn btn-primary w-100" onclick="sendCampaign()">
                                <i class="fas fa-paper-plane"></i> Envoyer la campagne
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="table-container">
                    <h5 class="mb-3">📜 Historique des campagnes</h5>
                    <div id="campaignHistory">
                        <div class="text-center text-muted">Chargement...</div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-4">
            <div class="col-md-12">
                <div class="table-container">
                    <h5 class="mb-3">📊 Statistiques</h5>
                    <div class="row">
                        <div class="col-md-4">
                            <div class="stat-card text-center">
                                <i class="fas fa-bell"></i>
                                <h3 id="statTotalSent">0</h3>
                                <p>Notifications envoyées</p>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="stat-card text-center">
                                <i class="fas fa-check-circle"></i>
                                <h3 id="statDelivered">0</h3>
                                <p>Taux de délivrance</p>
                            </div>
                        </div>
                        <div class="col-md-4">
                            <div class="stat-card text-center">
                                <i class="fas fa-chart-line"></i>
                                <h3 id="statEngagement">0%</h3>
                                <p>Taux d'engagement</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    loadCampaignHistory();
    loadNotificationStats();
}

async function sendCampaign() {
    const target = document.getElementById('campaignTarget').value;
    const title = document.getElementById('campaignTitle').value;
    const body = document.getElementById('campaignBody').value;
    const image = document.getElementById('campaignImage').value;

    if (!title || !body) {
        alert('Veuillez remplir le titre et le message');
        return;
    }

    const targetLabels = {
        'ALL': 'tous les utilisateurs',
        'CLIENTS': 'tous les clients',
        'DRIVERS': 'tous les chauffeurs'
    };

    if (!confirm(`Envoyer "${title}" à ${targetLabels[target]} ?`)) return;

    const btn = event.target;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Envoi...';
    btn.disabled = true;

    try {
        const response = await fetch(`${API_NOTIFICATIONS_URL}/campaign/send`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ target, title, body, image })
        });

        if (response.ok) {
            alert(`✅ Campagne envoyée avec succès à ${targetLabels[target]}!`);
            document.getElementById('campaignTitle').value = '';
            document.getElementById('campaignBody').value = '';
            document.getElementById('campaignImage').value = '';
            loadCampaignHistory();
            loadNotificationStats();
        } else {
            const error = await response.text();
            alert('❌ Erreur lors de l\'envoi: ' + error);
        }
    } catch (error) {
        alert('Erreur: ' + error.message);
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

async function loadCampaignHistory() {
    try {
        const response = await fetch(`${API_NOTIFICATIONS_URL}/campaigns/history`);
        const campaigns = await response.json();

        const historyDiv = document.getElementById('campaignHistory');
        if (!historyDiv) return;

        if (!campaigns || !campaigns.length) {
            historyDiv.innerHTML = '<div class="text-center text-muted">Aucune campagne envoyée</div>';
            return;
        }

        historyDiv.innerHTML = campaigns.map(c => `
            <div class="card mb-2">
                <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <strong>${c.title || 'Sans titre'}</strong><br>
                            <small class="text-muted">${c.body ? (c.body.substring(0, 100) + (c.body.length > 100 ? '...' : '')) : '-'}</small><br>
                            <small class="text-muted">
                                <i class="fas fa-users"></i> ${c.target || 'ALL'}
                                | <i class="fas fa-clock"></i> ${c.sentAt ? new Date(c.sentAt).toLocaleString() : '-'}
                            </small>
                        </div>
                        <span class="badge bg-success">${c.sentCount || 'Envoyée'}</span>
                    </div>
                </div>
            </div>
        `).join('');

    } catch (error) {
        console.error('Erreur chargement historique:', error);
        const historyDiv = document.getElementById('campaignHistory');
        if (historyDiv) {
            historyDiv.innerHTML = '<div class="alert alert-warning">Erreur chargement historique</div>';
        }
    }
}

async function loadNotificationStats() {
    try {
        const response = await fetch(`${API_NOTIFICATIONS_URL}/stats`);
        const stats = await response.json();

        const totalSentEl = document.getElementById('statTotalSent');
        const deliveredEl = document.getElementById('statDelivered');
        const engagementEl = document.getElementById('statEngagement');

        if (totalSentEl) totalSentEl.textContent = stats.totalSent || 0;
        if (deliveredEl) deliveredEl.textContent = stats.deliveryRate || '0%';
        if (engagementEl) engagementEl.textContent = stats.engagementRate || '0%';
    } catch (error) {
        console.error('Erreur stats:', error);
    }
}

// ==================== GESTION DES VERSIONS ====================
async function loadVersionManagement() {
    const content = document.getElementById('content');

    let clientVersion = 2;
    let driverVersion = 2;

    try {
        const response = await fetch(`${API_BASE_URL}/version/current`);
        if (response.ok) {
            const data = await response.json();
            clientVersion = data.clientVersion || 2;
            driverVersion = data.driverVersion || 2;
        }
    } catch (error) {
        console.error('Erreur chargement versions:', error);
    }

    content.innerHTML = `
        <div class="table-container">
            <h5 class="mb-3">📱 Gestion des versions</h5>
            <div class="card">
                <div class="card-body">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i>
                        <strong>Comment ça fonctionne ?</strong><br>
                        - Augmentez le numéro de version pour forcer les utilisateurs à mettre à jour l'application<br>
                        - Les utilisateurs avec une version inférieure verront un message de mise à jour obligatoire<br>
                        - Pensez à augmenter le <code>versionCode</code> dans le <code>build.gradle</code> des applications
                    </div>

                    <div class="mb-3">
                        <label class="form-label">📱 Version minimale CLIENT</label>
                        <div class="input-group">
                            <input type="number" class="form-control" id="clientVersion" value="${clientVersion}" min="1" step="1">
                            <button class="btn btn-warning" onclick="testClientVersion()">
                                <i class="fas fa-mobile-alt"></i> Tester
                            </button>
                        </div>
                        <small class="text-muted">Les clients avec version < ${clientVersion} seront bloqués</small>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">🚖 Version minimale CHAUFFEUR</label>
                        <div class="input-group">
                            <input type="number" class="form-control" id="driverVersion" value="${driverVersion}" min="1" step="1">
                            <button class="btn btn-warning" onclick="testDriverVersion()">
                                <i class="fas fa-car"></i> Tester
                            </button>
                        </div>
                        <small class="text-muted">Les chauffeurs avec version < ${driverVersion} seront bloqués</small>
                    </div>

                    <button class="btn btn-primary w-100" onclick="updateVersions()">
                        <i class="fas fa-save"></i> Enregistrer les modifications
                    </button>
                </div>
            </div>
        </div>

        <div class="table-container mt-4">
            <h5 class="mb-3">📋 Procédure de mise à jour</h5>
            <div class="card">
                <div class="card-body">
                    <ol>
                        <li><strong>Augmentez le versionCode</strong> dans le fichier <code>build.gradle</code> de l'application (client et/ou chauffeur)</li>
                        <li><strong>Augmentez le numéro</strong> ci-dessus dans cette interface admin</li>
                        <li><strong>Cliquez sur "Enregistrer"</strong> pour appliquer le changement</li>
                        <li>Les utilisateurs avec une ancienne version verront un message de mise à jour forcée</li>
                    </ol>
                </div>
            </div>
        </div>
    `;
}

async function testClientVersion() {
    try {
        const response = await fetch(`${API_BASE_URL}/version/client`);
        const data = await response.json();
        alert(`📱 Version minimale requise: ${data.minVersionCode}\nMessage: ${data.message}\nForce update: ${data.forceUpdate}`);
    } catch (error) {
        alert('Erreur: ' + error.message);
    }
}

async function testDriverVersion() {
    try {
        const response = await fetch(`${API_BASE_URL}/version/driver`);
        const data = await response.json();
        alert(`🚖 Version minimale requise: ${data.minVersionCode}\nMessage: ${data.message}\nForce update: ${data.forceUpdate}`);
    } catch (error) {
        alert('Erreur: ' + error.message);
    }
}

async function updateVersions() {
    const clientVersion = document.getElementById('clientVersion').value;
    const driverVersion = document.getElementById('driverVersion').value;

    if (!clientVersion || !driverVersion) {
        alert('Veuillez remplir les deux champs');
        return;
    }

    if (!confirm(`⚠️ Attention !\n\nVersion minimale Client: ${clientVersion}\nVersion minimale Chauffeur: ${driverVersion}\n\nLes utilisateurs avec une version inférieure ne pourront plus utiliser l'application.\n\nContinuer ?`)) {
        return;
    }

    const btn = event.target;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Mise à jour...';
    btn.disabled = true;

    try {
        const response = await fetch(`${API_BASE_URL}/version/update?clientVersion=${clientVersion}&driverVersion=${driverVersion}`, {
            method: 'POST'
        });

        if (response.ok) {
            const data = await response.json();
            alert(`✅ ${data.message}\n\nClient: v${data.clientVersion}\nChauffeur: v${data.driverVersion}`);
            loadVersionManagement();
        } else {
            alert('❌ Erreur lors de la mise à jour');
        }
    } catch (error) {
        alert('Erreur: ' + error.message);
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

// ==================== CARTE THERMIQUE ADMIN (SIMPLIFIÉE) ====================
async function loadHeatmapAdmin() {
    const content = document.getElementById('content');

    content.innerHTML = `
        <div class="row">
            <div class="col-md-12">
                <div class="card">
                    <div class="card-header bg-primary text-white">
                        <i class="fas fa-fire"></i> Carte thermique des demandes
                    </div>
                    <div class="card-body">
                        <div id="heatmapMap" style="height: 500px; background: #e9ecef; display: flex; align-items: center; justify-content: center;">
                            <div class="text-center">
                                <i class="fas fa-map-marked-alt fa-3x text-muted"></i>
                                <p class="mt-2">Fonctionnalité en cours de développement</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// ==================== COURSES PROGRAMMÉES ====================
async function loadScheduledRides() {
    const content = document.getElementById('content');

    try {
        const response = await fetch(`${API_BASE_URL_TAXI}/scheduled/admin/all`);
        const rides = await response.json();

        // Vérifier que rides est un tableau
        if (!Array.isArray(rides) || rides.length === 0) {
            content.innerHTML = `
                <div class="table-container">
                    <h5 class="mb-3">📅 Courses programmées</h5>
                    <div class="alert alert-info">Aucune course programmée pour le moment</div>
                </div>
            `;
            return;
        }

        content.innerHTML = `
            <div class="table-container">
                <h5 class="mb-3">📅 Courses programmées</h5>
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Client</th>
                                <th>Téléphone</th>
                                <th>Départ</th>
                                <th>Destination</th>
                                <th>Date/Heure</th>
                                <th>Prix</th>
                                <th>Statut</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${rides.map(ride => {
                                let statusBadge = '';
                                switch(ride.status) {
                                    case 'PENDING':
                                        statusBadge = '<span class="badge bg-warning">En attente</span>';
                                        break;
                                    case 'PROCESSED':
                                        statusBadge = '<span class="badge bg-success">Traitée</span>';
                                        break;
                                    case 'FAILED':
                                        statusBadge = '<span class="badge bg-danger">Échec</span>';
                                        break;
                                    default:
                                        statusBadge = '<span class="badge bg-secondary">' + ride.status + '</span>';
                                }
                                return `
                                    </tr>
                                        <td>#${ride.id}</td>
                                        <td>${ride.clientName || '-'}</td>
                                        <td>${ride.clientPhone || '-'}</td>
                                        <td>${ride.pickupAddress || '-'}</td>
                                        <td>${ride.destinationAddress || '-'}</td>
                                        <td>${new Date(ride.scheduledDateTime).toLocaleString()}</td>
                                        <td>${ride.estimatedPrice || 0} FCFA</td>
                                        <td>${statusBadge}</td>
                                        <td>
                                            <button class="btn btn-sm btn-danger" onclick="deleteScheduledRide(${ride.id})">
                                                <i class="fas fa-trash"></i>
                                            </button>
                                        </td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

async function deleteScheduledRide(id) {
    if (confirm('Supprimer cette course programmée ?')) {
        try {
            await fetch(`${API_BASE_URL_TAXI}/scheduled/${id}`, { method: 'DELETE' });
            loadScheduledRides();
            alert('Course programmée supprimée');
        } catch (error) {
            alert('Erreur: ' + error.message);
        }
    }
}

// ==================== GESTION DES LITIGES ====================
async function loadDisputes() {
    const content = document.getElementById('content');

    try {
        const response = await fetch(`${API_BASE_URL}/disputes/all`);
        const disputes = await response.json();

        // ✅ S'assurer que disputes est bien un tableau
        if (!Array.isArray(disputes) || disputes.length === 0) {
            content.innerHTML = `
                <div class="table-container">
                    <h5 class="mb-3">⚖️ Gestion des litiges</h5>
                    <div class="alert alert-info">Aucun litige pour le moment</div>
                </div>
            `;
            return;
        }

        content.innerHTML = `
            <div class="table-container">
                <h5 class="mb-3">⚖️ Gestion des litiges</h5>
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Course ID</th>
                                <th>Client</th>
                                <th>Chauffeur</th>
                                <th>Motif</th>
                                <th>Statut</th>
                                <th>Date</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${disputes.map(dispute => {
                                let statusBadge = '';
                                switch(dispute.status) {
                                    case 'PENDING':
                                        statusBadge = '<span class="badge bg-warning">En attente</span>';
                                        break;
                                    case 'RESOLVED':
                                        statusBadge = '<span class="badge bg-success">Résolu</span>';
                                        break;
                                    case 'REJECTED':
                                        statusBadge = '<span class="badge bg-danger">Rejeté</span>';
                                        break;
                                }
                                return `
                                    <tr>
                                        <td>#${dispute.id}</td>
                                        <td>#${dispute.rideId}</td>
                                        <td>Client #${dispute.clientId}</td>
                                        <td>Chauffeur #${dispute.driverId}</td>
                                        <td>${dispute.reason}</td>
                                        <td>${statusBadge}</td>
                                        <td>${new Date(dispute.createdAt).toLocaleString()}</td>
                                        <td>
                                            ${dispute.status === 'PENDING' ? `
                                                <button class="btn btn-sm btn-success" onclick="resolveDispute(${dispute.id})">
                                                    <i class="fas fa-check"></i> Résoudre
                                                </button>
                                                <button class="btn btn-sm btn-danger" onclick="rejectDispute(${dispute.id})">
                                                    <i class="fas fa-times"></i> Rejeter
                                                </button>
                                            ` : `
                                                <span class="text-muted">${dispute.resolution || 'Traité'}</span>
                                            `}
                                        </td>
                                    </tr>
                                `;
                            }).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur: ${error.message}</div>`;
    }
}

async function resolveDispute(id) {
    const resolution = prompt("Solution apportée au litige :");
    if (!resolution) return;

    const refundAmount = parseFloat(prompt("Montant du remboursement (0 si aucun) :", "0"));
    if (isNaN(refundAmount)) return;

    try {
        const response = await fetch(`${API_BASE_URL}/disputes/resolve/${id}?resolution=${encodeURIComponent(resolution)}&refundAmount=${refundAmount}`, {
            method: 'PUT'
        });

        if (response.ok) {
            alert('✅ Litige résolu avec succès');
            loadDisputes();
        } else {
            alert('❌ Erreur lors de la résolution');
        }
    } catch (error) {
        alert('Erreur: ' + error.message);
    }
}

async function rejectDispute(id) {
    const reason = prompt("Raison du rejet :");
    if (!reason) return;

    try {
        const response = await fetch(`${API_BASE_URL}/disputes/reject/${id}?reason=${encodeURIComponent(reason)}`, {
            method: 'PUT'
        });

        if (response.ok) {
            alert('✅ Litige rejeté');
            loadDisputes();
        } else {
            alert('❌ Erreur lors du rejet');
        }
    } catch (error) {
        alert('Erreur: ' + error.message);
    }
}

// ==================== TAXI PUB (ADVERTISING) ====================
async function loadAdvertising() {
    const content = document.getElementById('content');
    if (!content) return;

    content.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i><p>Chargement...</p></div>';

    try {
        const response = await fetch(`${API_BASE_URL}/advertising/admin/all`);
        let data = await response.json();

        // ✅ CORRECTION : s'assurer que 'ads' est un tableau
        let ads = Array.isArray(data) ? data : (data.data || data.content || data.advertisements || []);

        if (!ads || ads.length === 0) {
            content.innerHTML = `
                <div class="table-container">
                    <h5 class="mb-3">🚀 Demandes de publicité</h5>
                    <div class="alert alert-info">Aucune demande de publicité pour le moment.</div>
                </div>
            `;
            return;
        }

        content.innerHTML = `
            <div class="table-container">
                <h5 class="mb-3"><i class="fas fa-bullhorn"></i> Demandes de publicité (Taxi Pub)</h5>
                <div class="table-responsive">
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Client</th>
                                <th>Produit / Service</th>
                                <th>Durée</th>
                                <th>Prix</th>
                                <th>Paiement</th>
                                <th>Statut</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${ads.map(ad => `
                                <tr>
                                    <td>#${ad.id}</td>
                                    <td>${ad.clientName || '-'}</td>
                                    <td><strong>${ad.productName || '-'}</strong><br><small>${ad.description || ''}</small></td>
                                    <td>${ad.duration}</td>
                                    <td>${ad.price.toLocaleString()} FCFA</td>
                                    <td>${ad.paymentMethod === 'CASH' ? '💵 Espèces' : '💰 Porte-monnaie'}</td>
                                    <td><span class="badge bg-${getAdStatusClass(ad.status)}">${getAdStatusLabel(ad.status)}</span></td>
                                    <td>
                                        ${ad.paymentMethod === 'CASH' && ad.status === 'PENDING_ADMIN'
                                            ? `<button class="btn btn-sm btn-success" onclick="validateCashAdvertising(${ad.id})">
                                                    💰 Valider paiement espèces
                                               </button>`
                                            : ad.status === 'PAID' || ad.status === 'VALIDATED_BY_ADMIN'
                                            ? `<span class="text-success">✓ Payé</span>`
                                            : `-`}
                                    </td>
                                </tr>
                            `).join('')}
                        </tbody>
                    </table>
                </div>
            </div>
        `;
    } catch (error) {
        content.innerHTML = `<div class="alert alert-danger">Erreur chargement: ${error.message}</div>`;
    }
}

function getAdStatusClass(status) {
    switch(status) {
        case 'PENDING': return 'warning';
        case 'PAID': return 'info';
        case 'PENDING_ADMIN': return 'warning';
        case 'VALIDATED_BY_ADMIN': return 'success';
        default: return 'secondary';
    }
}

function getAdStatusLabel(status) {
    switch(status) {
        case 'PENDING': return 'En attente de paiement';
        case 'PAID': return 'Payé (wallet)';
        case 'PENDING_ADMIN': return 'En attente validation admin (espèces)';
        case 'VALIDATED_BY_ADMIN': return 'Validé par admin';
        default: return status;
    }
}

async function validateCashAdvertising(adId) {
    const notes = prompt("📝 Notes internes (optionnel) :");
    if (notes === null) return; // annulé

    try {
        const response = await fetch(`${API_BASE_URL}/advertising/admin/validate/${adId}?adminNotes=${encodeURIComponent(notes)}`, {
            method: 'PUT'
        });
        if (response.ok) {
            alert('✅ Paiement espèces validé. La publicité sera imprimée et posée sur les taxis.');
            loadAdvertising(); // recharge la liste
        } else {
            const err = await response.text();
            alert('❌ Erreur lors de la validation : ' + err);
        }
    } catch (error) {
        alert('Erreur réseau : ' + error.message);
    }
}