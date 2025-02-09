document.addEventListener("DOMContentLoaded", function() {
    if (window.errorMessage && window.errorMessage.trim() !== "") {
        // Crea l'elemento alert
        var alertDiv = document.createElement("div");
        alertDiv.className = "alert alert-danger alert-dismissible";
        alertDiv.setAttribute("role", "alert");
        alertDiv.innerHTML = '<strong>Errore:</strong> ' + window.errorMessage +
            '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>';

        // Cerca un contenitore per l'alert, se definito; altrimenti lo inserisce sopra il modal
        var container = document.getElementById("errorContainer");
        if (container) {
            container.appendChild(alertDiv);
        } else {
            var modalElement = document.getElementById("setpasswordModal");
            if (modalElement) {
                modalElement.parentNode.insertBefore(alertDiv, modalElement);
            } else {
                document.body.insertBefore(alertDiv, document.body.firstChild);
            }
        }

        // Apre automaticamente il modal
        var modalElement = document.getElementById("setpasswordModal");
        if (modalElement) {
            var modal = new bootstrap.Modal(modalElement);
            modal.show();
        }
    }
});


document.addEventListener("DOMContentLoaded", function() {
    // Recupera i parametri della URL
    const urlParams = new URLSearchParams(window.location.search);
    
    // Se il parametro "success" è uguale a "true", crea l'alert
    if (urlParams.get('success') === "true") {
        // Crea l'elemento alert
        var alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-success alert-dismissible';
        alertDiv.setAttribute("role", "alert");
        alertDiv.innerHTML = 'Cambio password avvenuto con successo!' +
            '<button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>';
        
        // Cerca un contenitore specifico per l'alert 
        var container = document.getElementById("successContainer");
        if (container) {
            container.appendChild(alertDiv);
        } else {
            // In alternativa, inserisci l'alert all'inizio del body
            document.body.insertBefore(alertDiv, document.body.firstChild);
        }
    }
});

