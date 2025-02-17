document.addEventListener("DOMContentLoaded", function() {
    /**
     * Input per la ricerca del codice della proposta.
     * @type {HTMLInputElement}
     */
    const searchInput = document.getElementById('order-search');
    
    /**
     * Select per il filtro in base allo stato.
     * @type {HTMLSelectElement}
     */
    const filterSelect = document.getElementById('order-status');
    
    /**
     * NodeList degli elementi ordine (con classe "order-container").
     * @type {NodeList}
     */
    const orders = document.querySelectorAll('.order-container');
    
    /**
     * Filtra gli ordini in base al termine di ricerca e allo stato selezionato.
     */
    function filterOrders() {
      // Valore di ricerca in minuscolo
      const searchTerm = searchInput.value.toLowerCase();
      // Stato selezionato
      const selectedStatus = filterSelect.value;
      
      orders.forEach(function(order) {
        // Ottieni il codice della proposta e lo stato dall'attributo data
        const codice = order.getAttribute('data-codice');
        const status = order.getAttribute('data-status');
        
        // Controlla se il codice contiene il termine cercato
        const matchCode = codice.includes(searchTerm);
        // Controlla se lo stato corrisponde o se il filtro è impostato su "tutti"
        const matchStatus = (selectedStatus === 'tutti' || status === selectedStatus);
        
        // Mostra o nascondi l'ordine in base ai filtri
        if (matchCode && matchStatus) {
          order.style.display = '';
        } else {
          order.style.display = 'none';
        }
      });
    }
    
    // Aggiungi il listener per il campo di ricerca
    searchInput.addEventListener('input', filterOrders);
    
    // Aggiungi il listener per il select di filtro stato
    filterSelect.addEventListener('change', filterOrders);
  });
  