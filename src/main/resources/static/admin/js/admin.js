// Configuration
const API_BASE_URL = 'https://abdil-taxi-backend.onrender.com/api/admin';
const API_BASE_URL_TAXI = 'https://abdil-taxi-backend.onrender.com/api/taxi';
const API_NOTIFICATIONS_URL = 'https://abdil-taxi-backend.onrender.com/api/notifications';

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
        case 'gps':
            loadGPSPage();
            break;
        case 'licenses':
            loadLicenses();
            break;
        default:
            loadDashboard();
            break;
    }
}

// ==================== GPS SUIVI AVEC ZOOM PERSISTANT ====================
let gpsMap;
let gpsMarkers = [];
let gpsRefreshInterval;
let gpsUserInteracted = false;
let gpsResetTimeout;

function loadGPSPage() {
    const content = document.getElementById('content');
    if (!content) return;

    content.innerHTML = `
        <div class="row">
            <div class="col-md-4">
                <div class="card gps-card">
                    <div class="card-header bg-primary text-white">
                        <i class="fas fa-taxi"></i> Chauffeurs en ligne
                    </div>
                    <div class="card-body" id="driversList" style="max-height: 500px; overflow-y: auto;">
                        <div class="text-center">Chargement des chauffeurs...</div>
                    </div>
                </div>
            </div>
            <div class="col-md-8">
                <div class="card gps-card">
                    <div class="card-header bg-success text-white">
                        <i class="fas fa-map"></i> Carte des chauffeurs
                    </div>
                    <div class="card-body">
                        <div id="map" style="height: 500px; width: 100%;"></div>
                        <div id="legend" class="mt-2">
                            <span class="status-online">● En ligne</span>
                            <span class="status-busy">● En course</span>
                            <span class="status-offline">● Hors ligne</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    initGPSMap();
}

function initGPSMap() {
    if (typeof google === 'undefined' || !google.maps) {
        console.log('Google Maps pas encore chargé');
        setTimeout(initGPSMap, 500);
        return;
    }

    gpsMap = new google.maps.Map(document.getElementById('map'), {
        center: { lat: 33.5731, lng: -7.5898 },
        zoom: 12,
        styles: [
            { elementType: "geometry", stylers: [{ color: "#0a0e27" }] },
            { elementType: "labels.text.stroke", stylers: [{ color: "#0a0e27" }] },
            { elementType: "labels.text.fill", stylers: [{ color: "#00ffff" }] },
            { featureType: "road", elementType: "geometry", stylers: [{ color: "#1a1a3a" }] },
            { featureType: "road", elementType: "labels.text.fill", stylers: [{ color: "#ffffff" }] }
        ]
    });

    google.maps.event.addListener(gpsMap, 'zoom_changed', () => {
        gpsUserInteracted = true;
        clearTimeout(gpsResetTimeout);
        gpsResetTimeout = setTimeout(() => {
            gpsUserInteracted = false;
        }, 5000);
    });

    google.maps.event.addListener(gpsMap, 'dragend', () => {
        gpsUserInteracted = true;
        clearTimeout(gpsResetTimeout);
        gpsResetTimeout = setTimeout(() => {
            gpsUserInteracted = false;
        }, 5000);
    });

    loadGPSDrivers();
    startGPSAutoRefresh();
}

async function loadGPSDrivers() {
    try {
        const response = await fetch(`${API_BASE_URL}/drivers`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const drivers = await response.json();
        console.log('Chauffeurs reçus:', drivers);
        updateGPSDriversList(drivers);
        updateGPSMap(drivers);
    } catch (error) {
        console.error('Erreur:', error);
        const driversList = document.getElementById('driversList');
        if (driversList) {
            driversList.innerHTML = '<div class="text-center text-danger">Erreur de chargement: ' + error.message + '</div>';
        }
    }
}

function updateGPSDriversList(drivers) {
    const driversList = document.getElementById('driversList');
    if (!driversList) return;

    if (!drivers || drivers.length === 0) {
        driversList.innerHTML = '<div class="text-center">Aucun chauffeur enregistré</div>';
        return;
    }

    driversList.innerHTML = drivers.map(driver => {
        let statusClass = '';
        let statusText = '';
        if (driver.status === 'ONLINE') {
            statusClass = 'status-online';
            statusText = 'En ligne';
        } else if (driver.status === 'ON_TRIP') {
            statusClass = 'status-busy';
            statusText = 'En course';
        } else {
            statusClass = 'status-offline';
            statusText = 'Hors ligne';
        }

        const hasLocation = driver.latitude && driver.longitude && driver.latitude !== 0 && driver.longitude !== 0;
        const locationInfo = hasLocation ?
            `<div class="driver-location"><i class="fas fa-map-marker-alt"></i> ${driver.latitude.toFixed(4)}, ${driver.longitude.toFixed(4)}</div>` :
            `<div class="driver-location text-muted"><i class="fas fa-exclamation-triangle"></i> Position non disponible</div>`;

        return `
            <div class="driver-card card mb-2 driver-${driver.status}" onclick="centerOnDriverGPS(${driver.latitude || 33.5731}, ${driver.longitude || -7.5898})">
                <div class="card-body p-2">
                    <div class="d-flex justify-content-between align-items-start">
                        <div>
                            <strong><i class="fas fa-user"></i> ${driver.fullName}</strong><br>
                            <small><i class="fas fa-car"></i> ${driver.vehicleType || 'Non spécifié'} - ${driver.licensePlate || 'Non spécifié'}</small><br>
                            <small><i class="fas fa-phone"></i> ${driver.phone}</small>
                            ${locationInfo}
                        </div>
                        <div>
                            <span class="${statusClass}">${statusText}</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

function updateGPSMap(drivers) {
    if (!gpsMap) return;

    gpsMarkers.forEach(marker => marker.setMap(null));
    gpsMarkers = [];

    let hasValidMarkers = false;

    drivers.forEach(driver => {
        if (driver.latitude && driver.longitude && driver.latitude !== 0 && driver.longitude !== 0) {
            hasValidMarkers = true;
            let iconColor = '#4CAF50';
            if (driver.status === 'ON_TRIP') iconColor = '#FF9800';
            if (driver.status === 'OFFLINE') iconColor = '#9E9E9E';

            const marker = new google.maps.Marker({
                position: { lat: driver.latitude, lng: driver.longitude },
                map: gpsMap,
                title: driver.fullName,
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    fillColor: iconColor,
                    fillOpacity: 0.9,
                    scale: 12,
                    strokeColor: '#ffffff',
                    strokeWeight: 2
                }
            });

            const infoWindow = new google.maps.InfoWindow({
                content: `
                    <div style="font-family: Arial, sans-serif; min-width: 200px; background: #0a0e27; color: #00ffff; padding: 8px; border-radius: 8px;">
                        <strong><i class="fas fa-user"></i> ${driver.fullName}</strong><br>
                        <i class="fas fa-car"></i> ${driver.vehicleType || 'Non spécifié'}<br>
                        <i class="fas fa-tag"></i> ${driver.licensePlate || 'Non spécifié'}<br>
                        <i class="fas fa-phone"></i> ${driver.phone}<br>
                        <span class="${driver.status === 'ONLINE' ? 'status-online' : driver.status === 'ON_TRIP' ? 'status-busy' : 'status-offline'}" style="display: inline-block; margin-top: 8px;">
                            ${driver.status === 'ONLINE' ? 'En ligne' : driver.status === 'ON_TRIP' ? 'En course' : 'Hors ligne'}
                        </span>
                    </div>
                `
            });

            marker.addListener('click', () => {
                infoWindow.open(gpsMap, marker);
            });

            gpsMarkers.push(marker);
        }
    });

    if (!gpsUserInteracted && hasValidMarkers && gpsMarkers.length > 0) {
        const bounds = new google.maps.LatLngBounds();
        gpsMarkers.forEach(marker => bounds.extend(marker.getPosition()));
        gpsMap.fitBounds(bounds);
    }
}

