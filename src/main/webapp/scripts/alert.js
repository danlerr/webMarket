

function showNotification(message, type = 'success', duration = 5000) {
    // 1. Crea l'elemento HTML della notifica
    const notificationDiv = document.createElement('div');
    
    notificationDiv.classList.add('alert', `alert-${type}`, 'alert-dismissible');
    notificationDiv.setAttribute('role', 'alert');

     
    notificationDiv.innerHTML = `
        <div>${message}</div>
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;


    // 2. Aggiungi la notifica al contenitore
    const container = document.getElementById('notification-container');
    container.appendChild(notificationDiv);

    


    // 4. Nascondi la notifica dopo un certo tempo 
    setTimeout(() => {
       notificationDiv.remove();
    }, duration);
}


// Funzione helper per mostrare notifiche da parametri URL
function checkUrlForNotifications() {
    const urlParams = new URLSearchParams(window.location.search);
    const successMessage = urlParams.get('success');
    const errorMessage = urlParams.get('error');

    if (successMessage) {
        showNotification(decodeURIComponent(successMessage), 'success');
    }
    if (errorMessage) {
        showNotification(decodeURIComponent(errorMessage), 'danger'); // o 'warning', 'info', etc.
    }
}

// Esegui la funzione helper al caricamento della pagina
document.addEventListener('DOMContentLoaded', checkUrlForNotifications);