function centerOnDriverGPS(lat, lng) {
    if (gpsMap && lat && lng && lat !== 0 && lng !== 0) {
        gpsMap.setCenter({ lat: lat, lng: lng });
        gpsMap.setZoom(15);
    }
}

function startGPSAutoRefresh() {
    if (gpsRefreshInterval) clearInterval(gpsRefreshInterval);
    gpsRefreshInterval = setInterval(() => {
        loadGPSDrivers();
    }, 10000);
}

function refreshGPS() {
    loadGPSDrivers();
}

window.centerOnDriverGPS = centerOnDriverGPS;
window.refreshGPS = refreshGPS;
window.loadGPSPage = loadGPSPage;

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
                                    </tr>
                                </thead>
                                <tbody>
                                    ${rides.slice(0, 5).map(ride => `
                                        <tr>
                                            <td>#${ride.id}</td>
                                            <td>${ride.clientName || '-'}</td>
                                            <td>${ride.pickupAddress || '-'}</td>
                                            <td>${ride.destinationAddress || '-'}</td>
                                            <td>${ride.estimatedPrice || 0} FCFA\n
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
                                        <td>${ride.distance || 0} km\n
                                        <td>${ride.estimatedPrice || 0} FCFA\n
                                        <td>${statusBadge}\n
                                        <td>${statusOptions}\n
                                        <td><small class="text-danger">${cancelReason}</small>\n
                                        <td>${ride.driverName || '-'}\n
                                        <td>${ratingDisplay}\n
                                        <td>${ride.createdAt ? new Date(ride.createdAt).toLocaleString() : '-'}\n
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

// ==================== CARTE DES ZONES DE DEMANDE ====================
let demandMap;
let demandRefreshInterval;

async function loadHeatmapAdmin() {
    const content = document.getElementById('content');

    content.innerHTML = `
        <div class="row">
            <div class="col-md-12">
                <div class="card">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
                        <div>
                            <i class="fas fa-chart-line"></i> Zones de forte demande clients
                        </div>
                        <div>
                            <button class="btn btn-sm btn-light" onclick="refreshDemandMap()">
                                <i class="fas fa-sync-alt"></i> Rafraîchir
                            </button>
                        </div>
                    </div>
                    <div class="card-body">
                        <div id="demandMap" style="height: 550px; width: 100%;"></div>
                        <div class="mt-3">
                            <div class="alert alert-info">
                                <i class="fas fa-info-circle"></i>
                                <strong>Carte des zones de demande</strong> - Les zones sont classées par niveau d'activité :
                                <strong>🟢 Faible</strong> | <strong>🟡 Moyenne</strong> | <strong>🟠 Élevée</strong> | <strong>🔴 Très élevée</strong>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;

    initDemandMap();
    startDemandAutoRefresh();
}

function initDemandMap() {
    if (typeof google === 'undefined' || !google.maps) {
        console.log('Google Maps pas encore chargé, attente...');
        setTimeout(initDemandMap, 500);
        return;
    }

    const mapDiv = document.getElementById('demandMap');
    if (!mapDiv) return;

    demandMap = new google.maps.Map(mapDiv, {
        center: { lat: 33.5731, lng: -7.5898 },
        zoom: 12,
        styles: [
            { elementType: "geometry", stylers: [{ color: "#0a0e27" }] },
            { elementType: "labels.text.stroke", stylers: [{ color: "#0a0e27" }] },
            { elementType: "labels.text.fill", stylers: [{ color: "#00ffff" }] },
            { featureType: "road", elementType: "geometry", stylers: [{ color: "#1a1a3a" }] },
            { featureType: "road", elementType: "labels.text.fill", stylers: [{ color: "#ffffff" }] }
        ]
    });

    loadDemandData();
}

async function loadDemandData() {
    try {
        const response = await fetch(`${API_BASE_URL}/rides`);
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const rides = await response.json();

        const driversResponse = await fetch(`${API_BASE_URL}/drivers`);
        const drivers = await driversResponse.json();

        const zones = [
            { name: "Centre-ville (Bd Mohammed V)", lat: 33.5898, lng: -7.6122, baseActivity: 85 },
            { name: "Gare Casa-Port", lat: 33.5988, lng: -7.6189, baseActivity: 72 },
            { name: "Marché Central", lat: 33.5955, lng: -7.6205, baseActivity: 68 },
            { name: "Quartier Maârif", lat: 33.5731, lng: -7.6083, baseActivity: 65 },
            { name: "Aïn Diab (Corniche)", lat: 33.5864, lng: -7.7102, baseActivity: 45 },
            { name: "Anfa", lat: 33.5778, lng: -7.6419, baseActivity: 50 },
            { name: "Roches Noires", lat: 33.6125, lng: -7.5833, baseActivity: 40 },
            { name: "Hay Hassani", lat: 33.5583, lng: -7.5750, baseActivity: 48 },
            { name: "Aéroport Mohammed V", lat: 33.3675, lng: -7.5833, baseActivity: 35 },
            { name: "Université Hassan II", lat: 33.5458, lng: -7.5542, baseActivity: 42 }
        ];

        const oneDayAgo = new Date(Date.now() - 24 * 60 * 60 * 1000);
        const recentRides = rides.filter(ride =>
            ride.createdAt && new Date(ride.createdAt) > oneDayAgo
        );

        zones.forEach(zone => {
            let activityCount = 0;
            recentRides.forEach(ride => {
                const address = (ride.pickupAddress || '').toLowerCase();
                if (address.includes(zone.name.toLowerCase()) ||
                    (zone.name === "Centre-ville (Bd Mohammed V)" &&
                     (address.includes('centre') || address.includes('ville') || address.includes('mohammed v'))) ||
                    (zone.name === "Gare Casa-Port" && address.includes('gare'))) {
                    activityCount++;
                }
            });

            zone.realActivity = Math.min(100, zone.baseActivity + (activityCount * 5));
            zone.recentRideCount = activityCount;
        });

        zones.sort((a, b) => b.realActivity - a.realActivity);

        const totalRecent = recentRides.length;
        const alertDiv = document.querySelector('#demandMap').closest('.card').querySelector('.alert-info');
        if (alertDiv) {
            alertDiv.innerHTML = `
                <i class="fas fa-info-circle"></i>
                <strong>Carte des zones de demande</strong> - ${totalRecent} courses demandées dans les dernières 24h.<br>
                <strong>🟢 Faible</strong> | <strong>🟡 Moyenne</strong> | <strong>🟠 Élevée</strong> | <strong>🔴 Très élevée</strong>
            `;
        }

        zones.forEach(zone => {
            let color = '';
            let radius = 100;
            let activityLabel = '';

            if (zone.realActivity >= 70) {
                color = '#FF0000';
                activityLabel = '🔴 Très élevée';
                radius = 180;
            } else if (zone.realActivity >= 55) {
                color = '#FF6600';
                activityLabel = '🟠 Élevée';
                radius = 150;
            } else if (zone.realActivity >= 40) {
                color = '#FFCC00';
                activityLabel = '🟡 Moyenne';
                radius = 120;
            } else {
                color = '#33CC33';
                activityLabel = '🟢 Faible';
                radius = 90;
            }

            const circle = new google.maps.Circle({
                map: demandMap,
                center: { lat: zone.lat, lng: zone.lng },
                radius: radius,
                fillColor: color,
                fillOpacity: 0.4,
                strokeColor: color,
                strokeWeight: 2,
                strokeOpacity: 0.8
            });

            const marker = new google.maps.Marker({
                position: { lat: zone.lat, lng: zone.lng },
                map: demandMap,
                icon: {
                    path: google.maps.SymbolPath.CIRCLE,
                    fillColor: color,
                    fillOpacity: 0.9,
                    scale: 14,
                    strokeColor: '#ffffff',
                    strokeWeight: 2
                },
                label: {
                    text: '📍',
                    color: '#ffffff',
                    fontSize: '14px'
                }
            });

            const infoWindow = new google.maps.InfoWindow({
                content: `
                    <div style="font-family: Arial, sans-serif; min-width: 220px; background: #0a0e27; color: #00ffff; padding: 8px; border-radius: 8px;">
                        <strong><i class="fas fa-map-marker-alt"></i> ${zone.name}</strong><br>
                        <span style="color: ${color}">●</span> <strong>Activité: ${activityLabel}</strong> (${zone.realActivity}%)<br>
                        <i class="fas fa-calendar-alt"></i> ${zone.recentRideCount} courses dans les dernières 24h<br>
                        <i class="fas fa-chart-line"></i> Niveau de demande: ${zone.realActivity > 70 ? 'Excellent pour se positionner' : zone.realActivity > 55 ? 'Bon secteur' : 'Activité modérée'}
                    </div>
                `
            });

            marker.addListener('click', () => {
                infoWindow.open(demandMap, marker);
            });
        });

        const onlineDrivers = drivers.filter(d => d.status === 'ONLINE' || d.status === 'ON_TRIP');

        onlineDrivers.forEach(driver => {
            if (driver.latitude && driver.longitude && driver.latitude !== 0 && driver.longitude !== 0) {
                const marker = new google.maps.Marker({
                    position: { lat: driver.latitude, lng: driver.longitude },
                    map: demandMap,
                    title: driver.fullName,
                    icon: {
                        url: 'https://maps.google.com/mapfiles/ms/icons/yellow-dot.png',
                        scaledSize: new google.maps.Size(32, 32)
                    }
                });

                const infoWindow = new google.maps.InfoWindow({
                    content: `
                        <div style="font-family: Arial, sans-serif; min-width: 180px;">
                            <strong><i class="fas fa-user"></i> ${driver.fullName}</strong><br>
                            <i class="fas fa-car"></i> ${driver.vehicleType || 'Non spécifié'}<br>
                            <i class="fas fa-phone"></i> ${driver.phone}<br>
                            <span class="badge ${driver.status === 'ONLINE' ? 'bg-success' : 'bg-warning'}">
                                ${driver.status === 'ONLINE' ? 'En ligne' : 'En course'}
                            </span>
                        </div>
                    `
                });

                marker.addListener('click', () => {
                    infoWindow.open(demandMap, marker);
                });
            }
        });

        const bounds = new google.maps.LatLngBounds();
        zones.forEach(zone => {
            bounds.extend(new google.maps.LatLng(zone.lat, zone.lng));
        });
        demandMap.fitBounds(bounds);

    } catch (error) {
        console.error('Erreur chargement zones:', error);
        const mapDiv = document.getElementById('demandMap');
        if (mapDiv) {
            mapDiv.innerHTML = `
                <div style="height: 100%; display: flex; align-items: center; justify-content: center; flex-direction: column; background: #0a0e27; color: #00ffff;">
                    <i class="fas fa-map-marked-alt fa-4x"></i>
                    <p class="mt-3">Erreur de chargement: ${error.message}</p>
                    <button class="btn btn-sm btn-primary mt-2" onclick="refreshDemandMap()">Réessayer</button>
                </div>
            `;
        }
    }
}

function refreshDemandMap() {
    loadDemandData();
}

function startDemandAutoRefresh() {
    if (demandRefreshInterval) clearInterval(demandRefreshInterval);
    demandRefreshInterval = setInterval(() => {
        loadDemandData();
    }, 60000);
}

// ==================== COURSES PROGRAMMÉES ====================
async function loadScheduledRides() {
    const content = document.getElementById('content');

    try {
        const response = await fetch(`${API_BASE_URL_TAXI}/scheduled/admin/all`);
        const rides = await response.json();

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
                                    <tr>
                                        <td>#${ride.id}</td>
                                        <td>${ride.clientName || '-'}</td>
                                        <td>${ride.clientPhone || '-'}</td>
                                        <td>${ride.pickupAddress || '-'}</td>
                                        <td>${ride.destinationAddress || '-'}</td>
                                        <td>${new Date(ride.scheduledDateTime).toLocaleString()}</td>
                                        <td>${ride.estimatedPrice || 0} FCFA\n
                                        <td>${statusBadge}\n
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

// ==================== TAXI PUB ====================
async function loadAdvertising() {
    const content = document.getElementById('content');
    if (!content) return;

    content.innerHTML = '<div class="loading"><i class="fas fa-spinner fa-spin"></i><p>Chargement...</p></div>';

    try {
        const response = await fetch(`${API_BASE_URL}/advertising/admin/all`);
        let data = await response.json();

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
                                    <td>${ad.price.toLocaleString()} FCFA\n
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
                    <table>
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
    if (notes === null) return;

    try {
        const response = await fetch(`${API_BASE_URL}/advertising/admin/validate/${adId}?adminNotes=${encodeURIComponent(notes)}`, {
            method: 'PUT'
        });
        if (response.ok) {
            alert('✅ Paiement espèces validé. La publicité sera imprimée et posée sur les taxis.');
            loadAdvertising();
        } else {
            const err = await response.text();
            alert('❌ Erreur lors de la validation : ' + err);
        }
    } catch (error) {
        alert('Erreur réseau : ' + error.message);
    }
}

// ==================== GESTION DES LICENCES ====================
let licensesChart;

async function loadLicenses() {
    const content = document.getElementById('content');
    if (!content) return;

    content.innerHTML = `
        <div class="row">
            <div class="col-md-12">
                <div class="card mb-4">
                    <div class="card-header bg-primary text-white">
                        <i class="fas fa-key"></i> Générer une licence chauffeur
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-4">
                                <label class="form-label">Type de licence</label>
                                <select class="form-select" id="licenseType">
                                    <option value="TRIAL">🎁 Essai (7 jours - Gratuit)</option>
                                    <option value="1_YEAR">📅 1 an - 1000 FCFA</option>
                                    <option value="2_YEARS">📅 2 ans - 1900 FCFA</option>
                                    <option value="3_YEARS">📅 3 ans - 2800 FCFA</option>
                                    <option value="4_YEARS">📅 4 ans - 3700 FCFA</option>
                                    <option value="5_YEARS">📅 5 ans - 4500 FCFA</option>
                                    <option value="PERPETUAL">♾️ Perpétuelle - 10000 FCFA</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Application</label>
                                <select class="form-select" id="licenseAppType">
                                    <option value="DRIVER">🚖 Chauffeur</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Chauffeur (optionnel)</label>
                                <select class="form-select" id="licenseUserId">
                                    <option value="">-- Aucun (licence non attribuée) --</option>
                                </select>
                            </div>
                        </div>
                        <div class="row mt-3">
                            <div class="col-md-12">
                                <button class="btn btn-success w-100" onclick="generateLicense()">
                                    <i class="fas fa-plus-circle"></i> Générer la licence chauffeur
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-md-12">
                <div class="card">
                    <div class="card-header bg-info text-white d-flex justify-content-between align-items-center">
                        <span><i class="fas fa-list"></i> Liste des licences chauffeurs</span>
                        <button class="btn btn-sm btn-light" onclick="refreshLicensesList()">
                            <i class="fas fa-sync-alt"></i> Rafraîchir
                        </button>
                    </div>
                    <div class="card-body">
                        <div class="table-responsive">
                            <table class="table table-striped" id="licensesTable">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Clé</th>
                                        <th>Type</th>
                                        <th>Application</th>
                                        <th>Utilisateur</th>
                                        <th>Créée le</th>
                                        <th>Expiration</th>
                                        <th>Statut</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody id="licensesTableBody">
                                    <tr><td colspan="9" class="text-center">Chargement des licences...</td></tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-4">
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <i class="fas fa-key fa-3x text-primary"></i>
                        <h3 id="statTotalLicenses">0</h3>
                        <p>Total licences</p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <i class="fas fa-check-circle fa-3x text-success"></i>
                        <h3 id="statActiveLicenses">0</h3>
                        <p>Licences actives</p>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card text-center">
                    <div class="card-body">
                        <i class="fas fa-chart-line fa-3x text-warning"></i>
                        <h3 id="statTotalRevenue">0 FCFA</h3>
                        <p>Revenu total</p>
                    </div>
                </div>
            </div>
        </div>

        <div class="row mt-4">
            <div class="col-md-12">
                <div class="card">
                    <div class="card-header">
                        <i class="fas fa-chart-pie"></i> Statistiques des licences
                    </div>
                    <div class="card-body">
                        <canvas id="licensesStatsChart" height="100"></canvas>
                    </div>
                </div>
            </div>
        </div>
    `;

    await loadUsersForSelect();
    await refreshLicensesList();
    await loadLicenseStats();
}

async function loadUsersForSelect() {
    try {
        // ✅ Ne charger que les chauffeurs (pas les clients)
        const driversRes = await fetch(`${API_BASE_URL}/drivers`);
        const drivers = await driversRes.json();

        const select = document.getElementById('licenseUserId');
        if (!select) return;

        select.innerHTML = '<option value="">-- Aucun (licence non attribuée) --</option>';

        if (drivers && drivers.length) {
            drivers.forEach(d => {
                const option = document.createElement('option');
                option.value = `DRIVER_${d.id}`;
                option.textContent = `${d.fullName || d.email || 'Chauffeur'} (ID: ${d.id})`;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Erreur chargement chauffeurs:', error);
    }
}

async function generateLicense() {
    const licenseType = document.getElementById('licenseType').value;
    const appType = "DRIVER"; // ✅ Forcé à DRIVER
    const userIdRaw = document.getElementById('licenseUserId').value;

    let userId = null;
    let userType = "DRIVER";

    if (userIdRaw) {
        const parts = userIdRaw.split('_');
        userType = parts[0];
        userId = parseInt(parts[1]);
    }

    const durationMap = {
        'TRIAL': 7,
        '1_YEAR': 365,
        '2_YEARS': 730,
        '3_YEARS': 1095,
        '4_YEARS': 1460,
        '5_YEARS': 1825,
        'PERPETUAL': -1
    };

    const priceMap = {
        'TRIAL': 0,
        '1_YEAR': 1000,
        '2_YEARS': 1900,
        '3_YEARS': 2800,
        '4_YEARS': 3700,
        '5_YEARS': 4500,
        'PERPETUAL': 10000
    };

    const durationDays = durationMap[licenseType];
    const price = priceMap[licenseType];

    if (!confirm(`💰 Génération de licence chauffeur\n\nType: ${licenseType}\nPrix: ${price} FCFA\nDurée: ${durationDays === -1 ? 'Perpétuelle' : durationDays + ' jours'}\n\nContinuer ?`)) {
        return;
    }

    const btn = event.target;
    const originalText = btn.innerHTML;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Génération...';
    btn.disabled = true;

    try {
        const response = await fetch(`${API_BASE_URL}/licenses/generate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                licenseType: licenseType,
                durationDays: durationDays,
                price: price,
                appType: appType,
                userId: userId,
                userType: userType
            })
        });

        if (response.ok) {
            const data = await response.json();
            alert(`✅ Licence chauffeur générée avec succès !\n\nClé: ${data.licenseKey}\nPrix: ${data.price} FCFA\nExpiration: ${data.endDate || 'Perpétuelle'}`);
            await refreshLicensesList();
            await loadLicenseStats();
            document.getElementById('licenseType').value = '1_YEAR';
            document.getElementById('licenseUserId').value = '';
        } else {
            const error = await response.text();
            alert('❌ Erreur: ' + error);
        }
    } catch (error) {
        alert('Erreur: ' + error.message);
    } finally {
        btn.innerHTML = originalText;
        btn.disabled = false;
    }
}

async function refreshLicensesList() {
    try {
        const response = await fetch(`${API_BASE_URL}/licenses/all`);
        const licenses = await response.json();

        const tbody = document.getElementById('licensesTableBody');
        if (!tbody) {
            console.error('Table body not found');
            return;
        }

        if (!licenses || !licenses.length) {
            tbody.innerHTML = '<tr><td colspan="9" class="text-center">Aucune licence trouvée</td></tr>';
            return;
        }

        tbody.innerHTML = licenses.map(license => {
            let statusBadge = '';
            let statusColor = '';

            if (license.status === 'ACTIVE') {
                statusBadge = '<span class="badge bg-success">🟢 Active</span>';
                statusColor = 'success';
            } else if (license.status === 'EXPIRED') {
                statusBadge = '<span class="badge bg-danger">🔴 Expirée</span>';
                statusColor = 'danger';
            } else if (license.status === 'REVOKED') {
                statusBadge = '<span class="badge bg-dark">⚫ Révoquée</span>';
                statusColor = 'dark';
            } else {
                statusBadge = '<span class="badge bg-secondary">Inactive</span>';
                statusColor = 'secondary';
            }

            const endDateDisplay = license.endDate ? new Date(license.endDate).toLocaleDateString() : (license.durationDays === -1 ? '♾️ Perpétuelle' : '-');

            const typeLabels = {
                'TRIAL': '🎁 Essai 7j',
                '1_YEAR': '📅 1 an',
                '2_YEARS': '📅 2 ans',
                '3_YEARS': '📅 3 ans',
                '4_YEARS': '📅 4 ans',
                '5_YEARS': '📅 5 ans',
                'PERPETUAL': '♾️ Perpétuelle'
            };

            // ✅ Afficher correctement le nom du chauffeur
            const userName = license.userName || license.userEmail || '-';

            return `
                <tr class="license-${statusColor}">
                    <td>${license.id}</td>
                    <td><code style="background: #1a1a2e; padding: 4px 8px; border-radius: 6px; color: #00e676;">${license.licenseKey}</code></td>
                    <td>${typeLabels[license.licenseType] || license.licenseType}</td>
                    <td>🚖 Chauffeur</td>
                    <td>${userName}</td>
                    <td>${new Date(license.createdAt).toLocaleDateString()}</td>
                    <td>${endDateDisplay}</td>
                    <td>${statusBadge}</td>
                    <td>
                        <button class="btn btn-sm btn-danger" onclick="revokeLicense(${license.id})" title="Révoquer">
                            <i class="fas fa-ban"></i>
                        </button>
                        <button class="btn btn-sm btn-info" onclick="copyLicenseKey('${license.licenseKey}')" title="Copier la clé">
                            <i class="fas fa-copy"></i>
                        </button>
                    </td>
                </tr>
            `;
        }).join('');

    } catch (error) {
        console.error('Erreur chargement licences:', error);
        const tbody = document.getElementById('licensesTableBody');
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="9" class="text-center text-danger">Erreur: ${error.message}</td></tr>`;
        }
    }
}

async function loadLicenseStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/licenses/stats`);
        const stats = await response.json();

        const totalEl = document.getElementById('statTotalLicenses');
        const activeEl = document.getElementById('statActiveLicenses');
        const revenueEl = document.getElementById('statTotalRevenue');

        if (totalEl) totalEl.textContent = stats.total || 0;
        if (activeEl) activeEl.textContent = stats.active || 0;
        if (revenueEl) revenueEl.textContent = (stats.totalRevenue || 0).toLocaleString() + ' FCFA';

        const ctx = document.getElementById('licensesStatsChart');
        if (ctx && stats.byType) {
            if (licensesChart) licensesChart.destroy();

            const labels = Object.keys(stats.byType);
            const data = Object.values(stats.byType);
            const typeColors = {
                'TRIAL': '#FF9800',
                '1_YEAR': '#2196F3',
                '2_YEARS': '#9C27B0',
                '3_YEARS': '#FF5722',
                '4_YEARS': '#E91E63',
                '5_YEARS': '#00BCD4',
                'PERPETUAL': '#4CAF50'
            };

            licensesChart = new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: labels.map(l => {
                        const labelsMap = {
                            'TRIAL': 'Essai 7j',
                            '1_YEAR': '1 an',
                            '2_YEARS': '2 ans',
                            '3_YEARS': '3 ans',
                            '4_YEARS': '4 ans',
                            '5_YEARS': '5 ans',
                            'PERPETUAL': 'Perpétuelle'
                        };
                        return labelsMap[l] || l;
                    }),
                    datasets: [{
                        data: data,
                        backgroundColor: labels.map(l => typeColors[l] || '#00E676'),
                        borderWidth: 0
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    plugins: {
                        legend: { position: 'bottom' }
                    }
                }
            });
        }
    } catch (error) {
        console.error('Erreur stats licences:', error);
    }
}

async function revokeLicense(licenseId) {
    if (!confirm('⚠️ Révoquer cette licence ? L\'utilisateur perdra l\'accès immédiatement.')) return;

    try {
        const response = await fetch(`${API_BASE_URL}/licenses/revoke/${licenseId}`, {
            method: 'PUT'
        });

        if (response.ok) {
            alert('✅ Licence révoquée');
            await refreshLicensesList();
            await loadLicenseStats();
        } else {
            alert('❌ Erreur lors de la révocation');
        }
    } catch (error) {
        alert('Erreur: ' + error.message);
    }
}

function copyLicenseKey(key) {
    navigator.clipboard.writeText(key);
    alert('🔑 Clé copiée : ' + key);
}

// Exposer les fonctions globalement
window.loadLicenses = loadLicenses;
window.generateLicense = generateLicense;
window.revokeLicense = revokeLicense;
window.copyLicenseKey = copyLicenseKey;
window.refreshLicensesList = refreshLicensesList;